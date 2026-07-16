import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import axios from 'axios'
// import 'font-awesome/css/font-awesome.min.css'
import router from './router' // 确保路径正确

const app = createApp(App)
app.use(ElementPlus)
app.use(router) // 注册路由
// 配置axios
axios.defaults.baseURL = 'http://localhost:8080'
app.config.globalProperties.$http = axios

// 挂载消息组件
import { ElMessage } from 'element-plus'
app.config.globalProperties.$message = ElMessage
// 在main.js或axios配置文件中
axios.defaults.withCredentials = true;
app.mount('#app')