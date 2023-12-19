package pt.tecnico.meditrack;
import pt.tecnico.meditrack.SecureDocument;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsServer;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.SocketFactory;
import javax.sound.sampled.Port;
import javax.net.ssl.*;

import java.io.FileInputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.Headers;



public class ApiMeditrack {

    private final static int PORT = 3306;
    private final static int PORT_HTTPS = 433;

    private final static String ADDRESS = "localhost";

    private final static String PATIENT = "patient";
    private final static String DOCTOR = "doctor";
    private final static String SOS = "sos";

    private final static int CHALLENGE_SIZE = 16;


    public static void main(String[] args) throws NoSuchAlgorithmException {
        try {
            // HTTPS server configuration
            HttpsServer httpsServer = configureHttpsServer();
            httpsServer.createContext("/api", exchange -> {
            
                System.out.println("Received connection from: " + exchange.getRemoteAddress());
                
                try (InputStream request = exchange.getRequestBody()) {
                    String requestBody = new String(request.readAllBytes(), StandardCharsets.UTF_8);
                    
                    System.out.println("Received JSON request: " + requestBody);
                    JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();
                    String response;

                    try {
                        response = processJsonRequest(jsonRequest);
                        exchange.sendResponseHeaders(200, response.length());

                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response.getBytes(StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch ( Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            httpsServer.start();
        
    
            System.out.println("Server is running on port " + PORT_HTTPS + "...");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            configSecureSockets();
            System.out.println("API is running on port " + PORT + "...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static HttpsServer configureHttpsServer() throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, KeyManagementException {
        // Specify the keystore and truststore file paths and passwords
        String keystoreFilePath = "certificates/server.p12";
        String keystorePassword = "changeme";
        String truststoreFilePath = "certificates/servertruststore.jks";
        String truststorePassword = "changeme";
    
        char[] keystorePasswordChars = keystorePassword.toCharArray();
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(keystoreFilePath), keystorePasswordChars);
    
        char[] truststorePasswordChars = truststorePassword.toCharArray();
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(new FileInputStream(truststoreFilePath), truststorePasswordChars);
    
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keystorePasswordChars);
    
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
    
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
    
        HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(PORT_HTTPS), 0);
        httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext));

        return httpsServer;
    }
    


    private static String processJsonRequest(JsonObject file) throws Exception {
        
        String user = file.get("user").getAsString();
        System.out.println("User from file: " + user);
        String response = "Internal server error";
        String query = "";
        if (user.equals(PATIENT)) {

            String command = file.get("command").getAsString();
            System.out.println("command from file: " + command);

            switch (command) {
                case "authPatient":
                    JsonObject payload = JsonParser.parseString(file.get("payload").getAsString()).getAsJsonObject();
                    //JsonObject payload = file.get("payload").getAsJsonObject();
                    String username = payload.get("username").getAsString();
                    String publicKeyString = payload.get("publicKey").getAsString();
                    response = handleAuthString(username, publicKeyString);

                    break;

                case "getRegisters":
                    payload = JsonParser.parseString(file.get("payload").getAsString()).getAsJsonObject();
                    String patient = payload.get("patientName").getAsString();
                    System.out.println("patientName from file: " + patient);
                    query = getPatientRegisters(patient);
                    System.out.println("query: " + query);
                    response =  sendRequestToDatabase(query);
                    break;

                case "allowAccessToAllRegisters":
                    payload = JsonParser.parseString(file.get("payload").getAsString()).getAsJsonObject();
                    patient = payload.get("patientName").getAsString();
                    String doctor = payload.get("doctorName").getAsString();
                    query = allowAccessToAllRegisters(patient, doctor);
                    response =  sendRequestToDatabase(query);
                    break;
                
                case "deletePersonalInformation":
                    payload = JsonParser.parseString(file.get("payload").getAsString()).getAsJsonObject();
                    patient = payload.get("patientName").getAsString();
                    query = deletePersonalInformation(patient);
                    response =  sendRequestToDatabase(query);
                    break;
            }
        }

        if (user.equals(DOCTOR)) {

            String command = file.get("command").getAsString();
            System.out.println("command from file: " + command);

            switch (command) {
                case "getPatientsConsultations":
                    JsonObject payload = JsonParser.parseString(file.get("payload").getAsString()).getAsJsonObject();
                    String patient = payload.get("patientName").getAsString();
                    String doctor = payload.get("doctorName").getAsString();
                    query = getPatientsConsultations(doctor, patient);

                    System.out.println("query: " + query);
                    response =  sendRequestToDatabase(query);
                    break;
            
                case "createConsultation":
                    payload = JsonParser.parseString(file.get("payload").getAsString()).getAsJsonObject();
                    patient = payload.get("patientName").getAsString();
                    String date = payload.get("date").getAsString();
                    doctor = payload.get("doctorName").getAsString();
                    String medicalSpeciality = payload.get("medicalSpeciality").getAsString();
                    String practice = payload.get("pratice").getAsString();
                    String treatmentSummary = payload.get("treatmentSummary").getAsString();
                    query = createConsultation(patient, date, medicalSpeciality, doctor, practice, treatmentSummary);
                    response =  sendRequestToDatabase(query);
                    break;
                
                case "changeMedicalSpeciality":
                    payload = JsonParser.parseString(file.get("payload").getAsString()).getAsJsonObject();
                    doctor = payload.get("doctorName").getAsString();
                    medicalSpeciality = payload.get("newMedicalSpeciality").getAsString();
                    query = changeMedicalSpeciality(doctor, medicalSpeciality);
                    response =  sendRequestToDatabase(query);
                    break;
            }
        }
        return response;
    }

    private static void configSecureSockets(){
        System.setProperty("javax.net.ssl.keyStore", "certificates/api.p12");
        System.setProperty("javax.net.ssl.keyStorePassword", "changeme");
        System.setProperty("javax.net.ssl.trustStore", "certificates/apitruststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeme");
    }	

    private static String sendRequestToDatabase(String query){
        SocketFactory factory = SSLSocketFactory.getDefault();
        try (SSLSocket socket = (SSLSocket) factory.createSocket(ADDRESS, PORT)) {

            socket.setEnabledCipherSuites(new String[] { "TLS_AES_128_GCM_SHA256" });
            socket.setEnabledProtocols(new String[] { "TLSv1.3" });

            System.out.println("Connected to server");

            JsonObject requestJson = new JsonObject();
            requestJson.addProperty("value", query);
    
            OutputStream os = new BufferedOutputStream(socket.getOutputStream());
            os.write(requestJson.toString().getBytes());
            os.flush();

            InputStream is = new BufferedInputStream(socket.getInputStream());
            byte[] data = new byte[2048];
            int len = is.read(data);

            String response = new String(data);

            return response;

        } catch (IOException i) {
            System.out.println(i);
            return "error";
        }
    }

    private static String handleAuthString(String username, String publicKeyString) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyString);
        JsonObject response = new JsonObject();
        response.addProperty("challenge", "2");
        return response.toString();
    } 

    public static byte[] generateRandomString(int length) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);

