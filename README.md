# Awesome Pizza

Sistema backend per gestire gli ordini di una pizzeria. I clienti possono ordinare senza registrarsi e ricevono un codice per tracciare lo stato dell'ordine. I pizzaioli hanno un'area autenticata per gestire gli ordini.

## Stack

- Java 21
- Spring Boot 4.0.2
- PostgreSQL
- Spring Security (HTTP Basic)
- Docker / Docker Compose

## Setup

### Avvio rapido (plug & play)

È possibile avviare **database e applicazione insieme** tramite Docker Compose:

```bash
docker compose up --build -d
```

Questo comando:

avvia PostgreSQL

builda l'applicazione Spring Boot

espone l'app su http://localhost:8080

Prerequisiti: Docker (Docker Desktop su Windows/macOS oppure Docker Engine su Linux)

## Setup Manuale

### Database

Avvia PostgreSQL con Docker:

```bash
docker run --name awesomepizza-postgres \
  -e POSTGRES_DB=awesomepizza_db \
  -e POSTGRES_USER=awesomepizza_user \
  -e POSTGRES_PASSWORD=awesomepizza_password \
  -p 5433:5432 \
  -d postgres:15
```

Oppure crea manualmente il database:

```sql
CREATE DATABASE awesomepizza_db;
CREATE USER awesomepizza_user WITH PASSWORD 'awesomepizza_password';
GRANT ALL PRIVILEGES ON DATABASE awesomepizza_db TO awesomepizza_user;
```

### Applicazione

```bash
mvn clean install
mvn spring-boot:run
```

L'app gira su `http://localhost:8080`

## Documentazione API

Swagger UI disponibile su: `http://localhost:8080/swagger-ui/index.html`

### Endpoint pubblici (clienti)

- `POST /api/v1/orders` - Crea ordine
- `GET /api/v1/orders/{code}` - Controlla stato ordine
- `PUT /api/v1/orders/{code}` - Modifica ordine (solo se PENDING)
- `POST /api/v1/orders/{code}/cancel` - Annulla ordine (solo se PENDING)

### Endpoint autenticati (pizzaioli)

Username: `pizzaiolo` / Password: `password`

- `GET /api/v1/pizzaiolo/orders` - Lista tutti gli ordini
- `GET /api/v1/pizzaiolo/orders/pending` - Lista ordini in attesa
- `POST /api/v1/pizzaiolo/orders/{code}/take` - Prendi in carico ordine
- `POST /api/v1/pizzaiolo/orders/takeNext` - Prendi prossimo ordine in coda
- `POST /api/v1/pizzaiolo/orders/{code}/status/{newStatus}` - Aggiorna stato

## Stati ordine

- **PENDING** → ordine appena creato
- **IN_PREPARATION** → pizzaiolo sta preparando
- **READY** → ordine pronto per la consegna
- **COMPLETED** → ordine consegnato
- **CANCELED** → ordine annullato

## Test

```bash
mvn test
```

## Note

- Gli ordini in PENDING possono essere modificati/annullati dal cliente
- Solo un ordine alla volta può essere IN_PREPARATION
- Optimistic locking per gestire modifiche concorrenti