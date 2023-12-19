package pt.tecnico.meditrack;
//import pt.tecnico.meditrack.SecureDocument;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.security.PublicKey;
import java.sql.Connection;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
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
                        // Client menu
                        // Authenticate the user
                        System.out.println("Enter your username: ");
                        String username = scanner.next();
                        
                        sendClientAuthenticationRequest(username);
                            
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


    private static void sendClientAuthenticationRequest(String username) {

        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty("user", "patient");
        jsonPayload.addProperty("command", "authPatient");

        JsonObject payload = new JsonObject();
        payload.addProperty("username", username);

        //load public key from /keys

        try {
            byte[] publicKeyBytes = readFile("pub/patient.pubkey");           
            String publicKeyString =  Base64.getEncoder().encodeToString(publicKeyBytes);
            payload.addProperty("publicKey", publicKeyString);
            jsonPayload.addProperty("payload", payload.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String jsonRequest = jsonPayload.toString();
            sendJsonRequest(jsonRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }     
    }

    private static byte[] readFile(String path) throws FileNotFoundException, IOException {
		FileInputStream fis = new FileInputStream(path);
		byte[] content = new byte[fis.available()];
		fis.read(content);
		fis.close();
		return content;
	}

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
        try {
            sendJsonRequest(jsonRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        try {
            sendJsonRequest(jsonRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        try {
            sendJsonRequest(jsonRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }    
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
        try {
            sendJsonRequest(jsonRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        try {
            sendJsonRequest(jsonRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        try {
            sendJsonRequest(jsonRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendJsonRequest(String jsonRequest) throws IOException {
        URL url = new URL(API_URL);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        try {
           
            // Set up the HTTP request
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            //connection.setRequestProperty("Connection", " keep-alive");
            connection.setDoOutput(true);

            // Send the JSON request payload
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonRequest.getBytes();
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
            
            //connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }
    }
}
