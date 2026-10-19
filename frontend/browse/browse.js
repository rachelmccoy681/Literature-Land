// LOAD HEADER

async function loadHeader() {
    try {
        const response = await fetch("../components/header.html");

        if (!response.ok) {
            throw new Error("Could not load header");
        }

        const headerHTML = await response.text();

        document.getElementById("header-container").innerHTML = headerHTML;

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

        const sidebarHTML = await response.text();

        document.getElementById("sidebar-container").innerHTML = sidebarHTML;

    } catch (error) {
        console.error("Error loading sidebar:", error);
    }
}

// START SIDEBAR
loadSidebar();


// BACKEND API ADDRESS

const API_URL = "http://localhost:8081/books";


// STORE BOOKS FROM BACKEND

let books = [];


// PAGINATION SETTINGS

let currentPage = 1;
const booksPerPage = 9;
let filteredBooks = [];


// LOAD BOOKS FROM STOCK SERVICE

async function loadBooks() {

    try {
        const response = await fetch(API_URL);

        if (!response.ok) {
            throw new Error("Could not load books from backend");
        }

        // GET BOOK DATA FROM BACKEND

        books = await response.json();

        // UPDATE BOOK LIST

        filteredBooks = [...books];

        currentPage = 1;

        // DISPLAY BOOKS

        sortBooks();

        console.log("Books loaded from backend:", books);

    } catch (error) {

        console.error("Error loading books:", error);

        document.getElementById("booksGrid").textContent =
            "Unable to load books. Please check that the Stock Service is running.";
    }
}


// DISPLAY BOOKS

function displayBooks() {

    const booksGrid = document.getElementById("booksGrid");

    booksGrid.innerHTML = "";

    const startIndex = (currentPage - 1) * booksPerPage;
    const endIndex = startIndex + booksPerPage;

    const visibleBooks = filteredBooks.slice(startIndex, endIndex);

    visibleBooks.forEach(function (book) {

        // DISPLAY BOOK RATINGS

        const roundedRating = Math.max(0, Math.min(5, Math.round(book.rating)));

        const fullStars = "★".repeat(roundedRating);
        const emptyStars = "☆".repeat(5 - roundedRating);

        const bookCard = document.createElement("div");
        bookCard.className = "browse-book-card";

        bookCard.innerHTML = `
            <!-- WISHLIST -->
            <button class="wishlist-button" type="button" aria-label="Add ${book.title} to wishlist">
                <img src="../assets/icons/heart.svg" alt="">
            </button>

            <!-- BOOK COVER -->
            <div class="browse-book-cover">
                Book Cover
            </div>

            <!-- BOOK DETAILS -->
            <h3>${book.title}</h3>

            <p class="browse-book-author">${book.author}</p>

            <p class="browse-book-rating">
                ${fullStars}${emptyStars}
                <span>(${book.rating.toFixed(1)})</span>
            </p>

            <p class="browse-book-price">
                $${book.price.toFixed(2)}
            </p>
        `;

        // OPEN BOOK DETAILS WHEN BOOK CARD IS CLICKED

        bookCard.style.cursor = "pointer";

        bookCard.addEventListener("click", function (event) {

            // DO NOT OPEN BOOK DETAILS WHEN WISHLIST BUTTON IS CLICKED

            if (event.target.closest(".wishlist-button")) {
                return;
            }

            // OPEN SELECTED BOOK USING ITS BACKEND ID

            window.location.href =
                `../book-details/book-details.html?id=${encodeURIComponent(book.id)}`;
        });

        booksGrid.appendChild(bookCard);
    });

    // UPDATE RESULTS INFORMATION

    const startResult = filteredBooks.length === 0 ? 0 : startIndex + 1;
    const endResult = Math.min(endIndex, filteredBooks.length);

    document.getElementById("resultsInfo").textContent =
        filteredBooks.length === 0
            ? "Showing 0 results"
            : `Showing ${startResult}-${endResult} of ${filteredBooks.length} results`;

    document.getElementById("resultsCount").textContent =
        `${filteredBooks.length} books found`;

    updatePagination();
}


// APPLY FILTERS

