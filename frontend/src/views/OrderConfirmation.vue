<template>
  <div class="page-container">
    <div class="order-confirmation">
      <div class="page-header">
        <div class="back-link" @click="goBack">
          <i class="fas fa-arrow-left"></i> 返回套餐详情
        </div>
        <h1>确认订单</h1>
      </div>

      <div class="order-section package-info">
        <div class="package-image">
          <img :src="packageData.imageUrl" :alt="packageData.title">
        </div>
        <div class="package-details">
          <h2>{{ packageData.title }}</h2>
          <div class="merchant-name">{{ packageData.businessName }}</div>
          <div class="package-price">￥{{ packageData.price.toFixed(2) }}</div>
        </div>
      </div>

      <div class="order-section coupon-section">
        <div class="section-header">
          <h2>优惠券</h2>
          <span v-if="!loading && availableCoupons.length > 0" class="coupon-count">{{ getCouponCount() }}</span>
        </div>

        <div v-if="loading" class="loading-state">
          <i class="fas fa-spinner fa-spin"></i> 加载中...
        </div>

        <div v-else-if="availableCoupons.length === 0" class="no-coupon">
          暂无可用优惠券
        </div>

        <div v-else class="coupon-selector" @click="showCouponModal = true">
          <div v-if="selectedCoupon" class="selected-coupon">
            <div class="coupon-title">{{ selectedCoupon.title }}</div>
            <div class="coupon-amount" :class="getCouponValueClass(selectedCoupon)">
              -￥{{ calculateDiscount(selectedCoupon).toFixed(2) }}
            </div>
          </div>
          <div v-else class="no-selected-coupon">
            {{ availableCoupons.length > 0 ? '选择优惠券' : '暂无可用' }}
          </div>
          <i class="fas fa-chevron-right"></i>
        </div>
      </div>

      <!-- 添加邀请码输入区域 -->
      <div class="order-section">
        <div class="section-header">
          <h2>邀请码</h2>
        </div>
        <div class="invitation-input">
          <input 
            type="text" 
            v-model="invitationCode"
            placeholder="请输入邀请码（选填）"
            @input="validateInvitationCode"
          >
          <span class="invitation-message" :class="{ error: invitationError }">
            {{ invitationMessage }}
          </span>
          <span class="invitation-hint" v-if="finalPrice <= 10">
            订单金额需大于10元才是有效邀请
          </span>
        </div>
      </div>

      <div class="order-section price-calculation">
        <div class="price-item">
          <span>套餐金额</span>
          <span>￥{{ packageData.price.toFixed(2) }}</span>
        </div>
        <div class="price-item discount" v-if="discount > 0">
          <span>优惠券抵扣</span>
          <span :class="getCouponValueClass(selectedCoupon)">-￥{{ discount.toFixed(2) }}</span>
        </div>
        <div class="price-item total">
          <span>实付金额</span>
          <span class="final-price">￥{{ finalPrice.toFixed(2) }}</span>
        </div>
      </div>

      <div class="submit-order">
        <button class="submit-btn" @click="submitOrder" :disabled="submitting">
          {{ submitting ? '提交中...' : `提交订单 ￥${finalPrice.toFixed(2)}` }}
        </button>
      </div>
    </div>

    <!-- 优惠券选择弹窗 -->
    <div class="modal-overlay" v-if="showCouponModal" @click="closeCouponModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>选择优惠券</h2>
          <button class="close-btn" @click="closeCouponModal">
            <i class="fas fa-times"></i>
          </button>
        </div>

        <div class="coupon-list">
          <!-- 不使用优惠券选项 -->
          <div class="coupon-option no-coupon-option" @click="selectCoupon(null)">
            <div class="radio-button">
              <div class="radio-inner" :class="{active: !selectedCoupon}"></div>
            </div>
            <div class="coupon-text">不使用优惠券</div>
          </div>

          <!-- 优惠券列表（包括可用和不可用） -->
          <div
            v-for="coupon in availableCoupons"
            :key="coupon.id"
            class="coupon-option"
            :class="{'disabled': !isCouponUsable(coupon)}"
            @click="isCouponUsable(coupon) && selectCoupon(coupon)"
          >
            <div class="radio-button">
              <div class="radio-inner" :class="{
                active: selectedCoupon && selectedCoupon.id === coupon.id,
                disabled: !isCouponUsable(coupon)
              }"></div>
            </div>

            <div class="coupon-card" :class="[getCouponClass(coupon), {'disabled': !isCouponUsable(coupon)}]">
              <div class="coupon-left">
                <div class="coupon-value">{{ formatCouponValueDisplay(coupon) }}</div>
                <div class="coupon-name">{{ coupon.couponName }}</div>
                <div class="coupon-condition" v-if="coupon.minAmount > 0">
                  满{{ coupon.minAmount }}元可用
                </div>
                <div class="coupon-condition" v-else>
                  无门槛
                </div>
              </div>

              <div class="coupon-right">
                <div class="coupon-title">{{ coupon.title }}</div>
                <div class="coupon-desc">{{ getCouponDescription(coupon) }}</div>
                <!-- 添加不可用原因提示 -->
                <div v-if="!isCouponUsable(coupon)" class="coupon-unusable-reason">
                  {{ getUnusableReason(coupon) }}
                </div>
                <div class="coupon-validity">{{ formatValidity(coupon) }}</div>
                <div class="coupon-discount-preview">
                  预计优惠: ￥{{ calculateDiscount(coupon).toFixed(2) }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import { NULL } from 'sass';

export default {
  data() {
    return {
      packageId: null,
      packageData: {
        id: '',
        title: '',
        price: 0,
        businessId: '', // 后端需要的是商家ID
        businessName: '', // 前端显示用
        imageUrl: '',
        category: '' // 套餐所属品类，用于优惠券匹配
      },
      merchantData: {
        id: '',
        merchantName: '',
        category: '', // 商家品类，用于优惠券匹配
        rating: 0,
        address: '',
        avgPrice: 0,
        // 其他商家信息
      },
      availableCoupons: [],
      selectedCoupon: null,
      loading: true,
      showCouponModal: false,
      userInfo: null,
      submitting: false, // 防止重复提交
      invitationCode: '',
      invitationMessage: '',
      invitationError: false,
      invitationValid: false
    }
  },
  computed: {
    // 添加新的计算属性：实际可用的优惠券列表 
    usableCoupons() {
      return this.availableCoupons.filter(this.isCouponUsable);
    },
    // 计算当前选定优惠券的折扣金额
    discount() {
      if (!this.selectedCoupon) return 0;
      return this.calculateDiscount(this.selectedCoupon);
    },
    // 计算最终支付价格
    finalPrice() {
      // 确保最终价格不小于0元
      const price = this.packageData.price - this.discount;
      return Math.max(price, 0);
    }
  },
  async created() { // 使用 created 代替 mounted，确保数据在模板渲染前获取
    this.packageId = this.$route.params.packageId;
    const storedUserInfo = localStorage.getItem('userInfo');
    if (!storedUserInfo) {
      // 处理未登录情况，例如跳转到登录页
      console.error("用户未登录");
      this.$router.push('/login'); // 假设登录页路由为 /login
      return;
    }
    this.userInfo = JSON.parse(storedUserInfo);

    await this.fetchPackageDetails(); // 先获取套餐详情，拿到价格和商家信息

    // 确保获取到套餐信息后再获取优惠券
    if (this.packageData.id) {
      await this.fetchAvailableCoupons();
      this.selectBestCoupon(); // 获取完优惠券后尝试自动选择最优
    } else {
      this.loading = false; // 如果套餐信息获取失败，停止加载
    }
  },
  methods: {
    async fetchPackageDetails() {
      try {
        const response = await axios.get(`/api/packages/${this.packageId}`);
        this.packageData = response.data;
        this.packageData.price = Number(this.packageData.price) || 0;

        // 获取商家信息
        if (this.packageData.merchantId) {
          const merchantResponse = await axios.get(`/api/businesses/${this.packageData.merchantId}`);
          this.merchantData = merchantResponse.data;
          // 更新商家名称到 packageData
          this.packageData.businessName = this.merchantData.merchantName;
        }

        // 处理图片URL
        if (this.packageData.imageUrl && !this.packageData.imageUrl.startsWith('http')) {
          this.packageData.imageUrl = `http://localhost:8080${this.packageData.imageUrl}`;
        }
      } catch (error) {
        console.error('获取套餐详情失败:', error);
        // 可以添加用户提示
      }
    },
    async fetchAvailableCoupons() {
      this.loading = true;
      try {
        const response = await axios.get('/api/coupons/all', {
          params: {
            businessId: this.merchantData.id,
            amount: this.packageData.price,
            category: this.merchantData.category // 使用商家的品类
          },
          headers: {
            'UserId': this.userInfo.id
          }
        });

        this.availableCoupons = response.data;
      } catch (error) {
        console.error('获取可用优惠券失败:', error);
        this.availableCoupons = [];
      } finally {
        this.loading = false;
      }
    },
    // 根据优惠券计算实际折扣金额
    calculateDiscount(coupon) {
      if (!coupon) return 0;

      const packagePrice = this.packageData.price;
      
      // 检查品类匹配
      if (coupon.category && coupon.category !== this.merchantData.category) {
        return 0; // 品类不匹配时返回0
      }

      let discountAmount = 0;
      switch (coupon.type) {
        case '满减': // 满减券
          discountAmount = Math.min(coupon.value, packagePrice);
          break;

        case '立减': // 立减券
          discountAmount = Math.min(coupon.value, packagePrice); // 直接减去固定金额
          break;

        case '折扣': // 折扣券
          const discountValue = coupon.value;
          const potentialDiscountPercent = packagePrice * (1 - discountValue / 10);
          discountAmount = coupon.maxDiscount > 0
            ? Math.min(potentialDiscountPercent, coupon.maxDiscount)
            : potentialDiscountPercent;
          break;
        
        case '免单': // 免单券
          discountAmount = Math.min(packagePrice, 20); // 免单券直接减去全部价格
          break;

        case '秒杀':
          discountAmount = Math.min(packagePrice-0.1, 20); // 秒杀券直接减去全部价格
          break;
        default:
          discountAmount = 0;
      }
      // 保证折扣金额不为负数且不超过原价
      return Math.max(0, Math.min(discountAmount, packagePrice));
    },
    // 修改优惠券选择逻辑，使用过滤后的列表
    selectBestCoupon() {
      if (this.usableCoupons.length === 0) {
        this.selectedCoupon = null;
        return;
      }

      let bestCoupon = null;
      let maxDiscount = 0;

      this.usableCoupons.forEach(coupon => {
        const currentDiscount = this.calculateDiscount(coupon);
        if (currentDiscount > maxDiscount) {
          maxDiscount = currentDiscount;
          bestCoupon = coupon;
        }
      });

      this.selectedCoupon = bestCoupon;
    },
    // 格式化优惠券面值显示 (弹窗和已选区域)
    formatCouponValueDisplay(coupon) {
      switch (coupon.type) {
        case '满减':
          return `￥${coupon.value}`;
        case '立减':
          // 显示 "减至X元" 可能更清晰，或者直接显示优惠金额
          // return `减至￥${coupon.value}`;
          return `￥${this.calculateDiscount(coupon).toFixed(2)}`; // 直接显示优惠额
        case '折扣':
          return `${coupon.value}折`;
        case '免单': // 新增免单券的显示逻辑
          return '免单';
        default:
          return '';
      }
    },
    // 获取优惠券值的样式类 (用于区分颜色)
    getCouponValueClass(coupon) {
       if (!coupon) return '';
       switch (coupon.type) {
         case '满减': return 'fixed-amount-text';
         case '立减': return 'fix-to-amount-text';
         case '折扣': return 'percentage-text';
         case '免单': return 'free-order-text'; // 新增免单券的颜色
         case '秒杀': return 'flash-sale-text'; // 新增秒杀券的样式类
         default: return '';
       }
    },
    getCouponDescription(coupon) {
      let description = [];
      
      if (coupon.couponName) {
        description.push(coupon.couponName);
      }

      if (coupon.category) {
        description.push(`限${coupon.category}商家使用`);
      }
      
      if (coupon.minAmount > 0) {
        description.push(`满${coupon.minAmount}元可用`);
      }

      return description.join(' | ');
    },
    formatValidity(coupon) {
      if (coupon.expireTime) {
        return new Date(coupon.expireTime).toLocaleDateString();
      }
      return '长期有效';
    },
    getCouponClass(coupon) {
      switch (coupon.type) {
        case '满减':
          return 'fixed-amount';
        case '立减':
          return 'fix-to-amount';
        case '折扣':
          return 'percentage';
        case '免单': // 新增免单券的样式类
          return 'free-order';
        case '秒杀': return 'flash-sale'; // 新增秒杀券的样式类
        default:
          return '';
      }
    },
    selectCoupon(coupon) {
      this.selectedCoupon = coupon;
      this.showCouponModal = false;
    },
    closeCouponModal() {
      this.showCouponModal = false;
    },
    goBack() {
      this.$router.go(-1);
    },
    async validateInvitationCode() {
      if (!this.invitationCode) {
        this.invitationMessage = '';
        this.invitationError = false;
        this.invitationValid = false;
        return;
      }

      // 检查是否是自己的邀请码
      if (this.invitationCode === this.userInfo.invitationCode) {
        this.invitationMessage = '不能使用自己的邀请码';
        this.invitationError = true;
        this.invitationValid = false;
        return;
      }
      
      try {
        const response = await axios.get('/users/validate-invitation', {
          params: {
            invitationCode: this.invitationCode
          }
        });
        
        if (response.data.valid) {
          this.invitationMessage = `邀请人: ${response.data.inviterUsername}`;
          this.invitationError = false;
          this.invitationValid = true;
        } else {
          this.invitationMessage = response.data.message;
          this.invitationError = true;
          this.invitationValid = false;
        }

        // 如果订单金额小于10元，添加提示信息
        if (this.finalPrice <= 10 && this.invitationValid) {
          this.invitationMessage += '';
        }
      } catch (error) {
        this.invitationMessage = error.response?.data?.message || '验证邀请码失败';
        this.invitationError = true;
        this.invitationValid = false;
      }
    },
    async submitOrder() {
      if (this.submitting) return;
      
      // 修改邀请码相关验证
      if (this.invitationCode) {
        if (this.invitationCode === this.userInfo.invitationCode) {
          this.$message.error('不能使用自己的邀请码');
          return;
        }
        if (!this.invitationValid) {
          this.$message.error('请输入有效的邀请码');
          return;
        }
      }

      this.submitting = true;

      try {
        const orderData = {
          packageId: this.packageId,
          packagePrice: this.packageData.price,
          businessId: this.merchantData.id,
          couponId: this.selectedCoupon ? this.selectedCoupon.id : null,
          discount: this.discount,
          finalPrice: this.finalPrice,
          invitationCode: this.invitationValid ? this.invitationCode : null
        };

        console.log('提交的订单数据:', orderData); // 添加日志

        const response = await axios.post('/api/orders', orderData, {
          headers: {
            'UserId': this.userInfo.id,
            'Content-Type': 'application/json'
          }
        });

        console.log('服务器响应:', response.data); // 添加日志

        if (response.data && response.data.orderId) {
          this.$message.success('下单成功！');
          this.$router.push({
            path: '/my-orders',
            query: { highlight: response.data.orderId }
          });
        }
      } catch (error) {
        console.error('提交订单失败:', error);
        console.error('错误详情:', error.response?.data); // 添加详细错误信息
        this.$message.error(error.response?.data?.message || '订单提交失败，请重试');
      } finally {
        this.submitting = false;
      }
    },
    // 添加新方法：检查优惠券是否可用
    isCouponUsable(coupon) {
      // 检查商家匹配
      if (coupon.category == null && coupon.shopId && coupon.shopId !== this.merchantData.id) {
          return false; // 优惠券仅限特定商家使用
      }

      // 检查品类匹配
      if (coupon.category && coupon.category !== this.merchantData.category) {  
          return false; // 优惠券仅限特定品类商家使用
      }
      
      // 检查满减门槛
      if (this.packageData.price < coupon.minAmount) {
        return false;
      }

      // 是否使用
      if (coupon.couponAmount == 0) {
      return false;
      }

      // 检查过期时间
        if (coupon.expireTime) {
        const now = new Date();
        const expireTime = new Date(coupon.expireTime);
        if (expireTime < now) {
          return false; // 优惠券已过期
        }
      }

      return true;
    },
    // 添加新方法：获取优惠券不可用原因
    getUnusableReason(coupon) {
      if (coupon.category == null && coupon.shopId && coupon.shopId !== this.merchantData.id) {
        return `仅限${coupon.shopId}商家使用`;
      }
      if (coupon.category && coupon.category !== this.merchantData.category) {
        return `仅限${coupon.category}商家使用`;
      }
      if (coupon.minAmount && this.packageData.price < coupon.minAmount) {
        return `未满${coupon.minAmount}元`;
      }
      if (coupon.couponAmount == 0) {
        return '优惠券已使用';
      }
      if (coupon.expireTime) {
        const now = new Date();
        const expireTime = new Date(coupon.expireTime);
        if (expireTime < now) {
          return '优惠券已过期';
        }
      }
      return '';
    },
    // 修改优惠券数量显示逻辑
    getCouponCount() {
      const usableCount = this.availableCoupons.filter(this.isCouponUsable).length;
      const totalCount = this.availableCoupons.length;
      return `${usableCount}/${totalCount}张可用`;
    }
  }
}
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
  box-sizing: border-box; /* 确保 padding 不会撑大容器 */
}

