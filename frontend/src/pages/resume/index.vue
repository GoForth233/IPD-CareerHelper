<template>
  <view class="resume-page" :class="{ 'is-dark': darkPref }">
    <view class="status-spacer" :style="{ height: topSafeHeight + 'px' }"></view>

    <view class="page-header">
      <text class="page-title">简历库</text>
      <text class="page-subtitle">管理你的求职资产</text>
    </view>

    <view class="hero-card">
      <view class="hero-left">
        <text class="hero-count">{{ resumeList.length }}</text>
        <text class="hero-unit">份简历</text>
      </view>
      <view class="hero-deco"></view>
    </view>

    <view class="section-bar">
      <text class="section-title">我的简历</text>
      <text class="section-count">{{ resumeList.length }} 份文件</text>
    </view>

    <view class="resume-list">
      <view class="resume-card" v-for="(item, idx) in resumeList" :key="idx">
        <view class="rc-icon-wrap">
          <view class="rc-icon" :class="'rc-icon-' + (idx % 2)">
            <text class="rc-icon-text">PDF</text>
          </view>
        </view>
        <view class="rc-body">
          <text class="rc-name">{{ item.name }}</text>
          <view class="rc-meta-row">
            <text class="rc-time">{{ item.date }}</text>
            <view class="rc-badge" :class="'badge-' + item.status">
              <text class="rc-badge-text">{{ item.statusLabel }}</text>
            </view>
          </view>
        </view>
        <view class="rc-actions">
          <view class="rc-action-btn" @click="handlePreview(item)">
            <text class="rc-action-text">预览</text>
          </view>
          <view class="rc-more" @click="handleMore(idx)">
            <text class="rc-more-dots">···</text>
          </view>
        </view>
      </view>

      <view class="add-card" @click="handleUploadClick">
        <view class="add-icon">
          <text class="add-plus">+</text>
        </view>
        <view class="add-info">
          <text class="add-title">新建简历</text>
          <text class="add-desc">模板填写或上传 PDF</text>
        </view>
      </view>
    </view>

    <view class="bottom-safe"></view>

    <view class="sheet-mask" v-if="showSheet" @click="closeSheet"></view>
    <view class="sheet" :class="{ 'sheet-open': showSheet }">
      <view class="sheet-title-bar">
        <text class="sheet-title">选择创建方式</text>
      </view>
      <view class="sheet-option" @click="selectAction('template')">
        <text class="sheet-option-icon">📝</text>
        <text class="sheet-option-text">模板填写</text>
      </view>
      <view class="sheet-option" @click="selectAction('upload')">
        <text class="sheet-option-icon">📎</text>
        <text class="sheet-option-text">上传 PDF 简历</text>
      </view>
      <view class="sheet-cancel" @click="closeSheet">
        <text class="sheet-cancel-text">取消</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';

interface ResumeItem {
  name: string;
  date: string;
  status: 'recent' | 'normal';
  statusLabel: string;
}

const resumeList = ref<ResumeItem[]>([
  { name: '前端开发岗_2026秋招.pdf', date: '更新于 04-08', status: 'recent', statusLabel: '解析成功' },
  { name: '通用简历_v2.pdf', date: '更新于 04-05', status: 'normal', statusLabel: '可编辑' },
]);

const showSheet = ref(false);
const topSafeHeight = ref(44);
const darkPref = ref(false);

const handleUploadClick = () => {
  showSheet.value = true;
};

const closeSheet = () => {
  showSheet.value = false;
};

const selectAction = (type: string) => {
  closeSheet();

  if (type === 'upload') {
    uni.chooseMessageFile({
      count: 1,
      type: 'file',
      success: (fileRes) => {
        const file = fileRes.tempFiles?.[0];
        const fileName = file?.name || '未命名简历.pdf';

        if (!/\.pdf$/i.test(fileName)) {
          uni.showToast({ title: '请选择 PDF 文件', icon: 'none' });
          return;
        }

        uni.showLoading({ title: '上传并解析中...' });
        setTimeout(() => {
          uni.hideLoading();
          resumeList.value.unshift({
            name: fileName,
            date: '刚刚',
            status: 'recent',
            statusLabel: '解析成功',
          });
          uni.showToast({ title: '上传成功', icon: 'success' });
        }, 900);
      },
      fail: () => {
        uni.showToast({ title: '已取消选择', icon: 'none' });
      },
    });
    return;
  }

  if (type === 'template') {
    uni.navigateTo({ url: '/pages/resume/template' });
  }
};

