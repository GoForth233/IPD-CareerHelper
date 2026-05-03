/**
 * F5/F6: Theme & font-scale utilities.
 *
 * Storage keys:
 *   app_pref_dark  '0'|'1'        — dark mode toggle (pre-existing)
 *   app_pref_theme 'light'|'dark'|'green'   — explicit theme (new)
 *   app_pref_font  'compact'|'standard'|'large'  — font scale (pre-existing, now applied)
 *
 * Usage in page components:
 *   import { useTheme } from '@/utils/theme';
 *   const { themeClass, fontClass } = useTheme();
 *   // In template: <view :class="[themeClass, fontClass]">
 */
import { ref, computed } from 'vue';

export type ThemeKey = 'light' | 'dark' | 'green';
export type FontKey  = 'compact' | 'standard' | 'large';

const THEME_KEY = 'app_pref_theme';
const DARK_KEY  = 'app_pref_dark';
const FONT_KEY  = 'app_pref_font';

export function useTheme() {
  const theme = ref<ThemeKey>(_readTheme());
  const font  = ref<FontKey>(_readFont());

  const themeClass = computed(() => {
    if (theme.value === 'dark') return 'is-dark';
    if (theme.value === 'green') return 'theme-green';
    return '';
  });

  const fontClass = computed(() => {
    if (font.value === 'compact') return 'font-compact';
    if (font.value === 'large')   return 'font-large';
    return '';
  });

  function setTheme(t: ThemeKey) {
    theme.value = t;
    uni.setStorageSync(THEME_KEY, t);
    uni.setStorageSync(DARK_KEY, t === 'dark' ? '1' : '0');
  }

  function setFont(f: FontKey) {
    font.value = f;
    uni.setStorageSync(FONT_KEY, f);
  }

  function refresh() {
    theme.value = _readTheme();
    font.value  = _readFont();
  }

  return { theme, font, themeClass, fontClass, setTheme, setFont, refresh };
}

function _readTheme(): ThemeKey {
  const stored = uni.getStorageSync(THEME_KEY) as ThemeKey;
  if (stored === 'dark' || stored === 'green' || stored === 'light') return stored;
  const isDark = uni.getStorageSync(DARK_KEY) === '1';
  return isDark ? 'dark' : 'light';
}

function _readFont(): FontKey {
  const stored = uni.getStorageSync(FONT_KEY) as FontKey;
  if (stored === 'compact' || stored === 'large') return stored;
  return 'standard';
}
