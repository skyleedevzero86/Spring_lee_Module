import { useCallback, useEffect, useState } from 'react';
import { api, formatDate, toQuery } from '../api.js';
import { Notice, useNotice } from '../hooks.jsx';
import Pager, { usePaged } from '../components/Pager.jsx';

export const ACTION_LABELS = {
  STORAGE_SERVER_C: '스토리지 등록',
  STORAGE_SERVER_U: '스토리지 수정',
  STORAGE_SERVER_D: '스토리지 삭제',
  STORAGE_SERVER_TEST: '스토리지 연결 테스트',
  STORED_FILE_C: '파일 정보 등록',
  STORED_FILE_U: '파일 정보 수정',
  STORED_FILE_D: '파일 정보 삭제',
  STORED_FILE_SAVE: '파일 내용 저장',
  STORAGE_OBJECT_DELETE: '스토리지 파일 삭제',
  DB_CONNECTION_ACTIVATE: 'DB 연결 사용',
};

export default function AuditLogPage({ dbVersion }) {
  const [targetType, setTargetType] = useState('');
  const [worker, setWorker] = useState('');
  const [targetId, setTargetId] = useState('');
  const [logs, setLogs] = useState([]);
  const [notice, run] = useNotice();
  const paged = usePaged(logs);

  const load = useCallback(async () => {
    const data = await run(() => api.get(`/api/audit-logs${toQuery({ targetType, worker, targetId, limit: 300 })}`));
    if (data) setLogs(data);
  }, [run, targetType, worker, targetId]);

  useEffect(() => {
    load();
  }, [dbVersion]);

  return (
    <section className="page">
      <div className="panel">
        <h2>작업 로그</h2>
        <p className="hint">누가 언제 무엇을 했는지 남긴 기록입니다.</p>
        <Notice notice={notice} />
        <div className="toolbar">
          <select value={targetType} onChange={(event) => setTargetType(event.target.value)}>
            <option value="">전체 대상</option>
            <option value="STORED_FILE">파일</option>
            <option value="STORAGE_OBJECT">스토리지 파일</option>
            <option value="STORAGE_SERVER">스토리지 서버</option>
            <option value="DB_CONNECTION">DB 연결</option>
          </select>
          <input value={worker} onChange={(event) => setWorker(event.target.value)} placeholder="작업자" />
          <input value={targetId} onChange={(event) => setTargetId(event.target.value)} placeholder="대상 번호" />
          <button onClick={load}>조회</button>
        </div>
        <table>
          <thead>
            <tr><th>일시</th><th>작업자</th><th>IP</th><th>작업</th><th>대상</th><th>내용</th></tr>
          </thead>
          <tbody>
            {paged.pageItems.map((log) => (
              <tr key={log.id}>
                <td>{formatDate(log.workedAt)}</td>
                <td>{log.worker}</td>
                <td className="mono">{log.workIp}</td>
                <td>{ACTION_LABELS[log.action] || log.action}</td>
                <td>{log.targetType}<div className="sub mono">{log.targetId}</div></td>
                <td>{log.detail}</td>
              </tr>
            ))}
            {logs.length === 0 && <tr><td colSpan="6" className="empty">로그가 없습니다.</td></tr>}
          </tbody>
        </table>
        <Pager paged={paged} />
      </div>
    </section>
  );
}
