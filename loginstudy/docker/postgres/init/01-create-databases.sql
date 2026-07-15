-- Runs only on first container initialization (empty data volume).
-- auth-server and member-service each own a dedicated database.
CREATE DATABASE loginstudy_auth OWNER loginstudy;
CREATE DATABASE loginstudy_member OWNER loginstudy;
