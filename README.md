# Literature-Land

## Cart service API

The cart service runs on port `8082` and exposes these endpoints:

| Method | Endpoint | Behavior |
| --- | --- | --- |
| `POST` | `/api/carts/{customerId}/items` | Add `{ "bookId": 1, "quantity": 2 }`. Repeated adds increase the existing quantity. |
| `GET` | `/api/carts/{customerId}` | Return the cart and total quantity. Supports `sortBy=bookId\|quantity` and `direction=asc\|desc`. |
| `PUT` | `/api/carts/{customerId}/items/{bookId}` | Set quantity with `{ "quantity": 3 }`. |
| `DELETE` | `/api/carts/{customerId}/items/{bookId}` | Remove a book from the cart. |

Book and customer IDs and item quantities must be positive integers. A cart is created the first time a book is added; requesting a cart or item that does not exist returns `404`.