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


## Autor

Esteban Barrantes