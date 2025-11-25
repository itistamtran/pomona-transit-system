-- ============================================================
-- Create Database
-- ============================================================
DROP DATABASE IF EXISTS pomonatransit;
CREATE DATABASE pomonatransit;
USE pomonatransit;

-- ============================================================
-- Table: Trip
-- ============================================================
CREATE TABLE Trip (
    TripNumber INT PRIMARY KEY,
    StartLocationName VARCHAR(50),
    DestinationName VARCHAR(50)
);

-- ============================================================
-- Table: Bus
-- ============================================================
CREATE TABLE Bus (
    BusID INT PRIMARY KEY,
    Model VARCHAR(50),
    Year INT
);

-- ============================================================
-- Table: Driver
-- ============================================================
CREATE TABLE Driver (
    DriverName VARCHAR(50) PRIMARY KEY,
    DriverTelephoneNumber VARCHAR(20)
);

-- ============================================================
-- Table: Stop
-- ============================================================
CREATE TABLE Stop (
    StopNumber INT PRIMARY KEY,
    StopAddress VARCHAR(100)
);

-- ============================================================
-- Table: TripOffering
-- ============================================================
CREATE TABLE TripOffering (
    TripNumber INT,
    Date DATE,
    ScheduledStartTime TIME,
    ScheduledArrivalTime TIME,
    DriverName VARCHAR(50),
    BusID INT,
    PRIMARY KEY (TripNumber, Date, ScheduledStartTime),
    FOREIGN KEY (TripNumber) REFERENCES Trip(TripNumber),
    FOREIGN KEY (DriverName) REFERENCES Driver(DriverName),
    FOREIGN KEY (BusID) REFERENCES Bus(BusID)
);

-- ============================================================
-- Table: TripStopInfo
-- ============================================================
CREATE TABLE TripStopInfo (
    TripNumber INT,
    StopNumber INT,
    SequenceNumber INT,
    DrivingTime INT,
    PRIMARY KEY (TripNumber, StopNumber),
    FOREIGN KEY (TripNumber) REFERENCES Trip(TripNumber),
    FOREIGN KEY (StopNumber) REFERENCES Stop(StopNumber)
);

-- ============================================================
-- Table: ActualTripStopInfo
-- ============================================================
CREATE TABLE ActualTripStopInfo (
    TripNumber INT,
    Date DATE,
    ScheduledStartTime TIME,
    StopNumber INT,
    ScheduledArrivalTime TIME,
    ActualStartTime TIME,
    ActualArrivalTime TIME,
    NumberOfPassengerIn INT,
    NumberOfPassengerOut INT,
    PRIMARY KEY (TripNumber, Date, ScheduledStartTime, StopNumber),
    FOREIGN KEY (TripNumber) REFERENCES Trip(TripNumber),
    FOREIGN KEY (StopNumber) REFERENCES Stop(StopNumber)
);

-- ============================================================
-- Sample Data
-- ============================================================

-- Trip
INSERT INTO Trip VALUES
(1, 'Pomona', 'Claremont'),
(2, 'Pomona', 'Montclair'),
(3, 'Pomona', 'Los Angeles'),
(4, 'Claremont', 'Pomona');

-- Bus
INSERT INTO Bus VALUES
(10, 'Volvo X12', 2018),
(11, 'Mercedes M45', 2020),
(100, 'Volvo X1', 2015),
(200, 'Mercedes M2', 2018),
(300, 'BlueBird B3', 2012);

-- Driver
INSERT INTO Driver VALUES
('John Doe', '909-111-2222'),
('Sarah Kim', '909-333-4444'),
('Alice Johnson', '909-555-0001'),
('Bob Smith', '909-555-0002'),
('Carol Lee', '909-555-0003');

-- Stops
INSERT INTO Stop VALUES
(100, 'Pomona Transit Center'),
(101, 'Claremont Village'),
(102, 'Montclair Plaza'),
(1, 'Disneyland Station'),
(2, 'Downtown Pomona'),
(3, 'Claremont Station'),
(4, 'Los Angeles Union Station');

-- Trip Offering
INSERT INTO TripOffering VALUES
(1, '2026-01-01', '08:00:00', '08:30:00', 'John Doe', 10),
(1, '2026-01-01', '12:00:00', '12:30:00', 'Sarah Kim', 11),
(2, '2026-01-01', '09:00:00', '09:40:00', 'John Doe', 10),
(1, '2025-11-30', '08:00:00', '08:35:00', 'Alice Johnson', 100),
(1, '2025-11-28', '09:00:00', '09:35:00', 'Bob Smith', 200),
(2, '2025-11-27', '07:30:00', '08:20:00', 'Carol Lee', 300),
(3, '2025-12-30', '10:00:00', '10:30:00', 'Alice Johnson', 100);

-- Trip Stop Info
INSERT INTO TripStopInfo VALUES
(1, 100, 1, 10),
(1, 101, 2, 15),
(2, 100, 1, 12),
(2, 102, 2, 18),
(1, 1, 1, 10),
(1, 2, 2, 10),
(1, 3, 3, 15),
(2, 1, 1, 10),
(2, 4, 2, 40),
(3, 3, 1, 15),
(3, 1, 2, 15);

-- Actual Trip Stop Info
INSERT INTO ActualTripStopInfo VALUES
(1, '2025-01-01', '08:00:00', 100, '08:10:00', '08:09:00', '08:29:00', 5, 2),
(1, '2025-01-01', '08:00:00', 101, '08:25:00', '08:24:00', '08:31:00', 3, 1);
