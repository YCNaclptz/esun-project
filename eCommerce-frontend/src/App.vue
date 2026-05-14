<script setup>
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'

import { useAuthStore } from './stores/auth'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()
const isLoginPage = computed(() => route.name === 'login')

const menuItems = computed(() => {
  if (!authStore.isLoggedIn.value) {
    return [{ label: '登入', to: '/login' }]
  }

  return [
    { label: '購買商品', to: '/products' },
    { label: '新增商品', to: '/products/new' },
  ]
})

const currentAccountLabel = computed(() => authStore.account.value || '尚未登入')

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="app-shell" :class="{ 'app-shell-login': isLoginPage }">
    <aside v-if="!isLoginPage" class="sidebar">
      <div>
        <h1>eCommerce</h1>
      </div>

      <nav class="menu">
        <RouterLink v-for="item in menuItems" :key="item.to" :to="item.to" class="menu-link">
          {{ item.label }}
        </RouterLink>
      </nav>
    </aside>

    <main class="content" :class="{ 'content-login': isLoginPage }">
      <header class="topbar">
        <div class="topbar-actions">
          <span class="topbar-account">登入帳號：{{ currentAccountLabel }}</span>
          <button
            v-if="authStore.isLoggedIn.value"
            class="logout-button"
            type="button"
            @click="handleLogout"
          >
            登出
          </button>
        </div>
      </header>

      <div class="content-body" :class="{ 'content-body-centered': isLoginPage }">
        <RouterView />
      </div>
    </main>
  </div>
</template>

<style scoped>
:global(body) {
  margin: 0;
  font-family: 'Segoe UI', sans-serif;
  background: #f4f1ea;
  color: #1f2933;
}

:global(*) {
  box-sizing: border-box;
}

#app {
  min-height: 100vh;
}

.app-shell {
  display: grid;
  grid-template-columns: 260px 1fr;
  min-height: 100vh;
}

.app-shell-login {
  grid-template-columns: 1fr;
}

.sidebar {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  padding: 2rem 1.5rem;
  background: linear-gradient(180deg, #1f3a5f 0%, #284b63 100%);
  color: #f8fafc;
}

.eyebrow {
  margin: 0 0 0.5rem;
  font-size: 0.85rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #cbd5e1;
}

h1 {
  margin: 0;
  font-size: 1.8rem;
}

.menu {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.menu-link {
  display: block;
  padding: 0.9rem 1rem;
  border-radius: 14px;
  color: #e2e8f0;
  text-decoration: none;
  background: rgba(255, 255, 255, 0.08);
  transition:
    background-color 0.2s ease,
    transform 0.2s ease;
}

.menu-link:hover {
  background: rgba(255, 255, 255, 0.16);
  transform: translateX(4px);
}

.menu-link.router-link-active {
  background: #f59e0b;
  color: #172033;
  font-weight: 700;
}

.content {
  display: grid;
  grid-template-rows: auto 1fr;
  gap: 1.5rem;
  padding: 2rem;
}

.content-login {
  min-height: 100vh;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 72px;
  padding: 1rem 1.5rem;
  border-radius: 20px;
  background: rgba(255, 253, 248, 0.88);
  box-shadow: 0 10px 24px rgba(31, 41, 51, 0.08);
}

.topbar-title {
  font-size: 1.05rem;
  font-weight: 700;
  color: #243b53;
}

.topbar-account {
  color: #52606d;
  font-weight: 600;
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-left: auto;
}

.logout-button {
  padding: 0.65rem 1rem;
  border: 0;
  border-radius: 999px;
  background: #1f3a5f;
  color: #f8fafc;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
}

.content-body {
  min-height: 0;
}

.content-body-centered {
  display: grid;
  place-items: center;
}

@media (max-width: 768px) {
  .app-shell {
    grid-template-columns: 1fr;
  }

  .sidebar {
    padding: 1.5rem;
  }

  .content {
    padding: 1.5rem;
  }

  .topbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .topbar-actions {
    flex-direction: column;
    align-items: flex-start;
    margin-left: 0;
  }
}
</style>
