package pt.tecnico.meditrack;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;


public class Database {

    private final static int PORT = 5555;

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
            InputStream is = null;
            OutputStream os = null;
            try (Socket socket = listener.accept()) {
                while (true) {
                    try {
                        int len = -1;
                        byte[] data = new byte[2048];
                        while (len == -1 ) {
                            is = new BufferedInputStream(socket.getInputStream());
                            len = is.read(data);
                        }

                        String message = new String(data, 0, len);
                        System.out.printf("server received %d bytes: %s%n", len, message);

                        JsonObject clientJson = JsonParser.parseString(message).getAsJsonObject();
                        String query = clientJson.get("value").getAsString();

                        String response = handleQuery(query);
                        System.out.println("response: " + response);

                        os = new BufferedOutputStream(socket.getOutputStream());
                        os.write(response.getBytes(), 0, response.getBytes().length);
                        os.flush();
                        

                    } catch (IOException i) {
                        System.out.println(i);
                        return;
                    }
                }
            } catch (IOException i) {
                System.out.println(i);
                return;
            }
        }
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
                return getData(query).toString();
            }
    }

    public static JsonObject getData(String query) throws IOException{

        String jdbcUrl = "jdbc:sqlite:src/main/java/pt/tecnico/meditrack/meditrack.db";
        JsonObject data = new JsonObject();
        JsonArray resultArray = new JsonArray();

        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement()) {

            System.out.println("Query received: " + query);
            ResultSet resultSet = statement.executeQuery(query);
            ResultSetMetaData metaData = resultSet.getMetaData();

            while (resultSet.next()) {
                JsonObject rowObject = new JsonObject();

                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    String columnName = metaData.getColumnName(i);
                    Object columnValue = resultSet.getObject(i);
                    rowObject.addProperty(columnName, columnValue.toString());
                }

                resultArray.add(rowObject);                    
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        data.add("value", resultArray);
        return data;
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
            return -1; // or handle the error in an appropriate way
        }
    }
}
