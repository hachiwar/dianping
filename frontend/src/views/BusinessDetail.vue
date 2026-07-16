<template>
<div class="page-container">
  <div class="business-detail">
    <div class="cover-image-container">
      <div class="cover-overlay"></div>
      <img :src="coverImage" :alt="business.merchantName" class="cover-image">
      <div class="back-btn" @click="$router.go(-1)">
        <i class="fas fa-arrow-left"></i>
      </div>
      <h1 class="business-title">{{ business.merchantName }}</h1>
    </div>

    <div class="content-wrapper">
      <div class="info-section">
        <div class="rating-box">
          <span class="stars">{{ renderStars(business.rating) }}</span>
          <span class="rating-value">{{ business.rating }}分</span>
        </div>
        
        <div class="info-grid">
          <div class="info-item">
            <i class="fas fa-yen-sign"></i>
            <div class="info-text">
              <label>人均消费</label>
              <span>￥{{ business.avgPrice }}</span>
            </div>
          </div>
          
          <div class="info-item">
            <i class="fas fa-map-marker-alt"></i>
            <div class="info-text">
              <label>地址</label>
              <span>{{ business.address }}</span>
            </div>
          </div>
          
          <div class="info-item">
            <i class="fas fa-clock"></i>
            <div class="info-text">
              <label>营业时间</label>
              <span>{{ business.businessHours }}</span>
            </div>
          </div>
          
          <div class="info-item">
            <i class="fas fa-phone"></i>
            <div class="info-text">
              <label>联系电话</label>
              <span>{{ business.telephone }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 团购套餐列表 -->
      <div class="packages-section">
        <h3><i class="fas fa-tag"></i> 团购套餐</h3>
        <div v-if="packages.length === 0" class="no-packages">
          暂无可用团购套餐
        </div>
        <div v-else class="packages-list">
          <div 
            v-for="pkg in packages" 
            :key="pkg.id" 
            class="package-item"
            @click="goToPackageDetail(pkg.id)"
          >
            <div class="package-image">
              <img :src="pkg.imageUrl" :alt="pkg.title">
            </div>
            <div class="package-info">
              <h4>{{ pkg.title }}</h4>
              <div class="package-price">
                <span class="current-price">￥{{ pkg.price }}</span>
                <span class="original-price" v-if="pkg.originalPrice">￥{{ pkg.originalPrice }}</span>
              </div>
              <div class="package-sales">已售 {{ pkg.sales }} 份</div>
            </div>
          </div>
        </div>
      </div>

      <div class="description-section">
        <h3><i class="fas fa-store"></i> 商家特色</h3>
        <p>{{ business.description }}</p>
      </div>

      <div class="gallery-section">
        <h3><i class="fas fa-images"></i> 店铺实景</h3>
        <div class="gallery">
          <div v-for="(photo, index) in processedPhotos" 
               :key="index" 
               class="gallery-item"
               @click="showFullImage(photo)">
            <img :src="photo" alt="商家图片">
          </div>
        </div>
      </div>

      <!-- 添加评论区 -->
      <div class="reviews-section">
        <div class="reviews-header">
          <h3><i class="fas fa-comment"></i> 用户评论</h3>
          <el-button type="primary" size="small" @click="showReviewDialog">
            <i class="fas fa-pen"></i> 我要评论
          </el-button>
        </div>

        <div v-if="reviews.length === 0" class="no-reviews">
          暂无评论，快来发表第一条评论吧！
        </div>
        
        <div v-else class="reviews-list">
          <!-- 显示所有评论 -->
          <div v-for="review in filterTopLevelReviews()" :key="review.id" class="review-item">            
            <div class="review-content-wrapper">
              <div class="review-avatar">
                <div class="avatar-circle" :style="{ backgroundColor: getUserColor(review.userID) }">
                  {{ getUserInitial(review.userID) }}
                </div>
              </div>
              <div class="review-content">
                <div class="review-header">
                  <span class="review-username">{{ getUserName(review.userID) }}</span>
                  <span class="review-rating">{{ renderStars(review.rating) }}</span>
                  <span class="review-date">{{ formatDate(review.createTime) }}</span>
                </div>
                <div class="review-text">{{ review.comment }}</div>
                
                <div class="review-actions">
                  <span class="reply-button" @click="showReplyDialog(review)">
                    <i class="fas fa-reply"></i> 回复
                  </span>
                  
                  <!-- 如果有回复，显示查看回复按钮 -->
                  <span v-if="hasReplies(review.id)" class="view-replies" @click="toggleReplies(review.id)">
                    <i :class="['fas', isRepliesVisible(review.id) ? 'fa-chevron-up' : 'fa-chevron-down']"></i>
                    {{ isRepliesVisible(review.id) ? '收起回复' : `查看回复(${countReplies(review.id)})` }}
                  </span>
                </div>
                
                <!-- 显示评论的回复 -->
                <review-replies 
                  v-if="hasReplies(review.id) && isRepliesVisible(review.id)"
                  :review-id="review.id"
                  :all-reviews="reviews"
                  :users="users"
                  :replies-visible="repliesVisible"
                  @reply="showReplyDialog"
                  @toggle-replies="toggleReplies"
                ></review-replies>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 新增评论对话框 -->
  <el-dialog
    v-model="reviewDialogVisible"
    :title="isReply ? '回复评论' : '发表评论'"
    width="500px"
  >
    <el-form :model="newReview" label-width="80px">
      <el-form-item v-if="!isReply" label="评分">
        <el-rate
          v-model="newReview.rating"
          :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
          :texts="['很差', '较差', '一般', '不错', '很好']"
          show-text
        ></el-rate>
      </el-form-item>
      <el-form-item label="评论">
        <el-input
          type="textarea"
          v-model="newReview.comment"
          :rows="4"
          placeholder="请输入您的评论..."
        ></el-input>
        <div v-if="isReply" class="reply-to-info">
          回复给: {{ getUserName(replyToReview.userID) }}
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交</el-button>
      </span>
    </template>
  </el-dialog>
</div>
</template>

<script>
import axios from 'axios';
import ReviewReplies from '@/components/ReviewReplies.vue';

export default {
  components: {
    ReviewReplies
  },
  data() {
    return {
      business: {
        merchantName: '',
        rating: 0,
        avgPrice: 0,
        address: '',
        businessHours: '',
        telephone: '',
        description: '',
        coverUrl: '',
        photoUrls: [],
      },
      packages: [],
      // 评论相关数据
      reviews: [],
      users: {},
      reviewDialogVisible: false,
      isReply: false,
      replyToReview: null,
      newReview: {
        rating: 5,
        comment: '',
        parentID: null
      },
      // 记录哪些评论的回复是展开的
      repliesVisible: []
    }
  },
  computed: {
    // 处理封面图路径
    coverImage() {
      return `http://localhost:8080${this.business.coverUrl}`
    },
    // 处理详情图路径
    processedPhotos() {
      if (!this.business.photoUrls || this.business.photoUrls.length === 0) return [];

      const rawUrls = Array.isArray(this.business.photoUrls)
          ? this.business.photoUrls
          : this.business.photoUrls.split(',');

      return rawUrls.map(url => {
        const trimmedUrl = url.trim();
        return trimmedUrl.startsWith('http')
            ? trimmedUrl
            : `http://localhost:8080${trimmedUrl}`;
      });
    },
    // 筛选顶级评论（没有父评论的评论）
    topLevelReviews() {
      console.log('筛选顶级评论，全部评论:', this.reviews);
      const topReviews = this.reviews.filter(review => {
        // 注意：如果parentID是null或undefined，表示是顶级评论
        const isTopLevel = !review.parentID;
        console.log(`评论ID: ${review.id}, parentID: ${review.parentID}, 是否顶级: ${isTopLevel}`);
        return isTopLevel;
      });
      console.log('顶级评论:', topReviews);
      return topReviews;
    }
  },
  async mounted() {
    try {
      const id = this.$route.params.id;
      const response = await axios.get(`/api/businesses/${id}`);
      this.business = response.data;
      
      // 加载用户名缓存
      this.loadUserNameCache();
      
      // 获取该商户的套餐列表
      this.fetchPackages(id);
      
      // 获取该商户的评论列表
      this.fetchReviews(id);
    } catch (error) {
      console.error('加载商家详情失败:', error);
    }
  },
  methods: {
    renderStars(rating) {
      const fullStars = Math.floor(rating);
      const halfStar = rating % 1 >= 0.5 ? 1 : 0;
      return '★'.repeat(fullStars) + (halfStar ? '☆' : '');
    },
    showFullImage(photo) {
      // 创建一个全屏图片预览容器
      const previewContainer = document.createElement('div');
      previewContainer.classList.add('image-preview-container');
      
      // 创建关闭按钮
      const closeBtn = document.createElement('div');
      closeBtn.classList.add('preview-close-btn');
      closeBtn.innerHTML = '<i class="fas fa-times"></i>';
      
      // 创建图片元素
      const image = document.createElement('img');
      image.src = photo;
      image.alt = '商家图片预览';
      image.classList.add('preview-image');
      
      // 添加到容器
      previewContainer.appendChild(closeBtn);
      previewContainer.appendChild(image);
      document.body.appendChild(previewContainer);
      
      // 防止滚动
      document.body.style.overflow = 'hidden';
      
      // 添加关闭事件
      closeBtn.addEventListener('click', () => {
        previewContainer.classList.add('closing');
        setTimeout(() => {
          document.body.removeChild(previewContainer);
          document.body.style.overflow = '';
        }, 300);
      });
      
      // 点击背景也可以关闭
      previewContainer.addEventListener('click', (e) => {
        if (e.target === previewContainer) {
          previewContainer.classList.add('closing');
          setTimeout(() => {
            document.body.removeChild(previewContainer);
            document.body.style.overflow = '';
          }, 300);
        }
      });
      
      // 添加动画效果
      setTimeout(() => {
        previewContainer.classList.add('active');
      }, 10);
    },
    async fetchPackages(businessId) {
      try {
        const response = await axios.get(`/api/packages/business/${businessId}`);
        this.packages = response.data;
        this.packages.forEach(pkg => {
          if (pkg.imageUrl && !pkg.imageUrl.startsWith('http')) {
            pkg.imageUrl = `http://localhost:8080${pkg.imageUrl}`;
          }
        });

      } catch (error) {
        console.error('获取套餐列表失败:', error);
        this.packages = [];
      }
    },
    goToPackageDetail(packageId) {
      this.$router.push(`/package/${packageId}`);
    },
    
    // 评论相关方法
    async fetchReviews(merchantId) {
      try {
        console.log(`开始获取商家评论，商家ID: ${merchantId}`);
        const response = await axios.get(`/api/reviews/merchant/${merchantId}`);
        console.log('评论获取结果:', response.data);
        this.reviews = response.data;
        
        // 检查评论数据中是否包含username字段，有则更新用户缓存
        let hasUpdatedCache = false;
        this.reviews.forEach(review => {
          if (review.username && (!this.users[review.userID] || this.users[review.userID].username === `用户${review.userID}`)) {
            this.users[review.userID] = {
              id: review.userID,
              username: review.username
            };
            hasUpdatedCache = true;
            console.log(`从评论中获取到用户名 (ID: ${review.userID}): ${review.username}`);
          }
        });
        
        // 如果有更新，保存缓存
        if (hasUpdatedCache) {
          this.saveUserNameCache();
        }
        
        // 获取所有评论中出现的用户ID
        const userIds = [...new Set(this.reviews.map(review => review.userID))];
        console.log('评论中的用户IDs:', userIds);
        
        // 获取所有用户信息
        for (const userId of userIds) {
          await this.fetchUserInfo(userId);
        }
      } catch (error) {
        console.error('获取评论列表失败:', error);
        this.reviews = [];
      }
    },
    
    async fetchUserInfo(userId) {
      if (this.users[userId]) return;
      
      try {
        // 首先尝试从localStorage中获取当前用户信息
        const currentUserInfo = JSON.parse(localStorage.getItem('userInfo'));
        
        // 如果是当前登录用户，直接使用本地存储的用户信息
        if (currentUserInfo && currentUserInfo.id === userId) {
          console.log(`使用本地缓存的用户信息 (ID: ${userId}):`, currentUserInfo);
          this.users[userId] = {
            id: currentUserInfo.id,
            username: currentUserInfo.username
          };
          
          // 保存到缓存中
          this.saveUserNameCache();
          return;
        }
        
        // 对于其他用户，我们创建一个默认的用户信息对象
        console.log(`创建默认用户信息 (ID: ${userId})`);
        this.users[userId] = { 
          id: userId,
          username: `用户${userId}`
        };
      } catch (error) {
        console.error(`获取/创建用户信息失败 (ID: ${userId}):`, error);
        this.users[userId] = { 
          id: userId,
          username: `用户${userId}`
        };
      }
    },
    
    getUserName(userId) {
      // 对于已缓存的用户，直接使用其信息
      if (this.users[userId] && this.users[userId].username) {
        return this.users[userId].username;
      }
      
      // 如果无法获取用户名，返回默认值
      return `用户${userId}`;
    },
    
    formatDate(dateString) {
      if (!dateString) return '';
      
      const date = new Date(dateString);
      return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
    },
    
    getChildReviews(parentId) {
      console.log(`获取父评论ID ${parentId} 的子评论`, this.reviews);
      // 转换为数字类型进行比较，防止字符串比较问题
      const childReviews = this.reviews.filter(review => {
        // 将两者都转换为相同类型后比较
        const reviewParentId = review.parentID;
        const isChild = reviewParentId !== null && reviewParentId !== undefined && 
                       (reviewParentId === parentId || 
                        Number(reviewParentId) === Number(parentId));
        console.log(`评论ID: ${review.id}, parentID: ${review.parentID}, 是否子评论: ${isChild}`);
        return isChild;
      });
      console.log(`父评论ID ${parentId} 的子评论:`, childReviews);
      return childReviews;
    },
    
    showReviewDialog() {
      // 检查用户是否登录
      const userInfo = JSON.parse(localStorage.getItem('userInfo'));
      if (!userInfo) {
        this.$message.error('请先登录后再评论');
        this.$router.push('/auth');
        return;
      }
      
      this.isReply = false;
      this.replyToReview = null;
      this.newReview = {
        rating: 5,
        comment: '',
        parentID: null
      };
      this.reviewDialogVisible = true;
    },
    
    showReplyDialog(review) {
      // 检查用户是否登录
      const userInfo = JSON.parse(localStorage.getItem('userInfo'));
      if (!userInfo) {
        this.$message.error('请先登录后再评论');
        this.$router.push('/auth');
        return;
      }
      
      this.isReply = true;
      this.replyToReview = review;
      
      // 将评论ID转换为数字类型
      const reviewId = review.id ? Number(review.id) : null;
      
      this.newReview = {
        rating: 0, // 回复评论不需要评分
        comment: '',
        parentID: reviewId
      };
      this.reviewDialogVisible = true;
      
      console.log('准备回复评论:', {
        评论ID: reviewId,
        评论内容: review.comment,
        回复者: review.userID
      });
    },
    
    async submitReview() {
      // 检查评论内容是否为空
      if (!this.newReview.comment.trim()) {
        this.$message.error('评论内容不能为空');
        return;
      }
      
      // 如果是直接评论商家，需要评分
      if (!this.isReply && (!this.newReview.rating || this.newReview.rating < 1)) {
        this.$message.error('请为商家评分');
        return;
      }
      
      try {
        const userInfo = JSON.parse(localStorage.getItem('userInfo'));
        
        // 确保parentID是数字类型
        let parentID = this.newReview.parentID;
        if (parentID) {
          parentID = Number(parentID);
        }
        
        // 在评论数据中添加username字段，便于前端显示
        const reviewData = {
          userID: userInfo.id,
          merchantID: this.business.id,
          rating: this.isReply ? 0 : this.newReview.rating,
          comment: this.newReview.comment,
          parentID: parentID,
          createTime: new Date().toISOString(),
          // 添加用户名信息，这个字段会被后端忽略，但会在前端的响应中返回
          username: userInfo.username
        };
        
        // 同时更新本地用户缓存
        this.users[userInfo.id] = {
          id: userInfo.id,
          username: userInfo.username
        };
        this.saveUserNameCache();
        
        console.log('准备提交的评论数据:', reviewData);
        const response = await axios.post('/api/reviews/add', reviewData);
        console.log('评论提交响应:', response);
        
        this.$message.success('评论提交成功');
        this.reviewDialogVisible = false;
        
        // 重新获取评论列表
        this.fetchReviews(this.business.id);
      } catch (error) {
        console.error('提交评论失败:', error);
        this.$message.error('评论提交失败，请稍后重试');
      }
    },
    hasReplies(reviewId) {
      return this.reviews.some(review => review.parentID === reviewId);
    },
    getDirectReplies(reviewId) {
      return this.reviews.filter(review => review.parentID === reviewId);
    },
    isRepliesVisible(reviewId) {
      // 对每个reviewId使用唯一的标识符，以便于区分不同层级的同一ID
      return this.repliesVisible.includes(reviewId);
    },
    toggleReplies(reviewId) {
      console.log(`切换评论ID ${reviewId} 的回复显示状态`);
      if (this.isRepliesVisible(reviewId)) {
        // 收起当前评论的回复时，也需要收起其所有子回复
        this.repliesVisible = this.repliesVisible.filter(id => id !== reviewId);
        
        // 递归查找并收起所有子回复
        const childReplies = this.getDirectReplies(reviewId);
        childReplies.forEach(reply => {
          if (this.isRepliesVisible(reply.id)) {
            this.toggleReplies(reply.id); // 递归收起子回复
          }
        });
        
        console.log(`收起评论ID ${reviewId} 的回复，当前可见回复:`, this.repliesVisible);
      } else {
        this.repliesVisible = [...this.repliesVisible, reviewId];
        console.log(`展开评论ID ${reviewId} 的回复，当前可见回复:`, this.repliesVisible);
      }
    },
    countReplies(reviewId) {
      return this.getDirectReplies(reviewId).length;
    },
    getReviewById(reviewId) {
      return this.reviews.find(review => review.id === reviewId);
    },
    filterTopLevelReviews() {
      return this.reviews.filter(review => !review.parentID);
    },
    getUserInitial(userId) {
      const user = this.users[userId];
      if (user && user.username) {
        return user.username.charAt(0).toUpperCase();
      }
      return 'U';
    },
    
    // 根据用户ID生成一个固定的颜色
    getUserColor(userId) {
      // 预定义的颜色数组，柔和但有区分度的颜色
      const colors = [
        '#4A90E2', '#50C878', '#F5A623', '#E74C3C', '#9B59B6', 
        '#3498DB', '#2ECC71', '#F1C40F', '#E67E22', '#8E44AD'
      ];
      
      // 使用userId作为索引选择颜色
      const colorIndex = (userId % colors.length);
      return colors[colorIndex];
    },
    loadUserNameCache() {
      try {
        // 从localStorage加载用户名缓存
        const cachedUsers = localStorage.getItem('userNameCache');
        if (cachedUsers) {
          const parsedCache = JSON.parse(cachedUsers);
          // 合并缓存到当前用户映射
          this.users = { ...this.users, ...parsedCache };
          console.log('已加载用户名缓存:', this.users);
        }
      } catch (error) {
        console.error('加载用户名缓存失败:', error);
      }
    },
    
    saveUserNameCache() {
      try {
        // 将当前用户映射保存到localStorage
        localStorage.setItem('userNameCache', JSON.stringify(this.users));
        console.log('用户名缓存已保存:', this.users);
      } catch (error) {
        console.error('保存用户名缓存失败:', error);
      }
    }
  }
}
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e9ecef 100%);
  padding-bottom: 40px;
}

