import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 1,
  iterations: 40,
};

const BASE = __ENV.BASE_URL || 'http://127.0.0.1:8088';

export default function () {
  const login = http.post(`${BASE}/auth/login`, JSON.stringify({
    username: 'user',
    password: 'user123',
  }), { headers: { 'Content-Type': 'application/json' } });
  const token = login.json('token');

  const chat = http.post(`${BASE}/chat/send`, JSON.stringify({
    currentUserName: 'budget-user',
    message: '양자컴퓨터 원리를 길게 설명해줘 ' + __ITER,
    mode: 'DIRECT',
  }), {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
  });

  check(chat, { 'sent': (r) => r.status < 500 });
  sleep(0.2);
}
