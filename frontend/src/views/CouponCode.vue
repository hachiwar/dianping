<template>
  <div class="page-container">
    <div class="back-link" @click="goBack">
      <i class="fas fa-arrow-left"></i> 返回
    </div>
    <div class="qr-section">
      <div class="qr-code">
        <qrcode-vue :value="couponCode" :size="200" />
      </div>
      <p class="qr-instruction">请出示此二维码以使用优惠券</p>
    </div>
  </div>
</template>

<script>
import QrcodeVue from 'qrcode.vue';
import axios from 'axios';

export default {
  components: { QrcodeVue },
  data() {
    return {
      orderId: null,
      couponCode: ''
    };
  },
  async mounted() {
    this.orderId = this.$route.params.id;
    await this.fetchOrderDetails();
  },
  methods: {
    async fetchOrderDetails() {
      try {
        const response = await axios.get(`/api/orders/${this.orderId}`);
        this.couponCode = response.data.couponCode;
      } catch (error) {
        console.error('获取订单详情失败:', error);
      }
    },
    goBack() {
      this.$router.go(-1);
    }
  }
};
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  box-sizing: border-box;
}

.back-link {
  align-self: flex-start;
  display: inline-flex;
  align-items: center;
  color: #4a90e2;
  margin-bottom: 20px;
  cursor: pointer;
  font-size: 1rem;
}

.back-link i {
  margin-right: 8px;
}

.qr-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.qr-code {
  margin-bottom: 15px;
}

.qr-instruction {
  font-size: 1rem;
  color: #666;
  text-align: center;
}
</style>