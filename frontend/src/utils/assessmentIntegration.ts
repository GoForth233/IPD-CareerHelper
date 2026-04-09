export type AssessmentProfile = {
  assessmentType: 'mbti' | 'holland' | 'mixed';
  recommendedRole: string;
  roleKey: 'frontend' | 'backend' | 'product' | 'data';
  mapProgress: number;
  levelStatus: Array<'done' | 'active' | 'locked' | 'locked'>;
};

const STORAGE_KEY = 'assessment_profile_v1';

export const saveAssessmentProfile = (profile: AssessmentProfile) => {
  uni.setStorageSync(STORAGE_KEY, profile);
  uni.setStorageSync('assessment_recommended_role', profile.recommendedRole);
};

export const getAssessmentProfile = (): AssessmentProfile | null => {
  const profile = uni.getStorageSync(STORAGE_KEY);
  return profile || null;
};

export const buildProfileFromAnswers = (answers: Record<number, number>, type: string): AssessmentProfile => {
  const score = Object.values(answers).reduce((sum, v) => sum + (v || 0), 0);

  if (type === 'holland' && score <= 1) {
    return {
      assessmentType: 'holland',
      recommendedRole: '产品经理',
      roleKey: 'product',
      mapProgress: 35,
      levelStatus: ['done', 'active', 'locked', 'locked'],
    };
  }

  if (score >= 2) {
    return {
      assessmentType: type === 'holland' ? 'holland' : 'mbti',
      recommendedRole: '后端开发工程师',
      roleKey: 'backend',
      mapProgress: 52,
      levelStatus: ['done', 'active', 'active', 'locked'],
    };
  }

  return {
    assessmentType: type === 'holland' ? 'holland' : 'mbti',
    recommendedRole: '前端开发工程师',
    roleKey: 'frontend',
    mapProgress: 45,
    levelStatus: ['done', 'active', 'locked', 'locked'],
  };
};
