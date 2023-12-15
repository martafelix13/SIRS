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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


public class ApiMeditrack {

    private final static int PORT = 5555;
    private final static String ADDRESS = "localhost";

    private final static String PATIENT = "patient";
    private final static String DOCTOR = "doctor";
    private final static String SOS = "sos";

    public static void main(String[] args) throws FileNotFoundException, IOException {
        configSecureSockets();

        JsonObject file = readRequestFromClient();

        

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
            System.out.printf("client received %d bytes: %s%n", len, new String(data, 0, len));

        } catch (IOException i) {
            System.out.println(i);
            return;
        }
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
