import { createApp } from 'vue'
import { createPinia } from 'pinia'
import 'element-plus/dist/index.css'
import { setWasmUrl } from '@lottiefiles/dotlottie-wc'
import { MotionPlugin } from '@vueuse/motion'
import App from './App.vue'
import router from './router'
import './styles/index.scss'
import './styles/themes.scss'

setWasmUrl('/vendor/dotlottie/dotlottie-player.wasm')

const app = createApp(App)

const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(MotionPlugin)

app.mount('#app')
