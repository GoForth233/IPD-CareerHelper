<template>
  <view class="msg-page" :class="{ 'is-dark': darkPref }">
    <view class="status-spacer" :style="{ height: topSafeHeight + 'px' }"></view>

    <view class="page-header">
      <text class="page-title">消息</text>
    </view>

    <view class="segment-wrap">
      <view class="segment-bar">
        <view
          v-for="tab in tabs"
          :key="tab.key"
          class="seg-item"
          :class="{ 'seg-active': currentTab === tab.key }"
          @click="currentTab = tab.key"
        >
          <text>{{ tab.label }}</text>
          <view class="seg-badge" v-if="tab.count > 0">
            <text class="badge-num">{{ tab.count }}</text>
          </view>
        </view>
      </view>
    </view>

    <scroll-view class="msg-list" scroll-y>
      <view v-if="currentTab === 'hr'" class="list-wrap">
        <view
          class="msg-card"
          :class="{ 'msg-unread': item.unread }"
          v-for="(item, idx) in hrMessages"
          :key="idx"
          @click="openHrModal(item)"
        >
          <view class="avatar-wrap">
            <view class="msg-avatar" :class="'av-' + (idx % 3)">
              <text class="av-text">{{ item.avatarChar }}</text>
            </view>
            <view class="unread-dot" v-if="item.unread"></view>
          </view>
          <view class="msg-body">
            <view class="msg-top-row">
              <text class="msg-name">{{ item.name }}</text>
              <text class="msg-time">{{ item.time }}</text>
            </view>
            <text class="msg-preview">{{ item.preview }}</text>
          </view>
        </view>
      </view>

      <view v-if="currentTab === 'system'" class="list-wrap">
        <view
          class="msg-card"
          :class="{ 'msg-unread': item.unread }"
          v-for="(item, idx) in systemMessages"
          :key="idx"
          @click="handleSystemClick(item)"
        >
          <view class="avatar-wrap">
            <view class="msg-avatar sys-av" :class="'sys-' + (idx % 2)">
              <text class="av-emoji">{{ item.icon }}</text>
            </view>
            <view class="unread-dot" v-if="item.unread"></view>
          </view>
          <view class="msg-body">
            <view class="msg-top-row">
              <text class="msg-name">{{ item.name }}</text>
              <text class="msg-time">{{ item.time }}</text>
            </view>
            <text class="msg-preview">{{ item.preview }}</text>
          </view>
        </view>
      </view>

      <view v-if="currentTab === 'status'" class="list-wrap">
        <view class="msg-card" v-for="(item, idx) in statusMessages" :key="idx" @click="openStatusModal(item)">
          <view class="avatar-wrap">
            <view class="msg-avatar status-av">
              <text class="av-emoji">{{ item.icon }}</text>
            </view>
          </view>
          <view class="msg-body">
            <view class="msg-top-row">
              <text class="msg-name">{{ item.name }}</text>
              <text class="msg-time">{{ item.time }}</text>
            </view>
            <text class="msg-preview">{{ item.preview }}</text>
            <view class="status-tag" :class="'tag-' + item.status">
              <text class="tag-text">{{ item.statusLabel }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="empty-state" v-if="currentListEmpty">
        <text class="empty-icon">📭</text>
        <text class="empty-text">暂无消息</text>
        <text class="empty-sub">新消息会出现在这里</text>
      </view>
    </scroll-view>

    <view class="modal-overlay" v-if="activeHrChat" @click="activeHrChat = null">
      <view class="modal-content bottom-sheet" @click.stop>
        <view class="sheet-handle"></view>
        <view class="modal-header">
          <view class="msg-avatar av-0 sheet-av"><text class="av-text">{{ activeHrChat.avatarChar }}</text></view>
          <text class="modal-title">{{ activeHrChat.name }}</text>
        </view>
        <view class="modal-body">
          <view class="chat-bubble received">
            <text>{{ activeHrChat.preview }}</text>
          </view>
        </view>
        <view class="modal-actions">
          <input class="reply-input" placeholder="输入回复内容..." @confirm="replyHr" />
          <button class="btn-primary" @click="viewProfile">查看岗位</button>
        </view>
      </view>
    </view>

    <view class="modal-overlay" v-if="activeStatus" @click="activeStatus = null">
      <view class="modal-content bottom-sheet" @click.stop>
        <view class="sheet-handle"></view>
        <view class="modal-header">
          <text class="modal-title">{{ activeStatus.name }}</text>
        </view>
        <view class="modal-body">
          <view class="timeline">
            <view class="tl-item completed">
              <view class="tl-dot"></view>
              <view class="tl-line"></view>
              <view class="tl-content">
                <text class="tl-title">已投递简历</text>
                <text class="tl-time">2天前</text>
              </view>
            </view>
            <view class="tl-item" :class="{ completed: activeStatus.status === 'progress' || activeStatus.status === 'pass' }">
              <view class="tl-dot"></view>
              <view class="tl-line"></view>
              <view class="tl-content">
                <text class="tl-title">复筛中</text>
                <text class="tl-time" v-if="activeStatus.status !== 'sent'">昨天</text>
              </view>
            </view>
            <view class="tl-item" :class="{ completed: activeStatus.status === 'pass' }">
              <view class="tl-dot"></view>
              <view class="tl-content">
                <text class="tl-title">已通过</text>
                <text class="tl-time" v-if="activeStatus.status === 'pass'">今天</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';

const currentTab = ref<'hr' | 'system' | 'status'>('hr');
const topSafeHeight = ref(44);
const darkPref = ref(false);
const activeHrChat = ref<any>(null);
const activeStatus = ref<any>(null);

const tabs = ref([
  { key: 'hr', label: '沟通', count: 2 },
  { key: 'system', label: '通知', count: 1 },
  { key: 'status', label: '投递', count: 0 },
]);

const hrMessages = ref([
  {
    avatarChar: '字',
    name: '字节跳动 · 张HR',
    time: '10:30',
    preview: '你的简历已通过初筛，方便本周五进行线上沟通吗？',
    unread: true,
  },
  {
    avatarChar: '腾',
    name: '腾讯 · 李HR',
    time: '昨天',
    preview: '你好，我们在看前端岗简历，想进一步了解你的项目经历。',
    unread: true,
  },
  {
    avatarChar: '阿',
    name: '阿里 · 王HR',
    time: '周一',
    preview: '恭喜你通过笔试，请留意后续面试邀请邮件。',
    unread: false,
  },
]);

const systemMessages = ref([
  {
    icon: '🔔',
    name: '系统通知',
    time: '刚刚',
    preview: '你的面试复盘报告已生成，点击查看详情。',
    unread: true,
  },
  {
    icon: '✨',
    name: 'AI助手',
    time: '昨天',
    preview: '简历诊断完成，匹配度 85 分，建议优化 3 处内容。',
    unread: false,
  },
]);

const statusMessages = ref([
  {
    icon: '🚀',
    name: '前端开发工程师 · 字节跳动',
    time: '14:20',
    preview: '状态更新：已进入复筛阶段，请保持电话畅通。',
    status: 'progress',
    statusLabel: '复筛中',
  },
  {
    icon: '📝',
    name: '全栈工程师 · 美团',
    time: '2天前',
    preview: '投递成功：你的简历已送达招聘负责人。',
    status: 'sent',
    statusLabel: '已投递',
  },
  {
    icon: '✅',
    name: '后端实习生 · 小红书',
    time: '上周',
    preview: '恭喜你通过终面，offer 将在 3 个工作日内发出。',
    status: 'pass',
    statusLabel: '已通过',
  },
]);

const currentListEmpty = computed(() => {
  if (currentTab.value === 'hr') return hrMessages.value.length === 0;
  if (currentTab.value === 'system') return systemMessages.value.length === 0;
  return statusMessages.value.length === 0;
});

const openHrModal = (item: any) => {
  item.unread = false;
  tabs.value[0].count = hrMessages.value.filter((m) => m.unread).length;
  activeHrChat.value = item;
};

const replyHr = () => {
  activeHrChat.value = null;
  uni.showToast({ title: '回复已发送', icon: 'success' });
};

const viewProfile = () => {
  uni.showToast({ title: '下个版本开放岗位详情', icon: 'none' });
};

const handleSystemClick = (item: any) => {
  item.unread = false;
  tabs.value[1].count = systemMessages.value.filter((m) => m.unread).length;

  if (item.preview.includes('复盘')) {
    uni.navigateTo({ url: '/pages/interview/report' });
  } else if (item.preview.includes('简历诊断')) {
    uni.navigateTo({ url: '/pages/resume-ai/index' });
  }
};

const openStatusModal = (item: any) => {
  activeStatus.value = item;
};

onMounted(() => {
  darkPref.value = uni.getStorageSync('app_pref_dark') === '1';
  const systemInfo = uni.getSystemInfoSync();
  topSafeHeight.value = (systemInfo.statusBarHeight || 20) + 8;
});
</script>

<style scoped>
.msg-page { height: 100vh; background: #f5f7fa; display: flex; flex-direction: column; box-sizing: border-box; font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue', sans-serif; }
.status-spacer { width: 100%; flex-shrink: 0; }
.page-header { padding: 8px 20px 12px; }
.page-title { font-size: 32px; font-weight: 800; color: #0f172a; letter-spacing: -0.5px; }
.segment-wrap { padding: 0 20px 16px; }
.segment-bar { display: flex; background: #e8ecf1; border-radius: 12px; padding: 3px; gap: 2px; border: 1px solid #dbe1ea; }
.seg-item { flex: 1; display: flex; align-items: center; justify-content: center; height: 36px; border-radius: 10px; font-size: 14px; font-weight: 500; color: #64748b; position: relative; gap: 6px; }
.seg-active { background: #fff; color: #0f172a; font-weight: 600; box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08); }
.seg-badge { min-width: 18px; height: 18px; border-radius: 9px; background: #ef4444; display: flex; align-items: center; justify-content: center; padding: 0 5px; }
.badge-num { font-size: 10px; font-weight: 700; color: #fff; }
.msg-list { flex: 1; height: 0; min-height: 0; padding-bottom: calc(20px + env(safe-area-inset-bottom)); box-sizing: border-box; }
.list-wrap { padding: 0 20px; display: flex; flex-direction: column; gap: 10px; }
.msg-card { display: flex; align-items: center; background: #fff; padding: 16px; border-radius: 16px; border: 1px solid #dbe1ea; box-shadow: 0 5px 16px rgba(15, 23, 42, 0.08); }
.msg-unread { border-left: 3px solid #2563eb; }
.avatar-wrap { position: relative; margin-right: 14px; flex-shrink: 0; }
.msg-avatar { width: 48px; height: 48px; border-radius: 16px; display: flex; align-items: center; justify-content: center; }
.av-text { font-size: 18px; font-weight: 700; color: #fff; }
.av-0 { background: linear-gradient(135deg, #3b82f6, #2563eb); }
.av-1 { background: linear-gradient(135deg, #10b981, #059669); }
.av-2 { background: linear-gradient(135deg, #f97316, #ea580c); }
.sys-0 { background: linear-gradient(135deg, #fef08a, #fbbf24); }
.sys-1 { background: linear-gradient(135deg, #c4b5fd, #8b5cf6); }
.status-av { background: linear-gradient(135deg, #dbeafe, #bfdbfe); border-radius: 16px; }
.av-emoji { font-size: 22px; }
.unread-dot { position: absolute; top: -2px; right: -2px; width: 10px; height: 10px; border-radius: 5px; background: #2563eb; border: 2px solid #fff; }
.msg-body { flex: 1; min-width: 0; }
.msg-top-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 5px; }
.msg-name { font-size: 15px; font-weight: 600; color: #1e293b; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; flex: 1; margin-right: 8px; }
.msg-time { font-size: 12px; color: #94a3b8; flex-shrink: 0; }
.msg-preview { font-size: 13px; color: #64748b; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; line-height: 1.5; }
.status-tag { display: inline-flex; margin-top: 8px; padding: 3px 10px; border-radius: 8px; }
.tag-text { font-size: 11px; font-weight: 600; }
.tag-progress { background: #eff6ff; }
.tag-progress .tag-text { color: #2563eb; }
.tag-sent { background: #f0fdf4; }
.tag-sent .tag-text { color: #16a34a; }
.tag-pass { background: #fef3c7; }
.tag-pass .tag-text { color: #d97706; }
.empty-state { display: flex; flex-direction: column; align-items: center; padding: 80px 20px; }
.empty-icon { font-size: 48px; margin-bottom: 16px; }
.empty-text { font-size: 16px; font-weight: 600; color: #94a3b8; margin-bottom: 6px; }
.empty-sub { font-size: 13px; color: #cbd5e1; }
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0, 0, 0, 0.4); z-index: 1000; display: flex; align-items: flex-end; }
.modal-content.bottom-sheet { width: 100%; background: #fff; border-radius: 24px 24px 0 0; padding: 16px 20px 32px; box-sizing: border-box; min-height: 250px; border-top: 1px solid #e2e8f0; }
.sheet-handle { width: 36px; height: 5px; border-radius: 3px; background: #e2e8f0; margin: 0 auto 16px; }
.modal-header { display: flex; align-items: center; margin-bottom: 24px; }
.sheet-av { width: 40px; height: 40px; margin-right: 12px; }
.modal-title { font-size: 18px; font-weight: 700; color: #0f172a; }
.chat-bubble { background: #f1f5f9; padding: 14px 16px; border-radius: 4px 16px 16px 16px; font-size: 15px; color: #1e293b; line-height: 1.5; margin-bottom: 24px; }
.modal-actions { display: flex; flex-direction: column; gap: 12px; }
.reply-input { width: 100%; height: 48px; background: #f8fafc; border: 1.5px solid #e2e8f0; border-radius: 12px; padding: 0 16px; font-size: 15px; box-sizing: border-box; }
.btn-primary { width: 100%; height: 48px; background: #2563eb; color: #fff; border-radius: 12px; font-weight: 600; line-height: 48px; border: none; }
.timeline { display: flex; flex-direction: column; padding-left: 8px; }
.tl-item { position: relative; padding-left: 28px; padding-bottom: 24px; }
.tl-item:last-child { padding-bottom: 8px; }
.tl-dot { position: absolute; left: 0; top: 4px; width: 12px; height: 12px; border-radius: 6px; background: #cbd5e1; z-index: 2; }
.tl-line { position: absolute; left: 5.5px; top: 16px; bottom: -4px; width: 1px; background: #e2e8f0; z-index: 1; }
.tl-item.completed .tl-dot { background: #2563eb; }
.tl-item.completed .tl-line { background: #2563eb; }
.tl-content { display: flex; flex-direction: column; gap: 4px; }
.tl-title { font-size: 15px; font-weight: 600; color: #1e293b; }
.tl-time { font-size: 12px; color: #94a3b8; }
.tl-item:not(.completed) .tl-title { color: #94a3b8; font-weight: 500; }
.is-dark { background: #0f172a; }
.is-dark .page-title,
.is-dark .seg-active,
.is-dark .msg-name { color: #f8fafc; }
.is-dark .segment-bar,
.is-dark .msg-card { background: #1e293b; box-shadow: none; border-color: #334155; }
.is-dark .seg-item,
.is-dark .msg-preview,
.is-dark .msg-time { color: #94a3b8; }
.is-dark .modal-content { background: #1e293b; border-color: #334155; }
.is-dark .sheet-handle { background: #334155; }
.is-dark .modal-title { color: #f8fafc; }
.is-dark .chat-bubble { background: #0f172a; color: #e2e8f0; }
.is-dark .reply-input { background: #0f172a; border-color: #334155; color: #f8fafc; }
.is-dark .tl-title { color: #f8fafc; }
.is-dark .tl-item:not(.completed) .tl-title { color: #64748b; }
.is-dark .tl-dot,
.is-dark .tl-line { background: #334155; }
</style>
