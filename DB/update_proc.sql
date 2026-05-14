SET NAMES utf8mb4;

DELIMITER //

DROP PROCEDURE IF EXISTS sp_InsertProduct //
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

DROP PROCEDURE IF EXISTS sp_InsertOrder //
CREATE PROCEDURE sp_InsertOrder(
    IN p_OrderID VARCHAR(50),
    IN p_MemberID VARCHAR(50),
    IN p_Price DECIMAL(10, 2),
    IN p_PayStatus TINYINT
)
BEGIN
    INSERT INTO Order_Main(OrderID, MemberID, Price, PayStatus)
    VALUES (p_OrderID, p_MemberID, p_Price, p_PayStatus);
END //

DROP PROCEDURE IF EXISTS sp_CreateOrderDetailAndReduceStock //
CREATE PROCEDURE sp_CreateOrderDetailAndReduceStock(
    IN p_OrderID VARCHAR(50),
    IN p_MemberID VARCHAR(50),
    IN p_ProductID VARCHAR(50),
    IN p_Quantity INT,
    IN p_PayStatus TINYINT
)
BEGIN
    DECLARE v_CurrentStock INT;
    DECLARE v_StandPrice DECIMAL(10, 2);
    DECLARE v_ItemPrice DECIMAL(10, 2);
    DECLARE v_OrderExists INT DEFAULT 0;

    IF p_Quantity IS NULL OR p_Quantity <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MYSQL_ERRNO = 10003,
            MESSAGE_TEXT = 'INVALID_QUANTITY';
    END IF;

    SELECT Quantity, Price
    INTO v_CurrentStock, v_StandPrice
    FROM Product
    WHERE ProductID = p_ProductID
    FOR UPDATE;

    IF v_CurrentStock IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MYSQL_ERRNO = 10002,
            MESSAGE_TEXT = 'PRODUCT_NOT_FOUND';
    END IF;

    IF v_CurrentStock < p_Quantity THEN
        SIGNAL SQLSTATE '45000'
        SET MYSQL_ERRNO = 10001,
            MESSAGE_TEXT = 'INSUFFICIENT_STOCK';
    END IF;

    SET v_ItemPrice = v_StandPrice * p_Quantity;

    SELECT COUNT(*)
    INTO v_OrderExists
    FROM Order_Main
    WHERE OrderID = p_OrderID;

    IF v_OrderExists = 0 THEN
        INSERT INTO Order_Main (OrderID, MemberID, Price, PayStatus)
        VALUES (p_OrderID, p_MemberID, v_ItemPrice, p_PayStatus);
    ELSE
        UPDATE Order_Main
        SET Price = Price + v_ItemPrice,
            PayStatus = p_PayStatus
        WHERE OrderID = p_OrderID;
    END IF;

    INSERT INTO Order_Detail (OrderID, ProductID, Quantity, StandPrice, ItemPrice)
    VALUES (p_OrderID, p_ProductID, p_Quantity, v_StandPrice, v_ItemPrice);

    UPDATE Product
    SET Quantity = Quantity - p_Quantity
    WHERE ProductID = p_ProductID;
END //

DELIMITER ;
