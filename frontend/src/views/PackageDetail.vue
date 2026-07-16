<template>
  <div class="page-container">
    <div class="package-detail">
      <div class="back-link" @click="goBack">
        <i class="fas fa-arrow-left"></i> 返回商家
      </div>
      
      <div class="package-header">
        <div class="package-image">
          <img :src="packageData.imageUrl" :alt="packageData.title">
        </div>
        <div class="package-main-info">
          <h1>{{ packageData.title }}</h1>
          <div class="price-container">
            <span class="current-price">￥{{ packageData.price }}</span>
            <span class="original-price" v-if="packageData.originalPrice">￥{{ packageData.originalPrice }}</span>
            <span class="discount" v-if="packageData.originalPrice">
              {{ Math.round((1 - packageData.price / packageData.originalPrice) * 10) }}折
            </span>
          </div>
          <div class="sales-info">
            <span>已售 {{ packageData.sales }} 份</span>
            <span class="merchant-name">{{ merchantName }}</span>
          </div>
          <button class="buy-button" @click="buyNow">立即购买</button>
        </div>
      </div>
      
      <div class="package-section">
        <h2>套餐详情</h2>
        <div class="package-description">
          {{ packageData.description }}
        </div>
      </div>
      
      <div class="package-section">
        <h2>套餐内容</h2>
        <div class="package-items">
          <div v-for="(item, index) in packageData.items" :key="index" class="package-item">
            <span class="item-name">{{ item.name }}</span>
            <span class="item-quantity">x{{ item.quantity }}</span>
          </div>
        </div>
      </div>
      
      <div class="package-section">
        <h2>使用须知</h2>
        <div class="package-notes">
          <div class="note-item">
            <i class="fas fa-map-marker-alt"></i>
            <div>
              <h3>适用门店</h3>
              <p>{{ packageData.businessName }}</p>
            </div>
          </div>
          <div class="note-item">
            <i class="fas fa-clock"></i>
            <div>
              <h3>使用时间</h3>
              <p>{{ packageData.useTimeDescription || '营业时间内可用' }}</p>
            </div>
          </div>
          <div class="note-item">
            <i class="fas fa-exclamation-circle"></i>
            <div>
              <h3>使用规则</h3>
              <p>{{ packageData.useRules || '需在有效期内使用，过期作废' }}</p>
            </div>
          </div>
        </div>
      </div>
      
      <div class="merchant-info" @click="goToMerchant">
        <i class="fas fa-store"></i>
        <span>{{ merchantName }}</span>
        <i class="fas fa-chevron-right"></i>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      packageId: null,
      packageData: {
        id: '',
        title: '',
        price: 0,
        originalPrice: 0,
        sales: 0,
        businessId: '',
        businessName: '',
        description: '',
        imageUrl: '',
        items: [],
        useTimeDescription: '',
        useRules: ''
      },
      merchantName: ''
    };
  },
  async mounted() {
    this.packageId = this.$route.params.id;
    await this.fetchPackageDetails();
  },
  methods: {
    async fetchPackageDetails() {
      try {
        const response = await axios.get(`/api/packages/${this.packageId}`);
        this.packageData = response.data;
        this.merchantName = this.packageData.businessName;
        
        // 如果图片没有完整URL，添加前缀
        if (this.packageData.imageUrl && !this.packageData.imageUrl.startsWith('http')) {
          this.packageData.imageUrl = `http://localhost:8080${this.packageData.imageUrl}`;
        }
      } catch (error) {
        console.error('获取套餐详情失败:', error);
      }
    },
    buyNow() {
      this.$router.push(`/order-confirmation/${this.packageId}`);
    },
    goBack() {
      this.$router.go(-1);
    },
    goToMerchant() {
      this.$router.push(`/businessDetail/${this.packageData.businessId}`);
    }
  }
};
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.package-detail {
  max-width: 800px;
  margin: 0 auto;
}

.back-link {
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

.package-header {
  display: flex;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.package-image {
  width: 300px;
  height: 250px;
  overflow: hidden;
}

.package-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.package-main-info {
  flex: 1;
  padding: 25px;
  position: relative;
}

.package-main-info h1 {
  font-size: 1.8rem;
  margin: 0 0 20px 0;
  color: #333;
}

.price-container {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 15px;
}

.current-price {
  font-size: 2rem;
  font-weight: bold;
  color: #e53935;
}

.original-price {
  font-size: 1.2rem;
  color: #999;
  text-decoration: line-through;
}

.discount {
  font-size: 1rem;
  color: #fff;
  background: #e53935;
  padding: 3px 8px;
  border-radius: 12px;
}

.sales-info {
  display: flex;
  justify-content: space-between;
  color: #666;
  margin-bottom: 25px;
}

.merchant-name {
  color: #4a90e2;
}

.buy-button {
  width: 100%;
  padding: 15px;
  background: #e53935;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1.2rem;
  cursor: pointer;
  transition: all 0.3s;
}

.buy-button:hover {
  background: #c62828;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(229, 57, 53, 0.3);
}

.package-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.package-section h2 {
  font-size: 1.3rem;
  color: #333;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.package-description {
  font-size: 1.1rem;
  line-height: 1.6;
  color: #444;
}

.package-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.package-item {
  display: flex;
  justify-content: space-between;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 8px;
}

.item-name {
  font-weight: 500;
}

.item-quantity {
  color: #666;
}

.package-notes {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.note-item {
  display: flex;
  gap: 15px;
}

.note-item i {
  font-size: 1.5rem;
  color: #4a90e2;
  margin-top: 5px;
}

.note-item h3 {
  font-size: 1.1rem;
  margin: 0 0 5px 0;
  color: #333;
}

.note-item p {
  font-size: 1rem;
  color: #666;
  margin: 0;
}

.merchant-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  padding: 15px 20px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  cursor: pointer;
  transition: all 0.3s;
}

.merchant-info:hover {
  background: #f8f9fa;
}

.merchant-info i:first-child {
  font-size: 1.2rem;
  color: #4a90e2;
}

.merchant-info span {
  flex: 1;
  margin-left: 10px;
  font-size: 1.1rem;
  color: #333;
}
</style>
