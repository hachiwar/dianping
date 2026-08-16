import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import '@fortawesome/fontawesome-free/css/all.min.css'
import App from './App.vue'
import axios from 'axios'
// import 'font-awesome/css/font-awesome.min.css'
import router from './router' // 确保路径正确

const app = createApp(App)
app.use(ElementPlus)
app.use(router) // 注册路由
// 生产环境与 Nginx 同源；开发环境由 vue.config.js 代理到后端。
axios.defaults.baseURL = ''
app.config.globalProperties.$http = axios

// 挂载消息组件
import { ElMessage } from 'element-plus'
app.config.globalProperties.$message = ElMessage
// 在main.js或axios配置文件中
axios.defaults.withCredentials = true;
app.mount('#app')
