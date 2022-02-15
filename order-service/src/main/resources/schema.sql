DROP TABLE IF EXISTS `order`;

CREATE TABLE `order` (
    id VARCHAR(256),
    product VARCHAR(72),
    amount INTEGER,
    status VARCHAR(10),
    username VARCHAR(96),
    reason VARCHAR(512),
    address VARCHAR(256),
    PRIMARY KEY (id)
);