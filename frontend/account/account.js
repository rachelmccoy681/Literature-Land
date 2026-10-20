// LOAD HEADER

async function loadAccountHeader() {

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

async function loadAccountSidebar() {

    try {

        const response = await fetch("../components/sidebar.html");

        if (!response.ok) {
            throw new Error("Could not load sidebar.");
        }

        document.getElementById("sidebar-container").innerHTML =
            await response.text();

        // HIGHLIGHT MY ACCOUNT

        const sidebarLinks = document.querySelectorAll(
            "#sidebar-container a"
        );

        sidebarLinks.forEach(function (link) {

            link.classList.remove("active");

            const linkAddress = link.getAttribute("href") || "";

            if (
                linkAddress.includes("account") &&
                !linkAddress.includes("login")
            ) {

                link.classList.add("active");

            }

        });

    } catch (error) {

        console.error("Sidebar error:", error);

    }

}


// STORAGE SETTINGS

const ACCOUNT_STORAGE_KEY = "literatureLandAccount";
const PREFERENCES_STORAGE_KEY = "literatureLandPreferences";


// DEFAULT ACCOUNT INFORMATION

const defaultAccount = {

    fullName: "",
    email: "",
    dateOfBirth: "",
    phone: "",
    shippingAddress: ""

};


// DEFAULT PREFERENCES

const defaultPreferences = {

    emailNotifications: true,
    marketingEmails: true,
    language: "English"

};


// CURRENT SAVED DATA

let savedAccount = { ...defaultAccount };
let savedPreferences = { ...defaultPreferences };


// SHOW MESSAGE

function showAccountMessage(message, isError = false) {

    const messageElement = document.getElementById("accountMessage");

    messageElement.textContent = message;

    messageElement.classList.toggle("error", isError);

    messageElement.hidden = false;

    // HIDE MESSAGE AFTER 4 SECONDS

    setTimeout(function () {

        messageElement.hidden = true;

    }, 4000);

}


// GET SAVED DATA

function getSavedAccountData() {

    try {

        const storedData = localStorage.getItem(ACCOUNT_STORAGE_KEY);

        if (storedData) {

            return {
                ...defaultAccount,
                ...JSON.parse(storedData)
            };

        }

    } catch (error) {

        console.error("Account storage error:", error);

    }

    return { ...defaultAccount };

}


// GET SAVED PREFERENCES

function getSavedPreferences() {

    try {

        const storedData = localStorage.getItem(PREFERENCES_STORAGE_KEY);

        if (storedData) {

            return {
                ...defaultPreferences,
                ...JSON.parse(storedData)
            };

        }

    } catch (error) {

        console.error("Preferences storage error:", error);

    }

    return { ...defaultPreferences };

}


// DISPLAY ACCOUNT INFORMATION

function displayAccountInformation() {

    document.getElementById("accountFullName").value =
        savedAccount.fullName;

    document.getElementById("accountEmail").value =
        savedAccount.email;

    document.getElementById("accountDateOfBirth").value =
        savedAccount.dateOfBirth;

    document.getElementById("accountPhone").value =
        savedAccount.phone;

    document.getElementById("accountShippingAddress").value =
        savedAccount.shippingAddress;

}


// DISPLAY PREFERENCES

function displayPreferences() {

    document.getElementById("emailNotifications").checked =
        savedPreferences.emailNotifications;

    document.getElementById("marketingEmails").checked =
        savedPreferences.marketingEmails;

    document.getElementById("accountLanguage").value =
        savedPreferences.language;

}


// ENABLE OR DISABLE ACCOUNT FIELDS

function setAccountEditing(isEditing) {

    const form = document.getElementById("accountInformationForm");

    const fields = form.querySelectorAll(
        "input, textarea"
    );

    fields.forEach(function (field) {

        field.disabled = !isEditing;

    });

    document.getElementById("accountActions").hidden =
        !isEditing;

    document.getElementById("editAccountButton").hidden =
        isEditing;

}


// ENABLE OR DISABLE PREFERENCES

function setPreferencesEditing(isEditing) {

    const form = document.getElementById("preferencesForm");

    const fields = form.querySelectorAll(
        "input, select"
    );

    fields.forEach(function (field) {

        field.disabled = !isEditing;

    });

    document.getElementById("preferencesActions").hidden =
        !isEditing;

    document.getElementById("editPreferencesButton").hidden =
        isEditing;

}


// EDIT ACCOUNT BUTTON

document.getElementById("editAccountButton").addEventListener(
    "click",
    function () {

        setAccountEditing(true);

        document.getElementById("accountFullName").focus();

    }
);


// CANCEL ACCOUNT CHANGES

document.getElementById("cancelAccountButton").addEventListener(
    "click",
    function () {

        // RESTORE SAVED INFORMATION

        displayAccountInformation();

        // DISABLE FIELDS

        setAccountEditing(false);

    }
);


// SAVE ACCOUNT INFORMATION

document.getElementById("accountInformationForm").addEventListener(
    "submit",
    function (event) {

        event.preventDefault();

        // CHECK REQUIRED FIELDS

        if (!this.reportValidity()) {
            return;
        }

        // GET UPDATED INFORMATION

        const updatedAccount = {

            fullName: document.getElementById("accountFullName")
                .value.trim(),

            email: document.getElementById("accountEmail")
                .value.trim(),

            dateOfBirth: document.getElementById("accountDateOfBirth")
                .value,

            phone: document.getElementById("accountPhone")
                .value.trim(),

            shippingAddress: document.getElementById("accountShippingAddress")
                .value.trim()

        };

        // SAVE TO BROWSER

        try {

            localStorage.setItem(
                ACCOUNT_STORAGE_KEY,
                JSON.stringify(updatedAccount)
            );

            // UPDATE SAVED DATA

            savedAccount = updatedAccount;

            // DISPLAY UPDATED INFORMATION

            displayAccountInformation();

            // DISABLE EDITING

            setAccountEditing(false);

            showAccountMessage(
                "Account information saved successfully!"
            );

        } catch (error) {

            console.error("Save account error:", error);

            showAccountMessage(
                "Could not save account information.",
                true
            );

        }

    }
);


// EDIT PREFERENCES BUTTON

document.getElementById("editPreferencesButton").addEventListener(
    "click",
    function () {

        setPreferencesEditing(true);

    }
);


// CANCEL PREFERENCES CHANGES

document.getElementById("cancelPreferencesButton").addEventListener(
    "click",
    function () {

        // RESTORE SAVED PREFERENCES

        displayPreferences();

        // DISABLE FIELDS

        setPreferencesEditing(false);

    }
);


// SAVE PREFERENCES

document.getElementById("preferencesForm").addEventListener(
    "submit",
    function (event) {

        event.preventDefault();

        // GET UPDATED PREFERENCES

        const updatedPreferences = {

            emailNotifications:
                document.getElementById("emailNotifications").checked,

            marketingEmails:
                document.getElementById("marketingEmails").checked,

            language:
                document.getElementById("accountLanguage").value

        };

        // SAVE TO BROWSER

        try {

            localStorage.setItem(
                PREFERENCES_STORAGE_KEY,
                JSON.stringify(updatedPreferences)
            );

            // UPDATE SAVED PREFERENCES

            savedPreferences = updatedPreferences;

            // DISPLAY UPDATED PREFERENCES

            displayPreferences();

            // DISABLE EDITING

            setPreferencesEditing(false);

            showAccountMessage(
                "Preferences saved successfully!"
            );

        } catch (error) {

            console.error("Save preferences error:", error);

            showAccountMessage(
                "Could not save preferences.",
                true
            );

        }

    }
);


// INITIALIZE ACCOUNT PAGE

async function initializeAccountPage() {

    // LOAD SAVED INFORMATION

    savedAccount = getSavedAccountData();

    savedPreferences = getSavedPreferences();

    // DISPLAY ACCOUNT INFORMATION

    displayAccountInformation();

    displayPreferences();

    // START WITH EDITING DISABLED

    setAccountEditing(false);

    setPreferencesEditing(false);

    // LOAD SHARED COMPONENTS

    await Promise.all([
        loadAccountHeader(),
        loadAccountSidebar()
    ]);

}


// START PAGE

initializeAccountPage();