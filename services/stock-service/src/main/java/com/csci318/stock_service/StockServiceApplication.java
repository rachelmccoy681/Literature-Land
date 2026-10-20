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

            // more sample books, about eight for each category on the categories page

            // fiction
            addBook(bookRepository, "The Kite Runner", "Khaled Hosseini", "Fiction", 17.99, 9, 4.3,
                    "In Kabul and later California, Amir confronts a childhood betrayal of his friend Hassan and seeks a way to make things right.");
            addBook(bookRepository, "The Road", "Cormac McCarthy", "Fiction", 16.50, 6, 4.0,
                    "A father and his young son walk through a burned, post-apocalyptic America, carrying little more than each other.");
            addBook(bookRepository, "Life of Pi", "Yann Martel", "Fiction", 15.99, 11, 4.0,
                    "After a shipwreck, a teenage boy survives 227 days on a lifeboat in the Pacific Ocean with a Bengal tiger.");
            addBook(bookRepository, "The Midnight Library", "Matt Haig", "Fiction", 18.99, 14, 4.1,
                    "Between life and death, Nora finds a library whose books let her try out the other lives she could have lived.");
            addBook(bookRepository, "Where the Crawdads Sing", "Delia Owens", "Fiction", 19.50, 12, 4.4,
                    "Abandoned as a child, Kya grows up alone in the North Carolina marshes and becomes the prime suspect in a local murder.");
            addBook(bookRepository, "The Book Thief", "Markus Zusak", "Fiction", 16.99, 8, 4.4,
                    "Narrated by Death, the story follows Liesel, a girl in Nazi Germany who steals books and shares them with others.");
            addBook(bookRepository, "A Man Called Ove", "Fredrik Backman", "Fiction", 17.50, 10, 4.4,
                    "A grumpy, grieving widower's plans are upended when lively new neighbours move in across the street.");
            addBook(bookRepository, "Normal People", "Sally Rooney", "Fiction", 18.00, 0, 3.9,
                    "Connell and Marianne, from very different backgrounds in a small Irish town, drift together and apart through school and university.");

            // non fiction
            addBook(bookRepository, "Sapiens", "Yuval Noah Harari", "Non Fiction", 24.99, 13, 4.4,
                    "A sweeping history of humankind, from early hunter-gatherers to the scientific and economic revolutions of the modern world.");
            addBook(bookRepository, "Atomic Habits", "James Clear", "Non Fiction", 22.00, 20, 4.4,
                    "A practical guide to building good habits and breaking bad ones through small changes that compound over time.");
            addBook(bookRepository, "Thinking, Fast and Slow", "Daniel Kahneman", "Non Fiction", 21.50, 7, 4.2,
                    "A Nobel Prize-winning psychologist explains the two systems of thought that shape our judgements and decisions.");
            addBook(bookRepository, "Quiet", "Susan Cain", "Non Fiction", 18.99, 9, 4.1,
                    "Explores the strengths of introverts and how a culture built around extroversion overlooks them.");
            addBook(bookRepository, "The Body Keeps the Score", "Bessel van der Kolk", "Non Fiction", 23.99, 6, 4.4,
                    "A psychiatrist describes how trauma affects the brain and body, and the therapies that help people recover.");
            addBook(bookRepository, "Freakonomics", "Steven D. Levitt and Stephen J. Dubner", "Non Fiction", 17.99, 10, 4.0,
                    "An economist and a journalist use data to uncover the hidden incentives behind everyday behaviour.");
            addBook(bookRepository, "A Short History of Nearly Everything", "Bill Bryson", "Non Fiction", 20.99, 8, 4.2,
                    "Bill Bryson explains how scientists came to understand the universe, the Earth and life itself, from the Big Bang onwards.");
            addBook(bookRepository, "The Immortal Life of Henrietta Lacks", "Rebecca Skloot", "Non Fiction", 19.50, 5, 4.1,
                    "The story of the woman whose cells, taken without her knowledge in 1951, became one of medicine's most important tools.");

            // mystery
            addBook(bookRepository, "The Girl with the Dragon Tattoo", "Stieg Larsson", "Mystery", 18.99, 9, 4.2,
                    "A journalist and a brilliant hacker investigate the decades-old disappearance of a wealthy industrialist's niece.");
            addBook(bookRepository, "Gone Girl", "Gillian Flynn", "Mystery", 17.99, 11, 4.1,
                    "When Amy Dunne vanishes on her fifth wedding anniversary, suspicion quickly falls on her husband Nick.");
            addBook(bookRepository, "The Da Vinci Code", "Dan Brown", "Mystery", 16.99, 14, 3.9,
                    "A symbologist and a cryptologist follow clues hidden in famous artworks after a murder at the Louvre.");
            addBook(bookRepository, "And Then There Were None", "Agatha Christie", "Mystery", 14.99, 10, 4.3,
                    "Ten strangers are invited to an island off the English coast, where they begin to die one by one.");
            addBook(bookRepository, "The Hound of the Baskervilles", "Arthur Conan Doyle", "Mystery", 12.99, 7, 4.1,
                    "Sherlock Holmes and Dr Watson investigate the legend of a supernatural hound haunting a family on Dartmoor.");
            addBook(bookRepository, "Big Little Lies", "Liane Moriarty", "Mystery", 18.50, 8, 4.2,
                    "A death at a school trivia night in a seaside Australian town exposes the secrets of three mothers.");
            addBook(bookRepository, "The Silent Patient", "Alex Michaelides", "Mystery", 19.99, 12, 4.1,
                    "A famous painter shoots her husband and then never speaks again, until a psychotherapist sets out to uncover why.");
            addBook(bookRepository, "The Thursday Murder Club", "Richard Osman", "Mystery", 18.99, 0, 4.0,
                    "Four friends in a retirement village who meet to discuss cold cases find themselves investigating a real murder.");

            // romance
            addBook(bookRepository, "The Notebook", "Nicholas Sparks", "Romance", 15.99, 10, 4.1,
                    "An elderly man reads to a woman in a nursing home the story of a summer romance that shaped both of their lives.");
            addBook(bookRepository, "Me Before You", "Jojo Moyes", "Romance", 17.50, 12, 4.3,
                    "A young woman takes a job caring for a man paralysed in an accident, and their lives change in ways neither expected.");
            addBook(bookRepository, "The Fault in Our Stars", "John Green", "Romance", 16.99, 13, 4.2,
                    "Two teenagers who meet at a cancer support group fall in love and travel to Amsterdam to meet a reclusive author.");
            addBook(bookRepository, "Outlander", "Diana Gabaldon", "Romance", 21.99, 7, 4.3,
                    "A nurse from 1945 is swept back in time to 18th-century Scotland, where she is torn between two very different men.");
            addBook(bookRepository, "It Ends with Us", "Colleen Hoover", "Romance", 18.99, 15, 4.2,
                    "Lily falls for a charming neurosurgeon, but as cracks appear in the relationship she must face painful choices.");
            addBook(bookRepository, "The Hating Game", "Sally Thorne", "Romance", 16.50, 9, 4.0,
                    "Two executive assistants who can't stand each other compete for the same promotion, and the rivalry turns into something else.");
            addBook(bookRepository, "Beach Read", "Emily Henry", "Romance", 17.99, 11, 4.0,
                    "Two writers with writer's block swap genres for a summer: a romance novelist tries literary fiction and a literary novelist tries romance.");

            // fantasy
            addBook(bookRepository, "The Fellowship of the Ring", "J.R.R. Tolkien", "Fantasy", 22.99, 8, 4.6,
                    "Frodo Baggins sets out from the Shire on a perilous quest to destroy the One Ring before the Dark Lord Sauron can reclaim it.");
            addBook(bookRepository, "A Game of Thrones", "George R.R. Martin", "Fantasy", 24.99, 10, 4.5,
                    "Noble families fight for control of the Iron Throne of Westeros while an ancient threat gathers beyond the Wall.");
            addBook(bookRepository, "The Name of the Wind", "Patrick Rothfuss", "Fantasy", 21.50, 6, 4.5,
                    "Kvothe, now an innkeeper in hiding, recounts how he grew from a gifted orphan into a legendary wizard.");
            addBook(bookRepository, "Mistborn: The Final Empire", "Brandon Sanderson", "Fantasy", 20.99, 9, 4.5,
                    "A street thief with rare magical powers joins a crew planning to overthrow an immortal emperor.");
            addBook(bookRepository, "Good Omens", "Terry Pratchett and Neil Gaiman", "Fantasy", 18.99, 7, 4.3,
                    "An angel and a demon who have grown fond of life on Earth team up to stop the coming Apocalypse.");
            addBook(bookRepository, "Circe", "Madeline Miller", "Fantasy", 19.99, 11, 4.3,
                    "The daughter of the sun god Helios is banished to an island, where she discovers her power as a witch.");

            // science fiction
            addBook(bookRepository, "Dune", "Frank Herbert", "Science Fiction", 21.99, 12, 4.3,
                    "Young Paul Atreides is caught up in a struggle for the desert planet Arrakis, the only source of the universe's most valuable substance.");
            addBook(bookRepository, "Ender's Game", "Orson Scott Card", "Science Fiction", 16.99, 9, 4.3,
                    "A gifted child is trained at a military academy in orbit to lead humanity's fight against an alien invasion.");
            addBook(bookRepository, "The Martian", "Andy Weir", "Science Fiction", 18.50, 14, 4.4,
                    "Stranded alone on Mars after his crew leaves him for dead, astronaut Mark Watney must use his skills to survive.");
            addBook(bookRepository, "Project Hail Mary", "Andy Weir", "Science Fiction", 20.99, 10, 4.5,
                    "A science teacher wakes up alone on a spaceship with no memory, on a mission to save Earth from a dimming sun.");
            addBook(bookRepository, "Neuromancer", "William Gibson", "Science Fiction", 15.99, 5, 3.9,
                    "A washed-up computer hacker is hired for one last job in a cyberpunk world of artificial intelligence and corporate power.");
            addBook(bookRepository, "The Hitchhiker's Guide to the Galaxy", "Douglas Adams", "Science Fiction", 14.99, 13, 4.2,
                    "Seconds before Earth is demolished, Arthur Dent is rescued by an alien friend and begins a comic journey through space.");
            addBook(bookRepository, "Foundation", "Isaac Asimov", "Science Fiction", 17.50, 8, 4.2,
                    "A mathematician predicts the fall of the Galactic Empire and creates a foundation to shorten the dark age that follows.");
            addBook(bookRepository, "The Left Hand of Darkness", "Ursula K. Le Guin", "Science Fiction", 16.50, 0, 4.1,
                    "An envoy visits a frozen planet whose inhabitants have no fixed sex, and must learn to understand them to succeed.");

            // biography
            addBook(bookRepository, "Steve Jobs", "Walter Isaacson", "Biography", 24.99, 7, 4.2,
                    "Based on interviews with Jobs and the people around him, the life of the Apple co-founder and his impact on technology.");
            addBook(bookRepository, "Becoming", "Michelle Obama", "Biography", 22.99, 12, 4.6,
                    "The former First Lady of the United States describes her childhood in Chicago, her career and her years in the White House.");
            addBook(bookRepository, "Educated", "Tara Westover", "Biography", 19.99, 10, 4.5,
                    "Raised by survivalists in Idaho without schooling, Tara Westover teaches herself enough to go to university and beyond.");
            addBook(bookRepository, "Long Walk to Freedom", "Nelson Mandela", "Biography", 21.50, 6, 4.5,
                    "Nelson Mandela's autobiography, from his childhood and decades in prison to becoming South Africa's first democratically elected president.");
            addBook(bookRepository, "The Diary of a Young Girl", "Anne Frank", "Biography", 13.99, 15, 4.6,
                    "The diary Anne Frank kept while hiding with her family from the Nazis in a secret annex in Amsterdam.");
            addBook(bookRepository, "I Know Why the Caged Bird Sings", "Maya Angelou", "Biography", 16.99, 8, 4.3,
                    "Maya Angelou's memoir of growing up in the American South, and how a love of literature helped her overcome trauma and racism.");
            addBook(bookRepository, "Born a Crime", "Trevor Noah", "Biography", 18.50, 11, 4.5,
                    "Comedian Trevor Noah tells funny and moving stories of growing up mixed-race in South Africa during and after apartheid.");
            addBook(bookRepository, "Einstein: His Life and Universe", "Walter Isaacson", "Biography", 23.50, 5, 4.2,
                    "A biography of Albert Einstein drawing on his personal letters, covering his science, his politics and his family life.");

            // history
            addBook(bookRepository, "Guns, Germs, and Steel", "Jared Diamond", "History", 22.50, 8, 4.0,
                    "Explores why some societies came to dominate others, pointing to geography, farming and disease rather than the people themselves.");
            addBook(bookRepository, "The Rise and Fall of the Third Reich", "William L. Shirer", "History", 27.99, 4, 4.3,
                    "A journalist who lived in Berlin in the 1930s gives a detailed history of Nazi Germany from Hitler's rise to its defeat.");
            addBook(bookRepository, "SPQR", "Mary Beard", "History", 21.99, 7, 4.1,
                    "A history of ancient Rome, from its founding myths to the third century, told by one of Britain's best-known classicists.");
            addBook(bookRepository, "The Guns of August", "Barbara W. Tuchman", "History", 19.99, 6, 4.2,
                    "A Pulitzer Prize-winning account of the first month of the First World War and the decisions that led to it.");
            addBook(bookRepository, "Team of Rivals", "Doris Kearns Goodwin", "History", 24.50, 5, 4.4,
                    "How Abraham Lincoln brought his political rivals into his cabinet and led the United States through the Civil War.");
            addBook(bookRepository, "The Silk Roads", "Peter Frankopan", "History", 22.99, 9, 4.1,
                    "Retells world history from the perspective of the trade routes linking East and West, and the empires that grew along them.");
            addBook(bookRepository, "1776", "David McCullough", "History", 18.99, 10, 4.2,
                    "The story of the American Revolution's pivotal year, following George Washington's army and their British opponents.");
            addBook(bookRepository, "A People's History of the United States", "Howard Zinn", "History", 20.50, 0, 4.1,
                    "American history told from the point of view of workers, women, enslaved people and others often left out of textbooks.");

            // travel
            addBook(bookRepository, "In a Sunburned Country", "Bill Bryson", "Travel", 17.99, 9, 4.1,
                    "Bill Bryson travels across Australia, finding humour and wonder in its people, wildlife and enormous landscapes.");
            addBook(bookRepository, "Into the Wild", "Jon Krakauer", "Travel", 16.50, 11, 4.0,
                    "The true story of Christopher McCandless, who gave away his savings and walked alone into the Alaskan wilderness.");
            addBook(bookRepository, "Eat, Pray, Love", "Elizabeth Gilbert", "Travel", 17.50, 13, 3.6,
                    "After a painful divorce, the author spends a year in Italy, India and Indonesia searching for pleasure, devotion and balance.");
            addBook(bookRepository, "Wild", "Cheryl Strayed", "Travel", 17.99, 10, 4.0,
                    "After losing her mother, Cheryl Strayed hikes more than a thousand miles of the Pacific Crest Trail alone.");
            addBook(bookRepository, "A Walk in the Woods", "Bill Bryson", "Travel", 16.99, 8, 4.1,
                    "Bill Bryson sets out to hike the Appalachian Trail with an out-of-shape old friend, with comic and thoughtful results.");
            addBook(bookRepository, "The Great Railway Bazaar", "Paul Theroux", "Travel", 18.50, 5, 3.9,
                    "Paul Theroux travels by train from London through Asia and back, describing the people he meets along the way.");
            addBook(bookRepository, "Under the Tuscan Sun", "Frances Mayes", "Travel", 16.50, 7, 3.8,
                    "An American writer buys and restores an abandoned villa in Tuscany, discovering the food and culture of rural Italy.");
            addBook(bookRepository, "Travels with Charley", "John Steinbeck", "Travel", 15.99, 6, 4.1,
                    "In his late fifties, John Steinbeck drives across the United States with his poodle Charley to rediscover his country.");

            // children's books
            addBook(bookRepository, "Charlotte's Web", "E.B. White", "Children's Books", 11.99, 15, 4.2,
                    "A pig named Wilbur is saved from slaughter by his clever friend Charlotte, a spider who spins words into her web.");
            addBook(bookRepository, "Matilda", "Roald Dahl", "Children's Books", 12.99, 14, 4.4,
                    "A brilliant girl with neglectful parents and a cruel headmistress discovers she has extraordinary powers.");
            addBook(bookRepository, "The Lion, the Witch and the Wardrobe", "C.S. Lewis", "Children's Books", 13.50, 12, 4.3,
                    "Four siblings step through a wardrobe into Narnia, a magical land trapped in eternal winter by the White Witch.");
            addBook(bookRepository, "Charlie and the Chocolate Factory", "Roald Dahl", "Children's Books", 12.50, 13, 4.2,
                    "Charlie Bucket wins a golden ticket to tour the mysterious chocolate factory of the eccentric Willy Wonka.");
            addBook(bookRepository, "The Very Hungry Caterpillar", "Eric Carle", "Children's Books", 9.99, 20, 4.3,
                    "A caterpillar eats its way through a week of foods before turning into a beautiful butterfly.");
            addBook(bookRepository, "Where the Wild Things Are", "Maurice Sendak", "Children's Books", 10.99, 16, 4.2,
                    "Sent to bed without supper, Max sails to a land of wild creatures and becomes their king.");
            addBook(bookRepository, "Wonder", "R.J. Palacio", "Children's Books", 14.99, 11, 4.4,
                    "Auggie, a boy born with a facial difference, goes to a mainstream school for the first time in fifth grade.");
            addBook(bookRepository, "The Gruffalo", "Julia Donaldson", "Children's Books", 10.50, 18, 4.4,
                    "A clever mouse invents a fearsome creature to scare off predators in the deep dark wood, then meets the real thing.");

            // classics
            addBook(bookRepository, "Pride and Prejudice", "Jane Austen", "Classics", 12.99, 14, 4.3,
                    "Elizabeth Bennet clashes with the proud Mr Darcy as she navigates manners, marriage and money in Regency England.");
            addBook(bookRepository, "To Kill a Mockingbird", "Harper Lee", "Classics", 14.99, 12, 4.3,
                    "In 1930s Alabama, young Scout Finch watches her father defend a Black man falsely accused of a crime.");
            addBook(bookRepository, "1984", "George Orwell", "Classics", 13.99, 15, 4.2,
                    "Winston Smith works for a totalitarian regime that watches its citizens' every move, and begins to rebel.");
            addBook(bookRepository, "The Great Gatsby", "F. Scott Fitzgerald", "Classics", 11.99, 10, 3.9,
                    "Nick Carraway is drawn into the world of his mysterious millionaire neighbour Jay Gatsby on Long Island.");
            addBook(bookRepository, "Jane Eyre", "Charlotte Brontë", "Classics", 12.50, 8, 4.1,
                    "An orphaned governess falls in love with her employer, Mr Rochester, who hides a dark secret at Thornfield Hall.");
            addBook(bookRepository, "Wuthering Heights", "Emily Brontë", "Classics", 11.99, 7, 3.9,
                    "The passionate and destructive love between Heathcliff and Catherine Earnshaw echoes across two generations on the Yorkshire moors.");
            addBook(bookRepository, "Frankenstein", "Mary Shelley", "Classics", 10.99, 9, 3.9,
                    "A young scientist creates a living being from dead matter and is horrified by what he has made.");
            addBook(bookRepository, "Moby-Dick", "Herman Melville", "Classics", 13.50, 5, 3.5,
                    "Ishmael joins the whaling ship Pequod, whose Captain Ahab is obsessed with hunting the white whale that took his leg.");
            addBook(bookRepository, "Little Women", "Louisa May Alcott", "Classics", 12.99, 11, 4.1,
                    "The four March sisters grow up in New England during the Civil War, each pursuing her own dreams.");

            // poetry
            addBook(bookRepository, "Milk and Honey", "Rupi Kaur", "Poetry", 14.99, 15, 3.9,
                    "A collection of poems and prose about love, loss, trauma and healing, divided into four chapters.");
            addBook(bookRepository, "The Sun and Her Flowers", "Rupi Kaur", "Poetry", 15.99, 12, 3.9,
                    "Rupi Kaur's second collection explores growth, roots, family and immigration through the life cycle of a flower.");
            addBook(bookRepository, "Leaves of Grass", "Walt Whitman", "Poetry", 13.50, 6, 4.1,
                    "Walt Whitman's lifelong collection celebrating democracy, nature, the body and the individual self.");
            addBook(bookRepository, "The Complete Poems of Emily Dickinson", "Emily Dickinson", "Poetry", 18.99, 5, 4.4,
                    "Nearly 1,800 short, inventive poems on death, nature and the inner life, most of them unpublished during Dickinson's lifetime.");
            addBook(bookRepository, "Ariel", "Sylvia Plath", "Poetry", 13.99, 7, 4.2,
                    "Sylvia Plath's intense final poems, published after her death, which established her as a major poet.");
            addBook(bookRepository, "The Prophet", "Kahlil Gibran", "Poetry", 11.99, 10, 4.2,
                    "A prophet about to leave a city shares his wisdom on love, marriage, work, freedom and death.");
            addBook(bookRepository, "Where the Sidewalk Ends", "Shel Silverstein", "Poetry", 14.50, 13, 4.5,
                    "Funny and imaginative poems and drawings for children and adults alike.");
            addBook(bookRepository, "The Odyssey", "Homer", "Poetry", 15.50, 8, 3.9,
                    "Odysseus struggles for ten years to return home after the Trojan War, facing monsters, gods and temptation.");
            addBook(bookRepository, "The Road Not Taken and Other Poems", "Robert Frost", "Poetry", 9.99, 11, 4.3,
                    "A collection of Robert Frost's best-known poems about rural New England life, choices and nature.");
        };
    }

    // creates a sample book and saves it to the database
    private static void addBook(
            BookRepository bookRepository,
            String title,
            String author,
            String genre,
            double price,
            int stockQuantity,
            double rating,
            String description) {

        Book book = new Book(title, author, genre, price, stockQuantity);
        book.setRating(rating);
        book.setDescription(description);
        bookRepository.save(book);
    }
}