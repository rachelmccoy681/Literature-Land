# Payment service

Requirement specification

## Description:
The system shall allow customers to review their shopping cart and complete an order for the selected books. 

## User Story:
As a customer, I want to complete an order so that I can purchase the books I have selected. 

## Rationale:
Completing an order is a core function of the online bookstore and allows customers to finalise their selected purchases. 

## Acceptance Criteria:
A customer with valid items in their cart can successfully place an order and receive confirmation that the order has been created. 

-

implemented as a microservice, the payment module does not involve real payment, and only removes the item from a cart/wishlist (where it likely shouldnt be anyway - purchasing items should be available from the cart (in practice)), and is able to be located amongst a user's "recent purchases"/order history, which has its use case outlined below:

# Order history

## Description:
The system shall allow customers to view information about their previously completed orders. 

## User Story:
As a customer, I want to view my order history so that I can keep track of the books I have previously purchased. 

## Rationale:
Order history allows customers to review their previous purchases and provides a record of their activity within the bookstore. 

## Acceptance Criteria:
A customer can request their order history and view the available information for their previous orders.