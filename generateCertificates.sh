#!/bin/bash


echo "Generating the database key pair..."
openssl genrsa -out database.key

echo "Generating the api key pair..."
openssl genrsa -out api.key

echo "Generating a self-signed certificate..."
openssl req -new -key database.key -out database.csr

echo "Generating a self-signed certificate..."
openssl req -new -key api.key -out api.csr

--password: sirs2024

echo "Self-signing..."
openssl x509 -req -days 365 -in database.csr -signkey database.key -out database.crt

echo "creating database for the certificate..."
echo 01 > database.srl

echo "Signing API using a the server CA..."
openssl x509 -req -days 365 -in api.csr -CA database.crt -CAkey database.key -out api.crt

echo "Converting certificates..."
openssl x509 -in database.crt -out database.pem
openssl x509 -in api.crt -out api.pem

echo "creating p12 file..."
openssl pkcs12 -export -in database.crt -inkey database.key -out database.p12
openssl pkcs12 -export -in api.crt -inkey api.key -out api.p12


echo "Importing certificates..."
keytool -import -trustcacerts -file api.pem -keypass changeme -storepass changeme -keystore databasetruststore.jks

keytool -import -trustcacerts -file database.pem -keypass changeme -storepass changeme -keystore apitruststore.jks