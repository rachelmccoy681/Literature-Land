package com.csci318.customer_service.service;

import com.csci318.customer_service.domain.Customer;
import com.csci318.customer_service.repository.CustomerRepository;
import com.csci318.customer_service.request.RegisterRequest;
import com.csci318.customer_service.request.LoginRequest;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // connects to the customer repository
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // gets all customers from the database
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    // registers a new customer
    public Customer registerCustomer(RegisterRequest request) {

        // checks if the username is empty
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        // checks if the email is empty
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        // checks if the email format is valid
        if (!request.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Invalid email address");
        }

        // checks if the password is empty
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        // checks if the username is already taken
        if (customerRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        // checks if the email is already registered
        if (customerRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        // creates a new customer
        Customer customer = new Customer();

        customer.setUsername(request.getUsername());
        customer.setName(request.getUsername());
        customer.setEmail(request.getEmail());

        // hashes the password before saving
        customer.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // saves the customer
        return customerRepository.save(customer);
    }

    // checks customer login details
    public Customer loginCustomer(LoginRequest request) {

        // checks if the email is empty
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        // checks if the password is empty
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        // finds the customer using their email
        Customer customer = customerRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        // checks if the password matches
        if (customer.getPasswordHash() == null ||
                !passwordEncoder.matches(request.getPassword(), customer.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        // returns the customer if login details are correct
        return customer;
    }

    // saves a new customer
    public Customer addCustomer(Customer customer) {

        // checks if the name is empty
        if (customer.getName() == null || customer.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }

        // checks if the email is empty
        if (customer.getEmail() == null || customer.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        // checks if the email format is valid
        if (!customer.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Invalid email address");
        }

        // checks if the phone number is empty
        if (customer.getPhone() == null || customer.getPhone().isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        // checks if the email is already registered
        if (customerRepository.existsByEmailIgnoreCase(customer.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        // saves the customer
        return customerRepository.save(customer);
    }

    // gets a customer using their id
    public Customer getCustomerById(Long id) {

        // finds the customer or returns an error
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer not found"));
    }

    // updates a customer's details
    public Customer updateCustomer(Long id, Customer updatedCustomer) {

        // finds the customer using their id
        Customer customer = customerRepository.findById(id).orElse(null);

        // checks if the customer exists
        if (customer == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Customer not found");
        }

        // checks if the name is empty
        if (updatedCustomer.getName() == null || updatedCustomer.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }

        // checks if the email is empty
        if (updatedCustomer.getEmail() == null || updatedCustomer.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        // checks if the email format is valid
        if (!updatedCustomer.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Invalid email address");
        }

        // checks if the phone number is empty
        if (updatedCustomer.getPhone() == null || updatedCustomer.getPhone().isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        // checks if the new email belongs to another customer
        if (!customer.getEmail().equalsIgnoreCase(updatedCustomer.getEmail())
                && customerRepository.existsByEmailIgnoreCase(updatedCustomer.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
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

        // checks if the customer exists
        if (!customerRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Customer not found");
        }

        // deletes the customer
        customerRepository.deleteById(id);
    }
}