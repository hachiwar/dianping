<template>
  <div class="invitation-container">
    <button class="back-btn" @click="$router.back()">
      <i class="fas fa-arrow-left"></i>
      返回
    </button>

    <div class="invitation-card">
      <h2>我的邀请码</h2>
      <div class="code-display">
        {{ userInfo.invitationCode }}
      </div>
      <button class="copy-btn" @click="copyCode">
        <i class="fas fa-copy"></i>
        复制邀请码
      </button>
    </div>

    <div class="invitation-records">
      <h3>邀请记录</h3>
      <ul>
        <li v-for="record in invitationRecords" :key="record.id">
          用户名： {{ record.inviteeName }} <br>- 下单时间：{{ record.orderTime }} <br>- 金额：¥{{ record.price }}
        </li>
      </ul>
    </div>

    <div class="reward-coupons">
      <h3>奖励券明细</h3>
      <h4 v-if="remaining>0">还差{{ remaining }}位，可再次领取奖励券</h4>
      <h4 v-else>邀请 2 位新人朋友，可领取奖励券</h4>
      <ul>
        <li v-for="coupon in rewardCoupons" :key="coupon.id">
          {{ coupon.couponName }} <br>- 发放时间：{{coupon.atTime}} <br>- 有效期至 {{ coupon.expireTime }}
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      userInfo: {},
      invitationRecords: [],
      rewardCoupons: [],
      totalCount: 0,
      remaining: 0
    };
  },
  mounted() {
    const userData = localStorage.getItem('userInfo');
    if (userData) {
      this.userInfo = JSON.parse(userData);
      this.fetchInvitationRecords();
      this.fetchRewardCoupons();
    } else {
      this.$router.push('/auth');
    }
  },
  methods: {
    copyCode() {
      navigator.clipboard.writeText(this.userInfo.invitationCode)
          .then(() => alert('邀请码已复制到剪贴板'))
          .catch(err => console.error('复制失败:', err));
    },
    fetchInvitationRecords() {
      // Replace with actual API call
      fetch('http://localhost:8080/api/invitation-records', {
        headers: {
          Authorization: `Bearer ${this.userInfo.token}`,
          'UserId': this.userInfo.id.toString()  // 添加 UserId 请求头
        }
      })
          .then(response => {
            if (!response.ok) throw new Error(`HTTP错误! 状态码: ${response.status}`);
            const contentType = response.headers.get('content-type');
            if (!contentType || !contentType.includes('application/json')) {
              throw new TypeError("响应不是JSON格式");
            }
            return response.json();
          })
          .then(data => {
            this.invitationRecords = data;
            this.totalCount = data.length ;
            if(this.totalCount>0)
            this.remaining = (2 - (this.totalCount % 2));
            else this.remaining=-1;
          })
          .catch(err => console.error('获取邀请记录失败:', err));
    },
    fetchRewardCoupons() {
      // Replace with actual API call
      fetch('http://localhost:8080/api/reward-coupons', {
        headers: {
          Authorization: `Bearer ${this.userInfo.token}`,
          'UserId': this.userInfo.id  // 添加 UserId 请求头
        }
      })
          .then(response => response.json())
          .then(data => {
            this.rewardCoupons = data;
          })
          .catch(err => console.error('获取奖励券明细失败:', err));
    }
  }
};
</script>

<style scoped>
.invitation-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.invitation-card {
  background: white;
  border-radius: 15px;
  padding: 30px;
  width: 100%;
  max-width: 500px;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.invitation-records, .reward-coupons {
  background: white;
  border-radius: 15px;
  padding: 20px;
  width: 100%;
  max-width: 500px;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
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
  z-index: 1000;
}

h2 {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
}

.code-display {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 10px;
  font-size: 2rem;
  text-align: center;
  letter-spacing: 3px;
  margin-bottom: 20px;
  font-family: monospace;
  color: #4a90e2;
}

.copy-btn {
  width: 100%;
  padding: 12px;
  background: #4a90e2;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 30px;
}

h3 {
  color: #666;
  margin-bottom: 15px;
}

ul {
  list-style: none;
  padding: 0;
}

li {
  padding: 10px;
  border-bottom: 1px solid #eee;
}

li:last-child {
  border-bottom: none;
}
</style>
