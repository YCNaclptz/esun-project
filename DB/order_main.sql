CREATE DATABASE IF NOT EXISTS esun_db;
USE esun_db;

CREATE TABLE Order_Main (
    OrderID     VARCHAR(50) PRIMARY KEY COMMENT '訂單編號',
    MemberID    VARCHAR(50) NOT NULL    COMMENT '會員編號',
    Price       DECIMAL(10, 2) NOT NULL COMMENT '訂單總金額',
    PayStatus   TINYINT(1) DEFAULT 0    COMMENT '付款狀態(0未付/1已付)'
);
