CREATE TABLE kotlin_webdemo.users
(
  id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  picture VARCHAR(255),
  gid VARCHAR(255) NOT NULL
);
CREATE UNIQUE INDEX users_email_uindex ON kotlin_webdemo.users (email);
CREATE UNIQUE INDEX users_gid_uindex ON kotlin_webdemo.users (gid);