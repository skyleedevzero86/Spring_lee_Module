import { useCallback, useEffect, useState } from 'react';
import { api, formatDate } from '../api.js';
import { Notice, StatusBadge, useNotice } from '../hooks.jsx';
import HelpHeading from '../components/HelpHeading.jsx';

const DEFAULT_PORTS = { ORACLE: 1521, MYSQL: 3306, POSTGRESQL: 5432 };
const DATABASE_HINT = { ORACLE: '서비스 이름, 예: FREEPDB1', MYSQL: 'DB 이름', POSTGRESQL: 'DB 이름' };
const EMPTY = { id: null, name: '', vendor: 'ORACLE', host: 'localhost', port: '', database: '', username: '', password: '' };

export default function DbConnectionPage({ onChanged }) {
  const [connections, setConnections] = useState([]);
  const [form, setForm] = useState(EMPTY);
  const [notice, run] = useNotice();

  const load = useCallback(() => {
    run(() => api.get('/api/db-connections')).then((data) => data && setConnections(data));
  }, [run]);

  useEffect(() => {
    load();
  }, [load]);

  const update = (field) => (event) => setForm({ ...form, [field]: event.target.value });

  const select = (connection) => {
    setForm({ ...EMPTY, ...connection, port: connection.port, username: '', password: '' });
  };

  const body = () => ({
    name: form.name,
    vendor: form.vendor,
    host: form.host,
    port: form.port === '' ? null : Number(form.port),
    database: form.database,
    username: form.username,
    password: form.password,
  });

  const save = async () => {
    const saved = await run(
      () => (form.id ? api.put(`/api/db-connections/${form.id}`, body()) : api.post('/api/db-connections', body())),
      '저장했습니다. 연결 테스트를 눌러 확인하세요.',
    );
    if (saved) {
      select(saved);
      load();
    }
  };

  const test = async () => {
    const result = await run(() => api.post(`/api/db-connections/${form.id}/test`),
      (data) => data.lastMessage);
    if (result) {
      select(result);
      load();
    }
  };

  const activate = async () => {
    const result = await run(() => api.post(`/api/db-connections/${form.id}/activate`),
      (data) => `${data.name} DB를 사용합니다. 테이블과 프로시저를 준비했습니다.`);
    if (result) {
      load();
      onChanged();
    }
  };

  return (
    <section className="page two-column">
      <div className="panel">
        <h2>등록된 DB</h2>
        <Notice notice={notice} />
        <table>
          <thead>
            <tr><th>이름</th><th>종류</th><th>접속 정보</th><th>아이디</th><th>상태</th><th>확인 시각</th></tr>
          </thead>
          <tbody>
            {connections.map((connection) => (
              <tr key={connection.id} className={form.id === connection.id ? 'selected' : ''} onClick={() => select(connection)}>
                <td>{connection.name}{connection.active && <span className="badge badge-ok inline">사용 중</span>}</td>
                <td>{connection.vendor}</td>
                <td>{connection.host}:{connection.port}/{connection.database}</td>
                <td>{connection.maskedUsername}</td>
                <td><StatusBadge status={connection.status} /></td>
                <td>{formatDate(connection.lastCheckedAt)}</td>
              </tr>
            ))}
            {connections.length === 0 && <tr><td colSpan="6" className="empty">등록된 DB가 없습니다.</td></tr>}
          </tbody>
        </table>
      </div>
      <div className="panel">
        <HelpHeading title={form.id ? 'DB 수정' : '새 DB 등록'}>
          아이디와 비밀번호는 서버에서 암호화해 저장합니다.<br />저장한 뒤 연결 테스트를 누르세요.<br />연결되면 이 DB 사용을 누릅니다.
        </HelpHeading>
        <div className="form">
          <label>이름<input value={form.name} onChange={update('name')} placeholder="예: 개발 Oracle" /></label>
          <label>DB 종류
            <select value={form.vendor} onChange={update('vendor')}>
              <option value="ORACLE">Oracle</option>
              <option value="MYSQL">MySQL</option>
              <option value="POSTGRESQL">PostgreSQL</option>
            </select>
          </label>
          <label>호스트<input value={form.host} onChange={update('host')} /></label>
          <label>포트<input value={form.port} onChange={update('port')} placeholder={String(DEFAULT_PORTS[form.vendor])} /></label>
          <label>DB 정보<input value={form.database} onChange={update('database')} placeholder={DATABASE_HINT[form.vendor]} /></label>
          <label>아이디<input value={form.username} onChange={update('username')} placeholder={form.id ? '바꿀 때만 입력' : ''} autoComplete="off" /></label>
          <label>비밀번호<input type="password" value={form.password} onChange={update('password')} placeholder={form.id ? '바꿀 때만 입력' : ''} autoComplete="new-password" /></label>
          <div className="actions end">
            <button className="go" onClick={save}>저장</button>
            <button onClick={test} disabled={!form.id}>연결 테스트</button>
            <button onClick={activate} disabled={!form.id || form.status !== 'CONNECTED'}>이 DB 사용</button>
            <button onClick={() => setForm(EMPTY)}>새로 입력</button>
          </div>
        </div>
      </div>
    </section>
  );
}
