CREATE TABLE dbo.app_user
(
    id              BIGINT IDENTITY (1,1) NOT NULL,
    department_id   VARCHAR(255) NOT NULL,
    organization_id VARCHAR(255) NOT NULL,
    password        VARCHAR(255) NOT NULL,
    role            VARCHAR(255) NOT NULL,
    security_level  INT NOT NULL,
    username        VARCHAR(255) NOT NULL,

    CONSTRAINT pk_app_user
        PRIMARY KEY (id),

    CONSTRAINT uk_app_user_username
        UNIQUE (username)
);

CREATE TABLE dbo.conversation
(
    id         BIGINT IDENTITY (1,1) NOT NULL,
    answer     VARCHAR(MAX) NOT NULL,
    created_at DATETIME2 NOT NULL,
    question   VARCHAR(4000) NOT NULL,
    user_id    BIGINT NOT NULL,
    username   VARCHAR(255) NOT NULL,

    CONSTRAINT pk_conversation
        PRIMARY KEY (id)
);