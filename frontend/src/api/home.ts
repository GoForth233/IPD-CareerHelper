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

export interface CareerCard {
  pathId: number;
  name: string;
  description: string;
}

export interface HomepageFeedResponse {
  careerCards: CareerCard[];
  articles: { title: string; summary: string; imageUrl: string }[];
  stats: { totalCareerPaths: number; totalUsers: number; totalInterviews: number };
}

/**
 * Get homepage aggregated feed from backend
 */
export const getHomeContentApi = (userId?: number) => {
  return request<HomepageFeedResponse>({
    url: userId ? `/api/homepage/feed?userId=${userId}` : '/api/homepage/feed',
    method: 'GET',
    silent: true,
  });
};
