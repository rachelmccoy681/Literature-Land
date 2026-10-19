
// BACKEND SETTINGS

const ORDER_API_URL = "http://localhost:8083/orders";
const STOCK_API_URL = "http://localhost:8081/books";
const CUSTOMER_ID = 1;

// ORDER DATA

let allOrders = [];
let allBooks = [];

// LOAD HEADER

async function loadOrdersHeader() {

    try {

        const response = await fetch("../components/header.html");

        if (!response.ok) {
            throw new Error("Could not load header.");
        }

        document.getElementById("header-container").innerHTML =
            await response.text();

    } catch (error) {

        console.error("Header error:", error);

    }

}

// LOAD SIDEBAR

async function loadOrdersSidebar() {

    try {

        const response = await fetch("../components/sidebar.html");

        if (!response.ok) {
            throw new Error("Could not load sidebar.");
        }

        document.getElementById("sidebar-container").innerHTML =
            await response.text();

        // HIGHLIGHT MY ORDERS

        const links = document.querySelectorAll(
            "#sidebar-container .sidebar-link"
        );

        links.forEach(function (link) {

            link.classList.remove("active");

            if (link.getAttribute("href")?.includes("orders")) {
                link.classList.add("active");
            }

        });

    } catch (error) {

        console.error("Sidebar error:", error);

    }

}

// FORMAT PRICE

function formatOrderPrice(price) {

    return "$" + Number(price).toFixed(2);

}

// LOAD ORDERS

async function loadOrders() {

    const ordersList = document.getElementById("ordersList");

    ordersList.textContent = "Loading your orders...";

    try {

        // LOAD ORDER HISTORY

        const ordersResponse = await fetch(
            `${ORDER_API_URL}/customer/${CUSTOMER_ID}`
        );

        if (!ordersResponse.ok) {
            throw new Error("Could not load your orders.");
        }

        const orders = await ordersResponse.json();

        if (!Array.isArray(orders)) {
            throw new Error("Invalid order information.");
        }

        // LOAD BOOK INFORMATION

        const booksResponse = await fetch(STOCK_API_URL);

        if (!booksResponse.ok) {
            throw new Error("Could not load book information.");
        }

        allBooks = await booksResponse.json();

        // LOAD ITEMS FOR EACH ORDER

        allOrders = await Promise.all(

            orders.map(async function (order) {

                const itemsResponse = await fetch(
                    `${ORDER_API_URL}/${order.id}/items`
                );

                if (!itemsResponse.ok) {
                    throw new Error(
                        "Could not load items for order " + order.id
                    );
                }

                const items = await itemsResponse.json();

                return {
                    ...order,
                    items: items
                };

            })

        );

        // SHOW ORDERS

        displayOrders();

    } catch (error) {

        console.error("Orders error:", error);

        ordersList.textContent =
            "Could not load orders. Please check that your Order Service is running.";

    }

}

// FIND BOOK DETAILS

function findOrderBook(bookId) {

    return allBooks.find(function (book) {

        return Number(book.id) === Number(bookId);

    });

}

// DISPLAY ORDERS

function displayOrders() {

    const ordersList = document.getElementById("ordersList");
    const emptyMessage = document.getElementById("emptyOrders");

    const selectedFilter =
        document.getElementById("orderFilter").value;

    ordersList.innerHTML = "";

    // FILTER ORDERS

    const filteredOrders = allOrders.filter(function (order) {

        if (selectedFilter === "all") {
            return true;
        }

        return order.status === selectedFilter;

    });

    // SHOW EMPTY MESSAGE

    if (filteredOrders.length === 0) {

        emptyMessage.hidden = false;
        return;

    }

    emptyMessage.hidden = true;

    // DISPLAY EACH ORDER

    filteredOrders.forEach(function (order) {

        const card = document.createElement("div");
        card.className = "order-card";

        // ORDER HEADER

        const header = document.createElement("div");
        header.className = "order-card-header";

        const orderNumber = document.createElement("div");
        orderNumber.className = "order-number";
        orderNumber.textContent = "Order #" + order.id;

        const status = document.createElement("span");
        status.className = "order-status";

        const statusText = String(order.status || "PENDING");

        status.textContent = statusText;

        if (statusText === "PENDING" || statusText === "PROCESSING") {
            status.classList.add("processing");
        }

        if (statusText === "DELIVERED") {
            status.classList.add("delivered");
        }

        if (statusText === "CANCELLED") {
            status.classList.add("cancelled");
        }

        header.appendChild(orderNumber);
        header.appendChild(status);

        // ORDER BODY

        const body = document.createElement("div");
        body.className = "order-card-body";

        // BOOKS

        const booksContainer = document.createElement("div");
        booksContainer.className = "order-books";

        order.items.forEach(function (item) {

            const book = findOrderBook(item.bookId);

            const bookElement = document.createElement("div");
            bookElement.className = "order-book";

            const cover = document.createElement("div");
            cover.className = "order-book-cover";
            cover.textContent = "Book Cover";

            const title = document.createElement("h3");
            title.textContent = book ? book.title : "Book #" + item.bookId;

            const author = document.createElement("p");
            author.textContent = book ? book.author : "Unknown author";

            const quantity = document.createElement("p");
            quantity.textContent = "Qty: " + item.quantity;

            bookElement.appendChild(cover);
            bookElement.appendChild(title);
            bookElement.appendChild(author);
            bookElement.appendChild(quantity);

            booksContainer.appendChild(bookElement);

        });

        // ORDER DETAILS

        const right = document.createElement("div");
        right.className = "order-card-right";

        // CALCULATE CURRENT CATALOGUE TOTAL

        let total = 0;
        let pricesAvailable = true;

        order.items.forEach(function (item) {

            const book = findOrderBook(item.bookId);

            if (!book || !Number.isFinite(Number(book.price))) {
                pricesAvailable = false;
                return;
            }

            total += Number(book.price) * Number(item.quantity);

        });

        const totalElement = document.createElement("div");
        totalElement.className = "order-total";

        totalElement.textContent = pricesAvailable
            ? formatOrderPrice(total)
            : "Total unavailable";

        const priceNote = document.createElement("p");
        priceNote.className = "order-delivery";
        priceNote.textContent =
            "Estimated from current book prices, excluding shipping.";

        // VIEW DETAILS BUTTON

        const detailsButton = document.createElement("button");
        detailsButton.className = "view-details-button";
        detailsButton.textContent = "View Details";

        detailsButton.addEventListener("click", function () {

            showOrderDetails(order);

        });

        right.appendChild(totalElement);
        right.appendChild(priceNote);
        right.appendChild(detailsButton);

        // COMPLETE ORDER CARD

        body.appendChild(booksContainer);
        body.appendChild(right);

        card.appendChild(header);
        card.appendChild(body);

        ordersList.appendChild(card);

    });

}

// VIEW ORDER DETAILS

function showOrderDetails(order) {

    let message = "Order #" + order.id + "\n";
    message += "Status: " + order.status + "\n\n";
    message += "Books:\n";

    order.items.forEach(function (item) {

        const book = findOrderBook(item.bookId);

        const title = book ? book.title : "Book #" + item.bookId;

        message += title + " - Qty: " + item.quantity + "\n";

    });

    alert(message);

}

// FILTER ORDERS

document.getElementById("orderFilter").addEventListener(
    "change",
    function () {

        displayOrders();

    }
);

// START PAGE

async function initializeOrdersPage() {

    await Promise.all([
        loadOrdersHeader(),
        loadOrdersSidebar()
    ]);

    await loadOrders();

}

initializeOrdersPage();