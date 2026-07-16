// vite.config.js
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import setupMock from './mock';

export default defineConfig({
    plugins: [
        vue(),
        {
            name: 'setup-mock',
            configureServer(server) {
                // 配置模拟接口
                setupMock(server.middlewares);
            },
        }
    ],
    define: {
        __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: false,
        __VUE_OPTIONS_API__: true,     // 可选
        __VUE_PROD_DEVTOOLS__: false   // 可选
    },
    server: {
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api/, ''),
                // 当存在mock数据时不代理到真实服务器
                bypass: (req) => {
                    // 如果是开发环境，使用mock数据
                    if (process.env.NODE_ENV === 'development') {
                        return req.url;
                    }
                }
            }
        }
    }
});