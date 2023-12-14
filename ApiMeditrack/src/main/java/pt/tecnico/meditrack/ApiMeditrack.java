package pt.tecnico.meditrack;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class ApiMeditrack {

    private final static int PORT = 5555;
    private final static String ADDRESS = "localhost";

    private final static String PATIENT = "patient";
    private final static String DOCTOR = "doctor";
    private final static String SOS = "sos";

    public static void main(String[] args) throws FileNotFoundException, IOException {

        String filename = "src/main/java/pt/tecnico/meditrack/getRegisterJson.json";
        //String filename = "src/main/java/pt/tecnico/meditrack/allowAccessToAllRegisters.json";


        JsonObject file = readJsonFile(filename);

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

/*             String command = args[1];

            switch (command) {
            
                case "getPatientsConsultations":
                    query = "SELECT patients.name AS patient_name, consultations.id AS consultation_id, consultations.date, consultations.medicalSpeciality, consultations.doctorName, consultations.practice, consultations.treatmentSummary FROM consultations JOIN autorizations ON consultations.id = autorizations.consultationId JOIN patients ON consultations.patientName = patients.name WHERE autorizations.doctorName = 'Dr. Smith' AND patients.name = 'Bob';";
                    break;
                
                case "addConsultation":
                    query = "INSERT INTO consultations (patientName, date, medicalSpeciality, doctorName, practice, treatmentSummary) VALUES ('Bob', '2023-12-31', 'Cardiology', 'Dr. Smith', 'Cardiology Clinic', 'Initial consultation for Bob by Dr. Smith');";
                    break;
                }

                case "changeMedicalSpeciality": */

        }
        

        try (Socket socket = new Socket(ADDRESS, PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {

            System.out.println("Connected to server");

            JsonObject requestJson = new JsonObject();
  
            requestJson.addProperty("value", query);

            out.println(requestJson.toString());

            String serverResponse = in.readLine();
            System.out.println("Received from server: " + serverResponse.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
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
                "JOIN consultations ON patients.name = consultations.patientName " + 
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

}