.business-detail {
  max-width: 1200px;
  margin: 0 auto;
}

/* 封面区域样式优化 */
.cover-image-container {
  position: relative;
  height: 450px;
  overflow: hidden;
  border-radius: 0 0 35px 35px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}

.cover-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60%;
  background: linear-gradient(to bottom, transparent, rgba(0,0,0,0.8));
  z-index: 1;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s ease;
  animation: zoomIn 0.8s ease;
}

@keyframes zoomIn {
  from {
    transform: scale(1.1);
  }
  to {
    transform: scale(1);
  }
}

.cover-image-container:hover .cover-image {
  transform: scale(1.05);
}

.back-btn {
  position: absolute;
  top: 20px;
  left: 20px;
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  z-index: 10;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  transition: all 0.3s ease;
  animation: fadeIn 0.6s ease;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.back-btn i {
  font-size: 1.2rem;
}

.business-title {
  position: absolute;
  bottom: 30px;
  left: 40px;
  color: white;
  font-size: 2.8rem;
  font-weight: 700;
  text-shadow: 2px 2px 10px rgba(0,0,0,0.5);
  z-index: 2;
  opacity: 0;
  transform: translateY(20px);
  animation: fadeInUp 0.8s ease forwards 0.3s;
}

@keyframes fadeInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.content-wrapper {
  padding: 0 30px;
  margin-top: -70px;
  position: relative;
  z-index: 3;
}

/* 信息区域样式优化 */
.info-section {
  background: white;
  border-radius: 18px;
  padding: 30px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
  font-size: 1.1rem;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  animation: fadeIn 0.8s ease;
}

.info-section:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 40px rgba(0,0,0,0.15);
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.rating-box {
  text-align: center;
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.stars {
  color: #ffce00;
  font-size: 28px;
  margin-right: 12px;
  letter-spacing: 3px;
}

.rating-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  background: linear-gradient(45deg, #f5a623, #ff9500);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 25px;
  margin-top: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  border-left: 4px solid #4a90e2;
  transition: all 0.3s ease;
}

.info-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.08);
  background: #f0f5ff;
}

