<template>
  <view class="chat-page">
    <view class="chat-nav">
      <view class="nav-spacer" :style="{ height: topSafeHeight + 'px' }"></view>
      <view class="nav-row">
        <view class="nav-bot-avatar">
          <text class="nav-bot-emoji">🤖</text>
        </view>
        <view class="nav-meta">
          <text class="nav-name">AI职业顾问</text>
          <view class="nav-status-row">
            <view class="online-dot"></view>
            <text class="nav-status">在线 · 已结合你的求职画像</text>
          </view>
        </view>
      </view>
    </view>

    <scroll-view class="chat-list" scroll-y :scroll-top="scrollTop" scroll-with-animation>
      <view class="welcome-card">
        <view class="welcome-icon">🎯</view>
        <text class="welcome-title">你的专属求职助手</text>
        <text class="welcome-desc">我可以结合你的测评和简历，给你个性化的求职建议。</text>
        <view class="quick-actions">
          <view class="quick-chip" @click="sendQuick('帮我优化简历')">
            <text class="chip-text">优化简历</text>
          </view>
          <view class="quick-chip" @click="sendQuick('给我一些面试技巧')">
            <text class="chip-text">面试技巧</text>
          </view>
          <view class="quick-chip" @click="sendQuick('前端岗位薪资范围怎么样')">
            <text class="chip-text">行业薪资</text>
          </view>
        </view>
      </view>

      <view class="time-divider">
        <text class="time-text">今日</text>
      </view>

      <view class="msg-row" v-for="(msg, index) in messages" :key="index" :class="msg.role === 'user' ? 'row-user' : 'row-ai'">
        <view class="bot-avatar" v-if="msg.role === 'ai'">
          <text class="bot-emoji">🤖</text>
        </view>

        <view class="bubble-area">
          <view class="bubble" :class="msg.role === 'user' ? 'bubble-user' : 'bubble-ai'">
            <view class="typing-dots" v-if="msg.isTyping">
              <view class="dot"></view>
              <view class="dot"></view>
              <view class="dot"></view>
            </view>
            <text class="bubble-text" v-else>{{ msg.content }}</text>
          </view>
        </view>
      </view>

      <view class="scroll-pad"></view>
    </scroll-view>

    <view class="input-bar" :style="{ paddingBottom: safeBottom + 'px' }">
      <view class="input-row">
        <input
          class="chat-input"
          v-model="inputText"
          placeholder="随时问我求职相关问题..."
          placeholder-class="input-ph"
          @confirm="sendMessage"
          confirm-type="send"
        />
        <view class="send-btn" :class="{ 'send-active': inputText.trim().length > 0 }" @click="sendMessage">
          <text class="send-arrow">↑</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue';

interface ChatMessage {
  role: 'user' | 'ai';
  content: string;
  isTyping?: boolean;
}

const messages = ref<ChatMessage[]>([
  {
    role: 'ai',
    content: '你好！我是你的AI职业顾问。你可以问我简历优化、面试准备、岗位选择和薪资信息。',
  },
]);

const inputText = ref('');
const scrollTop = ref(0);
const safeBottom = ref(20);
const topSafeHeight = ref(44);

const scrollToBottom = () => {
  nextTick(() => {
    scrollTop.value = scrollTop.value === 99998 ? 99999 : 99998;
  });
};

const sendQuick = (text: string) => {
  inputText.value = text;
  sendMessage();
};

const mockReplies = [
  '建议你在简历项目经历里用“动作+技术+结果”结构写 bullet，比如：基于 Vue3 + Pinia 重构状态管理，首屏加载时间降低 32%。',
  '面试建议分三层准备：1) 八股与原理，2) 项目深挖，3) 场景题表达。你想先练哪一块？',
  '当前校招前端薪资和城市相关性很强，一线城市通常区间更高。你如果告诉我目标城市，我可以给更具体建议。',
  '你可以把目标 JD 发我，我会按 JD 关键词帮你做简历匹配和改写建议。',
];

const sendMessage = () => {
  const text = inputText.value.trim();
  if (!text) return;

  messages.value.push({ role: 'user', content: text });
  inputText.value = '';
  scrollToBottom();

  setTimeout(() => {
    messages.value.push({ role: 'ai', content: '', isTyping: true });
    scrollToBottom();

    setTimeout(() => {
      const last = messages.value[messages.value.length - 1];
      last.isTyping = false;
      last.content = mockReplies[Math.floor(Math.random() * mockReplies.length)];
      scrollToBottom();
    }, 1500);
  }, 500);
};

