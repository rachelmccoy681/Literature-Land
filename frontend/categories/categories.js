// BACKEND API ADDRESS

const API_URL = "http://localhost:8081/books";


// CATEGORY SETTINGS

const categoriesGrid = document.getElementById("categoriesGrid");

const categorySort = document.getElementById("categorySort");

const previousButton = document.getElementById("previousCategories");

const nextButton = document.getElementById("nextCategories");

const pageNumbers = document.getElementById("categoryPageNumbers");

const categoriesPerPage = 8;

let currentPage = 1;

let categories = [];


// LOAD HEADER

async function loadHeader() {

    try {

        const response = await fetch("../components/header.html");

        if (!response.ok) {
            throw new Error("Could not load header");
        }

        const headerHTML = await response.text();

        document.getElementById("header-container").innerHTML = headerHTML;

        // UPDATE CART COUNT

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

        // HIGHLIGHT CATEGORIES LINK

        const links = document.querySelectorAll(
            "#sidebar-container a"
        );

        links.forEach(function (link) {

            if (link.getAttribute("href")?.includes("categories.html")) {
                link.classList.add("active");
            }

        });

    } catch (error) {

        console.error("Error loading sidebar:", error);

    }
}


// GET CATEGORIES FROM HTML

function getCategories() {

    categories = Array.from(
        document.querySelectorAll(".category-card")
    );

}


// LOAD BOOKS FROM BACKEND

async function loadCategoryCounts() {

    try {

        const response = await fetch(API_URL);

        if (!response.ok) {
            throw new Error("Could not load books");
        }

        const books = await response.json();

        // COUNT BOOKS IN EACH CATEGORY

        categories.forEach(function (category) {

            const categoryName = category.dataset.category
                .trim()
                .toLowerCase();

            const count = books.filter(function (book) {

                return String(book.genre || "")
                    .trim()
                    .toLowerCase() === categoryName;

            }).length;

            // UPDATE CATEGORY COUNT

            category.dataset.count = count;

            const countText = category.querySelector("p");

            countText.textContent =
                count === 1 ? "1 book" : `${count} books`;

        });

        // UPDATE SORTING AND PAGINATION

        sortCategories();

    } catch (error) {

        console.error("Error loading category counts:", error);

        // SHOW THAT COUNTS COULD NOT BE LOADED

        categories.forEach(function (category) {

            category.dataset.count = 0;

            category.querySelector("p").textContent =
                "Count unavailable";

        });

        sortCategories();

    }
}


// SORT CATEGORIES

function sortCategories() {

    const sortValue = categorySort.value;

    if (sortValue === "az") {

        categories.sort(function (a, b) {

            return a.dataset.category.localeCompare(
                b.dataset.category
            );

        });

    }

    if (sortValue === "za") {

        categories.sort(function (a, b) {

            return b.dataset.category.localeCompare(
                a.dataset.category
            );

        });

    }

    if (sortValue === "most") {

        categories.sort(function (a, b) {

            return Number(b.dataset.count) -
                Number(a.dataset.count);

        });

    }

    if (sortValue === "least") {

        categories.sort(function (a, b) {

            return Number(a.dataset.count) -
                Number(b.dataset.count);

        });

    }

    displayCategories();

}


// DISPLAY CATEGORIES

function displayCategories() {

    categoriesGrid.innerHTML = "";

    const startIndex = (currentPage - 1) * categoriesPerPage;

    const endIndex = startIndex + categoriesPerPage;

    const visibleCategories = categories.slice(
        startIndex,
        endIndex
    );

    visibleCategories.forEach(function (category) {

        categoriesGrid.appendChild(category);

    });

    updatePagination();

}


// UPDATE PAGINATION

function updatePagination() {

    const totalPages = Math.max(
        1,
        Math.ceil(categories.length / categoriesPerPage)
    );

    pageNumbers.innerHTML = "";

    // PREVIOUS BUTTON

    previousButton.disabled = currentPage === 1;

    // PAGE NUMBERS

    for (let page = 1; page <= totalPages; page++) {

        const button = document.createElement("button");

        button.type = "button";

        button.textContent = page;

        if (page === currentPage) {
            button.classList.add("active");
        }

        button.addEventListener("click", function () {

            currentPage = page;

            displayCategories();

        });

        pageNumbers.appendChild(button);

    }

    // NEXT BUTTON

    nextButton.disabled = currentPage === totalPages;

}


// PREVIOUS PAGE

previousButton.addEventListener("click", function () {

    if (currentPage > 1) {

        currentPage--;

        displayCategories();

    }

});


// NEXT PAGE

nextButton.addEventListener("click", function () {

    const totalPages = Math.ceil(
        categories.length / categoriesPerPage
    );

    if (currentPage < totalPages) {

        currentPage++;

        displayCategories();

    }

});


// SORT DROPDOWN

categorySort.addEventListener("change", function () {

    currentPage = 1;

    sortCategories();

});


// START PAGE

loadHeader();

loadSidebar();

getCategories();

sortCategories();

loadCategoryCounts();