
// BACKEND SETTINGS

const CHECKOUT_CART_API_URL = "http://localhost:8082/api/carts";
const CHECKOUT_STOCK_API_URL = "http://localhost:8081/books";

// CHECK YOUR ORDER SERVICE PORT
const CHECKOUT_ORDER_API_URL = "http://localhost:8083/orders";

const CHECKOUT_CUSTOMER_ID = 1;

// SHIPPING SETTINGS

const FREE_SHIPPING_THRESHOLD = 50;
const STANDARD_SHIPPING_COST = 5;
const EXPRESS_SHIPPING_COST = 9.99;

// CHECKOUT DATA

let checkoutItems = [];
let checkoutSubtotal = 0;
let checkoutShipping = 0;
let checkoutTotal = 0;
let checkoutLoaded = false;
let currentCheckoutStep = 1;

let demoCardLastFour = "";
let demoCardExpiry = "";

let orderSubmitting = false;

// LOAD HEADER

async function loadCheckoutHeader() {

    try {

        const response = await fetch("../components/header.html");

        if (!response.ok) {
            throw new Error("Could not load header.");
        }

        document.getElementById("header-container").innerHTML =
            await response.text();

        // UPDATE SHARED CART COUNTER

        if (typeof updateCartCount === "function") {
            updateCartCount();
        }

    } catch (error) {

        console.error("Header error:", error);

    }

}

// LOAD SIDEBAR

async function loadCheckoutSidebar() {

    try {

        const response = await fetch("../components/sidebar.html");

        if (!response.ok) {
            throw new Error("Could not load sidebar.");
        }

        document.getElementById("sidebar-container").innerHTML =
            await response.text();

        // REMOVE HOME HIGHLIGHT

        document.querySelectorAll(
            "#sidebar-container .sidebar-link"
        ).forEach(function (link) {

            link.classList.remove("active");

        });

    } catch (error) {

        console.error("Sidebar error:", error);

    }

}

// FORMAT PRICE

function formatCheckoutPrice(price) {

    return "$" + Number(price).toFixed(2);

}

// GET SELECTED BOOK IDS

function getSelectedCheckoutIds() {

    const savedSelection = sessionStorage.getItem(
        "literatureLandCheckout"
    );

    if (!savedSelection) {

        throw new Error(
            "Please select books from your cart before checkout."
        );

    }

    let selectedItems;

    try {

        selectedItems = JSON.parse(savedSelection);

    } catch (error) {

        throw new Error("Invalid checkout selection.");

    }

    // CHECK SAVED ITEMS

    if (!Array.isArray(selectedItems) || selectedItems.length === 0) {

        throw new Error("No books selected for checkout.");

    }

    // SUPPORT OBJECTS SAVED BY CART.JS

    const selectedIds = selectedItems.map(function (item) {

        if (typeof item === "number") {
            return item;
        }

        return Number(item.bookId);

    });

    // CHECK BOOK IDS

    const validIds = selectedIds.every(function (id) {

        return Number.isInteger(id) && id > 0;

    });

    if (!validIds) {

        throw new Error("Invalid checkout selection.");

    }

    // REMOVE DUPLICATE IDS

    return [...new Set(selectedIds)];

}

// LOAD SELECTED CART ITEMS

async function loadCheckoutCart() {

    const container = document.getElementById("checkoutItems");

    checkoutLoaded = false;
    checkoutItems = [];

    container.textContent = "Loading your order...";

    updateCheckoutButtons();

    try {

        // GET SELECTED BOOK IDS

        const selectedIds = getSelectedCheckoutIds();

        // GET CART FROM BACKEND

        const cartResponse = await fetch(
            `${CHECKOUT_CART_API_URL}/${CHECKOUT_CUSTOMER_ID}`
        );

        if (!cartResponse.ok) {

            throw new Error("Could not load your cart.");

        }

        const cart = await cartResponse.json();

        if (!Array.isArray(cart.items)) {

            throw new Error("Invalid cart information.");

        }

        // ONLY GET SELECTED CART ITEMS

        const selectedCartItems = cart.items.filter(function (item) {

            return selectedIds.includes(Number(item.bookId));

        });

        // CHECK SELECTED BOOKS ARE STILL IN CART

        if (selectedCartItems.length !== selectedIds.length) {

            throw new Error(
                "Some selected books are no longer in your cart."
            );

        }

        // LOAD BOOK INFORMATION

        const booksResponse = await fetch(CHECKOUT_STOCK_API_URL);

        if (!booksResponse.ok) {

            throw new Error("Could not load book information.");

        }

        const books = await booksResponse.json();

        if (!Array.isArray(books)) {

            throw new Error("Invalid book information.");

        }

        // COMBINE CART AND BOOK DETAILS

        checkoutItems = selectedCartItems.map(function (item) {

            const book = books.find(function (book) {

                return Number(book.id) === Number(item.bookId);

            });

            return {

                bookId: Number(item.bookId),

                quantity: Number(item.quantity),

                title: book ? book.title : "Book unavailable",

                author: book ? book.author : "Unknown author",

                price: book ? Number(book.price) : 0,

                stockQuantity: book
                    ? Number(book.stockQuantity)
                    : 0,

                available: Boolean(book)

            };

        });

        // DISPLAY BOOKS

        checkoutLoaded = true;

        displayCheckoutItems();
        updateCheckoutSummary();

    } catch (error) {

        console.error("Checkout error:", error);

        checkoutLoaded = false;
        checkoutItems = [];

        container.textContent =
            error.message +
            " Please return to your cart and try again.";

        updateCheckoutSummary();

    }

}

