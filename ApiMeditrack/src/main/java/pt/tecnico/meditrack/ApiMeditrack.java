package pt.tecnico.meditrack;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.sun.net.httpserver.HttpsServer;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.SocketFactory;
import javax.net.ssl.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import com.sun.net.httpserver.HttpsConfigurator;



public class ApiMeditrack {

    private final static int PORT = 3306;
    private final static int PORT_HTTPS = 433;

    private final static String ADDRESS = "localhost";

    private final static String PATIENT = "patient";
    private final static String DOCTOR = "doctor";
    private final static String SOS = "sos";

    final static String filename_db="db.json";
    final static String filename_client="response.json";

    private final static int CHALLENGE_SIZE = 16;

    private static Map<String, String> clientChallenges = new HashMap<>();
    private static Map<String, PublicKey> clientPublicKeys = new HashMap<>();

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
                        System.out.println("Sending JSON response: " + response);
                        String encryptResponse = protectResponse(response);
                        System.out.println("Encrypt Json: " + encryptResponse);
                        //JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
                        
                        //saveJsonToFile(jsonResponse, filename_db);
                        exchange.sendResponseHeaders(200, encryptResponse.length());

                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(encryptResponse.getBytes(StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch ( Exception e) {
                        e.printStackTrace();
                    };
                } catch ( Exception e) {
                            e.printStackTrace();
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

    private static String protectResponse(String response) {
        Gson gson = new Gson();
        JsonObject input = gson.fromJson(response, JsonObject.class);
        //JsonObject input = JsonParser.parseString(response).getAsJsonObject();
        PrivateKey privateKey;
        PublicKey publicKey;
        JsonObject output = new JsonObject();
        try {
            privateKey = readPrivateKey("keys/api.privkey");
            publicKey = readPublicKey("keys/patient.pubkey");
            output = SecureDocument.protectJson(input, publicKey, privateKey, "keys/secret.key");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
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
        
        String pathToPrivateString = "./keys/api.privkey";
        PrivateKey privKey = readPrivateKey(pathToPrivateString);
        String pathToPublicString = "./keys/patient.pubkey";
        PublicKey pubKey = readPublicKey(pathToPublicString);

        file = SecureDocument.unprotectJson(file, privKey, pubKey);

        System.out.println("Received JSON request unprotected: " + file.toString());
        
        //Get file content

        JsonObject content = JsonParser.parseString(file.get("content").getAsString()).getAsJsonObject();

        String user = content.get("user").getAsString();
        System.out.println("User from file: " + user);
        String response = "Internal server error";
        String query = "";
        if (user.equals(PATIENT)) {

            String command = content.get("command").getAsString();
            System.out.println("command from file: " + command);

            switch (command) {
                case "authPatient":
                    JsonObject payload = JsonParser.parseString(content.get("payload").getAsString()).getAsJsonObject();
                    //JsonObject payload = file.get("payload").getAsJsonObject();
                    String username = payload.get("username").getAsString();
                    String publicKeyString = payload.get("publicKey").getAsString();
                    response = handleAuthString(username, publicKeyString);
                    System.out.println(response);
                    break;
                
                case "validateAuth":
                    payload = JsonParser.parseString(content.get("payload").getAsString()).getAsJsonObject();
                    String challengeResponse = payload.get("challengeResponse").getAsString();
                    username = payload.get("username").getAsString();
                    response = validateAuth(username, challengeResponse);
                    System.out.println(response);
                    break;
                
                case "getRegisters":
                    payload = JsonParser.parseString(content.get("payload").getAsString()).getAsJsonObject();
                    String patient = payload.get("patientName").getAsString();
                    System.out.println("patientName from file: " + patient);
                    query = getPatientRegisters(patient);
                    response = sendRequestToDatabase(query);
                    break;

                case "allowAccessToAllRegisters":
                    payload = JsonParser.parseString(content.get("payload").getAsString()).getAsJsonObject();
                    patient = payload.get("patientName").getAsString();
                    String doctor = payload.get("doctorName").getAsString();
                    query = allowAccessToAllRegisters(patient, doctor);
                    response = sendRequestToDatabase(query);
                    break;
                
                case "deletePersonalInformation":
                    payload = JsonParser.parseString(content.get("payload").getAsString()).getAsJsonObject();
                    patient = payload.get("patientName").getAsString();
                    query = deletePersonalInformation(patient);
                    response = sendRequestToDatabase(query);
                    break;
            }
        }

        if (user.equals(DOCTOR)) {

            String command = content.get("command").getAsString();
            System.out.println("command from file: " + command);

            switch (command) {

                case "authDoctor":
                    JsonObject payload = JsonParser.parseString(content.get("payload").getAsString()).getAsJsonObject();
                    String username = payload.get("username").getAsString();
                    String publicKeyString = payload.get("publicKey").getAsString();
                    response = handleAuthString(username, publicKeyString);
                    System.out.println(response);
                    break;

                case "getPatientsConsultations":
                    payload = JsonParser.parseString(content.get("payload").getAsString()).getAsJsonObject();
                    String patient = payload.get("patientName").getAsString();
                    String doctor = payload.get("doctorName").getAsString();
                    query = getPatientsConsultations(doctor, patient);
                    System.out.println("query: " + query);
                    response =  sendRequestToDatabase(query);
                    break;
            
                case "createConsultation":
                    payload = JsonParser.parseString(content.get("payload").getAsString()).getAsJsonObject();
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
                    payload = JsonParser.parseString(content.get("payload").getAsString()).getAsJsonObject();
                    doctor = payload.get("doctorName").getAsString();
                    medicalSpeciality = payload.get("newMedicalSpeciality").getAsString();
                    query = changeMedicalSpeciality(doctor, medicalSpeciality);
                    response =  sendRequestToDatabase(query);
                    break;
            }
        }
        return response;
    }

    /*private static void protectResponse(String response, String username) {
        try {
            System.out.println("Response: " + response);
            String pathToPrivateString = "./keys/api.privkey";

            JsonObject clientResponse = new JsonObject();


        
            //String pathToPrivateString = "./keys/api.privkey";
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
            //saveJsonToFile(jsonResponse, "db.json");

            PublicKey pubKey = clientPublicKeys.get(username);
            PrivateKey privKey = readPrivateKey(pathToPrivateString);

            SecureDocument.protectJson(jsonResponse, clientResponse, pubKey , privKey, "keys/secret.key");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    
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
            os.write(requestJson.toString().getBytes("UTF-8"));
            os.flush();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                // Read JSON data from the client
                String data = reader.readLine();
                System.out.println("Received JSON: " + data);
        
                return data;

            } catch (IOException i) {
                System.out.println(i);
                return "error";
            }
        } catch (IOException i) {
                System.out.println(i);
                return "error";
        }
    }

    private static String handleAuthString(String username, String publicKeyString) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] challengeBytes = generateRandomString(CHALLENGE_SIZE);
        String challenge = Base64.getEncoder().encodeToString(challengeBytes);
        System.out.println("Original Challenge: " + challenge);
        clientChallenges.put(username, challenge);

        //string to byte[]
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyString);
  
        X509EncodedKeySpec publicKey = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(publicKey);

        try {
            saveKey(pubKey, "./keys/patient.pubkey");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            // Encrypting the challenge
            challengeBytes = challenge.getBytes(); // Replace with your actual challenge
            byte[] encryptChallenge = encryptContentRSAWithPublicKey(challengeBytes, pubKey);
            String encryptedChallenge = Base64.getEncoder().encodeToString(encryptChallenge);
            System.out.println("Encrypted Challenge: " + encryptedChallenge);

            

            ///TESTE///
            JsonObject response = new JsonObject();
            response.addProperty("challenge", encryptedChallenge);

            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Authentication process failed";
        }
    }
 

    private static String validateAuth(String username, String challengeResponse) {
        JsonObject responseObject = new JsonObject();
        
        String challenge = clientChallenges.get(username);

        System.out.println("Challenge: " + challenge);
        System.out.println("Challenge Response: " + challengeResponse);
        System.out.println("challenge.equals(challengeResponse) " + challenge.equals(challengeResponse));
        if (challenge != null && challenge.equals(challengeResponse)) {
            responseObject.addProperty("validation", true);
        } else {
            responseObject.addProperty("validation", false);
            clientPublicKeys.remove(username);
        }

        System.out.println("Clients Keys " + clientPublicKeys.toString());
        System.out.println("Clients Challenges " + clientChallenges.toString());
        return responseObject.toString();
    }



    private static byte[] generateRandomString(int length) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);

        return randomBytes;
    }

    private static String getPatientRegisters(String name) {
        String adaptQuery = "";
        try {
            String query = readQueryFromFile("queries/getPatientsQuery.sql");
            adaptQuery = query.replace("?", "'" + name + "'");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return adaptQuery;
    }

    private static String allowAccessToAllRegisters(String patient, String doctor) {
        return "INSERT INTO autorizations (consultation_id, doctor_name) " +
        "SELECT consultation_id, '" + doctor + "' AS doctor_name " + 
        "FROM consultations " + 
        "WHERE patient_name = '" + patient + "'";
    }

    private static String deletePersonalInformation(String patient) {
        return  "DELETE FROM allergies WHERE patient_name = '" + patient + "';\n" + 
                "DELETE FROM autorizations WHERE consultation_id IN (" +
                    "SELECT consultation_id FROM consultations WHERE patient_name = '" + patient + "'); \n" +
                "DELETE FROM consultations WHERE patient_name = '" + patient + "';\n" +
                "DELETE FROM patients WHERE name = '" + patient + "';";
    }

    private static String getPatientsConsultations(String doctor, String patient) {
        String adaptQuery = "";
        try {
            String query = readQueryFromFile("queries/getConsultationsRecords.sql");
            adaptQuery = query.replace("?", "'" + doctor + "'");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return adaptQuery;
    }

    private static String createConsultation(String patientName, String date, String medicalSpeciality, String doctorName, String practice, String treatmentSummary) {
        return "INSERT INTO consultations (patient_name, date, medical_speciality, doctor_name, practice, treatment_summary)\n" + 
                "\n VALUES ( " + patientName + ", " + date + "," + medicalSpeciality + "," + doctorName + ", " + practice + ", " + treatmentSummary + ")";
    }

    private static String changeMedicalSpeciality(String doctor, String medicalSpeciality) {
        return "UPDATE doctors SET medical_speciality = " + medicalSpeciality + " WHERE name = " + doctor;
    }

    private static byte [] decryptRSAWithPrivateKey(byte[] content, PrivateKey privateKey) throws Exception{
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(content);
    }

	private static byte[] encryptContentRSAWithPublicKey(byte[] content, PublicKey publicKey) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		return cipher.doFinal(content);
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

    private static void writeFile(String path, byte[] content) throws FileNotFoundException, IOException {
		FileOutputStream fos = new FileOutputStream(path);
		fos.write(content);
		fos.close();
	}

    private static byte[] readFile(String path) throws FileNotFoundException, IOException {
		FileInputStream fis = new FileInputStream(path);
		byte[] content = new byte[fis.available()];
		fis.read(content);
		fis.close();
		return content;
	}


    private static void saveKey(Key key, String filePath) throws Exception {
            byte[] keyBytes = key.getEncoded();
            Files.write(Paths.get(filePath), keyBytes);
        }

    private static void saversJsonToFile(JsonObject document, String output_filename) throws IOException {
            try (FileWriter fileWriter = new FileWriter(output_filename)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(document, fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        };
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
}
