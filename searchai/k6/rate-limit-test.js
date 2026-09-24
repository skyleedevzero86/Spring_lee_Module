import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    burst: {
      executor: 'constant-arrival-rate',
      rate: 40,
      timeUnit: '1s',
      duration: '20s',
      preAllocatedVUs: 20,
      maxVUs: 50,
    },
  },
};

const BASE = __ENV.BASE_URL || 'http://127.0.0.1:8088';

export default function () {
  const login = http.post(`${BASE}/auth/login`, JSON.stringify({
    username: 'user',
    password: 'user123',
  }), { headers: { 'Content-Type': 'application/json' } });
  const token = login.json('token');

  const chat = http.post(`${BASE}/chat/send`, JSON.stringify({
    currentUserName: 'rate-user',
    message: 'burst',
    mode: 'DIRECT',
  }), {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
  });

  check(chat, {
    'ok or rate-limited': (r) => r.status === 200 || r.status === 202 || r.status === 429 || r.status === 403,
  });
  sleep(0.1);
}
