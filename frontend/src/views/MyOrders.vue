<template>
  <div class="page-container">
    <div class="orders-container">
      <div class="page-header">
        <div class="back-link" @click="goBack">
          <i class="fas fa-arrow-left"></i> 返回
        </div>
        <h1>我的订单</h1>
      </div>
      
      <div v-if="loading" class="loading-container">
        <i class="fas fa-spinner fa-spin"></i>
        <span>加载中...</span>
      </div>
      
      <div v-else-if="orders.length === 0" class="empty-state">
        <div class="empty-icon">
          <i class="fas fa-receipt"></i>
        </div>
        <h2>暂无订单</h2>
        <p>您还没有购买任何套餐</p>
        <button class="explore-btn" @click="$router.push('/nearby-food')">去逛逛</button>
      </div>
      
      <div v-else class="orders-list">
        <div 
          v-for="order in orders" 
          :key="order.id" 
          class="order-card"
          :class="{ 'highlight': isHighlighted(order.id) }"
          @click="goToOrderDetail(order.id)"
        >
          <div class="order-header">
            <div class="order-time">{{ formatDateTime(order.createTime) }}</div>
            <div class="order-status" :class="getStatusClass(order)">{{ getOrderStatus(order) }}</div>
          </div>
          
          <div class="order-content">
            <div class="business-info">
              <div class="order-business">{{ order.businessName }}</div>
              <div class="order-title">{{ order.packageTitle }}</div>
            </div>
            <div class="order-price">￥{{ order.finalPrice }}</div>
          </div>
                     
          <div class="order-footer">
            <button class="view-code-btn">
              <i class="fas fa-qrcode"></i>
              查看券码
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      loading: true,
      orders: [],
      userInfo: null,
      highlightOrderId: null
    }
  },
  mounted() {
    this.userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    this.fetchOrders();
    // 获取URL中的高亮订单ID
    this.highlightOrderId = this.$route.query.highlight;
  },
  methods: {
    async fetchOrders() {
      try {
        const response = await axios.get('/api/orders/user', {
          headers: {
            'UserId': this.userInfo.id
          }
        });
        console.log('订单数据:', response.data); // 添加这行来调试
        this.orders = response.data;
      } catch (error) {
        console.error('获取订单列表失败:', error);
        this.orders = [];
      } finally {
        this.loading = false;
      }
    },
    formatDateTime(dateStr) {
      if (!dateStr) return '';
      
      const date = new Date(dateStr);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      
      return `${year}-${month}-${day} ${hours}:${minutes}`;
    },
    getOrderStatus(order) {
      // 这里可以根据实际业务扩展更多状态
      return '未使用';
    },
    getStatusClass(order) {
      // 根据订单状态返回对应的样式类名
      // 这里简单处理，实际应根据业务返回不同状态
      return 'status-unused';
    },
    goToOrderDetail(orderId) {
      this.$router.push(`/coupon-code/${orderId}`);
    },
    goBack() {
      this.$router.push('/my');
    },
    isHighlighted(orderId) {
      return this.highlightOrderId && orderId.toString() === this.highlightOrderId.toString();
    }
  }
}
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e9ecef 100%);
  padding: 20px;
}

.orders-container {
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 30px;
  position: relative;
}

.back-link {
  display: inline-flex;
  align-items: center;
  color: #4a90e2;
  margin-bottom: 10px;
  cursor: pointer;
  font-size: 1rem;
  transition: all 0.2s ease;
  padding: 5px 10px;
  border-radius: 20px;
}

.back-link:hover {
  background: rgba(74, 144, 226, 0.1);
  transform: translateX(-3px);
}

.back-link i {
  margin-right: 8px;
}

.page-header h1 {
  font-size: 1.8rem;
  margin: 0;
  color: #2c3e50;
  position: relative;
  display: inline-block;
}

.page-header h1::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 0;
  width: 120px;
  height: 3px;
  background: #4a90e2;
  border-radius: 3px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: #666;
}

.loading-container i {
  font-size: 2.5rem;
  margin-bottom: 15px;
  color: #4a90e2;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 0;
  text-align: center;
  background: white;
  border-radius: 15px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.05);
  margin: 20px 0;
}

.empty-icon {
  font-size: 5rem;
  color: #e0e6ed;
  margin-bottom: 20px;
}

.empty-state h2 {
  color: #2c3e50;
  margin-bottom: 10px;
  font-size: 1.5rem;
}

.empty-state p {
  color: #7f8c8d;
  margin-bottom: 25px;
}

.explore-btn {
  padding: 12px 25px;
  background: linear-gradient(45deg, #4a90e2, #5ca9fb);
  color: white;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
  box-shadow: 0 4px 10px rgba(74, 144, 226, 0.2);
}

.explore-btn:hover {
  background: linear-gradient(45deg, #357abd, #4a90e2);
  transform: translateY(-3px);
  box-shadow: 0 6px 15px rgba(74, 144, 226, 0.3);
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 10px 0;
}

.order-card {
  background: white;
  border-radius: 15px;
  padding: 22px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.04);
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.order-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: #4a90e2;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.order-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.08);
}

.order-card:hover::before {
  opacity: 1;
}

.order-card.highlight {
  animation: highlight 2s ease-in-out;
  border: 2px solid #4a90e2;
  box-shadow: 0 6px 20px rgba(74, 144, 226, 0.15);
}

@keyframes highlight {
  0% { transform: translateY(-5px); box-shadow: 0 8px 25px rgba(74, 144, 226, 0.25); }
  100% { transform: translateY(0); box-shadow: 0 4px 12px rgba(0,0,0,0.04); }
}

.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 18px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f3f8;
}

.order-time {
  color: #7f8c8d;
  font-size: 0.9rem;
}

.order-status {
  font-weight: 600;
  border-radius: 12px;
  padding: 3px 10px;
  font-size: 0.85rem;
}

.status-unused {
  color: #4a90e2;
  background: rgba(74, 144, 226, 0.1);
}

.order-content {
  margin-bottom: 18px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.business-info {
  flex: 1;
}

.order-business {
  color: #7f8c8d;
  font-size: 0.9rem;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
}

.order-title {
  font-size: 1.2rem;
  color: #2c3e50;
  margin-bottom: 5px;
  font-weight: 500;
}

.order-price {
  font-size: 1.5rem;
  font-weight: bold;
  color: #e53935;
  text-shadow: 0 1px 2px rgba(229, 57, 53, 0.1);
}

.order-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
  border-top: 1px solid #f0f3f8;
}

.view-code-btn {
  padding: 8px 18px;
  background: linear-gradient(45deg, #4a90e2, #5ca9fb);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 3px 8px rgba(74, 144, 226, 0.2);
}

.view-code-btn:hover {
  background: linear-gradient(45deg, #357abd, #4a90e2);
  transform: translateY(-2px);
  box-shadow: 0 5px 12px rgba(74, 144, 226, 0.3);
}

.view-code-btn i {
  font-size: 0.9rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-container {
    padding: 15px;
  }

  .order-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .order-price {
    margin-top: 10px;
  }
}

@media (max-width: 480px) {
  .page-container {
    padding: 10px;
  }

  .order-card {
    padding: 18px;
  }

  .order-header {
    flex-direction: column;
    gap: 8px;
  }

  .order-status {
    align-self: flex-start;
  }

  .order-footer {
    justify-content: center;
  }

  .view-code-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
