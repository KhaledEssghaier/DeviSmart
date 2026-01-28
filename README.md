# DeviSmart

Application de gestion de devis et factures développée avec Spring Boot et Angular.

## Fonctionnalités

### Gestion des Devis
- Création et modification de devis
- Ajout de lignes de produits/services avec quantité et prix
- Calcul automatique des totaux (HT, TVA, TTC)
- Suivi du statut des devis (Brouillon, Envoyé, Validé, Refusé)
- Conversion de devis en factures

### Gestion des Factures
- Création manuelle de factures
- Génération automatique depuis les devis validés
- Numérotation automatique des factures
- Suivi du statut de paiement
- Historique des factures en cache

### Tableau de Bord
- Vue d'ensemble de l'activité
- Accès rapide aux actions principales
- Interface moderne et intuitive

## Technologies

### Backend
- Java 21
- Spring Boot 4.0.2
- Spring Data JPA
- Base de données H2 (développement)

### Frontend
- Angular 19
- Angular Material
- SCSS
- TypeScript

## Installation

### Prérequis
- Java 21+
- Node.js 18+
- Maven

### Backend
```bash
cd DeviSmart
./mvnw spring-boot:run
```
Le serveur démarre sur `http://localhost:8080`

### Frontend
```bash
cd angular-invoice
npm install
ng serve
```
L'application est accessible sur `http://localhost:4200`

## Structure du Projet

```
DeviSmart/
├── src/main/java/          # Code source Java (Spring Boot)
├── src/main/resources/     # Configuration et ressources
├── angular-invoice/        # Application Angular
│   ├── src/app/
│   │   ├── pages/          # Composants de pages
│   │   ├── services/       # Services Angular
│   │   └── models/         # Interfaces TypeScript
│   └── ...
└── pom.xml                 # Configuration Maven
```

## Licence

Projet développé dans un cadre de stage.
