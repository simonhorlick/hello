CREATE TABLE greetings (
  greeting_id BIGINT AUTO_INCREMENT,
  name VARCHAR(64),
  time_ns BIGINT,
  PRIMARY KEY (greeting_id, name)
)
