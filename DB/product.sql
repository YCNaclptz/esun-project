CREATE DATABASE IF NOT EXISTS esun_db;
USE esun_db;

CREATE TABLE Product (
    ProductID   VARCHAR(50) PRIMARY KEY COMMENT '商品編號',
    ProductName VARCHAR(255) NOT NULL   COMMENT '商品名稱',
    Price       DECIMAL(10, 2) NOT NULL COMMENT '售價',
    Quantity    INT NOT NULL            COMMENT '庫存' 
);