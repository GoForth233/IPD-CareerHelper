import request from '@/utils/request';

export type HomeContentType = 'video' | 'article';

export interface HomeContentItem {
  id?: number;
  type: HomeContentType;
  tag?: string;
  title: string;
  views?: string;
  heat?: string;
  thumbnail?: string;
  url: string;
}

export interface HomeContentResponse {
  careerInsights: HomeContentItem[];
  trendingTopics: HomeContentItem[];
}

/**
 * Get home page content (Career Insights + Trending Topics)
 */
export const getHomeContentApi = () => {
  return request<HomeContentResponse>({
    url: '/api/home/content',
    method: 'GET',
  });
};
