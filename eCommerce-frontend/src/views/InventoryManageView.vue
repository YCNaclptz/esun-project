<script setup>
import { onMounted, ref } from 'vue'

import api, { isAxiosError } from '../services/api'

const products = ref([])
const isLoading = ref(false)
const loadError = ref('')

const formatPrice = (price) => `NT$ ${Number(price).toLocaleString('zh-TW')}`

const toProductViewModel = (product) => {
  const quantity = Number(product.quantity ?? product.Quantity ?? product.stock ?? 0)

  return {
    productId: product.productId ?? product.ProductID ?? product.Product_ID,
    productName: product.productName ?? product.ProductName ?? product.Product_Name,
    price: Number(product.price ?? product.Price ?? 0),
    quantity,
    setQuantity: String(quantity),
    delta: '',
    isSetting: false,
    isAdjusting: false,
    message: '',
    messageType: '',
  }
}

const clearRowMessage = (product) => {
  product.message = ''
  product.messageType = ''
}

const setRowError = (product, message) => {
  product.message = message
  product.messageType = 'error'
}

const setRowSuccess = (product, message) => {
  product.message = message
  product.messageType = 'success'
}

const parseInteger = (value, allowSigned = false) => {
  const normalizedValue = String(value).trim()
  const integerPattern = allowSigned ? /^[+-]?\d+$/ : /^\d+$/

  if (!integerPattern.test(normalizedValue)) {
    return null
  }

  const parsedValue = Number(normalizedValue)
  return Number.isSafeInteger(parsedValue) ? parsedValue : null
}

const resolveErrorMessage = (error) => {
  if (isAxiosError(error)) {
    const apiMessage = error.response?.data?.message

    if (apiMessage === 'product not found') {
      return '找不到商品，請重新載入後再試。'
    }

    if (apiMessage === 'invalid quantity') {
      return '庫存數量不合法，請輸入有效整數。'
    }

    if (apiMessage === 'insufficient stock') {
      return '扣減後庫存不可小於 0。'
    }

    if (typeof apiMessage === 'string' && apiMessage.length > 0) {
      return `庫存更新失敗：${apiMessage}`
    }
  }

  return '庫存更新失敗，請確認後端服務是否已啟動。'
}

const applyUpdatedProduct = (product, updatedProduct) => {
  const nextProduct = toProductViewModel(updatedProduct)
  product.productName = nextProduct.productName
  product.price = nextProduct.price
  product.quantity = nextProduct.quantity
  product.setQuantity = nextProduct.setQuantity
  product.delta = ''
}

const loadProducts = async () => {
  isLoading.value = true
  loadError.value = ''

  try {
    const { data } = await api.get('/api/products')
    products.value = data.map(toProductViewModel)
  } catch {
    loadError.value = '無法取得商品資料，請確認後端服務是否已啟動。'
  } finally {
    isLoading.value = false
  }
}

const saveSetQuantity = async (product) => {
  if (product.isSetting || product.isAdjusting) {
    return
  }

  clearRowMessage(product)
  const quantity = parseInteger(product.setQuantity)

  if (quantity === null) {
    setRowError(product, '設定總量需為 0 以上整數。')
    return
  }

  product.isSetting = true

  try {
    const { data } = await api.put(`/api/products/${encodeURIComponent(product.productId)}/quantity`, {
      quantity,
    })
    applyUpdatedProduct(product, data)
    setRowSuccess(product, '庫存總量已更新。')
  } catch (error) {
    setRowError(product, resolveErrorMessage(error))
  } finally {
    product.isSetting = false
  }
}

const saveDelta = async (product) => {
  if (product.isSetting || product.isAdjusting) {
    return
  }

  clearRowMessage(product)
  const delta = parseInteger(product.delta, true)

  if (delta === null || delta === 0) {
    setRowError(product, '調整量需為非 0 整數。')
    return
  }

  if (product.quantity + delta < 0) {
    setRowError(product, '扣減後庫存不可小於 0。')
    return
  }

  product.isAdjusting = true

  try {
    const { data } = await api.post(
      `/api/products/${encodeURIComponent(product.productId)}/quantity-adjustments`,
      { delta },
    )
    applyUpdatedProduct(product, data)
    setRowSuccess(product, '庫存調整已更新。')
  } catch (error) {
    setRowError(product, resolveErrorMessage(error))
  } finally {
    product.isAdjusting = false
  }
}

const isProductSaving = (product) => product.isSetting || product.isAdjusting

onMounted(() => {
  loadProducts()
})
</script>

