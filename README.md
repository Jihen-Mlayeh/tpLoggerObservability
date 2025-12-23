# 🎓 Système de Gestion de Produits - Logging & Observability

**Projet académique - Master GL (Génie Logiciel)**  
**Auteur**: Jihen Mlayeh  
**Année**: 2025

---

## 📋 Table des Matières

1. [Vue d'ensemble](#vue-densemble)
2. [Prérequis](#prérequis)
3. [Installation Backend](#installation-backend)
4. [Installation Frontend](#installation-frontend)
5. [Installation Docker (Jaeger)](#installation-docker-jaeger)
6. [Lancement de l'Application](#lancement-de-lapplication)
7. [Accès aux Services](#accès-aux-services)
8. [Dépendances Principales](#dépendances-principales)
9. [Troubleshooting](#troubleshooting)

---

## 🎯 Vue d'ensemble

Ce projet implémente un système complet de gestion de produits avec :
- **Backend** : Spring Boot avec MongoDB, OpenTelemetry, Spoon
- **Frontend** : Angular 18 avec Material Design, OpenTelemetry
- **Observabilité** : Jaeger pour le tracing distribué
- **Logging** : Système de profiling automatique des utilisateurs

### Fonctionnalités principales
- ✅ CRUD complet sur les produits
- ✅ Authentification utilisateur (JWT)
- ✅ Logging structuré avec Spoon
- ✅ Profiling utilisateur automatique (Read Heavy, Write Heavy, Expensive Seeker)
- ✅ Parsing de logs et extraction de profils
- ✅ OpenTelemetry frontend et backend
- ✅ Tracing distribué end-to-end avec Jaeger
- ✅ Interface Angular Material moderne et responsive

---

## ⚙️ Prérequis

Avant de commencer, assurez-vous d'avoir installé :

### Logiciels requis
- **Java JDK 17** ou supérieur
- **Node.js 18+** et **npm 9+**
- **MongoDB 6.0+** (local ou cloud)
- **Maven 3.8+**
- **Docker Desktop** (pour Jaeger)
- **Git**

### Vérification des versions
```bash
# Java
java -version  # Devrait afficher 17 ou plus

# Node.js et npm
node -v        # Devrait afficher 18+
npm -v         # Devrait afficher 9+

# Maven
mvn -version   # Devrait afficher 3.8+

# Docker
docker --version
docker compose version

# MongoDB
mongod --version  # Devrait afficher 6.0+
```

---

## 🔧 Installation Backend

### 1. Cloner le repository
```bash
git clone https://github.com/Jihen-Mlayeh/tpLoggerObservability.git
cd tpLoggerObservability
```

### 2. Configurer MongoDB

**Option A : MongoDB local**
```bash
# Démarrer MongoDB
mongod --dbpath /chemin/vers/data

# Ou via service (Linux/Mac)
sudo systemctl start mongod
```

**Option B : MongoDB Atlas (Cloud)**
- Créer un compte sur [MongoDB Atlas](https://www.mongodb.com/cloud/atlas)
- Créer un cluster gratuit
- Obtenir la connection string
- Modifier `src/main/resources/application.properties` :
```properties
spring.data.mongodb.uri=mongodb+srv://username:password@cluster.mongodb.net/products_db
```

### 3. Configuration de l'application

Vérifier/modifier le fichier `application.properties` :
```properties
# Port du serveur
server.port=8080

# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/products_db

# Logs
logging.file.name=logs/product-management.log
```

### 4. Installer les dépendances Maven

```bash
mvn clean install
```

### Dépendances Backend principales

Les dépendances suivantes seront installées automatiquement :

#### Spring Boot & Data
- `spring-boot-starter-web` : Framework web et REST
- `spring-boot-starter-data-mongodb` : Intégration MongoDB
- `spring-boot-starter-validation` : Validation des données

#### Logging & Observability
- **Spoon** : `spoon-core` (instrumentation code)
- **SLF4J + Logback** : Logging structuré (inclus dans Spring Boot)
- **OpenTelemetry** : 
  - `opentelemetry-api`
  - `opentelemetry-sdk`
  - `opentelemetry-exporter-otlp`
  - `opentelemetry-semconv`

#### Utilitaires
- `lombok` : Réduction du boilerplate
- `jackson-databind` : Sérialisation JSON

### 5. Lancer le backend

```bash
# Méthode 1 : Via Maven
mvn spring-boot:run

# Méthode 2 : Via JAR (après build)
mvn clean package
java -jar target/logging-and-observability-0.0.1-SNAPSHOT.jar
```

Le backend démarre sur **http://localhost:8080**

### 6. Vérifier le démarrage

```bash
# Test de l'API
curl http://localhost:8080/api/products

# Devrait retourner un tableau JSON (vide ou avec produits)
```

---

## 🎨 Installation Frontend

### 1. Cloner le repository
```bash
git clone https://github.com/Jihen-Mlayeh/ProductManagerFrontEnd.git
cd ProductManagerFrontEnd
```

### 2. Installer les dépendances npm

```bash
npm install
```

### Dépendances Frontend principales

Les dépendances suivantes seront installées automatiquement :

#### Angular Framework
- `@angular/core`, `@angular/common`, `@angular/router` : Framework Angular 18
- `@angular/platform-browser` : Support navigateur
- `@angular/forms` : Formulaires réactifs

#### Angular Material
- `@angular/material` : Composants Material Design
- `@angular/cdk` : Component Dev Kit
- `@angular/animations` : Animations Material

#### HTTP & Routing
- `@angular/common/http` : Client HTTP (HttpClient)
- Interceptors custom pour authentification et telemetry

#### Notifications
- `ngx-toastr` : Toast notifications
- `sweetalert2` : Dialogues de confirmation

#### OpenTelemetry (Tracing)
- `@opentelemetry/api` : API OpenTelemetry
- `@opentelemetry/sdk-trace-web` : SDK pour navigateur
- `@opentelemetry/instrumentation` : Framework instrumentation
- `@opentelemetry/instrumentation-document-load` : Tracing chargement page
- `@opentelemetry/instrumentation-user-interaction` : Tracing interactions
- `@opentelemetry/instrumentation-xml-http-request` : Tracing XHR
- `@opentelemetry/instrumentation-fetch` : Tracing Fetch API
- `@opentelemetry/context-zone` : Intégration Zone.js (Angular)
- `@opentelemetry/exporter-trace-otlp-http` : Export traces vers Jaeger
- `@opentelemetry/resources` : Métadonnées ressources
- `@opentelemetry/semantic-conventions` : Conventions sémantiques
- `@opentelemetry/sdk-trace-base` : Base SDK tracing

#### Build & Development
- `typescript` : Langage TypeScript
- `@angular/cli` : Angular CLI
- `rxjs` : Programmation réactive

### 3. Configuration du frontend

Vérifier le fichier `src/environments/environment.ts` :
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'  // URL du backend
};
```

### 4. Lancer le frontend

```bash
# Mode développement
ng serve

# Ou avec port spécifique
ng serve --port 4200
```

Le frontend démarre sur **http://localhost:4200**

### 5. Build pour production (optionnel)

```bash
ng build --configuration production

# Les fichiers sont générés dans dist/
```

---

## 🐳 Installation Docker (Jaeger)

### 1. Créer le fichier docker-compose.yml

À la racine du projet frontend (ou backend), créer `docker-compose.yml` :

```yaml
version: '3.8'

services:
  jaeger:
    image: jaegertracing/all-in-one:latest
    ports:
      - "16686:16686"  # Jaeger UI
      - "4317:4317"    # OTLP gRPC (backend)
      - "4318:4318"    # OTLP HTTP (frontend)
      - "14250:14250"  # Jaeger gRPC
    environment:
      - COLLECTOR_OTLP_ENABLED=true
      - LOG_LEVEL=debug
```

### 2. Démarrer Jaeger

```bash
# Démarrer en arrière-plan
docker compose up -d

# Vérifier que le conteneur tourne
docker ps

# Devrait afficher :
# jaegertracing/all-in-one:latest   Up   0.0.0.0:16686->16686/tcp, ...
```

### 3. Accéder à Jaeger UI

Ouvrir dans le navigateur : **http://localhost:16686**

### 4. Arrêter Jaeger (quand nécessaire)

```bash
docker compose down
```

---

## 🚀 Lancement de l'Application

### Ordre de démarrage recommandé

1. **MongoDB** (doit être démarré en premier)
```bash
# Via service
sudo systemctl start mongod

# Ou manuellement
mongod --dbpath /chemin/vers/data
```

2. **Jaeger** (Docker)
```bash
cd /chemin/vers/docker-compose.yml
docker compose up -d
```

3. **Backend** (Spring Boot)
```bash
cd /chemin/vers/tpLoggerObservability
mvn spring-boot:run
```

Attendre le message : `Started LoggingAndObservabilityApplication in X seconds`

4. **Frontend** (Angular)
```bash
cd /chemin/vers/ProductManagerFrontEnd
ng serve
```

Attendre le message : `Angular Live Development Server is listening on localhost:4200`

### Vérification du démarrage complet

✅ **Backend** : http://localhost:8080/api/products  
✅ **Frontend** : http://localhost:4200  
✅ **Jaeger** : http://localhost:16686  
✅ **MongoDB** : `mongo` (dans terminal, vérifier connexion)

---

## 🌐 Accès aux Services

| Service | URL | Description |
|---------|-----|-------------|
| **Frontend** | http://localhost:4200 | Interface Angular |
| **Backend API** | http://localhost:8080/api | API REST Spring Boot |
| **Jaeger UI** | http://localhost:16686 | Interface de tracing |
| **MongoDB** | mongodb://localhost:27017 | Base de données |
| **Logs Backend** | `logs/application.log` | Fichier de logs |
| **Profils Extraits** | `extracted-profiles/` | Profils JSON extraits |
| **Profils Générés** | `user-profiles/` | Profils scénarios |

---

## 📦 Dépendances Principales

### Backend (Maven)

```xml
<!-- Framework -->
spring-boot-starter-web
spring-boot-starter-data-mongodb
spring-boot-starter-validation

<!-- Logging & Instrumentation -->
spoon-core (10.4.2)
slf4j-api + logback-classic (inclus Spring Boot)

<!-- OpenTelemetry -->
opentelemetry-api (1.32.0)
opentelemetry-sdk (1.32.0)
opentelemetry-exporter-otlp (1.32.0)
opentelemetry-semconv (1.21.0-alpha)

<!-- Utilitaires -->
lombok (1.18.30)
jackson-databind
```

### Frontend (npm)

```json
{
  "dependencies": {
    "@angular/core": "^18.x",
    "@angular/material": "^18.x",
    "@angular/router": "^18.x",
    "ngx-toastr": "^latest",
    "sweetalert2": "^latest",
    "@opentelemetry/api": "^latest",
    "@opentelemetry/sdk-trace-web": "^latest",
    "@opentelemetry/instrumentation-*": "^latest",
    "@opentelemetry/exporter-trace-otlp-http": "^latest"
  }
}
```

**Note** : Les versions exactes sont spécifiées dans `package.json` et `pom.xml`

---

## 🔧 Troubleshooting

### Problème : Backend ne démarre pas

**Erreur** : `Cannot connect to MongoDB`
```bash
# Solution 1 : Vérifier que MongoDB tourne
sudo systemctl status mongod

# Solution 2 : Démarrer MongoDB
sudo systemctl start mongod

# Solution 3 : Vérifier le port
netstat -an | grep 27017
```

**Erreur** : `Port 8080 already in use`
```bash
# Solution 1 : Changer le port dans application.properties
server.port=8081

# Solution 2 : Tuer le processus occupant le port
lsof -ti:8080 | xargs kill -9
```

### Problème : Frontend ne démarre pas

**Erreur** : `npm install fails`
```bash
# Solution : Supprimer node_modules et réinstaller
rm -rf node_modules package-lock.json
npm install
```

**Erreur** : `ng: command not found`
```bash
# Solution : Installer Angular CLI globalement
npm install -g @angular/cli
```

### Problème : Jaeger ne capture pas les traces

**Diagnostic** :
```bash
# Vérifier que Jaeger tourne
docker ps | grep jaeger

# Vérifier les logs Jaeger
docker logs <container-id>

# Vérifier les ports
docker ps  # Doit montrer 0.0.0.0:4317->4317/tcp et 4318
```

**Solution** :
```bash
# Redémarrer Jaeger
docker compose down
docker compose up -d
```

### Problème : CORS Errors

**Erreur** : `Access-Control-Allow-Origin`

**Solution** : Vérifier `CorsConfig.java` :
```java
config.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
```

### Problème : Traces Frontend pas visibles

**Solution** :
1. Ouvrir DevTools (F12) → Console
2. Vérifier : `✅ OpenTelemetry initialized`
3. Network tab → Vérifier header `traceparent` dans les requêtes
4. Attendre 10-30 secondes que Jaeger reçoive les traces

---

## 📚 Utilisation de l'Application

### 1. Première connexion

1. Ouvrir http://localhost:4200
2. Cliquer sur "Sign up" pour créer un compte
3. Remplir : Name, Email, Password
4. Se connecter avec les credentials créés

### 2. Gestion des produits

- **Voir la liste** : Automatique après login
- **Ajouter un produit** : Bouton "ADD PRODUCT"
- **Modifier** : Clic sur "EDIT" sur une card
- **Supprimer** : Clic sur "DELETE" → Confirmer
- **Détails** : Clic sur "VIEW"

### 3. Consulter les traces

1. Ouvrir http://localhost:16686
2. Service dropdown : Choisir `unknown_service` (frontend) ou `product-management-backend`
3. Cliquer "Find Traces"
4. Cliquer sur une trace pour voir les détails

### 4. Consulter les logs

```bash
# Logs backend
tail -f logs/application.log

# Profils extraits
ls -la extracted-profiles/

# Profils générés par scénarios
ls -la user-profiles/
```

---

## 🎓 Contexte Académique

**Projet** : Logging & Observability  
**Objectif** : Implémentation d'un système de tracing distribué et de profiling utilisateur  
**Formation** : Master Génie Logiciel  
**Étudiant** : Jihen Mlayeh  
**Année** : 2025

### Exercices couverts

- ✅ **Backend** : Questions 1-5 (CRUD, Auth, Logging Spoon, Scénarios, Parsing)
- ✅ **Frontend** : Exercise 2 Questions 1-4 (Interface Angular, OpenTelemetry Frontend, Scénarios, OpenTelemetry Backend + End-to-End)

---

## 📞 Support

Pour toute question ou problème :

- **GitHub Backend** : https://github.com/Jihen-Mlayeh/tpLoggerObservability
- **GitHub Frontend** : https://github.com/Jihen-Mlayeh/ProductManagerFrontEnd
- **Email** : [Ajouter email si souhaité]

---

## 📄 Licence

Ce projet est réalisé dans un cadre académique. Tous droits réservés © 2025 Jihen Mlayeh

---

**Dernière mise à jour** : Décembre 2025  
**Version** : 1.0.0
