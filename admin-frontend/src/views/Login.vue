<template>
  <div class="login-page">
    <el-card class="login-card">
      <h1 class="title">CareerLoop · Admin</h1>
      <p class="sub">Use your back-office account. Regular student logins won't pass the role check.</p>
      <el-form :model="form" label-position="top" @submit.prevent="submit">
        <el-form-item label="Email">
          <el-input v-model="form.email" placeholder="admin@careerloop.app" />
        </el-form-item>
        <el-form-item label="Password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-button type="primary" :loading="loading" @click="submit" style="width: 100%;">
          Sign in
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { api } from '@/api';

const form = reactive({ email: '', password: '' });
const loading = ref(false);
const router = useRouter();

const submit = async () => {
  if (!form.email || !form.password) {
    ElMessage.warning('Email and password are required');
    return;
  }
  loading.value = true;
  try {
    const result: any = await api.login(form.email.trim().toLowerCase(), form.password);
    const token = result?.token || result?.data?.token;
    if (!token) throw new Error('No token returned');
    localStorage.setItem('admin_token', token);
    // Confirm the role server-side; reject regular student logins here.
    const isAdmin = await api.whoami();
    if (!isAdmin) {
      localStorage.removeItem('admin_token');
      throw new Error('Account is not an admin');
    }
    ElMessage.success('Signed in');
    router.push({ name: 'dashboard' });
  } catch (e: any) {
    ElMessage.error(e?.message || 'Sign-in failed');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-page { min-height: 100vh; background: linear-gradient(135deg, #0f172a, #1e3a8a); display: flex; align-items: center; justify-content: center; }
.login-card { width: 360px; padding: 24px; border-radius: 16px; }
.title { font-size: 22px; font-weight: 800; margin: 0 0 4px; }
.sub { color: #64748b; font-size: 13px; margin: 0 0 18px; }
</style>
