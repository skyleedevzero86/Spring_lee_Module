import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '10s', target: 10 },
    { duration: '20s', target: 30 },
    { duration: '10s', target: 0 },
  ],
};

const BASE = __ENV.BASE_URL || 'http://127.0.0.1:8088';

export default function () {
  const login = http.post(`${BASE}/auth/login`, JSON.stringify({
    username: 'user',
    password: 'user123',
  }), { headers: { 'Content-Type': 'application/json' } });
  const token = login.json('token');
  http.post(`${BASE}/chat/send`, JSON.stringify({
    currentUserName: `c-${__VU}`,
    message: 'concurrency',
    mode: 'DIRECT',
  }), {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
  });
  sleep(0.5);
}
