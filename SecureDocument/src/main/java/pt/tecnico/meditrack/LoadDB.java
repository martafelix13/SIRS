import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class LoadDB {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:db/patiente_db.sql";  // Replace with the path to your SQLite database file
        try (Connection connection = DriverManager.getConnection(url)) {
            // Example: Retrieve all patients and their consultation records
            String query = "SELECT p.name AS patient_name, c.date, c.medicalSpeciality, c.doctorName, c.practice, c.treatmentSummary " +
                           "FROM patients p " +
                           "JOIN consultations c ON p.id = c.patient_id";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    JsonArray resultsArray = new JsonArray();

                    while (resultSet.next()) {
                        JsonObject resultObject = new JsonObject();
                        resultObject.addProperty("patientName", resultSet.getString("patient_name"));
                        resultObject.addProperty("date", resultSet.getString("date"));
                        resultObject.addProperty("medicalSpeciality", resultSet.getString("medicalSpeciality"));
                        resultObject.addProperty("doctorName", resultSet.getString("doctorName"));
                        resultObject.addProperty("practice", resultSet.getString("practice"));
                        resultObject.addProperty("treatmentSummary", resultSet.getString("treatmentSummary"));

                        resultsArray.add(resultObject);
                    }

                    // Convert the results to JSON and print
                    System.out.println(resultsArray.toString());
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
