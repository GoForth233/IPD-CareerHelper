<template>
  <view class="container">
    <!-- Header -->
    <view class="header">
      <text class="title">Sprint 2 Demo</text>
      <text class="subtitle">MySQL + MongoDB Dual Write</text>
    </view>

    <!-- Area A: Create Resume -->
    <view class="card">
      <view class="card-header">
        <text class="card-title">1. Write Data (Create)</text>
      </view>
      
      <!-- New: Upload Resume -->
      <view class="form-item">
        <text class="label">Upload Resume (PDF/Doc):</text>
        <view class="upload-area">
          <button class="btn-choose" @click="handleChoose">
            {{ selectedFileName || '1. Choose File' }}
          </button>
          
          <button 
            v-if="selectedFilePath && !form.fileUrl" 
            class="btn-upload-cloud" 
            @click="handleUpload"
            :loading="uploading"
          >
            2. Upload Resume
          </button>
        </view>
        <text v-if="form.fileUrl" class="file-success">✅ Resume Uploaded</text>
      </view>

      <!-- New: Upload Avatar -->
      <view class="form-item">
        <text class="label">Upload Avatar (Image):</text>
        <button class="btn-choose" @click="handleChooseImage">Choose Image</button>
        <image 
          v-if="avatarUrl" 
          :src="avatarUrl" 
          mode="aspectFill" 
          class="avatar-preview"
        />
      </view>

      <view class="form-item">
        <text class="label">Title (MySQL):</text>
        <input class="input" v-model="form.title" placeholder="e.g., Java Engineer" />
      </view>
      <!-- ... -->
      <view class="form-item">
        <text class="label">Target Job (MySQL):</text>
        <input class="input" v-model="form.targetJob" placeholder="e.g., Backend Dev" />
      </view>
      <view class="form-item">
        <text class="label">Skills (MongoDB):</text>
        <input class="input" v-model="skillInput" placeholder="Comma separated (e.g., Java, Vue)" />
      </view>
      <button class="btn-primary" @click="handleSubmit" :loading="loading">Submit to DB</button>
    </view>

    <!-- Area C: AI Chat -->
    <view class="card chat-card">
      <view class="card-header">
        <text class="card-title">3. AI Chat (Career Mentor)</text>
      </view>
      
      <scroll-view scroll-y class="chat-history" :scroll-top="99999">
        <view 
          v-for="(msg, index) in chatHistory" 
          :key="index"
          :class="['message', msg.role === 'user' ? 'msg-user' : 'msg-ai']"
        >
          <text class="msg-content">{{ msg.content }}</text>
        </view>
        <view v-if="chatLoading" class="message msg-ai">
          <text class="msg-content">Thinking...</text>
        </view>
      </scroll-view>

      <view class="chat-input-area">
        <input 
          class="chat-input" 
          v-model="chatInput" 
          placeholder="Ask me anything..." 
          @confirm="handleSendChat"
        />
        <button class="btn-send" @click="handleSendChat" :disabled="chatLoading">Send</button>
      </view>
    </view>

    <!-- Area B: Read Result -->
    <view class="card" v-if="createdResumeId">
      <view class="card-header">
        <text class="card-title">2. Read Data (Retrieve)</text>
      </view>
      <view class="result-info">
        <text>Created ID: {{ createdResumeId }}</text>
      </view>
      <button class="btn-secondary" @click="handleQuery">Query from DB (ID: {{ createdResumeId }})</button>
      
      <!-- API Response Display -->
      <view class="json-box" v-if="queryResult">
        <text class="json-content">{{ JSON.stringify(queryResult, null, 2) }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { createResumeApi, getResumeApi, type Resume } from '@/api/resume';
import { uploadFileApi } from '@/api/file';
import { chatAiApi } from '@/api/ai';

const loading = ref(false);
const uploading = ref(false);
const aiLoading = ref(false);
const aiResult = ref('');
const createdResumeId = ref<number | null>(null);
const queryResult = ref<Resume | null>(null);
const selectedFilePath = ref('');
const selectedFileName = ref('');
const avatarUrl = ref('');

// Form Data
const form = ref({
  title: 'My Resume v1',
  targetJob: 'Java Developer',
  fileUrl: '' 
});
const skillInput = ref('Java, Spring Boot, MySQL');

// ============ AI Chat State ============
const chatHistory = ref<Array<{ role: string; content: string }>>([]);
const chatLoading = ref(false);
const chatInput = ref('');

// Choose Resume Handler
const handleChoose = () => {
  // ... existing code ...
  // (Keep the logic but change comments if needed)
  form.value.fileUrl = '';
  selectedFilePath.value = '';
  selectedFileName.value = '';

  // @ts-ignore
  if (typeof uni.chooseFile === 'function') {
    // H5
    // @ts-ignore
    uni.chooseFile({
      count: 1,
      type: 'all', 
      extension: ['.pdf', '.doc', '.docx'],
      success: (res: any) => {
          selectedFilePath.value = res.tempFilePaths[0];
          selectedFileName.value = res.tempFiles[0].name;
      }
    });
  } else {
    // Mini Program
    uni.chooseMessageFile({
      count: 1,
      type: 'file',
      extension: ['.pdf', '.doc', '.docx'],
      success: (res: any) => {
        selectedFilePath.value = res.tempFiles[0].path;
        selectedFileName.value = res.tempFiles[0].name;
      }
    });
  }
};

// Choose Image Handler
const handleChooseImage = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: async (res) => {
      const filePath = res.tempFilePaths[0];
      uni.showLoading({ title: 'Uploading Image...' });
      try {
        // Upload to 'avatars' folder
        const url = await uploadFileApi(filePath, 'avatars');
        avatarUrl.value = url; // Display image
        uni.showToast({ title: 'Image Success', icon: 'success' });
      } catch (e: any) {
        uni.showToast({ title: 'Failed: ' + e.message, icon: 'none' });
      } finally {
        uni.hideLoading();
      }
    }
  });
};

