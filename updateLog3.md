# 日志：
主要内容：

- 后端：用户注册时生成唯一邀请码，存入用户表。
- 前端：“我的邀请”页面，展示我的邀请码、邀请记录、奖励券明细。
- 数据库：用户表增加邀请码字段。

# 用户表结构调整说明
- 新增字段invitation_code存储用户的邀请码
# 邀请码生成逻辑
- UserService.java
```
private String generateInvitationCode() {
        // Generate 6 characters code with numbers and uppercase letters
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int index = (int) (chars.length() * Math.random());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
private String generateInvitationCode() {
        // Generate 6 characters code with numbers and uppercase letters
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int index = (int) (chars.length() * Math.random());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
```
# 前端邀请码页面
- MyInvitation.vue

# 后端接口定义

## 获取邀请记录
- **接口路径**: `/api/invitation-records`
- **请求方式**: `GET`
- **请求头**: 
  - `Authorization`: `Bearer <用户令牌>`
- **响应示例**:
```json
[
  {
    "id": 1,
    "inviteeName": "张三",
    "status": "已完成有效下单"
  },
  {
    "id": 2,
    "inviteeName": "李四",
    "status": "未完成有效下单"
  }
]
```

## 获取奖励券明细
- **接口路径**: `/api/reward-coupons`
- **请求方式**: `GET`
- **请求头**: 
  - `Authorization`: `Bearer <用户令牌>`
- **响应示例**:
```json
[
  {
    "id": 101,
    "description": "20元无门槛优惠券",
    "expiryDate": "2025-05-26"
  }
]
```