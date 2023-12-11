# A34 MediTrack Project Report

## 1. Introduction

In response to the growing need for enhanced security in managing patient data within Portugal's healthcare institutions, our project focuses on fortifying the MediTrack Electronic Health Records (EHR) system. The primary components of our project include the development of a secure document format, the establishment of a robust infrastructure, and addressing specific security challenges inherent in healthcare data management.


## 2. Project Development

### 2.1. Secure Document Format

#### 2.1.1. Design

Our custom cryptographic library, implemented in Java using the Java Cryptography Architecture (JCA), is meticulously designed to uphold the authenticity and confidentiality of patient data within the MediTrack EHR system. The following is an illustrative example of our data format, showcasing the designed protections.

{
  "patient": {
    "name": "Bob",
    "sex": "Male",
    "dateOfBirth": "2004-05-15",
    "bloodType": "A+",
    "knownAllergies": ["Penicillin"],
    "consultationRecords": [
      {
        "date": "2022-05-15",
        "medicalSpeciality": "Orthopedic",
        "doctorName": "Dr. Smith",
        "practice": "OrthoCare Clinic",
        "treatmentSummary": "Fractured left tibia; cast applied."
        "access": ["Dr. Smith"]
      },
      {
        "date": "2023-04-20",
        "medicalSpeciality": "Gastroenterology",
        "doctorName": "Dr. Johnson",
        "practice": "Digestive Health Center",
        "treatmentSummary": "Diagnosed with gastritis; prescribed antacids."
        "access":["Dr. Smith"]
      },
      {
        "date": "2023-09-05",
        "medicalSpeciality": "Dermatology",
        "doctorName": "Dr. Martins",
        "practice": "SkinCare Clinic",
        "treatmentSummary": "Treated for Molluscum Contagiosum; prescribed topical corticosteroids."
        "access":["Dr. Smith"]
      }
    ]
  },
  "authenticity": "digital_signature_here",
  "freshness": "encrypted_data_here"
}

authenticity: Digital signatures guarantee the origin and integrity of patient data.
freshness: Assures the data freshness


Document should always be a valid json

#### 2.1.2. Implementation

The implementation of our custom cryptographic library was carried out in Java, utilizing the Java Cryptography Architecture (JCA) for encryption and digital signature operations.

Digital Signature (Authenticity): Utilized JCA for asymmetric cryptography, generating and verifying digital signatures with a securely held private key and shared public key for users.

Encryption (Confidentiality): Leveraged JCA to apply the Advanced Encryption Standard (AES) for symmetric encryption, establishing a shared secret key through a secure key exchange mechanism.

Freshness-Token: Timestamp and counter methods .


### 2.2. Infrastructure

#### 2.2.1. Network and Machine Setup

(_Provide a brief description of the built infrastructure._)

(_Justify the choice of technologies for each server._)

#### 2.2.2. Server Communication Security

(_Discuss how server communications were secured, including the secure channel solutions implemented and any challenges encountered._)

(_Explain what keys exist at the start and how are they distributed?_)

### 2.3. Security Challenge

#### 2.3.1. Challenge Overview

(_Describe the new requirements introduced in the security challenge and how they impacted your original design._)

#### 2.3.2. Attacker Model

(_Define who is fully trusted, partially trusted, or untrusted._)

(_Define how powerful the attacker is, with capabilities and limitations, i.e., what can he do and what he cannot do_)

#### 2.3.3. Solution Design and Implementation

(_Explain how your team redesigned and extended the solution to meet the security challenge, including key distribution and other security measures._)

(_Identify communication entities and the messages they exchange with a UML sequence or collaboration diagram._)  

## 3. Conclusion

(_State the main achievements of your work._)

(_Describe which requirements were satisfied, partially satisfied, or not satisfied; with a brief justification for each one._)

(_Identify possible enhancements in the future._)

(_Offer a concluding statement, emphasizing the value of the project experience._)

## 4. Bibliography

(_Present bibliographic references, with clickable links. Always include at least the authors, title, "where published", and year._)

----
END OF REPORT
