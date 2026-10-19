// LOAD HEADER

async function loadHeader() {
    try {
        const response = await fetch("../components/header.html");

        if (!response.ok) {
            throw new Error("Could not load header");
        }

        document.getElementById("header-container").innerHTML =
            await response.text();

    } catch (error) {
        console.error("Error loading header:", error);
    }
}

// START HEADER

loadHeader();


// LOAD SIDEBAR

async function loadSidebar() {
    try {
        const response = await fetch("../components/sidebar.html");

        if (!response.ok) {
            throw new Error("Could not load sidebar");
        }

        document.getElementById("sidebar-container").innerHTML =
            await response.text();

    } catch (error) {
        console.error("Error loading sidebar:", error);
    }
}

// START SIDEBAR

loadSidebar();


// BACKEND API ADDRESS

const API_URL = "http://localhost:8081/books";


// STORE CURRENT BOOK INFORMATION

let currentBook = null;


// GET BOOK ID FROM PAGE URL

const urlParams = new URLSearchParams(window.location.search);

const bookId = urlParams.get("id");


// LOAD BOOK DETAILS FROM BACKEND

async function loadBookDetails() {

    // CHECK IF BOOK ID EXISTS

    if (!bookId) {
        showBookError("No book selected. Please choose a book from Browse Books.");
        return;
    }

    try {

        // GET BOOK DETAILS FROM STOCK SERVICE

        const response = await fetch(`${API_URL}/${encodeURIComponent(bookId)}`);

        if (!response.ok) {
            throw new Error("Could not find this book");
        }

        currentBook = await response.json();

        // DISPLAY BOOK INFORMATION

        displayBookDetails(currentBook);

        // LOAD RECOMMENDED BOOKS

        loadRecommendedBooks();

        console.log("Book loaded from backend:", currentBook);

    } catch (error) {

        console.error("Error loading book details:", error);

        showBookError("Unable to load book details. Please check that the Stock Service is running.");
    }
}


// DISPLAY ERROR MESSAGE

function showBookError(message) {

    document.getElementById("bookTitle").textContent = "Book unavailable";

    document.getElementById("bookDescription").textContent = message;

    document.getElementById("stockStatus").textContent = "Unavailable";

    document.getElementById("addToCart").disabled = true;
    document.getElementById("addToWishlist").disabled = true;
}


// DISPLAY BOOK DETAILS

function displayBookDetails(book) {

    // BOOK TITLE AND AUTHOR

    document.getElementById("bookTitle").textContent = book.title;
    document.getElementById("bookAuthor").textContent = book.author;

    // BREADCRUMB

    document.getElementById("breadcrumbGenre").textContent = book.genre;
    document.getElementById("breadcrumbTitle").textContent = book.title;

    // BOOK RATING

    const rating = Number(book.rating) || 0;
    const roundedRating = Math.max(0, Math.min(5, Math.round(rating)));

    const fullStars = "★".repeat(roundedRating);
    const emptyStars = "☆".repeat(5 - roundedRating);

    document.getElementById("bookStars").textContent =
        fullStars + emptyStars;

    document.getElementById("bookRating").textContent =
        rating.toFixed(1);

    // BOOK PRICE

    document.getElementById("bookPrice").textContent =
        "$" + Number(book.price).toFixed(2);

    // BOOK GENRE

    document.getElementById("bookGenre").textContent =
        book.genre || "Not available";

    // BOOK DESCRIPTION

    document.getElementById("bookDescription").textContent =
        book.description || "No description available for this book.";

    // BOOK DETAILS TAB

    document.getElementById("detailTitle").textContent = book.title;
    document.getElementById("detailAuthor").textContent = book.author;
    document.getElementById("detailGenre").textContent = book.genre;

    document.getElementById("detailPrice").textContent =
        "$" + Number(book.price).toFixed(2);

    // STOCK AVAILABILITY

    const stock = Number(book.stockQuantity) || 0;
    const inStock = stock > 0;

    document.getElementById("bookAvailability").textContent =
        inStock ? "In Stock" : "Out of Stock";

    document.getElementById("stockStatus").textContent =
        inStock ? "In Stock" : "Out of Stock";

    document.getElementById("stockQuantity").textContent =
        inStock ? `${stock} copies available` : "Currently unavailable";

    // DISABLE PURCHASE IF OUT OF STOCK

    document.getElementById("addToCart").disabled = !inStock;
    document.getElementById("increaseQuantity").disabled = !inStock;
    document.getElementById("decreaseQuantity").disabled = !inStock;

    // RESET QUANTITY

    document.getElementById("quantityInput").value = 1;

    // SHOW BOOK COVER PLACEHOLDER

    document.getElementById("bookCover").textContent = "Book Cover";
}


// QUANTITY SELECTOR

const quantityInput = document.getElementById("quantityInput");


// DECREASE QUANTITY

document.getElementById("decreaseQuantity").addEventListener("click", function () {

    if (!currentBook) {
        return;
    }

    const quantity = Number(quantityInput.value);

    if (quantity > 1) {
        quantityInput.value = quantity - 1;
    }
});


// INCREASE QUANTITY

