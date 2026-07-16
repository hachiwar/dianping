<template>
  <div class="app-container">
    <div class="container">
      <!-- 搜索区域 -->
      <div class="search-section">
        <div class="search-box">
          <div class="search-input-container">
            <input
                v-model="searchKeyword"
                @keyup.enter="handleSearch"
                @focus="showHistory = true"
                placeholder="请输入商家名称..."
            />
            <button @click="handleSearch" class="search-btn">搜索</button>
            
            <!-- 修改搜索历史的显示条件 -->
            <div v-if="searchHistory.length > 0 && showHistory" class="history-dropdown">
              <div class="history-header">
                <span>搜索历史</span>
                <button @click="clearAllHistory" class="clear-all">清空</button>
              </div>
              <ul class="history-list">
                <li v-for="item in displayedHistory" 
                    :key="item.id" 
                    @click="quickSearch(item.keyword)">
                  <span class="keyword">{{ item.keyword }}</span>
                  <button @click.stop="deleteHistory(item.id)" class="delete-btn">×</button>
                </li>
              </ul>
            </div>
          </div>
        </div>

        <!-- 筛选和排序区域 -->
        <div class="filter-sort-section">
          <div class="filter-options">
            <div class="filter-group">
              <label>评分：</label>
              <select v-model="selectedRating">
                <option value="">全部</option>
                <option value="4.0">4.0分以上</option>
                <option value="4.5">4.5分以上</option>
              </select>
            </div>

            <div class="filter-group">
              <label>价格区间：</label>
              <select v-model="selectedPriceRange">
                <option value="">全部</option>
                <option value="10-50">￥10-50</option>
                <option value="50-100">￥50-100</option>
                <option value="100-200">￥100-200</option>
                <option value="200">￥200以上</option>
              </select>
            </div>

            <div class="filter-group">
              <label>人均消费：</label>
              <select v-model="selectedAvgPrice">
                <option value="">全部</option>
                <option value="50">￥50以下</option>
                <option value="100">￥100以下</option>
                <option value="150">￥150以下</option>
              </select>
            </div>

            <button @click="applyFilters" class="apply-btn">应用筛选</button>
            <button @click="resetFilters" class="reset-btn">重置</button>
          </div>

          <div class="sort-options">
            <label>排序方式：</label>
            <select v-model="selectedSort" @change="applySort">
              <option value="default">综合排序</option>
              <option value="rating">评分最高</option>
              <option value="price_asc">人均消费最低</option>
              <option value="price_desc">人均消费最高</option>
            </select>
          </div>
        </div>
      </div>

      <!-- 商家列表 -->
      <div class="business-list">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="businesses.length === 0" class="no-result">
          没有找到符合条件的商家
        </div>
        <template v-else>
          <div class="business-card" v-for="business in businesses" :key="business.id" @click="goToDetail(business.id)">
            <div class="business-image">
              <img :src="business.image" :alt="business.name"/>
            </div>
            <div class="business-info">
              <h3 v-html="business.highlightedName"></h3>
              <p class="business-name">{{ business.name }}</p> <!-- 添加商家名称 -->
              <div class="rating">
                <span class="stars">{{ renderStars(business.rating) }}</span>
                <span class="rating-value">{{ business.rating }}</span>
              </div>
              <div class="price">人均: ￥{{ business.avgPrice }}</div>
              <div class="address">
                <i class="fas fa-map-marker-alt"></i> {{ business.address }}
              </div>
              <div class="hours">
                <i class="fas fa-clock"></i> {{ business.businessHours }}
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- 用户信息 -->
    <div class="user-section">
      <div class="left-buttons">
        <button class="back-btn" @click="$router.push('/my')">
          <i class="fas fa-arrow-left"></i>
          <span>返回</span>
        </button>
        <button class="home-btn" @click="goToHome">
          <i class="fas fa-home"></i>
          <span>主页</span>
        </button>
      </div>
      <div class="user-info" @click="goToUserInfo">
        <span class="username">{{ userInfo.username }}</span>
        <div class="avatar">
          {{ userInfo.username?.charAt(0)?.toUpperCase() }}
        </div>
      </div>
    </div>
    <div>
      <button class="logout-btn" @click="handleLogout">
        <i class="fas fa-sign-out-alt"></i>
        <span>退出登录</span>
      </button>
      <button class="order-btn" @click="showOrders">
        <i class="fas fa-receipt"></i>
        <span>我的订单</span>
      </button>
    </div>
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
      isExpanded: false,
      displayLimit: 15, // 修改为15条
      searchKeyword: '',
      searchHistory: [],
      loading: false,
      businesses: [],

      // 筛选条件
      selectedRating: '',
      selectedPriceRange: '',
      selectedAvgPrice: '',

      // 排序条件
      selectedSort: 'default',

      showHistory: false, // 添加控制搜索历史显示的变量
    }
  },
  computed: {
    displayedHistory() {
      return this.isExpanded
          ? this.searchHistory
          : this.searchHistory.slice(0, this.displayLimit)
    },
    showExpand() {
      return this.searchHistory.length > this.displayLimit
    }
  },
  mounted() {
    const userData = localStorage.getItem('userInfo')
    if (userData) {
      this.userInfo = JSON.parse(userData)
      this.loadSearchHistory()
      this.fetchBusinesses()
    } else {
      this.$router.push('/login')
    }
    // 添加点击事件监听器
    document.addEventListener('click', this.handleClickOutside)
  },
  beforeDestroy() {
    // 组件销毁前移除事件监听器
    document.removeEventListener('click', this.handleClickOutside)
  },
  methods: {
    toggleExpand() {
      this.isExpanded = !this.isExpanded
    },
    async loadSearchHistory() {
      try {
        const response = await axios.get('/api/search', {
          headers: {
            'UserId': this.userInfo.id
          }
        })
        this.searchHistory = response.data

      } catch (error) {
        console.error('加载搜索历史失败:', error)
      }
    },
    async handleSearch() {
  if (!this.searchKeyword.trim()) return;

  this.loading = true; // 开始加载
  try {
 // 调用 fetchBusinesses 方法以更新商家列表
    await this.fetchBusinesses();

    // 保存搜索关键词到搜索历史
    await this.saveSearchKeyword(this.searchKeyword.trim());
  } catch (error) {
    console.error('搜索失败:', error);
    this.businesses = [];
  } finally {
    this.loading = false; // 加载结束
  }
},
    async saveSearchKeyword(keyword) {
      try {
        await axios.post('/api/search', null, {
          params: {keyword: this.searchKeyword},
          headers: {
            'UserId': this.userInfo.id
          }
        });
        this.loadSearchHistory(); 
      } catch (error) {
        console.error('保存搜索关键词失败:', error);
      }
    },
    highlightKeyword(name, keyword) {
      const regex = new RegExp(`(${keyword})`, 'gi');
      return name.replace(regex, '<span class="highlight">$1</span>');
    },
    quickSearch(keyword) {
      this.searchKeyword = keyword  // 保持这一行，让搜索词显示在搜索框中
      this.showHistory = false  // 选择历史记录后隐藏下拉框
      this.handleSearch()  // 执行搜索
    },
    async deleteHistory(id) {
      try {
        await axios.delete(`/api/search/${id}`, {
          headers: {
            'UserId': this.userInfo.id
          }
        })
        this.searchHistory = this.searchHistory.filter(item => item.id !== id)
      } catch (error) {
        console.error('删除失败:', error)
      }
    },
    async clearAllHistory() {
      try {
        await axios.delete('/api/search', {
          headers: {
            'UserId': this.userInfo.id
          }
        })
        this.searchHistory = []
      } catch (error) {
        console.error('清空失败:', error)
      }
    },
    handleLogout() {
      localStorage.removeItem('userInfo')
      this.$router.push('/login')
    },
    showOrders() {
      this.$router.push('/my-orders')
    },
    goToUserInfo() {
      this.$router.push('/user-info')
    },
    goToHome() {
      // 重置所有筛选条件
      this.searchKeyword = '';
      this.selectedRating = '';
      this.selectedPriceRange = '';
      this.selectedAvgPrice = '';
      this.selectedSort = 'default';
      // 重新加载商家列表
      this.fetchBusinesses();
    },

    async fetchBusinesses() {
      this.loading = true;
      try {
        const params = {
          keyword: this.searchKeyword.trim() || null,
          rating: this.selectedRating || null,
          priceRange: this.selectedPriceRange || null,
          avgPrice: this.selectedAvgPrice || null,
          sort: this.selectedSort
        };
        Object.keys(params).forEach(key => {
          if (params[key] === null || params[key] === undefined) {  // 已移除重复的 undefined 检查
            delete params[key];
          }
        });
        const response = await axios.get('/api/businesses', {params, withCredentials: true});
        this.businesses = response.data.map(item => ({
          id: item.id,
          name: item.merchantName,
          rating: item.rating,
          avgPrice: item.avgPrice,
          address: item.address,
          businessHours: item.businessHours,
          image: item.coverUrl ? `http://localhost:8080${item.coverUrl}` :
              '/placeholder.jpg',
          phone: item.telephone,
          description: item.description
        }));
      } catch (error) {
        console.error('获取商家数据失败:', error);
        this.businesses = [];
      } finally {
        this.loading = false;
      }
    },

    applyFilters() {
      this.fetchBusinesses()
    },

    resetFilters() {
      this.selectedRating = ''
      this.selectedPriceRange = ''
      this.selectedAvgPrice = ''
      this.selectedSort = 'default'
      this.fetchBusinesses()
    },

    applySort() {
      this.fetchBusinesses()
    },

    renderStars(rating) {
      const fullStars = Math.floor(rating)
      const halfStar = rating % 1 >= 0.5 ? 1 : 0
      const emptyStars = 5 - fullStars - halfStar

      return '★'.repeat(fullStars) + (halfStar ? '☆' : '') + '☆'.repeat(emptyStars)
    },

    goToDetail(id) {
      this.$router.push(`/businessDetail/${id}`)
    },

    handleClickOutside(event) {
      const container = document.querySelector('.search-input-container')
      if (container && !container.contains(event.target)) {
        this.showHistory = false
      }
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('/src/assets/home.jpg');
  background-position: center center;
  background-repeat: no-repeat;
  background-size: cover;
  background-attachment: fixed; /* 添加固定背景效果，滚动时更流畅 */
  position: relative;
}

.search-section {
  max-width: 1000px;
  margin: 20px auto;
  padding: 20px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.search-box {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.search-input-container {
  position: relative;
  flex: 1;
  width: 100%;  /* 确保容器宽度正确 */
}

.search-box input {
  flex: 1;
  padding: 12px;
  border: 2px solid #4a90e2;
  border-radius: 25px;
  font-size: 16px;
}

.search-btn {
  padding: 12px 30px;
  background: #4a90e2;
  color: white;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s;
}

.search-btn:hover {
  background: #357abd;
  transform: scale(1.05);
}

.history-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #ddd;
  border-radius: 0 0 8px 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  max-height: 300px;
  overflow-y: auto;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid #eee;
}

.history-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.history-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.history-list li:hover {
  background-color: #f5f5f5;
}

.keyword {
  flex: 1;
  margin-right: 8px;
}

.delete-btn {
  opacity: 0.6;
  padding: 2px 6px;
  background: none;
  border: none;
  cursor: pointer;
  transition: opacity 0.2s;
}

.delete-btn:hover {
  opacity: 1;
}

.filter-sort-section {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: center;
}

.filter-group {
  display: flex;
  align-items: center;
}

.filter-group label {
  margin-right: 8px;
  font-weight: 500;
}

.filter-group select {
  padding: 8px 12px;
  border-radius: 5px;
  border: 1px solid #ddd;
}

.apply-btn, .reset-btn {
  padding: 8px 15px;
  border-radius: 5px;
  cursor: pointer;
  transition: all 0.2s;
}

.apply-btn {
  background: #4a90e2;
  color: white;
  border: none;
}

.apply-btn:hover {
  background: #357abd;
}

.reset-btn {
  background: #f8f9fa;
  border: 1px solid #ddd;
}

.reset-btn:hover {
  background: #e9ecef;
}

.sort-options {
  display: flex;
  align-items: center;
}

.sort-options label {
  margin-right: 8px;
  font-weight: 500;
}

.sort-options select {
  padding: 8px 12px;
  border-radius: 5px;
  border: 1px solid #ddd;
}

.business-list {
  max-width: 1000px;
  margin: 30px auto;
  display: grid;
  grid-auto-flow: row;
  grid-template-columns: repeat(auto-fill, minmax(450px, 1fr));
  gap: 20px;
}

@media (max-width: 768px) {
  .business-list {
    grid-template-columns: 1fr; /* 小屏幕时单列 */
  }
}

.business-card {
  background: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
  cursor: pointer;
  display:flex;
}

.business-card:hover {
  transform: translateY(-5px);
}

.business-image {
  width: 150px;
  height: 150px;
  overflow: hidden;
}

.business-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.business-info {
  padding: 15px;
  flex: 1;
}

.business-info h3 {
  margin: 0 0 10px 0;
  color: #333;
}

.business-name {
  font-size: 16px; /* 字体稍微大一点 */
  font-weight: bold; /* 加粗 */
  color: #333;
  margin-bottom: 8px;
}

.rating {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.stars {
  color: #ffb800;
  margin-right: 8px;
}

.rating-value {
  color: #666;
}

.price {
  color: #e53935;
  font-weight: bold;
  margin-bottom: 8px;
}

.address, .hours {
  color: #666;
  font-size: 14px;
  margin-bottom: 5px;
  display: flex;
  align-items: center;
}

.address i, .hours i {
  margin-right: 5px;
  color: #4a90e2;
}

.loading, .no-result {
  text-align: center;
  padding: 50px;
  grid-column: 1 / -1;
  color: #666;
}

.user-section {
  position: fixed;
  top: 20px;
  right: 30px;
  display: flex;
  align-items: center;
}

.left-buttons {
  position: fixed;
  top: 20px;
  left: 30px;
  display: flex;
  gap: 10px;
  z-index: 1000;
}

.back-btn {
  padding: 12px 25px;
  background: #666;
  color: white;
  border: none;
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 5px;
}

.back-btn:hover {
  background: #555;
  transform: scale(1.05);
}

.home-btn {
  padding: 12px 25px;
  background: #4CAF50;
  color: white;
  border: none;
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 5px;
}

.avatar {
  width: 40px;
  height: 40px;
  background: #4a90e2;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
}

.username {
  font-size: 1.1rem;
  color: #333;
  font-weight: 500;
}

.logout-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  padding: 12px 25px;
  background: #e74c3c;
  color: white;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.logout-btn:hover {
  background: #c0392b;
  transform: scale(1.05);
}

.fas {
  font-size: 1rem;
}

.highlight {
  background-color: yellow;
}

.order-btn {
  position: fixed;
  bottom: 90px; /* 位于退出登录按钮上方 */
  right: 30px;
  padding: 12px 25px;
  background: #4a90e2;
  color: white;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
}
.order-btn:hover {
  background: #357abd;
  transform: scale(1.05);
}
</style>
