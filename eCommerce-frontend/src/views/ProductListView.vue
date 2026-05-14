<template>
  <section class="panel">
    <h2>商品列表</h2>

    <p v-if="isLoading" class="status-message">商品資料載入中...</p>
    <p v-else-if="loadError" class="purchase-message">{{ loadError }}</p>

    <div v-else class="placeholder-grid">
      <article v-for="product in products" :key="product.Product_ID" class="product-card">
        <p><span>商品編號：</span>{{ product.Product_ID }}</p>
        <h3><span>商品名稱：</span>{{ product.Product_Name }}</h3>
        <strong><span>售價：</span>{{ product.Price }}</strong>

        <div class="quantity-row">
          <span class="quantity-label">數量</span>
          <div class="quantity-control">
            <button type="button" class="quantity-button" @click="decreaseQuantity(product)">
              -
            </button>
            <span class="quantity-value">{{ product.quantity }}</span>
            <button
              type="button"
              class="quantity-button"
              :disabled="product.quantity >= product.stock"
              @click="increaseQuantity(product)"
            >
              +
            </button>
          </div>
        </div>
      </article>
    </div>
    <p v-if="purchaseMessage" class="purchase-message">{{ purchaseMessage }}</p>
    <div class="actions">
      <button type="button" class="purchase-button" @click="handlePurchase">購買</button>
    </div>
  </section>
</template>

<script setup>
import axios from 'axios'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { useOrderStore } from '../stores/order'

const router = useRouter()
const orderStore = useOrderStore()
const purchaseMessage = ref('')
const loadError = ref('')
const isLoading = ref(false)
const products = ref([])

const formatPrice = (price) => `NT$ ${Number(price).toLocaleString('zh-TW')}`

const toProductViewModel = (product) => ({
  Product_ID: product.productId ?? product.ProductID ?? product.Product_ID,
  Product_Name: product.productName ?? product.ProductName ?? product.Product_Name,
  Price: formatPrice(product.price ?? product.Price),
  stock: product.quantity ?? product.Quantity ?? product.stock ?? 0,
  quantity: 0,
})

const loadProducts = async () => {
  isLoading.value = true
  loadError.value = ''

  try {
    const { data } = await axios.get('http://localhost:8080/api/products')
    products.value = data.map(toProductViewModel)
  } catch {
    loadError.value = '無法取得商品資料，請確認後端服務是否已啟動。'
  } finally {
    isLoading.value = false
  }
}

const increaseQuantity = (product) => {
  purchaseMessage.value = ''
  if (product.quantity < product.stock) {
    product.quantity += 1
  }
}

const decreaseQuantity = (product) => {
  purchaseMessage.value = ''
  if (product.quantity > 0) {
    product.quantity -= 1
  }
}

const handlePurchase = () => {
  const selectedProducts = products.value
    .filter((product) => product.quantity > 0)
    .map(({ Product_ID, Product_Name, Price, quantity }) => ({
      Product_ID,
      Product_Name,
      Price,
      quantity,
    }))

  if (selectedProducts.length === 0) {
    purchaseMessage.value = '請至少選擇一項商品再進行購買。'
    return
  }

  orderStore.setItems(selectedProducts)
  purchaseMessage.value = ''
  router.push({ name: 'order-detail' })
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.panel {
  padding: 2rem;
  border-radius: 24px;
  background: #fffdf8;
  box-shadow: 0 18px 40px rgba(31, 41, 51, 0.08);
}

.eyebrow {
  margin: 0 0 0.5rem;
  color: #9a6700;
  font-size: 0.85rem;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

h2 {
  margin: 0 0 0.5rem 0;
  font-size: 2rem;
}

.description {
  margin: 0.75rem 0 2rem;
  color: #52606d;
}

.placeholder-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem;
}

.product-card {
  padding: 1.25rem;
  border-radius: 18px;
  background: #f8f5ef;
  display: grid;
  gap: 0.75rem;
}

.quantity-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

.quantity-label {
  color: #52606d;
  font-weight: 600;
}

.quantity-control {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.35rem 0.5rem;
  border-radius: 999px;
  background: #fffdf8;
}

.quantity-button {
  width: 2rem;
  height: 2rem;
  border: none;
  border-radius: 50%;
  background: #d9c5a1;
  color: #3d2b1f;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
}

.quantity-value {
  min-width: 1.5rem;
  text-align: center;
  font-weight: 600;
  color: #3d2b1f;
}

.actions {
  display: flex;
  justify-content: flex-end;
}

.purchase-message {
  margin: 1rem 0 0;
  color: #b42318;
  font-weight: 600;
}

.status-message {
  margin: 1rem 0 0;
  color: #52606d;
  font-weight: 600;
}

.purchase-button {
  margin: 0.5rem 0 0 0;
  padding: 0.7rem 1.25rem;
  border: none;
  border-radius: 999px;
  background: #9a6700;
  color: #fff;
  font-weight: 700;
  cursor: pointer;
}

.product-card h3,
.product-card p,
.product-card strong {
  margin: 0;
}

.product-card p {
  color: #52606d;
}

.product-card span {
  margin-right: 0.35rem;
  color: #7c5a12;
  font-weight: 600;
}

.quantity-button:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}
</style>
