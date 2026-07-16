<template>
  <div class="page-container">
    <div class="coupons-container">
      <div class="page-header">
        <div class="back-link" @click="goBack">
          <i class="fas fa-arrow-left"></i> 返回
        </div>
        <h1>我的卡包</h1>
      </div>

      <div class="tab-controls">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          :class="['tab-btn', { active: currentTab === tab.value }]"
          @click="currentTab = tab.value"
        >
          {{ tab.label }}
          <span v-if="getTabCount(tab.value) > 0" class="tab-count">{{ getTabCount(tab.value) }}</span>
        </button>
      </div>

      <div v-if="loading" class="loading-container">
        <i class="fas fa-spinner fa-spin"></i>
        <span>加载中...</span>
      </div>

      <div v-else-if="getFilteredCoupons().length === 0" class="empty-state">
        <div class="empty-icon">
          <i class="fas fa-ticket-alt"></i>
        </div>
        <h2>暂无{{ currentTabLabel }}优惠券</h2>
        <p v-if="currentTab === 'unused'">快去领券中心看看吧~</p>
        <div v-if="currentTab === 'unused'">
          <button class="explore-btn" @click="$router.push('/new-user-coupons')">去领券</button>
        </div>
      </div>

      <div v-else class="coupons-list">
        <div
          v-for="coupon in getFilteredCoupons()"
          :key="coupon.id"
          class="coupon-card"
          :class="[getCouponClass(coupon), coupon.status]"
        >
          <div class="coupon-left">
            <div class="coupon-value">{{ formatCouponValueDisplay(coupon) }}</div>
            <div class="coupon-name">{{ coupon.title || coupon.couponName }}</div>
          </div>

          <div class="coupon-right">
            <div class="coupon-info">
              <div class="coupon-title">{{ coupon.couponName }}</div>
              <div class="coupon-desc">
                <span v-if="coupon.shopId">可用商家: {{ coupon.shopId }}</span>
                <span v-else>品类: {{ getCategoryDisplay(coupon) }}</span>
                <span v-if="coupon.expireTime"> | 有效期至: {{ formatValidity(coupon) }}</span>
                <span v-else> | 长期有效</span>
              </div>
            </div>

            <div class="coupon-status-badge" :class="coupon.status">
              <img v-if="coupon.status === 'used'" src="@/assets/used-stamp.png" alt="已使用">
              <img v-if="coupon.status === 'expired'" src="@/assets/expired-stamp.png" alt="已过期">
            </div>

            <button v-if="coupon.status === 'unused'" class="use-btn" @click="goToUse(coupon)">
              去使用 <i class="fas fa-chevron-right"></i>
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
      allCoupons: [],
      userInfo: null,
      currentTab: 'unused',
      tabs: [
        { label: '未使用', value: 'unused' },
        { label: '已使用', value: 'used' },
        { label: '已过期', value: 'expired' }
      ]
    };
  },
  computed: {
    processedCoupons() {
      const now = new Date();
      return this.allCoupons.map(coupon => {
        let status = coupon.couponAmount > 0 ? 'unused' : 'used';
        if (this.isCouponExpired(coupon, now)) {
          status = 'expired';
        }
        return { 
          ...coupon, 
          status, 
          category: coupon.category || '全商家通用' // 确保 category 有默认值
        };
      });
    },
    currentTabLabel() {
      const tab = this.tabs.find(t => t.value === this.currentTab);
      return tab ? tab.label : '';
    }
  },
  mounted() {
    const storedUserInfo = localStorage.getItem('userInfo');
    if (!storedUserInfo) {
      this.$router.push('/auth');
      return;
    }
    this.userInfo = JSON.parse(storedUserInfo);
    this.fetchCoupons();
  },
  methods: {
    async fetchCoupons() {
      this.loading = true;
      try {
        const response = await axios.get('/api/coupons/all', { // 确保路径正确
          headers: {
            'UserId': this.userInfo.id
          }
        });
        this.allCoupons = response.data;
      } catch (error) {
        console.error('获取优惠券列表失败:', error);
        this.allCoupons = [];
      } finally {
        this.loading = false;
      }
    },
    getFilteredCoupons() {
      return this.processedCoupons.filter(coupon => coupon.status === this.currentTab);
    },
    getTabCount(tabValue) {
      return this.processedCoupons.filter(coupon => coupon.status === tabValue).length;
    },
    isCouponExpired(coupon, now = new Date()) {
      if (coupon.expireTime) {
        return new Date(coupon.expireTime) < now;
      }
      return false;
    },
    formatValidity(coupon) {
      if (coupon.expireTime) {
        return new Date(coupon.expireTime).toLocaleDateString();
      }
      return '长期有效';
    },
    getCategoryDisplay(coupon) {
    if (coupon.category) {
      return coupon.category;
    }  
    return '全商家通用';
    },

    getCouponClass(coupon) {
      if (coupon.status !== 'unused') {
        return coupon.status;
      }
      
      switch (coupon.type) {
        case '满减':
          return 'fixed-amount';
        case '立减':
          return 'fix-to-amount';
        case '折扣':
          return 'percentage';
        default:
          return 'fixed-amount'; // 默认样式
      }
    },
    
    // 格式化优惠券面值显示
    formatCouponValueDisplay(coupon) {
      switch (coupon.type) {
        case '满减':
          return `￥${coupon.value}`;
        case '立减':
          return `￥${coupon.value}`;
        case '折扣':
          return `${coupon.value}折`;
        case '免单':
          return '免单';
        default:
          // 如果没有类型或值，则显示名称
          return coupon.value ? `￥${coupon.value}` : coupon.couponName;
      }
    },
    goToUse(coupon) {
      this.$router.push('/nearby-food');
    },
    goBack() {
      this.$router.push('/my');
    }
  }
};
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e9ecef 100%);
  padding: 20px;
  box-sizing: border-box;
}

