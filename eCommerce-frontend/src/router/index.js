import { createRouter, createWebHistory } from 'vue-router'

import { useAuthStore } from '../stores/auth'
import LoginView from '../views/LoginView.vue'
import OrderDetailView from '../views/OrderDetailView.vue'
import ProductCreateView from '../views/ProductCreateView.vue'
import ProductListView from '../views/ProductListView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login',
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/products',
      name: 'products',
      component: ProductListView,
    },
    {
      path: '/products/new',
      name: 'product-create',
      component: ProductCreateView,
    },
    {
      path: '/orders/detail',
      name: 'order-detail',
      component: OrderDetailView,
    },
  ],
})

router.beforeEach((to) => {
  const authStore = useAuthStore()

  if (!authStore.isLoggedIn.value && to.name !== 'login') {
    return { name: 'login' }
  }

  if (authStore.isLoggedIn.value && to.name === 'login') {
    return { name: 'products' }
  }

  return true
})

export default router
