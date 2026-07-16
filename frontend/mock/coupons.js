// 模拟优惠券数据
const coupons = [
  {
    id: '1',
    title: '新人KFC9折券',
    type: 'PERCENTAGE',
    value: 9,  // 9折
    minAmount: 10,
    maxDiscount: 0, // 不限制最大抵扣
    applicableCategory: '',
    applicableBusiness: 'KFC南区店',
    endDate: null,
    validDays: 7,
    totalQuantity: 10000,
    remainingQuantity: 9985,
    maxPerUser: 1,
    used: false,
    claimed: false,
    available: true
  },
  {
    id: '2',
    title: '新人奶茶免单券',
    type: 'FIX_TO_AMOUNT',
    value: 0,  // 免单
    minAmount: 0,  // 无门槛
    maxDiscount: 15, // 最高抵扣15元
    applicableCategory: '奶茶',
    applicableBusiness: '',
    endDate: null,
    validDays: 7,
    totalQuantity: 100,
    remainingQuantity: 45,
    maxPerUser: 1,
    used: false,
    claimed: false,
    available: true
  },
  {
    id: '3',
    title: '新人100元优惠券',
    type: 'FIXED_AMOUNT',
    value: 100,  // 减100元
    minAmount: 200,  // 满200可用
    maxDiscount: 0, // 不限制最大抵扣
    applicableCategory: '',
    applicableBusiness: '',
    endDate: null,
    validDays: 1,
    totalQuantity: 1,
    remainingQuantity: 1,
    maxPerUser: 1,
    used: false,
    claimed: false,
    available: true
  },
  {
    id: '4',
    title: '海底捞20元代金券',
    type: 'FIXED_AMOUNT',
    value: 20,
    minAmount: 100,
    maxDiscount: 0,
    applicableCategory: '火锅',
    applicableBusiness: '海底捞火锅',
    endDate: '2025-12-31',
    validDays: 0,
    totalQuantity: 5000,
    remainingQuantity: 4800,
    maxPerUser: 5,
    used: false,
    claimed: true,
    available: true
  },
  {
    id: '5',
    title: '全场8.5折券',
    type: 'PERCENTAGE',
    value: 8.5,
    minAmount: 50,
    maxDiscount: 30,
    applicableCategory: '',
    applicableBusiness: '',
    endDate: '2025-06-30',
    validDays: 0,
    totalQuantity: 3000,
    remainingQuantity: 2450,
    maxPerUser: 2,
    used: false,
    claimed: true,
    available: true
  },
  {
    id: '6',
    title: '满100减30券',
    type: 'FIXED_AMOUNT',
    value: 30,
    minAmount: 100,
    maxDiscount: 0,
    applicableCategory: '',
    applicableBusiness: '',
    endDate: '2024-05-15',
    validDays: 0,
    totalQuantity: 2000,
    remainingQuantity: 1200,
    maxPerUser: 1,
    used: true,
    claimed: true,
    available: false
  },
  {
    id: '7',
    title: '喜茶买一送一券',
    type: 'FIX_TO_AMOUNT',
    value: 0,
    minAmount: 30,
    maxDiscount: 30,
    applicableCategory: '奶茶',
    applicableBusiness: '喜茶',
    endDate: '2024-03-01',
    validDays: 0,
    totalQuantity: 1000,
    remainingQuantity: 0,
    maxPerUser: 1,
    used: false,
    claimed: false,
    available: false
  }
];

// 用户已领取的优惠券
const userCoupons = [
  { ...coupons.find(c => c.id === '4'), claimTime: '2024-04-01' },
  { ...coupons.find(c => c.id === '5'), claimTime: '2024-04-05' },
  { ...coupons.find(c => c.id === '6'), claimTime: '2024-03-20', useTime: '2024-03-25' }
];

// 新用户可领取的优惠券
const newUserCoupons = coupons.filter(c => ['1', '2', '3'].includes(c.id));

export default [
  // 获取用户可用优惠券
  {
    url: '/api/coupons/available',
    method: 'GET',
    response: (req) => {
      const { businessId, amount, category } = req.query;
      
      // 筛选可用优惠券
      return userCoupons.filter(c => {
        // 排除已使用的优惠券
        if (c.used) return false;
        
        // 检查是否过期
        if (c.endDate && new Date(c.endDate) < new Date()) return false;
        
        // 检查金额门槛
        if (c.minAmount > 0 && amount < c.minAmount) return false;
        
        // 检查适用商家
        if (c.applicableBusiness && c.applicableBusiness !== businessId) return false;
        
        // 检查适用品类
        if (c.applicableCategory && c.applicableCategory !== category) return false;
        
        return true;
      });
    }
  },
  
  // 获取用户所有优惠券
  {
    url: '/api/coupons/user',
    method: 'GET',
    response: () => {
      return userCoupons;
    }
  },
  
  // 获取新用户优惠券
  {
    url: '/api/coupons/new-user',
    method: 'GET',
    response: () => {
      return newUserCoupons;
    }
  },
  
  // 领取优惠券
  {
    url: '/api/coupons/:couponId/claim',
    method: 'POST',
    response: (req) => {
      const { couponId } = req.params;
      const coupon = coupons.find(c => c.id === couponId);
      
      if (!coupon) {
        return { success: false, message: '优惠券不存在' };
      }
      
      if (coupon.remainingQuantity <= 0) {
        return { success: false, message: '优惠券已抢光' };
      }
      
      // 模拟领取成功
      coupon.remainingQuantity -= 1;
      
      // 添加到用户优惠券列表
      const claimedCoupon = { 
        ...coupon, 
        claimed: true, 
        claimTime: new Date().toISOString()
      };
      userCoupons.push(claimedCoupon);
      
      return { success: true, coupon: claimedCoupon };
    }
  }
];
