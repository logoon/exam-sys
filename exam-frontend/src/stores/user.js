import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, getUserInfo } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const roles = ref([])

  const isAdmin = computed(() => {
    return roles.value.includes('ADMIN')
  })

  const isLoggedIn = computed(() => {
    return !!token.value
  })

  async function loginAction(loginData) {
    const res = await login(loginData)
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    await fetchUserInfo()
    return res
  }

  async function fetchUserInfo() {
    if (!token.value) return
    try {
      const res = await getUserInfo()
      userInfo.value = res.data
      roles.value = res.data.roles || []
    } catch (e) {
      logout()
      throw e
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    roles.value = []
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    roles,
    isAdmin,
    isLoggedIn,
    loginAction,
    fetchUserInfo,
    logout
  }
})
