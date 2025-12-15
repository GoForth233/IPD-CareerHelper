<template>
  <view class="container">
    <view class="header">
      <text class="title">Career Platform</text>
      <text class="subtitle">开启你的职业发展之旅</text>
    </view>

    <view class="card">
      <view class="tabs">
        <view :class="['tab', isLogin ? 'active' : '']" @click="isLogin = true">
          <text>登录</text>
        </view>
        <view :class="['tab', !isLogin ? 'active' : '']" @click="isLogin = false">
          <text>注册</text>
        </view>
      </view>

      <view class="form">
        <view class="form-item" v-if="!isLogin">
          <text class="label">昵称</text>
          <input class="input" v-model="form.nickname" placeholder="请输入昵称" />
        </view>
        <view class="form-item">
          <text class="label">{{ isLogin ? '用户名/手机号' : '手机号' }}</text>
          <input class="input" v-model="form.identifier" placeholder="请输入手机号" />
        </view>
        <view class="form-item">
          <text class="label">密码</text>
          <input class="input" type="password" v-model="form.credential" placeholder="请输入密码" />
        </view>

        <button class="btn-primary" @click="handleSubmit" :loading="loading">
          {{ isLogin ? '登录' : '注册' }}
        </button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { registerApi, loginApi } from '@/api/user';

const isLogin = ref(true);
const loading = ref(false);

const form = ref({
  nickname: '',
  identifier: '',
  credential: ''
});

const handleSubmit = async () => {
  if (!form.value.identifier || !form.value.credential) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' });
    return;
  }

  if (!isLogin.value && !form.value.nickname) {
    uni.showToast({ title: '请输入昵称', icon: 'none' });
    return;
  }

  loading.value = true;
  try {
    if (isLogin.value) {
      // 登录
      const user = await loginApi({
        identityType: 'PASSWORD',
        identifier: form.value.identifier,
        credential: form.value.credential
      });
      
      uni.setStorageSync('userId', user.userId);
      uni.setStorageSync('userInfo', user);
      uni.showToast({ title: '登录成功', icon: 'success' });
      
      setTimeout(() => {
        uni.switchTab({ url: '/pages/user/profile' });
      }, 1000);
    } else {
      // 注册
      const user = await registerApi({
        nickname: form.value.nickname,
        identityType: 'PASSWORD',
        identifier: form.value.identifier,
        credential: form.value.credential
      });
      
      uni.setStorageSync('userId', user.userId);
      uni.setStorageSync('userInfo', user);
      uni.showToast({ title: '注册成功', icon: 'success' });
      
      setTimeout(() => {
        uni.switchTab({ url: '/pages/user/profile' });
      }, 1000);
    }
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60px 20px;
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.title {
  font-size: 32px;
  font-weight: bold;
  color: #fff;
  display: block;
  margin-bottom: 10px;
}

.subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
  display: block;
}

.card {
  background-color: #fff;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.tabs {
  display: flex;
  border-bottom: 2px solid #f0f0f0;
  margin-bottom: 30px;
}

.tab {
  flex: 1;
  text-align: center;
  padding: 12px;
  font-size: 16px;
  color: #999;
  cursor: pointer;
}

.tab.active {
  color: #667eea;
  font-weight: bold;
  border-bottom: 3px solid #667eea;
  margin-bottom: -2px;
}

.form-item {
  margin-bottom: 20px;
}

.label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
  display: block;
}

.input {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 12px;
  font-size: 14px;
  background-color: #f9f9f9;
  width: 100%;
  box-sizing: border-box;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 8px;
  margin-top: 20px;
  font-size: 16px;
  font-weight: bold;
}
</style>

