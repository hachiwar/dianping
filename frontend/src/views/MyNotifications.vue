<template>
  <div class="page">
    <button class="back" @click="$router.back()">返回</button>
    <section class="panel">
      <h1>订单通知</h1>
      <p v-if="loading">加载中……</p>
      <p v-else-if="notifications.length === 0" class="empty">暂无通知</p>
      <article v-for="item in notifications" :key="item.id" class="notification">
        <strong>{{ item.content }}</strong>
        <time>{{ formatTime(item.createdAt) }}</time>
      </article>
    </section>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  data: () => ({ loading: true, notifications: [] }),
  async mounted() {
    try {
      const response = await axios.get('/api/orders/notifications')
      this.notifications = response.data
    } finally {
      this.loading = false
    }
  },
  methods: {
    formatTime(value) { return value ? new Date(value).toLocaleString() : '' }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; padding: 30px; background: #f4f7fb; }
.panel { max-width: 760px; margin: 30px auto; padding: 28px; background: white; border-radius: 16px; box-shadow: 0 8px 24px rgba(0,0,0,.08); }
.back { border: 0; background: transparent; color: #4a90e2; cursor: pointer; }
.notification { display: flex; justify-content: space-between; gap: 20px; padding: 18px 0; border-bottom: 1px solid #edf0f5; }
.notification time, .empty { color: #7f8c8d; }
@media (max-width: 600px) { .notification { flex-direction: column; gap: 6px; } }
</style>
