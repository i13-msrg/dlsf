# DLSF - Distributed Ledger Simulation Framework

## Project Modules and Folders
This repository contains a number of sub-modules and folders. For more detailed information, refer to the paper.

Framework currently consists of 3 modules:
- __dlsf-core__: DLSF Core module.
- __dlsf-bitcoin__: DLSF Bitcoin module.
- __dlsf-boot__: DLSF Boot module.

Following modules prefixed with `x-` are part of example simulations: 
- __x-bitcoin-explorer__: Bitcoin explorer simulation’s module.
- __x-bitcoin-tx-protocols__: Bitcoin transaction protocol simulations’ module. It includes both Flood and Erlay simulations.
- __x-bitcoin-server__: This module is used to bootstrap the whole application. It includes all the example simulations and utilizes DLSF boot module.
- __x-bitcoin-client__: This module folder contains the web client’s code.

## Additional Files
###
- Research paper for DLSF can be found under `extras/paper`:
[Design and Implementation of an Extensible Large-Scale Blockchain Simulation Framework](extras/paper/DLSF-Design_and_Implementation_of_an_Extensible_Large-Scale_Blockchain_Simulation_Framework.pdf)

- Intellij code convention configuration file can be found under `extras/convention`.

## Running Instructions
### Prerequisites
- Java 11
- Maven 3
- NPM 6

### Server
To build the whole project, run the following at the project root:
```shell script
mvn clean install
```
Like any other maven multi-module project, the simulation system can be packaged and run in various ways. To start the server on local machine, navigate to __x-bitcoin-server__ folder and run the following command.
 

```shell script
mvn compile exec:java
```

### Client
Web client is an Angular application that can be built and run using npm scripts found in package.json file. To install dependencies and to serve client in development mode, navigate to the __x-bitcoin-client__ folder and run the following commands.

Install dependencies from npm:
```shell script
npm install
```

Serve client in development mode. Site should be accessible at localhost:4200.
```shell script
npm run start
```