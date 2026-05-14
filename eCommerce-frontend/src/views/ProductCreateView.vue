<template>
  <section class="panel">
    <h2>新增商品</h2>

    <form class="product-form" @submit.prevent="handleSubmit">
      <label>
        商品編號
        <input v-model="form.productId" type="text" placeholder="系統自動帶入商品編號" readonly />
      </label>

      <label>
        商品名稱
        <input v-model.trim="form.productName" type="text" placeholder="請輸入商品名稱" />
      </label>

      <label>
        商品價格
        <input v-model="form.price" type="number" min="0" step="0.01" placeholder="請輸入價格" />
      </label>

      <label>
        庫存
        <input v-model="form.quantity" type="number" min="0" step="1" placeholder="請輸入庫存" />
      </label>

      <p v-if="message" :class="['status-message', messageType]">{{ message }}</p>

      <button type="submit" :disabled="isSubmitting">
        {{ isSubmitting ? '儲存中...' : '儲存商品' }}
      </button>
    </form>
  </section>
</template>

<script setup>
import axios from 'axios'
import { onMounted, reactive, ref } from 'vue'

const createInitialForm = () => ({
  productId: '',
  productName: '',
  price: '',
  quantity: '',
})

const form = reactive(createInitialForm())
const isSubmitting = ref(false)
const isLoadingProductId = ref(false)
const message = ref('')
const messageType = ref('')

const resetForm = (productId = '') => {
  Object.assign(form, {
    ...createInitialForm(),
    productId,
  })
}

const getNextProductId = (products) => {
  const productNumbers = products
    .map((product) =>
      String(product.productId ?? product.ProductID ?? '')
        .trim()
        .toUpperCase(),
    )
    .map((productId) => /^P(\d{3})$/.exec(productId))
    .filter(Boolean)
    .map((match) => Number(match[1]))

  const nextNumber = productNumbers.length === 0 ? 1 : Math.max(...productNumbers) + 1

  return `P${String(nextNumber).padStart(3, '0')}`
}

const loadNextProductId = async () => {
  isLoadingProductId.value = true

  try {
    const { data } = await axios.get('http://localhost:8080/api/products')
    form.productId = getNextProductId(Array.isArray(data) ? data : [])
  } catch {
    form.productId = ''
    message.value = '無法取得下一個商品編號，請確認後端服務是否已啟動。'
    messageType.value = 'error'
  } finally {
    isLoadingProductId.value = false
  }
}

const handleSubmit = async () => {
  message.value = ''
  messageType.value = ''

  if (!form.productId || !form.productName || form.price === '' || form.quantity === '') {
    message.value = '請完整填寫商品資料。'
    messageType.value = 'error'
    return
  }

  if (isLoadingProductId.value || !form.productId) {
    message.value = '商品編號載入中，請稍後再試。'
    messageType.value = 'error'
    return
  }

  isSubmitting.value = true

  try {
    await axios.post('http://localhost:8080/api/products', {
      productId: form.productId,
      productName: form.productName,
      price: Number(form.price),
      quantity: Number(form.quantity),
    })

    message.value = '商品新增成功。'
    messageType.value = 'success'
    resetForm(form.productId)
    await loadNextProductId()
  } catch {
    message.value = '商品新增失敗，請確認後端服務是否已啟動。'
    messageType.value = 'error'
  } finally {
    isSubmitting.value = false
  }
}

onMounted(() => {
  loadNextProductId()
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

.product-form {
  display: grid;
  gap: 1rem;
}

.status-message {
  margin: 0;
  font-weight: 600;
}

.status-message.success {
  color: #0f766e;
}

.status-message.error {
  color: #b42318;
}

label {
  display: grid;
  gap: 0.45rem;
  color: #334e68;
  font-weight: 600;
}

input,
textarea {
  width: 100%;
  padding: 0.85rem 1rem;
  border: 1px solid #d9e2ec;
  border-radius: 14px;
  font: inherit;
  color: #1f2933;
  background: #fff;
}

button {
  width: fit-content;
  padding: 0.9rem 1.4rem;
  border: 0;
  border-radius: 999px;
  background: #f59e0b;
  color: #172033;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
}

button:disabled {
  opacity: 0.7;
  cursor: wait;
}
</style>
