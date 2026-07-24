const DOCUMENT_HISTORY_KEY =
    "springAiUploadedDocuments";


const documentUploadForm =
    document.getElementById(
        "documentUploadForm"
    );

const documentFile =
    document.getElementById(
        "documentFile"
    );

const securityLevel =
    document.getElementById(
        "securityLevel"
    );

const uploadButton =
    document.getElementById(
        "uploadButton"
    );

const uploadStatus =
    document.getElementById(
        "uploadStatus"
    );

const uploadedDocumentList =
    document.getElementById(
        "uploadedDocumentList"
    );

const emptyDocumentMessage =
    document.getElementById(
        "emptyDocumentMessage"
    );

const clearDocumentHistoryButton =
    document.getElementById(
        "clearDocumentHistoryButton"
    );


const chatForm =
    document.getElementById(
        "chatForm"
    );

const questionInput =
    document.getElementById(
        "questionInput"
    );

const messageList =
    document.getElementById(
        "messageList"
    );

const statusMessage =
    document.getElementById(
        "statusMessage"
    );

const sendButton =
    document.getElementById(
        "sendButton"
    );

const clearConversationButton =
    document.getElementById(
        "clearConversationButton"
    );

document.addEventListener(
    "DOMContentLoaded",
    initializePage
);

documentUploadForm.addEventListener(
    "submit",
    uploadDocument
);


clearDocumentHistoryButton.addEventListener(
    "click",
    clearDocumentHistory
);

