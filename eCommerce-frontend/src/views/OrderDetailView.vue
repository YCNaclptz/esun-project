<script setup>
import { computed } from 'vue'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

import api, { isAxiosError } from '../services/api'
import { useAuthStore } from '../stores/auth'
import { useOrderStore } from '../stores/order'

const orderStore = useOrderStore()
const authStore = useAuthStore()
const router = useRouter()
const submitError = ref('')
const submitMessage = ref('')
const isSubmitting = ref(false)

const parsePrice = (price) => Number(price.replace(/[^\d]/g, ''))

const orderItems = computed(() => orderStore.items)

const totalAmount = computed(() => {
  return orderItems.value.reduce((sum, item) => sum + parsePrice(item.Price) * item.quantity, 0)
})

const displayOrderId = computed(() => orderStore.orderId || '尚未建立')

const displayTotalAmount = computed(() => {
  const savedTotal = Number(orderStore.totalPrice)
  return Number.isFinite(savedTotal) ? savedTotal : totalAmount.value
})

const formatCurrency = (amount) => `NT$ ${amount.toLocaleString('zh-TW')}`

const goBackToProducts = () => {
  router.push({ name: 'products' })
}

const buildOrderPayload = () => ({
  memberId: authStore.account.value,
  payStatus: 0,
  orderDetail: orderItems.value.map((item) => ({
    productId: item.Product_ID,
    quantity: item.quantity,
  })),
})

const resolveErrorMessage = (error) => {
  if (isAxiosError(error)) {
    const apiMessage = error.response?.data?.message
    if (typeof apiMessage === 'string' && apiMessage.length > 0) {
      return `建立訂單失敗：${apiMessage}`
    }
  }

  return '建立訂單失敗，請確認後端服務是否已啟動。'
}

const submitOrder = async () => {
  if (orderStore.isSubmitted || isSubmitting.value || orderItems.value.length === 0) {
    return
  }

  isSubmitting.value = true
  submitError.value = ''
  submitMessage.value = ''

  try {
    const { data } = await api.post('/api/orders', buildOrderPayload())
    orderStore.setOrderSummary(data)
    submitMessage.value = '訂單已建立成功。'
  } catch (error) {
    submitError.value = resolveErrorMessage(error)
  } finally {
    isSubmitting.value = false
  }
}

const clearOrder = () => {
  orderStore.clearItems()
  router.push({ name: 'products' })
}
</script>

<template>
  <section class="panel">
    <div class="heading-row">
      <div>
        <h2>訂單明細</h2>
        <p class="description">確認本次購買商品與數量。</p>
      </div>

      <button type="button" class="secondary-button" @click="goBackToProducts">返回商品列表</button>
    </div>

    <div v-if="orderStore.hasItems" class="order-layout">
      <div class="order-items">
        <article v-for="item in orderItems" :key="item.Product_ID" class="order-card">
          <p><span>商品編號：</span>{{ item.Product_ID }}</p>
          <h3><span>商品名稱：</span>{{ item.Product_Name }}</h3>
          <p><span>單價：</span>{{ item.Price }}</p>
          <p><span>數量：</span>{{ item.quantity }}</p>
          <strong
            ><span>小計：</span>{{ formatCurrency(parsePrice(item.Price) * item.quantity) }}</strong
          >
        </article>
      </div>

      <aside class="summary-card">
        <p><span>會員編號：</span>{{ authStore.account }}</p>
        <p><span>訂單編號：</span>{{ displayOrderId }}</p>
        <strong><span>訂單總額：</span>{{ formatCurrency(displayTotalAmount) }}</strong>

        <p v-if="submitMessage" class="success-message">{{ submitMessage }}</p>
        <p v-if="submitError" class="error-message">{{ submitError }}</p>

        <button
          type="button"
          class="primary-button"
          :disabled="orderStore.isSubmitted || isSubmitting"
          @click="submitOrder"
        >
          {{ isSubmitting ? '建立中...' : orderStore.isSubmitted ? '訂單已建立' : '建立訂單' }}
        </button>

        <button type="button" class="secondary-button" @click="clearOrder">完成並返回</button>
      </aside>
    </div>

    <div v-else class="empty-state">
      <p>目前沒有可顯示的訂單資料，請先回到商品列表選擇商品。</p>
      <button type="button" class="primary-button" @click="goBackToProducts">前往商品列表</button>
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
p,
strong {
  margin: 0;
}

.description {
  margin-top: 0.5rem;
  color: #52606d;
}

.order-layout {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(260px, 320px);
  gap: 1rem;
  align-items: start;
}

.order-items {
  display: grid;
  gap: 1rem;
}

.order-card {
  padding: 1.25rem;
  border-radius: 18px;
  background: #f8f5ef;
  display: grid;
  gap: 0.75rem;
}

.summary-card {
  padding: 1.5rem;
  border-radius: 18px;
  background: #f3ead8;
  display: grid;
  align-content: start;
  gap: 1rem;
  height: fit-content;
}

.empty-state {
  display: grid;
  justify-items: start;
  gap: 1rem;
  padding: 1.5rem;
  border-radius: 18px;
  background: #f8f5ef;
}

.primary-button,
.secondary-button {
  padding: 0.7rem 1.25rem;
  border: none;
  border-radius: 999px;
  font-weight: 700;
  cursor: pointer;
}

.primary-button {
  background: #9a6700;
  color: #fff;
}

.secondary-button {
  background: #e2e8f0;
  color: #243b53;
}

.primary-button:disabled,
.secondary-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.success-message {
  color: #166534;
  font-weight: 600;
}

.error-message {
  color: #b42318;
  font-weight: 600;
}

span {
  margin-right: 0.35rem;
  color: #7c5a12;
  font-weight: 600;
}

@media (max-width: 900px) {
  .heading-row,
  .order-layout {
    grid-template-columns: 1fr;
  }

  .heading-row {
    align-items: stretch;
  }
}
</style>
