<template>
  <div class="page-container">
    <div class="new-user-coupons">
      <div class="page-header">
        <div class="back-link" @click="goBack">
          <i class="fas fa-arrow-left"></i> 返回
        </div>
        <h1>新人专享优惠</h1>
      </div>

      <div class="banner">
        <h2>欢迎加入小众点评!</h2>
        <p>请选择以下优惠券中的一张</p>
      </div>

      <div v-if="loading" class="loading-container">
        <i class="fas fa-spinner fa-spin"></i>
        <span>加载中...</span>
      </div>

      <div v-else class="coupons-grid">
        <div
          v-for="coupon in availableCoupons"
          :key="coupon.choice"
          class="coupon-card"
          :class="getCouponClass(coupon)"
        >
          <div class="coupon-content">
            <div class="coupon-header">
              <h3>{{ coupon.name }}</h3>
            </div>
            <div class="coupon-value-section">
              <div class="coupon-value">{{ getCouponValue(coupon) }}</div>
              <div class="coupon-description">{{ getCouponDescription(coupon) }}</div>
            </div>
          </div>
          <button
            class="claim-btn"
            @click="selectCoupon(coupon.choice)"
            :disabled="hasSelected"
          >
            <i class="fas fa-gift"></i> 选择此券
          </button>
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
      availableCoupons: [
        { choice: 'A', name: '火锅满减', description: '满100减38元(火锅专用券),领取后7天可用', type: 'fixed-amount', value: '¥38' },
        { choice: 'B', name: '喜茶折扣', description: '满9元8折券(喜茶专用券),领取后7天可用', type: 'percentage', value: '8折' },
        { choice: 'C', name: '奶茶免单', description: '奶茶畅喝免单券,领取后1天可用', type: 'fix-to-amount', value: '免单' },
        { choice: 'D', name: '通用立减', description: '通用立减10元券,长期可用', type: 'fixed-amount', value: '¥10' }
      ],
      userInfo: null,
      hasSelected: false
    };
  },
  mounted() {
    const storedUserInfo = localStorage.getItem('userInfo');
    if (!storedUserInfo) {
      this.$router.push('/login');
      return;
    }
    this.userInfo = JSON.parse(storedUserInfo);

    // Check if the user already has any of the four specific coupons
    axios.get('/api/coupons/all', {
      headers: {
        'UserId': this.userInfo.id
      }
    }).then(response => {
      const userCoupons = response.data;
      const hasSpecificCoupons = userCoupons.some(coupon =>
        ['满100减38元(火锅专用券)', '满9元8折券(奶茶专用券)', '咖啡畅喝免单券', '通用立减10元券'].includes(coupon.couponName)
      );
      if (hasSpecificCoupons) {
        alert('您已领取新人优惠券，无法再次访问此页面。');
        this.$router.push('/my');
      } else {
        this.loading = false;
      }
    }).catch(error => {
      console.error('获取用户优惠券失败:', error);
      alert('加载失败，请稍后重试');
      this.$router.push('/my');
    });
  },
  methods: {
    getCouponClass(coupon) {
      return coupon.type;
    },
    getCouponValue(coupon) {
      return coupon.value;
    },
    getCouponDescription(coupon) {
      return coupon.description.split(',')[0];
    },
    async selectCoupon(choice) {
      try {
        this.hasSelected = true;
        await axios.post('/api/coupons/issue-by-choice', null, {
          headers: {
            'UserId': this.userInfo.id
          },
          params: { choice }
        });
        alert('优惠券领取成功！');
        this.$router.push('/my-coupons');
      } catch (error) {
        console.error('领取优惠券失败:', error);
        alert('领取失败，请稍后重试');
        this.hasSelected = false;
      }
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

.new-user-coupons {
  max-width: 1000px;
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
  width: 180px;
  height: 3px;
  background: #4a90e2;
  border-radius: 3px;
}

.banner {
  text-align: center;
  padding: 30px;
  background: linear-gradient(135deg, #ff7e5f, #feb47b);
  color: white;
  border-radius: 15px;
  margin: 0 10px 35px 10px;
  box-shadow: 0 8px 20px rgba(254, 180, 123, 0.3);
  position: relative;
  overflow: hidden;
  animation: fadeIn 0.5s ease-out;
}

.banner::before {
  content: '';
  position: absolute;
  top: -20px;
  right: -20px;
  width: 120px;
  height: 120px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.banner::after {
  content: '';
  position: absolute;
  bottom: -30px;
  left: -30px;
  width: 160px;
  height: 160px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 50%;
}

.banner h2 {
  font-size: 1.8rem;
  margin-bottom: 10px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1;
}

.banner p {
  font-size: 1.1rem;
  opacity: 0.9;
  position: relative;
  z-index: 1;
}

.loading-container {
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

.coupons-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 25px;
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
  background: white;
  border-radius: 15px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.06);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  border: none;
}

.coupon-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 6px;
  height: 100%;
  background: #4a90e2;
  opacity: 0.7;
}

.coupon-card.fixed-amount::before {
  background: linear-gradient(to bottom, #4a90e2, #5ca9fb);
}

.coupon-card.percentage::before {
  background: linear-gradient(to bottom, #ff9800, #ffb74d);
}

.coupon-card.fix-to-amount::before {
  background: linear-gradient(to bottom, #e53935, #f44336);
}

.coupon-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
}

.coupon-card:hover::before {
  opacity: 1;
}

.coupon-content {
  padding: 25px 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.coupon-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.coupon-header h3 {
  font-size: 1.2rem;
  margin: 0;
  color: #2c3e50;
  line-height: 1.3;
  font-weight: 600;
}

.coupon-value-section {
  display: flex;
  flex-direction: column;
  margin-bottom: 25px;
}

.coupon-value {
  font-size: 2.2rem;
  font-weight: bold;
  line-height: 1.2;
  margin-bottom: 10px;
}

.fixed-amount .coupon-value {
  color: #4a90e2;
  text-shadow: 0 1px 2px rgba(74, 144, 226, 0.1);
}

.percentage .coupon-value {
  color: #ff9800;
  text-shadow: 0 1px 2px rgba(255, 152, 0, 0.1);
}

.fix-to-amount .coupon-value {
  color: #e53935;
  text-shadow: 0 1px 2px rgba(229, 57, 53, 0.1);
}

.coupon-description {
  font-size: 1rem;
  color: #7f8c8d;
  line-height: 1.4;
}

.claim-btn {
  width: calc(100% - 40px);
  margin: 0 20px 20px 20px;
  padding: 12px 15px;
  border: none;
  border-radius: 25px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  color: white;
  background: linear-gradient(45deg, #4a90e2, #5ca9fb);
  box-shadow: 0 4px 10px rgba(74, 144, 226, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.claim-btn:hover:not(:disabled) {
  background: linear-gradient(45deg, #357abd, #4a90e2);
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(74, 144, 226, 0.3);
}

.claim-btn i {
  font-size: 0.9rem;
}

.claim-btn:disabled {
  background: linear-gradient(45deg, #bdc3c7, #cbd1d6);
  color: white;
  cursor: not-allowed;
  box-shadow: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-container {
    padding: 15px;
  }
  
  .banner {
    padding: 25px;
  }
  
  .banner h2 {
    font-size: 1.6rem;
  }
  
  .banner p {
    font-size: 1rem;
  }
  
  .coupons-grid {
    gap: 20px;
  }
}

@media (max-width: 480px) {
  .page-container {
    padding: 10px;
  }
  
  .banner {
    padding: 20px;
    margin-bottom: 25px;
  }
  
  .banner h2 {
    font-size: 1.4rem;
  }
  
  .coupon-content {
    padding: 20px 15px;
  }
  
  .coupon-value {
    font-size: 1.8rem;
  }
  
  .claim-btn {
    width: calc(100% - 30px);
    margin: 0 15px 15px 15px;
  }
}
</style>
