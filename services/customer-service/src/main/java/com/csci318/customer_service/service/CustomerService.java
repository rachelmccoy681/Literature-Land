package com.csci318.customer_service.service;

import com.csci318.customer_service.domain.Customer;
import com.csci318.customer_service.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    // connects to the customer repository
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // gets all customers from the database
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    // saves a new customer
    public Customer addCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    // gets a customer using their id
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id).orElse(null);
    }

    // updates a customer's details
    public Customer updateCustomer(Long id, Customer updatedCustomer) {

        // finds the customer using their id
        Customer customer = customerRepository.findById(id).orElse(null);

        // checks if the customer exists
        if (customer == null) {
            return null;
        }

        // updates the customer information
        customer.setName(updatedCustomer.getName());
        customer.setEmail(updatedCustomer.getEmail());
        customer.setPhone(updatedCustomer.getPhone());

        // saves the updated customer
        return customerRepository.save(customer);
    }
    // deletes a customer using their id
public void deleteCustomer(Long id) {
    customerRepository.deleteById(id);
}
}