.order-confirmation {
  max-width: 800px;
  margin: 0 auto;
  padding-bottom: 80px; /* 为底部提交按钮留出空间 */
}

.page-header {
  margin-bottom: 20px;
  padding: 0 10px; /* 左右留白 */
}

.back-link {
  display: inline-flex;
  align-items: center;
  color: #4a90e2;
  margin-bottom: 10px;
  cursor: pointer;
  font-size: 1rem;
}

.back-link i {
  margin-right: 8px;
}

.page-header h1 {
  font-size: 1.8rem;
  margin: 0;
  color: #333;
}

.order-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin: 0 10px 15px 10px; /* 左右留白 */
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.package-info {
  display: flex;
  gap: 20px;
  align-items: center; /* 垂直居中 */
}

.package-image {
  width: 100px; /* 调整图片大小 */
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0; /* 防止图片被压缩 */
}

.package-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.package-details {
  flex: 1;
  min-width: 0; /* 防止文字过长撑开布局 */
}

.package-details h2 {
  font-size: 1.2rem; /* 调整字体大小 */
  margin: 0 0 8px 0;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis; /* 超长显示省略号 */
}

.merchant-name {
  color: #666;
  font-size: 0.9rem; /* 调整字体大小 */
  margin-bottom: 10px;
}

