/**
 * F4: i18n setup for CareerLoop.
 *
 * Language detection order:
 *   1. `app_lang` in localStorage (user explicit choice)
 *   2. WeChat system language (uni.getSystemInfoSync().language)
 *   3. Falls back to 'zh-CN'
 *
 * Supported locales: zh-CN, en-US
 */
import { createI18n } from 'vue-i18n';
import zhCN from './zh-CN';
import enUS from './en-US';

export const LANG_KEY = 'app_lang';
export type LangCode = 'zh-CN' | 'en-US';

function detectLang(): LangCode {
  try {
    const stored = uni.getStorageSync(LANG_KEY) as LangCode;
    if (stored === 'zh-CN' || stored === 'en-US') return stored;
    const sysLang = uni.getSystemInfoSync().language || '';
    if (sysLang.startsWith('en')) return 'en-US';
  } catch { /* ignore — uni not ready or storage unavailable */ }
  return 'zh-CN';
}

export const i18n = createI18n({
  legacy: false,
  locale: detectLang(),
  fallbackLocale: 'zh-CN',
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS,
  },
});

export function setLocale(lang: LangCode) {
  uni.setStorageSync(LANG_KEY, lang);
  (i18n.global.locale as any).value = lang;
}

export function currentLocale(): LangCode {
  return (i18n.global.locale as any).value as LangCode;
}
