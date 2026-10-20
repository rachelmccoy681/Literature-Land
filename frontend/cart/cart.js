// BACKEND SETTINGS

const CART_API_URL = "http://localhost:8082/api/carts";
const STOCK_API_URL = "http://localhost:8081/books";
const CUSTOMER_ID = 1;

const FREE_SHIPPING_THRESHOLD = 50;
const SHIPPING_COST = 5;

let cartItems = [];
let allBooks = [];
let selectedBookIds = new Set();


// LOAD SHARED HEADER AND SIDEBAR

async function loadComponent(file, containerId) {
    try {
        const response = await fetch(file);

        if (!response.ok) {
            throw new Error(`Could not load ${file}`);
        }

        document.getElementById(containerId).innerHTML =
            await response.text();

    } catch (error) {
        console.error(error);
    }
}


// FORMAT PRICE

function formatPrice(price) {
    return "$" + Number(price).toFixed(2);
}


// LOAD CART FROM BACKEND

async function loadCart() {
    try {
        const response = await fetch(
            `${CART_API_URL}/${CUSTOMER_ID}`
        );

        if (response.status === 404) {
            cartItems = [];
            selectedBookIds.clear();

            displayCart();
            updateOrderSummary();
            await updateHeaderCartCount();
            return;
        }

        if (!response.ok) {
            throw new Error("Could not load cart");
        }

        const cart = await response.json();

        // GET BOOK INFORMATION

        const booksResponse = await fetch(STOCK_API_URL);

        if (!booksResponse.ok) {
            throw new Error("Could not load books");
        }

        allBooks = await booksResponse.json();

        // COMBINE CART ITEMS WITH BOOK DETAILS

        cartItems = cart.items.map(item => {
            const book = allBooks.find(
                book => Number(book.id) === Number(item.bookId)
            );

            return {
                bookId: item.bookId,
                quantity: item.quantity,
                title: book?.title || "Book unavailable",
                author: book?.author || "Unknown author",
                price: Number(book?.price || 0),
                stockQuantity: Number(book?.stockQuantity || 0),
                available: Boolean(book)
            };
        });

        // SELECT ALL ON FIRST LOAD

        if (selectedBookIds.size === 0) {
            cartItems.forEach(item => {
                selectedBookIds.add(Number(item.bookId));
            });
        }

        // REMOVE SELECTIONS FOR DELETED BOOKS

        const currentIds = new Set(
            cartItems.map(item => Number(item.bookId))
        );

        selectedBookIds.forEach(id => {
            if (!currentIds.has(id)) {
                selectedBookIds.delete(id);
            }
        });

        displayCart();
        updateOrderSummary();
        await updateHeaderCartCount();

    } catch (error) {
        console.error("Cart error:", error);

        document.getElementById("cartItems").textContent =
            "Unable to load your cart. Please check that the backend services are running.";
    }
}


// DISPLAY CART ITEMS

function displayCart() {
    const container = document.getElementById("cartItems");
    container.innerHTML = "";

    const totalQuantity = cartItems.reduce(
        (total, item) => total + item.quantity, 0
    );

    document.getElementById("cartItemCount").textContent =
        `${totalQuantity} ${totalQuantity === 1 ? "item" : "items"} in your cart`;

    document.getElementById("clearCart").disabled =
        cartItems.length === 0;

    if (cartItems.length === 0) {
        container.innerHTML = `
            <div class="cart-empty">
                <p>Your shopping cart is empty.</p>
                <p>Browse our books to find something you'll love!</p>
            </div>
        `;

        updateSelectAll();
        return;
    }

    cartItems.forEach(item => {
        const selected = selectedBookIds.has(Number(item.bookId));

        let stockText = "In Stock";
        let stockClass = "";

        if (item.stockQuantity <= 0) {
            stockText = "Out of Stock";
            stockClass = "out-of-stock";
        } else if (item.stockQuantity <= 5) {
            stockText = "Low Stock";
            stockClass = "low-stock";
        }

        const row = document.createElement("div");
        row.className = "cart-item";

        row.innerHTML = `
            <div class="cart-item-info">

                <input
                    type="checkbox"
                    class="cart-item-checkbox"
                    data-id="${item.bookId}"
                    ${selected ? "checked" : ""}
                >

                <div class="cart-book-cover">
                    Book Cover
                </div>

                <div class="cart-item-details">
                    <h3>
                        <a
                            class="cart-book-link"
                            href="../book-details/book-details.html?id=${encodeURIComponent(item.bookId)}"
                        ></a>
                    </h3>

                    <p class="cart-book-author"></p>

                    <div class="cart-stock-status ${stockClass}">
                        <span class="stock-dot ${stockClass}"></span>
                        <span>${stockText}</span>
                    </div>
                </div>

            </div>

            <div class="cart-item-price">
                ${formatPrice(item.price)}
            </div>

            <div class="cart-quantity-controls">

                <button
                    class="decrease-cart-quantity"
                    data-id="${item.bookId}"
                    ${item.quantity <= 1 ? "disabled" : ""}
                >−</button>

                <input
                    type="number"
                    value="${item.quantity}"
                    readonly
                >

                <button
                    class="increase-cart-quantity"
                    data-id="${item.bookId}"
                    ${item.quantity >= item.stockQuantity ? "disabled" : ""}
                >+</button>

            </div>

            <div class="cart-item-total">
                ${formatPrice(item.price * item.quantity)}
            </div>

            <button
                class="remove-cart-item"
                data-id="${item.bookId}"
                aria-label="Remove book"
            >
                <img src="../assets/icons/trash-2.svg" alt="">
            </button>
        `;

        // DISPLAY TEXT SAFELY

        row.querySelector(".cart-book-link").textContent = item.title;
        row.querySelector(".cart-book-author").textContent = item.author;

        container.appendChild(row);
    });

    updateSelectAll();
}


