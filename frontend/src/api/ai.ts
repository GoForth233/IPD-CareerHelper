import request from '@/utils/request';

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
}

/**
 * AI Chat API
 */
export const chatAiApi = (messages: ChatMessage[]) => {
  return request<string>({
    url: '/api/ai/chat',
    method: 'POST',
    data: { messages },
  });
};
