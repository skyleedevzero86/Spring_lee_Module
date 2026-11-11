import { useEffect, useState } from 'react';
import { api, formatDate } from '../api.js';
import { Notice, StatusBadge, useNotice, useStorageServers } from '../hooks.jsx';
import Pager, { usePaged } from '../components/Pager.jsx';
import HelpHeading from '../components/HelpHeading.jsx';

const DEFAULT_ENDPOINTS = { MINIO_CONSOLE: 'http://localhost:9001', MINIO: 'http://localhost:9000', S3: '' };
const TYPE_LABELS = { MINIO_CONSOLE: 'MinIO 콘솔 9001', MINIO: 'MinIO S3 API 9000', S3: 'AWS S3' };
const EMPTY = { id: null, name: '', type: 'MINIO_CONSOLE', endpoint: DEFAULT_ENDPOINTS.MINIO_CONSOLE, region: '', bucket: '', accessKey: '', secretKey: '' };

export default function StorageServerPage({ dbVersion }) {
  const [reload, setReload] = useState(0);
  const [servers, loadError] = useStorageServers(`${dbVersion}-${reload}`);
  const [form, setForm] = useState(EMPTY);
  const [createBucket, setCreateBucket] = useState(false);
  const [notice, run, setNotice] = useNotice();
  const paged = usePaged(servers);

  useEffect(() => {
    if (loadError) setNotice({ type: 'error', text: loadError });
  }, [loadError, setNotice]);

  const update = (field) => (event) => setForm({ ...form, [field]: event.target.value });

  const select = (server) => setForm({ ...EMPTY, ...server, endpoint: server.endpoint || '', region: server.region || '', accessKey: '', secretKey: '' });

  const changeType = (event) => {
    const type = event.target.value;
    setForm({
      ...form,
      type,
      endpoint: DEFAULT_ENDPOINTS[type],
      region: type === 'S3' ? form.region || 'ap-northeast-2' : form.region,
    });
  };

  const body = () => ({
    name: form.name,
    type: form.type,
    endpoint: form.endpoint,
    region: form.region,
    bucket: form.bucket,
    accessKey: form.accessKey,
    secretKey: form.secretKey,
  });

  const save = async () => {
    const saved = await run(
      () => (form.id ? api.put(`/api/storage-servers/${form.id}`, body()) : api.post('/api/storage-servers', body())),
      '저장했습니다. 연결 테스트를 눌러 확인하세요.',
    );
    if (saved) {
      select(saved);
      setReload((value) => value + 1);
    }
  };

  const test = async () => {
    const result = await run(
      () => api.post(`/api/storage-servers/${form.id}/test?createBucket=${createBucket}`),
      (data) => (data.status === 'CONNECTED' ? data.lastMessage : `연결 실패: ${data.lastMessage}`),
    );
    if (result) {
      select(result);
      setReload((value) => value + 1);
    }
  };

  return (
    <section className="page two-column">
      <div className="panel">
        <h2>스토리지 서버</h2>
        <Notice notice={notice} />
        <table>
          <thead>
            <tr><th>이름</th><th>종류</th><th>주소</th><th>버킷</th><th>아이디</th><th>상태</th><th>확인 시각</th></tr>
          </thead>
          <tbody>
            {paged.pageItems.map((server) => (
              <tr key={server.id} className={form.id === server.id ? 'selected' : ''} onClick={() => select(server)}>
                <td>{server.name}</td>
                <td>{TYPE_LABELS[server.type] || server.type}</td>
                <td>{server.endpoint || `AWS ${server.region}`}</td>
                <td>{server.bucket}</td>
                <td>{server.maskedAccessKey}</td>
                <td><StatusBadge status={server.status} /></td>
                <td>{formatDate(server.lastCheckedAt)}</td>
              </tr>
            ))}
            {servers.length === 0 && <tr><td colSpan="7" className="empty">등록된 스토리지가 없습니다.<br />DB를 먼저 연결하세요.</td></tr>}
          </tbody>
        </table>
        <Pager paged={paged} />
      </div>
      <div className="panel">
        <HelpHeading title={form.id ? '스토리지 수정' : '새 스토리지 등록'}>
          MinIO 콘솔 9001 포트는 콘솔 로그인 아이디와 비밀번호를 넣습니다.<br />S3 API 9000 포트를 열지 않아도 됩니다.<br />아이디와 비밀번호는 암호화해 DB에 저장합니다.<br />등록과 수정은 저장 프로시저로 처리합니다.
        </HelpHeading>
        <div className="form">
          <label>이름<input value={form.name} onChange={update('name')} placeholder="예: 로컬 MinIO" /></label>
          <label>종류
            <select value={form.type} onChange={changeType}>
              <option value="MINIO_CONSOLE">MinIO 콘솔 9001</option>
              <option value="MINIO">MinIO S3 API 9000</option>
              <option value="S3">AWS S3</option>
            </select>
          </label>
          <label>접속 주소
            <input value={form.endpoint} onChange={update('endpoint')} placeholder={form.type === 'S3' ? '비우면 AWS 기본 주소' : DEFAULT_ENDPOINTS[form.type]} />
          </label>
          <label>리전<input value={form.region} onChange={update('region')} placeholder={form.type === 'S3' ? 'ap-northeast-2' : '비우면 us-east-1'} /></label>
          <label>버킷<input value={form.bucket} onChange={update('bucket')} placeholder="소문자·숫자·하이픈" /></label>
          <label>아이디<input value={form.accessKey} onChange={update('accessKey')} placeholder={form.id ? '바꿀 때만 입력' : ''} autoComplete="off" /></label>
          <label>비밀번호<input type="password" value={form.secretKey} onChange={update('secretKey')} placeholder={form.id ? '바꿀 때만 입력' : ''} autoComplete="new-password" /></label>
          <label className="check"><input type="checkbox" checked={createBucket} onChange={(event) => setCreateBucket(event.target.checked)} /> 버킷이 없으면 만들기</label>
          <div className="actions end">
            <button className="go" onClick={save}>저장</button>
            <button onClick={test} disabled={!form.id}>연결 테스트</button>
            <button onClick={() => setForm(EMPTY)}>새로 입력</button>
          </div>
        </div>
      </div>
    </section>
  );
}
