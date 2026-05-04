<template>
  <div>
    <!-- Toolbar -->
    <el-card style="margin-bottom: 16px;">
      <div class="toolbar">
        <el-select v-model="statusFilter" placeholder="All Status" clearable style="width: 160px" @change="load">
          <el-option label="Pending"    value="PENDING" />
          <el-option label="Processing" value="PROCESSING" />
          <el-option label="Replied"    value="REPLIED" />
          <el-option label="Closed"     value="CLOSED" />
        </el-select>
        <el-button @click="load">Reload</el-button>
        <span class="hint">Total: {{ total }}</span>
      </div>
    </el-card>

    <!-- Table -->
    <el-card>
      <el-table :data="rows" v-loading="loading" stripe border style="width:100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="Category" width="140">
          <template #default="{ row }">
            <el-tag :type="categoryType(row.category)" size="small">{{ categoryLabel(row.category) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="Content" show-overflow-tooltip min-width="260" />
        <el-table-column prop="contact" label="Contact" width="160" show-overflow-tooltip />
        <el-table-column label="Status" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="Submitted" width="175" />
        <el-table-column prop="repliedAt" label="Replied At" width="175" />
        <el-table-column label="Actions" width="260" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              size="small" type="warning"
              @click="setStatus(row, 'PROCESSING')"
            >Processing</el-button>
            <el-button
              v-if="row.status !== 'REPLIED' && row.status !== 'CLOSED'"
              size="small" type="success"
              @click="setStatus(row, 'REPLIED')"
            >Mark Replied</el-button>
            <el-button
              v-if="row.status !== 'CLOSED'"
              size="small" type="info"
              @click="setStatus(row, 'CLOSED')"
            >Close</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="load"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { api, type UserFeedback } from '@/api/index';

const rows = ref<UserFeedback[]>([]);
const loading = ref(false);
const total = ref(0);
const page = ref(1);
const pageSize = ref(20);
const statusFilter = ref('');

const load = async () => {
  loading.value = true;
  try {
    const res = await api.listFeedbacks({
      page: page.value - 1,
      size: pageSize.value,
      status: statusFilter.value || undefined,
    }) as any;
    rows.value = res?.content ?? [];
    total.value = res?.totalElements ?? 0;
  } catch (e: any) {
    ElMessage.error(e?.message || 'Failed to load feedbacks');
  } finally {
    loading.value = false;
  }
};

const setStatus = async (row: UserFeedback, status: string) => {
  try {
    const updated = await api.updateFeedbackStatus(row.id, status) as unknown as UserFeedback;
    row.status = updated.status;
    row.repliedAt = updated.repliedAt;
    ElMessage.success(`Marked as ${status}`);
  } catch (e: any) {
    ElMessage.error(e?.message || 'Update failed');
  }
};

const categoryLabel = (c: string) => {
  const map: Record<string, string> = {
    FUNCTION_BUG: '🐛 Bug',
    SUGGESTION: '💡 Suggestion',
    CONTENT_REPORT: '🚨 Report',
    OTHER: '📝 Other',
  };
  return map[c] ?? c;
};

const categoryType = (c: string): '' | 'danger' | 'warning' | 'success' | 'info' => {
  if (c === 'FUNCTION_BUG') return 'danger';
  if (c === 'SUGGESTION') return 'success';
  if (c === 'CONTENT_REPORT') return 'warning';
  return 'info';
};

const statusType = (s: string): '' | 'danger' | 'warning' | 'success' | 'info' => {
  if (s === 'PENDING') return 'danger';
  if (s === 'PROCESSING') return 'warning';
  if (s === 'REPLIED') return 'success';
  return 'info';
};

onMounted(load);
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; align-items: center; }
.hint { font-size: 13px; color: #909399; }
.pager { display: flex; justify-content: flex-end; padding-top: 16px; }
</style>
