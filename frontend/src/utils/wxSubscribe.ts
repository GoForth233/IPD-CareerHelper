import request from '@/utils/request';

/**
 * F10: WeChat subscribe message utility.
 *
 * Usage pattern (call after user completes an action that triggers a push):
 *
 *   import { requestSubscribe } from '@/utils/wxSubscribe';
 *   await requestSubscribe([TEMPLATE_IDS.WEEKLY_REPORT]);
 */

export interface QuotaItem {
  id: number;
  userId: number;
  templateId: string;
  remaining: number;
  updatedAt?: string;
}

/**
 * Call wx.requestSubscribeMessage for the given template IDs, then report
 * the results to the backend so it can track quota.
 *
 * Silently swallows errors — subscribe prompts are best-effort and must
 * never break the primary user flow.
 *
 * @param templateIds  list of WeChat subscribe template IDs to prompt for
 */
export async function requestSubscribe(templateIds: string[]): Promise<void> {
  if (!templateIds || templateIds.length === 0) return;

  try {
    const res: any = await new Promise((resolve) => {
      uni.requestSubscribeMessage({
        tmplIds: templateIds,
        success: (r) => resolve(r),
        fail: (e) => resolve(e),
      });
    });

    const results: Record<string, string> = {};
    templateIds.forEach((id) => {
      if (res && res[id]) results[id] = res[id];
    });

    if (Object.keys(results).length > 0) {
      await reportGrantToBackend(results);
    }
  } catch (e) {
    console.warn('[wxSubscribe] requestSubscribe error:', e);
  }
}

/**
 * Fetch remaining quota for all templates. Useful to decide which
 * templates to include in the next requestSubscribe prompt.
 */
export async function getSubscribeQuota(): Promise<QuotaItem[]> {
  try {
    return await request<QuotaItem[]>({
      url: '/api/wx-subscribe/quota',
      method: 'GET',
      silent: true,
    });
  } catch {
    return [];
  }
}

/**
 * POST the wx.requestSubscribeMessage result map to the backend.
 * Keys are template IDs, values are "accept" | "reject" | "ban".
 */
async function reportGrantToBackend(results: Record<string, string>): Promise<void> {
  try {
    await request<string>({
      url: '/api/wx-subscribe/grant',
      method: 'POST',
      data: { results },
      silent: true,
    });
  } catch (e) {
    console.warn('[wxSubscribe] reportGrant error:', e);
  }
}
