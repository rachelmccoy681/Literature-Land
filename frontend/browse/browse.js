// LOAD HEADER

async function loadHeader() {
    try {
        const response = await fetch("../components/header.html");

        if (!response.ok) {
            throw new Error("Could not load header");
        }

        const headerHTML = await response.text();

        document.getElementById("header-container").innerHTML = headerHTML;

        // UPDATE CART COUNTER

        if (typeof updateCartCount === "function") {
            updateCartCount();
        }

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
let filteredBooks = [];


// PAGINATION SETTINGS

let currentPage = 1;
const booksPerPage = 9;


// GET CATEGORY FROM URL

function getSelectedCategory() {

    const urlParams = new URLSearchParams(window.location.search);

    return urlParams.get("genre");
}


// SELECT CATEGORY FILTER

function selectCategoryFilter() {

    const selectedCategory = getSelectedCategory();

    if (!selectedCategory) {
        return false;
    }

    let categoryFound = false;

    document.querySelectorAll(".genre-options input").forEach(function (input) {

        if (
            input.value.trim().toLowerCase() ===
            selectedCategory.trim().toLowerCase()
        ) {

            input.checked = true;
            categoryFound = true;

        } else {

            input.checked = false;

        }

    });

    return categoryFound;
}


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

        // SELECT CATEGORY FROM URL

        const categorySelected = selectCategoryFilter();

        // APPLY CATEGORY FILTER

        if (categorySelected) {

            applyBookFilters();

        } else {

            sortBooks();

        }

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

        const rating = Number(book.rating || 0);

        const roundedRating = Math.max(
            0,
            Math.min(5, Math.round(rating))
        );

        const fullStars = "★".repeat(roundedRating);
        const emptyStars = "☆".repeat(5 - roundedRating);

        const bookCard = document.createElement("div");
        bookCard.className = "browse-book-card";

        bookCard.innerHTML = `
            <!-- WISHLIST -->
            <button
                class="wishlist-button"
                type="button"
                aria-label="Add ${book.title} to wishlist"
            >
                <img src="../assets/icons/heart.svg" alt="">
            </button>

            <!-- BOOK COVER -->
            <div class="browse-book-cover">
                Book Cover
            </div>

            <!-- BOOK DETAILS -->
            <h3>${book.title}</h3>

            <p class="browse-book-author">
                ${book.author}
            </p>

            <p class="browse-book-rating">
                ${fullStars}${emptyStars}
                <span>(${rating.toFixed(1)})</span>
            </p>

            <p class="browse-book-price">
                $${Number(book.price).toFixed(2)}
            </p>
        `;

        // OPEN BOOK DETAILS WHEN BOOK CARD IS CLICKED

        bookCard.style.cursor = "pointer";

        bookCard.addEventListener("click", function (event) {

            // DO NOT OPEN DETAILS WHEN WISHLIST IS CLICKED

            if (event.target.closest(".wishlist-button")) {
                return;
            }

            // OPEN SELECTED BOOK

            window.location.href =
                `../book-details/book-details.html?id=${encodeURIComponent(book.id)}`;
        });

        booksGrid.appendChild(bookCard);
    });

    // UPDATE RESULTS INFORMATION

    const startResult =
        filteredBooks.length === 0 ? 0 : startIndex + 1;

    const endResult =
        Math.min(endIndex, filteredBooks.length);

    document.getElementById("resultsInfo").textContent =
        filteredBooks.length === 0
            ? "Showing 0 results"
            : `Showing ${startResult}-${endResult} of ${filteredBooks.length} results`;

    document.getElementById("resultsCount").textContent =
        `${filteredBooks.length} books found`;

    // UPDATE PAGINATION

    updatePagination();
}


// APPLY BOOK FILTERS

