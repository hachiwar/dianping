<template>
  <div class="user-info-container">
    <button class="back-btn" @click="$router.back()">
      <i class="fas fa-arrow-left"></i>
      返回
    </button>
    <div class="user-card">
      <div class="user-header">
        <div class="avatar">
          {{ userInfo.username?.charAt(0)?.toUpperCase() }}
        </div>
        <h2>{{ userInfo.username }}</h2>
      </div>
      
      <div class="info-section">
        <div class="info-item">
          <i class="fas fa-id-card"></i>
          <span>ID: {{ userInfo.id }}</span>
        </div>
        
        <div class="info-item">
          <i class="fas fa-clock"></i>
          <span>注册时间: {{ formatDate() }}</span>
        </div>
      </div>

      <div class="action-buttons">
        <button class="edit-btn">
          <i class="fas fa-edit"></i>
          修改密码
        </button>
        <button class="logout-btn" @click="handleLogout">
          <i class="fas fa-sign-out-alt"></i>
          退出登录
        </button>
        <button class="my-coupons-btn" @click="goToMyCoupons">我的卡包</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      userInfo: {
        id: '',
        username: '',
      }, 
      user: {}
    }
  },
  mounted() {
    const userData = localStorage.getItem('userInfo');
    if (userData) {
      this.userInfo = JSON.parse(userData);
      this.user = JSON.parse(userData);
    } else {
      this.$router.push('/auth');
    }
  },
  methods: {
    handleLogout() {
      localStorage.removeItem('userInfo')
      this.$router.push('/auth')
    },
    formatDate() {
      return new Date().toLocaleDateString()
    },
    goToMyCoupons() {
      this.$router.push('/my-coupons');
    }
  }
}
</script>

<style scoped>
.user-info-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.user-card {
  background: white;
  border-radius: 15px;
  padding: 40px; /* 增加内边距 */
  width: 100%;
  max-width: 600px; /* 增加卡片宽度 */
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.user-header {
  text-align: center;
  margin-bottom: 30px;
}

.avatar {
  width: 120px; /* 增大头像尺寸 */
  height: 120px;
  background: linear-gradient(45deg, #4a90e2, #357abd);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 3rem; /* 增大头像文字 */
  color: white;
  margin: 0 auto 15px;
}

.user-header h2 {
  font-size: 2rem; /* 增大用户名文字 */
  margin-top: 20px;
}

.info-section {
  margin: 30px 0;
}

.info-item {
  display: flex;
  align-items: center;
  font-size: 1.2rem; /* 增大信息文字 */
  padding: 20px; /* 增加内边距 */
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 10px;
}

.info-item i {
  margin-right: 15px;
  color: #4a90e2;
  font-size: 1.4rem; /* 增大图标 */
}

.action-buttons {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  margin-top: 30px;
}

.edit-btn, .logout-btn {
  padding: 15px; /* 增加按钮内边距 */
  border-radius: 8px;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 1.1rem; /* 增大按钮文字 */
  transition: all 0.3s;
}

.edit-btn {
  background: #4a90e2;
  color: white;
}

.edit-btn:hover {
  background: #357abd;
}

.logout-btn {
  background: #dc3545;
  color: white;
}

.logout-btn:hover {
  background: #c82333;
}

.my-coupons-btn {
  padding: 10px 20px;
  background: #4a90e2;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 1rem;
  transition: all 0.3s;
  margin-top: 10px;
}

.my-coupons-btn:hover {
  background: #357abd;
  transform: translateY(-2px);
}

.back-btn {
  position: fixed;
  top: 20px;
  left: 20px;
  padding: 12px 25px;
  background: #666;
  color: white;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
  z-index: 1000;
}

.back-btn:hover {
  background: #555;
  transform: scale(1.05);
}
</style>