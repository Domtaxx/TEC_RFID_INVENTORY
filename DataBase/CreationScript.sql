-- Table to store roles
CREATE TABLE EMPLOYEE_ROLE (
    ID int NOT NULL IDENTITY(1,1),
    ROLE_NAME varchar(64) NOT NULL,
    SUMMARY varchar(256),
    PRIMARY KEY (ID)
);

CREATE TABLE DEPARTMENT (
    ID int NOT NULL IDENTITY(1,1),
    DEPARTMENT_NAME varchar(256) NOT NULL,
    PRIMARY KEY (ID)
);
-- Table to store rooms for each department
CREATE TABLE ROOM (
    ID int NOT NULL IDENTITY(1,1),  -- Auto-incremesnting room ID
    ROOM_NAME varchar(64) NOT NULL,  -- Name or number of the room
    ID_DEPARTMENT int NOT NULL,  -- Foreign key to link room to a department
    PRIMARY KEY (ID),
    CONSTRAINT FK_ROOM_DEPARTMENT FOREIGN KEY (ID_DEPARTMENT) REFERENCES DEPARTMENT(ID) ON DELETE CASCADE
);
CREATE TABLE EMPLOYEE (
    ID int NOT NULL IDENTITY(1,1),
    USER_PASSWORD BINARY(256) NOT NULL, -- Use BINARY(60) for hashed passwords (e.g., bcrypt)
    EMAIL varchar(255) NOT NULL UNIQUE,
    SSN varchar(20) NOT NULL UNIQUE, -- Allowing for longer SSNs
    FIRST_NAME varchar(50) NOT NULL, -- Allowing more characters
    SURNAME varchar(50) NOT NULL,    -- Allowing more characters
    ID_DEPARTMENT int,  -- Foreign key to link employee to one department
    ID_ROLE int,  -- Foreign key to link employee to one role
    PRIMARY KEY (ID),
    CONSTRAINT FK_EMPLOYEE_DEPARTMENT FOREIGN KEY (ID_DEPARTMENT) REFERENCES DEPARTMENT(ID) ON DELETE SET NULL, -- If department is deleted, set department to NULL
    CONSTRAINT FK_EMPLOYEE_ROLE FOREIGN KEY (ID_ROLE) REFERENCES EMPLOYEE_ROLE(ID) ON DELETE SET NULL -- If role is deleted, set role to NULL
);

CREATE TABLE ITEM_STATES (
    ID int NOT NULL IDENTITY(1,1),
    STATE_NAME varchar(32),
    PRIMARY KEY (ID)
);

CREATE TABLE ITEM (
    ID int NOT NULL IDENTITY(1,1),
    SERIAL_NUMBER varchar(32),
    ITEM_NAME varchar(64) NOT NULL,
    SUMMARY varchar(512),
    PLACA varchar(32),
    ID_DEPARTMENT int,
    NFS varchar(128),
    ID_STATE int,
    RESPONSIBLE_EMAIL varchar(256),
    PRIMARY KEY (ID),
    CONSTRAINT FK_ITEM_DEPARTMENT FOREIGN KEY (ID_DEPARTMENT) REFERENCES DEPARTMENT(ID) ON DELETE SET NULL,
    CONSTRAINT FK_ITEM_STATE FOREIGN KEY (ID_STATE) REFERENCES ITEM_STATES(ID) ON DELETE SET NULL
);

CREATE INDEX IDX_ITEM_DEPARTMENT ON ITEM(ID_DEPARTMENT); -- Adding index for the foreign key

CREATE TABLE ITEM_REGISTRY (
    ID_EMPLOYEE int NOT NULL,
    ID_ITEM int NOT NULL,
    REGISTRY_DATE DATETIME NOT NULL,
    ID_ROOM int NOT NULL,  -- Foreign key to reference a room
    PRIMARY KEY (ID_EMPLOYEE, ID_ITEM, REGISTRY_DATE),
    CONSTRAINT FK_ITEM_REGISTRY_EMPLOYEE FOREIGN KEY (ID_EMPLOYEE) REFERENCES EMPLOYEE(ID) ON DELETE CASCADE,
    CONSTRAINT FK_ITEM_REGISTRY_ITEM FOREIGN KEY (ID_ITEM) REFERENCES ITEM(ID) ON DELETE CASCADE,
    CONSTRAINT FK_ITEM_REGISTRY_ROOM FOREIGN KEY (ID_ROOM) REFERENCES ROOM(ID) ON DELETE CASCADE
);