// Real Upload Handler (Resume)
const handleUpload = async () => {
  if (!selectedFilePath.value) return;
  
  uploading.value = true;
  try {
    const url = await uploadFileApi(selectedFilePath.value, 'resumes');
    form.value.fileUrl = url;
    uni.showToast({ title: 'Resume Success', icon: 'success' });
  } catch (e: any) {
    uni.showToast({ title: 'Failed: ' + e.message, icon: 'none' });
  } finally {
    uploading.value = false;
  }
};

// Submit Handler
const handleSubmit = async () => {
  if (!form.value.title || !form.value.targetJob) {
    uni.showToast({ title: 'Please fill fields', icon: 'none' });
    return;
  }

  // Validate file upload
  if (!form.value.fileUrl) {
    uni.showToast({ 
      title: 'Please upload resume file first!', 
      icon: 'none',
      duration: 2000 
    });
    return;
  }

  loading.value = true;
  try {
    // Construct Payload
    const payload: Resume = {
      userId: 1, // Mock User ID
      title: form.value.title,
      targetJob: form.value.targetJob,
      fileUrl: form.value.fileUrl, // Use OSS URL (validated above)
      detail: {
        skills: skillInput.value.split(',').map(s => s.trim()),
        education: [{ school: 'Demo University', degree: 'Bachelor' }], // Mock Data
        rawContent: 'This is a raw content stored in MongoDB...'
      }
    };

    const res = await createResumeApi(payload);
    console.log('Create Success:', res);
    
    createdResumeId.value = res.resumeId || null;
    uni.showToast({ title: 'Created Success!', icon: 'success' });
    
    // Reset Query Result
    queryResult.value = null;
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// Query Handler
const handleQuery = async () => {
  if (!createdResumeId.value) return;
  
  uni.showLoading({ title: 'Loading...' });
  try {
    const res = await getResumeApi(createdResumeId.value);
    console.log('Query Success:', res);
    queryResult.value = res;
    uni.showToast({ title: 'Query Success', icon: 'none' });
  } catch (error) {
    console.error(error);
  } finally {
    uni.hideLoading();
  }
};

// ============ AI Chat Handler ============
const handleSendChat = async () => {
  const message = chatInput.value.trim();
  if (!message || chatLoading.value) return;

  // Add user message to history
  chatHistory.value.push({ role: 'user', content: message });
  chatInput.value = '';
  chatLoading.value = true;

  try {
    // Call AI API
    const response = await chatAiApi(chatHistory.value as any);
    
    // Add AI response to history
    chatHistory.value.push({ role: 'assistant', content: response });
  } catch (error: any) {
    console.error('AI Chat Error:', error);
    uni.showToast({ 
      title: error.message || 'Chat failed', 
      icon: 'none' 
    });
  } finally {
    chatLoading.value = false;
  }
};
</script>

<style>
.container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.header {
  margin-bottom: 20px;
  text-align: center;
}

.avatar-preview {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  margin-top: 10px;
  border: 2px solid #eee;
  display: block;
}

.title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  display: block;
}

.subtitle {
  font-size: 14px;
  color: #666;
  margin-top: 5px;
  display: block;
}

.card {
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.card-header {
  margin-bottom: 15px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.form-item {
  margin-bottom: 15px;
}

.label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
  display: block;
}

.input {
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 10px;
  font-size: 14px;
  background-color: #fff;
  width: 100%;
  box-sizing: border-box;
  /* Ensure input is clickable */
  position: relative; 
  z-index: 1;
  height: 44px; /* Explicit height */
}


.btn-choose {
  background-color: #f0ad4e;
  color: #fff;
  border-radius: 6px;
  font-size: 14px;
  margin-bottom: 8px;
}

.btn-upload-cloud {
  background-color: #5bc0de;
  color: #fff;
  border-radius: 6px;
  font-size: 14px;
  margin-bottom: 8px;
}

.file-success {
  font-size: 12px;
  color: #28a745;
  word-break: break-all;
  display: block;
  margin-bottom: 10px;
}

.btn-primary {
  background-color: #007aff;
  color: #fff;
  border-radius: 6px;
  margin-top: 10px;
}

.chat-card {
  height: 500px;
  display: flex;
  flex-direction: column;
}

.chat-history {
  flex: 1;
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 10px;
  margin-bottom: 10px;
  overflow-y: auto;
}

.message {
  display: flex;
  margin-bottom: 10px;
}

.msg-user {
  justify-content: flex-end;
}

.msg-ai {
  justify-content: flex-start;
}

.msg-content {
  max-width: 80%;
  padding: 8px 12px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
}

.msg-user .msg-content {
  background-color: #007aff;
  color: #fff;
  border-top-right-radius: 2px;
}

.msg-ai .msg-content {
  background-color: #e5e5ea;
  color: #333;
  border-top-left-radius: 2px;
}

.chat-input-area {
  display: flex;
  gap: 10px;
}

.chat-input {
  flex: 1;
  border: 1px solid #ddd;
  border-radius: 20px;
  padding: 8px 12px;
  font-size: 14px;
  background-color: #fff;
}

.btn-send {
  background-color: #007aff;
  color: #fff;
  border-radius: 20px;
  font-size: 13px;
  padding: 0 20px;
  line-height: 36px;
}

.btn-secondary {
  background-color: #34c759;
  color: #fff;
  border-radius: 6px;
  margin-top: 10px;
}

.result-info {
  margin-bottom: 10px;
  font-size: 14px;
  color: #333;
}

.json-box {
  background-color: #2d2d2d;
  color: #fff;
  padding: 15px;
  border-radius: 6px;
  margin-top: 15px;
  overflow-x: auto;
  min-height: 100px;
}

.json-content {
  font-family: monospace;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
