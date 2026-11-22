# ADR-002 : Spring Data Type

- **Date** : 2026-11-22
- **Statut** : Accepté

## Contexte

Le POC a besoin d'une base PostgreSQL avec l'extension pgvector pour stocker
et rechercher des embeddings.

## Options envisagées

1. **JPA**
2. **JDBC**

## Décision

Option 2 : JDBC
- Le type vector et l'opérateur <=> ne sont pas gérés nativement par JPA.
- Plus formateur d'écrire le SQL à la main
- Pour la recherche vectorielle, il aurait fallu contourner JPA avec des requêtes natives, aucun intérêt.
