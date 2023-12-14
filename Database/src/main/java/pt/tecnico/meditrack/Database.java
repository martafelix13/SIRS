package pt.tecnico.meditrack;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;



public class Database {

    private final static int PORT = 5555;

    public static void main(String[] args) throws IOException{

        System.out.println(System.getProperty("user.dir"));


            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Server is listening on port " + PORT);
    
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Connection accepted from client");
    
                    /// Read data from the client
                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String clientMessage = reader.readLine();
                    System.out.println("Received from client: " + clientMessage);
                    
                    JsonObject clientJson = JsonParser.parseString(clientMessage).getAsJsonObject();
                    String query = clientJson.get("value").getAsString();

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
                    } else {
                        data = getData(query);

                    }

                    // Send a response to the client
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                    writer.write(data.toString());
                    writer.newLine();
                    writer.flush();

                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
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


    public static JsonObject getData(String query) throws IOException{

        String jdbcUrl = "jdbc:sqlite:src/main/java/pt/tecnico/meditrack/meditrack.db";
        JsonObject data = new JsonObject();

        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement()) {

            
            System.out.println("Query received: " + query);
            ResultSet resultSet = statement.executeQuery(query);

            do {
                for (int i = 2; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    String columnName = resultSet.getMetaData().getColumnName(i);
                    Object columnValue = resultSet.getObject(i);
                    System.out.println(columnName + ':' + columnValue);
                    data.addProperty(columnName, columnValue.toString());
                }
            } while (resultSet.next());


        } catch (Exception e) {
            e.printStackTrace();
        }
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
