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

// ── Module-level singleton refs ──────────────────────────────────────────────
// All components that call useTheme() share these exact same refs, so a
// setTheme() call in user/index.vue instantly updates themeClass in
// home/index.vue and assistant/index.vue without needing a page remount.
const _theme = ref<ThemeKey>(_readTheme());
const _font  = ref<FontKey>(_readFont());

const _themeClass = computed(() => {
  if (_theme.value === 'dark')  return 'is-dark';
  if (_theme.value === 'green') return 'theme-green';
  return '';
});

const _fontClass = computed(() => {
  if (_font.value === 'compact') return 'font-compact';
  if (_font.value === 'large')   return 'font-large';
  return '';
});
// ─────────────────────────────────────────────────────────────────────────────

export function useTheme() {
  function setTheme(t: ThemeKey) {
    _theme.value = t;
    uni.setStorageSync(THEME_KEY, t);
    uni.setStorageSync(DARK_KEY, t === 'dark' ? '1' : '0');
  }

  function setFont(f: FontKey) {
    _font.value = f;
    uni.setStorageSync(FONT_KEY, f);
  }

  function refresh() {
    _theme.value = _readTheme();
    _font.value  = _readFont();
  }

  return {
    theme: _theme,
    font: _font,
    themeClass: _themeClass,
    fontClass: _fontClass,
    setTheme,
    setFont,
    refresh,
  };
}

function _readTheme(): ThemeKey {
  try {
    const stored = uni.getStorageSync(THEME_KEY) as ThemeKey;
    if (stored === 'dark' || stored === 'green' || stored === 'light') return stored;
    const isDark = uni.getStorageSync(DARK_KEY) === '1';
    return isDark ? 'dark' : 'light';
  } catch { return 'light'; }
}

function _readFont(): FontKey {
  try {
    const stored = uni.getStorageSync(FONT_KEY) as FontKey;
    if (stored === 'compact' || stored === 'large') return stored;
  } catch { /* ignore */ }
  return 'standard';
}
