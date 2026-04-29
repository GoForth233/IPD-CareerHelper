import request from '@/utils/request';

export interface Interview {
  interviewId?: number;
  userId: number;
  resumeId?: number;
  positionName: string;
  difficulty: string; // 'Easy', 'Normal', 'Hard'
  status?: string;    // 'ONGOING', 'COMPLETED', 'CANCELLED'
  finalScore?: number;
  reportMongoId?: string;
  startedAt?: string;
  endedAt?: string;
  durationSeconds?: number;
}

export interface InterviewMessage {
  msgId?: number;
  interviewId: number;
  role: string; // 'USER', 'AI'
  content: string;
  createdAt?: string;
}

export interface MessageResponse {
  userMessage: string;
  aiMessage: string;
}

/**
 * Start a new interview
 */
export const startInterviewApi = (data: {
  // userId is resolved server-side from the JWT; do not send it.
  resumeId?: number;
  positionName: string;
  difficulty: string;
}) => {
  return request<Interview>({
    url: '/api/interviews/start',
    method: 'POST',
    data,
  });
};

/**
 * Send message in interview (get AI response)
 */
export const sendInterviewMessageApi = (interviewId: number, content: string) => {
  return request<MessageResponse>({
    url: `/api/interviews/${interviewId}/message`,
    method: 'POST',
    data: { content },
  });
};

/**
 * Get interview message history
 */
export const getInterviewMessagesApi = (interviewId: number) => {
  return request<InterviewMessage[]>({
    url: `/api/interviews/${interviewId}/messages`,
    method: 'GET',
  });
};

/**
 * End interview. The final score is no longer client-supplied — it is
 * produced by the AI report endpoint based on the full transcript.
 */
export const endInterviewApi = (interviewId: number) => {
  return request<Interview>({
    url: `/api/interviews/${interviewId}/end`,
    method: 'POST',
  });
};

/**
 * Trigger the AI interviewer's opening question. Idempotent: returns the
 * existing first message if the conversation is non-empty.
 */
export const generateGreetingApi = (interviewId: number) => {
  return request<InterviewMessage>({
    url: `/api/interviews/${interviewId}/greeting`,
    method: 'POST',
  });
};

// ============================== Report ==============================

export interface RadarChartData {
  expression: number;
  logic: number;
  technical: number;
  pressureResistance: number;
  communication: number;
}

export interface AdviceItem {
  title: string;
  detail: string;
}

export interface InterviewReport {
  interviewId: number;
  positionName: string;
  difficulty: string;
  durationSeconds?: number;
  overallScore: number;
  totalQuestions: number;
  radarChart: RadarChartData;
  strengths: AdviceItem[];
  improvements: AdviceItem[];
  textSummary: string;
}

/**
 * Fetch (or generate) the AI evaluation report for an interview.
 * First call triggers a real AI evaluation (~10-20s); subsequent
 * calls hit the cached JSON on the interview row.
 */
export const getInterviewReportApi = (interviewId: number) => {
  return request<InterviewReport>({
    url: `/api/interviews/report/${interviewId}`,
    method: 'GET',
  });
};

/**
 * Get user's all interviews
 */
export const getUserInterviewsApi = (userId: number) => {
  return request<Interview[]>({
    url: `/api/interviews/user/${userId}`,
    method: 'GET',
  });
};

/**
 * Get interview by ID
 */
export const getInterviewByIdApi = (interviewId: number) => {
  return request<Interview>({
    url: `/api/interviews/${interviewId}`,
    method: 'GET',
  });
};

