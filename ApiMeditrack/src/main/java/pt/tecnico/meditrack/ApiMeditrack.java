package pt.tecnico.meditrack;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.BufferedReader;
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
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsServer;

import javax.net.SocketFactory;
import javax.sound.sampled.Port;
import javax.net.ssl.*;

import java.io.FileInputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
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

    public static void main(String[] args) throws FileNotFoundException, IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, KeyManagementException {

        try {
            // Specify the keystore and truststore file paths and passwords
            String keystoreFilePath = "certificates/server.p12";
            String keystorePassword = "changeme";
            String truststoreFilePath = "certificates/servertruststore.jks";
            String truststorePassword = "changeme";

            // Load the keystore
            char[] keystorePasswordChars = keystorePassword.toCharArray();
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new FileInputStream(keystoreFilePath), keystorePasswordChars);

            // Load the truststore
            char[] truststorePasswordChars = truststorePassword.toCharArray();
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(new FileInputStream(truststoreFilePath), truststorePasswordChars);
            


            // Initialize the key manager factory
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keystorePasswordChars);

            // Initialize the trust manager factory
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            // Initialize the SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            // Create the HTTPS server
            HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(PORT_HTTPS), 0);
            httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext));

            // Create a simple context to handle requests
            httpsServer.createContext("/api", exchange -> {
                // Handle the incoming request at the "/api" endpoint
                
                // notifica que o servidor recebeu um pedido
                System.out.println("Received connection from: " + exchange.getRemoteAddress());
                // Read the request method (e.g., POST)
                String requestMethod = exchange.getRequestMethod();
                
                // Read the request headers (e.g., Content-Type)
                Headers requestHeaders = exchange.getRequestHeaders();
                
                // Get the request input stream to read the request body
                try (InputStream request = exchange.getRequestBody()) {
                    // Read the JSON payload from the input stream
                    String requestBody = new String(request.readAllBytes(), StandardCharsets.UTF_8);
                    
                    // Handle the JSON payload (you can process or save it as needed)
                    System.out.println("Received JSON request: " + requestBody);
                    JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();

                    String response = processJsonRequest(jsonRequest);
                    
                    // Send a response back to the client
                    exchange.sendResponseHeaders(200, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes(StandardCharsets.UTF_8));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // Start the server
            httpsServer.start();

            System.out.println("Server is running on port " + PORT_HTTPS + "...");
        } catch (Exception e) {
            e.printStackTrace();
        }
     
        /** SECURE SOCKETS  */

        configSecureSockets();
        
    }
    

    private static String processJsonRequest(JsonObject file) {
        
        String user = file.get("user").getAsString();
        System.out.println("User from file: " + user);

        String query = "";
        if (user.equals(PATIENT)) {

            String command = file.get("command").getAsString();
            System.out.println("command from file: " + command);

            switch (command) {
                case "getRegisters":
                    JsonObject payload = JsonParser.parseString(file.get("payload").getAsString()).getAsJsonObject();
                    String patient = payload.get("patientName").getAsString();
                    System.out.println("patientName from file: " + patient);
                    query = getPatientRegisters(patient);
                    System.out.println("query: " + query);
                    break;

                case "allowAccessToAllRegisters":
                    payload = JsonParser.parseString(file.get("payload").getAsString()).getAsJsonObject();
                    patient = payload.get("patientName").getAsString();
                    String doctor = payload.get("doctorName").getAsString();
                    query = allowAccessToAllRegisters(patient, doctor);
    
                    break;
                
                case "deletePersonalInformation":
                    payload = JsonParser.parseString(file.get("payload").getAsString()).getAsJsonObject();
                    patient = payload.get("patientName").getAsString();
                    query = deletePersonalInformation(patient);
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
                    break;
                
                case "changeMedicalSpeciality":
                    payload = JsonParser.parseString(file.get("payload").getAsString()).getAsJsonObject();
                    doctor = payload.get("doctorName").getAsString();
                    medicalSpeciality = payload.get("newMedicalSpeciality").getAsString();
                    query = changeMedicalSpeciality(doctor, medicalSpeciality);
            }
        }

        String response =  sendRequestToDatabase(query); 
        return response;
    }

    private static void configSecureSockets(){
        System.setProperty("javax.net.ssl.keyStore", "certificates/api.p12");
        System.setProperty("javax.net.ssl.keyStorePassword", "changeme");
        System.setProperty("javax.net.ssl.trustStore", "certificates/apitruststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeme");
    }
    

    private static JsonObject readRequestFromClient() throws FileNotFoundException, IOException {
        String filename = "src/main/java/pt/tecnico/meditrack/getRegisterJson.json";
        return readJsonFile(filename);
    }

    private static JsonObject readJsonFile(String filename) throws FileNotFoundException, IOException{
		try (FileReader fileReader = new FileReader(filename)) {
            Gson gson = new Gson();
            JsonObject rootJson = gson.fromJson(fileReader, JsonObject.class);

			return rootJson;
        }
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
            //System.out.printf("client received %d bytes: %s%n", len, new String(data, 0, len));

            return response;

        } catch (IOException i) {
            System.out.println(i);
            return "error";
        }
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
