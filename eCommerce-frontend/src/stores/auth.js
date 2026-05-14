import { computed, ref } from 'vue'

const account = ref('')

const isLoggedIn = computed(() => account.value.length > 0)

function login(nextAccount) {
  account.value = nextAccount
}

function logout() {
  account.value = ''
}

export function useAuthStore() {
  return {
    account,
    isLoggedIn,
    login,
    logout,
  }
}
