
// BACKEND SETTINGS

const WISHLIST_STOCK_API_URL = "http://localhost:8081/books";
const WISHLIST_CART_API_URL = "http://localhost:8082/api/carts";

// TEMPORARY CUSTOMER ID

const WISHLIST_CUSTOMER_ID = 1;

// LOCAL WISHLIST STORAGE

const WISHLIST_STORAGE_KEY = "literatureLandWishlist";

// BOOK DATA

let wishlistBooks = [];
let allWishlistBooks = [];

// LOAD HEADER

async function loadWishlistHeader() {
    try {
        const response = await fetch("../components/header.html");

        if (!response.ok) {
            throw new Error("Could not load header");
        }

        document.getElementById("header-container").innerHTML =
            await response.text();

        updateWishlistHeaderCount();

    } catch (error) {
        console.error("Error loading header:", error);
    }
}

// LOAD SIDEBAR

async function loadWishlistSidebar() {
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

// GET SAVED WISHLIST IDS

function getWishlistIds() {
    try {
        const saved = JSON.parse(
            localStorage.getItem(WISHLIST_STORAGE_KEY) || "[]"
        );

        if (!Array.isArray(saved)) {
            return [];
        }

        return saved
            .map(Number)
            .filter(function (id) {
                return Number.isInteger(id) && id > 0;
            });

    } catch (error) {
        console.error("Error reading wishlist:", error);
        return [];
    }
}

// SAVE WISHLIST IDS

function saveWishlistIds(ids) {
    const uniqueIds = [...new Set(ids)];

    localStorage.setItem(
        WISHLIST_STORAGE_KEY,
        JSON.stringify(uniqueIds)
    );

    updateWishlistHeaderCount();

    window.dispatchEvent(new Event("wishlist:updated"));
}

// UPDATE WISHLIST HEADER COUNT

function updateWishlistHeaderCount() {
    const wishlistLink = document.querySelector(
        '.header-actions a[href*="wishlist"]'
    );

    if (!wishlistLink) {
        return;
    }

    const count = getWishlistIds().length;

    let countElement = wishlistLink.querySelector(".wishlist-count");

    if (!countElement) {
        countElement = document.createElement("span");
        countElement.className = "wishlist-count";

        const label = wishlistLink.querySelector("span");

        if (label) {
            label.appendChild(countElement);
        } else {
            wishlistLink.appendChild(countElement);
        }
    }

    countElement.textContent = ` (${count})`;
}

// FORMAT PRICE

function formatWishlistPrice(price) {
    return "$" + Number(price).toFixed(2);
}

// GET BOOK COVER

function getWishlistBookCover(book) {
    return book.coverImageUrl ||
        book.coverUrl ||
        book.imageUrl ||
        book.image ||
        null;
}

// CREATE BOOK COVER

function createWishlistBookCover(book, className) {
    const cover = document.createElement("div");
    cover.className = className;

    const imageUrl = getWishlistBookCover(book);

    if (imageUrl) {
        const image = document.createElement("img");

        image.src = imageUrl;
        image.alt = `Cover of ${book.title}`;

        image.onerror = function () {
            cover.textContent = "Book Cover";
        };

        cover.appendChild(image);

    } else {
        cover.textContent = "Book Cover";
    }

    return cover;
}

// GET STOCK STATUS

function getWishlistStockStatus(book) {
    const quantity = Number(book.stockQuantity);

    if (quantity <= 0) {
        return {
            className: "out-of-stock",
            label: "Out of Stock"
        };
    }

    if (quantity <= 5) {
        return {
            className: "low-stock",
            label: "Low Stock"
        };
    }

    return {
        className: "in-stock",
        label: "In Stock"
    };
}

// CREATE STOCK INDICATOR

function createWishlistStockStatus(book) {
    const status = getWishlistStockStatus(book);

    const container = document.createElement("div");
    container.className = `stock-status ${status.className}`;

    const dot = document.createElement("span");
    dot.className = "stock-dot";

    const label = document.createElement("span");
    label.textContent = status.label;

    container.appendChild(dot);
    container.appendChild(label);

    return container;
}

// GET BOOK DETAILS LINK

function getWishlistBookLink(bookId) {
    return `../book-details/book-details.html?id=${encodeURIComponent(bookId)}`;
}

// REMOVE BOOK FROM WISHLIST

function removeWishlistBook(bookId) {
    const updatedIds = getWishlistIds().filter(function (id) {
        return id !== Number(bookId);
    });

    saveWishlistIds(updatedIds);

    displayWishlistBooks();
    displayWishlistRecommendations();
}

// ADD BOOK TO WISHLIST

function addWishlistBook(bookId) {
    const ids = getWishlistIds();

    if (ids.includes(Number(bookId))) {
        return;
    }

    ids.push(Number(bookId));

    saveWishlistIds(ids);

    displayWishlistBooks();
    displayWishlistRecommendations();
}

// ADD BOOK TO CART

async function addWishlistBookToCart(book, button) {
    if (Number(book.stockQuantity) <= 0) {
        alert("This book is currently out of stock.");
        return;
    }

    button.disabled = true;
    button.textContent = "Adding...";

    try {
        const response = await fetch(
            `${WISHLIST_CART_API_URL}/${WISHLIST_CUSTOMER_ID}/items`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    bookId: book.id,
                    quantity: 1
                })
            }
        );

        if (!response.ok) {
            throw new Error("Could not add book to cart");
        }

        window.dispatchEvent(new Event("cart:updated"));

        alert(`${book.title} was added to your cart.`);

    } catch (error) {
        console.error("Error adding book to cart:", error);

        alert("Could not add the book to your cart. Please try again.");

    } finally {
        button.disabled = false;
        button.textContent = "Add to Cart";
    }
}

