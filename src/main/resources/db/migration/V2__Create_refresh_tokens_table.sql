CREATE TABLE refresh_tokens(
       id BIGINT PRIMARY KEY AUTO_INCREMENT,
       token VARCHAR(512) UNIQUE NOT NULL,
       user_id BIGINT NOT NULL,
       expires_at TIMESTAMP NOT NULL,
       FOREIGN KEY (user_id) REFERENCES users(id)
)