.coupons-container {
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 30px;
  padding: 0 10px;
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

.tab-controls {
  display: flex;
  margin: 0 10px 25px 10px;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
}

.tab-btn {
  flex: 1;
  padding: 15px 10px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1rem;
  color: #7f8c8d;
  position: relative;
  transition: all 0.3s;
  font-weight: 500;
}

.tab-btn:hover {
  background: rgba(74, 144, 226, 0.03);
}

.tab-btn.active {
  color: #4a90e2;
  background: rgba(74, 144, 226, 0.05);
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 15%;
  right: 15%;
  height: 3px;
  background: #4a90e2;
  border-radius: 3px;
}

.tab-count {
  display: inline-block;
  background: #e0e0e0;
  color: #7f8c8d;
  font-size: 0.75rem;
  min-width: 20px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  border-radius: 10px;
  margin-left: 6px;
  padding: 0 5px;
  font-weight: normal;
  transition: all 0.3s;
}

.tab-btn.active .tab-count {
  background: #4a90e2;
  color: white;
  transform: scale(1.05);
}

.loading-container, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 70px 20px;
  text-align: center;
  color: #666;
  background: white;
  border-radius: 15px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.05);
  margin: 20px 0;
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.loading-container i {
  font-size: 2.5rem;
  margin-bottom: 20px;
  color: #4a90e2;
}

.loading-container span {
  font-size: 1rem;
}

.empty-icon {
  font-size: 5rem;
  color: #e0e6ed;
  margin-bottom: 25px;
}

.empty-state h2 {
  color: #2c3e50;
  font-size: 1.3rem;
  margin-bottom: 12px;
}

.empty-state p {
  color: #7f8c8d;
  font-size: 0.95rem;
  margin-bottom: 25px;
}

.explore-btn {
  padding: 12px 30px;
  background: linear-gradient(45deg, #4a90e2, #5ca9fb);
  color: white;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 1rem;
  font-weight: 500;
  box-shadow: 0 4px 10px rgba(74, 144, 226, 0.2);
}

.explore-btn:hover {
  background: linear-gradient(45deg, #357abd, #4a90e2);
  transform: translateY(-3px);
  box-shadow: 0 6px 15px rgba(74, 144, 226, 0.3);
}

.coupons-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 0 10px;
  animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
  from { 
    opacity: 0; 
    transform: translateY(20px);
  }
  to { 
    opacity: 1;
    transform: translateY(0);
  }
}

.coupon-card {
  display: flex;
  border-radius: 12px;
  overflow: hidden;
  min-height: 120px;
  position: relative;
  box-shadow: 0 5px 15px rgba(0,0,0,0.06);
  background: white;
  transition: all 0.3s;
}

.coupon-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
}

