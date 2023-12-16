#!/bin/bash


echo "Generating the server key pair..."
openssl genrsa -out server.key

echo "Generating the client key pair..."
openssl genrsa -out client.key

echo "Generating a self-signed certificate..."
openssl req -new -key server.key -out server.csr

echo "Generating a self-signed certificate..."
openssl req -new -key client.key -out client.csr

--password: sirs2024

echo "Self-signing..."
openssl x509 -req -days 365 -in server.csr -signkey server.key -out server.crt

echo "creating server for the certificate..."
echo 01 > server.srl

echo "Signing client using a the server CA..."
openssl x509 -req -days 365 -in client.csr -CA server.crt -CAkey server.key -out client.crt

echo "Converting certificates..."
openssl x509 -in server.crt -out server.pem
openssl x509 -in client.crt -out client.pem

echo "creating p12 file..."
openssl pkcs12 -export -in server.crt -inkey server.key -out server.p12
openssl pkcs12 -export -in client.crt -inkey client.key -out client.p12


echo "Importing certificates..."
keytool -import -trustcacerts -file client.pem -keypass changeme -storepass changeme -keystore servertruststore.jks

keytool -import -trustcacerts -file server.pem -keypass changeme -storepass changeme -keystore clienttruststore.jks