# 后端开发说明

后端基于 Java 21 和 Spring Boot 3.4.3，负责用户会话、商家与套餐查询、优惠券、订单、评论、邀请奖励、可靠消息和运行指标。应用采用模块化单体结构，通过 MySQL、Redis 和 RabbitMQ 提供持久化、缓存、共享会话、地理位置检索和异步事件处理能力。

## 环境要求

- JDK 21
- Maven Wrapper（仓库已包含）
- MySQL 8
- Redis 7
- RabbitMQ 3

如只需运行完整系统，推荐直接在仓库根目录使用 Docker Compose，无需单独配置上述依赖。

## 启动后端

Windows PowerShell：

```powershell
.\mvnw.cmd spring-boot:run
```

Linux 或 macOS：

```bash
./mvnw spring-boot:run
```

后端默认监听 `http://localhost:8080`。健康检查地址为 `http://localhost:8080/actuator/health`。

## 环境变量

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `SERVER_PORT` | `8080` | HTTP 服务端口 |
| `DB_URL` | `jdbc:mysql://localhost:3306/dianping` | MySQL JDBC 地址 |
| `DB_USERNAME` | `root` | 数据库用户 |
| `DB_PASSWORD` | 空 | 数据库密码 |
| `JPA_DDL_AUTO` | `update` | Hibernate Schema 策略 |
| `REDIS_HOST` | `localhost` | Redis 主机 |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `RABBITMQ_HOST` | `localhost` | RabbitMQ 主机 |
| `RABBITMQ_PORT` | `5672` | RabbitMQ AMQP 端口 |
| `RABBITMQ_USERNAME` | `guest` | RabbitMQ 用户 |
| `RABBITMQ_PASSWORD` | `guest` | RabbitMQ 密码 |
| `DEMO_DATA_ENABLED` | `false` | 是否写入本地演示商家和套餐 |

## 模块结构

| 目录 | 职责 |
| --- | --- |
| `controller` | RESTful API、参数校验和当前用户边界 |
| `service` | 交易、缓存、优惠券、邀请奖励和分布式锁逻辑 |
| `repository` | Spring Data JPA 持久化及原子更新语句 |
| `entity` | MySQL 实体与唯一约束 |
| `event` | RabbitMQ 发布、消费、重试、死信和 Outbox 补发 |
| `config` | 安全、会话、消息队列、限流和演示数据配置 |
| `handler` | 全局异常响应 |
| `filter` | 请求 ID 与访问链路处理 |
| `metrics` | 缓存、Outbox 和死信指标 |

## 身份认证

- `POST /users` 完成注册，并在注册成功后建立 Session。
- `POST /login` 使用用户名和密码登录。
- `POST /logout` 销毁服务端 Session。
- 密码使用 BCrypt 保存，图形验证码保存在 Session 中。
- 多实例部署时，Session 统一存储在 Redis；业务接口从服务端 Session 解析用户身份，不信任客户端传入的用户 ID。

## 主要接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/users` | 注册并登录 |
| `POST` | `/login` | 登录 |
| `POST` | `/logout` | 退出登录 |
| `GET` | `/api/businesses` | 商家筛选与排序 |
| `GET` | `/api/businesses/nearby` | Redis GEO 附近商家查询 |
| `GET` | `/api/packages/business/{businessId}` | 商家套餐列表 |
| `GET` | `/api/packages/{packageId}` | 套餐详情 |
| `GET` | `/api/coupons/all` | 当前用户的优惠券 |
| `POST` | `/api/coupons/issue-by-choice` | 领取新人券 |
| `POST` | `/api/orders` | 幂等创建订单 |
| `GET` | `/api/orders/{orderId}` | 查询订单与异步券码 |
| `GET` | `/api/orders/user` | 当前用户订单列表 |
| `GET` | `/api/orders/notifications` | 当前用户订单通知 |
| `POST` | `/api/reviews/add` | 新增评论或回复 |
| `GET` | `/api/invitation-records` | 邀请记录 |
| `GET` | `/api/reward-coupons` | 邀请奖励券记录 |

创建订单的请求体由服务端校验：

```json
{
  "packageId": 1,
  "businessId": 1,
  "invitationCode": "ABC123",
  "idempotencyKey": "018f47a6-42ef-7bf2-9f13-f24e5c31f423"
}
```

`invitationCode` 可以省略。套餐价格、优惠金额、最终金额和当前用户均由服务端计算或解析，客户端不能指定。

## 一致性与可靠消息

- 下单使用幂等键、Redis 带租约锁和 MySQL 联合唯一索引防止重复订单。
- 套餐库存使用库存条件和版本号进行单条原子更新，避免超卖。
- 优惠券核销通过带剩余数量条件的更新完成，金额统一使用 `BigDecimal`。
- 订单事务同时写入 Outbox 事件；事务提交后向 RabbitMQ 发布。
- 消费者异步生成 16 位券码、处理邀请奖励并写入站内通知。
- 消息使用唯一键去重，处理失败后进行 3 次有限重试并进入死信队列。

## 测试

```powershell
.\mvnw.cmd test
```

测试覆盖金额边界、优惠券原子核销、库存并发扣减、Redis 锁所有权、Session 身份、注册后登录态、消息幂等、Outbox 发布确认、重试和死信路由。当前测试套件包含 19 项测试。
