import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 3,
  duration: '20s',
};

const BASE = __ENV.BASE_URL || 'http://127.0.0.1:8088';

export default function () {
  const login = http.post(`${BASE}/auth/login`, JSON.stringify({
    username: 'admin',
    password: 'admin123',
  }), { headers: { 'Content-Type': 'application/json' } });
  const token = login.json('token');
  const sessionId = `sse-${__VU}-${Date.now()}`;

  const res = http.get(`${BASE}/sse/connect?sessionId=${sessionId}&token=${token}`, {
    timeout: '15s',
  });
  check(res, { 'sse connected': (r) => r.status === 200 || r.status === 0 });
  sleep(2);
}
