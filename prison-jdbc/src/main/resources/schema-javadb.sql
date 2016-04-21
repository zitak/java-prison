-- schema-javadb.sql
-- DDL commands for JavaDB/Derby

CREATE TABLE cell (
    id       INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    floor    INT,
    capacity INT
);

CREATE TABLE prisoner (
    id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255),
    surname VARCHAR(255),
    born DATE
);

CREATE TABLE sentence (
    prisonerId INT REFERENCES prisoner(id) ON DELETE CASCADE,
	  cellId INT REFERENCES cell(id) ON DELETE CASCADE,
    punishment VARCHAR(255),
    startDay DATE,
    endDay DATE
);