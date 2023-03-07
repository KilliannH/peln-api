-- Insert roles into Roles table

INSERT INTO roles(name) VALUES('ROLE_USER');
INSERT INTO roles(name) VALUES('ROLE_MODERATOR');
INSERT INTO roles(name) VALUES('ROLE_ADMIN');

-- Insert admin in Users table
INSERT INTO users(username, email, password) VALUES('admin', 'admin@test.fr', '$2a$10$NZbKlm2ACGXX4p27XWRjFu1aHE5KyWmAgjwQDkhAV4Wccgx4GbNDS');