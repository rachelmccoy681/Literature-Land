package com.csci318.customer_service.controller;

import com.csci318.customer_service.domain.Customer;
import com.csci318.customer_service.service.CustomerService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    // connects to the customer service
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // gets all customers
    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    // adds a new customer
    @PostMapping
    public Customer addCustomer(@RequestBody Customer customer) {
        return customerService.addCustomer(customer);
    }

    // gets a customer by their id
    @GetMapping("/{id}")
    public Customer getCustomerById(@PathVariable Long id) {
        return customerService.getCustomerById(id);
    }

    // updates a customer's details
    @PutMapping("/{id}")
    public Customer updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
        return customerService.updateCustomer(id, customer);
    }
    // deletes a customer using their id
@DeleteMapping("/{id}")
public void deleteCustomer(@PathVariable Long id) {
    customerService.deleteCustomer(id);
}
}