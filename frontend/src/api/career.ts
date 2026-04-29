import request from '@/utils/request';

export interface CareerPath {
  pathId?: number;
  code: string;
  name: string;
  description?: string;
}

export interface CareerNode {
  nodeId?: number;
  pathId: number;
  name: string;
  level: number;
  parentId: number;
  description?: string;
  estimatedHours?: number;
  sortOrder?: number;
  iconUrl?: string;
}

export interface UserCareerProgress {
  id?: number;
  userId: number;
  nodeId: number;
  status: string; // 'LOCKED', 'UNLOCKED', 'COMPLETED'
  updatedAt?: string;
}

/**
 * Get all career paths
 */
export const getCareerPathsApi = () => {
  return request<CareerPath[]>({
    url: '/api/careers/paths',
    method: 'GET',
  });
};

/**
 * Get career path by ID
 */
export const getCareerPathByIdApi = (pathId: number) => {
  return request<CareerPath>({
    url: `/api/careers/paths/${pathId}`,
    method: 'GET',
  });
};

/**
 * Get nodes for a career path
 */
export const getPathNodesApi = (pathId: number) => {
  return request<CareerNode[]>({
    url: `/api/careers/paths/${pathId}/nodes`,
    method: 'GET',
  });
};

/**
 * Get user's career progress
 */
export const getUserProgressApi = (userId: number) => {
  return request<UserCareerProgress[]>({
    url: `/api/careers/progress/${userId}`,
    method: 'GET',
  });
};

/**
 * Unlock a node for user
 */
export const unlockNodeApi = (userId: number, nodeId: number) => {
  return request<string>({
    url: '/api/careers/progress/unlock',
    method: 'POST',
    data: { userId, nodeId },
  });
};

/**
 * Complete a node for user
 */
export const completeNodeApi = (userId: number, nodeId: number) => {
  return request<string>({
    url: '/api/careers/progress/complete',
    method: 'POST',
    data: { userId, nodeId },
  });
};

/**
 * Initialize default career paths (for testing)
 */
export const initializeCareerPathsApi = () => {
  return request<string>({
    url: '/api/careers/initialize',
    method: 'POST',
  });
};

