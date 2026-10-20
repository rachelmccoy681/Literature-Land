# Literature-Land

Literature Land is an online bookstore developed using Spring Boot microservices and an HTML, CSS and JavaScript frontend.

The application allows customers to browse books, search for titles, view book details, manage their shopping cart and wishlist, place orders and view their order history.

The backend is divided into separate services that communicate using REST APIs and Apache Kafka.

## Quick Start (Windows)

You need Java 21 or later, and Python or Node.js. Docker Desktop is optional (see Running Without Docker).

1. **Open Docker Desktop and leave it running in the background.** Wait until it says the engine is running. Docker Desktop only needs to be running so the script can start Kafka.

2. **Run `start.cmd`.** In File Explorer, open the project folder and double-click `start.cmd`, or run `.\start.cmd` from a terminal in that folder.

3. **Wait for the browser to open.** The script starts Kafka, opens one window per service (stock, cart and order), serves the frontend and opens `http://127.0.0.1:5500/home/home.html` once everything is ready. The first run may take a few minutes because dependencies need to be downloaded.

4. **To stop the project**, double-click `stop.cmd` or run `.\stop.cmd`. This closes the service windows and stops Kafka.

### Additional Startup Options

```sh
.\start.cmd -Services stock,cart,order,chatbot
.\start.cmd -SkipKafka
.\start.cmd -NoBrowser
.\stop.cmd -KeepKafka
```

### If a Service Crashes Due to Insufficient Memory

If a service stops immediately and creates an `hs_err_pid*.log` file reporting insufficient memory for the Java Runtime Environment, the computer may have run out of RAM.

The startup script already limits each Java process to 256 MB.

To reduce memory usage:

- Close unnecessary applications.
- Skip Docker using `.\start.cmd -SkipKafka`.
- Close unused Java projects in VS Code.
- Temporarily disable the Java language support extension if necessary.

Crash reports such as `hs_err_pid*.log` and `replay_pid*.log` can be deleted afterwards.

### Running Without Docker

If Docker Desktop is not installed or running, the startup script displays a warning and continues without Kafka.

The bookstore can still run, but event-driven features such as trending book updates and stock synchronisation will not update through Kafka.

The services run with `KAFKA_ENABLED=false`.

## Quick Start (macOS and Linux)

Start Docker if available, then run:

```sh
./start.sh
```

The script starts the required services in the background and writes logs to `.run/logs/`.

Additional commands:

```sh
./start.sh --services stock,cart,order,chatbot
./start.sh --skip-kafka --no-browser
./stop.sh
```

If permission is denied, run:

```sh
chmod +x start.sh stop.sh
```

Alternatively:

```sh
bash start.sh
```

If VS Code Live Server is already running on port `5500`, the startup script uses it instead of starting another server.

## Project Structure

The project contains the following services:

- `base-domains` - Shared domain classes used by backend services.
- `customer-service` - Manages customer information.
- `stock-service` - Manages books, stock availability and book activity.
- `cart-service` - Manages shopping carts and wishlists.
- `order-service` - Handles customer orders and order history.
- `payment-service` - Handles the backend checkout process.
- `chatbot-service` - Contains AI chatbot functionality.

The frontend is located in the `frontend` folder.

## Technologies Used

The project uses the following technologies:

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- H2 Database
- Spring Cloud Stream
- Apache Kafka
- Docker
- LangChain4j
- HTML
- CSS
- JavaScript

## Running the Project Manually

Before running the project, make sure the following are installed:

- Java 21
- Docker Desktop (for Kafka)
- Visual Studio Code or another IDE

The project includes Maven Wrapper, so Maven does not need to be installed separately.

### Starting Kafka

Open a terminal in the main Literature-Land project folder.

Run:

```sh
docker compose up -d
```

Kafka runs on port `9092`.

Make sure Docker Desktop is running before executing this command.

### Starting Backend Services

Each backend service needs to run in a separate terminal.

From the main project folder, navigate to the required service directory.

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS or Linux:

```sh
./mvnw spring-boot:run
```

### Customer Service

```sh
cd services/customer-service
./mvnw spring-boot:run
```

The Customer Service runs on port `8080`.

### Stock Service

```sh
cd services/stock-service
./mvnw spring-boot:run
```

The Stock Service runs on port `8081`.

### Cart Service

```sh
cd services/cart-service
./mvnw spring-boot:run
```

