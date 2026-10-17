# Cart service

Requirement specification

## Description:
The system shall allow customers to add books to their shopping cart, remove books and modify the quantity of selected books. 

## User Story:
As a customer, I want to manage books in my shopping cart so that I can control which books and quantities I intend to purchase. 
As a customer, I want to view and sort through each book in my cart, so i can view everything at-a-glance before i purchase the items.

## Rationale:
A shopping cart allows customers to temporarily store and review selected books before completing an order. 

## Acceptance Criteria:
Customers can successfully add and remove books from their cart and modify quantities, with the cart reflecting the changes.

### In association with the previous services, i would also like to implement the following:

# Wishlist service

## Description:
The system shall allow customers to add books to their wishlist, view their saved books and remove books from their wishlist. 

## User Story:
As a customer, I want to save books to my wishlist so that I can easily find books that I may want to purchase later. 

## Rationale:
A wishlist allows customers to store and review selected books to further evaluate before purchasing, or to let others know. 

## Acceptance Criteria:

A customer can add a book to their wishlist, view the saved book and remove it when it is no longer required or has been purchased.

The wishlist should follow a similar template to the cart, where items can be added and removed from it. the cart itself will contain a button to purchase/checkout the items, where a dummy transactional screen (that doesnt actually ask for payment) allows the user to add it to their account.