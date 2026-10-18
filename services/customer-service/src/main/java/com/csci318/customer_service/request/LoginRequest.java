package com.csci318.customer_service.request;

public class LoginRequest {

    private String email;
    private String password;

    // empty constructor
    public LoginRequest() {
    }

    // getters
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    // setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}