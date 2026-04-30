import request from '@/utils/request';

/**
 * Submit one base64-encoded interview frame for body-language scoring.
 * Best-effort — the backend swallows sidecar failures, so callers can
 * fire-and-forget. We mark the request silent so the global error toast
 * doesn't fire on transient hiccups.
 */
export const submitBodyLanguageFrameApi = (interviewId: number, frameBase64: string) =>
  request<void>({
    url: '/api/body-language/frame',
    method: 'POST',
    data: { interviewId, frameBase64 },
    silent: true,
  });
