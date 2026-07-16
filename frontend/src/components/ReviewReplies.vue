<template>
  <div class="replies-container">
    <div v-for="reply in directReplies" :key="reply.id" class="reply-item">
      <div class="review-avatar">
        <div class="avatar-circle" :style="{ backgroundColor: getUserColor(reply.userID) }">
          {{ getUserInitial(reply.userID) }}
        </div>
      </div>
      <div class="review-content">
        <div class="review-header">
          <span class="review-username">{{ getUserName(reply.userID) }}</span>
          <span class="review-date">{{ formatDate(reply.createTime) }}</span>
        </div>
        <div class="review-text">{{ reply.comment }}</div>
        <div class="review-actions">
          <span class="reply-button" @click="$emit('reply', reply)">
            <i class="fas fa-reply"></i> 回复
          </span>
          
          <!-- 递归显示下一级回复 -->
          <span v-if="hasReplies(reply.id)" class="view-replies" @click="$emit('toggle-replies', reply.id)">
            <i :class="['fas', isRepliesVisible(reply.id) ? 'fa-chevron-up' : 'fa-chevron-down']"></i>
            {{ isRepliesVisible(reply.id) ? '收起回复' : `查看回复(${countReplies(reply.id)})` }}
          </span>
        </div>
        
        <!-- 递归组件自身以显示更深层次的回复 -->
        <review-replies 
          v-if="hasReplies(reply.id) && isRepliesVisible(reply.id)"
          :review-id="reply.id"
          :all-reviews="allReviews"
          :users="users"
          :replies-visible="repliesVisible"
          @reply="$emit('reply', $event)"
          @toggle-replies="$emit('toggle-replies', $event)"
        ></review-replies>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ReviewReplies',
  props: {
    reviewId: {
      type: [Number, String],
      required: true
    },
    allReviews: {
      type: Array,
      required: true
    },
    users: {
      type: Object,
      required: true
    },
    repliesVisible: {
      type: Array,
      required: true
    }
  },
  computed: {
    directReplies() {
      return this.allReviews.filter(review => {
        return review.parentID === this.reviewId || 
               Number(review.parentID) === Number(this.reviewId);
      });
    }
  },
  methods: {
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
    hasReplies(reviewId) {
      return this.allReviews.some(review => {
        return review.parentID === reviewId || 
               Number(review.parentID) === Number(reviewId);
      });
    },
    countReplies(reviewId) {
      return this.allReviews.filter(review => {
        return review.parentID === reviewId || 
               Number(review.parentID) === Number(reviewId);
      }).length;
    },
    isRepliesVisible(reviewId) {
      return this.repliesVisible.includes(reviewId);
    },
    getUserInitial(userId) {
      if (this.users[userId]?.username) {
        return this.users[userId].username.charAt(0).toUpperCase();
      }
      return 'U';
    },
    getUserColor(userId) {
      // 预定义的颜色数组，柔和但有区分度的颜色
      const colors = [
        '#4A90E2', '#50C878', '#F5A623', '#E74C3C', '#9B59B6', 
        '#3498DB', '#2ECC71', '#F1C40F', '#E67E22', '#8E44AD'
      ];
      
      // 使用userId作为索引选择颜色
      const colorIndex = (userId % colors.length);
      return colors[colorIndex];
    }
  }
}
</script>

<style scoped>
.replies-container {
  margin-top: 18px;
  margin-left: 25px;
  padding-left: 18px;
  border-left: 2px solid #e0e0e0;
  animation: slideDown 0.4s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.reply-item {
  display: flex;
  margin-bottom: 18px;
  padding: 16px;
  background: #f9fafc;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.07);
  transition: all 0.3s ease;
  border-left: 3px solid transparent;
}

.reply-item:hover {
  transform: translateX(5px);
  background: #f0f5ff;
  border-left-color: #4a90e2;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.reply-item:last-child {
  margin-bottom: 0;
}

.review-avatar {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  margin-right: 15px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar-circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  color: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 1.2rem;
  font-weight: bold;
  box-shadow: 0 3px 10px rgba(0,0,0,0.15);
  transition: transform 0.3s ease;
}

.reply-item:hover .avatar-circle {
  transform: scale(1.1);
}

.review-content {
  flex: 1;
}

.review-header {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 10px;
}

.review-username {
  font-weight: bold;
  color: #333;
  position: relative;
  padding-bottom: 2px;
}

.review-username::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: 0;
  width: 0;
  height: 1px;
  background: #4a90e2;
  transition: width 0.3s ease;
}

.reply-item:hover .review-username::after {
  width: 100%;
}

.review-date {
  color: #999;
  font-size: 0.85rem;
}

.review-text {
  line-height: 1.6;
  margin-bottom: 12px;
  color: #444;
  letter-spacing: 0.2px;
}

.review-actions {
  display: flex;
  gap: 20px;
  margin-top: 10px;
}

.reply-button {
  cursor: pointer;
  color: #4a90e2;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s ease;
}

.reply-button:hover {
  color: #2a70c2;
  transform: translateX(3px);
}

.view-replies {
  cursor: pointer;
  color: #4a90e2;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s ease;
}

.view-replies:hover {
  color: #2a70c2;
  transform: translateY(-2px);
}

/* 响应式适配 */
@media (max-width: 768px) {
  .replies-container {
    margin-left: 18px;
    padding-left: 15px;
  }
  
  .reply-item {
    padding: 14px;
  }
  
  .avatar-circle {
    width: 35px;
    height: 35px;
    font-size: 1rem;
  }
  
  .review-avatar {
    width: 35px;
    height: 35px;
    margin-right: 12px;
  }
}

@media (max-width: 480px) {
  .replies-container {
    margin-left: 12px;
    padding-left: 12px;
  }
  
  .reply-item {
    padding: 12px;
  }
  
  .review-text {
    font-size: 0.95rem;
  }
  
  .avatar-circle {
    width: 32px;
    height: 32px;
    font-size: 0.9rem;
  }
  
  .review-avatar {
    width: 32px;
    height: 32px;
    margin-right: 10px;
  }
  
  .review-actions {
    gap: 15px;
  }
}
</style> 