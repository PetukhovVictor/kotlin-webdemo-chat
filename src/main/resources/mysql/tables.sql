/* Users table */
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

/* Dialogs table */
CREATE TABLE kotlin_webdemo.dialogs
(
  id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  owner_id INT NOT NULL,
  creation_date DATETIME DEFAULT CURRENT_TIMESTAMP() NOT NULL,
  title VARCHAR(255),
  last_update_date DATETIME DEFAULT CURRENT_TIMESTAMP() NOT NULL,
  CONSTRAINT dialogs_users_id_fk FOREIGN KEY (owner_id) REFERENCES users (id)
);

/* Dialog participants table */
CREATE TABLE kotlin_webdemo.dialog_participants
(
  id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  dialog_id INT NOT NULL,
  participant_id INT NOT NULL,
  join_date DATETIME DEFAULT CURRENT_TIMESTAMP() NOT NULL,
  CONSTRAINT dialog_participants_dialogs_id_fk FOREIGN KEY (dialog_id) REFERENCES dialogs (id),
  CONSTRAINT dialog_participants_users_id_fk FOREIGN KEY (participant_id) REFERENCES users (id)
);

/* Dialog messages table */
CREATE TABLE kotlin_webdemo.dialog_messages
(
  id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  dialog_id INT NOT NULL,
  author_id INT NOT NULL,
  message TEXT NOT NULL,
  date DATETIME DEFAULT CURRENT_TIMESTAMP() NOT NULL,
  CONSTRAINT dialog_messages_dialogs_id_fk FOREIGN KEY (dialog_id) REFERENCES dialogs (id),
  CONSTRAINT dialog_messages_users_id_fk FOREIGN KEY (author_id) REFERENCES users (id)
);