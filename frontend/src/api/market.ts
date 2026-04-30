import request from '@/utils/request';

export interface MarketQuestion {
  id: number;
  position: string;
  difficulty: string;
  content: string;
  summary?: string | null;
  contributorHash?: string | null;
  likes: number;
  drawCount: number;
  status: string;
  createdAt?: string;
}

export interface MarketPageResponse {
  items: MarketQuestion[];
  total: number;
  page: number;
  size: number;
}

export interface ListMarketParams {
  position?: string;
  difficulty?: string;
  page?: number;
  size?: number;
}

const buildQuery = (params: ListMarketParams): string => {
  const parts: string[] = [];
  (Object.keys(params) as Array<keyof ListMarketParams>).forEach((k) => {
    const v = params[k];
    if (v === undefined || v === null || v === '') return;
    parts.push(`${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`);
  });
  return parts.length ? `?${parts.join('&')}` : '';
};

/** Browse the market with optional filters and pagination. */
export const listMarketApi = (params: ListMarketParams = {}) =>
  request<MarketPageResponse>({
    url: '/api/questions' + buildQuery(params),
    method: 'GET',
    silent: true,
  });

/** Like a question. Backend bumps the counter and returns the fresh row. */
export const likeQuestionApi = (id: number) =>
  request<MarketQuestion>({
    url: `/api/questions/${id}/like`,
    method: 'POST',
  });

export interface ContributeQuestionPayload {
  position: string;
  difficulty: string;
  content: string;
  summary?: string;
}

/** Submit a fresh question to the market — JWT required (anonymized server-side). */
export const contributeQuestionApi = (payload: ContributeQuestionPayload) =>
  request<MarketQuestion>({
    url: '/api/questions',
    method: 'POST',
    data: payload,
  });
