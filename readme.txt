# eSun eCommerce 電商平台

全端電商應用程式，具備會員登入、商品管理及訂單處理功能。

---

## 技術架構

| 層級 | 技術 |
|------|------|
| 後端 | Spring Boot 4.0.6 (Java 21)、Spring Data JDBC |
| 前端 | Vue 3.5、Vue Router 5、Pinia、Vite、Axios |
| 資料庫 | MySQL 8.0 |
| 容器化 | Docker Compose |

---

## 專案結構

```
esun-project/
├── docker-compose.yml          # MySQL Docker 設定
├── DB/                         # SQL 初始化腳本
│   ├── product.sql
│   ├── order_main.sql
│   ├── order_detail.sql
│   ├── update_proc.sql         # 預存程序
│   └── db_test_data.sql        # 測試資料
├── db_data/                    # MySQL 資料持久化目錄
├── eCommerce-backend/          # Spring Boot 後端
└── eCommerce-frontend/         # Vue 3 前端
```

---

## 快速開始

### 1. 啟動資料庫

```bash
docker-compose up -d
```

### 2. 初始化資料庫（依序執行）

```sql
1. DB/product.sql
2. DB/order_main.sql
3. DB/order_detail.sql
4. DB/update_proc.sql
5. DB/db_test_data.sql   -- 選用：匯入測試資料
```

### 3. 啟動後端

```bash
cd eCommerce-backend
./mvnw spring-boot:run
```

後端服務預設啟動於 `http://localhost:8080`

### 4. 啟動前端

```bash
cd eCommerce-frontend
npm install
npm run dev
```

前端服務預設啟動於 `http://localhost:5173`

---

## 資料庫設定

| 項目 | 值 |
|------|----|
| Host | localhost |
| Port | 3306 |
| Database | esun_db |
| Username | root |
| Password | password123 |

---

## API 文件

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | `/api/products` | 取得所有商品 |
| GET | `/api/products/{productId}` | 取得單一商品 |
| POST | `/api/products` | 新增商品 |
| POST | `/api/orders` | 建立訂單 |

### 新增商品 Request Body

```json
{
  "productId": "P001",
  "productName": "商品名稱",
  "price": 100.00,
  "quantity": 10
}
```

### 建立訂單 Request Body

```json
{
  "memberId": "12345",
  "payStatus": 0,
  "orderDetail": [
    {
      "productId": "P001",
      "quantity": 2
    }
  ]
}
```

---

## 資料庫 Schema

### Product（商品）

| 欄位 | 型別 | 說明 |
|------|------|------|
| ProductID | VARCHAR(50) PK | 商品編號（如 P001） |
| ProductName | VARCHAR(255) | 商品名稱 |
| Price | DECIMAL(10,2) | 單價 |
| Quantity | INT | 庫存數量 |

### Order_Main（訂單主檔）

| 欄位 | 型別 | 說明 |
|------|------|------|
| OrderID | VARCHAR(50) PK | 訂單編號（如 ORD-xxxxx） |
| MemberID | VARCHAR(50) | 會員編號 |
| Price | DECIMAL(10,2) | 訂單總金額 |
| PayStatus | TINYINT(1) | 付款狀態（0=未付、1=已付） |

### Order_Detail（訂單明細）

| 欄位 | 型別 | 說明 |
|------|------|------|
| OrderItemSN | INT PK AUTO_INCREMENT | 明細序號 |
| OrderID | VARCHAR(50) FK | 對應訂單主檔 |
| ProductID | VARCHAR(50) FK | 對應商品 |
| Quantity | INT | 購買數量 |
| StandPrice | DECIMAL(10,2) | 下單時單價 |
| ItemPrice | DECIMAL(10,2) | 小計（Quantity × StandPrice） |

---

## 前端頁面

| 路由 | 頁面 | 說明 |
|------|------|------|
| `/login` | 登入頁 | 輸入5位數字會員編號登入 |
| `/products` | 商品列表 | 瀏覽商品、選擇數量、加入訂單 |
| `/products/new` | 新增商品 | 建立新商品資料 |
| `/orders/detail` | 訂單明細 | 確認訂單內容並送出 |

---

## 主要功能

- 會員登入（數字帳號驗證）
- 商品瀏覽與建立
- 購物車（數量加減）
- 訂單建立與庫存自動扣減
- 表單驗證與錯誤處理
- 交易安全（預存程序確保資料一致性）
