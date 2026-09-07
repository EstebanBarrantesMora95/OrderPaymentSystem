# Order Payment System Prueba Practica

Sistema de gestión y procesamiento de pedidos desarrollado con una arquitectura basada en microservicios y comunicación asíncrona mediante Apache Kafka.

El sistema permite crear pedidos desde una aplicación web en React. Los pedidos son registrados inicialmente con estado `PENDING` y posteriormente procesados por un microservicio de pagos, que responde mediante eventos Kafka indicando si el pago fue aprobado o rechazado.

## Tecnologías

### Backend
- Java 21
- Spring Boot
- Spring Data JPA
- Spring Kafka
- Maven

### Frontend
- React
- Vite
- JavaScript

### Infraestructura
- Apache Kafka
- PostgreSQL
- Docker
- Docker Compose

## Arquitectura

El proyecto está compuesto por:

### OrderMS

Microservicio encargado de:

- Crear y consultar pedidos.
- Persistir los pedidos en PostgreSQL.
- Cifrar la información sensible de la tarjeta mediante RSA.
- Publicar eventos `order-placed` en Kafka.
- Consumir eventos `payment-processed`.
- Actualizar el estado del pedido a `PAID` o `PAYMENT_FAILED`.

### PaymentMS

Microservicio encargado de:

- Consumir eventos `order-placed`.
- Descifrar la información de pago mediante RSA.
- Procesar/simular la aprobación del pago.
- Publicar el resultado mediante el tópico `payment-processed`.

### Frontend

Aplicación React que permite:

- Crear pedidos.
- Introducir información de pago.
- Consultar el historial de pedidos.
- Visualizar automáticamente los cambios de estado del pedido.

El frontend realiza polling mientras existen pedidos `PENDING` para reflejar el resultado del procesamiento asíncrono sin necesidad de recargar la página.

## Flujo del sistema

```text
React
  |
 HTTP
  |  
OrderMS
  |
Guarda PENDING
  |
PostgreSQL
  |
order-placed
  |
Kafka
  |
PaymentMS
  |
payment-processed
  |
Kafka
  |
OrderMS
  |
Actualiza estado
  |
PAID / PAYMENT_FAILED
```

## Estructura

```text
OrderPaymentSystem/
── OrderMS/
── PaymentMS/
── frontend/
── docker-compose.yml
── .gitignore
── README.md
```

## Seguridad de datos de pago

Los datos sensibles de la tarjeta no son enviados como texto plano a través de Kafka.

`OrderMS` cifra la información utilizando una llave pública RSA antes de publicar el evento.

`PaymentMS` utiliza la llave privada correspondiente para descifrar la información.

La llave privada:

```text
PaymentMS/src/main/resources/keys/private_key.pem
```

está excluida del repositorio mediante `.gitignore` y **no se almacena en Git**.

## Generación de llaves RSA

El sistema utiliza RSA para cifrar los datos sensibles de la tarjeta antes de enviarlos por Kafka.

`OrderMS` utiliza la llave pública para cifrar la información y `PaymentMS` utiliza la llave privada correspondiente para descifrarla.

### 1. Crear las carpetas

Verificar que existan las siguientes rutas:

```text
OrderMS/src/main/resources/keys/
PaymentMS/src/main/resources/keys/
```

### 2. Generar la llave privada

Desde la raíz del proyecto, ejecutar:

```bash
openssl genpkey -algorithm RSA -out private_key.pem -pkeyopt rsa_keygen_bits:2048
```

Este comando genera una llave privada RSA de 2048 bits en formato PKCS#8.

### 3. Generar la llave pública

A partir de la llave privada:

```bash
openssl pkey -in private_key.pem -pubout -out public_key.pem
```

### 4. Colocar las llaves

Copiar la llave pública en:

```text
OrderMS/src/main/resources/keys/public_key.pem
```

Copiar la llave privada en:

```text
PaymentMS/src/main/resources/keys/private_key.pem
```

### **La llave privada está excluida del repositorio mediante `.gitignore` y no debe subirse a GitHub.**


### **Si se genera un nuevo par de llaves, debe reemplazarse tanto `public_key.pem` como `private_key.pem`, ya que ambas deben pertenecer al mismo par RSA.**

### Seguridad

Los datos de tarjeta son cifrados en `OrderMS` utilizando:

```text
RSA/ECB/OAEPWithSHA-256AndMGF1Padding
```

y son descifrados en `PaymentMS` utilizando la llave privada correspondiente.



## Ejecución con Docker

Desde la raíz del proyecto:

```bash
docker compose up -d --build
```

Esto levanta:

| Servicio | Puerto |
|---|---:|
| Frontend | 3000 |
| OrderMS | 8081 |
| PaymentMS | 8082 |
| PostgreSQL | 5433 |
| Kafka | 9092 |

Para comprobar los contenedores:

```bash
docker compose ps
```

Para detener el sistema:

```bash
docker compose down
```

## Acceso al frontend

Con los contenedores levantados:

```text
http://localhost:3000
```

## Estados de los pedidos

Los pedidos pueden encontrarse en los siguientes estados:

- `PENDING`: pedido creado y esperando el resultado del pago.
- `PAID`: pago aprobado.
- `PAYMENT_FAILED`: pago rechazado o procesamiento fallido.

## Prueba de pago aprobado

Para simular un pago aprobado puede utilizarse:

```text
4111111111111111
```

El flujo esperado es:

```text
PENDING -> PAID
```

## Prueba de pago rechazado

PaymentMS contiene una tarjeta configurada para simular un rechazo:

```text
4000000000000002
```

Al utilizarla, el flujo esperado es:

```text
PENDING -> PAYMENT_FAILED
```


