SELECT
    p.name,
    p.sex,
    p.dateOfBirth,
    p.bloodType,
    JSON_ARRAY(DISTINCT a.allergy_name) AS knownAllergies,
    c.date AS consultationDate,
    c.medical_speciality AS medicalSpeciality,
    c.doctor_name AS doctorName,
    c.practice,
    c.treatment_summary AS treatmentSummary
FROM patients p
LEFT JOIN allergies a ON p.name = a.patient_name
LEFT JOIN consultations c ON p.name = c.patient_name
WHERE p.name = ?
GROUP BY p.name, c.date, c.medical_speciality, c.doctor_name, c.practice, c.treatment_summary
ORDER BY c.date;