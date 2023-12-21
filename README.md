# A34 MediTrack Project Read Me

## Team

| Number | Name                | User                                | E-mail                                     |
| -------|---------------------|-------------------------------------| -------------------------------------------|
| 99276  | Marta Félix         | <https://github.com/martafelix13>   | <marta.felix@tecnico.ulisboa.pt>           |
| 110859 | Luis Marques        | <https://github.com/oCaramelo>      | <luis.caramelo.marques@tecnico.ulisboa.pt> |
| 99221  | Francisco Gil Mata  | <https://github.com/franciscogmata> | <francisco.gil.mata@tecnico.ulisboa.pt>    |  

*(fill table above with team member information)*  

![Marta Félix](img/MartaFélix.jpeg) ![Luis Marques](img/LuisMarques.jpeg) ![Francisco Gil Mata](img/FranciscoGilMata.jpeg)

*(add face photos with 150px height; faces should have similar size and framing)*

## Contents

This repository contains documentation and source code for the *Network and Computer Security (SIRS)* project.

The [REPORT](REPORT.md) document provides a detailed overview of the key technical decisions and various components of the implemented project.
It offers insights into the rationale behind these choices, the project's architecture, and the impact of these decisions on the overall functionality and performance of the system.

This document presents installation and demonstration instructions.

*(adapt all of the following to your project, changing to the specific Linux distributions, programming languages, libraries, etc)*

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
(MATA PLS)

Now that all the networks and machines are up and running, ...


*(give a tour of the best features of the application; add screenshots when relevant)*

```sh
$ demo command
```

*(replace with actual commands)*

*(IMPORTANT: show evidence of the security mechanisms in action; show message payloads, print relevant messages, perform simulated attacks to show the defenses in action, etc.)*

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
