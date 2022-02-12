DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
    id INTEGER NOT NULL AUTO_INCREMENT,
    uuid VARCHAR(256),
    product VARCHAR(72),
    amount INTEGER,
    status VARCHAR(10),
    username VARCHAR(96),
    PRIMARY KEY (id)
);