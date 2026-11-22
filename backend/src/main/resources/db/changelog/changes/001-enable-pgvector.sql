--liquibase formatted sql

--changeset bastien:001-enable-pgvector
CREATE EXTENSION IF NOT EXISTS vector;