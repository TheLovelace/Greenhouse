ALTER TABLE usuario
    ADD COLUMN primeiro_acesso_feito BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE usuario
SET primeiro_acesso_feito = TRUE;
