<template>
  <view class="chat-page" :class="{ 'is-dark': darkPref }">
    <!-- Custom nav bar -->
    <view class="chat-nav">
      <view class="nav-spacer" :style="{ height: topSafeHeight + 'px' }"></view>
      <view class="nav-row">
        <view class="nav-bot-avatar" :style="{ background: currentPersona.gradient }">
          <text class="nav-bot-emoji">{{ currentPersona.emoji }}</text>
        </view>
        <view class="nav-meta">
          <text class="nav-name">{{ currentPersona.name }}</text>
          <view class="nav-status-row">
            <view class="online-dot"></view>
            <text class="nav-status">{{ currentPersona.tagline }}</text>
          </view>
        </view>
      </view>
      <!-- F15: Persona switcher -->
      <view class="persona-bar">
        <view
          v-for="p in PERSONAS"
          :key="p.key"
          class="persona-chip"
          :class="{ 'persona-active': persona === p.key }"
          @click="switchPersona(p.key)"
        >
          <text class="persona-emoji">{{ p.emoji }}</text>
          <text class="persona-label">{{ p.label }}</text>
        </view>
      </view>
    </view>

    <!-- Chat messages area -->
    <scroll-view
      class="chat-list"
      scroll-y
      :scroll-top="scrollTop"
      scroll-with-animation
    >
      <!-- Welcome card -->
      <view class="welcome-card">
        <view class="welcome-icon">{{ currentPersona.emoji }}</view>
        <text class="welcome-title">{{ currentPersona.name }}</text>
        <text class="welcome-desc">{{ currentPersona.intro }}</text>
        <view class="quick-actions">
          <view
            v-for="chip in currentPersona.chips"
            :key="chip"
            class="quick-chip"
            @click="sendQuick(chip)"
          >
            <text class="chip-text">{{ chip }}</text>
          </view>
        </view>
      </view>

      <!-- Timestamp -->
      <view class="time-divider">
        <text class="time-text">{{ chatTimeLabel }}</text>
      </view>

      <!-- Messages -->
      <view
        class="msg-row"
        v-for="(msg, index) in messages"
        :key="index"
        :class="msg.role === 'user' ? 'row-user' : 'row-ai'"
      >
        <!-- AI avatar (top-left of bubble) -->
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

    <!-- Input area -->
    <view class="input-bar">
      <view class="input-row">
        <input
          class="chat-input"
          v-model="inputText"
          placeholder="Ask me anything about your career..."
          placeholder-class="input-ph"
          @confirm="sendMessage"
          confirm-type="send"
        />
        <view
          class="send-btn"
          :class="{ 'send-active': inputText.trim().length > 0 }"
          @click="sendMessage"
        >
          <text class="send-label">Send</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted } from 'vue';
import { getTopSafeHeight } from '@/utils/safeArea';
import request from '@/utils/request';

interface ChatMessage {
  role: 'user' | 'ai';
  content: string;
  isTyping?: boolean;
}

type PersonaKey = 'MENTOR' | 'CHALLENGER' | 'INTERVIEWER';

// F15: persona definitions
const PERSONAS: { key: PersonaKey; emoji: string; label: string; name: string; tagline: string; intro: string; gradient: string; chips: string[] }[] = [
  {
    key: 'MENTOR',
    emoji: '🤝',
    label: '小职',
    name: '小职 · Career Mentor',
    tagline: 'Online · Warm & encouraging',
    intro: 'I\'m your warm career mentor! I can help with resume tips, interview prep, and career planning. What\'s on your mind?',
    gradient: 'linear-gradient(135deg, #2563eb, #60a5fa)',
    chips: ['Help me optimize my resume', 'Interview techniques?', 'Salary for frontend devs?'],
  },
  {
    key: 'CHALLENGER',
    emoji: '💪',
    label: '小严',
    name: '小严 · Tough Coach',
    tagline: 'Online · Strict & direct',
    intro: 'No sugarcoating here. Tell me your plan and I\'ll tell you exactly what\'s wrong with it.',
    gradient: 'linear-gradient(135deg, #dc2626, #f97316)',
    chips: ['Review my career plan', 'Challenge my resume', 'Why am I not getting interviews?'],
  },
  {
    key: 'INTERVIEWER',
    emoji: '🎙️',
    label: '小面',
    name: '小面 · Mock Interviewer',
    tagline: 'Online · Realistic interview practice',
    intro: 'Let\'s practice! Tell me what role and interview type you want to prepare for (behavioral / technical / HR).',
    gradient: 'linear-gradient(135deg, #7c3aed, #a78bfa)',
    chips: ['Practice behavioral interview', 'Technical interview for frontend', 'HR round for product manager'],
  },
];

