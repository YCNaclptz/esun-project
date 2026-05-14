# eSun eCommerce 電商平台

全端電商應用程式，提供會員登入、商品瀏覽與新增、購物車數量選擇、訂單建立，以及建立訂單時自動扣減庫存的流程。

## 技術架構

| 層級 | 技術 |
|------|------|
| 後端 | Spring Boot 4.0.6、Java 21、Spring Web MVC、Spring Data JDBC |
| 前端 | Vue 3.5、Vue Router 5、Pinia、Vite、Axios |
| 資料庫 | MySQL 8.0 |
| 容器化 | Docker Compose |

## 專案結構

```text
esun-project/
├── docker-compose.yml          # MySQL Docker 設定
├── DB/                         # SQL 初始化腳本與測試資料
│   ├── product.sql             # Product 表與 sp_InsertProduct
│   ├── order_main.sql          # Order_Main 表
│   ├── order_detail.sql        # Order_Detail 表與外鍵
│   ├── update_proc.sql         # 訂單與庫存相關預存程序
│   └── db_test_data.sql        # 選用測試資料
├── eCommerce-backend/          # Spring Boot 後端 API
└── eCommerce-frontend/         # Vue 3 前端
```

## 安裝前準備

請先確認本機已安裝以下工具：

| 工具 | 建議版本 | 確認指令 |
|------|----------|----------|
| Docker / Docker Compose | 可執行 MySQL 8.0 容器即可 | `docker --version`、`docker compose version` |
| Java | 21 | `java -version` |
| Node.js | `^20.19.0` 或 `>=22.12.0` | `node -v` |
| npm | 隨 Node.js 安裝 | `npm -v` |

Windows 使用者建議先啟動 Docker Desktop，再執行以下指令。後端 Maven Wrapper 已包含在 `eCommerce-backend/`，不需要另外安裝 Maven。

## 完整安裝與啟動流程

### 1. 啟動 MySQL

在專案根目錄執行：

```bash
docker-compose up -d
```

啟動後確認容器狀態：

```bash
docker ps --filter name=esun-mysql
```

資料庫設定會與後端 `application.properties` 對應：

| 項目 | 值 |
|------|----|
| Host | `localhost` |
| Port | `3306` |
| Database | `esun_db` |
| Username | `root` |
| Password | `password123` |

### 2. 初始化資料庫

第一次啟動資料庫後，依序匯入 SQL。請在專案根目錄執行：

```bash
docker exec -i esun-mysql mysql -uroot -ppassword123 < DB/product.sql
docker exec -i esun-mysql mysql -uroot -ppassword123 < DB/order_main.sql
docker exec -i esun-mysql mysql -uroot -ppassword123 < DB/order_detail.sql
docker exec -i esun-mysql mysql -uroot -ppassword123 < DB/update_proc.sql
```

如果需要範例商品與訂單資料，再執行：

```bash
docker exec -i esun-mysql mysql -uroot -ppassword123 esun_db < DB/db_test_data.sql
```

確認資料已建立：

```bash
docker exec -it esun-mysql mysql -uroot -ppassword123 -e "USE esun_db; SHOW TABLES; SELECT * FROM Product;"
```

預期會看到 `Product`、`Order_Main`、`Order_Detail` 三張表；若有匯入測試資料，`Product` 會有商品資料。

### 3. 啟動後端

開啟另一個終端機，執行：

```bash
cd eCommerce-backend
./mvnw spring-boot:run
```

後端預設啟動於：

```text
http://localhost:8080
```

確認 API 可連線：

```bash
curl http://localhost:8080/api/products
```

如果已匯入測試資料，應回傳商品 JSON；如果沒有匯入測試資料，回傳空陣列也代表後端與資料庫連線成功。

### 4. 啟動前端

開啟第三個終端機，執行：

```bash
cd eCommerce-frontend
npm install
npm run dev
```

前端預設啟動於：

```text
http://localhost:5173
```

進入 `/login` 後輸入 5 位以下數字帳號即可登入。登入狀態只存在前端記憶體中，重新整理頁面後需要重新登入。

## 常用開發指令

### 後端

```bash
cd eCommerce-backend
./mvnw spring-boot:run
./mvnw test
./mvnw -Dtest=ECommerceBackendApplicationTests test
./mvnw -Dtest=ECommerceBackendApplicationTests#contextLoads test
./mvnw package
```

### 前端

```bash
cd eCommerce-frontend
npm run dev
npm run build
npm run lint
npm run format
```

`npm run lint` 會執行 `oxlint . --fix` 與 `eslint . --fix --cache`，可能會直接修改前端檔案。

