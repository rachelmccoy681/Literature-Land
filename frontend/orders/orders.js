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

        // UPDATE CART COUNTER

        if (typeof updateCartCount === "function") {
            updateCartCount();
        }

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

    const emptyMessage = document.getElementById("emptyOrders");

    // HIDE EMPTY MESSAGE WHILE LOADING

    if (emptyMessage) {
        emptyMessage.hidden = true;
    }

    ordersList.textContent = "Loading your orders...";

    try {

        // LOAD ORDER HISTORY FROM ORDER SERVICE

        const ordersResponse = await fetch(
            `${ORDER_API_URL}/customer/${CUSTOMER_ID}`
        );

        // CHECK ORDER SERVICE RESPONSE

        if (!ordersResponse.ok) {

            throw new Error(
                `Order Service returned ${ordersResponse.status}`
            );

        }

        // CONVERT RESPONSE TO JSON

        const orders = await ordersResponse.json();

        // CHECK ORDER DATA

        if (!Array.isArray(orders)) {

            throw new Error("Invalid order information.");

        }

        console.log("Orders loaded:", orders);

        // HANDLE CUSTOMER WITH NO ORDERS

        if (orders.length === 0) {

            console.log("Customer has no orders yet.");

            // CLEAR PREVIOUS ORDER DATA

            allOrders = [];

            allBooks = [];

            // DISPLAY EMPTY ORDER MESSAGE

            displayOrders();

            // STOP LOADING ADDITIONAL INFORMATION

            return;

        }

        // LOAD BOOK INFORMATION FROM STOCK SERVICE

        const booksResponse = await fetch(STOCK_API_URL);

        // CHECK STOCK SERVICE RESPONSE

        if (!booksResponse.ok) {

            throw new Error(
                `Stock Service returned ${booksResponse.status}`
            );

        }

        // GET BOOK DATA

        allBooks = await booksResponse.json();

        // CHECK BOOK DATA

        if (!Array.isArray(allBooks)) {

            throw new Error("Invalid book information.");

        }

        console.log("Book information loaded:", allBooks.length);

        // LOAD ITEMS FOR EACH ORDER

        allOrders = await Promise.all(

            orders.map(async function (order) {

                // REQUEST ITEMS BELONGING TO THIS ORDER

                const itemsResponse = await fetch(
                    `${ORDER_API_URL}/${order.id}/items`
                );

                // CHECK ORDER ITEMS RESPONSE

                if (!itemsResponse.ok) {

                    throw new Error(
                        `Could not load items for order ${order.id}. ` +
                        `Status: ${itemsResponse.status}`
                    );

                }

                // GET ORDER ITEMS

                const items = await itemsResponse.json();

                // CHECK ORDER ITEMS

                if (!Array.isArray(items)) {

                    throw new Error(
                        `Invalid items for order ${order.id}`
                    );

                }

                // RETURN ORDER WITH ITS ITEMS

                return {

                    ...order,

                    items: items

                };

            })

        );

        console.log("Orders with items:", allOrders);

        // DISPLAY ALL ORDERS

        displayOrders();

    } catch (error) {

        console.error("Orders error:", error);

        // HIDE EMPTY MESSAGE WHEN THERE IS AN ERROR

        if (emptyMessage) {
            emptyMessage.hidden = true;
        }

        // DISPLAY MORE ACCURATE ERROR MESSAGE

        ordersList.textContent =
            "Unable to load your orders. " +
            "Please check the browser console for details.";

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

    // CLEAR PREVIOUS ORDER CARDS

    ordersList.innerHTML = "";

    // FILTER ORDERS

    const filteredOrders = allOrders.filter(function (order) {

        // SHOW ALL ORDERS

        if (selectedFilter === "all") {
            return true;
        }

        // CHECK ORDER STATUS

        return String(order.status || "").toUpperCase() ===
            String(selectedFilter).toUpperCase();

    });

    // SHOW EMPTY MESSAGE WHEN THERE ARE NO ORDERS

    if (filteredOrders.length === 0) {

        if (emptyMessage) {

            emptyMessage.hidden = false;

        } else {

            // FALLBACK MESSAGE IF EMPTY ELEMENT IS MISSING

            const message = document.createElement("p");

            message.textContent =
                "You haven't placed any orders yet.";

            ordersList.appendChild(message);

        }

        return;

    }

    // HIDE EMPTY MESSAGE WHEN ORDERS EXIST

    if (emptyMessage) {
        emptyMessage.hidden = true;
    }

    // DISPLAY EACH ORDER

    filteredOrders.forEach(function (order) {

        // CREATE ORDER CARD

        const card = document.createElement("div");

        card.className = "order-card";

        // ORDER HEADER

        const header = document.createElement("div");

        header.className = "order-card-header";

        // ORDER NUMBER

        const orderNumber = document.createElement("div");

        orderNumber.className = "order-number";

        orderNumber.textContent = "Order #" + order.id;

        // ORDER STATUS

        const status = document.createElement("span");

        status.className = "order-status";

        const statusText = String(
            order.status || "PENDING"
        ).toUpperCase();

        status.textContent = statusText;

        // PROCESSING STATUS

        if (
            statusText === "PENDING" ||
            statusText === "PROCESSING"
        ) {

            status.classList.add("processing");

        }

        // DELIVERED STATUS

        if (statusText === "DELIVERED") {

            status.classList.add("delivered");

        }

        // CANCELLED STATUS

        if (statusText === "CANCELLED") {

            status.classList.add("cancelled");

        }

        // COMPLETE ORDER HEADER

        header.appendChild(orderNumber);

        header.appendChild(status);

        // ORDER BODY

        const body = document.createElement("div");

        body.className = "order-card-body";

        // BOOKS CONTAINER

        const booksContainer = document.createElement("div");

        booksContainer.className = "order-books";

        // DISPLAY BOOKS IN THIS ORDER

        order.items.forEach(function (item) {

            // FIND BOOK INFORMATION

            const book = findOrderBook(item.bookId);

            // CREATE BOOK ELEMENT

            const bookElement = document.createElement("div");

            bookElement.className = "order-book";

            // BOOK COVER

            const cover = document.createElement("div");

            cover.className = "order-book-cover";

            cover.textContent = "Book Cover";

            // BOOK TITLE

            const title = document.createElement("h3");

            title.textContent = book
                ? book.title
                : "Book #" + item.bookId;

            // BOOK AUTHOR

            const author = document.createElement("p");

            author.textContent = book
                ? book.author
                : "Unknown author";

            // BOOK QUANTITY

            const quantity = document.createElement("p");

            quantity.textContent = "Qty: " + item.quantity;

            // ADD BOOK INFORMATION

            bookElement.appendChild(cover);

            bookElement.appendChild(title);

            bookElement.appendChild(author);

            bookElement.appendChild(quantity);

            // ADD BOOK TO CONTAINER

            booksContainer.appendChild(bookElement);

        });

        // ORDER DETAILS

        const right = document.createElement("div");

        right.className = "order-card-right";

        // CALCULATE ESTIMATED TOTAL

        let total = 0;

        let pricesAvailable = true;

        order.items.forEach(function (item) {

            // FIND BOOK INFORMATION

            const book = findOrderBook(item.bookId);

            // CHECK WHETHER PRICE IS AVAILABLE

            if (
                !book ||
                !Number.isFinite(Number(book.price))
            ) {

                pricesAvailable = false;

                return;

            }

            // ADD BOOK PRICE MULTIPLIED BY QUANTITY

            total +=
                Number(book.price) *
                Number(item.quantity);

        });

        // DISPLAY TOTAL PRICE

        const totalElement = document.createElement("div");

        totalElement.className = "order-total";

        totalElement.textContent = pricesAvailable
            ? formatOrderPrice(total)
            : "Total unavailable";

        // PRICE INFORMATION

        const priceNote = document.createElement("p");

        priceNote.className = "order-delivery";

        priceNote.textContent =
            "Estimated from current book prices, excluding shipping.";

        // VIEW DETAILS BUTTON

        const detailsButton = document.createElement("button");

        detailsButton.className = "view-details-button";

        detailsButton.type = "button";

        detailsButton.textContent = "View Details";

        // OPEN ORDER DETAILS

        detailsButton.addEventListener("click", function () {

            showOrderDetails(order);

        });

        // COMPLETE RIGHT SECTION

        right.appendChild(totalElement);

        right.appendChild(priceNote);

        right.appendChild(detailsButton);

        // COMPLETE ORDER BODY

        body.appendChild(booksContainer);

        body.appendChild(right);

        // COMPLETE ORDER CARD

        card.appendChild(header);

        card.appendChild(body);

        // DISPLAY ORDER CARD

        ordersList.appendChild(card);

    });

}


// VIEW ORDER DETAILS

function showOrderDetails(order) {

    // CREATE ORDER DETAILS MESSAGE

    let message = "Order #" + order.id + "\n";

    message += "Status: " + order.status + "\n\n";

    message += "Books:\n";

    // ADD EACH BOOK TO MESSAGE

    order.items.forEach(function (item) {

        // FIND BOOK INFORMATION

        const book = findOrderBook(item.bookId);

        const title = book
            ? book.title
            : "Book #" + item.bookId;

        // ADD BOOK AND QUANTITY

        message +=
            title +
            " - Qty: " +
            item.quantity +
            "\n";

    });

    // SHOW ORDER DETAILS

    alert(message);

}


// FILTER ORDERS

document.getElementById("orderFilter").addEventListener(
    "change",
    function () {

        // UPDATE DISPLAY WHEN FILTER CHANGES

        displayOrders();

    }
);


// START PAGE

async function initializeOrdersPage() {

    // LOAD HEADER AND SIDEBAR

    await Promise.all([

        loadOrdersHeader(),

        loadOrdersSidebar()

    ]);

    // LOAD CUSTOMER ORDERS

    await loadOrders();

}


// INITIALISE ORDERS PAGE

initializeOrdersPage();