SELECT
    JSON_OBJECT(
        'name', p.name,
        'sex', p.sex,
        'dateOfBirth', p.dateOfBirth,
        'bloodType', p.bloodType,
        'knownAllergies', (
            SELECT JSON_GROUP_ARRAY(DISTINCT allergy_name) 
            FROM allergies a 
            WHERE a.patient_name = p.name
        ),
        'consultationRecords', JSON_GROUP_ARRAY(
            JSON_OBJECT(
                'date', c.date,
                'medicalSpeciality', c.medical_speciality,
                'doctorName', c.doctor_name,
                'practice', c.practice,
                'treatmentSummary', c.treatment_summary
            )
        )
    ) AS patient
FROM patients p
LEFT JOIN allergies a ON p.name = a.patient_name
LEFT JOIN consultations c ON p.name = c.patient_name