## 資料庫與交易流程

資料表初始化順序必須是：

1. `DB/product.sql`
2. `DB/order_main.sql`
3. `DB/order_detail.sql`
4. `DB/update_proc.sql`
5. `DB/db_test_data.sql`，選用

訂單建立不直接用 ORM 寫入多張表，而是透過 `sp_CreateOrderDetailAndReduceStock` 預存程序處理：

1. 讀取商品庫存與價格，並使用 `SELECT ... FOR UPDATE` 鎖定商品列。
2. 檢查購買數量、商品是否存在、庫存是否足夠。
3. 建立或更新 `Order_Main`。
4. 寫入 `Order_Detail`。
5. 扣減 `Product.Quantity`。

後端 `OrderService` 會將預存程序的 MySQL 錯誤碼轉成 HTTP 狀態：

| MySQL 錯誤碼 | 情境 | HTTP 狀態 |
|--------------|------|-----------|
| `10001` | 庫存不足 | `409 Conflict` |
| `10002` | 商品不存在 | `404 Not Found` |
| `10003` | 數量不合法 | `400 Bad Request` |

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

`productId` 需符合 `P001` 這類格式。前端新增商品頁會讀取既有商品後自動產生下一個商品編號。

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

## 資料庫 Schema

### Product（商品）

| 欄位 | 型別 | 說明 |
|------|------|------|
| ProductID | VARCHAR(50) PK | 商品編號，如 `P001` |
| ProductName | VARCHAR(255) | 商品名稱 |
| Price | DECIMAL(10,2) | 單價 |
| Quantity | INT | 庫存數量 |

### Order_Main（訂單主檔）

| 欄位 | 型別 | 說明 |
|------|------|------|
| OrderID | VARCHAR(50) PK | 訂單編號 |
| MemberID | VARCHAR(50) | 會員編號 |
| Price | DECIMAL(10,2) | 訂單總金額 |
| PayStatus | TINYINT(1) | 付款狀態，`0` 未付、`1` 已付 |

### Order_Detail（訂單明細）

| 欄位 | 型別 | 說明 |
|------|------|------|
| OrderItemSN | INT PK AUTO_INCREMENT | 明細序號 |
| OrderID | VARCHAR(50) FK | 對應訂單主檔 |
| ProductID | VARCHAR(50) FK | 對應商品 |
| Quantity | INT | 購買數量 |
| StandPrice | DECIMAL(10,2) | 下單時單價 |
| ItemPrice | DECIMAL(10,2) | 小計，`Quantity × StandPrice` |

## 前端頁面

| 路由 | 頁面 | 說明 |
|------|------|------|
| `/login` | 登入頁 | 輸入 5 位以下數字會員編號登入 |
| `/products` | 商品列表 | 瀏覽商品、選擇數量、加入訂單 |
| `/products/new` | 新增商品 | 建立新商品資料 |
| `/orders/detail` | 訂單明細 | 確認訂單內容並送出 |

## 主要功能

- 會員登入，帳號僅允許數字輸入。
- 商品瀏覽與新增。
- 購買數量加減與庫存上限限制。
- 訂單明細確認與建立。
- 建立訂單時透過預存程序扣減庫存，避免庫存不一致。
- 前後端表單驗證與錯誤訊息顯示。

## 疑難排解

### MySQL 3306 連線埠已被使用

如果 `docker-compose up -d` 失敗並提示 port 已被使用，代表本機可能已有 MySQL 在使用 `3306`。請先停止本機 MySQL，或調整 `docker-compose.yml` 的對外 port，並同步更新後端 `application.properties`。

### SQL 匯入失敗或資料表已存在

這些 SQL 腳本主要提供第一次初始化使用。若需要重建乾淨資料庫，可以先停止容器並移除資料目錄：

```bash
docker-compose down
rm -rf db_data
docker-compose up -d
```

再重新執行資料庫初始化步驟。

### 後端啟動後無法連線資料庫

請確認：

1. `esun-mysql` 容器正在執行。
2. `application.properties` 的帳號密碼與 `docker-compose.yml` 一致。
3. 已執行 `DB/product.sql` 建立 `esun_db`。

### 前端顯示無法取得商品資料

請確認後端已在 `http://localhost:8080` 啟動，並先用以下指令確認 API：

```bash
curl http://localhost:8080/api/products
```

### Java 或 Node.js 版本不符

後端需要 Java 21。前端 `package.json` 指定 Node.js `^20.19.0 || >=22.12.0`。版本不符時，請切換到相容版本後重新執行後端或 `npm install`。
