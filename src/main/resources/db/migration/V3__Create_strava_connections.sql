CREATE TABLE strava_connections (
       id BIGINT PRIMARY KEY AUTO_INCREMENT,
       user_id BIGINT NOT NULL UNIQUE,
       access_token VARCHAR(512) NOT NULL,
       refresh_token VARCHAR(512) NOT NULL,
       expires_at BIGINT,
       FOREIGN KEY (user_id) REFERENCES users(id)
   );