<template>
  <view class="template-container" :class="{ 'is-dark': darkPref }">
    <!-- Header -->
    <view class="header-section">
      <text class="header-title">Complete Your Resume</text>
      <text class="header-desc">Fill in your real information. AI will generate a tailored resume and polish it professionally.</text>
    </view>

    <!-- Section 1: Basic Info -->
    <view class="form-group">
      <text class="group-title">Basic Information</text>
      <view class="form-card">
        <view class="form-item">
          <text class="label">Full Name</text>
          <input class="input" v-model="formData.name" placeholder="Enter your full name" placeholder-class="placeholder-text" />
        </view>
        <view class="form-item">
          <text class="label">Phone</text>
          <input class="input" v-model="formData.phone" type="number" placeholder="Enter your phone number" placeholder-class="placeholder-text" />
        </view>
        <view class="form-item">
          <text class="label">Email</text>
          <input class="input" v-model="formData.email" placeholder="your@email.com" placeholder-class="placeholder-text" />
        </view>
        <view class="form-item">
          <text class="label">Target Role</text>
          <input class="input" v-model="formData.targetRole" placeholder="e.g. Frontend Developer" placeholder-class="placeholder-text" />
        </view>
        <view class="form-item">
          <text class="label">Preferred City</text>
          <input class="input" v-model="formData.city" placeholder="e.g. Beijing, Shanghai" placeholder-class="placeholder-text" />
        </view>
      </view>
    </view>

    <!-- Section 2: Education -->
    <view class="form-group">
      <text class="group-title">Education</text>
      <view class="form-card">
        <view class="form-item">
          <text class="label">University</text>
          <input class="input" v-model="formData.university" placeholder="Enter university name" placeholder-class="placeholder-text" />
        </view>
        <view class="form-item">
          <text class="label">Major</text>
          <input class="input" v-model="formData.major" placeholder="Enter your major" placeholder-class="placeholder-text" />
        </view>
        <view class="form-item">
          <text class="label">Degree</text>
          <picker class="input-picker" mode="selector" :range="degreeOptions" @change="onDegreeChange">
            <view class="picker-value" :class="{ 'has-value': selectedDegree }">
              {{ selectedDegree || 'Select degree' }}
            </view>
          </picker>
        </view>
        <view class="form-item">
          <text class="label">Grad Year</text>
          <picker class="input-picker" mode="date" fields="year" @change="onYearChange">
            <view class="picker-value" :class="{ 'has-value': selectedYear }">
              {{ selectedYear || 'Select year' }}
            </view>
          </picker>
        </view>
      </view>
    </view>

    <!-- Section 3: Skills -->
    <view class="form-group">
      <text class="group-title">Core Skills</text>
      <view class="form-card">
        <view class="form-item">
          <text class="label">Skills</text>
          <input class="input" v-model="formData.skills" placeholder="e.g. Vue3, Node.js, Python" placeholder-class="placeholder-text" />
        </view>
      </view>
    </view>

    <!-- Section 4: Experience -->
    <view class="form-group">
      <text class="group-title">Internship & Project Experience</text>
      <view class="form-card textarea-card">
        <textarea
          class="textarea"
          v-model="formData.experience"
          placeholder="Briefly describe your key project experience or internship. Don't worry about writing style — AI will polish it using the STAR method..."
          placeholder-class="placeholder-text"
          maxlength="800"
          @input="onTextareaInput"
        />
        <view class="textarea-footer">
          <text class="word-count">{{ experienceLength }} / 800</text>
        </view>
      </view>
    </view>

    <!-- Bottom action -->
    <view class="bottom-action">
      <button class="btn-submit" @click="handleGenerate">Generate Resume with AI</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { generateResumeFromTemplateApi } from '@/api/resume';

const degreeOptions = ['Associate', 'Bachelor', 'Master', 'Doctorate'];
const selectedDegree = ref('');
const selectedYear = ref('');
const darkPref = ref(uni.getStorageSync('app_pref_dark') === '1');
const submitting = ref(false);

const formData = ref({
  name: '',
  phone: '',
  email: '',
  targetRole: '',
  city: '',
  university: '',
  major: '',
  skills: '',
  experience: '',
});

const experienceLength = computed(() => formData.value.experience.length);

const onDegreeChange = (e: any) => {
  selectedDegree.value = degreeOptions[e.detail.value];
};

