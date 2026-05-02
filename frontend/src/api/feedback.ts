import request from '@/utils/request';

export type FeedbackCategory = 'FUNCTION_BUG' | 'SUGGESTION' | 'CONTENT_REPORT' | 'OTHER';

export interface SubmitFeedbackDto {
  category: FeedbackCategory;
  content: string;
  contact?: string;
  attachmentUrls?: string;
}

/** POST /api/feedback/submit — auth optional */
export const submitFeedbackApi = (data: SubmitFeedbackDto) =>
  request<number>({
    url: '/api/feedback/submit',
    method: 'POST',
    data,
  });
