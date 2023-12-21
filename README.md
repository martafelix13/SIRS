# A34 MediTrack Project Read Me

## Team

| Number | Name                | User                                | E-mail                                     |
| -------|---------------------|-------------------------------------| -------------------------------------------|
| 99276  | Marta Félix         | <https://github.com/martafelix13>   | <marta.felix@tecnico.ulisboa.pt>           |
| 110859 | Luis Marques        | <https://github.com/oCaramelo>      | <luis.caramelo.marques@tecnico.ulisboa.pt> |
| 99221  | Francisco Gil Mata  | <https://github.com/franciscogmata> | <francisco.gil.mata@tecnico.ulisboa.pt>    |  

*![Marta Félix](img/MartaFelix.jpg) ![Luis Marques](img/LuisMarques.jpg) ![Francisco Gil Mata](img/FranciscoGilMata.jpg)

## Contents

This repository contains documentation and source code for the *Network and Computer Security (SIRS)* project.

The [REPORT](REPORT.md) document provides a detailed overview of the key technical decisions and various components of the implemented project.
It offers insights into the rationale behind these choices, the project's architecture, and the impact of these decisions on the overall functionality and performance of the system.

This document presents installation and demonstration instructions.

## Installation

To see the project in action, it is necessary to setup a virtual environment, with 2 networks and 4 machines.  

The following diagram shows the networks and machines:



*(include diagram from discord)*

### Prerequisites

All the virtual machines are based on: Linux 64-bit, Kali 2023.3  

1. Download VirtualBox at [Virtual Box](https://www.virtualbox.org) official website;
2. Install VirtualBox following [instructions from the manual](https://www.virtualbox.org/manual/ch02.html);
3. Create a new Virtual Machine following the [Kali inside VirtualBox tutorial](https://www.kali.org/docs/virtualization/install-virtualbox-guest-vm/);
4. Install Kali Linux inside the VM, following the [Kali Setup tutorial](KaliSetup.md).

Clone the base machine to create the other machines.
1. Make sure that the machine about to be cloned is powered off
2. Select the machine with the right button of your mouse and click Clone
3. Select the name you want to give to the new machine (we suggest using the names gives above to simplify the experience)
4. MAC Adress Policy: Generate new MAC addresses for all network adapters
5. Select Linked Clone


### Machine configurations

For each machine, there is an initialization script with the machine name, with prefix `init-` and suffix `.sh`, that installs all the necessary packages and makes all required configurations in the a clean machine.

Inside each machine, use Git to obtain a copy of all the scripts and code.

```sh
$ git clone https://github.com/tecnico-sec/a34-francisco-marta-luis.git
```

Next we have custom instructions for each machine.

#### Machine 1 - Database

The Database Machine serves as the host for the SQLite database and operates a Java program with a socket that listens for incoming requests from the API Machine. The Java program processes these requests, interacts with the SQLite database, and sends formatted responses back through the same socket channel. 

To setup the machine:
```sh
$ setup command
```

*(replace with actual commands)*

To test:
```sh
$ test command
```

*(replace with actual commands)*

The expected results are ...

If you receive the following message ... then ...

*(explain how to fix some known problem)*

#### Machine 2 - API Meditrack
The server machine is central to processing client requests, converting them into database queries, ensuring secure communication via client-side secure sockets, and managing HTTPS interactions with the Client, through the DMZ. It handles decryption of incoming client requests, encrypts responses, and employs RSA-based authentication. The software stack includes secure sockets, HTTPS communication, request decryption, response encryption, and robust authentication.

To setup the machine:
```sh
$ setup command
```

*(replace with actual commands)*

To test:
```sh
$ test command
```

*(replace with actual commands)*

The expected results are ...

If you receive the following message ... then ...

*(explain how to fix some known problem)*


#### Machine 3 - DMZ
(LUIS)
The server machine is central to processing client requests, converting them into database queries, ensuring secure communication via client-side secure sockets, and managing HTTPS interactions with the Client, through the DMZ. It handles decryption of incoming client requests, encrypts responses, and employs RSA-based authentication. The software stack includes secure sockets, HTTPS communication, request decryption, response encryption, and robust authentication.

To setup the machine:
```sh
$ setup command
```

*(replace with actual commands)*

To test:
```sh
$ test command
```

*(replace with actual commands)*

The expected results are ...

If you receive the following message ... then ...

*(explain how to fix some known problem)*


#### Machine 4 - Client [Patient and Doctor]
The client machine initiates requests to the server and processes encrypted responses. Its software encompasses a client-side application responsible for sending requests, decrypting received data, and providing users with an interactive experience for request processing and authentication. This introduces an abstraction and a security layer to enhance the project.

To setup the machine:
```sh
$ setup command
```

*(replace with actual commands)*

To test:
```sh
$ test command
```

This is list of usernames in the database for testing functionalities. 
Note: The input is case-sensitive

- Patients 
    - Bob
    - Alice
    - Charlie

- Doctors
    - Dr.Smith
    - Dr.Martins
    - Dr.Johnson
    - Dr.White
    - Dr.Davis
    - Dr.Brown
    - Dr.Lee

*(replace with actual commands)*

The expected results are ...

If you receive the following message ... then ...

*(explain how to fix some known problem)*

## Demonstration


Now that all the networks and machines are up and running, let's go through that main features of the MediTrack Platform

1- First exectute the java script on each machine (Database, Api and Client)
```sh
$ mvn compile exec: java
```

2- On the Client Machine you should see this Menu

![image](https://github.com/tecnico-sec/a34-francisco-marta-luis/assets/15965849/c01c634a-ce23-45da-bb64-009466fbcaa7)

3- Select what type of Client are you (Patient or Doctor)

4- Enter the name of your Client and the authentication process begins:

 -4.1 The Api send the Client and encrypted challage, with the Client's Public Key
    
![image](https://github.com/tecnico-sec/a34-francisco-marta-luis/assets/15965849/0160c23d-dc42-4fda-a77b-f0600b900ba3)


 -4.2 The Client recive the and decrypts it with his Private Key
    
![image](https://github.com/tecnico-sec/a34-francisco-marta-luis/assets/15965849/67d76ee8-eaf8-4c40-ad20-a70fa239fe37)

 -4.3 The Api check if the challenge was corrected decrypted and authenticates the Client
    
![image](https://github.com/tecnico-sec/a34-francisco-marta-luis/assets/15965849/67749b08-ce81-40ac-8135-c784732b9599)
    
5- Select and option on the Client Menu and see the results pulled from the database

![image](https://github.com/tecnico-sec/a34-francisco-marta-luis/assets/15965849/759cec12-67b9-4db2-ac96-7ad78fff1771)

6- All the communications are protected and unprotect using the Secure Docuemnts Library

![image](https://github.com/tecnico-sec/a34-francisco-marta-luis/assets/15965849/4875be41-79f2-45cb-b28c-8f8de133265a)

This concludes the demonstration.

## Additional Information

### Links to Used Tools and Libraries

- [Java 11.0.16.1](https://openjdk.java.net/)
- [Maven 3.9.5](https://maven.apache.org/)
- [SQLite 3.x](https://www.sqlite.org/)
- [Log4j 1.2.17](https://logging.apache.org/log4j/1.2/)
- [Gson 2.10.1](https://github.com/google/gson)
- [SecureDocument](https://github.com/tecnico-sec/a34-francisco-marta-luis/tree/29b64fcd269181bc1f49fdd212f720c93c48c16d/SecureDocument)

### Versioning

We use [SemVer](http://semver.org/) for versioning.  

### License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) for details.

----
END OF README
