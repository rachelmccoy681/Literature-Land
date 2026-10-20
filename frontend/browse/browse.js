// BACKEND API ADDRESS

const API_URL = "http://localhost:8081/books";

// STORE BOOKS FROM BACKEND

let books = [];
let filteredBooks = [];

// PAGINATION SETTINGS

let currentPage = 1;
const booksPerPage = 9;


// GET SEARCH TERM FROM URL

function getSearchKeyword() {
    const urlParams = new URLSearchParams(window.location.search);

    return (
        urlParams.get("search") ||
        urlParams.get("keyword") ||
        ""
    ).trim();
}


// GET CATEGORY FROM URL

function getSelectedCategory() {
    const urlParams = new URLSearchParams(window.location.search);

    return urlParams.get("genre");
}


// LOAD HEADER

async function loadHeader() {
    try {
        const response = await fetch("../components/header.html");

        if (!response.ok) {
            throw new Error("Could not load header");
        }

        const headerHTML = await response.text();

        document.getElementById("header-container").innerHTML = headerHTML;

        // CONNECT HEADER SEARCH

        setupBookSearch();

        // UPDATE CART COUNTER

        if (typeof updateCartCount === "function") {
            updateCartCount();
        }

    } catch (error) {
        console.error("Error loading header:", error);
    }
}


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


// SET UP HEADER SEARCH

function setupBookSearch() {
    const searchForm = document.getElementById("searchForm");
    const searchInput = document.getElementById("searchInput");

    if (!searchInput) {
        return;
    }

    // KEEP EXISTING SEARCH TERM IN HEADER

    searchInput.value = getSearchKeyword();

    if (!searchForm) {
        return;
    }

    // SEARCH WHEN ENTER OR SEARCH BUTTON IS CLICKED

    searchForm.addEventListener("submit", function (event) {
        event.preventDefault();

        const searchKeyword = searchInput.value.trim();
        const url = new URL(window.location.href);

        // USE THE SEARCH PARAMETER EXPECTED BY THE BROWSE PAGE

        if (searchKeyword) {
            url.searchParams.set("search", searchKeyword);
        } else {
            url.searchParams.delete("search");
        }

        // REMOVE OLD KEYWORD PARAMETER

        url.searchParams.delete("keyword");

        // RELOAD PAGE WITH SEARCH KEYWORD

        window.location.href = url.toString();
    });
}


// UPDATE WISHLIST BUTTON

function updateWishlistButton(button, bookId) {
    if (!button) {
        return;
    }

    // CHECK SAVED STATUS USING SHARED WISHLIST MANAGER

    const isSaved = isSharedBookWishlisted(bookId);

    button.classList.toggle("active", isSaved);

    button.setAttribute("aria-pressed", String(isSaved));

    button.setAttribute(
        "aria-label",
        isSaved ? "Remove from wishlist" : "Add to wishlist"
    );

    button.title = isSaved
        ? "Remove from wishlist"
        : "Add to wishlist";

    button.dataset.wishlistId = bookId;

    // DISPLAY FILLED DARK BROWN HEART WHEN SAVED

    if (isSaved) {
        button.innerHTML = `
            <span
                aria-hidden="true"
                style="
                    color: #503629;
                    font-size: 25px;
                    line-height: 1;
                "
            >♥</span>
        `;
    } else {
        // DISPLAY ORIGINAL SVG HEART WHEN NOT SAVED

        button.innerHTML = `
            <img src="../assets/icons/heart.svg" alt="">
        `;
    }
}


// LOAD BOOKS FROM STOCK SERVICE

