// CART SERVICE ADDRESS

const HEADER_CART_API_URL = "http://localhost:8082/api/carts";


// TEMPORARY CUSTOMER ID FOR TESTING

const HEADER_CUSTOMER_ID = 1;


// UPDATE HEADER CART COUNT

async function updateHeaderCartCount() {

    try {

        // GET CUSTOMER CART FROM BACKEND

        const response = await fetch(
            `${HEADER_CART_API_URL}/${HEADER_CUSTOMER_ID}`
        );

        // SHOW ZERO IF CUSTOMER DOES NOT HAVE A CART YET

        if (response.status === 404) {
            displayHeaderCartCount(0);
            return;
        }

        if (!response.ok) {
            throw new Error("Could not load cart count");
        }

        // GET CART INFORMATION

        const cart = await response.json();

        // DISPLAY TOTAL QUANTITY

        displayHeaderCartCount(cart.totalQuantity);

    } catch (error) {

        console.error("Error updating header cart count:", error);
    }
}


// DISPLAY CART COUNT IN HEADER

function displayHeaderCartCount(count) {

    // FIND CART COUNT IN SHARED HEADER

    const cartCount = document.getElementById("cart-count");

    if (!cartCount) {
        return;
    }

    // UPDATE CART NUMBER

    cartCount.textContent = count;
}


// WAIT FOR SHARED HEADER TO LOAD

function waitForCartHeader() {

    const headerContainer = document.getElementById("header-container");

    if (!headerContainer) {
        return;
    }

    // WATCH FOR HEADER HTML BEING INSERTED

    const observer = new MutationObserver(function () {

        const cartLink = headerContainer.querySelector(
            'a[href*="cart"]'
        );

        if (cartLink) {

            // UPDATE COUNT AFTER HEADER APPEARS

            updateHeaderCartCount();

            // STOP WATCHING AFTER HEADER LOADS

            observer.disconnect();
        }
    });

    observer.observe(headerContainer, {
        childList: true,
        subtree: true
    });

    // ALSO CHECK IF HEADER IS ALREADY LOADED

    if (headerContainer.querySelector('a[href*="cart"]')) {
        updateHeaderCartCount();
        observer.disconnect();
    }
}


// REFRESH CART COUNT AFTER CART CHANGES

window.addEventListener("cart:updated", function () {
    updateHeaderCartCount();
});


// START CART HEADER

waitForCartHeader();