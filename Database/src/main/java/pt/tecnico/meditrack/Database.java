package pt.tecnico.meditrack;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;


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
            String response = handleQuery(query);

            System.out.println("response: " + response);

            os.write(response.getBytes("UTF-8"), 0, response.getBytes("UTF-8").length);
            os.flush();
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
            jsonResult = convertMapListToJson(resultList);

            System.out.println(jsonResult);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonResult;
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

    private static String convertMapListToJson(List<Map<String, Object>> mapList) {
        // You need to implement a proper JSON converter based on the actual datatype
        // This is just a placeholder
        return mapList.toString();
    }
}
