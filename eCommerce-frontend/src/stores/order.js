import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

export const useOrderStore = defineStore('order', () => {
  const items = ref([])
  const orderId = ref('')
  const totalPrice = ref(null)
  const payStatus = ref(0)
  const isSubmitted = ref(false)

  const hasItems = computed(() => items.value.length > 0)

  function setItems(nextItems) {
    items.value = nextItems.map((item) => ({ ...item }))
    orderId.value = ''
    totalPrice.value = null
    payStatus.value = 0
    isSubmitted.value = false
  }

  function setOrderSummary(order) {
    orderId.value = order.orderId ?? ''
    totalPrice.value = order.price ?? null
    payStatus.value = order.payStatus ?? 0
    isSubmitted.value = true
  }

  function clearItems() {
    items.value = []
    orderId.value = ''
    totalPrice.value = null
    payStatus.value = 0
    isSubmitted.value = false
  }

  return {
    items,
    orderId,
    totalPrice,
    payStatus,
    isSubmitted,
    hasItems,
    setItems,
    setOrderSummary,
    clearItems,
  }
})
