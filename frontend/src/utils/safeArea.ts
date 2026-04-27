export function getTopSafeHeight(): number {
  let h = 0;
  // #ifndef H5
  const systemInfo = uni.getSystemInfoSync();
  const menuButton = uni.getMenuButtonBoundingClientRect?.();
  if (menuButton && menuButton.top) {
    h = menuButton.top;
  } else {
    h = (systemInfo.statusBarHeight || 20) + 8;
  }
  // #endif
  return h;
}
