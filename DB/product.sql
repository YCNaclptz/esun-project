SET NAMES utf8mb4;

CREATE TABLE Product (
    ProductID   VARCHAR(50) PRIMARY KEY COMMENT '商品編號',
    ProductName VARCHAR(255) NOT NULL   COMMENT '商品名稱',
    Price       DECIMAL(10, 2) NOT NULL COMMENT '售價',
    Quantity    INT NOT NULL            COMMENT '庫存'
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