document.getElementById("increaseQuantity").addEventListener("click", function () {

    if (!currentBook) {
        return;
    }

    const quantity = Number(quantityInput.value);
    const stock = Number(currentBook.stockQuantity);

    if (quantity < stock) {
        quantityInput.value = quantity + 1;
    }
});


// BOOK INFORMATION TABS

const tabButtons = document.querySelectorAll(".tab-button");

tabButtons.forEach(function (button) {

    button.addEventListener("click", function () {

        // REMOVE ACTIVE CLASS FROM ALL TABS

        tabButtons.forEach(function (tab) {
            tab.classList.remove("active");
        });

        document.querySelectorAll(".tab-content").forEach(function (content) {
            content.classList.remove("active");
        });

        // SHOW SELECTED TAB

        button.classList.add("active");

        const selectedTab = button.getAttribute("data-tab");

        document.getElementById(selectedTab).classList.add("active");
    });
});


// LOAD RECOMMENDED BOOKS FROM BACKEND

async function loadRecommendedBooks() {

    try {

        // GET ALL BOOKS FROM STOCK SERVICE

        const response = await fetch(API_URL);

        if (!response.ok) {
            throw new Error("Could not load recommended books");
        }

        const books = await response.json();

        // FIND OTHER BOOKS IN THE SAME GENRE

        const sameGenreBooks = books.filter(function (book) {
            return book.id !== currentBook.id &&
                book.genre === currentBook.genre;
        });

        // FIND OTHER BOOKS IF THERE ARE NOT ENOUGH IN THE SAME GENRE

        const otherBooks = books.filter(function (book) {
            return book.id !== currentBook.id &&
                book.genre !== currentBook.genre;
        });

        // DISPLAY UP TO FIVE BOOKS

        const recommendedBooks = [
            ...sameGenreBooks,
            ...otherBooks
        ].slice(0, 5);

        displayRecommendedBooks(recommendedBooks);

    } catch (error) {

        console.error("Error loading recommended books:", error);

        document.getElementById("recommendedBooks").textContent =
            "Unable to load recommendations.";
    }
}


// DISPLAY RECOMMENDED BOOKS

function displayRecommendedBooks(books) {

    const container = document.getElementById("recommendedBooks");

    container.innerHTML = "";

    if (books.length === 0) {
        container.textContent = "No other books available.";
        return;
    }

    books.forEach(function (book) {

        const card = document.createElement("div");
        card.className = "recommended-book-card";

        // BOOK COVER PLACEHOLDER

        const cover = document.createElement("div");
        cover.className = "recommended-book-cover";
        cover.textContent = "Book Cover";

        // BOOK TITLE

        const title = document.createElement("h3");
        title.textContent = book.title;

        // BOOK AUTHOR

        const author = document.createElement("p");
        author.textContent = book.author;

        // BOOK PRICE

        const price = document.createElement("p");
        price.className = "recommended-book-price";
        price.textContent = "$" + Number(book.price).toFixed(2);

        // OPEN BOOK DETAILS WHEN CLICKED

        card.style.cursor = "pointer";

        card.addEventListener("click", function () {
            window.location.href =
                `book-details.html?id=${encodeURIComponent(book.id)}`;
        });

        card.appendChild(cover);
        card.appendChild(title);
        card.appendChild(author);
        card.appendChild(price);

        container.appendChild(card);
    });
}


// CART SERVICE ADDRESS

const CART_API_URL = "http://localhost:8082/api/carts";


// TEMPORARY CUSTOMER ID FOR TESTING

const CUSTOMER_ID = 1;


// ADD TO CART BUTTON

document.getElementById("addToCart").addEventListener("click", async function () {

    // CHECK IF BOOK IS AVAILABLE

    if (!currentBook || Number(currentBook.stockQuantity) <= 0) {
        return;
    }

    // GET SELECTED BOOK AND QUANTITY

    const bookId = currentBook.id;
    const quantity = Number(document.getElementById("quantityInput").value);

    const addToCartButton = document.getElementById("addToCart");

    try {

        // PREVENT MULTIPLE CLICKS WHILE REQUEST IS RUNNING

        addToCartButton.disabled = true;

        // SEND BOOK TO CART SERVICE

        const response = await fetch(
            `${CART_API_URL}/${CUSTOMER_ID}/items`,
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    bookId: bookId,
                    quantity: quantity
                })
            }
        );

        // CHECK IF REQUEST WAS SUCCESSFUL

        if (!response.ok) {
            throw new Error(`Cart Service returned ${response.status}`);
        }

        // GET UPDATED CART

        const cart = await response.json();

        console.log("Updated cart:", cart);

        // UPDATE HEADER CART COUNT
        window.dispatchEvent(new Event("cart:updated"));

        alert("Book added to cart successfully!");

    } catch (error) {

        console.error("Error adding book to cart:", error);

        alert("Unable to add book to cart. Please check the Cart Service.");

    } finally {

        // ENABLE BUTTON AGAIN IF BOOK IS IN STOCK

        addToCartButton.disabled = Number(currentBook.stockQuantity) <= 0;
    }
});


// ADD TO WISHLIST BUTTON

document.getElementById("addToWishlist").addEventListener("click", function () {

    if (!currentBook) {
        return;
    }

    alert("Wishlist functionality will be added later.");
});


// LOAD BOOK DETAILS WHEN PAGE OPENS

loadBookDetails();