
// LOAD HEADER

async function loadAuthHeader() {
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

// LOAD SIDEBAR

async function loadAuthSidebar() {
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

// FORM ELEMENTS

const loginTab = document.getElementById("loginTab");
const registerTab = document.getElementById("registerTab");

const loginSection = document.getElementById("loginSection");
const registerSection = document.getElementById("registerSection");

const loginForm = document.getElementById("loginForm");
const registerForm = document.getElementById("registerForm");

const authMessage = document.getElementById("authMessage");

// SHOW MESSAGE

function showAuthMessage(message, type = "error") {
    authMessage.textContent = message;
    authMessage.className = type;
}

// CLEAR MESSAGE

function clearAuthMessage() {
    authMessage.textContent = "";
    authMessage.className = "";
}

// SWITCH TABS

function showAuthTab(tab) {
    const isLogin = tab === "login";

    loginSection.hidden = !isLogin;
    registerSection.hidden = isLogin;

    loginTab.classList.toggle("active", isLogin);
    registerTab.classList.toggle("active", !isLogin);

    clearAuthMessage();
}

// LOGIN TAB

loginTab.addEventListener("click", function () {
    showAuthTab("login");
});

// REGISTER TAB

registerTab.addEventListener("click", function () {
    showAuthTab("register");
});

// SWITCH TO REGISTER

document.getElementById("showRegister").addEventListener(
    "click",
    function () {
        showAuthTab("register");
    }
);

// SWITCH TO LOGIN

document.getElementById("showLogin").addEventListener(
    "click",
    function () {
        showAuthTab("login");
    }
);

// SHOW AND HIDE PASSWORD

document.querySelectorAll(".toggle-password").forEach(function (button) {
    button.addEventListener("click", function () {
        const inputId = button.dataset.target;
        const input = document.getElementById(inputId);

        const showPassword = input.type === "password";

        input.type = showPassword ? "text" : "password";

        button.textContent = showPassword ? "Hide" : "Show";

        button.setAttribute(
            "aria-label",
            showPassword ? "Hide password" : "Show password"
        );
    });
});

// VALIDATE EMAIL

function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

// VALIDATE PASSWORD

function isValidPassword(password) {
    return password.length >= 8;
}

// LOGIN FORM

loginForm.addEventListener("submit", function (event) {
    event.preventDefault();

    clearAuthMessage();

    const email = document.getElementById("loginEmail").value.trim();
    const password = document.getElementById("loginPassword").value;

    if (!email || !password) {
        showAuthMessage("Please fill in all required fields.");
        return;
    }

    if (!isValidEmail(email)) {
        showAuthMessage("Please enter a valid email address.");
        return;
    }

    // BACKEND LOGIN NOT CONNECTED YET

    showAuthMessage(
        "Login will be available once the authentication service is connected.",
        "error"
    );
});

// REGISTER FORM

registerForm.addEventListener("submit", function (event) {
    event.preventDefault();

    clearAuthMessage();

    const fullName = document.getElementById("registerName").value.trim();
    const email = document.getElementById("registerEmail").value.trim();
    const password = document.getElementById("registerPassword").value;
    const confirmPassword = document.getElementById("confirmPassword").value;

    if (!fullName || !email || !password || !confirmPassword) {
        showAuthMessage("Please fill in all required fields.");
        return;
    }

    if (!isValidEmail(email)) {
        showAuthMessage("Please enter a valid email address.");
        return;
    }

    if (!isValidPassword(password)) {
        showAuthMessage("Password must be at least 8 characters long.");
        return;
    }

    if (password !== confirmPassword) {
        showAuthMessage("Passwords do not match.");
        return;
    }

    // BACKEND REGISTRATION NOT CONNECTED YET

    showAuthMessage(
        "Registration will be available once the customer service is connected.",
        "error"
    );
});

// FORGOT PASSWORD

document.getElementById("forgotPassword").addEventListener(
    "click",
    function (event) {
        event.preventDefault();

        showAuthMessage(
            "Password reset will be available once the authentication service is connected."
        );
    }
);

// START AUTH PAGE

async function initializeAuthPage() {
    showAuthTab("login");

    await Promise.all([
        loadAuthHeader(),
        loadAuthSidebar()
    ]);
}

initializeAuthPage();