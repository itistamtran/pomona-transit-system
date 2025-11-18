# Pomona Transit System Management

A desktop application built with **JavaFX**, **MySQL** and **JDBC** for managing the Pomona Transit System database.

## Overview

This project provides a simple management interface for bus routes, stops, trip schedules, drivers, and actual trip stop information.  
It uses **JavaFX** for the user interface, **MySQL** as the backend database, and **JDBC** for database access.

## Features

- JavaFX based GUI
- MySQL integration using JDBC
- Add, view, and update transit system data
- Structured screens for each required operation
- Basic CSS styling support
- Modular Java package layout

## Technologies Used

- **Java 25+**
- **JavaFX 25**
- **MySQL Community Server**
- **JDBC (mysql-connector-j)**
- **VS Code**

## Project Structure

````txt
pomona-transit-system/
│
├── bin/ # Compiled Java class files (not committed)
├── lib/ # External libraries (e.g., MySQL Connector JAR)
│
├── src/
│ ├── database/ # Database connection and helper classes
│ └── screens/ # All JavaFX UI screens
│ ├── AddActualTripStopInfo.java
│ ├── AddBus.java
│ ├── AddDriver.java
│ ├── DeleteBus.java
│ ├── DisplayStops.java
│ ├── DisplayTripSchedule.java
│ ├── EditTripSchedule.java
│ ├── HomePage.java
│ └── WeeklyScheduleOfDriver.java
│
├── Main.java # JavaFX application entry point
├── styles.css # Global stylesheet for JavaFX UI
│
├── pomonatransit_setup.sql # SQL script to create & populate the database
│
├── config.properties # Database configuration (JDBC URL, user, pass)
├── .gitignore # Files and folders excluded from Git
├── .classpath # VS Code / Eclipse classpath config
└── .project # VS Code / Eclipse project config


## Database Setup

This project uses MySQL to store transit system data. The SQL script required to create the database and all necessary tables is included in the project.

1. Create a new database

```sql
CREATE DATABASE pomona_transit;
````

2. Select the new database:

```sql
USE pomona_transit;
```

3. Run the script located at the root of the project:

```sql
pomonatransit_setup.sql;
```

4. Configure Database Connection
   Edit the `config.properties` file located in the project root:

```bash
db.url=jdbc:mysql://localhost:3306/pomonatransit
db.user=your_mysql_username
db.password=your_mysql_password
```

5. Add MySQL Connector

Place the MySQL JDBC driver JAR in the `lib/` folder and ensure your IDE includes it in the classpath.
Download from [https://dev.mysql.com/downloads/connector/j/](https://dev.mysql.com/downloads/connector/j/)

## Running the Application

1. Combine program

```bash
javac \
  --module-path /Users/tamtran/javafx/javafx-sdk-25.0.1/lib \
  --add-modules javafx.controls,javafx.fxml \
  -cp "lib/mysql-connector-j.jar" \
  -d bin \
  src/*.java src/database/*.java src/screens/*.java
```

2. Run program

```bash
java \
  --module-path /path/to/javafx/lib \
  --add-modules javafx.controls,javafx.fxml \
  -cp "bin:lib/mysql-connector-j.jar" \
  Main
```

## System Requirements Based on Assignment

The system supports:

- Display schedule of all trips for a start location, destination, and date.
- Edit the TripOffering table
- Delete a trip offering
- Add multiple trip offerings
- Change the driver
- Change the bus
- Display stops of a given trip
- Display weekly schedule of a driver
- Add a driver
- Add a bus
- Delete a bus
- Record actual trip stop info
