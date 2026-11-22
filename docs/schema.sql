-- Modelagem do StreamStats usando o MySQL 8
-- Usando spring.jpa.hibernate.ddl-auto=update por que o Hibernate já cria as tabelas sozinho.


-- script DDL

CREATE DATABASE IF NOT EXISTS streamstats
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE streamstats;


-- Usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    nome          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    -- BCrypt, para que a senha nunca venha em texto
    senha_hash    VARCHAR(255) NOT NULL,           
    data_criacao  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuarios_email (email)
);

-- Lista das midias 
CREATE TABLE IF NOT EXISTS midias (
    id                          BIGINT        NOT NULL AUTO_INCREMENT,
    titulo                      VARCHAR(200)  NOT NULL,
    tipo                        VARCHAR(20)   NOT NULL,  
    genero                      VARCHAR(80),
    ano                         INT,
    total_episodios             INT           NOT NULL DEFAULT 1,
    duracao_media_episodio_min  INT           NOT NULL,
    sinopse                     VARCHAR(1000),
    capa_url                    VARCHAR(500),
    PRIMARY KEY (id),
    CONSTRAINT ck_midias_episodios CHECK (total_episodios >= 1),
    CONSTRAINT ck_midias_duracao   CHECK (duracao_media_episodio_min >= 1)
);

-- Lista pessoal por usuário
CREATE TABLE IF NOT EXISTS itens_lista (
    id                    BIGINT      NOT NULL AUTO_INCREMENT,
    usuario_id            BIGINT      NOT NULL,
    midia_id              BIGINT      NOT NULL,
    -- QUERO_ASSISTIR | ASSISTINDO | ASSISTIDO
    status                VARCHAR(20) NOT NULL,   
    episodios_assistidos  INT         NOT NULL DEFAULT 0,
    -- 1 a 10, apenas depois de começar a assistir
    nota                  INT         NULL,       
    data_atualizacao      DATE        NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuario_midia (usuario_id, midia_id),
    CONSTRAINT fk_itens_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_itens_midia   FOREIGN KEY (midia_id)   REFERENCES midias (id)   ON DELETE CASCADE,
    CONSTRAINT ck_itens_nota    CHECK (nota IS NULL OR nota BETWEEN 1 AND 10)
);
