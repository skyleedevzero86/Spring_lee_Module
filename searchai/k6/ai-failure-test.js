import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 2,
  iterations: 10,
};

const AI = __ENV.AI_HTTP || 'http://127.0.0.1:9091';
const BASE = __ENV.BASE_URL || 'http://127.0.0.1:8088';

export default function () {
  http.post(`${AI}/admin/mock-ai/mode/HTTP_503`);
  const login = http.post(`${BASE}/auth/login`, JSON.stringify({
    username: 'admin',
    password: 'admin123',
  }), { headers: { 'Content-Type': 'application/json' } });
  const token = login.json('token');

  const chat = http.post(`${BASE}/chat/send`, JSON.stringify({
    currentUserName: 'fault',
    message: 'hello under fault',
    mode: 'DIRECT',
  }), {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
  });

  check(chat, { 'handled': (r) => r.status < 500 });
  sleep(1);
}

export function teardown() {
  http.post(`${__ENV.AI_HTTP || 'http://127.0.0.1:9091'}/admin/mock-ai/mode/NORMAL`);
}
