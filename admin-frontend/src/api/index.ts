import axios from 'axios';

// Same-origin: vite dev server proxies /api to localhost:8080.
const http = axios.create({ baseURL: '', timeout: 30_000 });

http.interceptors.request.use((cfg) => {
  const token = localStorage.getItem('admin_token');
  if (token) cfg.headers.Authorization = `Bearer ${token}`;
  return cfg;
});

http.interceptors.response.use(
  (res) => {
    // Backend wraps every response in {code, message, data}; unwrap so
    // call sites don't have to handle that consistently themselves.
    if (res.data && typeof res.data.code !== 'undefined') {
      if (res.data.code === 200) return res.data.data;
      return Promise.reject(new Error(res.data.message || 'Request failed'));
    }
    return res.data;
  },
  (err) => Promise.reject(err)
);

export interface OrgDashboard {
  orgId: number;
  studentCount: number;
  interviewCount: number;
  reportCount: number;
  radarAverages: Record<string, number>;
  weakDimensionsTop3: string[];
}

export interface StudentRow {
  userId: number;
  nickname: string;
  school?: string;
  major?: string;
  interviewCount: number;
  lastInterviewScore?: number;
}

export interface CareerPath { pathId?: number; code: string; name: string; description?: string; }
export interface CareerNode {
  nodeId?: number;
  pathId: number;
  name: string;
  description?: string;
  iconUrl?: string;
  level?: number;
  sortOrder?: number;
  estimatedHours?: number;
  parentId?: number;
}

export interface InterviewQuestion {
  id?: number;
  position: string;
  difficulty: string;
  content: string;
  summary?: string;
  answer?: string;
  likes?: number;
  drawCount?: number;
  status?: string;
  source?: string;
  reviewStatus?: string;
  createdAt?: string;
}

export interface Organization {
  orgId?: number;
  code: string;
  name: string;
  description?: string;
  contactName?: string;
  contactEmail?: string;
  active?: boolean;
}

export const api = {
  // Auth — admin reuses the C-side /auth/login then checks /admin/whoami.
  login: (email: string, password: string) =>
    axios.post('/auth/login', { identityType: 'EMAIL_PASSWORD', identifier: email, credential: password })
      .then((r) => r.data?.data || r.data),
  whoami: () => http.get<boolean>('/api/admin/whoami'),

  // Organizations + dashboard
  listOrgs: () => http.get<Organization[]>('/api/admin/organizations'),
  saveOrg: (payload: Organization) => http.post<Organization>('/api/admin/organizations', payload),
  orgDashboard: (orgId: number) => http.get<OrgDashboard>(`/api/admin/organizations/${orgId}/dashboard`),
  orgStudents: (orgId: number) => http.get<StudentRow[]>(`/api/admin/organizations/${orgId}/students`),

  // Skill map
  listPaths: () => http.get<CareerPath[]>('/api/admin/career-paths'),
  savePath: (p: CareerPath) => http.post<CareerPath>('/api/admin/career-paths', p),
  deletePath: (pathId: number) => http.delete<void>(`/api/admin/career-paths/${pathId}`),
  listNodes: (pathId: number) => http.get<CareerNode[]>(`/api/admin/career-paths/${pathId}/nodes`),
  saveNode: (pathId: number, n: CareerNode) =>
    http.post<CareerNode>(`/api/admin/career-paths/${pathId}/nodes`, n),
  deleteNode: (nodeId: number) => http.delete<void>(`/api/admin/career-paths/nodes/${nodeId}`),

  // Question bank
  listQuestions: (params?: { source?: string; reviewStatus?: string }) =>
    http.get<InterviewQuestion[]>('/api/admin/questions', { params }),
  listPendingReview: () =>
    http.get<InterviewQuestion[]>('/api/admin/questions', { params: { reviewStatus: 'PENDING_REVIEW' } }),
  approveQuestion: (id: number) => http.post<InterviewQuestion>(`/api/admin/questions/${id}/approve`, {}),
  rejectQuestion: (id: number) => http.post<InterviewQuestion>(`/api/admin/questions/${id}/reject`, {}),
  updateQuestion: (id: number, payload: Partial<InterviewQuestion>) =>
    http.post<InterviewQuestion>(`/api/admin/questions/${id}`, payload),
  deleteQuestion: (id: number) => http.delete<void>(`/api/admin/questions/${id}`),

  // Weekly report
  runWeeklyReport: () => http.post<{ delivered: number; skipped: number }>('/api/admin/weekly-report/run'),
};

export default http;
