
// BACKEND SETTINGS

const TRENDING_STOCK_API = "http://localhost:8081/books";
const TRENDING_CART_API = "http://localhost:8082/api/carts";
const TRENDING_CUSTOMER_ID = 1;

const TRENDING_BOOKS_PER_PAGE = 12;

// PAGE DATA

let trendingBooks = [];
let filteredTrendingBooks = [];
let selectedTrendingGenre = "all";
let trendingCurrentPage = 1;

// LOAD HEADER

async function loadTrendingHeader() {

    try {

        const response = await fetch("../components/header.html");

        if (!response.ok) {
            throw new Error("Could not load header.");
        }

        document.getElementById("header-container").innerHTML =
            await response.text();

        // UPDATE CART COUNT

        if (typeof updateCartCount === "function") {
            await updateCartCount();
        }

    } catch (error) {

        console.error("Header error:", error);

    }

}

// LOAD SIDEBAR

async function loadTrendingSidebar() {

    try {

        const response = await fetch("../components/sidebar.html");

        if (!response.ok) {
            throw new Error("Could not load sidebar.");
        }

        document.getElementById("sidebar-container").innerHTML =
            await response.text();

        // HIGHLIGHT TRENDING NOW

        document.querySelectorAll("#sidebar-container a").forEach(
            function (link) {

                link.classList.remove("active");

                const href = link.getAttribute("href") || "";

                if (href.includes("trending")) {
                    link.classList.add("active");
                }

            }
        );

    } catch (error) {

        console.error("Sidebar error:", error);

    }

}

// FORMAT PRICE

function formatTrendingPrice(price) {

    return "$" + Number(price).toFixed(2);

}

// SHOW MESSAGE

function showTrendingMessage(message) {

    const element = document.getElementById("trendingMessage");

    element.textContent = message;
    element.hidden = false;

}

// HIDE MESSAGE

function hideTrendingMessage() {

    document.getElementById("trendingMessage").hidden = true;

}

// CREATE ICON

function createTrendingIcon(name) {

    const icon = document.createElement("i");

    icon.setAttribute("data-lucide", name);

    return icon;

}

// REFRESH SVG ICONS

function refreshTrendingIcons() {

    if (window.lucide) {
        window.lucide.createIcons();
    }

}

// LOAD BOOKS

async function loadTrendingBooks() {

    showTrendingMessage("Loading books...");

    try {

        const response = await fetch(TRENDING_STOCK_API);

        if (!response.ok) {
            throw new Error("Could not load books.");
        }

        const books = await response.json();

        if (!Array.isArray(books)) {
            throw new Error("Invalid book information.");
        }

        // USE ACTUAL STOCK SERVICE BOOKS

        trendingBooks = books.filter(function (book) {

            return book.id != null && book.title;

        });

        filterTrendingBooks();

    } catch (error) {

        console.error("Trending books error:", error);

        document.getElementById("trendingGrid").innerHTML = "";
        document.getElementById("trendingPagination").innerHTML = "";

        showTrendingMessage(
            "Could not load books. Please check that your Stock Service is running."
        );

    }

}

// FILTER BOOKS

function filterTrendingBooks() {

    if (selectedTrendingGenre === "all") {

        filteredTrendingBooks = [...trendingBooks];

    } else {

        filteredTrendingBooks = trendingBooks.filter(function (book) {

            const bookGenre = String(book.genre || "")
                .trim()
                .toLowerCase()
                .replace(/[\s-]/g, "");

            const selectedGenre = selectedTrendingGenre
                .trim()
                .toLowerCase()
                .replace(/[\s-]/g, "");

            return bookGenre === selectedGenre;

        });

    }

    trendingCurrentPage = 1;

    displayTrendingBooks();

}

// GET STOCK STATUS

function getTrendingStockStatus(quantity) {

    const stock = Number(quantity);

    if (!Number.isFinite(stock) || stock <= 0) {

        return {
            label: "Out of Stock",
            className: "out"
        };

    }

    if (stock <= 5) {

        return {
            label: "Low Stock",
            className: "low"
        };

    }

    return {
        label: "In Stock",
        className: ""
    };

}

// GET SAVED WISHLIST IDS

function getTrendingWishlist() {

    try {

        const stored = JSON.parse(
            localStorage.getItem("literatureLandWishlist") || "[]"
        );

        if (Array.isArray(stored)) {
            return stored.map(String);
        }

    } catch (error) {

        console.error("Wishlist error:", error);

    }

    return [];

}

// TOGGLE WISHLIST

