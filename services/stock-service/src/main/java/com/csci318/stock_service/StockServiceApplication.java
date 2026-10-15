package com.csci318.stock_service;

import com.csci318.stock_service.domain.Book;
import com.csci318.stock_service.repository.BookRepository;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class StockServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockServiceApplication.class, args);
    }

    // adds sample books to the database for testing
    @Bean
    CommandLineRunner addSampleBooks(BookRepository bookRepository) {
        return args -> {

            // harry potter
            Book harryPotter = new Book(
                    "Harry Potter",
                    "J.K. Rowling",
                    "Fantasy",
                    25.00,
                    10
            );
            harryPotter.setRating(4.8);
            harryPotter.setDescription(
                    "A young wizard discovers his magical abilities and begins his journey at Hogwarts School of Witchcraft and Wizardry."
            );
            bookRepository.save(harryPotter);

            // the hobbit
            Book theHobbit = new Book(
                    "The Hobbit",
                    "J.R.R. Tolkien",
                    "Fantasy",
                    20.00,
                    5
            );
            theHobbit.setRating(4.7);
            theHobbit.setDescription(
                    "Bilbo Baggins joins a group of dwarves on an unexpected adventure to reclaim their homeland from a dragon."
            );
            bookRepository.save(theHobbit);

            // the shining
            Book theShining = new Book(
                    "The Shining",
                    "Stephen King",
                    "Horror",
                    18.00,
                    8
            );
            theShining.setRating(4.5);
            theShining.setDescription(
                    "A family spends the winter in an isolated hotel, where supernatural forces begin to threaten their lives."
            );
            bookRepository.save(theShining);

            // they both die at the end
            Book theyBothDie = new Book(
                    "They Both Die at the End",
                    "Adam Silvera",
                    "Young Adult",
                    22.99,
                    12
            );
            theyBothDie.setRating(4.6);
            theyBothDie.setDescription(
                    "Two strangers discover they have only one day left to live and form an unexpected connection while making their final memories."
            );
            bookRepository.save(theyBothDie);

            // after
            Book after = new Book(
                    "After",
                    "Anna Todd",
                    "Romance",
                    19.99,
                    15
            );
            after.setRating(4.2);
            after.setDescription(
                    "A college student named Tessa experiences a complicated romance that challenges her expectations of love and relationships."
            );
            bookRepository.save(after);
        };
    }
}