<template>
  <div class="container">
    <!-- 用户信息展示 -->
    <div class="user-info" @click="goToUserInfo">
      <span class="username">{{ userInfo.username }}</span>
      <div class="avatar">
        {{ userInfo.username?.charAt(0)?.toUpperCase() }}
      </div>
    </div>

    <!-- 主内容区域 -->
    <h1 class="welcome-title">欢迎来到生活服务平台</h1>

    <div class="feature-cards">
      <div class="feature-card" @click="$router.push('/my-orders')">
        <i class="fas fa-receipt"></i>
        <span>我的订单</span>
      </div>
      
      <div class="feature-card" @click="$router.push('/my-coupons')">
        <i class="fas fa-ticket-alt"></i>
        <span>我的卡包</span>
      </div>

      <div class="feature-card" @click="$router.push('/my-invitation')">
        <i class="fas fa-user-plus"></i>
        <span>我的邀请</span>
      </div>
      
      <div v-if="!hasOrders" class="feature-card new-user-card" @click="$router.push('/new-user-coupons')">
        <i class="fas fa-gift"></i>
        <span>新人优惠</span>
        <div class="badge">新</div>
      </div>
    </div>

    <div class="button-group">
      <button
          class="main-btn food-btn"
          @click="$router.push('/nearby-food')"
      >
        <i class="fas fa-utensils"></i>
        <div class="btn-text">
          <span>附近美食</span>
        </div>
      </button>

      <button
          class="main-btn life-btn"
          @click="$router.push('/life-zone')"
      >
        <i class="fas fa-shopping-basket"></i>
        <div class="btn-text">
          <span>生活专区</span>
        </div>
      </button>
    </div>

    <button class="logout-btn" @click="handleLogout">
      <i class="fas fa-sign-out-alt"></i>
      退出登录
    </button>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      userInfo: {
        id: '',
        username: '未登录用户'
      },
      hasOrders: false
    }
  },
  async mounted() {
    const userData = localStorage.getItem('userInfo')
    if (userData) {
      this.userInfo = JSON.parse(userData)
      await this.checkUserOrders();
    } else {
      this.$router.push('/auth')
    }
  },
  methods: {
    handleLogout() {
      if (confirm('确定要退出登录吗？')) {
        localStorage.removeItem('userInfo')
        this.$router.push('/auth')
      }
    },
    goToUserInfo() {
      this.$router.push('/user-info')
    },
    async checkUserOrders() {
      try {
        const response = await axios.get('/api/orders/check-user-orders', {
          headers: {
            'UserId': this.userInfo.id
          }
        });
        this.hasOrders = response.data.hasOrders;
      } catch (error) {
        console.error('检查用户订单失败:', error);
        this.hasOrders = false;
      }
    }
  }
}
</script>

<style scoped>
.container {
  padding: 2rem;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.user-info {
  position: fixed;
  top: 20px;
  right: 30px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s;
  z-index: 10;
  background: rgba(255, 255, 255, 0.85);
  padding: 8px 15px;
  border-radius: 50px;
  backdrop-filter: blur(4px);
  box-shadow: 0 2px 10px rgba(0,0,0,0.08);
}

.user-info:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 15px rgba(0,0,0,0.12);
}

.avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(45deg, #4a90e2, #5ca9fb);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}

.username {
  font-size: 1.1rem;
  color: #333;
  font-weight: 500;
}

.welcome-title {
  text-align: center;
  margin: 80px 0 40px;
  color: #2c3e50;
  font-size: 2.2rem;
  animation: fadeIn 0.8s ease-out;
}

.feature-cards {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 50px;
  max-width: 800px;
  width: 100%;
  animation: slideUp 0.6s ease-out;
}

.feature-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 120px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.08);
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.feature-card::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: #4a90e2;
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.feature-card:hover::after {
  transform: scaleX(1);
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0,0,0,0.15);
}

.feature-card i {
  font-size: 2.5rem;
  color: #4a90e2;
  margin-bottom: 10px;
  transition: all 0.3s;
}

.feature-card:hover i {
  transform: scale(1.1);
}

.feature-card span {
  font-size: 1rem;
  color: #333;
}

.new-user-card {
  background: linear-gradient(135deg, rgba(255,236,210,0.7), rgba(252,182,159,0.7));
}

.new-user-card i {
  color: #e74c3c;
}

.badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #e53935;
  color: white;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  font-weight: bold;
  box-shadow: 0 2px 5px rgba(0,0,0,0.2);
}

.button-group {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 30px;
  max-width: 900px;
  margin: 0 auto 60px;
  padding: 20px;
  width: 100%;
}

.main-btn {
  width: 100%;
  height: 180px;
  border: none;
  border-radius: 20px;
  font-size: 1.5rem;
  color: white;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 15px;
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
  position: relative;
  overflow: hidden;
}

.main-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255,255,255,0.1);
  transform: translateX(-100%);
  transition: transform 0.5s ease;
}

.main-btn:hover::before {
  transform: translateX(100%);
}

.main-btn:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0,0,0,0.2);
}

.btn-text {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.food-btn { 
  background: linear-gradient(45deg, #ff6b6b, #ff8e8e); 
}

.life-btn { 
  background: linear-gradient(45deg, #ff9a44, #ffb347); 
}

.logout-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  padding: 12px 25px;
  background: rgba(231, 76, 60, 0.85);
  color: white;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 10px;
  backdrop-filter: blur(4px);
  box-shadow: 0 4px 10px rgba(0,0,0,0.15);
}

.logout-btn i {
  font-size: 1.2rem;
}

.logout-btn:hover {
  background: #c0392b;
  transform: scale(1.05);
  box-shadow: 0 6px 15px rgba(0,0,0,0.2);
}

.fas {
  font-size: 2.5rem;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from { 
    opacity: 0; 
    transform: translateY(30px);
  }
  to { 
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    padding: 1.5rem;
  }
  
  .feature-cards {
    gap: 15px;
  }
  
  .welcome-title {
    font-size: 1.8rem;
    margin: 60px 0 30px;
  }
  
  .user-info {
    top: 15px;
    right: 15px;
  }
}

@media (max-width: 480px) {
  .container {
    padding: 1rem;
  }
  
  .welcome-title {
    font-size: 1.6rem;
    margin: 50px 0 20px;
  }
  
  .feature-card {
    width: calc(50% - 15px);
  }
  
  .main-btn {
    height: 150px;
  }
  
  .logout-btn {
    bottom: 20px;
    right: 20px;
    padding: 10px 20px;
  }
}
</style>
