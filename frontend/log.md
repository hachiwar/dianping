# 团购套餐与优惠券前端实现日志
当需要连接真实后端时，只需将vite.config.js中的bypass条件改为false即可。
## 功能改动

1. 添加了团购套餐展示功能 
   - 在商户详情页显示该商户的团购套餐列表
   - 添加团购套餐详情页面
   - 实现"立即购买"功能

2. 添加了下单与券码功能
   - 创建订单确认页面，支持选择优惠券
   - 实现券码展示页面，包含16位数字和二维码
   - 添加我的订单页面查看所有订单

3. 添加了优惠券功能
   - 实现我的卡包页面，按未使用/已使用/已过期分类展示
   - 实现新人优惠券领取页面
   - 优惠券在下单时自动选择最优方案

4. 优化了用户页面
   - 添加我的订单、我的卡包、新人优惠入口

## API 接口定义

### 团购套餐接口

1. `GET /api/packages/business/{businessId}`
   - 功能：获取指定商户的团购套餐列表
   - 返回：套餐列表，包含id、title、price、imageUrl、sales等字段

2. `GET /api/packages/{packageId}`
   - 功能：获取指定套餐的详细信息
   - 返回：套餐详情，包含title、description、price、items等

### 优惠券接口

1. `GET /api/coupons/available`
   - 功能：获取当前用户针对特定场景可用的优惠券
   - 参数：
     - businessId: 商户ID
     - amount: 订单金额
     - category: 品类
   - 返回：可用优惠券列表

2. `GET /api/coupons/user`
   - 功能：获取当前用户所有优惠券
   - 返回：优惠券列表，包含id、title、type、value、minAmount、used、endDate等

3. `GET /api/coupons/new-user`
   - 功能：获取新用户可领取的优惠券
   - 返回：可领取优惠券列表

4. `POST /api/coupons/{couponId}/claim`
   - 功能：领取指定优惠券
   - 返回：领取结果

### 订单接口

1. `POST /api/orders`
   - 功能：创建订单
   - 参数：包含packageId、packagePrice、finalPrice、couponId、discount、businessId
   - 返回：创建的订单信息，包含orderId

2. `GET /api/orders/{orderId}`
   - 功能：获取指定订单详情，包括券码
   - 返回：订单详情，包含couponCode、orderNo、packageTitle、businessName等

3. `GET /api/orders/user`
   - 功能：获取当前用户的所有订单
   - 返回：订单列表

4. `GET /api/orders/check-user-orders`
   - 功能：检查用户是否有订单
   - 返回：{ hasOrders: true/false }

## 设计模式应用

### 1. 订单完成后的通知 - 观察者模式

在订单创建成功后，需要通知优惠券模块和销量统计模块。采用观察者模式将订单作为主题，优惠券和销量作为观察者。

### 2. 不同类型优惠券处理 - 策略模式

实现了三种不同的优惠券计算逻辑，采用策略模式将其封装到各自的类中，统一调用接口。

### 3. 构造优惠券 - 建造者模式

使用建造者模式创建优惠券对象，解决了构造函数参数过多的问题。

### 4. 优惠券校验 - 责任链模式

将优惠券的多项校验规则拆分成单独的处理器，按照链式结构依次校验，任一步骤失败则中断。
