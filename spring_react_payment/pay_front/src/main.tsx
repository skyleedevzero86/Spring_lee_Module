import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'

const isTossPaymentsError = (url: string) => {
  if (typeof url !== 'string') return false;
  return url.includes('payment-gateway-sandbox.tosspayments.com') || 
         url.includes('payment-gateway.tosspayments.com') ||
         url.includes('tosspayments.com/_next/data') ||
         url.includes('tosspayments.com/_next/') ||
         url.includes('tosspayments.com/static/') ||
         url.includes('/_next/data/') ||
         url.includes('/_next/static/');
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
  const url = typeof args[0] === 'string' ? args[0] : (args[0]?.url || '');
  if (url && isTossPaymentsError(url)) {
    try {
      const response = await originalFetch.apply(window, args);
      if (!response.ok && response.status === 404) {
        return response;
      }
      return response;
    } catch (error) {
      if (error instanceof TypeError && error.message.includes('Failed to fetch')) {
        return new Response(null, { status: 404, statusText: 'Not Found' });
      }
      return Promise.reject(error);
    }
  }
  return originalFetch.apply(window, args);
};

const originalXHROpen = XMLHttpRequest.prototype.open;
const originalXHRSend = XMLHttpRequest.prototype.send;

XMLHttpRequest.prototype.open = function(method: string, url: string | URL, ...rest: unknown[]) {
  (this as any)._url = typeof url === 'string' ? url : url.toString();
  return originalXHROpen.apply(this, [method, url, ...rest] as any);
};

XMLHttpRequest.prototype.send = function(...args: unknown[]) {
  const xhr = this as any;
  if (xhr._url && isTossPaymentsError(xhr._url)) {
    xhr.addEventListener('error', (event: Event) => {
      event.stopPropagation();
    });
    xhr.addEventListener('loadend', () => {
      if (xhr.status === 404 && xhr._url && isTossPaymentsError(xhr._url)) {
        return;
      }
    });
  }
  return originalXHRSend.apply(this, args);
};

window.addEventListener('unhandledrejection', (event) => {
  const reason = event.reason;
  if (!reason) return;
  
  if (typeof reason === 'string' && isTossPaymentsError(reason)) {
    event.preventDefault();
    return false;
  }
  
  if (reason && typeof reason === 'object') {
    if ('message' in reason && typeof reason.message === 'string' && isTossPaymentsError(reason.message)) {
      event.preventDefault();
      return false;
    }
    if ('url' in reason && typeof reason.url === 'string' && isTossPaymentsError(reason.url)) {
      event.preventDefault();
      return false;
    }
    if ('stack' in reason && typeof reason.stack === 'string' && isTossPaymentsError(reason.stack)) {
      event.preventDefault();
      return false;
    }
  }
});

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)