const handlePreview = (item: ResumeItem) => {
  uni.showModal({
    title: '预览提示',
    content: `文件：${item.name}\n小程序内暂不支持完整 PDF 预览，可导出后使用系统阅读器查看。`,
    showCancel: false,
    confirmText: '知道了',
  });
};

const handleMore = (idx: number) => {
  const current = resumeList.value[idx];
  if (!current) return;

  uni.showActionSheet({
    itemList: ['重命名', '分享', '删除'],
    success: (res) => {
      if (res.tapIndex === 0) {
        const baseName = current.name.replace(/\.pdf$/i, '');
        resumeList.value[idx].name = `${baseName}_重命名.pdf`;
        uni.showToast({ title: '已重命名', icon: 'success' });
        return;
      }

      if (res.tapIndex === 1) {
        uni.setClipboardData({
          data: `https://ipd-project/resume/share/${encodeURIComponent(current.name)}`,
          success: () => uni.showToast({ title: '分享链接已复制', icon: 'none' }),
          fail: () => uni.showToast({ title: '复制失败，请重试', icon: 'none' }),
        });
        return;
      }

      if (res.tapIndex === 2) {
        uni.showModal({
          title: '确认删除',
          content: '确定删除这份简历吗？',
          success: (result) => {
            if (result.confirm) {
              resumeList.value.splice(idx, 1);
              uni.showToast({ title: '已删除', icon: 'success' });
            }
          },
        });
      }
    },
  });
};

onMounted(() => {
  darkPref.value = uni.getStorageSync('app_pref_dark') === '1';
  const systemInfo = uni.getSystemInfoSync();
  topSafeHeight.value = (systemInfo.statusBarHeight || 20) + 8;
});
</script>

