<template>
  <view class="container">
    <view class="interview-info" v-if="interview">
      <text class="position">{{ interview.positionName }}</text>
      <text class="difficulty">难度: {{ interview.difficulty }}</text>
    </view>

    <scroll-view scroll-y class="chat-area" :scroll-top="99999" :scroll-with-animation="true">
      <view v-for="(msg, index) in messages" :key="index" :class="['message', msg.role.toLowerCase()]">
        <view class="msg-content">
          <text>{{ msg.content }}</text>
        </view>
      </view>
      <view v-if="aiTyping" class="message ai">
        <view class="msg-content typing">
          <text>AI正在思考...</text>
        </view>
      </view>
    </scroll-view>

    <view class="input-area">
      <input 
        class="chat-input" 
        v-model="inputText" 
        placeholder="输入你的回答..." 
        @confirm="sendMessage"
      />
      <button class="btn-send" @click="sendMessage" :disabled="aiTyping || !inputText.trim()">
        发送
      </button>
    </view>

    <view class="action-bar">
      <button class="btn-end" @click="endInterview">结束面试</button>
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

onMounted(async () => {
  const pages = getCurrentPages();
  const currentPage = pages[pages.length - 1] as any;
  interviewId.value = parseInt(currentPage.options?.interviewId || '0');

  if (interviewId.value) {
    try {
      interview.value = await getInterviewByIdApi(interviewId.value);
      messages.value = await getInterviewMessagesApi(interviewId.value);
      
      // 如果没有消息，发送欢迎语
      if (messages.value.length === 0) {
        aiTyping.value = true;
        const response = await sendInterviewMessageApi(interviewId.value, 'Hello, I am ready for the interview.');
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

  // 添加用户消息到界面
  messages.value.push({
    interviewId: interviewId.value,
    role: 'USER',
    content: text
  });
  inputText.value = '';
  aiTyping.value = true;

  try {
    const response = await sendInterviewMessageApi(interviewId.value, text);
    
    // 添加 AI 响应
    messages.value.push({
      interviewId: interviewId.value,
      role: 'AI',
      content: response.aiMessage
    });
  } catch (error) {
    console.error('Failed to send message:', error);
    uni.showToast({ title: '发送失败', icon: 'none' });
  } finally {
    aiTyping.value = false;
  }
};

const endInterview = () => {
  uni.showModal({
    title: '结束面试',
    content: '确定要结束面试吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          // 简单评分逻辑：根据消息数量
          const score = Math.min(messages.value.filter(m => m.role === 'USER').length * 10, 100);
          await endInterviewApi(interviewId.value, score);
          
          uni.showToast({ title: `面试结束，得分: ${score}`, icon: 'success' });
          setTimeout(() => {
            uni.navigateBack();
          }, 1500);
        } catch (error) {
          console.error('Failed to end interview:', error);
        }
      }
    }
  });
};
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.interview-info {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.position {
  font-size: 16px;
  font-weight: bold;
  color: #fff;
}

.difficulty {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.chat-area {
  flex: 1;
  padding: 15px;
  max-height: calc(100vh - 200px);
}

.message {
  margin-bottom: 15px;
  display: flex;
}

.message.user {
  justify-content: flex-end;
}

.message.ai {
  justify-content: flex-start;
}

.msg-content {
  max-width: 70%;
  padding: 12px 15px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
}

.message.user .msg-content {
  background-color: #667eea;
  color: #fff;
  border-top-right-radius: 4px;
}

.message.ai .msg-content {
  background-color: #fff;
  color: #333;
  border-top-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.msg-content.typing {
  font-style: italic;
  color: #999;
}

.input-area {
  display: flex;
  padding: 10px 15px;
  background-color: #fff;
  border-top: 1px solid #eee;
  gap: 10px;
}

.chat-input {
  flex: 1;
  border: 1px solid #ddd;
  border-radius: 20px;
  padding: 8px 15px;
  font-size: 14px;
  background-color: #f9f9f9;
}

.btn-send {
  background-color: #667eea;
  color: #fff;
  border-radius: 20px;
  padding: 0 20px;
  font-size: 14px;
}

.action-bar {
  padding: 10px 15px;
  background-color: #fff;
  border-top: 1px solid #eee;
}

.btn-end {
  background-color: #ff6b6b;
  color: #fff;
  border-radius: 8px;
  width: 100%;
}
</style>

