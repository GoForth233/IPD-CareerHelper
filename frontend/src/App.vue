<script setup lang="ts">
import { onHide, onLaunch, onShow } from '@dcloudio/uni-app';
import { isLoggedIn, LOGIN_PAGE } from '@/utils/auth';

const protectedRoutes = new Set([
  'pages/home/index',
  'pages/messages/index',
  'pages/assistant/index',
  'pages/resume/index',
  'pages/user/index',
  'pages/assessment/index',
  'pages/assessment/quiz',
  'pages/assessment/result',
  'pages/map/index',
  'pages/interview/index',
  'pages/interview/room',
  'pages/interview/report',
]);

const shouldRedirectToLogin = () => {
  const pages = getCurrentPages();
  if (!pages.length) return false;

  const current = pages[pages.length - 1] as any;
  const route = current?.route as string | undefined;

  if (!route) return false;
  if (route === 'pages/login/login') return false;

  return protectedRoutes.has(route) && !isLoggedIn();
};

onLaunch(() => {
  console.log('App Launch');
});

onShow(() => {
  console.log('App Show');

  if (!shouldRedirectToLogin()) return;

  uni.showToast({ title: '请先登录', icon: 'none' });
  setTimeout(() => {
    uni.reLaunch({ url: LOGIN_PAGE });
  }, 200);
});

onHide(() => {
  console.log('App Hide');
});
</script>
<style></style>
