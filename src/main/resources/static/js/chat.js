const chatForm =
    document.getElementById("chatForm");

const questionInput =
    document.getElementById("questionInput");

const messageList =
    document.getElementById("messageList");

const emptyMessage =
    document.getElementById("emptyMessage");

const statusMessage =
    document.getElementById("statusMessage");

const sendButton =
    document.getElementById("sendButton");


document.addEventListener(
    "DOMContentLoaded",
    loadConversations
);


chatForm.addEventListener(
    "submit",
    sendMessage
);


questionInput.addEventListener(
    "keydown",
    event => {

        if (event.key === "Enter"
            && !event.shiftKey) {

            event.preventDefault();

            chatForm.requestSubmit();
        }
    }
);


async function loadConversations() {

    setStatus("이전 대화를 불러오는 중입니다.");

    try {

        const response =
            await fetch(
                "/api/chat/messages",
                {
                    method: "GET",
                    headers: {
                        "Accept": "application/json"
                    }
                }
            );

        if (response.status === 401
            || response.status === 403) {

            window.location.href = "/login";
            return;
        }

        if (!response.ok) {
            throw new Error(
                "대화 목록을 불러오지 못했습니다."
            );
        }

        const conversations =
            await response.json();

        messageList.innerHTML = "";

        if (conversations.length === 0) {
            messageList.appendChild(
                createEmptyMessage()
            );

            setStatus("");
            return;
        }

        conversations
            .slice()
            .reverse()
            .forEach(
                conversation => {
                    appendConversation(
                        conversation
                    );
                }
            );

        scrollToBottom();
        setStatus("");

    } catch (error) {
        setStatus(error.message, true);
    }
}


async function sendMessage(event) {

    event.preventDefault();

    const question =
        questionInput.value.trim();

    if (!question) {
        setStatus(
            "질문을 입력해주세요.",
            true
        );

        questionInput.focus();
        return;
    }

    setLoading(true);
    setStatus("Gemini가 답변을 생성하고 있습니다.");

    try {

        const response =
            await fetch(
                "/api/chat/messages",
                {
                    method: "POST",
                    headers: {
                        "Content-Type":
                            "application/json",
                        "Accept":
                            "application/json"
                    },
                    body: JSON.stringify({
                        question: question
                    })
                }
            );

        if (response.status === 401
            || response.status === 403) {

            window.location.href = "/login";
            return;
        }

        if (!response.ok) {

            const errorMessage =
                await readErrorMessage(response);

            throw new Error(errorMessage);
        }

        const conversation =
            await response.json();

        removeEmptyMessage();

        appendConversation(conversation);

        questionInput.value = "";
        setStatus("");

        scrollToBottom();

    } catch (error) {
        setStatus(error.message, true);

    } finally {
        setLoading(false);
        questionInput.focus();
    }
}


function appendConversation(conversation) {

    const group =
        document.createElement("section");

    group.className = "message-group";

    group.appendChild(
        createMessage(
            "user",
            conversation.username,
            conversation.question,
            conversation.createdAt
        )
    );

    group.appendChild(
        createMessage(
            "ai",
            "AI",
            conversation.answer,
            conversation.createdAt
        )
    );

    messageList.appendChild(group);
}


function createMessage(
    type,
    role,
    content,
    createdAt
) {

    const wrapper =
        document.createElement("article");

    wrapper.className =
        type === "user"
            ? "message user-message"
            : "message ai-message";

    const roleElement =
        document.createElement("div");

    roleElement.className = "message-role";
    roleElement.textContent = role;

    const contentElement =
        document.createElement("div");

    contentElement.className =
        "message-content";

    contentElement.textContent =
        content ?? "";

    const timeElement =
        document.createElement("div");

    timeElement.className = "message-time";
    timeElement.textContent =
        formatDateTime(createdAt);

    wrapper.appendChild(roleElement);
    wrapper.appendChild(contentElement);
    wrapper.appendChild(timeElement);

    return wrapper;
}


function createEmptyMessage() {

    const element =
        document.createElement("div");

    element.id = "emptyMessage";
    element.className = "empty-message";
    element.textContent =
        "질문을 입력하면 대화가 시작됩니다.";

    return element;
}


function removeEmptyMessage() {

    const element =
        document.getElementById("emptyMessage");

    if (element) {
        element.remove();
    }
}


function formatDateTime(value) {

    if (!value) {
        return "";
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return date.toLocaleString("ko-KR");
}


function setLoading(loading) {

    sendButton.disabled = loading;
    questionInput.disabled = loading;

    sendButton.textContent =
        loading
            ? "답변 생성 중..."
            : "질문하기";
}


function setStatus(
    message,
    error = false
) {

    statusMessage.textContent =
        message;

    statusMessage.style.color =
        error
            ? "#dc2626"
            : "#667085";
}


function scrollToBottom() {

    messageList.scrollTop =
        messageList.scrollHeight;
}


async function readErrorMessage(response) {

    try {

        const data =
            await response.json();

        return data.message
            || data.error
            || "요청 처리 중 오류가 발생했습니다.";

    } catch (error) {

        return "요청 처리 중 오류가 발생했습니다.";
    }
}