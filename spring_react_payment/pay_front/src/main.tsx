import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'

const isTossPaymentsError = (url: string) => {
  return url.includes('payment-gateway-sandbox.tosspayments.com') || 
         url.includes('payment-gateway.tosspayments.com') ||
         url.includes('tosspayments.com/_next/data') ||
         url.includes('tosspayments.com/_next/') ||
         url.includes('tosspayments.com/static/');
};

const originalConsoleError = console.error;
console.error = (...args: unknown[]) => {
  const message = args.join(' ');
  if (isTossPaymentsError(message)) {
    return;
  }
  originalConsoleError.apply(console, args);
};

window.addEventListener('error', (event) => {
  if (event.message && isTossPaymentsError(event.message)) {
    event.preventDefault();
    return false;
  }
  if (event.filename && isTossPaymentsError(event.filename)) {
    event.preventDefault();
    return false;
  }
  if (event.target && event.target instanceof HTMLScriptElement) {
    const src = event.target.src;
    if (src && isTossPaymentsError(src)) {
      event.preventDefault();
      return false;
    }
  }
});

const originalFetch = window.fetch;
window.fetch = async (...args) => {
  const url = typeof args[0] === 'string' ? args[0] : args[0].url;
  if (url && isTossPaymentsError(url)) {
    try {
      return await originalFetch.apply(window, args);
    } catch (error) {
      return Promise.reject(error);
    }
  }
  return originalFetch.apply(window, args);
};

const originalXHROpen = XMLHttpRequest.prototype.open;
const originalXHRSend = XMLHttpRequest.prototype.send;

XMLHttpRequest.prototype.open = function(method: string, url: string | URL, ...rest: unknown[]) {
  this._url = typeof url === 'string' ? url : url.toString();
  return originalXHROpen.apply(this, [method, url, ...rest]);
};

XMLHttpRequest.prototype.send = function(...args: unknown[]) {
  if (this._url && isTossPaymentsError(this._url)) {
    this.addEventListener('error', (event) => {
      event.stopPropagation();
    });
    this.addEventListener('loadend', () => {
      if (this.status === 404 && this._url && isTossPaymentsError(this._url)) {
        return;
      }
    });
  }
  return originalXHRSend.apply(this, args);
};

window.addEventListener('unhandledrejection', (event) => {
  if (event.reason && typeof event.reason === 'string' && isTossPaymentsError(event.reason)) {
    event.preventDefault();
    return false;
  }
  if (event.reason && event.reason.message && isTossPaymentsError(event.reason.message)) {
    event.preventDefault();
    return false;
  }
  if (event.reason && event.reason.url && isTossPaymentsError(event.reason.url)) {
    event.preventDefault();
    return false;
  }
});

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)