onMounted(() => {
  const systemInfo = uni.getSystemInfoSync();
  const menuButton = uni.getMenuButtonBoundingClientRect?.();

  if (menuButton && menuButton.top && menuButton.height) {
    topSafeHeight.value = menuButton.top + menuButton.height + 8;
  } else {
    const statusBar = systemInfo.statusBarHeight || 20;
    topSafeHeight.value = statusBar + 44;
  }

  safeBottom.value = systemInfo.safeAreaInsets?.bottom || 20;
});
</script>

<style scoped>
.chat-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8fafc;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue', sans-serif;
}

.chat-nav {
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-bottom: 0.5px solid rgba(60, 60, 67, 0.12);
  flex-shrink: 0;
  z-index: 10;
}

.nav-spacer {
  width: 100%;
}

.nav-row {
  display: flex;
  align-items: center;
  padding: 10px 20px 12px;
}

.nav-bot-avatar {
  width: 38px;
  height: 38px;
  border-radius: 19px;
  background: linear-gradient(135deg, #2563eb, #60a5fa);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
}

.nav-bot-emoji {
  font-size: 20px;
}

.nav-name {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.nav-status-row {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 2px;
}

.online-dot {
  width: 6px;
  height: 6px;
  border-radius: 3px;
  background: #22c55e;
}

.nav-status {
  font-size: 12px;
  color: #64748b;
}

.chat-list {
  flex: 1;
  padding: 16px 16px 0;
  box-sizing: border-box;
}

.scroll-pad {
  height: 100px;
}

.welcome-card {
  background: #ffffff;
  border-radius: 20px;
  padding: 24px 20px;
  margin-bottom: 20px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 5px 16px rgba(15, 23, 42, 0.08);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.welcome-icon {
  font-size: 36px;
  margin-bottom: 12px;
}

.welcome-title {
  font-size: 17px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 8px;
}

.welcome-desc {
  font-size: 13px;
  color: #64748b;
  line-height: 1.55;
  margin-bottom: 18px;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.quick-chip {
  background: #eff6ff;
  border-radius: 20px;
  padding: 8px 16px;
}

.chip-text {
  font-size: 13px;
  font-weight: 500;
  color: #2563eb;
}

.time-divider {
  display: flex;
  justify-content: center;
  margin: 16px 0;
}

.time-text {
  font-size: 11px;
  color: #8e8e93;
  background: rgba(142, 142, 147, 0.12);
  padding: 3px 12px;
  border-radius: 10px;
}

.msg-row {
  display: flex;
  margin-bottom: 20px;
  align-items: flex-start;
}

.row-user {
  flex-direction: row-reverse;
}

.bot-avatar {
  width: 34px;
  height: 34px;
  border-radius: 17px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
  margin-top: 2px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.bot-emoji {
  font-size: 18px;
}

.bubble-area {
  max-width: 72%;
}

.bubble {
  padding: 12px 16px;
  font-size: 15px;
  line-height: 1.55;
  word-break: break-all;
}

.bubble-ai {
  background: #ffffff;
  color: #1c1c1e;
  border-radius: 4px 20px 20px 20px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 3px 10px rgba(15, 23, 42, 0.06);
}

.bubble-user {
  background: #2563eb;
  color: #ffffff;
  border-radius: 20px 4px 20px 20px;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.typing-dots {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 4px 6px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 3px;
  background: #8e8e93;
  animation: bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) {
  animation-delay: -0.32s;
}

.dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes bounce {
  0%,
  80%,
  100% {
    transform: scale(0);
  }

  40% {
    transform: scale(1);
  }
}

.input-bar {
  background: rgba(245, 245, 247, 0.92);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-top: 0.5px solid rgba(60, 60, 67, 0.1);
  padding: 10px 16px;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 10;
}

.input-row {
  display: flex;
  align-items: center;
  background: #ffffff;
  border-radius: 24px;
  padding: 4px 4px 4px 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 3px 10px rgba(15, 23, 42, 0.06);
}

.chat-input {
  flex: 1;
  height: 40px;
  font-size: 15px;
  color: #000000;
}

.input-ph {
  color: #8e8e93;
  font-size: 15px;
}

.send-btn {
  width: 36px;
  height: 36px;
  border-radius: 18px;
  background: #e5e5ea;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 8px;
  transition: background 0.2s;
  flex-shrink: 0;
}

.send-active {
  background: #2563eb;
}

.send-arrow {
  color: #ffffff;
  font-size: 18px;
  font-weight: 700;
}
</style>