// CREATE BOOK DISPLAY

function createCheckoutBook(item, review = false) {

    const bookElement = document.createElement("div");

    bookElement.className = review
        ? "review-book"
        : "checkout-book";

    // BOOK COVER

    const cover = document.createElement("div");

    cover.className = review
        ? "review-book-cover"
        : "checkout-book-cover";

    cover.textContent = "Book Cover";

    // BOOK INFORMATION

    const info = document.createElement("div");

    info.className = review
        ? "review-book-info"
        : "checkout-book-info";

    // BOOK TITLE

    const title = document.createElement(review ? "h4" : "h3");

    title.textContent = item.title;

    // BOOK AUTHOR

    const author = document.createElement("p");

    author.textContent = item.author;

    // BOOK QUANTITY

    const quantity = document.createElement("p");

    quantity.textContent = "Qty: " + item.quantity;

    info.appendChild(title);
    info.appendChild(author);
    info.appendChild(quantity);

    // STOCK WARNING

    if (!item.available || item.stockQuantity < item.quantity) {

        const warning = document.createElement("p");

        warning.textContent = "Unavailable or insufficient stock";
        warning.style.color = "#A33B32";

        info.appendChild(warning);

    }

    // BOOK PRICE

    const price = document.createElement("div");

    price.className = review
        ? "review-book-price"
        : "checkout-book-price";

    price.textContent = formatCheckoutPrice(
        item.price * item.quantity
    );

    // ADD BOOK DETAILS

    bookElement.appendChild(cover);
    bookElement.appendChild(info);
    bookElement.appendChild(price);

    return bookElement;

}

// DISPLAY CHECKOUT ITEMS

function displayCheckoutItems() {

    const container = document.getElementById("checkoutItems");

    container.innerHTML = "";

    if (checkoutItems.length === 0) {

        container.textContent = "No books selected for checkout.";
        return;

    }

    checkoutItems.forEach(function (item) {

        container.appendChild(createCheckoutBook(item));

    });

}

// GET SHIPPING METHOD

function getSelectedShippingMethod() {

    const selected = document.querySelector(
        'input[name="shippingMethod"]:checked'
    );

    return selected ? selected.value : "standard";

}

// CALCULATE SHIPPING

function calculateCheckoutShipping() {

    if (checkoutItems.length === 0) {
        return 0;
    }

    if (getSelectedShippingMethod() === "express") {
        return EXPRESS_SHIPPING_COST;
    }

    if (checkoutSubtotal >= FREE_SHIPPING_THRESHOLD) {
        return 0;
    }

    return STANDARD_SHIPPING_COST;

}

// UPDATE SHIPPING OPTIONS

function updateShippingOptions() {

    const standardCost =
        checkoutSubtotal >= FREE_SHIPPING_THRESHOLD
            ? 0
            : STANDARD_SHIPPING_COST;

    // STANDARD SHIPPING PRICE

    document.getElementById("standardShippingPrice").textContent =
        standardCost === 0
            ? "Free"
            : formatCheckoutPrice(standardCost);

    // EXPRESS SHIPPING PRICE

    const expressOption = document.querySelector(
        'input[name="shippingMethod"][value="express"]'
    );

    if (expressOption) {

        const expressPrice = expressOption
            .closest(".shipping-option")
            .querySelector(".shipping-price strong");

        expressPrice.textContent =
            formatCheckoutPrice(EXPRESS_SHIPPING_COST);

    }

    // HIGHLIGHT SELECTED SHIPPING METHOD

    document.querySelectorAll(".shipping-option").forEach(
        function (option) {

            const radio = option.querySelector(
                'input[type="radio"]'
            );

            option.classList.toggle("selected", radio.checked);

        }
    );

}

