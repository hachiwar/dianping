# 数据库设计规范

项目使用 MySQL 8.4 保存用户、商家、套餐、优惠券、订单、评论、邀请和消息可靠性数据。实体由 Spring Data JPA 管理，金额字段统一使用 `DECIMAL(19,2)` 对应 Java `BigDecimal`。

## Schema 管理

- 本地单实例开发默认使用 `JPA_DDL_AUTO=update`。
- Docker Compose 中 `backend-1` 使用 `update` 创建或更新表结构，`backend-2` 使用 `validate` 校验结构后启动。
- 两个实例连接同一个 MySQL 数据库，不分别维护 Schema。
- 演示数据由 `DemoDataInitializer` 写入；仅在 `DEMO_DATA_ENABLED=true` 且商家表为空时执行。

## 数据表

| 表名 | 对应实体 | 主要职责 |
| --- | --- | --- |
| `user` | `User` | 用户名、BCrypt 密码、邀请码、邀请人和订单次数 |
| `merchant` | `Merchant` | 商家名称、拼音、品类、评分、价格、地址、坐标和图片 |
| `packages` | `PackageGroup` | 团购套餐、价格、库存、销量和乐观锁版本 |
| `coupon` | `Coupon` | 用户优惠券、类型、金额规则、适用范围、有效期和来源 |
| `orders` | `Order` | 订单金额、套餐、券码、业务号、幂等键和状态 |
| `review` | `Review` | 商家评论、评分、回复关系和创建时间 |
| `search_history` | `SearchHistory` | 用户搜索词和创建时间 |
| `invitation_record` | `InvitationRecord` | 邀请人与被邀请人的有效订单记录 |
| `invitation_reward` | `InvitationReward` | 邀请阶梯奖励与奖励优惠券 |
| `order_notification` | `OrderNotification` | 订单异步处理完成后的站内通知 |
| `outbox_event` | `OutboxEvent` | 订单事务内创建的待发布事件 |
| `processed_message` | `ProcessedMessage` | 已消费消息的唯一标记 |

实体之间通过 `user_id`、`merchant_id`、`package_id`、`order_id` 等 ID 字段进行逻辑关联。交易写入由服务层事务保证顺序和一致性。

## 关键唯一约束

| 表 | 唯一字段 | 作用 |
| --- | --- | --- |
| `user` | `username` | 防止用户名重复 |
| `user` | `invitation_code` | 保证邀请码唯一 |
| `orders` | `business_no` | 保证业务订单号唯一 |
| `orders` | `voucher_code` | 保证 16 位券码唯一；生成前允许为空 |
| `orders` | `user_id, idempotency_key` | 防止同一用户重复下单 |
| `coupon` | `user_id, source` | 防止重复领取同来源新人券 |
| `invitation_record` | `user_id, invitee_id` | 防止同一有效邀请重复记账 |
| `order_notification` | `order_id` | 每笔订单只生成一条完成通知 |
| `outbox_event` | `message_id` | 防止事件重复写入 |
| `processed_message` | `message_id` | 保证消息消费幂等 |

## 金额规则

- 套餐原价、售价、优惠门槛、优惠值、最高抵扣、订单原价和最终价均使用两位小数。
- 金额计算统一采用 `RoundingMode.HALF_UP`。
- 订单价格只从套餐表和优惠券表读取，不能直接采用客户端提交的价格。
- 最终价不能小于 `0.00`。

## 库存与优惠券规则

- 套餐库存更新同时检查 `stock > 0` 和当前 `version`，成功后库存减 1、销量加 1、版本加 1。
- 优惠券使用时通过带 `coupon_amount > 0` 条件的更新原子扣减数量。
- 新人券来源标记为 `NEW_USER`，同一用户只能领取一次新人券。
- 当前新人券支持满减、折扣、秒杀和立减规则；订单计算同时兼容免单类型。

## 订单与消息规则

- 订单事务包含库存扣减、最优优惠券核销、订单写入和 Outbox 事件写入。
- `idempotency_key` 最长为 64 个字符，并与用户 ID 组成联合唯一键。
- `business_no` 在下单事务内生成，`voucher_code` 由消息消费者异步生成。
- 消息消费者先检查 `processed_message`，再处理券码、邀请奖励和订单通知，成功后写入消息标记。

## 图片路径

商家和套餐图片保存在 `backend/src/main/resources/static/images/`。数据库保存以 `/images/` 开头的相对 URL，Nginx 和 Vite 开发服务器均将该路径转发到后端。