document.getElementById("applyFilters").addEventListener("click", function () {

    // GET SELECTED GENRES

    const selectedGenres = Array.from(
        document.querySelectorAll(".genre-options input:checked")
    ).map(function (input) {
        return input.value;
    });

    // GET AUTHOR

    const author = document.getElementById("authorFilter").value.trim().toLowerCase();

    // GET MAXIMUM PRICE

    const maxPrice = Number(document.getElementById("priceFilter").value);

    // GET SELECTED RATING

    const selectedRating = document.querySelector(
        '.rating-options input[name="rating"]:checked'
    );

    const minRating = selectedRating ? Number(selectedRating.value) : 0;

    // FILTER BOOKS

    filteredBooks = books.filter(function (book) {

        const matchesGenre =
            selectedGenres.length === 0 || selectedGenres.includes(book.genre);

        const matchesAuthor =
            book.author.toLowerCase().includes(author);

        const matchesPrice =
            book.price <= maxPrice;

        const matchesRating =
            book.rating >= minRating;

        return matchesGenre && matchesAuthor && matchesPrice && matchesRating;
    });

    currentPage = 1;

    sortBooks();
});


// SORT BOOKS

function sortBooks() {

    const sortValue = document.getElementById("sortBooks").value;

    if (sortValue === "price-low") {
        filteredBooks.sort(function (a, b) {
            return a.price - b.price;
        });
    }

    if (sortValue === "price-high") {
        filteredBooks.sort(function (a, b) {
            return b.price - a.price;
        });
    }

    if (sortValue === "rating") {
        filteredBooks.sort(function (a, b) {
            return b.rating - a.rating;
        });
    }

    if (sortValue === "newest") {
        filteredBooks.sort(function (a, b) {
            return b.id - a.id;
        });
    }

    if (sortValue === "relevance") {
        filteredBooks.sort(function (a, b) {
            return a.id - b.id;
        });
    }

    displayBooks();
}


// SORT DROPDOWN

document.getElementById("sortBooks").addEventListener("change", function () {

    currentPage = 1;

    sortBooks();
});


// UPDATE PAGINATION

function updatePagination() {

    const totalPages = Math.max(1, Math.ceil(filteredBooks.length / booksPerPage));

    const pagination = document.querySelector(".pagination");

    pagination.innerHTML = "";

    // PREVIOUS BUTTON

    const previousButton = document.createElement("button");
    previousButton.textContent = "‹";
    previousButton.disabled = currentPage === 1;

    previousButton.addEventListener("click", function () {
        currentPage--;
        displayBooks();
    });

    pagination.appendChild(previousButton);

    // PAGE NUMBERS

    for (let page = 1; page <= totalPages; page++) {

        const pageButton = document.createElement("button");

        pageButton.textContent = page;

        if (page === currentPage) {
            pageButton.classList.add("active");
        }

        pageButton.addEventListener("click", function () {
            currentPage = page;
            displayBooks();
        });

        pagination.appendChild(pageButton);
    }

    // NEXT BUTTON

    const nextButton = document.createElement("button");
    nextButton.textContent = "›";
    nextButton.disabled = currentPage === totalPages;

    nextButton.addEventListener("click", function () {
        currentPage++;
        displayBooks();
    });

    pagination.appendChild(nextButton);
}


// CLEAR FILTERS

document.getElementById("clearFilters").addEventListener("click", function () {

    // CLEAR GENRE CHECKBOXES

    document.querySelectorAll(".genre-options input").forEach(function (input) {
        input.checked = false;
    });

    // CLEAR AUTHOR

    document.getElementById("authorFilter").value = "";

    // RESET PRICE

    document.getElementById("priceFilter").value = 100;

    // CLEAR RATING

    document.querySelectorAll(".rating-options input").forEach(function (input) {
        input.checked = false;
    });

    // RESET SORTING

    document.getElementById("sortBooks").value = "relevance";

    filteredBooks = [...books];
    currentPage = 1;

    // UPDATE PRICE LABEL

    document.getElementById("priceValue").textContent = "$100";

    displayBooks();
});


// UPDATE PRICE LABEL

document.getElementById("priceFilter").addEventListener("input", function () {

    document.getElementById("priceValue").textContent =
        "$" + this.value;
});


// LOAD BOOKS FROM BACKEND WHEN PAGE OPENS

loadBooks();