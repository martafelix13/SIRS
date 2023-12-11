ATTACH DATABASE ':memory:' AS patient_database;
-- to run sqlite3 < create_database.sql

-- Create the patient_records table
CREATE TABLE IF NOT EXISTS patients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) UNIQUE,
    sex VARCHAR(10),
    dateOfBirth DATE,
    bloodType VARCHAR(5),
    knownAllergies TEXT
);

CREATE TABLE IF NOT EXISTS consultations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT,
    date DATE,
    medicalSpeciality VARCHAR(255),
    doctorName VARCHAR(255),
    practice VARCHAR(255),
    treatmentSummary TEXT,
    FOREIGN KEY (patient_id) REFERENCES patients(id)
);

CREATE TABLE IF NOT EXISTS doctors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) UNIQUE
);

CREATE TABLE IF NOT EXISTS consultation_doctors (
    consultation_id INT,
    doctor_id INT,
    PRIMARY KEY (consultation_id, doctor_id),
    FOREIGN KEY (consultation_id) REFERENCES consultations(id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);


-- Insert patient data
INSERT INTO patients (name, sex, dateOfBirth, bloodType, knownAllergies)
VALUES ('Bob', 'Male', '2004-05-15', 'A+', '[\"Penicillin\"]');

-- Insert consultation records
INSERT INTO consultations (patient_id, date, medicalSpeciality, practice, treatmentSummary)
VALUES
((SELECT id FROM patients WHERE name = 'Bob'), '2022-05-15', 'Orthopedic', 'OrthoCare Clinic', 'Fractured left tibia; cast applied.'),
((SELECT id FROM patients WHERE name = 'Bob'), '2023-04-20', 'Gastroenterology', 'Digestive Health Center', 'Diagnosed with gastritis; prescribed antacids.'),
((SELECT id FROM patients WHERE name = 'Bob'), '2023-09-05', 'Dermatology', 'SkinCare Clinic', 'Treated for Molluscum Contagiosum; prescribed topical corticosteroids.');

-- Insert doctors
INSERT INTO doctors (name) VALUES ('Dr. Smith'), ('Dr. Johnson'), ('Dr. Martins');

-- Insert doctors associated with consultations
INSERT INTO consultation_doctors (consultation_id, doctor_id)
VALUES
((SELECT id FROM consultations WHERE date = '2022-05-15' AND practice = 'OrthoCare Clinic'), (SELECT id FROM doctors WHERE name = 'Dr. Smith')),
((SELECT id FROM consultations WHERE date = '2023-04-20' AND practice = 'Digestive Health Center'), (SELECT id FROM doctors WHERE name = 'Dr. Johnson')),
((SELECT id FROM consultations WHERE date = '2023-09-05' AND practice = 'SkinCare Clinic'), (SELECT id FROM doctors WHERE name = 'Dr. Martins'));