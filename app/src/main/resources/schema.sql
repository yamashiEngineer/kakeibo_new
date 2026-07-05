-- 起動のたびに古いテーブルを削除して作り直す場合
DROP TABLE IF EXISTS products;

--users（ユーザー）テーブルの作成
CREATE TABLE users (
                       id         BIGINT        NOT NULL AUTO_INCREMENT,
                       email      VARCHAR(255)  NOT NULL,
                       password   VARCHAR(255)  NOT NULL,
                       name       VARCHAR(100)  NOT NULL,
                       created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       PRIMARY KEY (id),
                       UNIQUE KEY uq_users_email (email)
);

--categories（カテゴリ）テーブルの作成
CREATE TABLE categories (
                            id      BIGINT       NOT NULL AUTO_INCREMENT,
                            user_id BIGINT       NOT NULL,
                            name    VARCHAR(50)  NOT NULL,
                            PRIMARY KEY (id),
                            CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users (id)
);

--transactions（収支）テーブルの作成
CREATE TABLE transactions (
                              id          BIGINT        NOT NULL AUTO_INCREMENT,
                              user_id     BIGINT        NOT NULL,
                              amount      INT           NOT NULL,
                              category_id BIGINT        NOT NULL,
                              memo        VARCHAR(200),
                              txn_date    DATE          NOT NULL,
                              type        VARCHAR(10)   NOT NULL,
                              created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (id),
                              CONSTRAINT fk_transactions_user     FOREIGN KEY (user_id)     REFERENCES users (id),
                              CONSTRAINT fk_transactions_category FOREIGN KEY (category_id) REFERENCES categories (id)
);
