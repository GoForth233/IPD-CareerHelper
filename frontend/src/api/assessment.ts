import request from '@/utils/request';

/**
 * Assessment scale (e.g. MBTI, Holland)
 */
export interface AssessmentScale {
  scaleId: number;
  title: string;
  description?: string;
  questionCount?: number;
  status?: number;
  version?: string;
  isActive?: boolean;
  createdAt?: string;
}

/**
 * One option for a quiz question. The backend never returns the dimension
 * code or score so a curious user can't reverse-engineer the answer key
 * from a captured response.
 */
export interface QuizOption {
  optionId: number;
  optionLabel: string; // 'A' | 'B' | ...
  optionText: string;
}

export interface QuizQuestion {
  questionId: number;
  questionText: string;
  questionType: 'SINGLE' | 'MULTIPLE' | 'TEXT' | 'SCORE';
  dimensionCode?: string; // not always populated for the client
  sortOrder: number;
  options: QuizOption[];
}

/**
 * Result row stored in `assessment_records`.
 *   resultSummary  -- short label e.g. "INTJ"
 *   resultJson     -- JSON string of {dimension: count}
 */
export interface AssessmentRecord {
  recordId: number;
  userId: number;
  scaleId: number;
  status: string;
  resultSummary?: string;
  resultJson?: string;
  /**
   * AI-generated personalised insight, JSON string of
   * {strengths: string, growth: string, suggestedRoles: string[]}.
   * Populated server-side at submission time and cached.
   */
  aiInsight?: string;
  createdAt?: string;
}

/** GET /api/assessments/scales */
export const getAssessmentScalesApi = () =>
  request<AssessmentScale[]>({
    url: '/api/assessments/scales',
    method: 'GET',
  });

/** GET /api/assessments/scales/{scaleId}/questions */
export const getScaleQuestionsApi = (scaleId: number) =>
  request<QuizQuestion[]>({
    url: `/api/assessments/scales/${scaleId}/questions`,
    method: 'GET',
  });

/** POST /api/assessments/submit  -- userId resolved from JWT server-side */
export const submitAssessmentApi = (
  scaleId: number,
  answers: Record<number, number>, // questionId -> optionId
) =>
  request<AssessmentRecord>({
    url: '/api/assessments/submit',
    method: 'POST',
    data: { scaleId, answers },
  });

/** GET /api/assessments/records */
export const getMyAssessmentRecordsApi = () =>
  request<AssessmentRecord[]>({
    url: '/api/assessments/records',
    method: 'GET',
  });

/** GET /api/assessments/records/detail/{recordId} */
export const getAssessmentRecordApi = (recordId: number) =>
  request<AssessmentRecord>({
    url: `/api/assessments/records/detail/${recordId}`,
    method: 'GET',
  });
