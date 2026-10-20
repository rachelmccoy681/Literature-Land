// BACKEND API

const HOME_BOOKS_API = "http://localhost:8081/books";


// STORE BOOKS

let homeBooks = [];


// LOAD HEADER

async function loadHeader() {
    try {
        const response = await fetch("../components/header.html");

        if (!response.ok) {
            throw new Error("Could not load header");
        }

        const headerHTML = await response.text();

        document.getElementById("header-container").innerHTML = headerHTML;

        // CONNECT SEARCH BAR

        setupHomeSearch();

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


// CONNECT HOME SEARCH TO BROWSE BOOKS

function setupHomeSearch() {

    const searchForm = document.getElementById("searchForm");
    const searchInput = document.getElementById("searchInput");

    if (!searchForm || !searchInput) {
        return;
    }

    searchForm.addEventListener("submit", function (event) {

        event.preventDefault();

        const keyword = searchInput.value.trim();

        const url = new URL("../browse/browse.html", window.location.href);

        if (keyword) {
            url.searchParams.set("keyword", keyword);
        }

        window.location.href = url.toString();
    });
}


// GET BOOK DETAILS LINK

function getHomeBookLink(bookId) {

    return `../book-details/book-details.html?id=${encodeURIComponent(bookId)}`;
}


// GET BOOK RATING STARS

function getHomeBookStars(rating) {

    const roundedRating = Math.max(
        0,
        Math.min(5, Math.round(Number(rating) || 0))
    );

    return "★".repeat(roundedRating) +
        "☆".repeat(5 - roundedRating);
}


// GET BOOK STOCK STATUS

function getHomeBookStock(book) {

    const quantity = Number(book.stockQuantity);

    if (quantity <= 0) {
        return {
            className: "out-of-stock",
            text: "● Out Of Stock"
        };
    }

    if (quantity <= 5) {
        return {
            className: "low-stock",
            text: "● Low Stock"
        };
    }

    return {
        className: "in-stock",
        text: "● In Stock"
    };
}


// CREATE BOOK CARD

function createHomeBookCard(book, recommended = false) {

    const card = document.createElement("div");

    card.className = recommended
        ? "recommended-card"
        : "book-card";

    // BOOK COVER

    const cover = document.createElement("div");

    cover.className = recommended
        ? "recommended-cover"
        : "book-cover";

    cover.textContent = "Book Cover";

    // BOOK TITLE

    const title = document.createElement("h3");
    title.textContent = book.title;

    // BOOK AUTHOR

    const author = document.createElement("p");

    author.className = recommended
        ? ""
        : "book-author";

    author.textContent = book.author || "Unknown author";

    // BOOK RATING

    const rating = document.createElement("p");

    rating.className = recommended
        ? "recommended-rating"
        : "book-rating";

    rating.textContent = getHomeBookStars(book.rating);

    if (!recommended) {

        const ratingValue = document.createElement("span");

        ratingValue.textContent =
            ` (${Number(book.rating || 0).toFixed(1)})`;

        rating.appendChild(ratingValue);
    }

    // BOOK PRICE

    const price = document.createElement("p");

    price.className = recommended
        ? "recommended-price"
        : "book-price";

    price.textContent = `$${Number(book.price).toFixed(2)}`;

    // ADD BOOK INFORMATION

    card.appendChild(cover);
    card.appendChild(title);
    card.appendChild(author);
    card.appendChild(rating);
    card.appendChild(price);

    // SHOW STOCK STATUS FOR TRENDING BOOKS

    if (!recommended) {

        const stock = getHomeBookStock(book);

        const stockElement = document.createElement("p");

        stockElement.className =
            `book-stock ${stock.className}`;

        stockElement.textContent = stock.text;

        card.appendChild(stockElement);
    }

    // OPEN BOOK DETAILS WHEN CLICKED

    card.style.cursor = "pointer";

    card.addEventListener("click", function () {

        window.location.href = getHomeBookLink(book.id);
    });

    return card;
}


// DISPLAY TRENDING BOOKS

function displayHomeTrendingBooks() {

    const container = document.getElementById("trendingBooks");

    if (!container) {
        return;
    }

    container.innerHTML = "";

    // TEMPORARY DISPLAY UNTIL REAL-TIME TRENDING IS CONNECTED

    const trending = [...homeBooks]
        .sort(function (a, b) {
            return Number(b.rating || 0) - Number(a.rating || 0);
        })
        .slice(0, 12);

    trending.forEach(function (book) {

        container.appendChild(createHomeBookCard(book));
    });
}


// DISPLAY RECOMMENDED BOOKS

function displayHomeRecommendedBooks() {

    const container = document.querySelector(".recommended-books");

    if (!container) {
        return;
    }

    container.innerHTML = "";

    // TEMPORARY RECOMMENDATIONS BASED ON BOOK RATINGS

    const recommended = [...homeBooks]
        .filter(function (book) {
            return Number(book.stockQuantity) > 0;
        })
        .sort(function (a, b) {
            return Number(b.rating || 0) - Number(a.rating || 0);
        })
        .slice(0, 3);

    recommended.forEach(function (book) {

        container.appendChild(createHomeBookCard(book, true));
    });
}


// CONNECT CATEGORY BUTTONS

function setupHomeCategories() {

    const categoryLinks = document.querySelectorAll(
        ".category-list .category-card"
    );

    categoryLinks.forEach(function (link) {

        const category = link.textContent.trim();

        // OPEN ALL BOOKS FOR MORE

        if (category.toLowerCase() === "more") {

            link.href = "../browse/browse.html";
            return;
        }

        // OPEN BROWSE PAGE WITH SELECTED GENRE

        link.href =
            `../browse/browse.html?genre=${encodeURIComponent(category)}`;
    });
}


// CONNECT NEW RELEASES BUTTON

function setupHomeNewReleases() {

    const newReleasesButton = document.querySelector(
        ".new-releases-section .feature-button"
    );

    if (!newReleasesButton) {
        return;
    }

    // SHOW NEWEST ADDED BOOKS IN BROWSE PAGE

    newReleasesButton.href =
        "../browse/browse.html?sort=newest";
}


// LOAD BOOKS FROM STOCK SERVICE

async function loadHomeBooks() {

    try {

        const response = await fetch(HOME_BOOKS_API);

        if (!response.ok) {
            throw new Error("Could not load books from Stock Service");
        }

        homeBooks = await response.json();

        if (!Array.isArray(homeBooks)) {
            throw new Error("Invalid book data received");
        }

        // DISPLAY BOOKS

        displayHomeTrendingBooks();
        displayHomeRecommendedBooks();

        console.log("Home books loaded:", homeBooks.length);

    } catch (error) {

        console.error("Error loading Home page books:", error);

        const trending = document.getElementById("trendingBooks");

        if (trending) {
            trending.textContent =
                "Unable to load books. Please check that Stock Service is running.";
        }

        const recommended = document.querySelector(".recommended-books");

        if (recommended) {
            recommended.textContent =
                "Unable to load recommendations.";
        }
    }
}


// TRENDING BOOK NAVIGATION

const trendingBooks = document.getElementById("trendingBooks");

const previousBooks = document.getElementById("previousBooks");
const nextBooks = document.getElementById("nextBooks");


// PREVIOUS BOOKS

if (previousBooks && trendingBooks) {

    previousBooks.addEventListener("click", function () {

        trendingBooks.scrollBy({
            left: -200,
            behavior: "smooth"
        });
    });
}


// NEXT BOOKS

if (nextBooks && trendingBooks) {

    nextBooks.addEventListener("click", function () {

        trendingBooks.scrollBy({
            left: 200,
            behavior: "smooth"
        });
    });
}


// START HOME PAGE

loadHeader();
loadSidebar();

setupHomeCategories();
setupHomeNewReleases();

loadHomeBooks();