// CREATE WISHLIST ITEM

function createWishlistItem(book) {
    const item = document.createElement("div");
    item.className = "wishlist-item";

    // HEART BUTTON

    const heart = document.createElement("button");
    heart.type = "button";
    heart.className = "wishlist-heart";
    heart.textContent = "♡";
    heart.title = "Remove from wishlist";

    heart.addEventListener("click", function () {
        removeWishlistBook(book.id);
    });

    // BOOK COVER

    const cover = createWishlistBookCover(
        book,
        "wishlist-book-cover"
    );

    // BOOK INFORMATION

    const info = document.createElement("div");
    info.className = "wishlist-book-info";

    const title = document.createElement("h3");

    const titleLink = document.createElement("a");
    titleLink.href = getWishlistBookLink(book.id);
    titleLink.textContent = book.title;

    title.appendChild(titleLink);

    const author = document.createElement("p");
    author.textContent = book.author || "Unknown author";

    info.appendChild(title);
    info.appendChild(author);
    info.appendChild(createWishlistStockStatus(book));

    // BOOK ACTIONS

    const actions = document.createElement("div");
    actions.className = "wishlist-book-actions";

    const price = document.createElement("span");
    price.className = "wishlist-book-price";
    price.textContent = formatWishlistPrice(book.price);

    const addButton = document.createElement("button");
    addButton.type = "button";
    addButton.className = "add-to-cart-button";
    addButton.textContent = "Add to Cart";
    addButton.disabled = Number(book.stockQuantity) <= 0;

    addButton.addEventListener("click", function () {
        addWishlistBookToCart(book, addButton);
    });

    const removeButton = document.createElement("button");
    removeButton.type = "button";
    removeButton.className = "remove-wishlist-button";
    removeButton.textContent = "🗑";
    removeButton.title = "Remove from wishlist";

    removeButton.addEventListener("click", function () {
        removeWishlistBook(book.id);
    });

    actions.appendChild(price);
    actions.appendChild(addButton);
    actions.appendChild(removeButton);

    item.appendChild(heart);
    item.appendChild(cover);
    item.appendChild(info);
    item.appendChild(actions);

    return item;
}

// DISPLAY WISHLIST BOOKS

