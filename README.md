# dianping

运行参考：

## 后端：

设置环境变量 `DB_URL`、`DB_USERNAME` 和 `DB_PASSWORD` 后启动 `Application.java`。本地默认仅用于开发，密码不写入仓库。

## 前端：

环境配置 + 命令行运行以下命令即可

```cmd
npm install
npm run build
```

## 多实例开发环境

先执行 `cd backend && ./mvnw package -DskipTests`，再在仓库根目录复制 `.env.example` 为 `.env` 并设置密码，运行 `docker compose up --build`。访问 `http://localhost:8080/actuator/health` 检查实例健康；停止任一 backend 容器后，Nginx 会转发新请求到另一实例。
