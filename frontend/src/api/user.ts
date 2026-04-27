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
  identityType: string; // 'EMAIL_PASSWORD'
  identifier: string;   // email
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
export interface LoginResponse {
  token: string;
  user: User;
}

export const registerApi = (data: RegisterDTO) => {
  return request<User>({
    url: '/auth/register',
    method: 'POST',
    data,
  });
};

/**
 * User Login API
 */
export const loginApi = (data: LoginDTO) => {
  return request<LoginResponse>({
    url: '/auth/login',
    method: 'POST',
    data,
  });
};

/**
 * WeChat Login API
 */
export const wechatLoginApi = (data: { code: string }) => {
  return request<LoginResponse>({
    url: '/auth/wechat-login',
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

export interface UpdateUserDTO {
  school?: string;
  major?: string;
  graduationYear?: number;
}

/**
 * Update User Profile (school / major / graduationYear)
 */
export const updateUserApi = (userId: number, data: UpdateUserDTO) => {
  return request<User>({
    url: `/users/${userId}`,
    method: 'PUT',
    data,
  });
};