function toggleTrendingWishlist(bookId) {

    const id = String(bookId);
    const wishlist = getTrendingWishlist();

    const updated = wishlist.includes(id)
        ? wishlist.filter(function (savedId) {
            return savedId !== id;
        })
        : [...wishlist, id];

    localStorage.setItem(
        "literatureLandWishlist",
        JSON.stringify(updated)
    );

    displayTrendingBooks();

}

// ADD BOOK TO CART

async function addTrendingBookToCart(book, button) {

    button.disabled = true;
    button.textContent = "Adding...";

    try {

        // CHECK CURRENT STOCK

        const response = await fetch(TRENDING_STOCK_API);

        if (!response.ok) {
            throw new Error("Could not check stock.");
        }

        const books = await response.json();

        const latestBook = books.find(function (item) {
            return Number(item.id) === Number(book.id);
        });

        if (!latestBook || Number(latestBook.stockQuantity) <= 0) {

            throw new Error("This book is currently out of stock.");

        }

        // SEND ITEM TO CART SERVICE
        // CONFIRM THIS ENDPOINT MATCHES YOUR CART CONTROLLER

        const cartResponse = await fetch(
            `${TRENDING_CART_API}/${TRENDING_CUSTOMER_ID}/items`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    bookId: Number(book.id),
                    quantity: 1
                })
            }
        );

        if (!cartResponse.ok) {

            throw new Error(
                "Could not add this book to your cart. Check the Cart Service endpoint."
            );

        }

        button.textContent = "Added!";

        // UPDATE SHARED CART COUNTER

        if (typeof updateCartCount === "function") {
            await updateCartCount();
        }

    } catch (error) {

        console.error("Add to cart error:", error);
        alert(error.message);

        button.textContent = "Add to Cart";

    } finally {

        button.disabled = false;

    }

}

// CREATE BOOK CARD

function createTrendingBookCard(book) {

    const card = document.createElement("article");

    card.className = "trending-card";

    // WISHLIST BUTTON

    const wishlistButton = document.createElement("button");

    wishlistButton.type = "button";
    wishlistButton.className = "trending-wishlist";

    const saved = getTrendingWishlist().includes(String(book.id));

    wishlistButton.classList.toggle("saved", saved);

    wishlistButton.setAttribute(
        "aria-label",
        saved ? "Remove from wishlist" : "Add to wishlist"
    );

    wishlistButton.appendChild(createTrendingIcon("heart"));

    wishlistButton.addEventListener("click", function () {

        toggleTrendingWishlist(book.id);

    });

    // BOOK COVER

    const cover = document.createElement("div");

    cover.className = "trending-cover";

    // USE COVER IF STOCK SERVICE PROVIDES ONE

    const coverUrl = book.coverUrl || book.imageUrl;

    if (coverUrl) {

        const image = document.createElement("img");

        image.src = coverUrl;
        image.alt = "Cover of " + book.title;

        image.onerror = function () {

            cover.innerHTML = "";
            cover.appendChild(createTrendingIcon("book-open"));

            refreshTrendingIcons();

        };

        cover.appendChild(image);

    } else {

        cover.appendChild(createTrendingIcon("book-open"));

    }

    // BOOK TITLE

    const title = document.createElement("h3");

    title.className = "trending-title";
    title.textContent = book.title;

    // BOOK AUTHOR

    const author = document.createElement("p");

    author.className = "trending-author";
    author.textContent = book.author || "Unknown author";

    // RATING

    const rating = document.createElement("div");

    rating.className = "trending-rating";

    if (
        book.rating != null &&
        Number.isFinite(Number(book.rating))
    ) {

        const ratingValue = Math.max(
            0,
            Math.min(5, Number(book.rating))
        );

        const filledStars = Math.round(ratingValue);

        const stars = document.createElement("span");

        stars.className = "trending-stars";

        stars.textContent =
            "★".repeat(filledStars) +
            "☆".repeat(5 - filledStars);

        rating.appendChild(stars);

        const ratingNumber = document.createElement("span");

        ratingNumber.textContent = ratingValue.toFixed(1) + "/5";

        rating.appendChild(ratingNumber);

    } else {

        rating.textContent = "No ratings yet";

    }

    // BOOK PRICE

    const price = document.createElement("p");

    price.className = "trending-price";

    price.textContent = formatTrendingPrice(book.price || 0);

    // STOCK STATUS

    const stockStatus = getTrendingStockStatus(book.stockQuantity);

    const stock = document.createElement("div");

    stock.className = "trending-stock";

    if (stockStatus.className) {
        stock.classList.add(stockStatus.className);
    }

    const stockDot = document.createElement("span");

    stockDot.className = "trending-stock-dot";

    const stockText = document.createElement("span");

    stockText.textContent = stockStatus.label;

    stock.appendChild(stockDot);
    stock.appendChild(stockText);

    // ADD TO CART BUTTON

    const cartButton = document.createElement("button");

    cartButton.type = "button";
    cartButton.className = "trending-add-cart";

    cartButton.textContent = stockStatus.className === "out"
        ? "Out of Stock"
        : "Add to Cart";

    cartButton.disabled = stockStatus.className === "out";

    cartButton.addEventListener("click", function () {

        addTrendingBookToCart(book, cartButton);

    });

    // ADD ELEMENTS TO CARD

    card.appendChild(wishlistButton);
    card.appendChild(cover);
    card.appendChild(title);
    card.appendChild(author);
    card.appendChild(rating);
    card.appendChild(price);
    card.appendChild(stock);
    card.appendChild(cartButton);

    return card;

}

