<template>
  <view class="container">
    <view class="header-card" v-if="userInfo">
      <view class="avatar-section">
        <image :src="userInfo.avatarUrl || '/static/logo.png'" class="avatar" mode="aspectFill"></image>
        <view class="user-info">
          <text class="nickname">{{ userInfo.nickname }}</text>
          <view class="badge-row">
            <text class="vip-badge" v-if="userInfo.isVip">VIP</text>
            <text class="points">积分: {{ userInfo.points || 0 }}</text>
          </view>
        </view>
      </view>
      <view class="school-info" v-if="userInfo.school">
        <text>🎓 {{ userInfo.school }} - {{ userInfo.major }}</text>
      </view>
    </view>

    <view class="card" v-if="!userInfo">
      <text class="empty-text">请先登录</text>
      <button class="btn-primary" @click="goLogin">去登录</button>
    </view>

    <view class="menu-list" v-if="userInfo">
      <view class="menu-item" @click="goToResumes">
        <text class="menu-icon">📄</text>
        <text class="menu-text">我的简历</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goToInterviews">
        <text class="menu-icon">💼</text>
        <text class="menu-text">面试历史</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goToProgress">
        <text class="menu-icon">📈</text>
        <text class="menu-text">学习进度</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="handleLogout">
        <text class="menu-icon">🚪</text>
        <text class="menu-text">退出登录</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getUserInfoApi, type User } from '@/api/user';

const userInfo = ref<User | null>(null);

onMounted(async () => {
  const userId = uni.getStorageSync('userId');
  if (userId) {
    try {
      userInfo.value = await getUserInfoApi(userId);
    } catch (error) {
      console.error('Failed to load user info:', error);
    }
  }
});

const goLogin = () => {
  uni.navigateTo({ url: '/pages/login/login' });
};

const goToResumes = () => {
  uni.switchTab({ url: '/pages/index/index' });
};

const goToInterviews = () => {
  uni.switchTab({ url: '/pages/interview/history' });
};

const goToProgress = () => {
  uni.navigateTo({ url: '/pages/career/progress' });
};

const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        uni.removeStorageSync('userId');
        uni.removeStorageSync('userInfo');
        userInfo.value = null;
        uni.showToast({ title: '已退出登录', icon: 'success' });
      }
    }
  });
};
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
}

.header-card {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 16px;
  padding: 30px 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.avatar-section {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.avatar {
  width: 70px;
  height: 70px;
  border-radius: 35px;
  border: 3px solid #fff;
  margin-right: 15px;
}

.user-info {
  flex: 1;
}

.nickname {
  font-size: 22px;
  font-weight: bold;
  color: #fff;
  display: block;
  margin-bottom: 8px;
}

.badge-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.vip-badge {
  background-color: #FFD700;
  color: #333;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.points {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.school-info {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  margin-top: 10px;
}

.card {
  background-color: #fff;
  border-radius: 12px;
  padding: 40px 20px;
  text-align: center;
}

.empty-text {
  font-size: 16px;
  color: #999;
  display: block;
  margin-bottom: 20px;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 8px;
}

.menu-list {
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 18px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  font-size: 24px;
  margin-right: 15px;
}

.menu-text {
  flex: 1;
  font-size: 16px;
  color: #333;
}

.menu-arrow {
  font-size: 24px;
  color: #ccc;
}
</style>

