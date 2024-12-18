-- Drop ITEM_REGISTRY table first (dependent on ITEM, EMPLOYEE, and ROOM)
DROP TABLE IF EXISTS ITEM_REGISTRY;

-- Drop ITEM table next (dependent on DEPARTMENT, EMPLOYEE, and STATE)
DROP TABLE IF EXISTS ITEM;

-- Drop ROOM table (dependent on DEPARTMENT)
DROP TABLE IF EXISTS ROOM;

-- Drop EMPLOYEE table (dependent on ROLE and DEPARTMENT)
DROP TABLE IF EXISTS EMPLOYEE;

-- Drop STATE table (independent)
DROP TABLE IF EXISTS ITEM_STATES;

-- Drop ROLE table (independent)
DROP TABLE IF EXISTS EMPLOYEE_ROLE;

-- Drop DEPARTMENT table (independent)
DROP TABLE IF EXISTS DEPARTMENT;

-- Drop indexes, specify the table and index name
DROP INDEX IF EXISTS IDX_ITEM_DEPARTMENT ON ITEM;
DROP INDEX IF EXISTS IDX_ITEM_REGISTRY_EMPLOYEE ON ITEM_REGISTRY;
DROP INDEX IF EXISTS IDX_ITEM_REGISTRY_ITEM ON ITEM_REGISTRY;
DROP INDEX IF EXISTS IDX_EMPLOYEE_DEPARTMENT ON EMPLOYEE;
DROP INDEX IF EXISTS IDX_EMPLOYEE_ROLE ON EMPLOYEE;