DROP TABLE IF EXISTS `inventory`;

CREATE TABLE `inventory` (
    id INTEGER NOT NULL AUTO_INCREMENT,
    product VARCHAR(256),
    amount INTEGER,
    price INTEGER,
    PRIMARY KEY (id)
);

INSERT INTO inventory (product, amount, price) VALUES ("Iphone", 200, 1000);