.package-price {
  font-size: 1.3rem; /* 调整字体大小 */
  font-weight: bold;
  color: #e53935;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-header h2 {
  font-size: 1.1rem; /* 调整字体大小 */
  margin: 0;
  color: #333;
}

.coupon-count {
  color: #e53935;
  font-weight: 500;
  font-size: 0.9rem;
}

.coupon-selector {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px; /* 调整内边距 */
  background: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  min-height: 40px; /* 保证一定高度 */
}

.selected-coupon {
  display: flex;
  flex-direction: column;
  flex: 1; /* 占据剩余空间 */
  min-width: 0;
}

.coupon-title {
  font-weight: 500;
  color: #333;
  font-size: 0.95rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.coupon-amount {
  font-weight: bold;
  margin-top: 3px;
  font-size: 0.9rem;
}
/* 不同类型优惠券抵扣金额的颜色 */
.fixed-amount-text { color: #4a90e2; }
.fix-to-amount-text { color: #e53935; }
.percentage-text { color: #ff9800; }
.free-order-text { color: #4caf50; } /* 新增免单券的颜色 */
.flash-sale-text {
  color: #ff5722; /* 橙色表示秒杀券 */
}


.no-selected-coupon {
  color: #999;
  font-size: 0.95rem;
}

.coupon-selector i {
  color: #ccc;
  margin-left: 10px;
}

.no-coupon, .loading-state {
  color: #999;
  text-align: center;
  padding: 15px 0;
  font-size: 0.95rem;
}

.loading-state i {
  margin-right: 5px;
}

.price-calculation {
  padding-bottom: 10px;
}

.price-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px; /* 调整间距 */
  color: #666;
  font-size: 0.95rem; /* 调整字体大小 */
}

.price-item span:last-child {
  font-weight: 500;
  color: #333;
}

.price-item.discount span:last-child {
  font-weight: bold;
}

.price-item.total {
  font-size: 1.1rem; /* 调整字体大小 */
  font-weight: bold;
  color: #333;
  padding-top: 15px;
  border-top: 1px solid #eee;
  margin-top: 10px;
  margin-bottom: 0;
}
.price-item.total span:last-child {
  color: #e53935; /* 最终价格用醒目颜色 */
  font-size: 1.3rem;
}


.submit-order {
  position: fixed; /* 固定在底部 */
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 15px 20px;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.1);
  z-index: 100; /* 确保在最上层 */
}

.submit-btn {
  width: 100%;
  padding: 14px; /* 调整按钮大小 */
  background: linear-gradient(90deg, #ff758c 0%, #ff7eb3 100%); /* 渐变色 */
  color: white;
  border: none;
  border-radius: 25px; /* 圆角 */
  font-size: 1.1rem; /* 调整字体大小 */
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(255, 126, 179, 0.4);
}

.submit-btn:hover:not(:disabled) {
  opacity: 0.9;
  box-shadow: 0 6px 16px rgba(255, 126, 179, 0.5);
}
.submit-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  box-shadow: none;
}


/* 优惠券选择弹窗 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6); /* 加深背景 */
  display: flex;
  justify-content: center;
  align-items: flex-end; /* 从底部弹出 */
  z-index: 1000;
}

.modal-content {
  width: 100%;
  max-height: 70vh; /* 限制最大高度 */
  background: #f5f7fa; /* 弹窗背景色 */
  border-radius: 16px 16px 0 0; /* 顶部圆角 */
  box-shadow: 0 -5px 20px rgba(0,0,0,0.1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  animation: slide-up 0.3s ease-out; /* 添加动画 */
}

@keyframes slide-up {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}


.modal-header {
  display: flex;
  justify-content: center; /* 标题居中 */
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
  position: relative; /* 为了关闭按钮定位 */
  background: white; /* 头部背景 */
}

.modal-header h2 {
  font-size: 1.1rem; /* 调整字体 */
  margin: 0;
  color: #333;
  font-weight: 500;
}

.close-btn {
  position: absolute; /* 绝对定位到右上角 */
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  font-size: 1.3rem; /* 增大图标 */
  color: #999;
  cursor: pointer;
  padding: 5px; /* 增大点击区域 */
}

.coupon-list {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
}

.coupon-option {
  display: flex;
  align-items: flex-start; /* 让 radio 和卡片顶部对齐 */
  gap: 10px; /* 调整间距 */
  margin-bottom: 15px;
  cursor: pointer;
  background: white; /* 每个选项背景 */
  border-radius: 8px;
  padding: 10px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.coupon-option.no-coupon-option {
  align-items: center; /* “不使用”选项垂直居中 */
  padding: 15px 10px;
}


.radio-button {
  width: 18px; /* 调整大小 */
  height: 18px;
  border-radius: 50%;
  border: 1.5px solid #ccc; /* 调整边框 */
  display: flex;
  justify-content: center;
  align-items: center;
  flex-shrink: 0;
  margin-top: 5px; /* 微调位置 */
}
.no-coupon-option .radio-button {
  margin-top: 0; /* “不使用”选项 radio 垂直居中 */
}

.radio-inner {
  width: 10px; /* 调整大小 */
  height: 10px;
  border-radius: 50%;
  background: transparent;
  transition: background-color 0.2s;
}

.radio-inner.active {
  background: #4a90e2;
}

.radio-inner.disabled {
  border-color: #ccc;
  background-color: #f5f5f5;
}

.coupon-text {
  font-size: 1rem;
  color: #333; /* 调整颜色 */
  flex: 1;
}

.coupon-card {
  flex: 1;
  display: flex;
  border-radius: 6px; /* 调整圆角 */
  overflow: hidden;
  min-height: 90px; /* 调整高度 */
  position: relative;
  border: 1px solid #eee; /* 添加边框 */
}

.coupon-card.disabled {
  opacity: 0.8;
}

.coupon-card.disabled .coupon-left {
  background: #999 !important;
}

.coupon-card.disabled .coupon-right {
  background: #f5f5f5;
}

.coupon-left {
  width: 85px; /* 调整宽度 */
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 10px 5px; /* 调整内边距 */
  text-align: center;
  position: relative; /* 为了虚线效果 */
}
/* 添加右侧虚线效果 */
.coupon-left::after {
    content: '';
    position: absolute;
    top: 5px; /* 调整虚线位置 */
    bottom: 5px;
    right: -1px; /* 盖住边框 */
    width: 2px; /* 虚线宽度 */
    background: linear-gradient(to bottom, white 5px, transparent 5px, transparent 10px);
    background-size: 100% 15px; /* 控制虚线间隔 */
}


.coupon-right {
  flex: 1;
  background: white; /* 右侧背景 */
  padding: 10px 12px; /* 调整内边距 */
  display: flex;
  flex-direction: column;
  justify-content: space-between; /* 内容上下分布 */
  min-width: 0;
}

.coupon-value {
  font-size: 1.2rem; /* 调整字体 */
  font-weight: bold;
  margin-bottom: 3px; /* 调整间距 */
  line-height: 1.2;
}

.coupon-name {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 4px;
  word-break: break-all;
  text-align: center;
}

.coupon-condition {
  font-size: 0.75rem; /* 调整字体 */
  opacity: 0.9;
}

.coupon-title {
  font-size: 1rem; /* 调整字体 */
  font-weight: 500;
  margin-bottom: 5px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.coupon-desc {
  font-size: 0.8rem; /* 调整字体 */
  color: #888; /* 调整颜色 */
  margin-bottom: 8px;
  line-height: 1.3;
}

.coupon-validity {
  font-size: 0.75rem; /* 调整字体 */
  color: #aaa; /* 调整颜色 */
}

.coupon-discount-preview {
  font-size: 0.8rem;
  color: #e53935;
  font-weight: 500;
  margin-top: 5px;
}

.coupon-unusable-reason {
  color: #ff4d4f;
  font-size: 0.75rem;
  margin-top: 4px;
  padding: 2px 0;
}

/* 不同类型优惠券的样式 */
.fixed-amount .coupon-left { background: #4a90e2; }
.fix-to-amount .coupon-left { background: #e53935; }
.percentage .coupon-left { background: #ff9800; }
.free-order .coupon-left { background: #4caf50; } /* 新增免单券的样式 */
.flash-sale .coupon-left {
  background: #ff5722; /* 橙色背景 */
}


.coupon-option.disabled {
  opacity: 0.8;
  cursor: not-allowed;
}

.invitation-input {
  margin-top: 10px;
}

.invitation-input input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.invitation-message {
  display: block;
  font-size: 12px;
  margin-top: 5px;
  color: #52c41a;
}

.invitation-message.error {
  color: #ff4d4f;
}

.invitation-hint {
  display: block;
  font-size: 12px;
  color: #4d7aff;
  margin-top: 5px;
}

.invitation-input input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}
</style>