clearConversationButton.addEventListener(
    "click",
    clearConversations
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


async function initializePage() {

    renderUploadedDocuments();

    await loadConversations();
}


async function uploadDocument(event) {

    event.preventDefault();

    const file =
        documentFile.files[0];

    if (!file) {

        setUploadStatus(
            "업로드할 파일을 선택해 주세요.",
            "error"
        );

        documentFile.focus();
        return;
    }

    if (!isAllowedFile(file.name)) {

        setUploadStatus(
            "PDF, TXT, DOC, DOCX, PPT, PPTX 파일만 업로드할 수 있습니다.",
            "error"
        );

        documentFile.value = "";
        documentFile.focus();
        return;
    }

    const maximumFileSize =
        10 * 1024 * 1024;

    if (file.size > maximumFileSize) {

        setUploadStatus(
            "파일 크기는 10MB를 초과할 수 없습니다.",
            "error"
        );

        documentFile.value = "";
        documentFile.focus();
        return;
    }

    const formData =
        new FormData();

    formData.append(
        "file",
        file
    );

    formData.append(
        "securityLevel",
        securityLevel.value
    );

    setUploadLoading(true);

    setUploadStatus(
        "문서를 분석하고 VectorStore에 저장하고 있습니다.",
        "loading"
    );

    try {

        const response =
            await fetch(
                "/api/vector-documents/upload",
                {
                    method: "POST",
                    body: formData
                }
            );

        if (response.status === 401
            || response.status === 403) {

            window.location.href =
                "/login";

            return;
        }

        if (!response.ok) {

            const errorMessage =
                await readErrorMessage(response);

            throw new Error(
                errorMessage
            );
        }

        const result =
            await response.json();

        saveUploadedDocument(
            result
        );

        renderUploadedDocuments();

        setUploadStatus(
            createUploadSuccessMessage(result),
            "success"
        );

        documentUploadForm.reset();

        securityLevel.value = "1";

        questionInput.focus();

    } catch (error) {

        setUploadStatus(
            error.message,
            "error"
        );

    } finally {

        setUploadLoading(false);
    }
}


function isAllowedFile(
    fileName
) {

    const allowedExtensions = [
        "pdf",
        "txt",
        "doc",
        "docx",
        "ppt",
        "pptx"
    ];

    const dotIndex =
        fileName.lastIndexOf(".");

    if (dotIndex < 0
        || dotIndex === fileName.length - 1) {

        return false;
    }

    const extension =
        fileName
            .substring(dotIndex + 1)
            .toLowerCase();

    return allowedExtensions.includes(
        extension
    );
}


function createUploadSuccessMessage(
    result
) {

    const fileName =
        result.fileName
        ?? "업로드 문서";

    const chunkCount =
        result.chunkCount
        ?? 0;

    return fileName
        + " 업로드가 완료되었습니다. "
        + "생성된 Chunk: "
        + chunkCount
        + "개";
}


function saveUploadedDocument(
    result
) {

    const documents =
        getUploadedDocuments();

    const uploadedDocument = {

        documentId:
            result.documentId
            ?? createTemporaryId(),

        fileName:
            result.fileName
            ?? "이름 없는 문서",

        chunkCount:
            result.chunkCount
            ?? 0,

        organizationId:
            result.organizationId
            ?? "",

        departmentId:
            result.departmentId
            ?? "",

        securityLevel:
            result.securityLevel
            ?? "",

        ownerId:
            result.ownerId
            ?? "",

        message:
            result.message
            ?? "",

        uploadedAt:
            new Date().toISOString()
    };

    const duplicateIndex =
        documents.findIndex(
            document =>
                document.documentId
                === uploadedDocument.documentId
        );

    if (duplicateIndex >= 0) {

        documents[duplicateIndex] =
            uploadedDocument;

    } else {

        documents.unshift(
            uploadedDocument
        );
    }

    localStorage.setItem(
        DOCUMENT_HISTORY_KEY,
        JSON.stringify(documents)
    );
}


function getUploadedDocuments() {

    const savedValue =
        localStorage.getItem(
            DOCUMENT_HISTORY_KEY
        );

    if (!savedValue) {
        return [];
    }

    try {

        const documents =
            JSON.parse(savedValue);

        return Array.isArray(documents)
            ? documents
            : [];

    } catch (error) {

        localStorage.removeItem(
            DOCUMENT_HISTORY_KEY
        );

        return [];
    }
}


function renderUploadedDocuments() {

    const documents =
        getUploadedDocuments();

    uploadedDocumentList.innerHTML = "";

    if (documents.length === 0) {

        emptyDocumentMessage.style.display =
            "block";

        clearDocumentHistoryButton.disabled =
            true;

        return;
    }

    emptyDocumentMessage.style.display =
        "none";

    clearDocumentHistoryButton.disabled =
        false;

    documents.forEach(
        document => {

            uploadedDocumentList.appendChild(
                createUploadedDocumentElement(
                    document
                )
            );
        }
    );
}


function createUploadedDocumentElement(
    document
) {

    const item =
        document.createElement
            ? null
            : window.document.createElement(
                "article"
            );

    item.className =
        "uploaded-document-item";


    const information =
        window.document.createElement(
            "div"
        );

    information.className =
        "document-info";


    const name =
        window.document.createElement(
            "div"
        );

    name.className =
        "document-name";

    name.textContent =
        document.fileName
        ?? "이름 없는 문서";


    const detail =
        window.document.createElement(
            "div"
        );

    detail.className =
        "document-detail";

    detail.textContent =
        "Chunk "
        + (document.chunkCount ?? 0)
        + "개 · 보안등급 "
        + (document.securityLevel ?? "-")
        + " · "
        + formatDateTime(
            document.uploadedAt
        );


    const badge =
        window.document.createElement(
            "div"
        );

    badge.className =
        "document-badge";

    badge.textContent =
        "저장 완료";


    information.appendChild(name);
    information.appendChild(detail);

    item.appendChild(information);
    item.appendChild(badge);

    return item;
}


function clearDocumentHistory() {

    const confirmed =
        window.confirm(
            "현재 브라우저에 저장된 업로드 기록을 지우시겠습니까?\n"
            + "VectorStore에 저장된 실제 문서는 삭제되지 않습니다."
        );

    if (!confirmed) {
        return;
    }

    localStorage.removeItem(
        DOCUMENT_HISTORY_KEY
    );

    renderUploadedDocuments();

    setUploadStatus(
        "브라우저의 업로드 기록을 지웠습니다. VectorStore 문서는 삭제되지 않았습니다.",
        "success"
    );
}


function setUploadLoading(
    loading
) {

    uploadButton.disabled =
        loading;

    documentFile.disabled =
        loading;

    securityLevel.disabled =
        loading;

    uploadButton.textContent =
        loading
            ? "문서 저장 중..."
            : "문서 업로드";
}


function setUploadStatus(
    message,
    type
) {

    uploadStatus.textContent =
        message;

    uploadStatus.className =
        "upload-status";

    if (type) {

        uploadStatus.classList.add(
            type
        );
    }
}


async function loadConversations() {

    setStatus(
        "이전 대화를 불러오는 중입니다."
    );

    try {

        const response =
            await fetch(
                "/api/chat/messages",
                {
                    method: "GET",
                    headers: {
                        "Accept":
                            "application/json"
                    }
                }
            );

        if (response.status === 401
            || response.status === 403) {

            window.location.href =
                "/login";

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

        setStatus(
            error.message,
            true
        );
    }
}


async function sendMessage(
    event
) {

    event.preventDefault();

    const question =
        questionInput
            .value
            .trim();

    if (!question) {

        setStatus(
            "질문을 입력해 주세요.",
            true
        );

        questionInput.focus();
        return;
    }

    setLoading(true);

    setStatus(
        "업로드된 문서를 검색하고 Gemini가 답변을 생성하고 있습니다."
    );

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

            window.location.href =
                "/login";

            return;
        }

        if (!response.ok) {

            const errorMessage =
                await readErrorMessage(
                    response
                );

            throw new Error(
                errorMessage
            );
        }

        const conversation =
            await response.json();

        removeEmptyMessage();

        appendConversation(
            conversation
        );

        questionInput.value = "";

        setStatus("");

        scrollToBottom();

    } catch (error) {

        setStatus(
            error.message,
            true
        );

    } finally {

        setLoading(false);

        questionInput.focus();
    }
}


