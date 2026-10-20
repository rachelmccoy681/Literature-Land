
// CHATBOT ELEMENTS

const chatbotMessages = document.getElementById("chatbotMessages");
const chatbotForm = document.getElementById("chatbotForm");
const chatbotInput = document.getElementById("chatbotInput");
const chatbotSendButton = document.getElementById("chatbotSendButton");

// AI BACKEND SETTINGS
// ADD THE REAL ENDPOINT WHEN THE AI SERVICE IS READY

const CHATBOT_API_URL = "";

// LOAD HEADER

async function loadChatbotHeader() {

    try {

        const response = await fetch("../components/header.html");

        if (!response.ok) {
            throw new Error("Could not load header.");
        }

        document.getElementById("header-container").innerHTML =
            await response.text();

        // UPDATE CART COUNTER

        if (typeof updateCartCount === "function") {
            await updateCartCount();
        }

    } catch (error) {

        console.error("Header error:", error);

    }

}

// LOAD SIDEBAR

async function loadChatbotSidebar() {

    try {

        const response = await fetch("../components/sidebar.html");

        if (!response.ok) {
            throw new Error("Could not load sidebar.");
        }

        document.getElementById("sidebar-container").innerHTML =
            await response.text();

        // HIGHLIGHT CHATBOT

        document.querySelectorAll("#sidebar-container a").forEach(
            function (link) {

                link.classList.remove("active");

                const href = link.getAttribute("href") || "";

                if (href.includes("chatbot")) {
                    link.classList.add("active");
                }

            }
        );

    } catch (error) {

        console.error("Sidebar error:", error);

    }

}

// REFRESH SVG ICONS

function refreshChatbotIcons() {

    if (window.lucide) {
        window.lucide.createIcons();
    }

}

// FORMAT MESSAGE TIME

function formatChatbotTime() {

    return new Date().toLocaleTimeString("en-AU", {
        hour: "numeric",
        minute: "2-digit"
    });

}

// ADD CHAT MESSAGE

function addChatbotMessage(text, sender, isError = false) {

    // REMOVE WELCOME MESSAGE

    const welcome = chatbotMessages.querySelector(".chatbot-welcome");

    if (welcome) {
        welcome.remove();
    }

    // CREATE MESSAGE ROW

    const row = document.createElement("div");

    row.className = "chatbot-message-row " + sender;

    if (isError) {
        row.classList.add("error");
    }

    // CREATE AVATAR

    const avatar = document.createElement("div");

    avatar.className = "chatbot-avatar";

    const avatarIcon = document.createElement("i");

    avatarIcon.setAttribute(
        "data-lucide",
        sender === "user" ? "user-round" : "sparkles"
    );

    avatar.appendChild(avatarIcon);

    // CREATE MESSAGE CONTENT

    const content = document.createElement("div");

    content.className = "chatbot-message-content";

    // CREATE MESSAGE BUBBLE

    const bubble = document.createElement("div");

    bubble.className = "chatbot-bubble";
    bubble.textContent = text;

    // CREATE TIME

    const time = document.createElement("span");

    time.className = "chatbot-time";
    time.textContent = formatChatbotTime();

    content.appendChild(bubble);
    content.appendChild(time);

    row.appendChild(avatar);
    row.appendChild(content);

    chatbotMessages.appendChild(row);

    // SCROLL TO LATEST MESSAGE

    chatbotMessages.scrollTop = chatbotMessages.scrollHeight;

    refreshChatbotIcons();

    return row;

}

// SHOW WELCOME MESSAGE

function showChatbotWelcome() {

    chatbotMessages.innerHTML = "";

    const welcome = document.createElement("div");

    welcome.className = "chatbot-welcome";

    const icon = document.createElement("i");

    icon.setAttribute("data-lucide", "messages-square");

    const title = document.createElement("h2");

    title.textContent = "Welcome to Literature Land!";

    const description = document.createElement("p");

    description.textContent =
        "Ask a question or select one of the suggested questions to get started.";

    welcome.appendChild(icon);
    welcome.appendChild(title);
    welcome.appendChild(description);

    chatbotMessages.appendChild(welcome);

    refreshChatbotIcons();

}

// SET LOADING STATE

function setChatbotLoading(isLoading) {

    chatbotInput.disabled = isLoading;
    chatbotSendButton.disabled = isLoading;

    document.querySelectorAll(".chatbot-suggestion").forEach(
        function (button) {

            button.disabled = isLoading;

        }
    );

}

// SEND MESSAGE

async function sendChatbotMessage(message) {

    const text = message.trim();

    if (!text || chatbotSendButton.disabled) {
        return;
    }

    // DISPLAY USER MESSAGE

    addChatbotMessage(text, "user");

    chatbotInput.value = "";

    // CHECK BACKEND CONNECTION

    if (!CHATBOT_API_URL) {

        addChatbotMessage(
            "The AI Service is not connected yet. Your message has been displayed, but AI responses will be available once the backend is connected.",
            "assistant",
            true
        );

        return;
    }

    // SEND TO REAL AI BACKEND

    setChatbotLoading(true);

    const loadingMessage = addChatbotMessage(
        "Waiting for AI response...",
        "assistant"
    );

    try {

        // UPDATE REQUEST FORMAT TO MATCH AI CONTROLLER

        const response = await fetch(CHATBOT_API_URL, {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify({
                message: text
            })

        });

        if (!response.ok) {
            throw new Error("AI Service request failed.");
        }

        const result = await response.json();

        // UPDATE RESPONSE FIELD TO MATCH AI CONTROLLER

        const answer = result.response || result.answer;

        if (!answer) {
            throw new Error("AI Service returned an empty response.");
        }

        loadingMessage.remove();

        addChatbotMessage(answer, "assistant");

    } catch (error) {

        console.error("Chatbot error:", error);

        loadingMessage.remove();

        addChatbotMessage(
            "Could not connect to the AI Service. Please try again later.",
            "assistant",
            true
        );

    } finally {

        setChatbotLoading(false);
        chatbotInput.focus();

    }

}

// MESSAGE FORM

chatbotForm.addEventListener("submit", function (event) {

    event.preventDefault();

    sendChatbotMessage(chatbotInput.value);

});

// SUGGESTED QUESTIONS

document.querySelectorAll(".chatbot-suggestion").forEach(
    function (button) {

        button.addEventListener("click", function () {

            sendChatbotMessage(button.textContent.trim());

        });

    }
);

// START CHATBOT PAGE

async function initializeChatbotPage() {

    showChatbotWelcome();

    await Promise.all([
        loadChatbotHeader(),
        loadChatbotSidebar()
    ]);

    refreshChatbotIcons();

}

initializeChatbotPage();