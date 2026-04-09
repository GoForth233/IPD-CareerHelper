export const LOGIN_PAGE = '/pages/login/login';

export const isLoggedIn = (): boolean => {
  const userId = uni.getStorageSync('userId');
  return Boolean(userId);
};

export const clearAuthState = () => {
  uni.removeStorageSync('token');
  uni.removeStorageSync('userId');
  uni.removeStorageSync('userInfo');
};

export const requireAuth = (redirectType: 'reLaunch' | 'navigateTo' = 'reLaunch'): boolean => {
  if (isLoggedIn()) return true;

  uni.showToast({ title: '请先登录', icon: 'none' });

  setTimeout(() => {
    if (redirectType === 'navigateTo') {
      uni.navigateTo({ url: LOGIN_PAGE });
    } else {
      uni.reLaunch({ url: LOGIN_PAGE });
    }
  }, 250);

  return false;
};
