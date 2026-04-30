<template>
  <el-container class="admin-shell">
    <el-aside width="220px" class="admin-aside">
      <div class="brand">CareerLoop · Admin</div>
      <el-menu :default-active="$route.name as string" router>
        <el-menu-item index="dashboard" :route="{ name: 'dashboard' }">Dashboard</el-menu-item>
        <el-menu-item index="students" :route="{ name: 'students' }">Students</el-menu-item>
        <el-menu-item index="skill-map" :route="{ name: 'skill-map' }">Skill Map Editor</el-menu-item>
        <el-menu-item index="questions" :route="{ name: 'questions' }">Question Bank</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <span class="title">{{ headerTitle }}</span>
        <el-button text @click="logout">Log out</el-button>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

const headerTitle = computed(() => {
  const map: Record<string, string> = {
    dashboard: 'Organization Dashboard',
    students: 'Students',
    'skill-map': 'Skill Map Editor',
    questions: 'Question Bank Moderation',
  };
  return map[route.name as string] || 'Admin';
});

const logout = () => {
  localStorage.removeItem('admin_token');
  router.push({ name: 'login' });
};
</script>

<style scoped>
.admin-shell { height: 100vh; }
.admin-aside { background: #0f172a; color: #f8fafc; padding-top: 12px; }
.admin-aside :deep(.el-menu) { background: transparent; border-right: none; }
.admin-aside :deep(.el-menu-item) { color: #cbd5f5; }
.admin-aside :deep(.el-menu-item.is-active) { background: rgba(37, 99, 235, 0.18); color: #fff; }
.brand { padding: 14px 20px 18px; font-size: 16px; font-weight: 700; color: #fff; }
.admin-header { background: #fff; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #e2e8f0; }
.admin-header .title { font-size: 18px; font-weight: 700; }
.admin-main { background: #f5f5f7; padding: 20px 24px; }
</style>
