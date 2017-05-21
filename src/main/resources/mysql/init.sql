CREATE DATABASE kotlin_webdemo DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
CREATE USER 'kotlin_webdemo'@'localhost' IDENTIFIED BY 'password';
GRANT SELECT,INSERT,UPDATE,DELETE ON kotlin_webdemo.* TO 'kotlin_webdemo'@'localhost';
FLUSH PRIVILEGES;