The Cart Service runs on port `8082`.

### Order Service

```sh
cd services/order-service
./mvnw spring-boot:run
```

The Order Service runs on port `8083`.

### Payment Service

```sh
cd services/payment-service
./mvnw spring-boot:run
```

The Payment Service runs on port `8084`.

### Chatbot Service

```sh
cd services/chatbot-service
./mvnw spring-boot:run
```

The Chatbot Service is configured to run on port `8085`.

The service requires an Anthropic API key to connect to the language model.

### Service Ports

| Service | Port |
|---|---|
| Customer Service | 8080 |
| Stock Service | 8081 |
| Cart Service | 8082 |
| Order Service | 8083 |
| Payment Service | 8084 |
| Chatbot Service | 8085 |
| Apache Kafka | 9092 |

Keep the required services running while using the frontend.

## Customer Service

The Customer Service manages customer information.

It provides backend functionality for storing and retrieving customer details.

Customer information is managed separately from the other services.

The service uses Spring Boot and Spring Data JPA for handling customer data.

## Stock Service

The Stock Service manages the books available in Literature Land.

It stores information such as book titles, authors, genres, prices and stock availability.

The service also provides book information to the frontend.

### Stock Service API

| Method | Endpoint | Function |
|---|---|---|
| GET | `/books` | Retrieve all books |
| GET | `/books/{bookId}` | Retrieve details of a specific book |
| POST | `/books/{bookId}/reduce-stock?quantity=2` | Reduce the available stock for a book |

### Examples

Retrieve all books:

```sh
curl -i http://localhost:8081/books
```

Retrieve information about a specific book:

```sh
curl -i http://localhost:8081/books/1
```

Reduce the stock of a book:

```sh
curl -i -X POST 'http://localhost:8081/books/1/reduce-stock?quantity=2'
```

The frontend uses the Stock Service to display books on the Browse Books and Book Details pages.

## Running Kafka Locally

The Stock Service publishes stock updates and book search/view activity to Kafka, and the Order Service consumes them.

Both expect a broker at `localhost:9092`.

Start Kafka using Docker from the repository root:

```sh
docker compose up -d
```

Then start both services in separate terminals.

Stock Service:

```sh
cd services/stock-service
./mvnw spring-boot:run
```

Order Service:

```sh
cd services/order-service
./mvnw spring-boot:run
```

The Stock Service runs on port `8081` and the Order Service runs on port `8083`.

### Testing Kafka Messages

To check that messages are flowing, reduce stock for a book:

```sh
curl -i -X POST 'http://localhost:8081/books/1/reduce-stock?quantity=2'
```

Then check the stock information received by the Order Service:

```sh
curl -i http://localhost:8083/stock-availability/1
```

View a book:

```sh
curl -i http://localhost:8081/books/2
```

Then check the trending books list:

```sh
curl -i http://localhost:8083/trending-books
```

The Order Service terminal also prints `stock updated:` and `book activity:` messages when it receives events.

To stop Kafka:

```sh
docker compose down
```

The Kafka integration tests (`StockKafkaIntegrationTests` and `OrderKafkaIntegrationTests`) use an embedded broker.

## Cart and Wishlist Service API

The Cart Service runs on port `8082`.

It manages shopping carts and wishlists.

The examples below use customer ID `25`. Replace it with the required customer ID.

### Cart Endpoints

| Method | Endpoint | Behaviour |
|---|---|---|
| POST | `/api/carts/{customerId}/items` | Add a book to the cart |
| GET | `/api/carts/{customerId}` | Retrieve the customer's cart |
| PUT | `/api/carts/{customerId}/items/{bookId}` | Update the quantity of a book |
| DELETE | `/api/carts/{customerId}/items/{bookId}` | Remove a book from the cart |

### Wishlist Endpoints

| Method | Endpoint | Behaviour |
|---|---|---|
| POST | `/api/wishlists/{customerId}/items` | Add a book to the wishlist |
| GET | `/api/wishlists/{customerId}` | Retrieve the customer's wishlist |
| DELETE | `/api/wishlists/{customerId}/items/{bookId}` | Remove a book from the wishlist |

### Cart Examples

Add a book to the cart:

```sh
curl -i -X POST http://localhost:8082/api/carts/25/items \
  -H 'Content-Type: application/json' \
  -d '{"bookId":1,"quantity":2}'
```