.info-item i {
  font-size: 1.6rem;
  margin-right: 18px;
  color: #4a90e2;
  transition: transform 0.3s ease;
}

.info-item:hover i {
  transform: scale(1.2);
}

.info-text {
  display: flex;
  flex-direction: column;
}

.info-text label {
  font-size: 14px;
  color: #666;
  margin-bottom: 6px;
  font-weight: 500;
}

.info-text span {
  font-size: 17px;
  color: #333;
  font-weight: 500;
}

/* 各区域共用样式 */
.description-section, .gallery-section, .packages-section, .reviews-section {
  background: white;
  border-radius: 18px;
  padding: 30px;
  margin-top: 25px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
  transition: transform 0.3s ease;
}

.description-section:hover, .gallery-section:hover, 
.packages-section:hover, .reviews-section:hover {
  transform: translateY(-5px);
}

.description-section h3, .gallery-section h3, .packages-section h3, .reviews-section h3 {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #333;
  margin-bottom: 20px;
  font-size: 1.8rem;
  position: relative;
  padding-bottom: 15px;
}

.description-section h3::after, .gallery-section h3::after, 
.packages-section h3::after, .reviews-section h3::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 80px;
  height: 3px;
  background: #4a90e2;
  border-radius: 2px;
}

.description-section p {
  line-height: 1.9;
  font-size: 1.1rem;
  color: #444;
  letter-spacing: 0.2px;
}

