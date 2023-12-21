package pt.tecnico.meditrack;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;


public class Database {

    private final static int PORT = 3306;


    public static void main(String args[]) throws IOException {
        System.setProperty("javax.net.ssl.keyStore", "certificates/database.p12");
        System.setProperty("javax.net.ssl.keyStorePassword", "changeme");
        System.setProperty("javax.net.ssl.trustStore", "certificates/databasetruststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeme");

        startSecureSocketServer();
    }


    public static void startSecureSocketServer() throws IOException {

        ServerSocketFactory factory = SSLServerSocketFactory.getDefault();
        try (SSLServerSocket listener = (SSLServerSocket) factory.createServerSocket(PORT)) {
            listener.setNeedClientAuth(true);
            listener.setEnabledCipherSuites(new String[] { "TLS_AES_128_GCM_SHA256" });
            listener.setEnabledProtocols(new String[] { "TLSv1.3" });
            System.out.println("listening for messages on port " + PORT);

            while (true) {
                try (Socket socket = listener.accept()) {
                    System.out.println("Connected");
                    handleConnection(socket);
                } catch (IOException e) {
                    System.out.println("Error accepting connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Error creating server socket: " + e.getMessage());
        }
    }


    private static void handleConnection(Socket socket) {
        try (InputStream is = new BufferedInputStream(socket.getInputStream());
             OutputStream os = new BufferedOutputStream(socket.getOutputStream())) {
            int len = -1;
            byte[] data = new byte[2048];
            while (len == -1) { len = is.read(data);}

            String message = new String(data, 0, len);
            //System.out.printf("server received %d bytes: %s%n", len, message);

            JsonObject clientJson = JsonParser.parseString(message).getAsJsonObject();
            String query = clientJson.get("value").getAsString();
            System.out.println("query: " + query);
            String response = handleQuery(query);

            System.out.println("response: " + response);
            
            try (PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
                // Send JSON data to the server
                writer.println(response);
                writer.flush();
            }
        } catch (IOException e) {
            System.out.println("Error handling connection: " + e.getMessage());
        }
    }

    

    private static String readQueryFromFile(String filePath) throws IOException {
        StringBuilder query = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                query.append(line).append("\n");
            }
        }
        return query.toString();
    }
    

    private static String getMethod(String input) {
        String trimmedInput = input.trim();
        String[] words = trimmedInput.split("\\s+");

        if (words.length > 0) {
            return words[0];
        } else {
            return "";
        }
    }

    private static String handleQuery(String query) throws IOException {
        JsonObject data = new JsonObject();
       
            if (getMethod(query).equals("UPDATE") || getMethod(query).equals("DELETE") || getMethod(query).equals("INSERT")) {
                int response = updateData(query);
                if (response != -1) {
                    System.out.println("Operation was sucessfull.");
                    data.addProperty("state", "successful");
                } else {
                    System.out.println("Operation failed.");
                    data.addProperty("state", "failed");
                }
                return data.toString();
            } else {
                return getData(query);
            }
    }

    public static String getData(String query) throws IOException{

        String jdbcUrl = "jdbc:sqlite:src/main/java/pt/tecnico/meditrack/meditrack.db";
  
        String jsonResult = "";

        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query)) {

            List<Map<String, Object>> resultList = convertResultSetToMapList(resultSet);

            if (resultList.size() == 1) {
                Map<String, Object> result = resultList.get(0);
                jsonResult = convertMapToJson(result);
                System.out.println(jsonResult);
            } else {
                //jsonResult = convertMapListToJson(resultList);
            }

                
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonResult;
    }

    public static String convertMapToJson(Map<String, Object> map) {
        Gson gson = new Gson();
        String json = gson.toJson(map);
        return json;
    }

    public static int updateData(String query) throws IOException {
        String jdbcUrl = "jdbc:sqlite:src/main/java/pt/tecnico/meditrack/meditrack.db";
    
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement()) {
    
            System.out.println("Query received: " + query);
            int rowsAffected = statement.executeUpdate(query);
            System.out.println("Rows affected: " + rowsAffected);
            return rowsAffected;
    
        } catch (Exception e) {
            e.printStackTrace();
            return -1; 
        }
    }

    private static List<Map<String, Object>> convertResultSetToMapList(ResultSet resultSet) throws Exception {
        List<Map<String, Object>> resultList = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();

        while (resultSet.next()) {
            Map<String, Object> row = new HashMap<>();

            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String columnName = metaData.getColumnName(i);
                Object columnValue = resultSet.getObject(i);
                row.put(columnName, columnValue);
            }

            resultList.add(row);
        }

        return resultList;
    }


   /*  public static JsonObject organizePatientJsonResult(ResultSet resultSet) throws IOException, SQLException {
        // Convert the result set to JSON
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        while (resultSet.next()) {
            jsonObject.addProperty("name", resultSet.getString("name"));
            jsonObject.addProperty("sex", resultSet.getString("sex"));
            jsonObject.addProperty("dateOfBirth", resultSet.getString("dateOfBirth"));
            jsonObject.addProperty("bloodType", resultSet.getString("bloodType"));
            jsonObject.add("knownAllergies", new Gson().fromJson(resultSet.getString("knownAllergies"), JsonArray.class));
            JsonObject consultations = new JsonObject();
            consultations.addProperty("consultationDate", resultSet.getString("consultationDate"));
            consultations.addProperty("medicalSpeciality", resultSet.getString("medicalSpeciality"));
            consultations.addProperty("doctorName", resultSet.getString("doctorName"));
            consultations.addProperty("practice", resultSet.getString("practice"));
            consultations.addProperty("treatmentSummary", resultSet.getString("treatmentSummary"));
            jsonArray.add(consultations);
        }

        // Print or use the JSON as needed
        System.out.println(new Gson().toJson(jsonArray));
        jsonObject.add("consultations", jsonArray);
        return jsonObject;
    }

    public static String organizeDoctorJsonResult(ResultSet resultSet) throws JsonSyntaxException, SQLException {
        List<JsonObject> patientsJsonList = new ArrayList<>();
        while (resultSet.next()) {
            JsonObject patientJsonObject = new JsonObject();
            patientJsonObject.addProperty("name", resultSet.getString("name"));
            patientJsonObject.addProperty("sex", resultSet.getString("sex"));
            patientJsonObject.addProperty("dateOfBirth", resultSet.getString("dateOfBirth"));
            patientJsonObject.addProperty("bloodType", resultSet.getString("bloodType"));

            // Convert knownAllergies to JsonArray
            JsonArray knownAllergiesArray = new Gson().fromJson(resultSet.getString("knownAllergies"), JsonArray.class);
            patientJsonObject.add("knownAllergies", knownAllergiesArray);

            // Convert consultationRecords to JsonArray
            JsonArray consultationRecordsArray = new Gson().fromJson(resultSet.getString("consultationRecords"), JsonArray.class);
            patientJsonObject.add("consultationRecords", consultationRecordsArray);

            patientsJsonList.add(patientJsonObject);
        }

        // Convert the list of patients to a JSON array
        JsonArray patientsJsonArray = new JsonArray();
        patientsJsonList.forEach(patientsJsonArray::add);

        // Convert the JSON array to a string
        return patientsJsonArray.toString();
   
    } */
}