// CHECK CART AVAILABILITY

function isCheckoutCartValid() {

    if (!checkoutLoaded || checkoutItems.length === 0) {
        return false;
    }

    return checkoutItems.every(function (item) {

        return item.available &&
            Number.isFinite(item.price) &&
            item.price >= 0 &&
            Number.isInteger(item.quantity) &&
            item.quantity > 0 &&
            Number.isFinite(item.stockQuantity) &&
            item.stockQuantity >= item.quantity;

    });

}

// UPDATE ORDER SUMMARY

function updateCheckoutSummary() {

    // CALCULATE SUBTOTAL

    checkoutSubtotal = checkoutItems.reduce(function (total, item) {

        return total + item.price * item.quantity;

    }, 0);

    // CALCULATE SHIPPING

    checkoutShipping = calculateCheckoutShipping();

    // CALCULATE TOTAL

    checkoutTotal = checkoutSubtotal + checkoutShipping;

    // COUNT BOOKS

    const totalQuantity = checkoutItems.reduce(function (total, item) {

        return total + item.quantity;

    }, 0);

    // UPDATE SUBTOTAL LABEL

    document.getElementById("checkoutSubtotalLabel").textContent =
        `Subtotal (${totalQuantity} ${
            totalQuantity === 1 ? "item" : "items"
        })`;

    // UPDATE PRICES

    document.getElementById("checkoutSubtotal").textContent =
        formatCheckoutPrice(checkoutSubtotal);

    document.getElementById("checkoutShipping").textContent =
        checkoutShipping === 0
            ? "Free"
            : formatCheckoutPrice(checkoutShipping);

    document.getElementById("checkoutTotal").textContent =
        formatCheckoutPrice(checkoutTotal);

    // UPDATE SHIPPING AND BUTTONS

    updateShippingOptions();
    updateCheckoutButtons();

}

// UPDATE BUTTONS

function updateCheckoutButtons() {

    const validCart = isCheckoutCartValid() && !orderSubmitting;

    document.getElementById("continueToPayment").disabled =
        !validCart;

    document.getElementById("reviewOrder").disabled =
        !validCart;

    document.getElementById("placeOrderButton").disabled =
        !validCart;

}

// SHOW CHECKOUT STEP

function showCheckoutStep(step) {

    currentCheckoutStep = step;

    // SHOW SHIPPING

    document.getElementById("shippingInformationSection").hidden =
        step !== 1;

    document.getElementById("shippingMethodSection").hidden =
        step !== 1;

    // SHOW PAYMENT

    document.getElementById("paymentSection").hidden =
        step !== 2;

    // SHOW REVIEW

    document.getElementById("reviewSection").hidden =
        step !== 3;

    // SHOW CORRECT BUTTONS

    document.getElementById("continueToPayment").hidden =
        step !== 1;

    document.getElementById("placeOrderSection").hidden =
        step !== 3;

    // SHOW BOOKS ON SHIPPING AND PAYMENT

    document.getElementById("checkoutItems").hidden =
        step === 3;

    document.getElementById("summaryEditCart").hidden =
        step === 3;

    // UPDATE PROGRESS INDICATOR

    document.querySelectorAll(".progress-step").forEach(
        function (element) {

            const stepNumber = Number(element.dataset.step);

            element.classList.toggle(
                "active",
                stepNumber === step
            );

            element.classList.toggle(
                "completed",
                stepNumber < step
            );

        }
    );

    // SCROLL TO TOP

    window.scrollTo(0, 0);

}

// VALIDATE SHIPPING FORM

function validateShippingForm() {

    const form = document.getElementById("shippingForm");
    const phoneInput = document.getElementById("phoneNumber");

    // CLEAR PREVIOUS PHONE ERROR

    phoneInput.setCustomValidity("");

    // CHECK REQUIRED FIELDS

    if (!form.reportValidity()) {
        return false;
    }

    // CHECK AUSTRALIAN PHONE NUMBER

    const phone = phoneInput.value.replace(/[\s()-]/g, "");

    if (!/^(?:\+61|0)[0-9]{9}$/.test(phone)) {

        phoneInput.setCustomValidity(
            "Please enter a valid Australian phone number."
        );

        phoneInput.reportValidity();

        return false;

    }

    return true;

}