// UPDATE SELECT ALL CHECKBOX

function updateSelectAll() {
    const checkbox = document.getElementById("selectAll");

    checkbox.checked =
        cartItems.length > 0 &&
        cartItems.every(item =>
            selectedBookIds.has(Number(item.bookId))
        );

    checkbox.indeterminate =
        selectedBookIds.size > 0 && !checkbox.checked;
}


// HANDLE SELECT ALL

document.getElementById("selectAll").addEventListener("change", event => {
    selectedBookIds.clear();

    if (event.target.checked) {
        cartItems.forEach(item => {
            selectedBookIds.add(Number(item.bookId));
        });
    }

    displayCart();
    updateOrderSummary();
});


// HANDLE CART BUTTONS

document.getElementById("cartItems").addEventListener("click", async event => {
    const button = event.target.closest("button[data-id]");

    if (!button) return;

    const bookId = Number(button.dataset.id);
    const item = cartItems.find(
        item => Number(item.bookId) === bookId
    );

    if (!item) return;

    if (button.classList.contains("increase-cart-quantity")) {
        await updateQuantity(bookId, item.quantity + 1);
    }

    if (button.classList.contains("decrease-cart-quantity")) {
        await updateQuantity(bookId, item.quantity - 1);
    }

    if (button.classList.contains("remove-cart-item")) {
        await removeBook(bookId);
    }
});


// HANDLE INDIVIDUAL CHECKBOXES

document.getElementById("cartItems").addEventListener("change", event => {
    if (!event.target.classList.contains("cart-item-checkbox")) {
        return;
    }

    const bookId = Number(event.target.dataset.id);

    if (event.target.checked) {
        selectedBookIds.add(bookId);
    } else {
        selectedBookIds.delete(bookId);
    }

    updateSelectAll();
    updateOrderSummary();
});


// UPDATE BOOK QUANTITY

async function updateQuantity(bookId, quantity) {
    try {
        const response = await fetch(
            `${CART_API_URL}/${CUSTOMER_ID}/items/${bookId}`,
            {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ quantity })
            }
        );

        if (!response.ok) {
            throw new Error("Could not update quantity");
        }

        await loadCart();

    } catch (error) {
        console.error(error);
        alert("Unable to update quantity.");
    }
}


// REMOVE BOOK

async function removeBook(bookId) {
    try {
        const response = await fetch(
            `${CART_API_URL}/${CUSTOMER_ID}/items/${bookId}`,
            { method: "DELETE" }
        );

        if (!response.ok) {
            throw new Error("Could not remove book");
        }

        selectedBookIds.delete(bookId);

        await loadCart();
        await loadRecommendedBooks();

    } catch (error) {
        console.error(error);
        alert("Unable to remove book.");
    }
}


// CLEAR CART

document.getElementById("clearCart").addEventListener("click", async () => {
    if (!cartItems.length) return;

    if (!confirm("Are you sure you want to clear your cart?")) {
        return;
    }

    try {
        const response = await fetch(
            `${CART_API_URL}/${CUSTOMER_ID}/items`,
            { method: "DELETE" }
        );

        if (!response.ok) {
            throw new Error("Could not clear cart");
        }

        selectedBookIds.clear();

        await loadCart();
        await loadRecommendedBooks();

    } catch (error) {
        console.error(error);
        alert("Unable to clear cart.");
    }
});


// UPDATE ORDER SUMMARY