/* 套餐区域样式优化 */
.gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 18px;
  margin-top: 25px;
}

.gallery-item {
  aspect-ratio: 1;
  overflow: hidden;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 5px 15px rgba(0,0,0,0.08);
  position: relative;
}

.gallery-item::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.2);
  opacity: 0;
  transition: opacity 0.3s;
}

.gallery-item:hover::after {
  opacity: 1;
}

.gallery-item:hover {
  transform: scale(1.05);
  box-shadow: 0 12px 25px rgba(0,0,0,0.15);
}

.gallery-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s;
}

.gallery-item:hover img {
  transform: scale(1.1);
}

/* 套餐样式优化 */
.packages-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 25px;
  margin-top: 25px;
}

.package-item {
  display: flex;
  background: #f8f9fa;
  border-radius: 15px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.4s ease;
  box-shadow: 0 8px 20px rgba(0,0,0,0.08);
  border: 1px solid #eee;
  height: 150px;
}

.package-item:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 30px rgba(0,0,0,0.15);
  border-color: #4a90e2;
}

.package-image {
  width: 150px;
  height: 150px;
  overflow: hidden;
}

.package-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.package-item:hover .package-image img {
  transform: scale(1.1);
}

.package-info {
  flex: 1;
  padding: 20px;
  position: relative;
  display: flex;
  flex-direction: column;
}

