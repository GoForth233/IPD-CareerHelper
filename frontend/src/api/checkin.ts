import request from '@/utils/request';

export interface CheckInStatus {
  /** 0..3 — how many of today's three core actions are done. */
  todayCompleted: number;
  todayTotal: number;
  /** 0..7 — distinct days with any check-in in the rolling 7-day window. */
  weeklyDays: number;
  /** Consecutive days with at least one check-in, ending today. */
  streakDays: number;
  /** True when the user already cleared the weekly badge bar. */
  badgeEarnedThisWeek: boolean;
  /** Action codes already done today (for chip green state). */
  completedActionsToday: string[];
}

export interface CheckInDay {
  id: number;
  userId: number;
  /** ISO date "yyyy-MM-dd". */
  day: string;
  action: string;
  createdAt?: string;
}

/** Read the current user's 7-day check-in status. */
export const getCheckInStatusApi = () =>
  request<CheckInStatus>({
    url: '/api/checkin/status',
    method: 'GET',
    silent: true,
  });

/**
 * Stamp a check-in manually (debug / admin paths only — the assessment,
 * interview, and skill-node flows already trigger this server-side).
 */
export const triggerCheckInApi = (action: 'ASSESSMENT' | 'INTERVIEW' | 'SKILL_NODE') =>
  request<CheckInStatus>({
    url: `/api/checkin/trigger?action=${encodeURIComponent(action)}`,
    method: 'POST',
  });

/** All check-in rows for the user in the last 30 days (calendar backing). */
export const getCheckInCalendarApi = () =>
  request<CheckInDay[]>({
    url: '/api/checkin/calendar',
    method: 'GET',
    silent: true,
  });