View the cart:

```sh
curl -i 'http://localhost:8082/api/carts/25?sortBy=bookId&direction=asc'
```

Change the quantity:

```sh
curl -i -X PUT http://localhost:8082/api/carts/25/items/1 \
  -H 'Content-Type: application/json' \
  -d '{"quantity":3}'
```

Remove a book:

```sh
curl -i -X DELETE http://localhost:8082/api/carts/25/items/1
```

### Wishlist Examples

Add a book to the wishlist:

```sh
curl -i -X POST http://localhost:8082/api/wishlists/25/items \
  -H 'Content-Type: application/json' \
  -d '{"bookId":1}'
```

View the wishlist:

```sh
curl -i http://localhost:8082/api/wishlists/25
```

Remove a book from the wishlist:

```sh
curl -i -X DELETE http://localhost:8082/api/wishlists/25/items/1
```

Book IDs, customer IDs and cart item quantities must be positive integers.

The first add creates a cart or wishlist.

Getting a cart or wishlist that has not been created, or removing a book that is not present, returns `404`.

## Order Service

The Order Service handles customer orders and order history.

It allows customers to place orders and retrieve information about previous purchases.

The service runs on port `8083`.

### Order Service API

| Method | Endpoint | Function |
|---|---|---|
| POST | `/orders?customerId={customerId}` | Place an order |
| GET | `/orders/customer/{customerId}` | Retrieve customer order history |
| GET | `/orders/{orderId}/items` | Retrieve books included in an order |

### Examples

View the order history for customer `1`:

```sh
curl -i http://localhost:8083/orders/customer/1
```

View the books included in order `1`:

```sh
curl -i http://localhost:8083/orders/1/items
```

Place an order:

```sh
curl -i -X POST 'http://localhost:8083/orders?customerId=1' \
  -H 'Content-Type: application/json' \
  -d '[{"bookId":1,"quantity":2}]'
```

The My Orders frontend page connects to the Order Service to display order history.

## Checkout and Order History

Checkout is a dummy transaction. It does not collect real payment details or charge money.

It records the books and quantities from the customer's cart as an order and then clears the cart.

Wishlist contents are not changed.

Run the following services in separate terminals:

```sh
cd services/cart-service
./mvnw spring-boot:run
```

```sh
cd services/order-service
./mvnw spring-boot:run
```

```sh
cd services/payment-service
./mvnw spring-boot:run
```

The services listen on ports `8082` (cart), `8083` (order history) and `8084` (checkout).

Add at least one item to the cart, then start checkout:

```sh
curl -i -X POST http://localhost:8084/api/payments/25/checkout
```

A successful checkout returns `201 Created` with the created order's ID, timestamp, books and quantities.

To view recent orders through the Order Service:

```sh
curl -i http://localhost:8083/orders/customer/25
```

An empty or missing cart returns `409 Conflict`.

If an order was recorded but the cart could not be cleared, the checkout response reports its ID so the customer does not retry and create a duplicate.

Payment Service URLs can be configured using the `CART_SERVICE_URL` and `ORDER_SERVICE_URL` environment variables.

## Chatbot Service

The chatbot service is designed to answer customer questions about books and using the store.

It uses LangChain4j with Anthropic's Claude Haiku 5.5 model.

It reads information from the Stock, Cart and Order Services.

The chatbot is designed to help customers search for books, view book details, check availability and retrieve store information.

It does not directly modify customer information or place orders.

### API Configuration

The chatbot requires an Anthropic API key in the `ANTHROPIC_API_KEY` environment variable.

Do not store the API key in `application.properties` or commit it to the repository.

Start the services the chatbot reads from, including Kafka for trending books.

Then start the chatbot from `services/chatbot-service`.

For macOS or Linux:

```sh
export ANTHROPIC_API_KEY=...
./mvnw spring-boot:run
```

For Windows PowerShell:

```powershell
$env:ANTHROPIC_API_KEY = "YOUR_API_KEY"
.\mvnw.cmd spring-boot:run
```

The chatbot service is configured to run on port `8085`.

### Chatbot API

Send a message as customer `25`:

```sh
curl -i -X POST http://localhost:8085/api/chat/25 \
  -H 'Content-Type: application/json' \
  -d '{"message":"Do you have any fantasy books under $25?"}'
```

The expected response format is:

```json
{
  "customerId": 25,
  "reply": "..."
}
```

