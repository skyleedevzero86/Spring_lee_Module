const WORKER_KEY = 'storage-migration.worker';

export function getWorker() {
  try {
    return localStorage.getItem(WORKER_KEY) || '';
  } catch {
    return '';
  }
}

export function saveWorker(value) {
  try {
    localStorage.setItem(WORKER_KEY, value);
  } catch {
    return;
  }
}

async function request(method, url, body, isForm = false) {
  const headers = {};
  const worker = getWorker();
  if (worker) {
    headers['X-Worker'] = encodeURIComponent(worker);
  }
  let payload;
  if (isForm) {
    payload = body;
  } else if (body !== undefined) {
    headers['Content-Type'] = 'application/json';
    payload = JSON.stringify(body);
  }
  let response;
  try {
    response = await fetch(url, { method, headers, body: payload });
  } catch {
    throw new Error('서버에 연결할 수 없습니다. 백엔드 8080 포트가 켜져 있는지 확인하세요.');
  }
  const text = await response.text();
  let json = null;
  if (text) {
    try {
      json = JSON.parse(text);
    } catch {
      json = null;
    }
  }
  if (!response.ok || (json && json.success === false)) {
    const error = json?.error;
    const message = error
      ? [error.message, error.detail].filter(Boolean).join(' - ')
      : `요청을 처리하지 못했습니다. 응답 코드는 ${response.status}입니다.`;
    throw new Error(message);
  }
  return json?.data;
}

export const api = {
  get: (url) => request('GET', url),
  post: (url, body) => request('POST', url, body),
  put: (url, body) => request('PUT', url, body),
  del: (url, body) => request('DELETE', url, body),
  upload: (url, form) => request('POST', url, form, true),
};

export function toQuery(params) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.append(key, value);
    }
  });
  const text = search.toString();
  return text ? `?${text}` : '';
}

export function formatBytes(bytes) {
  if (bytes === null || bytes === undefined) return '-';
  if (bytes < 1024) return `${bytes} B`;
  const units = ['KB', 'MB', 'GB', 'TB'];
  let value = bytes / 1024;
  let unit = 0;
  while (value >= 1024 && unit < units.length - 1) {
    value /= 1024;
    unit += 1;
  }
  return `${value.toFixed(1)} ${units[unit]}`;
}

export function formatDate(value) {
  if (!value) return '-';
  return new Date(value).toLocaleString('ko-KR', { hour12: false });
}