CREATE INDEX IDX_ITEM_REGISTRY_EMPLOYEE ON ITEM_REGISTRY(ID_EMPLOYEE);
CREATE INDEX IDX_ITEM_REGISTRY_ITEM ON ITEM_REGISTRY(ID_ITEM);

-- Optional index for optimizing department and role-based lookups
CREATE INDEX IDX_EMPLOYEE_DEPARTMENT ON EMPLOYEE(ID_DEPARTMENT);
CREATE INDEX IDX_EMPLOYEE_ROLE ON EMPLOYEE(ID_ROLE);

-- Inserting departments
INSERT INTO DEPARTMENT (DEPARTMENT_NAME) VALUES ('ESCUELA INGENIERÍA EN COMPUTADORES');
-- Inserting roles
INSERT INTO EMPLOYEE_ROLE (ROLE_NAME, SUMMARY) VALUES ('ADMIN', 'ACCESO COMPLETO');
INSERT INTO EMPLOYEE_ROLE (ROLE_NAME, SUMMARY) VALUES ('REGULAR', 'ACCESO BASICO');

INSERT INTO ITEM_STATES (STATE_NAME) VALUES ('E.O.');
INSERT INTO ITEM_STATES (STATE_NAME) VALUES ('Inactivo');

-- Inserting employees (Assuming role and department IDs exist)
INSERT INTO EMPLOYEE (USER_PASSWORD, EMAIL, SSN, FIRST_NAME, SURNAME, ID_DEPARTMENT, ID_ROLE) 
VALUES (HASHBYTES('SHA2_256', '123456789'), 'briwag88@estudiantec.cr', '118050449', 'Brian', 'Wagemans', 1, 1);

INSERT INTO EMPLOYEE (USER_PASSWORD, EMAIL, SSN, FIRST_NAME, SURNAME, ID_DEPARTMENT, ID_ROLE) 
VALUES (HASHBYTES('SHA2_256', '123456789'), 'jleiton@itcr.ac.cr', '116110036', 'Jason', 'Leiton', 1, 1);

INSERT INTO ITEM (ITEM_NAME, SUMMARY, ID_DEPARTMENT, NFS, ID_STATE, RESPONSIBLE_EMAIL) 
VALUES ('Laptop', 'Dell Latitude 5400', 1, '1', 1, 'briwag88@estudiantec.cr');

INSERT INTO ITEM (ITEM_NAME, SUMMARY, ID_DEPARTMENT, NFS, ID_STATE, RESPONSIBLE_EMAIL) 
VALUES ('Projector', 'Epson PowerLite', 1, '2', 1, 1);

INSERT INTO ROOM (ROOM_NAME, ID_DEPARTMENT) 
VALUES ('F2-07', 1);  

INSERT INTO ROOM (ROOM_NAME, ID_DEPARTMENT) 
VALUES ('F5-01', 1);  

INSERT INTO ROOM (ROOM_NAME, ID_DEPARTMENT) 
VALUES ('F2-02', 1);  

INSERT INTO ITEM_REGISTRY (ID_EMPLOYEE, ID_ITEM, REGISTRY_DATE, ID_ROOM)
VALUES (1, 1, '2024-10-01 09:00:00', 1);

INSERT INTO ITEM_REGISTRY (ID_EMPLOYEE, ID_ITEM, REGISTRY_DATE, ID_ROOM)
VALUES (1, 2, '2024-10-02 14:00:00', 1);

INSERT INTO ITEM_REGISTRY (ID_EMPLOYEE, ID_ITEM, REGISTRY_DATE, ID_ROOM)
VALUES (1, 2, '2024-10-02 14:00:01', 1);