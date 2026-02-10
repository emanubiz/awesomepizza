# API Examples

Esempi pratici per testare le API via curl.

## Setup variabili

```bash
export BASE_URL="http://localhost:8080"
export AUTH="pizzaiolo:password"
```

## Cliente

### Crea un ordine

```bash
curl -X POST $BASE_URL/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Mario Rossi",
    "phone": "+393331234567",
    "deliveryAddress": "Via Roma 1, Milano",
    "orderItems": [
      {"pizzaName": "Margherita", "quantity": 2, "price": 8.50},
      {"pizzaName": "Diavola", "quantity": 1, "price": 9.00}
    ]
  }'
```

Salva l'`orderCode` dalla risposta.

### Controlla stato ordine

```bash
curl $BASE_URL/api/v1/orders/ORD-A1B2C3D4
```

### Modifica ordine (solo se PENDING)

```bash
curl -X PUT $BASE_URL/api/v1/orders/ORD-A1B2C3D4 \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Mario Rossi",
    "phone": "+393337654321",
    "deliveryAddress": "Via Nuova 10, Milano"
  }'
```

### Annulla ordine (solo se PENDING)

```bash
curl -X POST $BASE_URL/api/v1/orders/ORD-A1B2C3D4/cancel
```

## Pizzaiolo

Tutti questi endpoint richiedono autenticazione.

### Lista tutti gli ordini

```bash
curl -u $AUTH $BASE_URL/api/v1/pizzaiolo/orders
```

### Lista ordini in attesa

```bash
curl -u $AUTH $BASE_URL/api/v1/pizzaiolo/orders/pending
```

### Prendi in carico un ordine specifico

```bash
curl -X POST -u $AUTH $BASE_URL/api/v1/pizzaiolo/orders/ORD-A1B2C3D4/take
```

Lo stato passa da `PENDING` a `IN_PREPARATION`.

### Prendi prossimo ordine in coda

```bash
curl -X POST -u $AUTH $BASE_URL/api/v1/pizzaiolo/orders/take-next
```

Prende automaticamente l'ordine PENDING più vecchio.

### Segna ordine come pronto

```bash
curl -X POST -u $AUTH $BASE_URL/api/v1/pizzaiolo/orders/ORD-A1B2C3D4/status/READY
```

### Completa ordine (consegnato)

```bash
curl -X POST -u $AUTH $BASE_URL/api/v1/pizzaiolo/orders/ORD-A1B2C3D4/status/COMPLETED
```

## Workflow completo

```bash
# 1. Cliente crea ordine
ORDER=$(curl -s -X POST $BASE_URL/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Luigi Verdi",
    "phone": "+393339876543",
    "deliveryAddress": "Corso Venezia 15, Milano",
    "orderItems": [{"pizzaName": "4 Formaggi", "quantity": 1, "price": 10.00}]
  }' | jq -r '.orderCode')

echo "Ordine: $ORDER"

# 2. Cliente controlla stato
curl $BASE_URL/api/v1/orders/$ORDER | jq '.status'

# 3. Pizzaiolo prende prossimo ordine
curl -s -X POST -u $AUTH $BASE_URL/api/v1/pizzaiolo/orders/take-next | jq '.status'

# 4. Pizzaiolo segna come pronto
curl -s -X POST -u $AUTH $BASE_URL/api/v1/pizzaiolo/orders/$ORDER/status/READY | jq '.status'

# 5. Pizzaiolo completa ordine
curl -s -X POST -u $AUTH $BASE_URL/api/v1/pizzaiolo/orders/$ORDER/status/COMPLETED | jq '.status'
```

## Note

- Stati validi: `PENDING`, `IN_PREPARATION`, `READY`, `COMPLETED`, `CANCELED`
- Solo un ordine alla volta può essere `IN_PREPARATION`
- Gli ordini `PENDING` possono essere modificati o annullati dal cliente