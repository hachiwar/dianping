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

先执行 `cd backend && ./mvnw package -DskipTests`，再在仓库根目录复制 `.env.example` 为 `.env` 并替换其中两个密码，运行 `docker compose up --build`。访问 `http://localhost:8080/actuator/health` 检查实例健康；停止任一 backend 容器后，Nginx 会转发新请求到另一实例。

停止环境使用 `docker compose down`；需要清空本地开发数据时使用 `docker compose down -v`。回滚时检出上一提交、重新执行后端打包与 `docker compose up --build -d`。

## 验证

```cmd
cd backend
mvnw.cmd test
```

可复现的本地并发冒烟压测（需先启动 Docker Compose）：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-load.ps1 -Requests 100 -Concurrency 10
```

运行中的指标入口为 `/actuator/metrics`：`http.server.requests` 提供 P50/P95/P99，`dianping.cache.requests` 提供缓存命中/未命中，`dianping.outbox.pending`、`dianping.rabbit.dead_letter.pending` 提供消息积压。将这四类指标的错误率、P95、连接池利用率和消息积压阈值配置到现有监控系统即可告警。

该测试包含优惠金额边界和库存为 1 时的并发原子扣减。完整环境启动后，使用 `GET /api/businesses/nearby?category=火锅&longitude=116.4&latitude=39.9` 验证 GEO；使用 `GET /api/operations/dead-letters` 查询死信，并用 `POST /api/operations/dead-letters/replay` 进行单条人工补偿。

2026-07-15 已在 Docker Compose 环境验证：两个后端实例共享 Redis Session；停止任一实例后 Nginx 请求仍成功；Redis 停止时商家详情降级到 MySQL；RabbitMQ 停止时订单事件保存到本地消息表，恢复后自动投递并消费。