// GET SHIPPING INFORMATION

function getShippingInformation() {

    return {

        fullName: document.getElementById("fullName").value.trim(),

        phoneNumber: document.getElementById("phoneNumber").value.trim(),

        streetAddress: document.getElementById("streetAddress").value.trim(),

        suburb: document.getElementById("suburb").value.trim(),

        state: document.getElementById("state").value,

        postcode: document.getElementById("postcode").value.trim(),

        defaultAddress: document.getElementById("defaultAddress").checked,

        shippingMethod: getSelectedShippingMethod()

    };

}

// VALIDATE DEMO PAYMENT

function validateDemoPayment() {

    const form = document.getElementById("paymentForm");

    const cardNumberInput = document.getElementById("cardNumber");
    const expiryInput = document.getElementById("expiryDate");
    const cvvInput = document.getElementById("cardCvv");

    // CLEAR PREVIOUS ERRORS

    cardNumberInput.setCustomValidity("");
    expiryInput.setCustomValidity("");
    cvvInput.setCustomValidity("");

    // CHECK REQUIRED FIELDS

    if (!form.reportValidity()) {
        return false;
    }

    // CHECK DEMO CARD NUMBER

    const cardNumber = cardNumberInput.value.replace(/\s/g, "");

    if (!/^[0-9]{16}$/.test(cardNumber)) {

        cardNumberInput.setCustomValidity(
            "Please enter a 16-digit demo card number."
        );

        cardNumberInput.reportValidity();

        return false;

    }

    // CHECK EXPIRY FORMAT

    const expiry = expiryInput.value.trim();

    const match = expiry.match(/^(\d{2})\s*\/\s*(\d{2})$/);

    if (!match) {

        expiryInput.setCustomValidity("Use MM/YY format.");
        expiryInput.reportValidity();

        return false;

    }

    // CHECK EXPIRY DATE

    const month = Number(match[1]);
    const year = 2000 + Number(match[2]);

    const now = new Date();

    if (
        month < 1 ||
        month > 12 ||
        year < now.getFullYear() ||
        (
            year === now.getFullYear() &&
            month < now.getMonth() + 1
        )
    ) {

        expiryInput.setCustomValidity(
            "Please enter a valid future expiry date."
        );

        expiryInput.reportValidity();

        return false;

    }

    // CHECK DEMO CVV

    if (!/^[0-9]{3,4}$/.test(cvvInput.value)) {

        cvvInput.setCustomValidity(
            "Please enter a 3 or 4 digit demo CVV."
        );

        cvvInput.reportValidity();

        return false;

    }

    return true;

}

// DISPLAY REVIEW ITEMS

function displayReviewItems() {

    const container = document.getElementById("reviewItems");

    container.innerHTML = "";

    // COUNT BOOKS

    const totalQuantity = checkoutItems.reduce(function (total, item) {

        return total + item.quantity;

    }, 0);

    document.getElementById("reviewItemsTitle").textContent =
        `Items (${totalQuantity})`;

    // DISPLAY BOOKS

    checkoutItems.forEach(function (item) {

        container.appendChild(createCheckoutBook(item, true));

    });

}

// DISPLAY REVIEW SHIPPING

function displayReviewShipping() {

    const shipping = getShippingInformation();

    document.getElementById("reviewFullName").textContent =
        shipping.fullName;

    document.getElementById("reviewStreetAddress").textContent =
        shipping.streetAddress;

    document.getElementById("reviewSuburbStatePostcode").textContent =
        `${shipping.suburb}, ${shipping.state} ${shipping.postcode}`;

    document.getElementById("reviewPhoneNumber").textContent =
        shipping.phoneNumber;

    document.getElementById("reviewShippingMethod").textContent =
        shipping.shippingMethod === "express"
            ? "Express Shipping"
            : "Standard Shipping";

}

// DISPLAY REVIEW PAYMENT

function displayReviewPayment() {

    document.getElementById("reviewCardDetails").textContent =
        "Demo card ending in " + demoCardLastFour;

    document.getElementById("reviewCardExpiry").textContent =
        "Expiry: " + demoCardExpiry;

}

// CONTINUE TO PAYMENT

document.getElementById("continueToPayment").addEventListener(
    "click",
    function () {

        // CHECK CART

        if (!isCheckoutCartValid()) {

            alert("Please check your cart before continuing.");
            return;

        }

        // CHECK SHIPPING FORM

        if (!validateShippingForm()) {
            return;
        }

        // GO TO PAYMENT

        showCheckoutStep(2);

    }
);

