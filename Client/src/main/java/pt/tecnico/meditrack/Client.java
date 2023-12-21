package pt.tecnico.meditrack;
//import pt.tecnico.meditrack.SecureDocument;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.net.ssl.HttpsURLConnection;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import netscape.javascript.JSException;
public class Client {
    private static final String API_URL = "https://192.168.2.10:443/api";


    private static boolean authenticated = false;
    private static String PatientName;
    private static String DoctorName;

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
                        
                        sendPatientAuthenticationRequest(username);
                        while (!authenticated) {
                            
                        }
                        authenticated = false;
                        PatientName = username;
                        printClientMenu();
                        int clientChoice = getUserChoice(scanner);
                        switch (clientChoice) {
                            case 1:
                                sendClientViewRequest(PatientName);
                                
                                break;
                            case 2:
                                System.out.println("Enter doctor name: ");
                                String doctorName = scanner.next();
                                sendClientGiveDoctorAccessRequest(PatientName, doctorName);
                                break;

                            case 3:
                                sendClientDeleteRequest(PatientName);
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
                        System.out.println("Enter your username: ");
                        String username = scanner.next();
                        
                        sendDoctorAuthenticationRequest(username);
                        while (!authenticated) {
                            
                        }
                        authenticated = false;
                        DoctorName = username;
                        printDoctorMenu();
                        int doctorChoice = getUserChoice(scanner);
                        switch (doctorChoice) {
                            case 1:
                                System.out.println("Enter patient name: ");
                                String patientName = scanner.next();
                                sendDoctorViewRequest(DoctorName, patientName);
                                break;
                            case 2:
                                
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
                                sendDoctorCreateConsultationRequest(DoctorName, patientName1, date, speciality, practice, treatmentSummary);
                                break;
                            case 3:
                                
                                System.out.println("Enter new speciality: ");
                                String newSpeciality = scanner.next();
                                sendDoctorChangeSpecialityRequest(DoctorName, newSpeciality);
                                break;

                            case 4:
                                System.out.println("Activating SOS Mode");
                                sendDoctorSOSRequest(username);
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
        System.out.println("4. Activate SOS Mode");
        System.out.println("5. Exit");
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


    private static void sendPatientAuthenticationRequest(String username) {

        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty("user", "patient");
        jsonPayload.addProperty("command", "authPatient");

        JsonObject payload = new JsonObject();
        payload.addProperty("username", username);

        //load public key from /keys

        try {
            byte[] publicKeyBytes = readFile("keys/patient.pubkey");   

            // Base64 encode the key
            String base64EncodedKey = Base64.getEncoder().encodeToString(publicKeyBytes);
            payload.addProperty("publicKey", base64EncodedKey);
            jsonPayload.addProperty("payload", payload.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
           
            String jsonRequest =  protectJsonClient(jsonPayload,"patient");
            // Send the JSON request to the server
            try {
                sendJsonRequestForAuth(jsonRequest,username,"patient");
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            
            e.printStackTrace();
        }
    }

    private static void sendDoctorAuthenticationRequest(String username) {

        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty("user", "doctor");
        jsonPayload.addProperty("command", "authDoctor");

        JsonObject payload = new JsonObject();
        payload.addProperty("username", username);

        //load public key from /keys

        try {
            byte[] publicKeyBytes = readFile("keys/doctor.pubkey");   

            // Base64 encode the key
            String base64EncodedKey = Base64.getEncoder().encodeToString(publicKeyBytes);
            payload.addProperty("publicKey", base64EncodedKey);
            jsonPayload.addProperty("payload", payload.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

         try {
           
            String jsonRequest =  protectJsonClient(jsonPayload,"doctor");
            // Send the JSON request to the server
            try {
                sendJsonRequestForAuth(jsonRequest,username,"doctor");
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            
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
        

        //Load public key from /keys
        
        try {
            
            String jsonRequest = protectJsonClient(jsonPayload,"patient");
            // Send the JSON request to the server
            try {
                sendJsonRequest(jsonRequest,"patient");
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            
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
        try {
            
            String jsonRequest = protectJsonClient(jsonPayload,"patient");
            // Send the JSON request to the server
            try {
                sendJsonRequest(jsonRequest, "patient");
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            
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
        try {
            
            String jsonRequest = protectJsonClient(jsonPayload,"patient");
            // Send the JSON request to the server
            try {
                sendJsonRequest(jsonRequest,"patient");
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            
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
        try {
            
            String jsonRequest = protectJsonClient(jsonPayload,"doctor");
            // Send the JSON request to the server
            try {
                sendJsonRequest(jsonRequest,"doctor");
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            
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
        try {
            
            String jsonRequest = protectJsonClient(jsonPayload,"doctor");
            // Send the JSON request to the server
            try {
                sendJsonRequest(jsonRequest,"doctor");
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            
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
        payload.addProperty("speciality", newSpeciality);

        // Convert the payload to a JSON string and add it to the main JSON object
        jsonPayload.addProperty("payload", payload.toString());

        // Convert the main JSON object to a string
        try {
            
            String jsonRequest = protectJsonClient(jsonPayload,"doctor");
            // Send the JSON request to the server
            try {
                sendJsonRequest(jsonRequest,"doctor");
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            
            e.printStackTrace();
        }
    }


    private static void sendDoctorSOSRequest(String doctorName) {
        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty("user", "doctor");
        jsonPayload.addProperty("command", "sosMode");

        JsonObject payload = new JsonObject();
        payload.addProperty("doctorName", doctorName);

        jsonPayload.addProperty("payload", payload.toString());

        try {
            
            String jsonRequest = protectJsonClient(jsonPayload,"doctor");
            // Send the JSON request to the server
            try {
                sendJsonRequest(jsonRequest,"doctor");
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            
            e.printStackTrace();
        }
    }



    private static void sendChallengeResponse(String username, String challengeResponse, String user) {
        // Construct your JSON request payload
        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty("user", "patient");
        jsonPayload.addProperty("command", "validateAuth");

        // Create a nested JSON object for the payload
        JsonObject payload = new JsonObject();
        payload.addProperty("username", username);
        payload.addProperty("challengeResponse", challengeResponse);

        // Convert the payload to a JSON string and add it to the main JSON object
        jsonPayload.addProperty("payload", payload.toString());

         try {

            if(user.equals("patient")){
                String jsonRequest =  protectJsonClient(jsonPayload,"patient");
                sendJsonRequestValidation(jsonRequest, "patient");
            }
            else if(user.equals("doctor")){
                String jsonRequest =  protectJsonClient(jsonPayload,"doctor");
                sendJsonRequestValidation(jsonRequest, "doctor");
            }
            else{
                System.out.println("Invalid user");
            }
            
            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    //////////////// JSON Requests for Auth ////////////////

    private static void sendJsonRequestForAuth(String jsonRequest, String username, String user) throws Exception {
        URL url = new URL(API_URL);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        try {
           
            // Set up the HTTP request
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Connection", " keep-alive");
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
                //System.out.println(response.toString());

                JsonObject responseJson =  JsonParser.parseString(response.toString()).getAsJsonObject();

               //decrypt the response
                JsonObject decryptJson = unprotectJsonClient(responseJson,user);

                JsonObject content = JsonParser.parseString(decryptJson.get("content").getAsString()).getAsJsonObject();

                // Get the payload from the response
                
                
                String challenge = content.get("challenge").getAsString();
                //System.out.println("Challenge:" + challenge);

                PrivateKey privKey = null;
                
                if (user.equals("patient")){
                    privKey = readPrivateKey("keys/patient.privkey");
                }
                else if (user.equals("doctor")){
                    privKey = readPrivateKey("keys/doctor.privkey");
                } 

                // Decrypting the challenge
                byte[] dencryptChallenge = Base64.getDecoder().decode(challenge);
                byte[] challengeResponse = decryptRSAWithPrivateKey(dencryptChallenge, privKey);
                //System.out.println("Decrypted Challenge: " + new String(challengeResponse));

                // Send the challenge response to the server
                sendChallengeResponse(username, new String(challengeResponse), user);


                
            }
            
            //connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }
    }

    

    //////////////// JSON Requests ////////////////

    private static void sendJsonRequest(String jsonRequest, String user) throws IOException {
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

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
               

                JsonObject responseJson =  JsonParser.parseString(response.toString()).getAsJsonObject();

               //decrypt the response
                JsonObject decryptJson = unprotectJsonClient(responseJson,user);

                JsonObject content = JsonParser.parseString(decryptJson.get("content").getAsString()).getAsJsonObject();

                // content to String
                StringBuilder contentString = new StringBuilder();
                contentString.append(content.toString());

                //pretty print
                String prettyJson = prettyPrintJson(contentString.toString());
                
                

                System.out.println("Server Response: " +'\n' +  prettyJson);
            }
            
            //connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }
    }

    private static void sendJsonRequestValidation(String jsonRequest, String user) throws IOException {
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
                
                JsonObject responseJson =  JsonParser.parseString(response.toString()).getAsJsonObject();

               //decrypt the response
                JsonObject decryptJson = unprotectJsonClient(responseJson,user);

                JsonObject content = JsonParser.parseString(decryptJson.get("content").getAsString()).getAsJsonObject();

                // content to String
                StringBuilder contentString = new StringBuilder();
                contentString.append(content.toString());
                //check if response is true
                checkresponse(contentString);

                //System.out.println("Server Response: " + response.toString());
            }
            
            //connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }
    }

    public static void checkresponse(StringBuilder response){
        JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
        String response1 = jsonResponse.get("validation").getAsString();
        if(response1.equals("true")){
            System.out.println("Authentication successful");
            authenticated = true;
        }
        else{
            System.out.println("Operation failed");
        }
    }

    //////////////// Decryption ////////////////

    private static byte [] decryptRSAWithPrivateKey(byte[] content, PrivateKey privateKey) throws Exception{

    Cipher cipher = Cipher.getInstance("RSA");
    cipher.init(Cipher.DECRYPT_MODE, privateKey);
    return cipher.doFinal(content);
    }

    private static PrivateKey getRSAPrivateKey(String key) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return keyFactory.generatePrivate(spec);
    }

       private static PrivateKey readPrivateKey(String privateKeyPath) throws Exception {
        byte[] privEncoded = readFile(privateKeyPath);
        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privEncoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(privSpec);
    }

    private static PublicKey readPublicKey(String publicKeyPath) throws Exception {
        byte[] pubEncoded = readFile(publicKeyPath);
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(pubSpec);
    }


    //// Protect JSON ////

    private static String protectJsonClient (JsonObject jsonPayload, String user){
        // get keys
        PublicKey publicKey;
        PrivateKey privateKey = null;
        

        try{
             publicKey = readPublicKey("keys/api.pubkey");
       
            if (user.equals("patient")){
                privateKey = readPrivateKey("keys/patient.privkey");
            }
            else if (user.equals("doctor")){
                privateKey = readPrivateKey("keys/doctor.privkey");
            } 
            else{
                System.out.println("Invalid user");
            }
            
            String secretKey = "keys/secretKey.txt";
            //  Protect the payload
            return  SecureDocument.protectJson(jsonPayload, publicKey, privateKey, secretKey).toString();
        }
        catch (Exception e){
        
        }
        
        return null;
        

    }

    // Unprotect JSON //

    private static JsonObject unprotectJsonClient (JsonObject jsonPayload, String user){
        // get keys
        PublicKey publicKey;
        PrivateKey privateKey = null;
        

        try{
             publicKey = readPublicKey("keys/api.pubkey");
       
            if (user.equals("patient")){
                privateKey = readPrivateKey("keys/patient.privkey");
            }
            else if (user.equals("doctor")){
                privateKey = readPrivateKey("keys/doctor.privkey");
            } 
            else{
                System.out.println("Invalid user");
            }
            
            
            return  SecureDocument.unprotectJson(jsonPayload, privateKey, publicKey);
        }
        catch (Exception e){
        
        }
        
        return null;
        

    }



    public static String prettyPrintJson(String jsonString) {
        StringBuilder prettyJson = new StringBuilder();
        int indentation = 0;
        boolean inQuotes = false;

        for (char character : jsonString.toCharArray()) {
            
            switch (character) {
                case '\\':
                    continue;
                case '{':
                case '[':
                    prettyJson.append(character).append("\n").append(indentation(++indentation));
                    break;
                case '}':
                case ']':
                    prettyJson.append("\n").append(indentation(--indentation)).append(character);
                    break;
                case ',':
                    prettyJson.append(character).append("\n").append(inQuotes ? "" : indentation(indentation));
                    break;
                case '"':
                    inQuotes = !inQuotes;
                    prettyJson.append(character);
                    break;
                default:
                    prettyJson.append(character);
            }
        }
        
        return prettyJson.toString();
    }

    private static String indentation(int level) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < level; i++) {
            indent.append("  "); // Use two spaces for each level of indentation
        }
        return indent.toString();
    }
}
