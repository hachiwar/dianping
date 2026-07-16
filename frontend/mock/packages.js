// 模拟套餐数据
const packages = [
  {
    id: '1',
    title: '双人精品套餐',
    price: 129.9,
    originalPrice: 199.9,
    businessId: '1',
    businessName: '海底捞火锅',
    description: '本套餐包含精选海底捞招牌菜品，含2份小料，2份主食，2份饮料，以及指定荤素菜品各2份。',
    imageUrl: '/mock/images/packages/package1.jpg',
    sales: 589,
    category: '火锅',
    items: [
      { name: '招牌鸳鸯锅底', quantity: 1 },
      { name: '精选肥牛', quantity: 2 },
      { name: '虾滑', quantity: 1 },
      { name: '午餐肉', quantity: 1 },
      { name: '香菇', quantity: 1 },
      { name: '豆腐', quantity: 1 },
      { name: '小料', quantity: 2 },
      { name: '米饭', quantity: 2 },
      { name: '饮料', quantity: 2 }
    ],
    useTimeDescription: '周一至周日 11:00-21:00',
    useRules: '需提前1小时预约，每单限2人使用，节假日可用'
  },
  {
    id: '2',
    title: '4人欢聚套餐',
    price: 258,
    originalPrice: 398,
    businessId: '1',
    businessName: '海底捞火锅',
    description: '适合4人聚餐的超值套餐，锅底四选一，包含多种肉类和蔬菜，以及小吃、甜点和饮料。',
    imageUrl: '/mock/images/packages/package2.jpg',
    sales: 320,
    category: '火锅',
    items: [
      { name: '四选一锅底', quantity: 1 },
      { name: '上等肥牛', quantity: 2 },
      { name: '精选羊肉', quantity: 2 },
      { name: '虾滑', quantity: 2 },
      { name: '蔬菜拼盘', quantity: 2 },
      { name: '小料', quantity: 4 },
      { name: '米饭/面条', quantity: 4 },
      { name: '饮料', quantity: 4 },
      { name: '甜点', quantity: 2 }
    ],
    useTimeDescription: '周一至周日 11:00-22:00',
    useRules: '需提前2小时预约，节假日可用，不与其他优惠同享'
  },
  {
    id: '3',
    title: '单人午市套餐',
    price: 69,
    originalPrice: 99,
    businessId: '2',
    businessName: 'KFC南区店',
    description: '工作日午餐特惠，包含主食、小食、饮料的超值组合',
    imageUrl: '/mock/images/packages/package3.jpg',
    sales: 425,
    category: '快餐',
    items: [
      { name: '招牌汉堡', quantity: 1 },
      { name: '炸鸡', quantity: 2 },
      { name: '薯条', quantity: 1 },
      { name: '可乐', quantity: 1 }
    ],
    useTimeDescription: '周一至周五 11:00-14:00',
    useRules: '仅限门店堂食，不与其他优惠同享'
  },
  {
    id: '4',
    title: '饮品畅享套餐',
    price: 28,
    originalPrice: 45,
    businessId: '3',
    businessName: '喜茶',
    description: '两杯精选饮品组合，可搭配任意小食一份',
    imageUrl: '/mock/images/packages/package4.jpg',
    sales: 752,
    category: '奶茶',
    items: [
      { name: '任选饮品', quantity: 2 },
      { name: '小食', quantity: 1 }
    ],
    useTimeDescription: '全天候可用',
    useRules: '不可使用于新品，不与其他优惠同享'
  }
];

// 按商家ID分组套餐
const packagesByBusiness = {
  '1': packages.filter(p => p.businessId === '1'),
  '2': packages.filter(p => p.businessId === '2'),
  '3': packages.filter(p => p.businessId === '3')
};

export default [
  // 获取商户的套餐列表
  {
    url: '/api/packages/business/:businessId',
    method: 'GET',
    response: (req) => {
      const { businessId } = req.params;
      return packagesByBusiness[businessId] || [];
    }
  },
  // 获取套餐详情
  {
    url: '/api/packages/:packageId',
    method: 'GET',
    response: (req) => {
      const { packageId } = req.params;
      return packages.find(p => p.id === packageId) || { error: '套餐不存在' };
    }
  }
];
