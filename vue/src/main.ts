import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { setRefreshFn } from './fetch'
import { useAuthStore } from './module/auth/store'
import './style.scss'

/**
 * Точка входа SPA. Создаёт экземпляр Vue-приложения,
 * подключает Pinia и Vue Router, затем монтируется в #app.
 */
const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)

setRefreshFn(() => {
  const auth = useAuthStore()
  return auth.refresh()
})

app.mount('#app')
