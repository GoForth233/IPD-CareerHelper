import request from '@/utils/request';

export interface Interview {
  interviewId?: number;
  userId: number;
  resumeId?: number;
  positionName: string;
  difficulty: string; // 'Easy', 'Normal', 'Hard'
  status?: string;    // 'ONGOING', 'COMPLETED', 'CANCELLED'
  /** Modality the candidate started with — drives history resume routing. */
  mode?: 'TEXT' | 'VOICE';
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
  /** TEXT (default) or VOICE — must match what the start page picked. */
  mode?: 'TEXT' | 'VOICE';
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

// ============================== Voice (Digital Human) ==============================

/**
 * Voice turn payload returned by both `voice-greeting` and `voice-turn`.
 *
 * `userText` is null for the greeting call (no candidate input yet); on the
 * answer-reply path it carries the Paraformer transcript so the UI can
 * confirm what we heard.
 *
 * `audioUrl` is a 30-min presigned OSS URL — pop it straight into
 * `<audio src="...">`. If the player still hasn't played 30 min later, ask
 * the backend for the same `audioKey` again with a fresh sign.
 *
 * `durationMs` is a coarse estimate the backend computes from byte count;
 * the player should overwrite it with the real duration once
 * `loadedmetadata` fires. It's there so the lip-sync animation can start
 * immediately even if the device is slow to load audio metadata.
 */
export interface VoiceTurnResponse {
  userText?: string;
  aiText: string;
  audioKey: string;
  audioUrl: string;
  durationMs: number;
}

/**
 * Generate (or fetch the cached text of) the AI interviewer's opening
 * question and return a freshly synthesized audio URL. Safe to call on
 * every entry to room.vue.
 */
export const voiceGreetingApi = (interviewId: number) => {
  return request<VoiceTurnResponse>({
    url: `/api/interviews/${interviewId}/voice-greeting`,
    method: 'POST',
  });
};

/**
 * Upload the candidate's recorded answer (mp3, 16 kHz, mono) and receive:
 *   - the Paraformer transcript of what they said
 *   - the AI interviewer's next-question text
 *   - a presigned URL playing back the AI's spoken next-question
 *
 * This is wired through `uni.uploadFile` (not the JSON axios layer) because
 * the WeChat mini-program multipart support is on `uni.uploadFile` only.
 */
export const voiceTurnApi = (
  interviewId: number,
  filePath: string,
  format: 'mp3' | 'aac' | 'wav' = 'mp3'
): Promise<VoiceTurnResponse> => {
  return new Promise((resolve, reject) => {
    const BASE_URL = (import.meta as any).env?.VITE_API_BASE_URL || 'http://localhost:8080';
    const token = uni.getStorageSync('token');
    uni.uploadFile({
      url: `${BASE_URL}/api/interviews/${interviewId}/voice-turn`,
      filePath,
      name: 'audio',
      formData: { format },
      // ngrok-free.dev shows an HTML interstitial unless this header is
      // present (any value); without it the upload returns ngrok HTML and
      // our JSON parser throws "Invalid voice-turn response". Harmless on
      // a real domain.
      header: {
        'ngrok-skip-browser-warning': '1',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      success: (res) => {
        try {
          const body = typeof res.data === 'string' ? JSON.parse(res.data) : res.data;
          if (res.statusCode === 200 && body && body.code === 200 && body.data) {
            resolve(body.data as VoiceTurnResponse);
          } else {
            reject(new Error(body?.message || `Voice turn failed (HTTP ${res.statusCode})`));
          }
        } catch {
          reject(new Error('Invalid voice-turn response'));
        }
      },
      fail: (err: any) => reject(new Error(err?.errMsg || 'Voice upload network error')),
    });
  });
};

// ============================== Report ==============================

export interface RadarChartData {
  expression: number;
  logic: number;
  technical: number;
  pressureResistance: number;
  communication: number;
  /** Sprint C-1 — composite of eye contact + facial expression + posture.
   *  Null when the candidate took the text path (no camera frames). */
  bodyLanguage?: number | null;
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