function displayWishlistBooks() {
    const container = document.getElementById("wishlistItems");
    const emptyMessage = document.getElementById("emptyWishlist");

    container.innerHTML = "";

    const savedIds = getWishlistIds();

    wishlistBooks = savedIds
        .map(function (id) {
            return allWishlistBooks.find(function (book) {
                return Number(book.id) === id;
            });
        })
        .filter(Boolean);

    if (savedIds.length === 0) {
        emptyMessage.hidden = false;
        return;
    }

    emptyMessage.hidden = true;

    if (wishlistBooks.length === 0) {
        const message = document.createElement("p");
        message.textContent =
            "Your saved books could not be found in the catalogue.";
        message.style.padding = "20px";

        container.appendChild(message);
        return;
    }

    wishlistBooks.forEach(function (book) {
        container.appendChild(createWishlistItem(book));
    });
}

// CREATE RECOMMENDATION

function createWishlistRecommendation(book) {
    const item = document.createElement("div");
    item.className = "recommended-book";

    // HEART BUTTON

    const heart = document.createElement("button");
    heart.type = "button";
    heart.className = "recommended-heart";
    heart.textContent = "♡";
    heart.title = "Add to wishlist";

    heart.addEventListener("click", function () {
        addWishlistBook(book.id);
    });

    // BOOK COVER

    const cover = createWishlistBookCover(
        book,
        "recommended-book-cover"
    );

    // BOOK INFORMATION

    const info = document.createElement("div");
    info.className = "recommended-book-info";

    const title = document.createElement("h3");

    const link = document.createElement("a");
    link.href = getWishlistBookLink(book.id);
    link.textContent = book.title;

    title.appendChild(link);

    const author = document.createElement("p");
    author.textContent = book.author || "Unknown author";

    const price = document.createElement("div");
    price.className = "recommended-book-price";
    price.textContent = formatWishlistPrice(book.price);

    const addButton = document.createElement("button");
    addButton.type = "button";
    addButton.className = "add-to-cart-button";
    addButton.textContent = "Add to Cart";
    addButton.disabled = Number(book.stockQuantity) <= 0;

    addButton.addEventListener("click", function () {
        addWishlistBookToCart(book, addButton);
    });

    info.appendChild(title);
    info.appendChild(author);
    info.appendChild(price);
    info.appendChild(addButton);

    item.appendChild(heart);
    item.appendChild(cover);
    item.appendChild(info);

    return item;
}

// DISPLAY RECOMMENDATIONS

function displayWishlistRecommendations() {
    const container = document.getElementById("recommendedBooks");

    container.innerHTML = "";

    const savedIds = getWishlistIds();

    const recommendations = allWishlistBooks
        .filter(function (book) {
            return !savedIds.includes(Number(book.id));
        })
        .slice(0, 3);

    if (recommendations.length === 0) {
        const message = document.createElement("p");
        message.textContent = "No recommendations available.";

        container.appendChild(message);
        return;
    }

    recommendations.forEach(function (book) {
        container.appendChild(createWishlistRecommendation(book));
    });
}

// LOAD BOOKS FROM STOCK SERVICE

async function loadWishlistBooks() {
    const container = document.getElementById("wishlistItems");

    container.textContent = "Loading your wishlist...";

    try {
        const response = await fetch(WISHLIST_STOCK_API_URL);

        if (!response.ok) {
            throw new Error("Could not load books");
        }

        const books = await response.json();

        if (!Array.isArray(books)) {
            throw new Error("Invalid book data");
        }

        allWishlistBooks = books;

        displayWishlistBooks();
        displayWishlistRecommendations();

    } catch (error) {
        console.error("Error loading wishlist:", error);

        container.textContent =
            "Unable to load books. Please check that the Stock Service is running.";

        document.getElementById("emptyWishlist").hidden = true;
    }
}

// START WISHLIST PAGE

async function initializeWishlistPage() {
    await Promise.all([
        loadWishlistHeader(),
        loadWishlistSidebar()
    ]);

    await loadWishlistBooks();
}

initializeWishlistPage();