<template>
  <div>
    <el-card>
      <div class="row-controls">
        <el-input v-model="filter" placeholder="Filter by position or content..." clearable style="max-width: 280px;" />
        <el-button @click="load">Reload</el-button>
      </div>
    </el-card>
    <el-card style="margin-top: 16px;">
      <el-table :data="filtered" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="position" label="Position" width="180" />
        <el-table-column prop="difficulty" label="Difficulty" width="110" />
        <el-table-column prop="content" label="Content" />
        <el-table-column prop="likes" label="Likes" width="80" />
        <el-table-column prop="drawCount" label="Draws" width="80" />
        <el-table-column prop="status" label="Status" width="100" />
        <el-table-column label="Actions" width="220">
          <template #default="{ row }">
            <el-button size="small" @click="edit(row)">Edit</el-button>
            <el-button v-if="row.status === 'APPROVED'" size="small" @click="setStatus(row, 'HIDDEN')">Hide</el-button>
            <el-button v-else size="small" @click="setStatus(row, 'APPROVED')">Show</el-button>
            <el-button size="small" type="danger" @click="remove(row)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="Edit question" width="640px">
      <el-form :model="form" label-position="top">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="Position"><el-input v-model="form.position" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="Difficulty">
            <el-select v-model="form.difficulty"><el-option label="Easy" value="Easy" /><el-option label="Normal" value="Normal" /><el-option label="Hard" value="Hard" /></el-select>
          </el-form-item></el-col>
          <el-col :span="6"><el-form-item label="Status">
            <el-select v-model="form.status"><el-option label="APPROVED" value="APPROVED" /><el-option label="HIDDEN" value="HIDDEN" /></el-select>
          </el-form-item></el-col>
        </el-row>
        <el-form-item label="Summary"><el-input v-model="form.summary" /></el-form-item>
        <el-form-item label="Content"><el-input v-model="form.content" type="textarea" :autosize="{ minRows: 4 }" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="save">Save</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { api, type InterviewQuestion } from '@/api';

const items = ref<InterviewQuestion[]>([]);
const filter = ref('');
const dialogVisible = ref(false);
const form = reactive<InterviewQuestion>({
  id: undefined, position: '', difficulty: 'Normal', content: '', summary: '', status: 'APPROVED'
});

const filtered = computed(() => {
  if (!filter.value) return items.value;
  const q = filter.value.toLowerCase();
  return items.value.filter((i) =>
    (i.position || '').toLowerCase().includes(q) ||
    (i.content || '').toLowerCase().includes(q)
  );
});

const load = async () => {
  try { items.value = await api.listQuestions(); } catch (e: any) { ElMessage.error(e?.message || 'Failed'); }
};

const edit = (row: InterviewQuestion) => {
  Object.assign(form, row);
  dialogVisible.value = true;
};

const save = async () => {
  if (!form.id) return;
  try {
    await api.updateQuestion(form.id, form);
    dialogVisible.value = false;
    ElMessage.success('Saved');
    load();
  } catch (e: any) { ElMessage.error(e?.message || 'Failed'); }
};

const setStatus = async (row: InterviewQuestion, status: string) => {
  if (!row.id) return;
  try {
    await api.updateQuestion(row.id, { status });
    ElMessage.success(status === 'HIDDEN' ? 'Hidden' : 'Shown');
    load();
  } catch (e: any) { ElMessage.error(e?.message || 'Failed'); }
};

const remove = async (row: InterviewQuestion) => {
  if (!row.id) return;
  try {
    await ElMessageBox.confirm('Delete this question?', 'Confirm', { type: 'warning' });
    await api.deleteQuestion(row.id);
    ElMessage.success('Deleted');
    load();
  } catch {}
};

onMounted(load);
</script>

<style scoped>
.row-controls { display: flex; gap: 12px; align-items: center; }
</style>
