<template>
  <div>
    <el-card>
      <div class="row-controls">
        <el-select v-model="orgId" placeholder="Select organization" @change="loadDashboard" style="width: 280px;">
          <el-option v-for="o in orgs" :key="o.orgId" :label="o.name + ' (' + o.code + ')'" :value="o.orgId" />
        </el-select>
        <el-button @click="loadOrgs">Refresh orgs</el-button>
        <el-button type="primary" @click="runWeekly">Run weekly report</el-button>
      </div>
    </el-card>

    <div v-if="!orgId" class="empty-state">Select an organization to load its dashboard.</div>

    <div v-else>
      <el-row :gutter="16" style="margin-top: 16px;">
        <el-col :span="6"><el-card><div class="kpi-label">Students</div><div class="kpi">{{ dashboard?.studentCount || 0 }}</div></el-card></el-col>
        <el-col :span="6"><el-card><div class="kpi-label">Interviews</div><div class="kpi">{{ dashboard?.interviewCount || 0 }}</div></el-card></el-col>
        <el-col :span="6"><el-card><div class="kpi-label">Reports</div><div class="kpi">{{ dashboard?.reportCount || 0 }}</div></el-card></el-col>
        <el-col :span="6"><el-card><div class="kpi-label">Weak top 3</div><div class="kpi-text">{{ (dashboard?.weakDimensionsTop3 || []).join(', ') || '—' }}</div></el-card></el-col>
      </el-row>

      <el-card style="margin-top: 16px;" header="Radar averages">
        <el-table :data="radarRows" stripe>
          <el-table-column prop="dim" label="Dimension" width="240" />
          <el-table-column prop="avg" label="Average" />
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { api, type OrgDashboard, type Organization } from '@/api';

const orgs = ref<Organization[]>([]);
const orgId = ref<number | null>(null);
const dashboard = ref<OrgDashboard | null>(null);

const radarRows = computed(() => {
  if (!dashboard.value) return [];
  return Object.entries(dashboard.value.radarAverages).map(([dim, avg]) => ({ dim, avg }));
});

const loadOrgs = async () => {
  try {
    orgs.value = await api.listOrgs();
    if (!orgId.value && orgs.value.length) orgId.value = orgs.value[0].orgId!;
    if (orgId.value) loadDashboard();
  } catch (e: any) {
    ElMessage.error(e?.message || 'Failed to load orgs');
  }
};

const loadDashboard = async () => {
  if (!orgId.value) return;
  try {
    dashboard.value = await api.orgDashboard(orgId.value);
  } catch (e: any) {
    ElMessage.error(e?.message || 'Failed to load dashboard');
  }
};

const runWeekly = async () => {
  try {
    const r = await api.runWeeklyReport();
    ElMessage.success(`Delivered ${r.delivered}, skipped ${r.skipped}`);
  } catch (e: any) {
    ElMessage.error(e?.message || 'Failed to run weekly report');
  }
};

onMounted(loadOrgs);
</script>

<style scoped>
.row-controls { display: flex; gap: 12px; align-items: center; }
.kpi { font-size: 26px; font-weight: 800; color: #2563eb; margin-top: 6px; }
.kpi-text { font-size: 14px; font-weight: 600; color: #0f172a; margin-top: 6px; }
.kpi-label { color: #64748b; font-size: 12px; text-transform: uppercase; letter-spacing: 0.06em; font-weight: 700; }
.empty-state { padding: 36px; text-align: center; color: #94a3b8; }
</style>
