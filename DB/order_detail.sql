CREATE DATABASE IF NOT EXISTS esun_db;
USE esun_db;

CREATE TABLE Order_Detail (
    OrderItemSN INT AUTO_INCREMENT PRIMARY KEY COMMENT '訂單明細流水號',
    OrderID     VARCHAR(50) NOT NULL           COMMENT '訂單編號',
    ProductID   VARCHAR(50) NOT NULL           COMMENT '商品編號',
    Quantity    INT NOT NULL                   COMMENT '數量',
    StandPrice  DECIMAL(10, 2) NOT NULL        COMMENT '單價',
    ItemPrice   DECIMAL(10, 2) NOT NULL        COMMENT '單品項總價',
    FOREIGN KEY (OrderID) REFERENCES Order_Main(OrderID),
    FOREIGN KEY (ProductID) REFERENCES Product(ProductID)
);