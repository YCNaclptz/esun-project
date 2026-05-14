SET NAMES utf8mb4;

-- 匯入商品範例
INSERT INTO Product (ProductID, ProductName, Price, Quantity) VALUES
('P001', 'osii舒壓按摩椅', 98000, 5),
('P002', '網友最愛起司蛋糕', 1200, 50),
('P003', '真愛密碼項鍊', 8500, 20);

-- 匯入訂單主檔範例
INSERT INTO Order_Main (OrderID, MemberID, Price, PayStatus) VALUES
('Ms20250801186230', '458', 98000, 1),
('Ms20250805157824', '55688', 9700, 0),
('Ms20250805258200', '1713', 2400, 1);

-- 匯入訂單明細範例
INSERT INTO Order_Detail (OrderID, ProductID, Quantity, StandPrice, ItemPrice) VALUES
('Ms20250801186230', 'P001', 1, 98000, 98000),
('Ms20250805157824', 'P002', 1, 1200, 1200),
('Ms20250805157824', 'P003', 1, 8500, 8500),
('Ms20250805258200', 'P002', 2, 1200, 2400);
