<template>
  <view class="container">
    <view class="header-card" v-if="userInfo">
      <view class="avatar-section">
        <image :src="userInfo.avatarUrl || '/static/logo.png'" class="avatar" mode="aspectFill"></image>
        <view class="user-info">
          <text class="nickname">{{ userInfo.nickname }}</text>
          <view class="badge-row">
            <text class="vip-badge" v-if="userInfo.isVip">VIP</text>
            <text class="points">Points: {{ userInfo.points || 0 }}</text>
          </view>
        </view>
      </view>
      <view class="school-info" v-if="userInfo.school">
        <text>🎓 {{ userInfo.school }} - {{ userInfo.major }}</text>
      </view>
    </view>

    <view class="card" v-if="!userInfo">
      <text class="empty-text">Please sign in first</text>
      <button class="btn-primary" @click="goLogin">Sign In</button>
    </view>

    <view class="menu-list" v-if="userInfo">
      <view class="menu-item" @click="goToResumes">
        <text class="menu-icon">📄</text>
        <text class="menu-text">My Resumes</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goToInterviews">
        <text class="menu-icon">💼</text>
        <text class="menu-text">Interview History</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goToProgress">
        <text class="menu-icon">📈</text>
        <text class="menu-text">Learning Progress</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="handleLogout">
        <text class="menu-icon">🚪</text>
        <text class="menu-text">Sign Out</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getUserInfoApi, type User } from '@/api/user';
import { clearAuthState, LOGIN_PAGE } from '@/utils/auth';

const userInfo = ref<User | null>(null);

onMounted(async () => {
  const userId = uni.getStorageSync('userId');
  const numericId = Number(userId);
  if (userId && !isNaN(numericId) && numericId > 0) {
    try {
      userInfo.value = await getUserInfoApi(numericId);
    } catch (error) {
      console.error('Failed to load user info:', error);
    }
  }
});

const goLogin = () => {
  uni.reLaunch({ url: LOGIN_PAGE });
};

const goToResumes = () => {
  uni.switchTab({ url: '/pages/resume/index' });
};

const goToInterviews = () => {
  uni.navigateTo({ url: '/pages/interview/history' });
};

const goToProgress = () => {
  uni.navigateTo({ url: '/pages/career/progress' });
};

const handleLogout = () => {
  uni.showModal({
    title: 'Sign Out',
    content: 'Are you sure you want to sign out?',
    success: (res) => {
      if (res.confirm) {
        clearAuthState();
        userInfo.value = null;
        uni.showToast({ title: 'Signed out successfully', icon: 'success' });
        setTimeout(() => {
          uni.reLaunch({ url: LOGIN_PAGE });
        }, 400);
      }
    }
  });
};
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: var(--page-ios-gray);
  padding: 20px;
}

.header-card {
  background: linear-gradient(135deg, #2563eb, #1e40af);
  border-radius: var(--radius-md);
  padding: 30px 20px;
  margin-bottom: 20px;
  box-shadow: var(--shadow-lg);
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

.user-info { flex: 1; }

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
  border-radius: var(--radius-md);
  padding: 40px 20px;
  text-align: center;
  box-shadow: var(--shadow-sm);
}

.empty-text {
  font-size: 16px;
  color: #999;
  display: block;
  margin-bottom: 20px;
}

.btn-primary {
  background: linear-gradient(135deg, #2563eb, #1e40af);
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

.menu-item:last-child { border-bottom: none; }

.menu-icon { font-size: 24px; margin-right: 15px; }

.menu-text { flex: 1; font-size: 16px; color: #333; }

.menu-arrow { font-size: 24px; color: #ccc; }
</style>
