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
├── docker-compose.yml          # 前端、後端、MySQL Docker Compose 設定
├── DB/                         # SQL 初始化腳本與測試資料
│   ├── Dockerfile              # 內建 SQL 初始化檔的 MySQL image
│   ├── product.sql             # Product 表
│   ├── order_main.sql          # Order_Main 表
│   ├── order_detail.sql        # Order_Detail 表與外鍵
│   ├── update_proc.sql         # 商品、訂單與庫存相關預存程序
│   └── db_test_data.sql        # 預設測試資料
├── eCommerce-backend/          # Spring Boot 後端 API
└── eCommerce-frontend/         # Vue 3 前端
```

## 快速開始

請先確認本機已安裝 Docker / Docker Compose。後端會在 Maven + Temurin JDK 21 容器中打包並使用 JRE 21 執行，前端會在 Node 22 容器中執行，因此本機不需要另外安裝 JDK、Node，或設定 `JAVA_HOME`。

在專案根目錄先建立 `.env`，再啟動服務：

```bash
cp .env.example .env
docker compose up --build
```

Compose 會啟動：

| 服務 | URL / Port |
|------|------------|
| 前端 | http://127.0.0.1:5173 |
| 後端 | http://127.0.0.1:8080 |
| MySQL | 127.0.0.1:3306 |

進入 `/login` 後輸入 5 位以下數字帳號即可登入。登入狀態只存在前端記憶體中，重新整理頁面後需要重新登入。

## 環境變數

`docker-compose.yml` 不提供 fallback 值，所有 compose 變數都必須由 `.env` 提供。可先複製範例檔，再依需要調整：

```bash
cp .env.example .env
```

可調整的變數：

| 變數 | 範例值 | 說明 |
|------|--------|------|
| MYSQL_DATABASE | esun_db | MySQL database 名稱 |
| MYSQL_ROOT_PASSWORD | password123 | MySQL root 密碼 |
| SPRING_DATASOURCE_USERNAME | root | 後端連線 MySQL 使用者 |
| MYSQL_PORT | 3306 | 對外 MySQL port |
| BACKEND_PORT | 8080 | 對外後端 port |
| FRONTEND_PORT | 5173 | 對外前端 port |
| VITE_API_BASE_URL | http://127.0.0.1:8080 | 瀏覽器呼叫後端 API 的 base URL |

## 資料庫初始化

首次建立 MySQL volume 時，Compose 會依序匯入：

1. `DB/product.sql`
2. `DB/order_main.sql`
3. `DB/order_detail.sql`
4. `DB/update_proc.sql`
5. `DB/db_test_data.sql`

SQL 匯入流程已固定使用 `utf8mb4`，可避免預設假資料在網頁中出現亂碼。

若要重新建立資料庫並重跑初始化腳本：

```bash
docker compose down -v
docker compose up --build
```

## 本機直接執行（選用）

如果仍想使用本機 JDK/Node，需先自行提供後端與前端環境變數。

後端：

```powershell
cd eCommerce-backend
$env:SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:3306/esun_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Taipei&useUnicode=true&characterEncoding=utf8"
$env:SPRING_DATASOURCE_USERNAME="root"
$env:SPRING_DATASOURCE_PASSWORD="password123"
./mvnw spring-boot:run
```

前端：

```powershell
cd eCommerce-frontend
$env:VITE_API_BASE_URL="http://127.0.0.1:8080"
npm install
npm run dev
```

## 常用開發指令

### Docker Compose

```bash
docker compose up --build
docker compose ps
docker compose logs -f backend
docker compose logs -f frontend
docker compose down
```

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

如果 `docker compose up --build` 失敗並提示 port 已被使用，代表本機可能已有 MySQL 在使用 `3306`。可在 `.env` 調整 `MYSQL_PORT`。

### 預設資料沒有重新匯入

MySQL 官方 image 只會在資料目錄是空的時候執行 `/docker-entrypoint-initdb.d`。若要重新匯入 schema 與測試資料，請移除 volume 後重啟：

```bash
docker compose down -v
docker compose up --build
```

### 後端啟動後無法連線資料庫

請確認：

1. `esun-mysql` 容器正在執行且狀態為 healthy。
2. `SPRING_DATASOURCE_URL` 使用 compose 內部主機名 `db`。
3. `.env` 中的 `MYSQL_ROOT_PASSWORD` 與後端 `SPRING_DATASOURCE_PASSWORD` 一致。

### 前端顯示無法取得商品資料

請確認後端已在 `http://127.0.0.1:8080` 啟動，並先用以下指令確認 API：

```bash
curl http://127.0.0.1:8080/api/products
```