**Dato importante**

Una vez realizada la prueba de pago, se implementó un mecanismo de polling que actualiza automáticamente el estado del pedido en la interfaz, permitiendo visualizar el cambio de PENDING a PAID o PAYMENT_FAILED sin necesidad de recargar la página manualmente ni presionar F5.

## Comunicación mediante Kafka

Se utilizan los tópicos:

```text
order-placed
payment-processed
```

`OrderMS` publica `order-placed` y consume `payment-processed`.

`PaymentMS` consume `order-placed` y publica `payment-processed`.

Esto permite desacoplar el procesamiento de pedidos del procesamiento de pagos mediante comunicación asíncrona.


## Script de Validación

Esta sección permite validar el flujo completo del sistema desde la creación del pedido hasta la actualización asíncrona del estado mediante Kafka.

### 1. Levantar el sistema

Desde la raíz del proyecto:

```bash
docker compose up -d --build
```

Verificar que todos los servicios estén activos:

```bash
docker compose ps
```

Deberían aparecer los siguientes contenedores:

```text
kafka
postgres-orders
order-ms
payment-ms
frontend
```

---

### 2. Crear un pedido

#### Opción A: Postman

Método:

```text
POST
```

URL:

```text
http://localhost:8081/api/orders
```

Body:

```json
{
  "customerName": "Esteban",
  "product": "Laptop Lenovo",
  "quantity": 1,
  "total": 750.50,
  "card": {
    "cardNumber": "4111111111111111",
    "cardHolder": "Test User",
    "expirationDate": "12/30",
    "cvv": "123"
  }
}
```

La respuesta inicial puede mostrar:

```json
{
  "id": 1,
  "customerName": "Esteban",
  "product": "Laptop Lenovo",
  "quantity": 1,
  "total": 750.50,
  "status": "PENDING"
}
```

El estado `PENDING` es esperado, ya que el procesamiento del pago se realiza de forma asíncrona mediante Kafka.

---

### 3. Consultar el estado inicial

Utilizar el `id` devuelto en la creación del pedido.

Ejemplo:

```text
GET http://localhost:8081/api/orders/1
```

La respuesta inicial puede mostrar:

```json
{
  "id": 1,
  "status": "PENDING"
}
```

---

### 4. Verificar el flujo de Kafka en PaymentMS

Ejecutar:

```bash
docker compose logs payment-ms
```

Se debería observar un mensaje indicando que `PaymentMS` recibió el evento del tópico:

```text
order-placed
```

También debería aparecer que los datos de la tarjeta fueron descifrados correctamente y que se generó el resultado del pago.

---

### 5. Verificar el resultado enviado por PaymentMS

En los logs de `PaymentMS` debería aparecer un mensaje similar a:

```text
Payment event sent to Kafka
PaymentProcessedEvent{orderId=1, approved=true, message='Payment approved'}
```

Esto confirma que `PaymentMS` publicó el evento en el tópico:

```text
payment-processed
```

---

### 6. Verificar la actualización en OrderMS

Ejecutar:

```bash
docker compose logs order-ms
```

Se debería observar un mensaje similar a:

```text
Payment result received for order: 1
Order 1 updated to: PAID
```

Esto confirma que `OrderMS` consumió el evento `payment-processed` y actualizó el estado del pedido en PostgreSQL.

---

### 7. Consultar el estado final

Ejecutar nuevamente:

```text
GET http://localhost:8081/api/orders/1
```

Para un pago aprobado, la respuesta final debería mostrar:

```json
{
  "id": 1,
  "status": "PAID"
}
```

---

## Prueba de Pago Rechazado

Para probar el flujo de error, utilizar la siguiente tarjeta:

```text
4000000000000002
```

Ejemplo:

```json
{
  "customerName": "Esteban",
  "product": "Monitor Gaming",
  "quantity": 1,
  "total": 350.00,
  "card": {
    "cardNumber": "4000000000000002",
    "cardHolder": "Test User",
    "expirationDate": "12/30",
    "cvv": "123"
  }
}
```

El flujo esperado es:

```text
PENDING
   ↓
PAYMENT_FAILED
```

Luego consultar:

```text
GET http://localhost:8081/api/orders/{id}
```

y verificar:

```json
{
  "status": "PAYMENT_FAILED"
}
```

---

## Prueba mediante cURL

### Crear pedido aprobado

```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Esteban",
    "product": "Laptop Lenovo",
    "quantity": 1,
    "total": 750.50,
    "card": {
      "cardNumber": "4111111111111111",
      "cardHolder": "Test User",
      "expirationDate": "12/30",
      "cvv": "123"
    }
  }'
```

### Consultar pedido

```bash
curl http://localhost:8081/api/orders/1
```

### Ver logs de PaymentMS

```bash
docker compose logs payment-ms
```

### Ver logs de OrderMS

```bash
docker compose logs order-ms
```

---

## Frontend

La aplicación web está disponible en:

```text
http://localhost:3000
```

Desde el frontend es posible:

- Crear pedidos.
- Consultar el historial completo.
- Buscar pedidos por ID, cliente o producto.
- Filtrar por estado.
- Ordenar los resultados.
- Navegar mediante paginación.
- Consultar el detalle de cada orden.
- Visualizar automáticamente el cambio de estado de `PENDING` a `PAID` o `PAYMENT_FAILED`.

### Dato importante

Una vez realizada la prueba de pago, se implementó un mecanismo de polling que actualiza automáticamente el estado del pedido en la interfaz, permitiendo visualizar el cambio de `PENDING` a `PAID` o `PAYMENT_FAILED` sin necesidad de recargar la página manualmente ni presionar `F5`.


## Autor

Esteban Barrantes