.coupon-left {
  width: 120px;
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 18px 10px;
  text-align: center;
  position: relative;
  flex-shrink: 0;
  background-image: radial-gradient(circle at 10% 10%, rgba(255,255,255,0.1) 0%, transparent 70%);
}

.coupon-left::after {
  content: '';
  position: absolute;
  top: 5px;
  bottom: 5px;
  right: -1px;
  width: 2px;
  background: linear-gradient(to bottom, white 5px, transparent 5px, transparent 10px);
  background-size: 100% 15px;
}

.coupon-right {
  flex: 1;
  background: white;
  padding: 15px 18px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  min-width: 0;
}

.coupon-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex: 1;
  margin-right: 10px;
  min-width: 0;
}

.coupon-value {
  font-size: 1.6rem;
  font-weight: bold;
  margin-bottom: 8px;
  line-height: 1.2;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.coupon-name {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 4px;
  word-break: break-all;
  text-align: center;
}

.coupon-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 8px;
  color: #2c3e50;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.coupon-desc {
  font-size: 0.9rem;
  color: #7f8c8d;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.coupon-status-badge {
  position: absolute;
  top: 0;
  right: 0;
  width: 70px;
  height: 70px;
  overflow: hidden;
  opacity: 0.9;
}

.coupon-status-badge img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.use-btn {
  background: linear-gradient(45deg, #4a90e2, #5ca9fb);
  border: none;
  color: white;
  padding: 8px 18px;
  border-radius: 20px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
  flex-shrink: 0;
  font-weight: 500;
  box-shadow: 0 3px 8px rgba(74, 144, 226, 0.2);
  display: flex;
  align-items: center;
}

.use-btn:hover {
  background: linear-gradient(45deg, #357abd, #4a90e2);
  transform: translateY(-2px);
  box-shadow: 0 5px 12px rgba(74, 144, 226, 0.3);
}

.use-btn i {
  margin-left: 6px;
  font-size: 0.8rem;
}

.fixed-amount .coupon-left { 
  background: linear-gradient(45deg, #4a90e2, #5ca9fb); 
}

.fix-to-amount .coupon-left { 
  background: linear-gradient(45deg, #e53935, #f44336); 
}

.percentage .coupon-left { 
  background: linear-gradient(45deg, #ff9800, #ffb74d); 
}

.coupon-card.used,
.coupon-card.expired {
  opacity: 0.7;
}

.coupon-card.used .coupon-left,
.coupon-card.expired .coupon-left {
  background: linear-gradient(45deg, #95a5a6, #bdc3c7);
}

.coupon-card.used .coupon-left::after,
.coupon-card.expired .coupon-left::after {
   background: linear-gradient(to bottom, #f5f7fa 5px, transparent 5px, transparent 10px);
   background-size: 100% 15px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-container {
    padding: 15px;
  }
  
  .coupon-left {
    width: 100px;
    padding: 15px 8px;
  }
  
  .coupon-value {
    font-size: 1.4rem;
  }
  
  .coupon-name {
    font-size: 0.8rem;
  }
}

@media (max-width: 480px) {
  .page-container {
    padding: 10px;
  }
  
  .tab-controls {
    margin: 0 5px 20px 5px;
  }
  
  .coupon-card {
    min-height: auto;
    flex-direction: column;
  }
  
  .coupon-left {
    width: 100%;
    padding: 15px;
    border-bottom: 1px dashed rgba(255,255,255,0.3);
  }
  
  .coupon-left::after {
    display: none;
  }
  
  .coupon-right {
    padding: 15px;
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .coupon-info {
    margin-right: 0;
    width: 100%;
  }
  
  .use-btn {
    align-self: center;
    width: 100%;
    justify-content: center;
  }
  
  .coupon-status-badge {
    width: 60px;
    height: 60px;
  }
}
</style>
