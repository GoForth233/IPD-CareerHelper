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
  userId: number;
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
 * End interview
 */
export const endInterviewApi = (interviewId: number, finalScore: number) => {
  return request<Interview>({
    url: `/api/interviews/${interviewId}/end`,
    method: 'POST',
    data: { finalScore },
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