function updateOrderSummary() {
    const selectedItems = cartItems.filter(item =>
        selectedBookIds.has(Number(item.bookId))
    );

    const quantity = selectedItems.reduce(
        (total, item) => total + item.quantity, 0
    );

    const subtotal = selectedItems.reduce(
        (total, item) => total + item.price * item.quantity, 0
    );

    const shipping =
        subtotal > 0 && subtotal < FREE_SHIPPING_THRESHOLD
            ? SHIPPING_COST
            : 0;

    const total = subtotal + shipping;

    document.getElementById("subtotalLabel").textContent =
        `Subtotal (${quantity} ${quantity === 1 ? "item" : "items"})`;

    document.getElementById("subtotalAmount").textContent =
        formatPrice(subtotal);

    document.getElementById("shippingAmount").textContent =
        formatPrice(shipping);

    document.getElementById("cartTotal").textContent =
        formatPrice(total);

    const unavailable = selectedItems.some(item =>
        !item.available || item.quantity > item.stockQuantity
    );

    document.getElementById("checkoutButton").disabled =
        selectedItems.length === 0 || unavailable;
}


// CHECKOUT BUTTON

document.getElementById("checkoutButton").addEventListener("click", () => {

    // GET SELECTED BOOKS
    const selectedItems = cartItems.filter(item =>
        selectedBookIds.has(Number(item.bookId))
    );

    // CHECK IF BOOKS ARE SELECTED
    if (selectedItems.length === 0) {
        alert("Please select at least one book to checkout.");
        return;
    }

    // CHECK STOCK AVAILABILITY
    const unavailable = selectedItems.some(item =>
        !item.available ||
        item.quantity > item.stockQuantity
    );

    if (unavailable) {
        alert("Some selected books are unavailable or do not have enough stock.");
        return;
    }

    // PREPARE CHECKOUT DETAILS
    const checkoutItems = selectedItems.map(item => ({
        bookId: item.bookId,
        title: item.title,
        price: item.price,
        quantity: item.quantity
    }));

    // SAVE CHECKOUT DETAILS TEMPORARILY
    sessionStorage.setItem(
        "literatureLandCheckout",
        JSON.stringify(checkoutItems)
    );

    // OPEN CHECKOUT PAGE
    window.location.href = "../checkout/checkout.html";

});


// LOAD RECOMMENDED BOOKS

async function loadRecommendedBooks() {
    try {
        const response = await fetch(STOCK_API_URL);

        if (!response.ok) {
            throw new Error("Could not load recommendations");
        }

        const books = await response.json();

        const cartIds = new Set(
            cartItems.map(item => Number(item.bookId))
        );

        const recommendations = books
            .filter(book => !cartIds.has(Number(book.id)))
            .slice(0, 5);

        const container = document.getElementById("recommendedBooks");
        container.innerHTML = "";

        recommendations.forEach(book => {
            const card = document.createElement("div");
            card.className = "cart-recommended-book-card";

            const rating = Math.max(
                0,
                Math.min(5, Math.round(Number(book.rating) || 0))
            );

            card.innerHTML = `
                <div class="cart-recommended-book-cover">
                    Book Cover
                </div>

                <h3 class="recommended-title"></h3>

                <p class="recommended-author"></p>

                <p class="cart-recommended-book-rating">
                    ${"★".repeat(rating)}${"☆".repeat(5 - rating)}
                </p>

                <p class="cart-recommended-book-price">
                    ${formatPrice(book.price)}
                </p>

                <button
                    class="recommended-add-cart"
                    ${Number(book.stockQuantity) <= 0 ? "disabled" : ""}
                >
                    Add to Cart
                </button>
            `;

            card.querySelector(".recommended-title").textContent =
                book.title;

            card.querySelector(".recommended-author").textContent =
                book.author;

            // OPEN BOOK DETAILS

            const openDetails = () => {
                window.location.href =
                    `../book-details/book-details.html?id=${encodeURIComponent(book.id)}`;
            };

            const cover = card.querySelector(".cart-recommended-book-cover");
            const title = card.querySelector(".recommended-title");

            cover.style.cursor = "pointer";
            title.style.cursor = "pointer";

            cover.addEventListener("click", openDetails);
            title.addEventListener("click", openDetails);

            // ADD RECOMMENDED BOOK TO CART

            card.querySelector(".recommended-add-cart")
                .addEventListener("click", async event => {
                    const button = event.currentTarget;
                    button.disabled = true;

                    try {
                        const addResponse = await fetch(
                            `${CART_API_URL}/${CUSTOMER_ID}/items`,
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

                        if (!addResponse.ok) {
                            throw new Error("Could not add book");
                        }

                        await loadCart();
                        await loadRecommendedBooks();

                    } catch (error) {
                        console.error(error);
                        alert("Unable to add book to cart.");
                        button.disabled = false;
                    }
                });

            container.appendChild(card);
        });

    } catch (error) {
        console.error(error);

        document.getElementById("recommendedBooks").textContent =
            "Unable to load recommendations.";
    }
}


// LOAD SHOPPING CART PAGE

async function initializeCartPage() {
    await loadComponent("../components/header.html", "header-container");
    await loadComponent("../components/sidebar.html", "sidebar-container");

    await loadCart();
    await loadRecommendedBooks();
}

initializeCartPage();