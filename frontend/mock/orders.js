import { packages } from './packages.js';

// 生成16位随机数字券码
function generateCouponCode() {
  return Array.from({ length: 16 }, () => Math.floor(Math.random() * 10)).join('');
}

// 订单数据
let orders = [
  {
    id: '1',
    orderNo: 'ORD202404100001',
    userId: '1',
    packageId: '1',
    packageTitle: '双人精品套餐',
    businessId: '1',
    businessName: '海底捞火锅',
    packagePrice: 129.9,
    discount: 0,
    finalPrice: 129.9,
    couponId: null,
    couponCode: '1234567890123456',
    createTime: '2024-04-10T15:30:00Z',
    status: 'UNUSED'
  },
  {
    id: '2',
    orderNo: 'ORD202404120002',
    userId: '1',
    packageId: '3',
    packageTitle: '单人午市套餐',
    businessId: '2',
    businessName: 'KFC南区店',
    packagePrice: 69,
    discount: 30,
    finalPrice: 39,
    couponId: '6',
    couponCode: '6543210987654321',
    createTime: '2024-04-12T12:15:00Z',
    status: 'UNUSED'
  }
];

let nextOrderId = orders.length + 1;

export default [
  // 创建订单
  {
    url: '/api/orders',
    method: 'POST',
    response: (req) => {
      const userId = req.headers.userid || '1';
      const orderData = req.body;
      
      // 获取套餐信息
      const packageInfo = packages.find(p => p.id === orderData.packageId) || {
        id: orderData.packageId,
        title: `套餐${orderData.packageId}`,
        businessId: orderData.businessId,
        businessName: '商家名称'
      };
      
      // 创建新订单
      const newOrder = {
        id: String(nextOrderId++),
        orderNo: `ORD${new Date().toISOString().slice(0, 10).replace(/-/g, '')}${String(nextOrderId).padStart(4, '0')}`,
        userId,
        packageId: orderData.packageId,
        packageTitle: packageInfo.title,
        businessId: orderData.businessId,
        businessName: packageInfo.businessName,
        packagePrice: orderData.packagePrice,
        discount: orderData.discount || 0,
        finalPrice: orderData.finalPrice,
        couponId: orderData.couponId,
        couponCode: generateCouponCode(),
        createTime: new Date().toISOString(),
        status: 'UNUSED'
      };
      
      // 添加到订单列表
      orders.push(newOrder);
      
      // 更新套餐销量
      if (packageInfo && typeof packageInfo.sales === 'number') {
        packageInfo.sales += 1;
      }
      
      return { 
        success: true, 
        orderId: newOrder.id, 
        message: '下单成功'
      };
    }
  },
  
  // 获取订单详情
  {
    url: '/api/orders/:orderId',
    method: 'GET',
    response: (req) => {
      const { orderId } = req.params;
      const order = orders.find(o => o.id === orderId);
      
      if (!order) {
        return { error: '订单不存在' };
      }
      
      return order;
    }
  },
  
  // 获取用户订单列表
  {
    url: '/api/orders/user',
    method: 'GET',
    response: (req) => {
      const userId = req.headers.userid || '1';
      return orders
        .filter(o => o.userId === userId)
        .sort((a, b) => new Date(b.createTime) - new Date(a.createTime));
    }
  },
  
  // 检查用户是否有订单
  {
    url: '/api/orders/check-user-orders',
    method: 'GET',
    response: (req) => {
      const userId = req.headers.userid || '1';
      const hasOrders = orders.some(o => o.userId === userId);
      return { hasOrders };
    }
  }
];