const persona = ref<PersonaKey>('MENTOR');
const currentPersona = computed(() => PERSONAS.find((p) => p.key === persona.value)!);

const messages = ref<ChatMessage[]>([]);
const apiHistory = ref<{ role: string; content: string }[]>([]);

const inputText = ref('');
const scrollTop = ref(0);
const topSafeHeight = ref(88);
const darkPref = ref(false);
const isSending = ref(false);
const chatTimeLabel = ref('');

const switchPersona = (key: PersonaKey) => {
  if (persona.value === key) return;
  persona.value = key;
  // Clear history for fresh start with new persona
  apiHistory.value = [];
  messages.value = [
    { role: 'ai', content: currentPersona.value.intro },
  ];
};

const scrollToBottom = () => {
  nextTick(() => {
    scrollTop.value = scrollTop.value === 99998 ? 99999 : 99998;
  });
};

const sendQuick = (text: string) => {
  inputText.value = text;
  sendMessage();
};

const sendMessage = async () => {
  const text = inputText.value.trim();
  if (!text || isSending.value) return;

  messages.value.push({ role: 'user', content: text });
  inputText.value = '';
  isSending.value = true;
  scrollToBottom();

  const typingIdx = messages.value.length;
  messages.value.push({ role: 'ai', content: '', isTyping: true });
  scrollToBottom();

  try {
    const res = await request<{ reply: string }>({
      url: '/api/chat/send',
      method: 'POST',
      data: {
        message: text,
        history: apiHistory.value,
        persona: persona.value,
      },
    });
    const reply = (res as any)?.reply ?? res ?? '';
    messages.value[typingIdx] = { role: 'ai', content: reply || 'Sorry, no response. Please try again.' };
    apiHistory.value.push(
      { role: 'user', content: text },
      { role: 'assistant', content: String(reply) },
    );
  } catch (err: any) {
    const errMsg = err?.message || String(err) || 'Unknown error';
    messages.value[typingIdx] = {
      role: 'ai',
      content: `⚠️ Request failed: ${errMsg}\n\nPlease check your network and try again.`,
    };
  } finally {
    isSending.value = false;
    scrollToBottom();
  }
};

