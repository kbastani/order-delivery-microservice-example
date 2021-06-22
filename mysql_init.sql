CREATE DATABASE orderweb;
GRANT ALL PRIVILEGES ON orderweb.* TO 'mysqluser'@'%';
SET GLOBAL max_connections = 1000;
