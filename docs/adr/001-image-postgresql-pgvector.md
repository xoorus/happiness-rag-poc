# ADR-001 : Image Docker PostgreSQL + pgvector

- **Date** : 2026-11-22
- **Statut** : Accepté

## Contexte

Le POC a besoin d'une base PostgreSQL avec l'extension pgvector pour stocker
et rechercher des embeddings. 
Elle doit tourner à l'identique sur Windows
(PC) et macOS (Mac M4), donc sur deux architectures : amd64 et arm64.

## Options envisagées

1. **Image officielle `pgvector/pgvector`, PostgreSQL 19**
    - Pour : pgvector déjà installé, multi-architecture, tag précis donc reproductible.
    - Contre : version majeure très récente, moins de retours d'expérience en cas de bug.
2. **Image `pgvector/pgvector`, PostgreSQL 17**
    - Pour : version éprouvée, chemin de données historique (`/var/lib/postgresql/data`).
    - Contre : plus ancienne.
3. **Image officielle `postgres` + installation manuelle de pgvector**
    - Pour : contrôle total.
    - Contre : build ou script d'installation à maintenir, aucun gain pour un POC.

## Décision

Option 1 : `pgvector/pgvector:0.8.7-pg19-trixie`, avec un tag précis (pas `latest`).

## Raisons

- Souhaite une base moderne pour le POC
- Le tag fixe garantit que le PC et le Mac utilisent exactement la même version.
- Risque inexistant, le POC ne tourne qu'en local sur des données fictive.

## Conséquences

- **Piège rencontré** : depuis PostgreSQL 18, le volume doit être monté sur
  `/var/lib/postgresql` et non plus sur `/var/lib/postgresql/data`. Sinon les
  données vont dans un volume anonyme et disparaissent quand le conteneur est
  recréé. Vérifié par un test (`down` puis `up`, la donnée survit).
- Si un bug lié à la version 19 bloque le projet, on repassera à la 17
  (changement de tag et de chemin de volume, puis réindexation).
- La base n'est exposée que sur `127.0.0.1:5433`, jamais sur le réseau.