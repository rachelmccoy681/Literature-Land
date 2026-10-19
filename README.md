# Literature-Land

To run the program, navigate to the service directory and type:
`./mvnw clean test` and
`./mvnw spring-boot:run`,
then use a second terminal to send requests as provided below.

## Running Kafka locally

The stock service publishes stock updates and book search/view activity to
Kafka, and the order service consumes them. Both expect a broker at
`localhost:9092`. Start one with Docker from the repository root:

```sh
docker compose up -d
```

Then start both services in separate terminals:

```sh
cd services/stock-service && ./mvnw spring-boot:run
cd services/order-service && ./mvnw spring-boot:run
```

The stock service runs on port `8081` and the order service on `8083`. To
check that messages are flowing, reduce stock for a book and read the copy the
order service received through Kafka:

```sh
curl -i -X POST 'http://localhost:8081/books/1/reduce-stock?quantity=2'

curl -i http://localhost:8083/stock-availability/1
```

View a book, then check the trending list:

```sh
curl -i http://localhost:8081/books/2

curl -i http://localhost:8083/trending-books
```

The order service terminal also prints `stock updated:` and `book activity:`
lines as it receives events. Stop the broker with `docker compose down`.

`./mvnw test` does not need Docker: the Kafka integration tests
(`StockKafkaIntegrationTests` and `OrderKafkaIntegrationTests`) start an
embedded broker. Without a running broker, the other tests still pass but log
connection retries for `localhost:9092`.

## Cart and wishlist service API

The service runs on port `8082`. Send requests to the API paths below
using `curl`, Postman, or another HTTP client. The examples use customer ID
`25`; replace it with the ID you want to use.

### Cart endpoints

| Method | Endpoint | Behavior |
| --- | --- | --- |
| `POST` | `/api/carts/{customerId}/items` | Add `{ "bookId": 1, "quantity": 2 }`. Repeated adds increase the existing quantity. |
| `GET` | `/api/carts/{customerId}` | Return the cart and total quantity. Supports `sortBy=bookId\|quantity` and `direction=asc\|desc`. |
| `PUT` | `/api/carts/{customerId}/items/{bookId}` | Set quantity with `{ "quantity": 3 }`. |
| `DELETE` | `/api/carts/{customerId}/items/{bookId}` | Remove a book from the cart. |

### Wishlist endpoints

| Method | Endpoint | Behavior |
| --- | --- | --- |
| `POST` | `/api/wishlists/{customerId}/items` | Add `{ "bookId": 1 }`. Adding a book already saved does not create a duplicate. |
| `GET` | `/api/wishlists/{customerId}` | Return the customer's saved books. |
| `DELETE` | `/api/wishlists/{customerId}/items/{bookId}` | Remove a saved book. |

### examples

Run these commands in a terminal while the service is running. Start the
service in one terminal from `services/cart-service`:

```sh
./mvnw spring-boot:run
```

Use a second terminal to add a book to the cart:

```sh
curl -i -X POST http://localhost:8082/api/carts/25/items \
  -H 'Content-Type: application/json' \
  -d '{"bookId":1,"quantity":2}'
```

View the cart (you can also open this `GET` URL in a browser):

```sh
curl -i 'http://localhost:8082/api/carts/25?sortBy=bookId&direction=asc'
```

Change the quantity or remove the book:

```sh
curl -i -X PUT http://localhost:8082/api/carts/25/items/1 \
  -H 'Content-Type: application/json' \
  -d '{"quantity":3}'

curl -i -X DELETE http://localhost:8082/api/carts/25/items/1
```

Add, view, and remove a wishlist book:

```sh
curl -i -X POST http://localhost:8082/api/wishlists/25/items \
  -H 'Content-Type: application/json' \
  -d '{"bookId":1}'

curl -i http://localhost:8082/api/wishlists/25

curl -i -X DELETE http://localhost:8082/api/wishlists/25/items/1
```

Book and customer IDs and cart item quantities must be positive integers.
The first add creates a cart or wishlist. Getting a cart or wishlist that has
not been created, or removing a book that is not present, returns `404`.

## Checkout and order history

Checkout is a dummy transaction: it does not collect payment details or charge
money. It records the books and quantities from the customer's cart as an
order, then clears the cart. Wishlist contents are not changed.

Run all three services in separate terminals:

```sh
cd services/cart-service && ./mvnw spring-boot:run
cd services/order-service && ./mvnw spring-boot:run
cd services/payment-service && ./mvnw spring-boot:run
```

The services listen on ports `8082` (cart), `8083` (order history), and `8084`
(checkout). Add at least one item to the cart, then start checkout:

```sh
curl -i -X POST http://localhost:8084/api/payments/25/checkout
```

A successful checkout returns `201 Created` with the created order's ID,
timestamp, books, and quantities. To view recent orders:

```sh
curl -i http://localhost:8083/api/orders/25
```

An empty or missing cart returns `409 Conflict`; checkout does not create an
order in that case. If an order was recorded but the cart could not be cleared,
the checkout response reports its ID so the customer does not retry and create
a duplicate. Payment service URLs can be configured with `CART_SERVICE_URL`
and `ORDER_SERVICE_URL` environment variables.