const onYearChange = (e: any) => {
  selectedYear.value = e.detail.value;
};

const onTextareaInput = (e: any) => {
  formData.value.experience = e.detail.value;
};

const handleGenerate = async () => {
  if (!formData.value.name || !formData.value.targetRole) {
    uni.showToast({ title: 'Please fill in name and target role', icon: 'none' });
    return;
  }
  const userId = Number(uni.getStorageSync('userId'));
  if (!userId || isNaN(userId) || userId <= 0) {
    uni.showToast({ title: 'Please log in first', icon: 'none' });
    return;
  }
  submitting.value = true;
  uni.showLoading({ title: 'AI generating...', mask: true });
  try {
    await generateResumeFromTemplateApi({
      userId,
      name: formData.value.name,
      phone: formData.value.phone,
      email: formData.value.email,
      targetRole: formData.value.targetRole,
      city: formData.value.city,
      university: formData.value.university,
      major: formData.value.major,
      degree: selectedDegree.value,
      graduationYear: selectedYear.value,
      skills: formData.value.skills,
      experience: formData.value.experience,
    });
    uni.hideLoading();
    uni.showToast({ title: 'Resume generated!', icon: 'success' });
    setTimeout(() => uni.navigateBack(), 1200);
  } catch (e: any) {
    uni.hideLoading();
    uni.showToast({ title: e?.message || 'Generation failed', icon: 'none' });
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.template-container {
  min-height: 100vh;
  background-color: #f5f5f7;
  padding: 20px 16px 100px 16px;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
  box-sizing: border-box;
}

.header-section { margin-bottom: 24px; padding: 0 8px; }

.header-title {
  font-size: 28px;
  font-weight: 700;
  color: #000000;
  letter-spacing: -0.5px;
  display: block;
  margin-bottom: 8px;
}

.header-desc { font-size: 15px; color: #636366; line-height: 1.5; display: block; }

.form-group { margin-bottom: 28px; }

.group-title {
  font-size: 14px;
  font-weight: 500;
  color: #636366;
  text-transform: uppercase;
  margin-bottom: 8px;
  padding-left: 16px;
  display: block;
  letter-spacing: 0.5px;
}

.form-card { background-color: #ffffff; border-radius: 12px; overflow: hidden; }

.form-item {
  display: flex;
  align-items: center;
  min-height: 52px;
  padding: 0 16px;
  background-color: #ffffff;
  position: relative;
}

.form-item:not(:last-child)::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 16px;
  right: 0;
  height: 0.5px;
  background-color: #e5e5ea;
}

.label { width: 95px; font-size: 16px; color: #000000; flex-shrink: 0; }

.input { flex: 1; height: 52px; font-size: 16px; color: #000000; text-align: right; }

.input-picker {
  flex: 1;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.picker-value { font-size: 16px; color: #c7c7cc; text-align: right; width: 100%; }

.picker-value.has-value { color: #000000; }

.placeholder-text { color: #c7c7cc; font-size: 16px; }

.textarea-card { padding: 16px; }

.textarea { width: 100%; height: 150px; font-size: 16px; color: #000000; line-height: 1.5; }

.textarea-footer { display: flex; justify-content: flex-end; margin-top: 8px; }

.word-count { font-size: 12px; color: #8e8e93; }

.bottom-action {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px 20px 32px 20px;
  background: rgba(245, 245, 247, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 0.5px solid rgba(60, 60, 67, 0.1);
  z-index: 100;
}

.btn-submit {
  background-color: #007aff;
  color: #ffffff;
  font-size: 17px;
  font-weight: 600;
  border-radius: 14px;
  height: 50px;
  line-height: 50px;
  border: none;
  width: 100%;
}

.btn-submit:active { background-color: #0062cc; }

/* Dark mode */
.is-dark { background-color: #0f172a; }

.is-dark .header-title { color: #f8fafc; }

.is-dark .header-desc,
.is-dark .group-title { color: #94a3b8; }

.is-dark .form-card,
.is-dark .form-item { background-color: #1e293b; }

.is-dark .label,
.is-dark .input,
.is-dark .textarea,
.is-dark .picker-value.has-value { color: #f8fafc; }

.is-dark .bottom-action { background: rgba(15, 23, 42, 0.88); border-color: #334155; }
</style>