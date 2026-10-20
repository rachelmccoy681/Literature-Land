package com.csci318.stock_service.config;

import com.csci318.stock_service.domain.Book;
import com.csci318.stock_service.repository.BookRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Configuration
public class SampleBookData {

    // ADD SAMPLE BOOKS TO DATABASE
    @Bean
    CommandLineRunner addSampleBooks(BookRepository bookRepository) {
        return args -> {

            // LOAD EXISTING BOOKS TO PREVENT DUPLICATES
            Set<String> existingBooks = new HashSet<>();

            bookRepository.findAll().forEach(book ->
                    existingBooks.add(
                            (book.getTitle() + "|" + book.getAuthor())
                                    .toLowerCase(Locale.ROOT)
                    )
            );

            // FANTASY

            addBook(bookRepository, existingBooks, "Harry Potter", "J.K. Rowling", "Fantasy", 25.00, 10, 4.8,
                    "A young wizard discovers his magical abilities and begins his journey at Hogwarts School of Witchcraft and Wizardry.");

            addBook(bookRepository, existingBooks, "The Hobbit", "J.R.R. Tolkien", "Fantasy", 20.00, 5, 4.7,
                    "Bilbo Baggins joins a group of dwarves on an unexpected adventure to reclaim their homeland from a dragon.");

            addBook(bookRepository, existingBooks, "Fourth Wing", "Rebecca Yarros", "Fantasy", 29.99, 20, 4.7,
                    "A young woman enters a dangerous academy where dragon riders are trained.");

            addBook(bookRepository, existingBooks, "A Court of Thorns and Roses", "Sarah J. Maas", "Fantasy", 24.99, 14, 4.6,
                    "A young huntress enters a dangerous magical world inhabited by faeries.");

            addBook(bookRepository, existingBooks, "The Name of the Wind", "Patrick Rothfuss", "Fantasy", 27.50, 7, 4.8,
                    "A legendary magician recounts the extraordinary events of his life.");

            addBook(bookRepository, existingBooks, "Mistborn", "Brandon Sanderson", "Fantasy", 26.99, 16, 4.8,
                    "A young thief discovers magical abilities and joins a rebellion.");

            addBook(bookRepository, existingBooks, "The Way of Kings", "Brandon Sanderson", "Fantasy", 34.99, 9, 4.9,
                    "Warriors and scholars struggle in a world shaped by powerful storms.");

            addBook(bookRepository, existingBooks, "The Priory of the Orange Tree", "Samantha Shannon", "Fantasy", 30.99, 12, 4.5,
                    "A world of dragons and magic faces an ancient threat.");

            addBook(bookRepository, existingBooks, "The Cruel Prince", "Holly Black", "Fantasy", 22.99, 18, 4.5,
                    "A human girl navigates dangerous politics in the world of the fae.");

            addBook(bookRepository, existingBooks, "Six of Crows", "Leigh Bardugo", "Fantasy", 24.50, 0, 4.7,
                    "A group of criminals attempts an impossible heist.");

            // ROMANCE

            addBook(bookRepository, existingBooks, "After", "Anna Todd", "Romance", 19.99, 15, 4.2,
                    "A college student named Tessa experiences a complicated romance that challenges her expectations of love and relationships.");

            addBook(bookRepository, existingBooks, "Pride and Prejudice", "Jane Austen", "Romance", 16.99, 18, 4.8,
                    "Elizabeth Bennet navigates family expectations and her changing feelings toward Mr Darcy.");

            addBook(bookRepository, existingBooks, "The Love Hypothesis", "Ali Hazelwood", "Romance", 22.99, 12, 4.5,
                    "A graduate student enters a fake relationship that becomes unexpectedly complicated.");

            addBook(bookRepository, existingBooks, "It Ends with Us", "Colleen Hoover", "Romance", 21.99, 16, 4.4,
                    "A young woman confronts difficult choices about love and relationships.");

            addBook(bookRepository, existingBooks, "Beach Read", "Emily Henry", "Romance", 23.50, 9, 4.4,
                    "Two writers challenge each other to write outside their usual genres.");

            addBook(bookRepository, existingBooks, "Book Lovers", "Emily Henry", "Romance", 24.99, 13, 4.5,
                    "A literary agent discovers an unexpected connection during a holiday.");

            addBook(bookRepository, existingBooks, "People We Meet on Vacation", "Emily Henry", "Romance", 22.99, 8, 4.4,
                    "Two longtime friends reconsider their relationship during a summer trip.");

            addBook(bookRepository, existingBooks, "The Spanish Love Deception", "Elena Armas", "Romance", 23.99, 14, 4.3,
                    "A woman asks her colleague to pretend to be her boyfriend at a wedding.");

            addBook(bookRepository, existingBooks, "Love on the Brain", "Ali Hazelwood", "Romance", 25.50, 6, 4.4,
                    "A scientist finds herself working alongside a former academic rival.");

            addBook(bookRepository, existingBooks, "The Hating Game", "Sally Thorne", "Romance", 20.99, 0, 4.3,
                    "Two competitive coworkers discover their rivalry may hide romantic feelings.");

            // FICTION

            addBook(bookRepository, existingBooks, "Normal People", "Sally Rooney", "Fiction", 22.00, 13, 4.3,
                    "Two young people develop a complicated relationship as they enter adulthood.");

            addBook(bookRepository, existingBooks, "Tomorrow, and Tomorrow, and Tomorrow", "Gabrielle Zevin", "Fiction", 28.99, 11, 4.6,
                    "Two childhood friends reconnect and create video games together.");

            addBook(bookRepository, existingBooks, "The Midnight Library", "Matt Haig", "Fiction", 24.50, 15, 4.4,
                    "A woman explores alternative versions of her life through a mysterious library.");

            addBook(bookRepository, existingBooks, "A Little Life", "Hanya Yanagihara", "Fiction", 29.99, 6, 4.5,
                    "Four friends navigate adulthood and personal struggles in New York.");

            addBook(bookRepository, existingBooks, "The Seven Husbands of Evelyn Hugo", "Taylor Jenkins Reid", "Fiction", 25.99, 17, 4.7,
                    "A Hollywood actress reveals the secrets behind her career and marriages.");

            addBook(bookRepository, existingBooks, "The Kite Runner", "Khaled Hosseini", "Fiction", 21.99, 12, 4.7,
                    "A man reflects on childhood friendship, betrayal and redemption.");

            addBook(bookRepository, existingBooks, "The Alchemist", "Paulo Coelho", "Fiction", 19.99, 21, 4.5,
                    "A young shepherd travels in search of treasure and discovers important life lessons.");

            addBook(bookRepository, existingBooks, "Where the Crawdads Sing", "Delia Owens", "Fiction", 23.99, 8, 4.6,
                    "A young woman growing up in the marshlands becomes connected to a mysterious death.");

            addBook(bookRepository, existingBooks, "The Book Thief", "Markus Zusak", "Fiction", 24.99, 10, 4.8,
                    "A young girl finds comfort in books during the Second World War.");

            addBook(bookRepository, existingBooks, "The Goldfinch", "Donna Tartt", "Fiction", 27.99, 0, 4.4,
                    "A boy's life changes after a tragedy at an art museum.");

            // NON FICTION

            addBook(bookRepository, existingBooks, "Atomic Habits", "James Clear", "Non Fiction", 27.99, 25, 4.8,
                    "A practical guide to building good habits and breaking unhelpful ones.");

            addBook(bookRepository, existingBooks, "Deep Work", "Cal Newport", "Non Fiction", 24.99, 10, 4.6,
                    "Explores how focused work can improve productivity.");

            addBook(bookRepository, existingBooks, "The Psychology of Money", "Morgan Housel", "Non Fiction", 26.50, 19, 4.7,
                    "Examines how human behaviour influences financial decisions.");

            addBook(bookRepository, existingBooks, "Thinking, Fast and Slow", "Daniel Kahneman", "Non Fiction", 29.99, 8, 4.6,
                    "Explains how different systems of thinking influence human judgement.");

            addBook(bookRepository, existingBooks, "Sapiens", "Yuval Noah Harari", "Non Fiction", 28.00, 12, 4.7,
                    "Explores the history of humanity from early humans to modern civilisation.");

            addBook(bookRepository, existingBooks, "The Power of Habit", "Charles Duhigg", "Non Fiction", 23.99, 14, 4.5,
                    "Investigates how habits form and how they can be changed.");

            addBook(bookRepository, existingBooks, "Outliers", "Malcolm Gladwell", "Non Fiction", 22.50, 11, 4.5,
                    "Explores the factors that contribute to extraordinary success.");

            addBook(bookRepository, existingBooks, "Quiet", "Susan Cain", "Non Fiction", 24.99, 7, 4.6,
                    "Examines the strengths of introverted people in modern society.");

            addBook(bookRepository, existingBooks, "The 5 AM Club", "Robin Sharma", "Non Fiction", 25.99, 5, 4.2,
                    "Presents a morning routine designed to improve productivity.");

            addBook(bookRepository, existingBooks, "Essentialism", "Greg McKeown", "Non Fiction", 21.99, 0, 4.5,
                    "Explores how focusing on fewer priorities can lead to meaningful progress.");

            // MYSTERY

            addBook(bookRepository, existingBooks, "Gone Girl", "Gillian Flynn", "Mystery", 21.99, 14, 4.5,
                    "A woman's disappearance raises disturbing questions about her marriage.");

            addBook(bookRepository, existingBooks, "The Silent Patient", "Alex Michaelides", "Mystery", 23.99, 20, 4.6,
                    "A psychotherapist investigates why a woman stopped speaking after a violent incident.");

            addBook(bookRepository, existingBooks, "The Thursday Murder Club", "Richard Osman", "Mystery", 24.99, 11, 4.5,
                    "A group of retirees investigates suspicious deaths in their community.");

            addBook(bookRepository, existingBooks, "And Then There Were None", "Agatha Christie", "Mystery", 18.99, 9, 4.8,
                    "Ten strangers visit an isolated island where mysterious deaths begin occurring.");

            addBook(bookRepository, existingBooks, "The Girl on the Train", "Paula Hawkins", "Mystery", 20.50, 0, 4.2,
                    "A commuter becomes involved in a missing-person investigation.");

            addBook(bookRepository, existingBooks, "The Murder of Roger Ackroyd", "Agatha Christie", "Mystery", 19.99, 8, 4.7,
                    "Detective Hercule Poirot investigates a murder in an English village.");

            addBook(bookRepository, existingBooks, "The Guest List", "Lucy Foley", "Mystery", 22.99, 13, 4.3,
                    "A wedding on a remote island turns deadly.");

            addBook(bookRepository, existingBooks, "The Paris Apartment", "Lucy Foley", "Mystery", 24.50, 7, 4.2,
                    "A woman searches for her missing brother in a mysterious Paris apartment building.");

            addBook(bookRepository, existingBooks, "The Maid", "Nita Prose", "Mystery", 23.99, 10, 4.4,
                    "A hotel maid becomes involved in a murder investigation.");

            addBook(bookRepository, existingBooks, "Big Little Lies", "Liane Moriarty", "Mystery", 21.99, 16, 4.5,
                    "Secrets and rivalries among parents lead to a shocking incident.");

            // SCIENCE FICTION

            addBook(bookRepository, existingBooks, "Dune", "Frank Herbert", "Science Fiction", 26.99, 16, 4.8,
                    "A young nobleman becomes involved in a struggle for control of a desert planet.");

            addBook(bookRepository, existingBooks, "The Martian", "Andy Weir", "Science Fiction", 24.99, 13, 4.7,
                    "An astronaut stranded on Mars uses science to survive.");

            addBook(bookRepository, existingBooks, "Project Hail Mary", "Andy Weir", "Science Fiction", 28.99, 18, 4.8,
                    "An astronaut awakens on a distant mission and must solve a threat to humanity.");

            addBook(bookRepository, existingBooks, "Neuromancer", "William Gibson", "Science Fiction", 22.50, 6, 4.4,
                    "A computer hacker becomes involved in a dangerous mission involving artificial intelligence.");

            addBook(bookRepository, existingBooks, "The Three-Body Problem", "Liu Cixin", "Science Fiction", 25.99, 0, 4.6,
                    "Scientists uncover a mysterious connection between humanity and an alien civilisation.");

            addBook(bookRepository, existingBooks, "Foundation", "Isaac Asimov", "Science Fiction", 23.99, 12, 4.7,
                    "A mathematician attempts to preserve knowledge as a galactic empire collapses.");

            addBook(bookRepository, existingBooks, "Ender's Game", "Orson Scott Card", "Science Fiction", 21.99, 14, 4.6,
                    "A gifted child trains to defend humanity against an alien threat.");

            addBook(bookRepository, existingBooks, "Ready Player One", "Ernest Cline", "Science Fiction", 24.50, 19, 4.5,
                    "A teenager searches for a hidden prize inside a virtual reality world.");

            addBook(bookRepository, existingBooks, "Snow Crash", "Neal Stephenson", "Science Fiction", 26.99, 7, 4.4,
                    "A hacker investigates a dangerous digital phenomenon.");

            addBook(bookRepository, existingBooks, "The Left Hand of Darkness", "Ursula K. Le Guin", "Science Fiction", 22.99, 5, 4.6,
                    "An envoy travels to a distant planet to establish diplomatic relations.");

            // BIOGRAPHY

            addBook(bookRepository, existingBooks, "Becoming", "Michelle Obama", "Biography", 29.99, 15, 4.8,
                    "Michelle Obama reflects on her childhood, career and experiences as First Lady.");

            addBook(bookRepository, existingBooks, "Steve Jobs", "Walter Isaacson", "Biography", 32.99, 9, 4.7,
                    "An account of the life and career of technology entrepreneur Steve Jobs.");

            addBook(bookRepository, existingBooks, "Educated", "Tara Westover", "Biography", 25.99, 14, 4.7,
                    "A memoir about growing up in an isolated family and pursuing an education.");

            addBook(bookRepository, existingBooks, "I Am Malala", "Malala Yousafzai", "Biography", 22.99, 11, 4.7,
                    "Malala recounts her experiences advocating for girls' education.");

            addBook(bookRepository, existingBooks, "Born a Crime", "Trevor Noah", "Biography", 24.50, 8, 4.8,
                    "Trevor Noah shares stories about growing up in South Africa.");

            addBook(bookRepository, existingBooks, "The Diary of a Young Girl", "Anne Frank", "Biography", 18.99, 17, 4.8,
                    "A young girl records her experiences while hiding during the Second World War.");

            addBook(bookRepository, existingBooks, "Long Walk to Freedom", "Nelson Mandela", "Biography", 30.99, 6, 4.8,
                    "Nelson Mandela recounts his life and struggle against apartheid.");

            addBook(bookRepository, existingBooks, "Open", "Andre Agassi", "Biography", 26.99, 10, 4.7,
                    "A tennis champion reflects on his career and personal life.");

            addBook(bookRepository, existingBooks, "The Story of My Life", "Helen Keller", "Biography", 19.99, 5, 4.6,
                    "Helen Keller describes her childhood and education.");

            addBook(bookRepository, existingBooks, "Finding Me", "Viola Davis", "Biography", 27.99, 0, 4.8,
                    "An actress reflects on her childhood and journey into entertainment.");

            // HISTORY

            addBook(bookRepository, existingBooks, "A Short History of Nearly Everything", "Bill Bryson", "History", 27.99, 12, 4.7,
                    "An accessible exploration of scientific discoveries and human understanding.");

            addBook(bookRepository, existingBooks, "The Silk Roads", "Peter Frankopan", "History", 29.99, 10, 4.6,
                    "Explores world history through trade routes connecting Asia and Europe.");

            addBook(bookRepository, existingBooks, "Guns, Germs, and Steel", "Jared Diamond", "History", 28.99, 9, 4.5,
                    "Examines geographical and environmental influences on human societies.");

            addBook(bookRepository, existingBooks, "SPQR", "Mary Beard", "History", 26.99, 8, 4.6,
                    "Explores the political and social history of ancient Rome.");

            addBook(bookRepository, existingBooks, "The Wright Brothers", "David McCullough", "History", 24.99, 7, 4.7,
                    "Tells the story of the brothers who pioneered powered flight.");

            addBook(bookRepository, existingBooks, "The Second World War", "Antony Beevor", "History", 34.99, 5, 4.6,
                    "An account of the major events of the Second World War.");

            addBook(bookRepository, existingBooks, "The History of the Ancient World", "Susan Wise Bauer", "History", 31.99, 6, 4.5,
                    "Surveys early civilisations and major developments in ancient history.");

            addBook(bookRepository, existingBooks, "The Crusades", "Thomas Asbridge", "History", 30.50, 11, 4.6,
                    "Examines the medieval conflicts known as the Crusades.");

            addBook(bookRepository, existingBooks, "The Romanovs", "Simon Sebag Montefiore", "History", 32.99, 4, 4.6,
                    "Chronicles the rise and fall of Russia's Romanov dynasty.");

            addBook(bookRepository, existingBooks, "The Warmth of Other Suns", "Isabel Wilkerson", "History", 29.99, 0, 4.8,
                    "Explores the migration of Black Americans from the southern United States.");

            // TRAVEL

            addBook(bookRepository, existingBooks, "Into the Wild", "Jon Krakauer", "Travel", 22.99, 10, 4.5,
                    "Follows a young man's journey into the Alaskan wilderness.");

            addBook(bookRepository, existingBooks, "A Walk in the Woods", "Bill Bryson", "Travel", 21.99, 14, 4.5,
                    "A humorous account of hiking the Appalachian Trail.");

            addBook(bookRepository, existingBooks, "Eat, Pray, Love", "Elizabeth Gilbert", "Travel", 23.99, 18, 4.3,
                    "A woman travels through Italy, India and Indonesia seeking personal growth.");

            addBook(bookRepository, existingBooks, "The Art of Travel", "Alain de Botton", "Travel", 24.50, 8, 4.4,
                    "Reflects on why people travel and how travel shapes their experiences.");

            addBook(bookRepository, existingBooks, "In a Sunburned Country", "Bill Bryson", "Travel", 22.50, 11, 4.6,
                    "A humorous exploration of Australia's landscapes, wildlife and culture.");

            addBook(bookRepository, existingBooks, "Wild", "Cheryl Strayed", "Travel", 24.99, 12, 4.6,
                    "A woman undertakes a long-distance hike while reflecting on her life.");

            addBook(bookRepository, existingBooks, "The Beach", "Alex Garland", "Travel", 20.99, 9, 4.2,
                    "A backpacker searches for a hidden island community in Thailand.");

            addBook(bookRepository, existingBooks, "Vagabonding", "Rolf Potts", "Travel", 21.50, 7, 4.5,
                    "Offers advice on long-term travel and exploring the world.");

            addBook(bookRepository, existingBooks, "The Geography of Bliss", "Eric Weiner", "Travel", 23.99, 6, 4.3,
                    "A journalist travels around the world exploring different ideas of happiness.");

            addBook(bookRepository, existingBooks, "The Lost City of Z", "David Grann", "Travel", 25.99, 0, 4.5,
                    "Investigates an explorer's disappearance in the Amazon.");

            // CHILDREN'S BOOKS

            addBook(bookRepository, existingBooks, "The Very Hungry Caterpillar", "Eric Carle", "Children's Books", 14.99, 24, 4.9,
                    "A colourful caterpillar eats through different foods before becoming a butterfly.");

            addBook(bookRepository, existingBooks, "Matilda", "Roald Dahl", "Children's Books", 16.99, 20, 4.8,
                    "A clever young girl discovers unusual powers and stands up to unfair adults.");

            addBook(bookRepository, existingBooks, "Charlie and the Chocolate Factory", "Roald Dahl", "Children's Books", 17.99, 18, 4.8,
                    "A young boy wins a chance to explore a mysterious chocolate factory.");

            addBook(bookRepository, existingBooks, "Where the Wild Things Are", "Maurice Sendak", "Children's Books", 15.99, 15, 4.7,
                    "A young boy imagines a journey to an island inhabited by wild creatures.");

            addBook(bookRepository, existingBooks, "The Gruffalo", "Julia Donaldson", "Children's Books", 14.50, 19, 4.8,
                    "A clever mouse invents a monster to protect itself in the forest.");

            addBook(bookRepository, existingBooks, "Green Eggs and Ham", "Dr. Seuss", "Children's Books", 13.99, 22, 4.7,
                    "A playful story about trying unfamiliar food.");

            addBook(bookRepository, existingBooks, "The Cat in the Hat", "Dr. Seuss", "Children's Books", 14.99, 17, 4.7,
                    "A mischievous cat creates chaos during a rainy afternoon.");

            addBook(bookRepository, existingBooks, "Charlotte's Web", "E.B. White", "Children's Books", 18.99, 13, 4.8,
                    "A spider helps save a young pig through an extraordinary friendship.");

            addBook(bookRepository, existingBooks, "The Tale of Peter Rabbit", "Beatrix Potter", "Children's Books", 12.99, 9, 4.7,
                    "A curious rabbit gets into trouble while exploring a vegetable garden.");

            addBook(bookRepository, existingBooks, "The Giving Tree", "Shel Silverstein", "Children's Books", 16.50, 0, 4.6,
                    "A story about the changing relationship between a boy and a tree.");

            // CLASSICS

            addBook(bookRepository, existingBooks, "1984", "George Orwell", "Classics", 18.99, 15, 4.8,
                    "A man struggles against a government that controls information and freedom.");

            addBook(bookRepository, existingBooks, "Jane Eyre", "Charlotte Bronte", "Classics", 17.99, 10, 4.7,
                    "A young governess navigates love, independence and difficult secrets.");

            addBook(bookRepository, existingBooks, "Little Women", "Louisa May Alcott", "Classics", 19.99, 13, 4.8,
                    "Four sisters grow up while navigating family, ambition and relationships.");

            addBook(bookRepository, existingBooks, "Wuthering Heights", "Emily Bronte", "Classics", 18.50, 9, 4.5,
                    "A dramatic story of love, revenge and family conflict.");

            addBook(bookRepository, existingBooks, "Great Expectations", "Charles Dickens", "Classics", 20.99, 11, 4.6,
                    "A young orphan experiences unexpected changes in fortune.");

            addBook(bookRepository, existingBooks, "To Kill a Mockingbird", "Harper Lee", "Classics", 21.99, 16, 4.8,
                    "A young girl witnesses racial injustice in a small American town.");

            addBook(bookRepository, existingBooks, "The Great Gatsby", "F. Scott Fitzgerald", "Classics", 17.50, 20, 4.6,
                    "A mysterious millionaire pursues an idealised vision of love and success.");

            addBook(bookRepository, existingBooks, "Moby-Dick", "Herman Melville", "Classics", 22.99, 6, 4.3,
                    "A ship captain becomes obsessed with hunting a legendary white whale.");

            addBook(bookRepository, existingBooks, "Frankenstein", "Mary Shelley", "Classics", 18.99, 12, 4.7,
                    "A scientist creates a living being and confronts the consequences.");

            addBook(bookRepository, existingBooks, "The Picture of Dorian Gray", "Oscar Wilde", "Classics", 19.50, 0, 4.7,
                    "A young man's portrait reflects the consequences of his choices.");

            // POETRY

            addBook(bookRepository, existingBooks, "Milk and Honey", "Rupi Kaur", "Poetry", 19.99, 17, 4.4,
                    "A collection of poems exploring love, loss and healing.");

            addBook(bookRepository, existingBooks, "The Sun and Her Flowers", "Rupi Kaur", "Poetry", 21.99, 14, 4.5,
                    "Poems exploring growth, relationships and self-discovery.");

            addBook(bookRepository, existingBooks, "The Waste Land", "T.S. Eliot", "Poetry", 17.99, 8, 4.5,
                    "A modernist poem exploring cultural fragmentation and renewal.");

            addBook(bookRepository, existingBooks, "Leaves of Grass", "Walt Whitman", "Poetry", 20.99, 11, 4.7,
                    "A collection celebrating humanity, nature and individual experience.");

            addBook(bookRepository, existingBooks, "Ariel", "Sylvia Plath", "Poetry", 22.50, 7, 4.7,
                    "A collection of emotionally intense poems exploring identity.");

            addBook(bookRepository, existingBooks, "The Collected Poems of Emily Dickinson", "Emily Dickinson", "Poetry", 24.99, 9, 4.8,
                    "A collection of poems exploring nature, mortality and human emotions.");

            addBook(bookRepository, existingBooks, "The Prophet", "Kahlil Gibran", "Poetry", 18.99, 15, 4.7,
                    "Poetic reflections on love, work, freedom and life.");

            addBook(bookRepository, existingBooks, "Twenty Love Poems and a Song of Despair", "Pablo Neruda", "Poetry", 19.50, 10, 4.6,
                    "A collection of poems exploring romantic love and longing.");

            addBook(bookRepository, existingBooks, "Devotions", "Mary Oliver", "Poetry", 26.99, 6, 4.8,
                    "Selected poems reflecting on nature and everyday life.");

            addBook(bookRepository, existingBooks, "The Essential Rumi", "Rumi", "Poetry", 23.99, 0, 4.7,
                    "A collection of spiritual and philosophical poetry.");

            // HORROR

            addBook(bookRepository, existingBooks, "The Shining", "Stephen King", "Horror", 18.00, 8, 4.5,
                    "A family spends the winter in an isolated hotel, where supernatural forces begin to threaten their lives.");

            // YOUNG ADULT

            addBook(bookRepository, existingBooks, "They Both Die at the End", "Adam Silvera", "Young Adult", 22.99, 12, 4.6,
                    "Two strangers discover they have only one day left to live and form an unexpected connection while making their final memories.");

        };
    }

    // ADD A BOOK ONLY IF IT DOES NOT ALREADY EXIST
    private void addBook(
            BookRepository bookRepository,
            Set<String> existingBooks,
            String title,
            String author,
            String genre,
            double price,
            int stockQuantity,
            double rating,
            String description) {

        // CHECK IF BOOK ALREADY EXISTS
        String bookKey = (title + "|" + author).toLowerCase(Locale.ROOT);

        if (existingBooks.contains(bookKey)) {
            return;
        }

        // CREATE BOOK
        Book book = new Book(
                title,
                author,
                genre,
                price,
                stockQuantity
        );

        // SET RATING AND DESCRIPTION
        book.setRating(rating);
        book.setDescription(description);

        // SAVE BOOK
        bookRepository.save(book);

        // RECORD BOOK TO PREVENT DUPLICATES
        existingBooks.add(bookKey);
    }
}