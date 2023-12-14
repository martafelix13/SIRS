-- to run sqlite3 < create_database.sql

-- Create the patient_records table
DROP TABLE IF EXISTS patients;
CREATE TABLE patients (
    name VARCHAR(255) PRIMARY KEY,
    sex VARCHAR(10),
    dateOfBirth DATE,
    bloodType VARCHAR(5),
    knownAllergies TEXT
);

DROP TABLE IF EXISTS consultations;
CREATE TABLE consultations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patientName VARCHAR(255),
    date DATE,
    medicalSpeciality VARCHAR(255),
    doctorName VARCHAR(255),
    practice VARCHAR(255),
    treatmentSummary TEXT,
    FOREIGN KEY (patientName) REFERENCES patients(name),
    FOREIGN KEY (doctorName) REFERENCES doctorName(name)

);

DROP TABLE IF EXISTS autorizations;
CREATE TABLE autorizations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    consultationId INT,
    doctorName VARCHAR(255),
    FOREIGN KEY (consultationId) REFERENCES consultations(id),
    FOREIGN KEY (doctorName) REFERENCES doctorName(name)
);


DROP TABLE IF EXISTS doctors;
CREATE TABLE doctors (
    name VARCHAR(255) PRIMARY KEY,
    medicalSpeciality VARCHAR(255)
);


-- Insert patient data
INSERT INTO patients (name, sex, dateOfBirth, bloodType, knownAllergies)
VALUES ('Bob', 'Male', '2004-05-15', 'A+', '[\"Penicillin\"]'),
       ('Alice', 'Female', '1990-08-23', 'B-', '[\"None\"]'),
       ('Charlie', 'Male', '1985-03-10', 'AB+', '[\"Aspirin\"]');

-- Insert consultation records
INSERT INTO consultations (id, patientName, date, medicalSpeciality, doctorName,  practice, treatmentSummary)
VALUES
(1, 'Bob', '2022-05-15', 'Orthopedic', 'Dr. Smith',  'OrthoCare Clinic', 'Fractured left tibia; cast applied.'),
(2, 'Bob', '2023-12-10', 'Orthopedic', 'Dr. Smith', 'OrthoCare Clinic', 'Follow-up for previous fracture; cast removal.'),
(3, 'Bob', '2023-09-05', 'Dermatology', 'Dr. Martins', 'SkinCare Clinic', 'Treated for Molluscum Contagiosum; prescribed topical corticosteroids.'),
(4, 'Bob', '2023-04-20', 'Gastroenterology', 'Dr. Johnson', 'Digestive Health Center', 'Diagnosed with gastritis; prescribed antacids.'),
(5, 'Alice', '2023-09-15', 'Gastroenterology', 'Dr. Johnson', 'Digestive Health Center', 'Routine checkup'),
(6, 'Alice', '2023-01-10', 'Cardiology', 'Dr. White', 'Heart Center', 'Routine checkup; normal heart rate and blood pressure.'),
(7, 'Alice', '2023-06-28', 'Ophthalmology', 'Dr. Davis', 'EyeCare Clinic', 'Prescription for corrective lenses issued.'),
(8, 'Charlie', '2022-11-15', 'Pediatrics', 'Dr. Brown', 'Kidz Health Clinic', 'Vaccination and general health checkup.'),
(9, 'Charlie', '2023-03-02', 'Neurology', 'Dr. Lee', 'BrainHealth Center', 'MRI scan conducted for headache; results normal.'),
(10, 'Charlie', '2023-07-20', 'Dermatology', 'Dr. Martins', 'SkinCare Clinic', 'Routine checkup');

INSERT INTO doctors(name, medicalSpeciality)
VALUES
('Dr. Smith', 'Orthopedic'),
('Dr. Martins',  'Dermatology'),
('Dr. Johnson', 'Gastroenterology' ), 
('Dr. White', 'Cardiology'), 
('Dr. Davis', 'Ophthalmology'),
('Dr. Brown', 'Pediatrics'), 
('Dr. Lee', 'Neurology');



INSERT INTO autorizations(id, consultationId, doctorName)
VALUES
(1, 1, 'Dr. Smith'),
(2, 2, 'Dr. Smith'),
(3, 3, 'Dr. Martins'),
(4, 4, 'Dr. Johnson'),
(5, 5, 'Dr. Johnson'),
(6, 6, 'Dr. White'),
(7, 7, 'Dr. Davis'),
(8, 8, 'Dr. Brown'),
(9, 9, 'Dr. Lee'),
(10, 10, 'Dr. Martins');



