# Literature-Land

## Quick start (Windows)

Start Docker Desktop, then from the repository root run (or double-click)
`start.cmd`. It starts Kafka, the stock, cart and order services (each in its
own window, so you can read its logs), serves the frontend on port `5500`, and
opens `http://127.0.0.1:5500/home/home.html` once everything is up. The first
run downloads Maven dependencies and can take a few minutes.

```sh
.\start.cmd                                   # everything the frontend needs
.\start.cmd -Services stock,cart,order,chatbot  # pick which services to start
.\start.cmd -SkipKafka -NoBrowser
.\stop.cmd                                    # close the service windows and stop Kafka
```

## Quick start (macOS and Linux)

`start.sh` does the same thing, but runs the services in the background and
writes their logs to `.run/logs/`:

```sh
./start.sh                                     # everything the frontend needs
./start.sh --services stock,cart,order,chatbot # pick which services to start
./start.sh --skip-kafka --no-browser
./stop.sh                                      # stop the services and Kafka
```

If you get `permission denied`, run `chmod +x start.sh stop.sh` once, or use
`bash start.sh`.

Docker is optional. Without it (or with `-SkipKafka`), the script starts the
services with `KAFKA_ENABLED=false`: the whole bookstore works, but the
order service's trending books and stock copy stop updating. To run the stock
service by hand without a broker, set `KAFKA_ENABLED=false` first.

Serving the frontend needs Python or Node.js. If VS Code Live Server is
already running on port `5500`, the script uses it instead.

## Running services manually

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

## Chatbot service

The chatbot answers customer questions about books and about using the store.
It uses LangChain4j with Anthropic's Claude Haiku 5.5 model. It reads from the
stock, cart, and order services, but it never changes anything: it can't add
to a cart, place an order, or edit an account. When a customer asks for one of
those, it explains how to do it.

It needs an Anthropic API key in the `ANTHROPIC_API_KEY` environment variable.
Don't put the key in `application.properties` or commit it. The service stops
at startup with a clear message if the key is missing.

Start the services the chatbot reads from (stock, cart, and order, plus Kafka
for trending books), then start the chatbot from `services/chatbot-service`:

```sh
export ANTHROPIC_API_KEY=...        # PowerShell: $env:ANTHROPIC_API_KEY = "..."
./mvnw spring-boot:run
```

It runs on port `8085`. Send a message as customer `25`:

```sh
curl -i -X POST http://localhost:8085/api/chat/25 \
  -H 'Content-Type: application/json' \
  -d '{"message":"Do you have any fantasy books under $25?"}'
```

The response is `{"customerId": 25, "reply": "..."}`. The chatbot remembers
each customer's last 20 messages, saved in its own H2 database, so follow-up
questions work. To start a fresh conversation:

```sh
curl -i -X DELETE http://localhost:8085/api/chat/25
```

It can search and filter books, show a book's details and stock, list trending
books, and show the customer's own cart, wishlist, and order history. Chatbot
searches count toward trending books, like any other search. An empty message,
a message over 2000 characters, or a non-positive customer ID returns
`400 Bad Request`. If Claude can't be reached, the response is
`503 Service Unavailable`.

The model, effort level, memory size, and service URLs are set under
`chatbot.*` in `application.properties`. `./mvnw test` doesn't need an API
key: the tests run against a local fake of the Anthropic API and the other
services.
