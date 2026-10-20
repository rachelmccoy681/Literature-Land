// WISHLIST STORAGE KEY

const SHARED_WISHLIST_KEY = "literatureLandWishlist";


// GET SAVED BOOK IDS

function getSharedWishlistIds() {

    try {

        const saved = JSON.parse(
            localStorage.getItem(SHARED_WISHLIST_KEY) || "[]"
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


// CHECK IF BOOK IS SAVED

function isSharedBookWishlisted(bookId) {

    return getSharedWishlistIds().includes(Number(bookId));
}


// SAVE WISHLIST

function saveSharedWishlistIds(ids) {

    const uniqueIds = [...new Set(ids)];

    localStorage.setItem(
        SHARED_WISHLIST_KEY,
        JSON.stringify(uniqueIds)
    );

    window.dispatchEvent(new Event("wishlist:updated"));
}


// ADD BOOK TO WISHLIST

function addSharedWishlistBook(bookId) {

    const ids = getSharedWishlistIds();
    const id = Number(bookId);

    if (!ids.includes(id)) {
        ids.push(id);
        saveSharedWishlistIds(ids);
    }
}


// REMOVE BOOK FROM WISHLIST

function removeSharedWishlistBook(bookId) {

    const ids = getSharedWishlistIds().filter(function (id) {
        return id !== Number(bookId);
    });

    saveSharedWishlistIds(ids);
}


// TOGGLE BOOK WISHLIST

function toggleSharedWishlistBook(bookId) {

    if (isSharedBookWishlisted(bookId)) {

        removeSharedWishlistBook(bookId);

    } else {

        addSharedWishlistBook(bookId);
    }
}


// UPDATE HEART APPEARANCE

function updateSharedWishlistHeart(button, bookId) {

    if (!button) {
        return;
    }

    const saved = isSharedBookWishlisted(bookId);

    button.classList.toggle("active", saved);

    button.setAttribute("aria-pressed", String(saved));

    button.title = saved
        ? "Remove from wishlist"
        : "Add to wishlist";

    // KEEP EXISTING SVG ICON AND FILL HEART DARK BROWN

    const icon = button.querySelector("img");

    if (icon) {

        icon.style.filter = saved
            ? "brightness(0) saturate(100%) invert(20%) sepia(19%) saturate(1186%) hue-rotate(338deg) brightness(94%) contrast(91%)"
            : "";
    }
}


// UPDATE HEARTS WHEN WISHLIST CHANGES

window.addEventListener("wishlist:updated", function () {

    document.querySelectorAll("[data-wishlist-id]").forEach(function (button) {

        updateSharedWishlistHeart(
            button,
            button.dataset.wishlistId
        );
    });
});