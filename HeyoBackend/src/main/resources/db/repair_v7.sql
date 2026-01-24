-- SQL Script to repair failed Flyway migration V7
-- Run this script manually in your MySQL database if migration V7 failed

-- Delete the failed migration record from flyway_schema_history
DELETE FROM flyway_schema_history WHERE version = '7' AND success = 0;

-- After running this script, restart the Spring Boot application
-- Flyway will re-run the migration V7
