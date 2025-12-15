import request from '@/utils/request';

export interface User {
  userId?: number;
  nickname: string;
  avatarUrl?: string;
  school?: string;
  major?: string;
  graduationYear?: number;
  points?: number;
  isVip?: boolean;
  status?: number;
}

export interface RegisterDTO {
  nickname: string;
  identityType: string; // 'PASSWORD', 'MOBILE'
  identifier: string;   // username or phone
  credential: string;   // password
}

export interface LoginDTO {
  identityType: string;
  identifier: string;
  credential: string;
}

/**
 * User Register API
 */
export const registerApi = (data: RegisterDTO) => {
  return request<User>({
    url: '/users/register',
    method: 'POST',
    data,
  });
};

/**
 * User Login API
 */
export const loginApi = (data: LoginDTO) => {
  return request<User>({
    url: '/users/login',
    method: 'POST',
    data,
  });
};

/**
 * Get User Info (with Redis cache)
 */
export const getUserInfoApi = (userId: number) => {
  return request<User>({
    url: `/users/${userId}`,
    method: 'GET',
  });
};