function applyBookFilters() {

    // GET SELECTED GENRES

    const selectedGenres = Array.from(
        document.querySelectorAll(".genre-options input:checked")
    ).map(function (input) {

        return input.value.trim().toLowerCase();

    });

    // GET AUTHOR

    const author = document.getElementById("authorFilter")
        .value.trim().toLowerCase();

    // GET MAXIMUM PRICE

    const maxPrice = Number(
        document.getElementById("priceFilter").value
    );

    // GET SELECTED RATING

    const selectedRating = document.querySelector(
        '.rating-options input[name="rating"]:checked'
    );

    const minRating = selectedRating
        ? Number(selectedRating.value)
        : 0;

    // FILTER BOOKS

    filteredBooks = books.filter(function (book) {

        // CHECK GENRE

        const matchesGenre =
            selectedGenres.length === 0 ||
            selectedGenres.includes(
                String(book.genre || "").trim().toLowerCase()
            );

        // CHECK AUTHOR

        const matchesAuthor =
            String(book.author || "")
                .toLowerCase()
                .includes(author);

        // CHECK PRICE

        const matchesPrice =
            Number(book.price) <= maxPrice;

        // CHECK RATING

        const matchesRating =
            Number(book.rating || 0) >= minRating;

        return matchesGenre &&
            matchesAuthor &&
            matchesPrice &&
            matchesRating;

    });

    currentPage = 1;

    // SORT AND DISPLAY FILTERED BOOKS

    sortBooks();
}


// APPLY FILTERS BUTTON

document.getElementById("applyFilters").addEventListener(
    "click",
    function () {

        applyBookFilters();

    }
);


// SORT BOOKS

function sortBooks() {

    const sortValue = document.getElementById("sortBooks").value;

    // PRICE LOW TO HIGH

    if (sortValue === "price-low") {

        filteredBooks.sort(function (a, b) {
            return a.price - b.price;
        });

    }

    // PRICE HIGH TO LOW

    if (sortValue === "price-high") {

        filteredBooks.sort(function (a, b) {
            return b.price - a.price;
        });

    }

    // HIGHEST RATING

    if (sortValue === "rating") {

        filteredBooks.sort(function (a, b) {
            return b.rating - a.rating;
        });

    }

    // NEWEST BOOKS

    if (sortValue === "newest") {

        filteredBooks.sort(function (a, b) {
            return b.id - a.id;
        });

    }

    // RELEVANCE

    if (sortValue === "relevance") {

        filteredBooks.sort(function (a, b) {
            return a.id - b.id;
        });

    }

    displayBooks();
}


// SORT DROPDOWN

document.getElementById("sortBooks").addEventListener(
    "change",
    function () {

        currentPage = 1;

        sortBooks();

    }
);


// UPDATE PAGINATION

function updatePagination() {

    const totalPages = Math.max(
        1,
        Math.ceil(filteredBooks.length / booksPerPage)
    );

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

document.getElementById("clearFilters").addEventListener(
    "click",
    function () {

        // CLEAR GENRE CHECKBOXES

        document.querySelectorAll(".genre-options input").forEach(
            function (input) {

                input.checked = false;

            }
        );

        // CLEAR AUTHOR

        document.getElementById("authorFilter").value = "";

        // RESET PRICE

        document.getElementById("priceFilter").value = 100;

        // CLEAR RATING

        document.querySelectorAll(".rating-options input").forEach(
            function (input) {

                input.checked = false;

            }
        );

        // RESET SORTING

        document.getElementById("sortBooks").value = "relevance";

        // REMOVE SELECTED CATEGORY FROM URL

        const url = new URL(window.location.href);

        url.searchParams.delete("genre");

        window.history.replaceState(
            {},
            "",
            url.pathname + url.search + url.hash
        );

        // RESET BOOK LIST

        filteredBooks = [...books];

        currentPage = 1;

        // UPDATE PRICE LABEL

        document.getElementById("priceValue").textContent = "$100";

        // DISPLAY ALL BOOKS

        sortBooks();

    }
);


// UPDATE PRICE LABEL

document.getElementById("priceFilter").addEventListener(
    "input",
    function () {

        document.getElementById("priceValue").textContent =
            "$" + this.value;

    }
);


// LOAD BOOKS FROM BACKEND WHEN PAGE OPENS

loadBooks();