<template>
  <section class="panel">
    <div class="heading-row">
      <div>
        <h2>庫存管理</h2>
        <p class="description">逐筆設定商品庫存總量，或輸入正負調整量進行補貨與扣減。</p>
      </div>

      <button type="button" class="secondary-button" :disabled="isLoading" @click="loadProducts">
        {{ isLoading ? '重新整理中...' : '重新整理' }}
      </button>
    </div>

    <p v-if="isLoading" class="status-message">商品庫存載入中...</p>
    <p v-else-if="loadError" class="error-message">{{ loadError }}</p>

    <div v-else-if="products.length === 0" class="empty-state">
      <p>目前沒有商品可管理，請先新增商品。</p>
    </div>

    <div v-else class="inventory-list">
      <article v-for="product in products" :key="product.productId" class="inventory-row">
        <div class="product-cell">
          <p class="product-id">商品編號：{{ product.productId }}</p>
          <h3>{{ product.productName }}</h3>
          <p class="product-price">售價：{{ formatPrice(product.price) }}</p>
        </div>

        <div class="stock-cell">
          <span>目前庫存</span>
          <strong>{{ product.quantity }}</strong>
        </div>

        <form class="control-cell" @submit.prevent="saveSetQuantity(product)">
          <label>
            設定總量
            <input
              v-model="product.setQuantity"
              type="number"
              min="0"
              step="1"
              :disabled="isProductSaving(product)"
              @input="clearRowMessage(product)"
            />
          </label>

          <button type="submit" :disabled="isProductSaving(product)">
            {{ product.isSetting ? '儲存中...' : '設定' }}
          </button>
        </form>

        <form class="control-cell" @submit.prevent="saveDelta(product)">
          <label>
            增加/扣減
            <input
              v-model.trim="product.delta"
              type="text"
              inputmode="numeric"
              placeholder="+10 或 -3"
              :disabled="isProductSaving(product)"
              @input="clearRowMessage(product)"
            />
          </label>

          <button type="submit" :disabled="isProductSaving(product)">
            {{ product.isAdjusting ? '儲存中...' : '調整' }}
          </button>
        </form>

        <p v-if="product.message" :class="['row-message', product.messageType]">
          {{ product.message }}
        </p>
      </article>
    </div>
  </section>
</template>

<style scoped>
.panel {
  padding: 2rem;
  border-radius: 24px;
  background: #fffdf8;
  box-shadow: 0 18px 40px rgba(31, 41, 51, 0.08);
}

.heading-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

h2,
h3,
p {
  margin: 0;
}

h2 {
  font-size: 2rem;
}

.description {
  margin-top: 0.5rem;
  color: #52606d;
}

.inventory-list {
  display: grid;
  gap: 1rem;
}

.inventory-row {
  display: grid;
  grid-template-columns: minmax(180px, 1.4fr) minmax(120px, 0.45fr) minmax(180px, 0.85fr) minmax(
      180px,
      0.85fr
    );
  align-items: end;
  gap: 1rem;
  padding: 1.25rem;
  border-radius: 18px;
  background: #f8f5ef;
}

.product-cell {
  display: grid;
  gap: 0.35rem;
}

.product-id,
.product-price {
  color: #52606d;
  font-weight: 600;
}

.stock-cell {
  display: grid;
  gap: 0.35rem;
  padding: 0.8rem 1rem;
  border-radius: 16px;
  background: #fffdf8;
}

.stock-cell span {
  color: #7c5a12;
  font-size: 0.9rem;
  font-weight: 700;
}

.stock-cell strong {
  color: #1f2933;
  font-size: 1.45rem;
}

.control-cell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: end;
  gap: 0.75rem;
}

label {
  display: grid;
  gap: 0.45rem;
  color: #334e68;
  font-weight: 700;
}

input {
  width: 100%;
  min-width: 0;
  padding: 0.75rem 0.85rem;
  border: 1px solid #d9e2ec;
  border-radius: 14px;
  background: #fff;
  color: #1f2933;
  font: inherit;
}

input:disabled {
  background: #edf2f7;
  color: #627d98;
}

button {
  padding: 0.75rem 1rem;
  border: 0;
  border-radius: 999px;
  background: #9a6700;
  color: #fff;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
}

button:disabled {
  opacity: 0.65;
  cursor: wait;
}

.secondary-button {
  background: #e2e8f0;
  color: #243b53;
}

.status-message,
.error-message {
  margin-top: 1rem;
  font-weight: 600;
}

.status-message {
  color: #52606d;
}

.error-message,
.row-message.error {
  color: #b42318;
}

.row-message {
  grid-column: 1 / -1;
  font-weight: 700;
}

.row-message.success {
  color: #166534;
}

.empty-state {
  padding: 1.5rem;
  border-radius: 18px;
  background: #f8f5ef;
  color: #52606d;
  font-weight: 600;
}

@media (max-width: 1100px) {
  .inventory-row {
    grid-template-columns: minmax(0, 1fr) minmax(120px, 180px);
  }

  .control-cell {
    grid-column: 1 / -1;
  }
}

@media (max-width: 768px) {
  .heading-row,
  .inventory-row,
  .control-cell {
    grid-template-columns: 1fr;
  }

  .heading-row {
    align-items: stretch;
  }

  .stock-cell {
    grid-template-columns: 1fr auto;
    align-items: center;
  }
}
</style>
