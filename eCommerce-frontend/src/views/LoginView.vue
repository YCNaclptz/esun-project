<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'

import { useAuthStore } from '../stores/auth'

const account = ref('')
const router = useRouter()
const authStore = useAuthStore()
const isDisabled = computed(() => account.value.length === 0)

function handleAccountInput(event) {
  const sanitizedValue = event.target.value.replace(/\D/g, '').slice(0, 5)
  account.value = sanitizedValue
}

function handleBeforeInput(event) {
  if (event.data && /\D/.test(event.data)) {
    event.preventDefault()
  }
}

function handlePaste(event) {
  event.preventDefault()

  const pastedText = event.clipboardData?.getData('text') ?? ''
  const sanitizedValue = pastedText.replace(/\D/g, '').slice(0, 5)

  account.value = sanitizedValue
}

function handleLogin() {
  if (isDisabled.value) {
    return
  }

  authStore.login(account.value)
  router.push('/products')
}
</script>

<template>
  <section class="panel">
    <h2>帳號登入</h2>
    <p class="description">帳號欄位只接受 5 位以下的數字。</p>

    <form class="login-form" @submit.prevent="handleLogin">
      <label>
        帳號
        <input
          :value="account"
          type="text"
          inputmode="numeric"
          pattern="[0-9]*"
          maxlength="5"
          placeholder="請輸入帳號"
          @beforeinput="handleBeforeInput"
          @input="handleAccountInput"
          @paste="handlePaste"
        />
      </label>

      <button type="submit" :disabled="isDisabled">登入</button>
    </form>
  </section>
</template>

<style scoped>
.panel {
  width: min(460px, 100%);
  max-width: 460px;
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
  margin: 0;
  font-size: 2rem;
}

.description {
  margin: 0.75rem 0 2rem;
  color: #52606d;
}

.login-form {
  display: grid;
  gap: 1rem;
}

label {
  display: grid;
  gap: 0.45rem;
  color: #334e68;
  font-weight: 600;
}

input {
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
  cursor: not-allowed;
  opacity: 0.55;
}
</style>
