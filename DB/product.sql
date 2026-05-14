CREATE DATABASE IF NOT EXISTS esun_db;
USE esun_db;

CREATE TABLE Product (
    ProductID   VARCHAR(50) PRIMARY KEY COMMENT '商品編號',
    ProductName VARCHAR(255) NOT NULL   COMMENT '商品名稱',
    Price       DECIMAL(10, 2) NOT NULL COMMENT '售價',
    Quantity    INT NOT NULL            COMMENT '庫存' 
);

DROP PROCEDURE IF EXISTS sp_InsertProduct;

DELIMITER //
CREATE PROCEDURE sp_InsertProduct(
    IN p_ProductID VARCHAR(50),
    IN p_ProductName VARCHAR(255),
    IN p_Price DECIMAL(10, 2),
    IN p_Quantity INT
)
BEGIN
    INSERT INTO Product (ProductID, ProductName, Price, Quantity)
    VALUES (p_ProductID, p_ProductName, p_Price, p_Quantity);
END //
DELIMITER ;