        return randomBytes;
    }

    private static String getPatientRegisters(String name) {

        return "SELECT * FROM patients " + 
                "JOIN consultations ON consultations.patientName = patients.name " + 
                    "WHERE patients.name = '" + name + "';";
    }

    private static String allowAccessToAllRegisters(String patient, String doctor) {

        return "INSERT INTO autorizations (consultationId, doctorName)" +
                " SELECT id, '" + doctor + "' " +
                    " FROM consultations"  +
                        " WHERE patientName = '"+ patient + "';";
    }

    private static String deletePersonalInformation(String patient) {

        return "DELETE FROM consultations WHERE patientName = '" + patient + "'; " + 
            "DELETE FROM patients WHERE name = '" + patient + "';";
    }


    private static String getPatientsConsultations(String doctor, String patient) {
        return "SELECT * FROM consultations " + 
                    "JOIN authorizations ON consultation.id = authorizations.consultationId" + 
                        "WHERE autorization.doctorName = '" + doctor + "' AND consultation.patientName = '" + patient+ "';";
    }

    private static String createConsultation(String patientName, String date, String medicalSpeciality, String doctorName, String practice, String treatmentSummary) {
        return "INSERT INTO consultations (patientName, date, medicalSpeciality, doctorName,  practice, treatmentSummary) "+ 
                    "VALUES (' " + patientName + "', '" + date + "', '" + medicalSpeciality + "', '" + doctorName + "',  '" + practice + "', '" + treatmentSummary +"');";
    }

    private static String changeMedicalSpeciality(String doctor, String medicalSpeciality) {
        return "UPDATE doctors SET medicalSpeciality = '" + medicalSpeciality + "' WHERE doctor = '" + doctor + "';";    
    }


}
