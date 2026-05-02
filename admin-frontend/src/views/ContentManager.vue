<template>
  <div>
    <el-tabs v-model="activeTab">
      <!-- ─── Videos tab ─── -->
      <el-tab-pane label="首页视频" name="videos">
        <el-table :data="videos" v-loading="loadingVideos" stripe border style="width:100%">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="title" label="标题" show-overflow-tooltip />
          <el-table-column prop="upName" label="UP主" width="140" />
          <el-table-column prop="keyword" label="关键词" width="110" />
          <el-table-column label="播放量" width="110">
            <template #default="{ row }">{{ fmtNum(row.viewCount) }}</template>
          </el-table-column>
          <el-table-column prop="fetchedAt" label="抓取时间" width="175" />
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="danger" @click="deleteVideo(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pager">
          <el-pagination
            v-model:current-page="videoPage"
            v-model:page-size="videoPageSize"
            :total="videoTotal"
            layout="total, prev, pager, next"
            @current-change="loadVideos"
          />
        </div>
      </el-tab-pane>

      <!-- ─── Articles tab ─── -->
      <el-tab-pane label="首页文章" name="articles">
        <div class="toolbar">
          <el-button type="primary" @click="openArticleDialog(null)">+ 新增文章</el-button>
        </div>
        <el-table :data="articles" v-loading="loadingArticles" stripe border style="width:100%">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="title" label="标题" show-overflow-tooltip />
          <el-table-column prop="category" label="分类" width="100" />
          <el-table-column prop="summary" label="摘要" show-overflow-tooltip />
          <el-table-column prop="publishedAt" label="发布时间" width="175" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openArticleDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteArticle(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- Article create/edit dialog -->
    <el-dialog v-model="articleDialog" :title="editingArticle?.id ? '编辑文章' : '新增文章'" width="560px">
      <el-form :model="articleForm" label-position="top">
        <el-form-item label="标题 *">
          <el-input v-model="articleForm.title" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="articleForm.summary" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="链接 *">
          <el-input v-model="articleForm.sourceUrl" placeholder="https://…" />
        </el-form-item>
        <el-form-item label="封面图 URL">
          <el-input v-model="articleForm.imageUrl" placeholder="https://…" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="articleForm.category" style="width:100%">
            <el-option label="interview" value="interview" />
            <el-option label="resume" value="resume" />
            <el-option label="skill" value="skill" />
            <el-option label="career" value="career" />
            <el-option label="other" value="other" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="articleDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingArticle" @click="saveArticle">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import http from '@/api';

interface VideoRow { id: number; bvid: string; title: string; upName?: string; keyword?: string; viewCount?: number; fetchedAt?: string; }
interface ArticleRow { id?: number; title: string; summary?: string; sourceUrl?: string; imageUrl?: string; category?: string; publishedAt?: string; }

const activeTab = ref('videos');

// ── Videos ──────────────────────────────────────
const videos = ref<VideoRow[]>([]);
const loadingVideos = ref(false);
const videoPage = ref(1);
const videoPageSize = ref(20);
const videoTotal = ref(0);

const loadVideos = async () => {
  loadingVideos.value = true;
  try {
    const data: any = await http.get('/api/admin/content/videos', {
      params: { page: videoPage.value - 1, size: videoPageSize.value }
    });
    videos.value = data.content;
    videoTotal.value = data.totalElements;
  } catch (e: any) { ElMessage.error(e?.message || '加载失败'); }
  finally { loadingVideos.value = false; }
};

const deleteVideo = async (row: VideoRow) => {
  await ElMessageBox.confirm(`确认删除视频「${row.title}」？`, '警告', { type: 'warning' });
  try {
    await http.delete(`/api/admin/content/videos/${row.id}`);
    ElMessage.success('已删除');
    loadVideos();
  } catch (e: any) { ElMessage.error(e?.message || '删除失败'); }
};

// ── Articles ─────────────────────────────────────
const articles = ref<ArticleRow[]>([]);
const loadingArticles = ref(false);
const articleDialog = ref(false);
const savingArticle = ref(false);
const editingArticle = ref<ArticleRow | null>(null);
const articleForm = ref<ArticleRow>({ title: '', summary: '', sourceUrl: '', imageUrl: '', category: 'career' });

const loadArticles = async () => {
  loadingArticles.value = true;
  try {
    articles.value = await http.get<ArticleRow[]>('/api/admin/content/articles') as any;
  } catch (e: any) { ElMessage.error(e?.message || '加载失败'); }
  finally { loadingArticles.value = false; }
};

const openArticleDialog = (row: ArticleRow | null) => {
  editingArticle.value = row;
  articleForm.value = row ? { ...row } : { title: '', summary: '', sourceUrl: '', imageUrl: '', category: 'career' };
  articleDialog.value = true;
};

const saveArticle = async () => {
  if (!articleForm.value.title?.trim()) { ElMessage.warning('标题不能为空'); return; }
  savingArticle.value = true;
  try {
    if (editingArticle.value?.id) {
      await http.put(`/api/admin/content/articles/${editingArticle.value.id}`, articleForm.value);
    } else {
      await http.post('/api/admin/content/articles', articleForm.value);
    }
    ElMessage.success('保存成功');
    articleDialog.value = false;
    loadArticles();
  } catch (e: any) { ElMessage.error(e?.message || '保存失败'); }
  finally { savingArticle.value = false; }
};

const deleteArticle = async (row: ArticleRow) => {
  await ElMessageBox.confirm(`确认删除文章「${row.title}」？`, '警告', { type: 'warning' });
  try {
    await http.delete(`/api/admin/content/articles/${row.id}`);
    ElMessage.success('已删除');
    loadArticles();
  } catch (e: any) { ElMessage.error(e?.message || '删除失败'); }
};

const fmtNum = (n?: number) => n == null ? '-' : n.toLocaleString();

watch(activeTab, (tab) => {
  if (tab === 'videos') loadVideos();
  else if (tab === 'articles') loadArticles();
});

onMounted(() => loadVideos());
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
.pager { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
