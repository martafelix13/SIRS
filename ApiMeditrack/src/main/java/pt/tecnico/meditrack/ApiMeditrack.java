package pt.tecnico.meditrack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

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



    // USER COMMAND ARGS
    public static void main(String[] args) {

        String query = "";
        if (args[0] == PATIENT) {

            String command = args[1];

            switch (command) {
                case "getRegisters":
                    query = "SELECT * FROM patients JOIN consultations ON patients.name = consultations.patientName WHERE patients.name = 'Bob';";
                    break;
            
                case "addAccessToAllRegisters":
                    query = "INSERT INTO autorizations (consultationId, doctorName, patientName)\n" +
                            "SELECT id, 'Dr. Smith', 'Bob'\n" +
                            "FROM consultations\n" + 
                            "WHERE patientName = 'Bob';";
                    break;
                
                case "deletePersonalInformation":
                    query = "DELETE FROM consultations WHERE patientName = 'John Doe'; DELETE FROM patients WHERE name = 'John Doe';";
            }
        }

        if (args[0] == DOCTOR) {

            String command = args[1];

            switch (command) {
                case "getConsultations":
                    query = "SELECT \n" +
                                "patients.name AS patient_name, \n" +
                                "consultations.id AS consultation_id,\n" +
                                "consultations.date,\n" +
                                "consultations.medicalSpeciality,\n" +
                                "consultations.doctorName,\n" +
                                "consultations.practice,\n" +
                                "consultations.treatmentSummary FROM consultations\n" +
                                "JOIN patients ON consultations.patientName = patients.name\n" +
                                "WHERE consultations.doctorName = 'Dr. Smith';\n";
                    break;
            
                case "getPatientsConsultations":
                    query = "SELECT patients.name AS patient_name, consultations.id AS consultation_id, consultations.date, consultations.medicalSpeciality, consultations.doctorName, consultations.practice, consultations.treatmentSummary FROM consultations JOIN autorizations ON consultations.id = autorizations.consultationId JOIN patients ON consultations.patientName = patients.name WHERE autorizations.doctorName = 'Dr. Smith' AND patients.name = 'Bob';";
                    break;
                
                case "addConsultation":
                    query = "INSERT INTO consultations (patientName, date, medicalSpeciality, doctorName, practice, treatmentSummary) VALUES ('Bob', '2023-12-31', 'Cardiology', 'Dr. Smith', 'Cardiology Clinic', 'Initial consultation for Bob by Dr. Smith');";
            }
        }
        

        try (Socket socket = new Socket(ADDRESS, PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {

            System.out.println("Connected to server");

            JsonObject requestJson = new JsonObject();
  
            // Query to be executed
            requestJson.addProperty("value", "SELECT * FROM patients;");

            // Send a message to the server
            out.println(requestJson.toString());

            // Read the response from the server
            String serverResponse = in.readLine();
            System.out.println("Received from server: " + serverResponse.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
