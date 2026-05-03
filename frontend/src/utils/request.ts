// Base URL logic: Prioritize environment variable, fallback to localhost
// Note: Backend controllers are mixed (e.g., /users, /api/resumes), so we point to root.
const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

interface Result<T> {
  code: number;
  message: string;
  data: T;
}

const isResultEnvelope = <T>(value: unknown): value is Result<T> => {
  return !!value
    && typeof value === 'object'
    && 'code' in value
    && ('data' in value || 'message' in value);
};

type RequestOptions = UniApp.RequestOptions & {
  silent?: boolean;   // suppress error toasts when true
  _retried?: boolean; // F21: internal flag to prevent double-retry
};

/**
 * Generic Request Function — with F21 global error handling:
 *   - 401: clear token, redirect to login
 *   - 5xx: one auto-retry after 1.5 s, then show toast
 *   - Network fail: friendly Chinese toast + reject
 */
const request = <T>(options: RequestOptions): Promise<T> => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token');
    const header: Record<string, string> = {
      'Content-Type': 'application/json',
      'ngrok-skip-browser-warning': '1',
      ...(options.header as Record<string, string>),
    };
    if (token) header['Authorization'] = `Bearer ${token}`;

    uni.request({
      // AI endpoints can take up to 2 min — keep this floor high
      timeout: 120_000,
      ...options,
      url: `${BASE_URL}${options.url}`,
      header,
      success: (res) => {
        const statusCode = res.statusCode;
        const data = res.data;

        if (statusCode >= 200 && statusCode < 300) {
          if (isResultEnvelope<T>(data)) {
            if (data.code === 200) {
              resolve(data.data);
              return;
            }

            const errorMsg = data.message || `请求失败 (${statusCode})`;
            if (!options.silent) {
              uni.showToast({ title: errorMsg, icon: 'none', duration: 2500 });
            }
            reject(new Error(errorMsg));
            return;
          }

          // Some endpoints return raw arrays/objects/files instead of the
          // common Result envelope. A 2xx HTTP status is still a success.
          resolve(data as T);
          return;
        }

        // F21: 401 → session expired, redirect to login
        if (statusCode === 401) {
          uni.removeStorageSync('token');
          uni.removeStorageSync('userId');
          if (!options.silent) {
            uni.showToast({ title: '登录已过期，请重新登录', icon: 'none', duration: 2000 });
          }
          setTimeout(() => uni.reLaunch({ url: '/pages/login/index' }), 1500);
          reject(new Error('Unauthorized'));
          return;
        }

        // F21: 5xx → retry once
        if (statusCode >= 500 && !options._retried) {
          setTimeout(() => {
            request<T>({ ...options, _retried: true }).then(resolve).catch(reject);
          }, 1500);
          return;
        }

        const errorMsg = isResultEnvelope<T>(data) && data.message
          ? data.message
          : `请求失败 (${statusCode})`;
        if (!options.silent) {
          uni.showToast({ title: errorMsg, icon: 'none', duration: 2500 });
        }
        reject(new Error(errorMsg));
      },
      fail: (_err) => {
        if (!options.silent) {
          uni.showToast({ title: '网络异常，请检查网络连接', icon: 'none', duration: 2500 });
        }
        reject(new Error('网络异常，请检查网络连接'));
      },
    });
  });
};

export default request;

