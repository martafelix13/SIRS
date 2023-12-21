-- Create the patients table
DROP TABLE IF EXISTS patients;
CREATE TABLE IF NOT EXISTS patients (
    name PRIMARY KEY,
    sex TEXT,
    dateOfBirth DATE,
    bloodType TEXT
);

-- Create the consultations table
DROP TABLE IF EXISTS consultations;
CREATE TABLE IF NOT EXISTS consultations (
    consultation_id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_name TEXT,
    date DATE,
    medical_speciality TEXT,
    doctor_name TEXT,
    practice TEXT,
    treatment_summary TEXT,
    FOREIGN KEY (patient_name) REFERENCES patients(name)
);

-- Create the autorizations table
DROP TABLE IF EXISTS autorizations;
CREATE TABLE IF NOT EXISTS autorizations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    consultation_id INTEGER,
    doctor_name TEXT,
    FOREIGN KEY (consultation_id) REFERENCES consultations(consultation_id),
    FOREIGN KEY (doctor_name) REFERENCES doctors(name)
);

-- Create the doctors table
DROP TABLE IF EXISTS doctors;
CREATE TABLE IF NOT EXISTS doctors (
    name TEXT PRIMARY KEY,
    medical_speciality TEXT
);

-- Create the allergies table
DROP TABLE IF EXISTS allergies;
CREATE TABLE IF NOT EXISTS allergies (
    allergy_id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_name TEXT,
    allergy_name TEXT,
    FOREIGN KEY (patient_name) REFERENCES patients(name)
);

-- Insert patient data
INSERT INTO patients (name, sex, dateOfBirth, bloodType)
VALUES ('Bob', 'Male', '2004-05-15', 'A+'),
       ('Alice', 'Female', '1990-08-23', 'B-'),
       ('Charlie', 'Male', '1985-03-10', 'AB+');

-- Insert allergy data
INSERT INTO allergies (patient_name, allergy_name)
VALUES ('Bob', 'Penicillin'),
       ('Bob', 'Aspirin'),
       ('Charlie', 'Aspirin');

-- Insert consultation records
INSERT INTO consultations (patient_name, date, medical_speciality, doctor_name, practice, treatment_summary)
VALUES
('Bob', '2022-05-15', 'Orthopedic', 'Dr.Smith', 'OrthoCare Clinic', 'Fractured left tibia; cast applied.'),
('Bob', '2023-12-10', 'Orthopedic', 'Dr.Smith', 'OrthoCare Clinic', 'Follow-up for previous fracture; cast removal.'),
('Bob', '2023-09-05', 'Dermatology', 'Dr.Martins', 'SkinCare Clinic', 'Treated for Molluscum Contagiosum; prescribed topical corticosteroids.'),
('Bob', '2023-04-20', 'Gastroenterology', 'Dr.Johnson', 'Digestive Health Center', 'Diagnosed with gastritis; prescribed antacids.'),
('Alice', '2023-09-15', 'Gastroenterology', 'Dr.Johnson', 'Digestive Health Center', 'Routine checkup'),
('Alice', '2023-01-10', 'Cardiology', 'Dr.White', 'Heart Center', 'Routine checkup; normal heart rate and blood pressure.'),
('Alice', '2023-06-28', 'Ophthalmology', 'Dr.Davis', 'EyeCare Clinic', 'Prescription for corrective lenses issued.'),
('Charlie', '2022-11-15', 'Pediatrics', 'Dr.Brown', 'Kidz Health Clinic', 'Vaccination and general health checkup.'),
('Charlie', '2023-03-02', 'Neurology', 'Dr.Lee', 'BrainHealth Center', 'MRI scan conducted for headache; results normal.'),
('Charlie', '2023-07-20', 'Dermatology', 'Dr.Martins', 'SkinCare Clinic', 'Routine checkup');

-- Insert doctor data
INSERT INTO doctors (name, medical_speciality)
VALUES
('Dr.Smith', 'Orthopedic'),
('Dr.Martins', 'Dermatology'),
('Dr.Johnson', 'Gastroenterology' ), 
('Dr.White', 'Cardiology'), 
('Dr.Davis', 'Ophthalmology'),
('Dr.Brown', 'Pediatrics'), 
('Dr.Lee', 'Neurology');

-- Insert authorization data
INSERT INTO autorizations (consultation_id, doctor_name)
VALUES
(1, 'Dr.Smith'),
(2, 'Dr.Smith'),
(3, 'Dr.Martins'),
(4, 'Dr.Johnson'),
(5, 'Dr.Johnson'),
(6, 'Dr.White'),
(7, 'Dr.Davis'),
(8, 'Dr.Brown'),
(9, 'Dr.Lee'),
(10, 'Dr.Martins');