async function loadBooks() {
    try {
        // SEARCH THE STOCK SERVICE WHEN THERE IS A SEARCH TERM

        const keyword = getSearchKeyword();

        const url = keyword
            ? `${API_URL}/search?keyword=${encodeURIComponent(keyword)}`
            : API_URL;

        const response = await fetch(url);

        if (!response.ok) {
            throw new Error("Could not load books from backend");
        }

        // GET BOOK DATA FROM BACKEND

        books = await response.json();

        if (!Array.isArray(books)) {
            throw new Error("Invalid book data");
        }

        // SELECT CATEGORY FROM URL

        selectCategoryFilter();

        // APPLY SEARCH AND FILTERS

        applyBookFilters();

        console.log("Books loaded from backend:", books.length);

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

        // CREATE BOOK CARD

        const bookCard = document.createElement("div");

        bookCard.className = "browse-book-card";

        bookCard.innerHTML = `
            <button
                class="wishlist-button"
                type="button"
                aria-label="Add to wishlist"
            >
                <img src="../assets/icons/heart.svg" alt="">
            </button>

            <div class="browse-book-cover">
                Book Cover
            </div>

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

        // CONNECT WISHLIST BUTTON

        const wishlistButton = bookCard.querySelector(".wishlist-button");

        // SHOW SAVED OR UNSAVED HEART

        updateWishlistButton(wishlistButton, book.id);

        // ADD OR REMOVE BOOK FROM WISHLIST

        wishlistButton.addEventListener("click", function (event) {
            event.preventDefault();
            event.stopPropagation();

            // USE SHARED WISHLIST MANAGER

            toggleSharedWishlistBook(book.id);

            // UPDATE HEART APPEARANCE

            updateWishlistButton(wishlistButton, book.id);
        });

        // OPEN BOOK DETAILS WHEN BOOK CARD IS CLICKED

        bookCard.style.cursor = "pointer";

        bookCard.addEventListener("click", function (event) {
            // DO NOT OPEN DETAILS WHEN HEART IS CLICKED

            if (event.target.closest(".wishlist-button")) {
                return;
            }

            window.location.href =
                `../book-details/book-details.html?id=${encodeURIComponent(book.id)}`;
        });

        booksGrid.appendChild(bookCard);
    });

    // UPDATE RESULTS INFORMATION

    const startResult =
        filteredBooks.length === 0 ? 0 : startIndex + 1;

    const endResult = Math.min(endIndex, filteredBooks.length);

    document.getElementById("resultsInfo").textContent =
        filteredBooks.length === 0
            ? "Showing 0 results"
            : `Showing ${startResult}-${endResult} of ${filteredBooks.length} results`;

    const keyword = getSearchKeyword();

    document.getElementById("resultsCount").textContent = keyword
        ? `${filteredBooks.length} books found for "${keyword}"`
        : `${filteredBooks.length} books found`;

    // UPDATE PAGINATION

    updatePagination();
}


// APPLY BOOK FILTERS

function applyBookFilters() {
    // GET SEARCH KEYWORD FROM URL

    const keyword = getSearchKeyword().toLowerCase();

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
        // SEARCH IN TITLE, AUTHOR OR GENRE

        const matchesSearch =
            !keyword ||
            String(book.title || "").toLowerCase().includes(keyword) ||
            String(book.author || "").toLowerCase().includes(keyword) ||
            String(book.genre || "").toLowerCase().includes(keyword);

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

        return (
            matchesSearch &&
            matchesGenre &&
            matchesAuthor &&
            matchesPrice &&
            matchesRating
        );
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

        // UPDATE PRICE LABEL

        document.getElementById("priceValue").textContent = "$100";

        // CLEAR RATING

        document.querySelectorAll(".rating-options input").forEach(
            function (input) {
                input.checked = false;
            }
        );

        // RESET SORTING

        document.getElementById("sortBooks").value = "relevance";

        // CHECK WHETHER A BACKEND SEARCH WAS ACTIVE

        const hadSearch = getSearchKeyword() !== "";

        // REMOVE CATEGORY AND SEARCH FROM URL

        const url = new URL(window.location.href);

        url.searchParams.delete("genre");
        url.searchParams.delete("search");
        url.searchParams.delete("keyword");

        window.history.replaceState(
            {},
            "",
            url.pathname + url.search + url.hash
        );

        // CLEAR HEADER SEARCH INPUT

        const searchInput = document.getElementById("searchInput");

        if (searchInput) {
            searchInput.value = "";
        }

        // RELOAD ALL BOOKS IF THE BACKEND SEARCH WAS ACTIVE

        if (hadSearch) {
            loadBooks();
            return;
        }

        // RESET BOOK LIST

        filteredBooks = [...books];

        currentPage = 1;

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


// UPDATE HEARTS WHEN WISHLIST CHANGES

window.addEventListener("wishlist:updated", function () {
    document.querySelectorAll(".wishlist-button[data-wishlist-id]")
        .forEach(function (button) {
            updateWishlistButton(
                button,
                button.dataset.wishlistId
            );
        });
});


// START BROWSE PAGE

loadHeader();
loadSidebar();
loadBooks();