<style scoped>
.resume-page { min-height: 100vh; background: #f5f5f7; padding: 0 20px; padding-bottom: env(safe-area-inset-bottom); font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue', sans-serif; box-sizing: border-box; }
.status-spacer { width: 100%; }
.page-header { padding: 8px 0 6px; }
.page-title { display: block; font-size: 32px; font-weight: 800; color: #0f172a; letter-spacing: -0.5px; }
.page-subtitle { display: block; font-size: 13px; color: #64748b; margin-top: 4px; line-height: 1.5; }
.hero-card { margin: 16px 0 24px; padding: 24px 20px; background: linear-gradient(135deg, #2563eb 0%, #1e40af 50%, #1e3a8a 100%); border-radius: 20px; position: relative; overflow: hidden; display: flex; align-items: center; box-shadow: 0 8px 24px rgba(37, 99, 235, 0.3); }
.hero-deco { position: absolute; top: -30px; right: -30px; width: 120px; height: 120px; border-radius: 60px; background: rgba(255, 255, 255, 0.08); }
.hero-left { display: flex; align-items: baseline; gap: 4px; margin-right: auto; }
.hero-count { font-size: 40px; font-weight: 800; color: #fff; letter-spacing: -2px; }
.hero-unit { font-size: 15px; font-weight: 500; color: rgba(255, 255, 255, 0.8); }
.section-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.section-title { font-size: 20px; font-weight: 700; color: #0f172a; }
.section-count { font-size: 12px; color: #64748b; }
.resume-list { display: flex; flex-direction: column; gap: 10px; margin-bottom: 28px; }
.resume-card { display: flex; align-items: center; background: #fff; border-radius: 16px; padding: 16px; border: 1px solid #dbe1ea; box-shadow: 0 5px 16px rgba(15, 23, 42, 0.08); }
.rc-icon-wrap { margin-right: 14px; flex-shrink: 0; }
.rc-icon { width: 44px; height: 52px; border-radius: 10px; display: flex; align-items: center; justify-content: center; }
.rc-icon-0 { background: linear-gradient(135deg, #dbeafe, #bfdbfe); }
.rc-icon-1 { background: linear-gradient(135deg, #e0e7ff, #c7d2fe); }
.rc-icon-text { font-size: 11px; font-weight: 800; color: #2563eb; letter-spacing: 0.5px; }
.rc-body { flex: 1; min-width: 0; }
.rc-name { font-size: 15px; font-weight: 600; color: #1e293b; display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 6px; }
.rc-meta-row { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.rc-time { font-size: 12px; color: #94a3b8; }
.rc-badge { padding: 2px 8px; border-radius: 6px; }
.badge-recent { background: #dbeafe; }
.badge-recent .rc-badge-text { color: #2563eb; }
.badge-normal { background: #eef2ff; }
.badge-normal .rc-badge-text { color: #4f46e5; }
.rc-badge-text { font-size: 11px; font-weight: 600; }
.rc-actions { display: flex; align-items: center; gap: 8px; margin-left: 10px; }
.rc-action-btn { padding: 6px 14px; border-radius: 10px; background: #eff6ff; }
.rc-action-text { font-size: 13px; font-weight: 500; color: #2563eb; }
.rc-more { padding: 6px 4px; }
.rc-more-dots { font-size: 16px; color: #94a3b8; font-weight: 700; letter-spacing: 1px; }
.add-card { display: flex; align-items: center; border: 1.5px dashed #a5b4fc; background: #fff; border-radius: 16px; padding: 16px; gap: 14px; box-shadow: 0 4px 12px rgba(15, 23, 42, 0.05); }
.add-icon { width: 44px; height: 44px; border-radius: 14px; background: #eff6ff; display: flex; align-items: center; justify-content: center; }
.add-plus { font-size: 22px; color: #2563eb; font-weight: 300; }
.add-title { font-size: 15px; font-weight: 600; color: #1e3a8a; margin-bottom: 3px; display: block; }
.add-desc { font-size: 12px; color: #64748b; display: block; }
.bottom-safe { height: 40px; }
.sheet-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.35); z-index: 998; }
.sheet { position: fixed; left: 12px; right: 12px; bottom: -320px; z-index: 999; transition: bottom 0.3s cubic-bezier(0.25, 0.8, 0.25, 1); }
.sheet-open { bottom: 30px; }
.sheet-title-bar { background: rgba(255,255,255,0.96); text-align: center; padding: 16px; border-top-left-radius: 16px; border-top-right-radius: 16px; border-bottom: 0.5px solid rgba(60,60,67,0.1); }
.sheet-title { font-size: 13px; color: #8e8e93; font-weight: 500; }
.sheet-option { background: rgba(255,255,255,0.96); display: flex; align-items: center; justify-content: center; gap: 10px; padding: 16px; border-bottom: 0.5px solid rgba(60,60,67,0.1); }
.sheet-option:last-of-type { border-bottom-left-radius: 16px; border-bottom-right-radius: 16px; border-bottom: none; }
.sheet-option-icon { font-size: 18px; }
.sheet-option-text { font-size: 17px; color: #007aff; }
.sheet-cancel { background: #fff; text-align: center; padding: 16px; border-radius: 16px; margin-top: 10px; }
.sheet-cancel-text { font-size: 17px; font-weight: 600; color: #007aff; }
.is-dark { background: #0f172a; }
.is-dark .page-title,
.is-dark .section-title,
.is-dark .rc-name,
.is-dark .add-title { color: #f8fafc; }
.is-dark .page-subtitle,
.is-dark .section-count,
.is-dark .rc-time,
.is-dark .add-desc { color: #94a3b8; }
.is-dark .resume-card,
.is-dark .add-card,
.is-dark .sheet-option,
.is-dark .sheet-title-bar,
.is-dark .sheet-cancel { background: #1e293b; border-color: #334155; }
</style>
