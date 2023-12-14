package pt.tecnico.meditrack;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
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
                    JsonObject data = getData(query);

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


    public static JsonObject getData(String query) throws IOException{

        String jdbcUrl = "jdbc:sqlite:src/main/java/pt/tecnico/meditrack/meditrack.db";
        JsonObject data = new JsonObject();

        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement()) {

            
            System.out.println("Query received: " + query);
            ResultSet resultSet = statement.executeQuery(query);

            for (int i = 2; i <= resultSet.getMetaData().getColumnCount(); i++) {
                String columnName = resultSet.getMetaData().getColumnName(i);
                Object columnValue = resultSet.getObject(i);
                System.out.println(columnName + ':' + columnValue);
                data.addProperty(columnName, columnValue.toString());
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

}
