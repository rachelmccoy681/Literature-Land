// LOAD HEADER

async function loadHeader() {
    try {
        const response = await fetch("../components/header.html");

        if (!response.ok) {
            throw new Error("Could not load header");
        }

        const headerHTML = await response.text();

        document.getElementById("header-container").innerHTML = headerHTML;

    } catch (error) {
        console.error("Error loading header:", error);
    }
}

// START HEADER

loadHeader();


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

// START SIDEBAR

loadSidebar();


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