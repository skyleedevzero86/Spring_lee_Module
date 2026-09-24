import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 5,
  duration: '30s',
  thresholds: {
    http_req_failed: ['rate<0.1'],
    http_req_duration: ['p(95)<2000'],
  },
};

const BASE = __ENV.BASE_URL || 'http://127.0.0.1:8088';

export default function () {
  const login = http.post(`${BASE}/auth/login`, JSON.stringify({
    username: 'user',
    password: 'user123',
  }), { headers: { 'Content-Type': 'application/json' } });

  check(login, { 'login 200': (r) => r.status === 200 });
  const token = login.json('token');

  const chat = http.post(`${BASE}/chat/send`, JSON.stringify({
    currentUserName: `k6-${__VU}`,
    message: 'hello from k6',
    mode: 'DIRECT',
    routingMode: 'AUTO',
  }), {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
  });

  check(chat, { 'chat accepted': (r) => r.status === 200 || r.status === 202 });
  sleep(1);
}
