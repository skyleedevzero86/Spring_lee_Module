export const redirectToLogin = () => {
  window.dispatchEvent(new CustomEvent('auth:redirect', { detail: { path: '/login' } }));
};