// BACK TO SHIPPING

document.getElementById("backToShipping").addEventListener(
    "click",
    function () {

        showCheckoutStep(1);

    }
);

// REVIEW ORDER

document.getElementById("reviewOrder").addEventListener(
    "click",
    function () {

        // CHECK CART

        if (!isCheckoutCartValid()) {

            alert("Please check your cart before continuing.");
            return;

        }

        // CHECK PAYMENT

        if (!validateDemoPayment()) {
            return;
        }

        // SAVE MASKED DEMO CARD DETAILS

        const cardNumber = document.getElementById("cardNumber")
            .value.replace(/\s/g, "");

        demoCardLastFour = cardNumber.slice(-4);

        demoCardExpiry = document.getElementById("expiryDate")
            .value.trim();

        // CLEAR FULL CARD NUMBER AND CVV

        document.getElementById("cardNumber").value = "";
        document.getElementById("cardCvv").value = "";

        // DISPLAY REVIEW DETAILS

        displayReviewItems();
        displayReviewShipping();
        displayReviewPayment();

        // GO TO REVIEW

        showCheckoutStep(3);

    }
);

// BACK TO PAYMENT

document.getElementById("backToPayment").addEventListener(
    "click",
    function () {

        showCheckoutStep(2);

    }
);

// EDIT SHIPPING

document.getElementById("editShippingAddress").addEventListener(
    "click",
    function () {

        showCheckoutStep(1);

    }
);

// EDIT PAYMENT

document.getElementById("editPaymentMethod").addEventListener(
    "click",
    function () {

        showCheckoutStep(2);

    }
);

// PLACE ORDER

document.getElementById("placeOrderButton").addEventListener(
    "click",
    async function () {

        // CHECK CART AND PREVENT DOUBLE CLICK

        if (!isCheckoutCartValid() || orderSubmitting) {

            alert("Please check your cart before placing an order.");
            return;

        }

        orderSubmitting = true;

        const button = document.getElementById("placeOrderButton");

        button.textContent = "Placing Order...";

        updateCheckoutButtons();

        try {

            // PREPARE ORDER ITEMS FOR BACKEND

            const orderItems = checkoutItems.map(function (item) {

                return {
                    bookId: item.bookId,
                    quantity: item.quantity
                };

            });

            // SEND ORDER TO ORDER SERVICE

            const response = await fetch(
                `${CHECKOUT_ORDER_API_URL}?customerId=${CHECKOUT_CUSTOMER_ID}`,
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify(orderItems)
                }
            );

            // CHECK RESPONSE

            if (!response.ok) {

                throw new Error(
                    "Could not place your order. HTTP " + response.status
                );

            }

            // GET CREATED ORDER

            const order = await response.json();

            // CLEAR CHECKOUT SELECTION

            sessionStorage.removeItem("literatureLandCheckout");

            // SHOW SUCCESS MESSAGE

            alert(
                "Order placed successfully!\n\n" +
                "Order Number: #" + order.id + "\n" +
                "Status: " + order.status + "\n\n" +
                "You can view your order in My Orders."
            );

            // GO TO MY ORDERS

            window.location.href = "../orders/orders.html";

        } catch (error) {

            console.error("Order error:", error);

            alert(
                "Could not confirm your order.\n" +
                error.message + "\n\n" +
                "Check your Order Service before trying again."
            );

            orderSubmitting = false;

            button.textContent = "Place Order";

            updateCheckoutButtons();

        }

    }
);

// CLEAR VALIDATION ERRORS

["phoneNumber", "cardNumber", "expiryDate", "cardCvv"].forEach(
    function (id) {

        document.getElementById(id).addEventListener(
            "input",
            function () {

                this.setCustomValidity("");

            }
        );

    }
);

// UPDATE SHIPPING PRICE

document.querySelectorAll(
    'input[name="shippingMethod"]'
).forEach(function (radio) {

    radio.addEventListener("change", function () {

        updateCheckoutSummary();

    });

});

// START CHECKOUT

async function initializeCheckoutPage() {

    // START ON SHIPPING

    showCheckoutStep(1);

    // LOAD SHARED HEADER AND SIDEBAR

    await Promise.all([
        loadCheckoutHeader(),
        loadCheckoutSidebar()
    ]);

    // LOAD SELECTED CART ITEMS

    await loadCheckoutCart();

}

initializeCheckoutPage();