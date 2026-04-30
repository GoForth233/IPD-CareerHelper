// Base URL logic: Prioritize environment variable, fallback to localhost
// Note: Backend controllers are mixed (e.g., /users, /api/resumes), so we point to root.
const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

interface Result<T> {
  code: number;
  message: string;
  data: T;
}

/**
 * Generic Request Function
 * @param options uni.request options
 * @returns Promise<T>
 */
const request = <T>(options: UniApp.RequestOptions & { silent?: boolean }): Promise<T> => {
  return new Promise((resolve, reject) => {
    // 1. Request Interceptor Logic
    const token = uni.getStorageSync('token');
    const header = {
      'Content-Type': 'application/json',
      // ngrok-free.dev gates every request behind an HTML "abuse" interstitial
      // unless this header is present (any value works). Without it the mini
      // program just gets back a chunk of HTML and our JSON parser throws.
      // Harmless when the backend is on a real domain — the header is just ignored.
      'ngrok-skip-browser-warning': '1',
      ...options.header,
    };

    if (token) {
      // @ts-ignore - Header index signature mismatch in Uni definitions sometimes
      header['Authorization'] = `Bearer ${token}`;
    }

    // 2. Execute Request
    uni.request({
      // AI-backed endpoints (diagnose / tailor / from-template) take 30-90s.
      // WeChat's default 60s timeout will silently abort long requests, so
      // raise the floor to 120s. Individual callers can override per-request.
      timeout: 120_000,
      ...options,
      url: `${BASE_URL}${options.url}`, // Auto-prepend Base URL
      header,
      success: (res) => {
        // 3. Response Interceptor Logic
        const statusCode = res.statusCode;
        const data = res.data as Result<T>;

        // Success Case: HTTP 200 AND Business Code 200
        if (statusCode === 200 && data.code === 200) {
          resolve(data.data);
        } else {
          const errorMsg = data.message || 'Request Failed';
          reject(new Error(errorMsg));
        }
      },
      fail: (err) => {
        reject(new Error('Network error, please check your connection'));
      },
    });
  });
};

export default request;

