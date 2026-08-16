# 前端开发说明

前端基于 Vue 3、Vue Router、Element Plus、Axios 和 Vite，实现注册登录、商家检索、附近商家、套餐详情、订单确认、优惠券、券码、评论、邀请记录和订单通知页面。

## 环境要求

- Node.js 20 或更高版本
- npm 10 或更高版本
- 已启动的后端服务，默认地址为 `http://localhost:8080`

## 安装与启动

```bash
npm ci
npm run serve
```

开发服务器监听 `http://localhost:8081`，并将下列路径代理到后端：

- `/api`
- `/login`
- `/logout`
- `/users`
- `/captcha`
- `/images`
- `/packages`

开发和生产环境均调用真实后端接口，前端不保留独立的模拟数据层。

## 生产构建

```bash
npm run build
```

构建产物写入 `dist/`。Docker Compose 使用 `nginx/Dockerfile` 完成前端构建，并由 Nginx 托管静态资源和反向代理后端请求。

## 页面路由

| 路径 | 页面 | 登录要求 |
| --- | --- | --- |
| `/auth` | 注册与登录 | 否 |
| `/nearby-food` | 商家检索与附近商家 | 是 |
| `/businessDetail/:id` | 商家详情、套餐和评论 | 是 |
| `/package/:id` | 套餐详情 | 是 |
| `/order-confirmation/:packageId` | 订单确认与优惠券选择 | 是 |
| `/coupon-code/:orderId` | 数字券码与二维码 | 是 |
| `/my-orders` | 我的订单 | 是 |
| `/my-coupons` | 我的卡包 | 是 |
| `/new-user-coupons` | 新人券领取 | 是 |
| `/my-invitation` | 邀请码、邀请记录和奖励 | 是 |
| `/my-notifications` | 订单通知 | 是 |
| `/my` | 个人中心 | 是 |

## 接口与状态规则

- Axios 使用同源相对路径并携带 Session Cookie，登录状态由服务端会话维护。
- `localStorage.userInfo` 只用于页面导航状态；后端以服务端 Session 中的身份作为授权依据。
- 下单前端只提交 `packageId`、`businessId`、可选邀请码和随机 `idempotencyKey`。
- 套餐价格、优惠金额、最终金额和优惠券可用性由后端计算，避免客户端篡改交易数据。
- 创建订单后进入券码页面；券码由消息消费者异步生成，页面通过订单详情接口轮询获取。
- 订单列表直接显示后端返回的订单状态和创建时间。

## 源码结构

| 目录 | 职责 |
| --- | --- |
| `src/views` | 业务页面 |
| `src/components` | 可复用页面组件 |
| `src/router` | 路由定义和登录导航守卫 |
| `src/assets` | 本地样式、图片和字体资源 |
| `src/main.js` | Vue、Element Plus、Axios 和 Font Awesome 初始化 |
| `vite.config.mjs` | Vite 插件、别名、端口和后端代理配置 |

## 验证

```bash
npm run build
npm audit
```

生产构建与依赖审计均应以命令退出码为准。当前锁文件审计结果为 0 个已知漏洞。
