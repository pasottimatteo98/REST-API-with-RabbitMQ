# REST API with RabbitMQ

A Java implementation of a REST API server that uses RabbitMQ for message handling, providing CRUD operations for a hardware inventory system.

## Project Overview

This project implements a REST API server that handles HTTP requests (GET, POST, PUT, DELETE) and processes them through a RabbitMQ message queue. The application demonstrates Remote Procedure Call (RPC) pattern using RabbitMQ as the messaging broker.

The system manages "Ferramenta" (Hardware) items with CRUD operations:
- **Create**: Add new hardware items
- **Read**: Retrieve hardware items
- **Update**: Modify existing hardware items
- **Delete**: Remove hardware items

## Components

- **RESTHttpServer**: HTTP server accepting REST requests on port 3000 (configurable)
- **RPCClient**: Handles HTTP requests and forwards them to RabbitMQ
- **RPCServer**: Listens to RabbitMQ queue and processes requests
- **CRUD**: Implements the CRUD operations logic
- **Ferramenta**: Data model for hardware items
- **Request**: Data structure for handling requests

## Prerequisites

- Java 8 or higher
- RabbitMQ server
- Required dependencies:
  - RabbitMQ Java client (amqp-client-5.7.1.jar)
  - SLF4J API (slf4j-api-1.7.26.jar)
  - SLF4J Simple (slf4j-simple-1.7.26.jar)
  - Google Gson

## Setup

1. Ensure RabbitMQ is installed and running
2. Configure RabbitMQ with:
   - Username: admin
   - Password: admin
3. Place all required JAR files in the same directory as the Java files

## Running the Application

1. Start the RPC Server:
   ```bash
   ./rpcserver.bash
   ```
   or
   ```bash
   java -cp .:amqp-client-5.7.1.jar:slf4j-api-1.7.26.jar:slf4j-simple-1.7.26.jar RPCServer
   ```

2. Start the REST HTTP Server:
   ```bash
   ./rpcclient.bash
   ```
   or
   ```bash
   java -cp .:amqp-client-5.7.1.jar:slf4j-api-1.7.26.jar:slf4j-simple-1.7.26.jar RESTHttpServer
   ```

   To specify a custom port:
   ```bash
   java -cp .:amqp-client-5.7.1.jar:slf4j-api-1.7.26.jar:slf4j-simple-1.7.26.jar RESTHttpServer -port <port-number>
   ```

## API Usage

### Base URL
`http://localhost:3000/index`

### Endpoints

#### GET
- List all paths: `/index`
- Get a specific item: `/index/path?type=<item-name>&version=<version-number>`
  - The version parameter is optional (defaults to 1.0)
  - Returns JSON representation of the item

#### POST
- Create a new item: `/index/path?type=<item-name>&id=<id>&N=<number>&Usato=<true|false>&version=<version-number>`
  - Required parameters: type, id, N
  - Optional parameters: Usato (default: false), version (default: 1.0)

#### PUT
- Update an existing item: `/index/path?type=<item-name>&id=<new-id>&N=<new-number>&version=<version-number>`
  - Required parameters: type, new values for fields to update
  - Optional parameter: version (default: 1.0)

#### DELETE
- Delete an item: `/index/path?type=<item-name>`

## Data Storage

Items are stored as JSON files in the `./Root` directory, following the path structure specified in requests.

## Data Model

The `Ferramenta` class represents hardware items with the following properties:
- `id` (String): Identifier
- `N` (int): Quantity
- `Usato` (boolean): Whether the item is used (only in version 1.1+)

## Version Support

The API supports versioning:
- Version 1.0: Basic fields (id, N)
- Version 1.1: Adds the 'Usato' field

## Notes

- All operations return JSON responses
- File paths are relative to the `./Root` directory
- The system uses Gson for JSON serialization/deserialization
