CREATE DATABASE kotlin_webdemo;
CREATE USER 'kotlin_webdemo'@'localhost' IDENTIFIED BY 'password';
GRANT SELECT,INSERT,UPDATE,DELETE ON kotlin_webdemo.* TO 'kotlin_webdemo'@'localhost';
FLUSH PRIVILEGES;