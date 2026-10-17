# Literature-Land

To run the program, navigate to the service directory and type:
`./mvnw clean test` and
`./mvnw spring-boot:run`,
then use a second terminal to send requests as provided below.

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