import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './style.scss'

/**
 * Точка входа SPA. Создаёт экземпляр Vue-приложения,
 * подключает Pinia и Vue Router, затем монтируется в #app.
 */
const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
