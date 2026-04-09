<template>
  <view class="chat-container" :class="{ 'is-dark': darkPref }">
    <view class="interview-info" v-if="interview">
      <text class="position">{{ interview.positionName }}</text>
      <text class="difficulty">{{ interview.difficulty }}</text>
    </view>

    <scroll-view scroll-y class="chat-area" :scroll-top="99999" :scroll-with-animation="true">
      <view v-for="(msg, index) in messages" :key="index" :class="['message', msg.role.toLowerCase()]">
        <view class="msg-content">
          <text>{{ msg.content }}</text>
        </view>
      </view>
      <view v-if="aiTyping" class="message ai">
        <view class="msg-content typing">
          <view class="typing-dots">
            <view class="dot"></view>
            <view class="dot"></view>
            <view class="dot"></view>
          </view>
        </view>
      </view>
    </scroll-view>

    <view class="input-area">
      <input 
        class="chat-input" 
        v-model="inputText" 
        placeholder="Type your answer..." 
        placeholder-class="ph"
        @confirm="sendMessage"
      />
      <view 
        class="send-btn" 
        :class="{ 'send-active': inputText.trim() && !aiTyping }"
        @click="sendMessage"
      >
        <text class="send-arrow">↑</text>
      </view>
    </view>

    <view class="action-bar">
      <button class="btn-end" @click="endInterview">End Interview</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getInterviewByIdApi, getInterviewMessagesApi, sendInterviewMessageApi, endInterviewApi } from '@/api/interview';
import type { Interview, InterviewMessage } from '@/api/interview';

const interviewId = ref<number>(0);
const interview = ref<Interview | null>(null);
const messages = ref<InterviewMessage[]>([]);
const inputText = ref('');
const aiTyping = ref(false);
const darkPref = ref(uni.getStorageSync('app_pref_dark') === '1');

onMounted(async () => {
  const pages = getCurrentPages();
  const currentPage = pages[pages.length - 1] as any;
  interviewId.value = parseInt(currentPage.options?.interviewId || '0');

  if (interviewId.value) {
    try {
      interview.value = await getInterviewByIdApi(interviewId.value);
      messages.value = await getInterviewMessagesApi(interviewId.value);
      
      if (messages.value.length === 0) {
        aiTyping.value = true;
        await sendInterviewMessageApi(interviewId.value, 'Hello, I am ready for the interview.');
        messages.value = await getInterviewMessagesApi(interviewId.value);
        aiTyping.value = false;
      }
    } catch (error) {
      console.error('Failed to load interview:', error);
    }
  }
});

const sendMessage = async () => {
  const text = inputText.value.trim();
  if (!text || aiTyping.value) return;

  messages.value.push({ interviewId: interviewId.value, role: 'USER', content: text });
  inputText.value = '';
  aiTyping.value = true;

  try {
    const response = await sendInterviewMessageApi(interviewId.value, text);
    messages.value.push({ interviewId: interviewId.value, role: 'AI', content: response.aiMessage });
  } catch (error) {
    console.error('Failed to send message:', error);
    uni.showToast({ title: 'Send failed', icon: 'none' });
  } finally {
    aiTyping.value = false;
  }
};

const endInterview = () => {
  uni.showModal({
    title: 'End Interview',
    content: 'Are you sure you want to end the interview?',
    confirmColor: '#ef4444',
    success: async (res) => {
      if (res.confirm) {
        try {
          const score = Math.min(messages.value.filter(m => m.role === 'USER').length * 10, 100);
          await endInterviewApi(interviewId.value, score);
          uni.showToast({ title: `Interview ended · Score: ${score}`, icon: 'success' });
          setTimeout(() => { uni.navigateBack(); }, 1500);
        } catch (error) {
          console.error('Failed to end interview:', error);
        }
      }
    }
  });
};
</script>

<style scoped>
.chat-container {
  height: 100vh;
  background-color: #f5f5f7;
  display: flex;
  flex-direction: column;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
}

.interview-info {
  background: linear-gradient(135deg, #2563eb, #1e40af);
  padding: 16px 20px;
  display: flex; justify-content: space-between; align-items: center;
  flex-shrink: 0;
}

.position { font-size: 16px; font-weight: 700; color: #fff; }

.difficulty { font-size: 14px; color: rgba(255, 255, 255, 0.8); }

.chat-area {
  flex: 1; padding: 16px;
  max-height: calc(100vh - 200px);
}

.message { margin-bottom: 16px; display: flex; }

.message.user { justify-content: flex-end; }

.message.ai { justify-content: flex-start; }

.msg-content {
  max-width: 72%; padding: 12px 16px;
  border-radius: 16px; font-size: 15px; line-height: 1.55;
}

.message.user .msg-content {
  background: #2563eb; color: #fff;
  border-radius: 20px 4px 20px 20px;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.25);
}

.message.ai .msg-content {
  background: #fff; color: #1e293b;
  border-radius: 4px 20px 20px 20px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
}

/* Typing dots */
.typing-dots { display: flex; gap: 5px; padding: 4px 6px; align-items: center; }

.dot {
  width: 6px; height: 6px; border-radius: 3px;
  background: #94a3b8; animation: bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.input-area {
  display: flex; padding: 10px 16px; gap: 10px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px);
  border-top: 0.5px solid rgba(60, 60, 67, 0.1);
  align-items: center;
}

.chat-input {
  flex: 1; border: 1.5px solid #e2e8f0; border-radius: 22px;
  padding: 10px 18px; font-size: 15px; background: #f8fafc;
  color: #0f172a; height: 44px;
}

.ph { color: #94a3b8; }

.send-btn {
  width: 38px; height: 38px; border-radius: 19px;
  background: #e5e5ea; display: flex;
  align-items: center; justify-content: center;
  flex-shrink: 0; transition: background 0.2s;
}

.send-active { background: #2563eb; }

.send-arrow { color: #ffffff; font-size: 18px; font-weight: 700; }

.action-bar {
  padding: 8px 16px calc(12px + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.92);
}

.btn-end {
  background: #fef2f2; color: #ef4444;
  border-radius: 14px; width: 100%; height: 44px; line-height: 44px;
  font-size: 15px; font-weight: 600; border: none;
}

.btn-end:active { background: #fee2e2; }

/* Dark mode */
.is-dark { background-color: #0f172a; }

.is-dark .message.ai .msg-content { background: #1e293b; color: #f8fafc; box-shadow: none; }

.is-dark .input-area,
.is-dark .action-bar { background: rgba(15, 23, 42, 0.92); border-color: #334155; }

.is-dark .chat-input { background: #1e293b; border-color: #334155; color: #f8fafc; }

.is-dark .btn-end { background: #1e293b; }
</style>
