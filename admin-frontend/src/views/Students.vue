<template>
  <div>
    <el-card>
      <div class="row-controls">
        <el-select v-model="orgId" placeholder="Organization" @change="load" style="width: 280px;">
          <el-option v-for="o in orgs" :key="o.orgId" :label="o.name" :value="o.orgId" />
        </el-select>
        <el-button @click="load">Reload</el-button>
      </div>
    </el-card>
    <el-card style="margin-top: 16px;">
      <el-table :data="students" stripe>
        <el-table-column prop="userId" label="ID" width="80" />
        <el-table-column prop="nickname" label="Nickname" />
        <el-table-column prop="school" label="School" />
        <el-table-column prop="major" label="Major" />
        <el-table-column prop="interviewCount" label="Interviews" width="120" />
        <el-table-column prop="lastInterviewScore" label="Last score" width="120" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { api, type Organization, type StudentRow } from '@/api';

const orgs = ref<Organization[]>([]);
const orgId = ref<number | null>(null);
const students = ref<StudentRow[]>([]);

const load = async () => {
  if (!orgId.value) return;
  try {
    students.value = await api.orgStudents(orgId.value);
  } catch (e: any) {
    ElMessage.error(e?.message || 'Failed to load students');
  }
};

onMounted(async () => {
  try {
    orgs.value = await api.listOrgs();
    if (orgs.value.length) {
      orgId.value = orgs.value[0].orgId!;
      load();
    }
  } catch (e: any) {
    ElMessage.error(e?.message || 'Failed to load orgs');
  }
});
</script>

<style scoped>
.row-controls { display: flex; gap: 12px; align-items: center; }
</style>
