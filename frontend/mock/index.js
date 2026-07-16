import packagesMock from './packages.js';
import couponsMock from './coupons.js';
import ordersMock from './orders.js';

// 合并所有模拟接口
const mockApis = [
  ...packagesMock,
  ...couponsMock,
  ...ordersMock
];

export default function setupMock(app) {
  mockApis.forEach(({ url, method, response }) => {
    app[method.toLowerCase()](url, (req, res) => {
      // 添加随机延迟，模拟网络请求
      setTimeout(() => {
        if (typeof response === 'function') {
          const responseData = response(req);
          res.json(responseData);
        } else {
          res.json(response);
        }
      }, Math.random() * 300 + 100);
    });
  });
}