// DISPLAY BOOKS

function displayTrendingBooks() {

    const grid = document.getElementById("trendingGrid");

    grid.innerHTML = "";

    if (filteredTrendingBooks.length === 0) {

        showTrendingMessage("No books found in this category.");

        document.getElementById("trendingPagination").innerHTML = "";

        return;

    }

    hideTrendingMessage();

    // GET BOOKS FOR CURRENT PAGE

    const startIndex =
        (trendingCurrentPage - 1) * TRENDING_BOOKS_PER_PAGE;

    const endIndex = startIndex + TRENDING_BOOKS_PER_PAGE;

    const pageBooks = filteredTrendingBooks.slice(
        startIndex,
        endIndex
    );

    // CREATE CARDS

    pageBooks.forEach(function (book) {

        grid.appendChild(createTrendingBookCard(book));

    });

    // UPDATE SVG ICONS

    refreshTrendingIcons();

    // UPDATE PAGINATION

    displayTrendingPagination();

}

// CREATE PAGINATION BUTTON

function createTrendingPageButton(label, page, disabled, active) {

    const button = document.createElement("button");

    button.type = "button";
    button.textContent = label;

    button.disabled = disabled;

    if (active) {
        button.classList.add("active");
        button.setAttribute("aria-current", "page");
    }

    button.addEventListener("click", function () {

        trendingCurrentPage = page;

        displayTrendingBooks();

        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });

    });

    return button;

}

// DISPLAY PAGINATION

function displayTrendingPagination() {

    const pagination = document.getElementById(
        "trendingPagination"
    );

    pagination.innerHTML = "";

    const totalPages = Math.ceil(
        filteredTrendingBooks.length / TRENDING_BOOKS_PER_PAGE
    );

    if (totalPages <= 1) {
        return;
    }

    // PREVIOUS BUTTON

    pagination.appendChild(
        createTrendingPageButton(
            "‹",
            trendingCurrentPage - 1,
            trendingCurrentPage === 1,
            false
        )
    );

    // PAGE NUMBERS

    for (let page = 1; page <= totalPages; page++) {

        pagination.appendChild(
            createTrendingPageButton(
                String(page),
                page,
                false,
                page === trendingCurrentPage
            )
        );

    }

    // NEXT BUTTON

    pagination.appendChild(
        createTrendingPageButton(
            "›",
            trendingCurrentPage + 1,
            trendingCurrentPage === totalPages,
            false
        )
    );

}

// SET ACTIVE FILTER

function setTrendingGenre(genre) {

    selectedTrendingGenre = genre;

    document.querySelectorAll(".trending-filter").forEach(
        function (button) {

            button.classList.toggle(
                "active",
                button.dataset.genre === genre
            );

        }
    );

    const moreGenres = document.getElementById(
        "trendingMoreGenres"
    );

    const isMoreGenre = Array.from(moreGenres.options).some(
        function (option) {

            return option.value === genre && genre !== "";

        }
    );

    moreGenres.classList.toggle("active", isMoreGenre);

    if (!isMoreGenre) {
        moreGenres.value = "";
    }

    filterTrendingBooks();

}

// FILTER BUTTON EVENTS

document.querySelectorAll(".trending-filter").forEach(
    function (button) {

        button.addEventListener("click", function () {

            setTrendingGenre(button.dataset.genre);

        });

    }
);

// MORE GENRES EVENT

document.getElementById("trendingMoreGenres").addEventListener(
    "change",
    function () {

        if (this.value) {
            setTrendingGenre(this.value);
        }

    }
);

// START PAGE

async function initializeTrendingPage() {

    await Promise.all([
        loadTrendingHeader(),
        loadTrendingSidebar()
    ]);

    await loadTrendingBooks();

}

initializeTrendingPage();