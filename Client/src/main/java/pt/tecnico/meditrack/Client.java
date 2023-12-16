package pt.tecnico.meditrack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import javax.net.ssl.HttpsURLConnection;
public class Client {
    private static final String API_URL = "https://localhost:5555/api";

    public static void main(String[] args) {

        System.setProperty("javax.net.ssl.keyStore", "certificates/client.p12");
        System.setProperty("javax.net.ssl.keyStorePassword", "changeme");
        System.setProperty("javax.net.ssl.trustStore", "certificates/clienttruststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeme");
    
        
        Scanner scanner = new Scanner(System.in);
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

        while (true) {
            printMenu();
            int choice = getUserChoice(scanner);

            switch (choice) {
                case 1:
                    while (true){
                        printClientMenu();
                        int clientChoice = getUserChoice(scanner);
                        switch (clientChoice) {
                            case 1:
                                System.out.println("Enter patient name: ");
                                String patientName = scanner.next();
                                sendClientRequest(patientName);
                                
                                break;
                            case 2:
                                System.out.println("Give Doctor Access");
                                break;
                            case 3:
                                System.out.println("Exiting to the main menu.");
                                break;
                                
                            default:
                                System.out.println("Invalid choice. Please try again.");
                        }
                        break;
                    }
                    break;
                    
                case 2:
                    while (true){
                        printDoctorMenu();
                        int doctorChoice = getUserChoice(scanner);
                        switch (doctorChoice) {
                            case 1:
                                System.out.println("View Patient History");
                                break;
                            case 2:
                                System.out.println("Exiting to the main menu.");
                                break;
                            default:
                                System.out.println("Invalid choice. Please try again.");
                        }
                        break;
                    }
                    break;

                    
                case 3:
                    System.out.println("Exiting the program. Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } 
    }


    //////////////// Menu ////////////////


    private static void printMenu() {
        System.out.println("===== Menu =====");
        System.out.println("1. Client Menu");
        System.out.println("2. Doctor Menu");
        System.out.println("3. Exit");
        System.out.println("=================");
    }

    private static void printClientMenu() {
        System.out.println("===== Client Menu =====");
        System.out.println("1. View Patient History");
        System.out.println("2. Give Doctor Access");
        System.out.println("3. Exit");
        System.out.println("=================");
       
    }

    private static void printDoctorMenu() {
        System.out.println("===== Doctor Menu =====");
        System.out.println("1. View Patient History");
        System.out.println("2. Exit");
        System.out.println("=================");
       
    }
    private static int getUserChoice(Scanner scanner) {
        System.out.print("Enter your choice: ");
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a number.");
            System.out.print("Enter your choice: ");
            scanner.next(); // consume invalid input
        }
        return scanner.nextInt();
    }



    //////////////// JSON Requests ////////////////


    private static void sendClientRequest(String patientName) {
        // Construct your JSON request payload
        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty("user", "patient");
        jsonPayload.addProperty("command", "getRegisters");

        // Create a nested JSON object for the payload
        JsonObject payload = new JsonObject();
        payload.addProperty("patientName", patientName);

        // Convert the payload to a JSON string and add it to the main JSON object
        jsonPayload.addProperty("payload", payload.toString());

        // Convert the main JSON object to a string
        String jsonRequest = jsonPayload.toString();

        // Send the JSON request to the server
        sendJsonRequest(jsonRequest);

        // Add more logic here based on the server's response
    }
    

    private static void sendDoctorRequest() {
        // Construct your JSON request payload
        String jsonRequest = "{\"action\":\"viewPatientHistory\"}";

        // Send the JSON request to the server
        sendJsonRequest(jsonRequest);

        // Add more logic here based on the server's response
    }


    private static void sendJsonRequest(String jsonRequest) {
        try {
            URL url = new URL(API_URL);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            // Set up the HTTP request
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Send the JSON request payload
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonRequest.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Read the server's response
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println("Server Response: " + response.toString());
            }

            // Close the connection
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}