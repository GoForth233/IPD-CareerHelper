import request from '@/utils/request';

/**
 * Resume Detail Interface (Stored in MongoDB)
 */
export interface ResumeDetail {
  skills: string[];
  education?: any[];
  projects?: any[];
  rawContent?: string;
}

/**
 * Resume Interface (Main Entity in MySQL)
 */
export interface Resume {
  resumeId?: number;
  userId: number;
  title: string;
  targetJob: string;
  fileUrl: string;
  status?: string;
  mongoDocId?: string;
  detail?: ResumeDetail; // Nested document for MongoDB
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Create Resume API
 * @param data Resume Data
 */
export const createResumeApi = (data: Resume) => {
  return request<Resume>({
    url: '/api/resumes',
    method: 'POST',
    data,
  });
};

/**
 * Get Resume API
 * @param resumeId Resume ID
 */
export const getResumeApi = (resumeId: number) => {
  return request<Resume>({
    url: `/api/resumes/${resumeId}`,
    method: 'GET',
  });
};

/**
 * Get User Resumes API
 * @param userId User ID
 */
export const getUserResumesApi = (userId: number) => {
  return request<Resume[]>({
    url: `/api/resumes/user/${userId}`,
    method: 'GET',
  });
};

/**
 * Delete Resume API
 */
export const deleteResumeApi = (resumeId: number) => {
  return request<void>({
    url: `/api/resumes/${resumeId}`,
    method: 'DELETE',
  });
};

/**
 * Update Resume API (rename / change targetJob etc.)
 */
export const updateResumeApi = (resumeId: number, data: Partial<Resume>) => {
  return request<Resume>({
    url: `/api/resumes/${resumeId}`,
    method: 'PUT',
    data,
  });
};

/**
 * Resume Diagnosis: send a resumeId (server pulls fileUrl from OSS) + JD,
 * get a structured AI analysis back (real Aliyun Qwen call).
 */
export interface DiagnosisResult {
  resumeId?: number;
  overallScore: number;
  strengths: string[];
  weaknesses: string[];
  suggestions: string[];
  rawAnalysis?: string;
}

export const diagnoseResumeApi = (data: {
  resumeId?: number;
  resumeText?: string;
  jobDescription: string;
}) => {
  return request<DiagnosisResult>({
    url: '/api/resume-diagnosis/analyze',
    method: 'POST',
    data,
  });
};

/**
 * Generate a brand-new resume from structured form data (AI -> PDF -> OSS).
 */
export const generateResumeFromTemplateApi = (data: Record<string, any>) => {
  return request<Resume>({
    url: '/api/resume-gen/from-template',
    method: 'POST',
    data,
  });
};

/**
 * Tailor an existing resume against a target Job Description.
 */
export const tailorResumeApi = (data: { userId: number; resumeId: number; jobDescription: string }) => {
  return request<Resume>({
    url: '/api/resume-gen/tailor',
    method: 'POST',
    data,
  });
};

/**
 * Upload a PDF resume file directly to backend (which forwards to Aliyun OSS).
 * Returns the public OSS URL on success.
 */
export const uploadResumeFile = (filePath: string, folder: string = 'resumes'): Promise<string> => {
  const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
  const token = uni.getStorageSync('token');
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: `${BASE_URL}/api/files/upload`,
      filePath,
      name: 'file',
      formData: { folder },
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success: (res) => {
        try {
          const body = typeof res.data === 'string' ? JSON.parse(res.data) : res.data;
          if (body && body.code === 200 && body.data) {
            resolve(body.data as string);
          } else {
            reject(new Error(body?.message || 'Upload failed'));
          }
        } catch (e) {
          reject(new Error('Invalid upload response'));
        }
      },
      fail: () => reject(new Error('Upload network error')),
    });
  });
};