The chatbot is designed to remember each customer's last 20 messages using its own H2 database.

To start a fresh conversation:

```sh
curl -i -X DELETE http://localhost:8085/api/chat/25
```

The model, effort level, memory size and service URLs are configured under `chatbot.*` in `application.properties`.

## Frontend

The frontend is built using HTML, CSS and JavaScript.

It provides the user interface for Literature Land and connects to the backend services using REST APIs.

The frontend is located in the `frontend` folder.

### Frontend Pages

| Page | Functionality |
|---|---|
| Home | Displays books and provides navigation around the website |
| Browse Books | Displays books with searching, filtering, sorting and pagination |
| Book Details | Displays information about individual books |
| Categories | Allows customers to explore books by category |
| Trending Books | Displays books based on recent search and viewing activity |
| Wishlist | Allows customers to save and remove books |
| Shopping Cart | Allows customers to manage selected books and quantities |
| Checkout | Provides shipping information, demo payment details and order review |
| My Orders | Displays customer order history |
| Chatbot | Provides the interface for the chatbot service |

### Running the Frontend

1. Make sure Docker Desktop is running if Kafka is required.
2. Start Kafka.
3. Start the required backend services.
4. Open the Literature-Land project in Visual Studio Code.
5. Open `frontend/index.html`.
6. Right-click the file and select **Open with Live Server**.
7. The website will open in your browser.

Keep the backend services running while using the website.

### Browse Books

The Browse Books page retrieves book information from the Stock Service on port `8081`.

Users can:

- Search for books by keyword.
- Filter books by genre, author, price and rating.
- Sort the displayed books.
- Browse books using pagination.
- Select a book to view its details.

### Book Details

The Book Details page retrieves information about the selected book from the Stock Service.

It displays the book's title, author, price, description and availability.

Users can select a quantity and add the book to their shopping cart.

### Cart and Wishlist

The Cart page allows users to add books, update quantities and remove items.

The Cart Service runs on port `8082`.

The frontend wishlist uses browser `localStorage` to save selected books across pages.

Users can add and remove books from their wishlist.

### Checkout

The Checkout page includes three steps:

1. Shipping Information
2. Payment Information
3. Review and Place Order

The payment information is used for demonstration purposes only.

No real payment is processed.

### My Orders

After successfully placing an order, customers can access the My Orders page.

The page retrieves order information from the Order Service.

It uses the following endpoints:

```http
GET /orders/customer/1
```

```http
GET /orders/{orderId}/items
```

The page displays order numbers, statuses, purchased books and quantities.

The frontend currently uses customer ID `1` for testing.

## Event-Driven Communication

Literature Land uses Apache Kafka for communication between backend services.

The Stock Service publishes events when stock information changes or when customers search for and view books.

The Order Service consumes these events.

This allows the services to exchange information without requiring direct REST requests for every update.

Spring Cloud Stream is used for messaging integration.

## Real-Time Features

The project includes functionality related to real-time book activity and stock information.

### Stock Updates

Stock changes are published through Kafka.

Other services can consume these events to receive updated stock information.

### Trending Books

Book search and viewing activity is recorded and published as events.

This activity is used to support the trending books functionality.

The trending books endpoint is:

```http
GET /trending-books
```

The endpoint is available through the Order Service on port `8083`.

## Data Storage

The backend services use Spring Data JPA and H2 for database operations.

Each microservice manages the data associated with its own functionality.

For example:

- Customer Service manages customer information.
- Stock Service manages book and stock information.
- Cart Service manages carts and wishlists.
- Order Service manages orders and order items.
- Chatbot Service includes configuration for storing conversation history.

The frontend also uses browser `localStorage` for wishlist functionality.

## Testing

Tests can be run using Maven Wrapper.

Navigate to the required service directory and run:

```sh
./mvnw clean test
```

On Windows:

```powershell
.\mvnw.cmd clean test
```

The project includes tests for backend functionality.

The Kafka integration tests use an embedded broker, so Docker is not required for those tests.

REST API endpoints can also be tested using curl or Postman.

## Stopping the Project

To stop a backend service, press:

```text
Ctrl + C
```

in the terminal where the service is running.

To stop Kafka, run the following command from the main project folder:

```sh
docker compose down
```

Alternatively, if you used the startup scripts, run:

```powershell
.\stop.cmd
```