.package-info h4 {
  margin: 0 0 12px 0;
  font-size: 1.25rem;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.package-price {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.current-price {
  color: #e53935;
  font-size: 1.4rem;
  font-weight: bold;
}

.original-price {
  color: #999;
  font-size: 1rem;
  text-decoration: line-through;
}

.package-sales {
  color: #666;
  font-size: 0.9rem;
  margin-top: auto;
}

.no-packages {
  padding: 40px 20px;
  text-align: center;
  color: #999;
  font-size: 1.2rem;
  background: #f9f9f9;
  border-radius: 10px;
  border: 1px dashed #ddd;
}

/* 评论区样式优化 */
.reviews-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.no-reviews {
  padding: 40px 20px;
  text-align: center;
  color: #999;
  font-size: 1.2rem;
  background: #f9f9f9;
  border-radius: 10px;
  border: 1px dashed #ddd;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.review-item {
  padding: 25px;
  background: #f8f9fa;
  border-radius: 12px;
  transition: all 0.3s ease;
  box-shadow: 0 5px 15px rgba(0,0,0,0.05);
  border-left: 4px solid transparent;
}

.review-item:hover {
  border-left-color: #4a90e2;
  transform: translateX(5px);
  background: #f0f5ff;
}

/* 回复评论的样式优化 */
.reply-review {
  margin-left: 45px;
  background: rgba(248, 249, 250, 0.7);
  border-left: 3px solid #4a90e2;
}

.review-content-wrapper {
  display: flex;
}

.review-avatar {
  flex-shrink: 0;
  width: 45px;
  height: 45px;
  margin-right: 18px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar-circle {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 1.3rem;
  font-weight: bold;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
  transition: transform 0.3s ease;
}

.review-item:hover .avatar-circle {
  transform: scale(1.1);
}

.review-content {
  flex: 1;
}

.review-header {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 12px;
}

.review-username {
  font-weight: bold;
  color: #333;
  font-size: 1.1rem;
}

.review-rating {
  color: #ffce00;
  letter-spacing: 2px;
}

.review-date {
  color: #999;
  font-size: 0.9rem;
}

.review-text {
  line-height: 1.7;
  margin-bottom: 15px;
  color: #444;
}

.review-actions {
  display: flex;
  gap: 20px;
  margin-top: 15px;
}

.reply-button {
  cursor: pointer;
  color: #4a90e2;
  font-size: 0.95rem;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.reply-button:hover {
  color: #2a70c2;
  transform: translateX(3px);
}

.reply-to-info {
  margin-top: 10px;
  color: #4a90e2;
  font-size: 0.95rem;
  background: rgba(74, 144, 226, 0.1);
  padding: 6px 12px;
  border-radius: 6px;
  display: inline-block;
}

.reply-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  color: #666;
  font-size: 0.95rem;
}

.reply-icon {
  color: #4a90e2;
  font-size: 0.9rem;
}

.view-replies {
  cursor: pointer;
  color: #4a90e2;
  font-size: 0.95rem;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s ease;
}

.view-replies:hover {
  color: #2a70c2;
  transform: translateY(-2px);
}

/* 响应式适配 */
@media (max-width: 992px) {
  .package-item {
    height: auto;
  }
  
  .content-wrapper {
    padding: 0 20px;
  }
  
  .cover-image-container {
    height: 380px;
  }
  
  .business-title {
    font-size: 2.4rem;
    left: 30px;
    bottom: 25px;
  }
}

@media (max-width: 768px) {
  .cover-image-container {
    height: 300px;
    border-radius: 0 0 25px 25px;
  }
  
  .business-title {
    font-size: 2rem;
    left: 20px;
    bottom: 20px;
  }
  
  .content-wrapper {
    padding: 0 15px;
    margin-top: -50px;
  }
  
  .info-section, .description-section, .gallery-section, .packages-section, .reviews-section {
    padding: 20px;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .gallery {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 10px;
  }
  
  .packages-list {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .description-section h3, .gallery-section h3, .packages-section h3, .reviews-section h3 {
    font-size: 1.4rem;
  }
  
  .description-section p {
    font-size: 1rem;
  }
  
  .review-avatar {
    width: 40px;
    height: 40px;
    margin-right: 15px;
  }
  
  .avatar-circle {
    width: 40px;
    height: 40px;
    font-size: 1.1rem;
  }
  
  .package-image {
    width: 120px;
  }
}

@media (max-width: 480px) {
  .cover-image-container {
    height: 250px;
  }
  
  .business-title {
    font-size: 1.7rem;
    left: 15px;
    bottom: 15px;
  }
  
  .content-wrapper {
    margin-top: -40px;
    padding: 0 10px;
  }
  
  .info-section, .description-section, .gallery-section, .packages-section, .reviews-section {
    padding: 15px;
    border-radius: 15px;
  }
  
  .rating-box {
    margin-bottom: 15px;
    padding-bottom: 15px;
  }
  
  .stars {
    font-size: 22px;
  }
  
  .rating-value {
    font-size: 20px;
  }
  
  .info-item {
    padding: 15px;
  }
  
  .info-text label {
    font-size: 12px;
  }
  
  .info-text span {
    font-size: 15px;
  }
  
  .gallery {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 8px;
  }
  
  .package-image {
    width: 100px;
  }
  
  .package-info {
    padding: 15px;
  }
  
  .package-info h4 {
    font-size: 1.1rem;
    margin-bottom: 8px;
  }
  
  .current-price {
    font-size: 1.2rem;
  }
  
  .review-item {
    padding: 15px;
  }
  
  .review-text {
    font-size: 0.95rem;
  }
  
  .review-header {
    gap: 10px;
  }
  
  .review-username {
    font-size: 1rem;
  }
  
  .review-avatar {
    width: 35px;
    height: 35px;
    margin-right: 12px;
  }
  
  .avatar-circle {
    width: 35px;
    height: 35px;
    font-size: 1rem;
  }
}

/* 图片预览样式 */
:global(.image-preview-container) {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

:global(.image-preview-container.active) {
  opacity: 1;
}

:global(.image-preview-container.closing) {
  opacity: 0;
}

:global(.preview-image) {
  max-width: 90%;
  max-height: 90%;
  object-fit: contain;
  border-radius: 4px;
  box-shadow: 0 5px 30px rgba(0, 0, 0, 0.3);
  transform: scale(0.9);
  transition: transform 0.3s ease;
}

:global(.image-preview-container.active .preview-image) {
  transform: scale(1);
}

:global(.preview-close-btn) {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  font-size: 1.2rem;
  transition: all 0.3s ease;
}

:global(.preview-close-btn:hover) {
  background: rgba(255, 255, 255, 0.2);
  transform: rotate(90deg);
}

/* 滚动动画效果 */
.info-section, .description-section, .gallery-section, .packages-section, .reviews-section {
  opacity: 0;
  transform: translateY(30px);
  animation: fadeInUp 0.8s ease forwards;
}

.info-section {
  animation-delay: 0.2s;
}

.packages-section {
  animation-delay: 0.4s;
}

.description-section {
  animation-delay: 0.6s;
}

.gallery-section {
  animation-delay: 0.8s;
}

.reviews-section {
  animation-delay: 1s;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>