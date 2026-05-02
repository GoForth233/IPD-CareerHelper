<template>
  <div>
    <!-- Header row -->
    <div class="toolbar">
      <el-input
        v-model="searchQ"
        placeholder="Search by nickname…"
        style="width: 260px"
        clearable
        @keyup.enter="loadUsers"
        @clear="loadUsers"
      />
      <el-button type="primary" @click="loadUsers">Search</el-button>
      <el-button @click="openBroadcast">📢 Broadcast</el-button>
    </div>

    <!-- User table -->
    <el-table :data="users" v-loading="loading" stripe border style="width: 100%">
      <el-table-column prop="userId" label="ID" width="80" />
      <el-table-column prop="nickname" label="Nickname" />
      <el-table-column prop="school" label="School" />
      <el-table-column prop="major" label="Major" />
      <el-table-column label="Status" width="110">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'danger' : 'info'">
            {{ row.status === 1 ? 'Active' : row.status === 2 ? 'Banned' : 'Inactive' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="bannedReason" label="Ban Reason" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="Registered" width="170" />
      <el-table-column label="Actions" width="180" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status !== 2" size="small" type="danger" @click="openBan(row)">Ban</el-button>
          <el-button v-else size="small" type="success" @click="doUnban(row)">Unban</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Pagination -->
    <div class="pager">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadUsers"
      />
    </div>

    <!-- Ban dialog -->
    <el-dialog v-model="banDialog" title="Ban User" width="420px">
      <p>User: <strong>{{ banTarget?.nickname }}</strong> (#{{ banTarget?.userId }})</p>
      <el-input
        v-model="banReason"
        placeholder="Enter ban reason…"
        type="textarea"
        :rows="3"
      />
      <template #footer>
        <el-button @click="banDialog = false">Cancel</el-button>
        <el-button type="danger" :loading="banLoading" @click="doConfirmBan">Confirm Ban</el-button>
      </template>
    </el-dialog>

    <!-- Broadcast dialog -->
    <el-dialog v-model="broadcastDialog" title="Admin Broadcast" width="480px">
      <el-form label-position="top">
        <el-form-item label="Target">
          <el-input v-model="broadcast.userId" placeholder="User ID (leave blank to send to all active users)" />
        </el-form-item>
        <el-form-item label="Title">
          <el-input v-model="broadcast.title" placeholder="Notification title" />
        </el-form-item>
        <el-form-item label="Content">
          <el-input v-model="broadcast.content" type="textarea" :rows="3" placeholder="Notification content" />
        </el-form-item>
        <el-form-item label="Link (optional)">
          <el-input v-model="broadcast.link" placeholder="/pages/…" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="broadcastDialog = false">Cancel</el-button>
        <el-button type="primary" :loading="broadcastLoading" @click="doSendBroadcast">Send</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import http from '@/api';

interface UserRow {
  userId: number;
  nickname: string;
  school?: string;
  major?: string;
  status?: number;
  bannedReason?: string;
  createdAt?: string;
}

const loading = ref(false);
const users = ref<UserRow[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(20);
const searchQ = ref('');

const banDialog = ref(false);
const banTarget = ref<UserRow | null>(null);
const banReason = ref('');
const banLoading = ref(false);

const broadcastDialog = ref(false);
const broadcastLoading = ref(false);
const broadcast = ref({ userId: '', title: '', content: '', link: '' });

const loadUsers = async () => {
  loading.value = true;
  try {
    const params: Record<string, string | number> = { page: page.value - 1, size: pageSize.value };
    if (searchQ.value.trim()) params.q = searchQ.value.trim();
    const data = await http.get<{ content: UserRow[]; totalElements: number }>('/api/admin/users', { params }).then((r: any) => r);
    users.value = (data as any).content;
    total.value = (data as any).totalElements;
  } catch (e: any) {
    ElMessage.error(e?.message || 'Failed to load users');
  } finally {
    loading.value = false;
  }
};

const openBan = (row: UserRow) => {
  banTarget.value = row;
  banReason.value = '';
  banDialog.value = true;
};

const doConfirmBan = async () => {
  if (!banTarget.value) return;
  banLoading.value = true;
  try {
    await http.post(`/api/admin/users/${banTarget.value.userId}/ban`, { reason: banReason.value });
    ElMessage.success(`User #${banTarget.value.userId} banned`);
    banDialog.value = false;
    loadUsers();
  } catch (e: any) {
    ElMessage.error(e?.message || 'Ban failed');
  } finally {
    banLoading.value = false;
  }
};

const doUnban = async (row: UserRow) => {
  try {
    await http.post(`/api/admin/users/${row.userId}/unban`);
    ElMessage.success(`User #${row.userId} unbanned`);
    loadUsers();
  } catch (e: any) {
    ElMessage.error(e?.message || 'Unban failed');
  }
};

const openBroadcast = () => {
  broadcast.value = { userId: '', title: '', content: '', link: '' };
  broadcastDialog.value = true;
};

const doSendBroadcast = async () => {
  if (!broadcast.value.title || !broadcast.value.content) {
    ElMessage.warning('Title and content are required');
    return;
  }
  broadcastLoading.value = true;
  try {
    const body: Record<string, string | number | null> = {
      title: broadcast.value.title,
      content: broadcast.value.content,
      link: broadcast.value.link || null,
      userId: broadcast.value.userId ? Number(broadcast.value.userId) : null,
    };
    const count = await http.post<number>('/api/admin/broadcast', body).then((r: any) => r);
    ElMessage.success(`Broadcast sent to ${count} user(s)`);
    broadcastDialog.value = false;
  } catch (e: any) {
    ElMessage.error(e?.message || 'Broadcast failed');
  } finally {
    broadcastLoading.value = false;
  }
};

onMounted(() => loadUsers());
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
