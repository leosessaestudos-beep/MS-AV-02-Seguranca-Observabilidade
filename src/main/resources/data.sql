-- Usuarios iniciais para teste (senha com no maximo 10 caracteres)
INSERT INTO usuarios (username, senha) VALUES ('admin', 'admin123');
INSERT INTO usuarios (username, senha) VALUES ('manutencao', 'manut123');
INSERT INTO usuarios (username, senha) VALUES ('usuario', 'user123');

INSERT INTO authorities (username, authority) VALUES ('admin', 'ROLE_ADMIN');
INSERT INTO authorities (username, authority) VALUES ('manutencao', 'ROLE_MANUTENCAO');
INSERT INTO authorities (username, authority) VALUES ('usuario', 'ROLE_USER');