function appendConversation(
    conversation
) {

    const group =
        document.createElement(
            "section"
        );

    group.className =
        "message-group";


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


    messageList.appendChild(
        group
    );
}


function createMessage(
    type,
    role,
    content,
    createdAt
) {

    const wrapper =
        document.createElement(
            "article"
        );

    wrapper.className =
        type === "user"
            ? "message user-message"
            : "message ai-message";


    const roleElement =
        document.createElement(
            "div"
        );

    roleElement.className =
        "message-role";

    roleElement.textContent =
        role ?? "";


    const contentElement =
        document.createElement(
            "div"
        );

    contentElement.className =
        "message-content";

    contentElement.textContent =
        content ?? "";


    const timeElement =
        document.createElement(
            "div"
        );

    timeElement.className =
        "message-time";

    timeElement.textContent =
        formatDateTime(
            createdAt
        );


    wrapper.appendChild(
        roleElement
    );

    wrapper.appendChild(
        contentElement
    );

    wrapper.appendChild(
        timeElement
    );

    return wrapper;
}


function createEmptyMessage() {

    const element =
        document.createElement(
            "div"
        );

    element.id =
        "emptyMessage";

    element.className =
        "empty-message";

    element.textContent =
        "문서를 업로드한 다음 질문을 입력해 주세요.";

    return element;
}


function removeEmptyMessage() {

    const element =
        document.getElementById(
            "emptyMessage"
        );

    if (element) {
        element.remove();
    }
}


function formatDateTime(
    value
) {

    if (!value) {
        return "";
    }

    const date =
        new Date(value);

    if (Number.isNaN(
        date.getTime()
    )) {

        return value;
    }

    return date.toLocaleString(
        "ko-KR"
    );
}


function setLoading(
    loading
) {

    sendButton.disabled =
        loading;

    questionInput.disabled =
        loading;

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


async function readErrorMessage(
    response
) {

    try {

        const data =
            await response.json();

        return data.message
            || data.error
            || data.detail
            || "요청 처리 중 오류가 발생했습니다.";

    } catch (error) {

        try {

            const text =
                await response.text();

            if (text) {
                return text;
            }

        } catch (ignoredError) {

        }
4
        return "요청 처리 중 오류가 발생했습니다.";
    }
}


function createTemporaryId() {

    if (window.crypto
        && window.crypto.randomUUID) {

        return window.crypto.randomUUID();
    }

    return Date.now().toString()
        + "-"
        + Math.random()
            .toString(16)
            .substring(2);
}

async function clearConversations() {

    // 사용자가 실수로 삭제하지 않도록 확인창을 표시한다.
    const confirmed =
        window.confirm(
            "현재 로그인 사용자의 대화 내역을 모두 삭제하시겠습니까?\n"
            + "삭제한 대화는 복구할 수 없습니다.\n"
            + "업로드한 문서는 삭제되지 않습니다."
        );

    // 취소를 누르면 삭제 요청을 보내지 않는다.
    if (!confirmed) {
        return;
    }

    // 중복 클릭을 방지하기 위해 버튼을 비활성화한다.
    clearConversationButton.disabled =
        true;

    clearConversationButton.textContent =
        "삭제 중...";

    setStatus(
        "대화 내역을 삭제하고 있습니다."
    );

    try {

        // 현재 로그인 사용자의 대화 삭제 API를 호출한다.
        const response =
            await fetch(
                "/api/chat/messages",
                {
                    method: "DELETE",
                    headers: {
                        "Accept":
                            "application/json"
                    }
                }
            );

        // 로그인이 만료된 경우 로그인 화면으로 이동한다.
        if (response.status === 401
            || response.status === 403) {

            window.location.href =
                "/login";

            return;
        }

        // 삭제 요청이 실패하면 서버 오류 메시지를 읽는다.
        if (!response.ok) {

            const errorMessage =
                await readErrorMessage(
                    response
                );

            throw new Error(
                errorMessage
            );
        }

        // 현재 화면의 모든 대화 내용을 제거한다.
        messageList.innerHTML =
            "";

        // 대화가 없다는 안내 문구를 다시 표시한다.
        messageList.appendChild(
            createEmptyMessage()
        );

        setStatus(
            "대화 내역을 모두 삭제했습니다."
        );

        questionInput.focus();

    } catch (error) {

        setStatus(
            error.message,
            true
        );

    } finally {

        // 삭제 처리 완료 후 버튼을 다시 활성화한다.
        clearConversationButton.disabled =
            false;

        clearConversationButton.textContent =
            "대화 내역 지우기";
    }
}