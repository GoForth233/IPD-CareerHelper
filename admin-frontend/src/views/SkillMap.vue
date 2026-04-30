<template>
  <div>
    <el-row :gutter="16">
      <el-col :span="10">
        <el-card header="Career paths">
          <el-button type="primary" size="small" style="margin-bottom: 8px;" @click="newPath">New path</el-button>
          <el-table :data="paths" highlight-current-row @row-click="onPathClick" :row-key="(r: any) => r.pathId">
            <el-table-column prop="code" label="Code" width="140" />
            <el-table-column prop="name" label="Name" />
            <el-table-column label="Actions" width="160">
              <template #default="{ row }">
                <el-button size="small" @click.stop="editPath(row)">Edit</el-button>
                <el-button size="small" type="danger" @click.stop="removePath(row)">Delete</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card>
          <template #header>
            <div class="card-head">
              <span>Nodes — {{ activePath?.name || '(select a path)' }}</span>
              <el-button v-if="activePath" type="primary" size="small" @click="newNode">New node</el-button>
            </div>
          </template>
          <el-table :data="nodes">
            <el-table-column prop="nodeId" label="ID" width="80" />
            <el-table-column prop="name" label="Name" />
            <el-table-column prop="level" label="Level" width="80" />
            <el-table-column prop="sortOrder" label="Order" width="80" />
            <el-table-column prop="estimatedHours" label="Hours" width="80" />
            <el-table-column label="Actions" width="160">
              <template #default="{ row }">
                <el-button size="small" @click="editNode(row)">Edit</el-button>
                <el-button size="small" type="danger" @click="removeNode(row)">Delete</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="pathDialogVisible" :title="pathForm.pathId ? 'Edit path' : 'New path'" width="480px">
      <el-form :model="pathForm" label-position="top">
        <el-form-item label="Code"><el-input v-model="pathForm.code" /></el-form-item>
        <el-form-item label="Name"><el-input v-model="pathForm.name" /></el-form-item>
        <el-form-item label="Description"><el-input v-model="pathForm.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pathDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="savePath">Save</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="nodeDialogVisible" :title="nodeForm.nodeId ? 'Edit node' : 'New node'" width="520px">
      <el-form :model="nodeForm" label-position="top">
        <el-form-item label="Name"><el-input v-model="nodeForm.name" /></el-form-item>
        <el-form-item label="Description"><el-input v-model="nodeForm.description" type="textarea" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="Level"><el-input-number v-model="nodeForm.level" :min="1" :max="10" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="Sort order"><el-input-number v-model="nodeForm.sortOrder" :min="0" :max="999" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="Hours"><el-input-number v-model="nodeForm.estimatedHours" :min="1" :max="500" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="Parent node id (0 = root)"><el-input-number v-model="nodeForm.parentId" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="nodeDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="saveNode">Save</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { api, type CareerNode, type CareerPath } from '@/api';

const paths = ref<CareerPath[]>([]);
const activePath = ref<CareerPath | null>(null);
const nodes = ref<CareerNode[]>([]);

const pathDialogVisible = ref(false);
const pathForm = reactive<CareerPath>({ code: '', name: '', description: '' });
const nodeDialogVisible = ref(false);
const nodeForm = reactive<CareerNode>({ pathId: 0, name: '', description: '', level: 1, sortOrder: 0, estimatedHours: 10, parentId: 0 });

const loadPaths = async () => {
  try { paths.value = await api.listPaths(); } catch (e: any) { ElMessage.error(e?.message || 'Failed'); }
};

const onPathClick = async (row: CareerPath) => {
  activePath.value = row;
  if (!row.pathId) return;
  try { nodes.value = await api.listNodes(row.pathId); } catch (e: any) { ElMessage.error(e?.message || 'Failed'); }
};

const newPath = () => {
  Object.assign(pathForm, { pathId: undefined, code: '', name: '', description: '' });
  pathDialogVisible.value = true;
};

const editPath = (row: CareerPath) => {
  Object.assign(pathForm, row);
  pathDialogVisible.value = true;
};

const savePath = async () => {
  try {
    await api.savePath({ ...pathForm });
    pathDialogVisible.value = false;
    ElMessage.success('Saved');
    loadPaths();
  } catch (e: any) { ElMessage.error(e?.message || 'Failed'); }
};

const removePath = async (row: CareerPath) => {
  if (!row.pathId) return;
  try {
    await ElMessageBox.confirm('Delete this path?', 'Confirm', { type: 'warning' });
    await api.deletePath(row.pathId);
    ElMessage.success('Deleted');
    loadPaths();
  } catch {}
};

const newNode = () => {
  if (!activePath.value?.pathId) return;
  Object.assign(nodeForm, { nodeId: undefined, pathId: activePath.value.pathId, name: '', description: '', level: 1, sortOrder: 0, estimatedHours: 10, parentId: 0 });
  nodeDialogVisible.value = true;
};

const editNode = (row: CareerNode) => {
  Object.assign(nodeForm, row);
  nodeDialogVisible.value = true;
};

const saveNode = async () => {
  if (!activePath.value?.pathId) return;
  try {
    await api.saveNode(activePath.value.pathId, { ...nodeForm });
    nodeDialogVisible.value = false;
    ElMessage.success('Saved');
    onPathClick(activePath.value);
  } catch (e: any) { ElMessage.error(e?.message || 'Failed'); }
};

const removeNode = async (row: CareerNode) => {
  if (!row.nodeId) return;
  try {
    await ElMessageBox.confirm('Delete this node?', 'Confirm', { type: 'warning' });
    await api.deleteNode(row.nodeId);
    ElMessage.success('Deleted');
    if (activePath.value) onPathClick(activePath.value);
  } catch {}
};

onMounted(loadPaths);
</script>

<style scoped>
.card-head { display: flex; align-items: center; justify-content: space-between; }
</style>