onMounted(() => {
  darkPref.value = uni.getStorageSync('app_pref_dark') === '1';
  topSafeHeight.value = getTopSafeHeight();
  const now = new Date();
  chatTimeLabel.value = `Today ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
  // Initialise with persona greeting
  messages.value = [{ role: 'ai', content: currentPersona.value.intro }];
});
</script>

<style scoped>
.chat-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--page-ios-gray);
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
}

/* ---- Nav bar ---- */
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

.nav-meta {
  display: flex;
  flex-direction: column;
}

.nav-name {
  font-size: var(--font-section);
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.3px;
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
  color: var(--text-secondary);
  line-height: 1.35;
}

/* ---- Persona bar ---- */
.persona-bar {
  display: flex;
  padding: 8px 16px 12px;
  gap: 8px;
}
.persona-chip {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.6);
  border: 1.5px solid rgba(60, 60, 67, 0.12);
  border-radius: 20px;
  transition: all 0.2s;
}
.persona-chip:active { opacity: 0.75; }
.persona-active {
  background: #eff6ff;
  border-color: #2563eb;
}
.persona-emoji { font-size: 14px; }
.persona-label { font-size: 13px; font-weight: 600; color: #374151; }
.persona-active .persona-label { color: #2563eb; }

/* ---- Chat list ---- */
.chat-list {
  flex: 1;
  padding: 16px 16px 0;
  box-sizing: border-box;
}

.scroll-pad {
  height: 140px;
}

/* ---- Welcome card ---- */
.welcome-card {
  background: #ffffff;
  border-radius: 20px;
  padding: 24px 20px;
  margin-bottom: 20px;
  border: 1px solid var(--border-strong);
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
  transition: all 0.15s;
}

.quick-chip:active {
  background: #dbeafe;
  transform: scale(0.96);
}

.chip-text {
  font-size: 13px;
  font-weight: 500;
  color: #2563eb;
}

/* ---- Time divider ---- */
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

/* ---- Messages ---- */
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
  overflow-wrap: break-word;
  word-break: normal;
}

.bubble-ai {
  background: #ffffff;
  color: #1c1c1e;
  border-radius: 4px 20px 20px 20px;
  border: 1px solid var(--border-color);
  box-shadow: 0 3px 10px rgba(15, 23, 42, 0.06);
}

.bubble-user {
  background: #2563eb;
  color: #ffffff;
  border-radius: 20px 4px 20px 20px;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.bubble-text {
  display: block;
}

/* ---- Typing dots ---- */
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

.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* ---- Input bar ---- */
.input-bar {
  background: rgba(245, 245, 247, 0.92);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-top: 0.5px solid rgba(60, 60, 67, 0.1);
  /* In mp-weixin the system tab bar is rendered OUTSIDE the page viewport,
     so 100vh / bottom:0 already sits flush with the tab bar's top edge.
     Adding `--tab-bar-height` here was double-counting and left a fat gap. */
  padding: 10px 16px calc(10px + env(safe-area-inset-bottom, 0px));
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 10;
  box-sizing: border-box;
}

.input-row {
  display: flex;
  align-items: center;
  background: #ffffff;
  border-radius: 24px;
  padding: 4px 4px 4px 16px;
  border: 1px solid var(--border-strong);
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
  min-width: 64px;
  height: 36px;
  padding: 0 14px;
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

.send-label {
  color: #94a3b8;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.02em;
}
.send-active .send-label {
  color: #ffffff;
}

/* ---- Dark mode ---- */
.is-dark {
  background: #0f172a;
}

.is-dark .chat-nav {
  background: rgba(15, 23, 42, 0.88);
  border-color: #334155;
}

.is-dark .nav-name,
.is-dark .welcome-title {
  color: #f8fafc;
}

.is-dark .welcome-desc,
.is-dark .nav-status {
  color: #94a3b8;
}

.is-dark .welcome-card,
.is-dark .bubble-ai,
.is-dark .bot-avatar {
  background: #1e293b;
  box-shadow: none;
}

.is-dark .bubble-ai {
  color: #f8fafc;
}

.is-dark .input-bar {
  background: rgba(15, 23, 42, 0.92);
  border-color: #334155;
}

.is-dark .input-row {
  background: #1e293b;
  border-color: #334155;
}

.is-dark .chat-input {
  color: #f8fafc;
}

/* ================================================================
 *  MP-WEIXIN parity overrides (scoped to assistant/chat page)
 * ================================================================ */
/* #ifdef MP-WEIXIN */

/* backdrop-filter unsupported — use solid background for input bar */
.input-bar {
  backdrop-filter: none;
  -webkit-backdrop-filter: none;
  background: #f5f5f7;
}

/* Stronger border on the pill-shaped input row */
.input-row {
  border: 1.5px solid #b8c8d8;
  box-shadow: 0 2px 8px rgba(0,0,0,0.10);
}

/* Chat nav: solid white instead of frosted */
.chat-nav {
  backdrop-filter: none;
  -webkit-backdrop-filter: none;
  background: #ffffff;
  border-bottom: 1px solid #d0dae4;
}

/* Welcome card: content card gets depth */
.welcome-card {
  overflow: visible;
  border: 1px solid #b8c8d8;
  box-shadow: 0 4px 14px rgba(0,0,0,0.14);
}

/* #endif */
</style>
