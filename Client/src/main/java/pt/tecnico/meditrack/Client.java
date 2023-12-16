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
    private static final String API_URL = "https://localhost:433/api";

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
                                sendClientViewRequest(patientName);
                                
                                break;
                            case 2:
                                System.out.println("Enter patient name: ");
                                String patientName1 = scanner.next();
                                System.out.println("Enter doctor name: ");
                                String doctorName = scanner.next();
                                sendClientGiveDoctorAccessRequest(patientName1, doctorName);
                                break;

                            case 3:
                                System.out.println("Enter patient name: ");
                                String patientName2 = scanner.next();
                                sendClientDeleteRequest(patientName2);
                                break;
                            case 4:
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
                                System.out.println("Enter doctor name: ");
                                String doctorName = scanner.next();
                                System.out.println("Enter patient name: ");
                                String patientName = scanner.next();
                                sendDoctorViewRequest(doctorName, patientName);
                                break;
                            case 2:
                                System.out.println("Enter doctor name: ");
                                String doctorName1 = scanner.next();
                                System.out.println("Enter patient name: ");
                                String patientName1 = scanner.next();
                                System.out.println("Enter date: ");
                                String date = scanner.next();
                                System.out.println("Enter speciality: ");
                                String speciality = scanner.next();
                                System.out.println("Enter practice: ");
                                String practice = scanner.next();
                                System.out.println("Enter Treatment Summary: ");
                                String treatmentSummary = scanner.next();
                                sendDoctorCreateConsultationRequest(doctorName1, patientName1, date, speciality, practice, treatmentSummary);
                                break;
                            case 3:
                                System.out.println("Enter doctor name: ");
                                String doctorName2 = scanner.next();
                                System.out.println("Enter new speciality: ");
                                String newSpeciality = scanner.next();
                                sendDoctorChangeSpecialityRequest(doctorName2, newSpeciality);
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
        System.out.println("3. Delete Personal Information");
        System.out.println("4. Exit");
        System.out.println("=================");
       
    }

    private static void printDoctorMenu() {
        System.out.println("===== Doctor Menu =====");
        System.out.println("1. View Patient History");
        System.out.println("2. Create Consultation");
        System.out.println("3. Change Speciality");
        System.out.println("4. Exit");
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


    private static void sendClientViewRequest(String patientName) {
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

    private static void sendClientGiveDoctorAccessRequest(String patientName, String doctorName) {
        // Construct your JSON request payload
        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty("user", "patient");
        jsonPayload.addProperty("command", "allowAccessToAllRegisters");

        // Create a nested JSON object for the payload
        JsonObject payload = new JsonObject();
        payload.addProperty("patientName", patientName);
        payload.addProperty("doctorName", doctorName);

        // Convert the payload to a JSON string and add it to the main JSON object
        jsonPayload.addProperty("payload", payload.toString());

        // Convert the main JSON object to a string
        String jsonRequest = jsonPayload.toString();

        // Send the JSON request to the server
        sendJsonRequest(jsonRequest);

    }

    private static void sendClientDeleteRequest(String patientName) {
        // Construct your JSON request payload
        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty("user", "patient");
        jsonPayload.addProperty("command", "deletePersonalInformation");

        // Create a nested JSON object for the payload
        JsonObject payload = new JsonObject();
        payload.addProperty("patientName", patientName);

        // Convert the payload to a JSON string and add it to the main JSON object
        jsonPayload.addProperty("payload", payload.toString());

        // Convert the main JSON object to a string
        String jsonRequest = jsonPayload.toString();

        // Send the JSON request to the server
        sendJsonRequest(jsonRequest);
    }
    

    private static void sendDoctorViewRequest(String doctorName, String patientName) {
        // Construct your JSON request payload
        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty("user", "doctor");
        jsonPayload.addProperty("command", "getPatientsConsultations");

        // Create a nested JSON object for the payload
        JsonObject payload = new JsonObject();
        payload.addProperty("doctorName", doctorName);
        payload.addProperty("patientName", patientName);

        // Convert the payload to a JSON string and add it to the main JSON object
        jsonPayload.addProperty("payload", payload.toString());

        // Convert the main JSON object to a string
        String jsonRequest = jsonPayload.toString();

        // Send the JSON request to the server
        sendJsonRequest(jsonRequest);

        // Add more logic here based on the server's response
    }

    private static void sendDoctorCreateConsultationRequest(String doctorName, String patientName, String date, String speciality, String practice, String treatmentSummary) {
        // Construct your JSON request payload
        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty("user", "doctor");
        jsonPayload.addProperty("command", "createConsultation");

        // Create a nested JSON object for the payload
        JsonObject payload = new JsonObject();
        payload.addProperty("doctorName", doctorName);
        payload.addProperty("patientName", patientName);
        payload.addProperty("date", date);
        payload.addProperty("speciality", speciality);
        payload.addProperty("practice", practice);
        payload.addProperty("treatmentSummary", treatmentSummary);

        // Convert the payload to a JSON string and add it to the main JSON object
        jsonPayload.addProperty("payload", payload.toString());

        // Convert the main JSON object to a string
        String jsonRequest = jsonPayload.toString();

        // Send the JSON request to the server
        sendJsonRequest(jsonRequest);
    }

    private static void sendDoctorChangeSpecialityRequest(String doctorName, String newSpeciality) {
        // Construct your JSON request payload
        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty("user", "doctor");
        jsonPayload.addProperty("command", "changeSpeciality");

        // Create a nested JSON object for the payload
        JsonObject payload = new JsonObject();
        payload.addProperty("doctorName", doctorName);
        payload.addProperty("newSpeciality", newSpeciality);

        // Convert the payload to a JSON string and add it to the main JSON object
        jsonPayload.addProperty("payload", payload.toString());

        // Convert the main JSON object to a string
        String jsonRequest = jsonPayload.toString();

        // Send the JSON request to the server
        sendJsonRequest(jsonRequest);
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
