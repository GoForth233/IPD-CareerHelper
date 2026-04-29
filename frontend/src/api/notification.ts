import request from '@/utils/request';

export interface Notification {
  notificationId: number;
  userId: number;
  type: string;          // 'INTERVIEW_COMPLETED' | 'ASSESSMENT_DONE' | ...
  title: string;
  content?: string;
  link?: string;         // optional deep link, e.g. /pages/interview/report?interviewId=42
  readFlag: boolean;
  createdAt?: string;
}

/** GET /api/notifications -- newest first */
export const listNotificationsApi = () =>
  request<Notification[]>({
    url: '/api/notifications',
    method: 'GET',
  });

/** GET /api/notifications/unread-count */
export const getUnreadCountApi = () =>
  request<{ count: number }>({
    url: '/api/notifications/unread-count',
    method: 'GET',
  });

/** POST /api/notifications/{id}/read */
export const markReadApi = (id: number) =>
  request<string>({
    url: `/api/notifications/${id}/read`,
    method: 'POST',
  });

/** POST /api/notifications/read-all */
export const markAllReadApi = () =>
  request<{ updated: number }>({
    url: '/api/notifications/read-all',
    method: 'POST',
  });
