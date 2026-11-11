import { useCallback, useEffect, useState } from 'react';
import { api } from './api.js';

export function useNotice() {
  const [notice, setNotice] = useState(null);
  const run = useCallback(async (work, success) => {
    setNotice(null);
    try {
      const result = await work();
      if (success) {
        setNotice({ type: 'ok', text: typeof success === 'function' ? success(result) : success });
      }
      return result;
    } catch (error) {
      setNotice({ type: 'error', text: error.message });
      return undefined;
    }
  }, []);
  return [notice, run, setNotice];
}

export function Notice({ notice }) {
  if (!notice) return null;
  return <div className={`notice notice-${notice.type}`}>{notice.text}</div>;
}

export function useStorageServers(reloadKey) {
  const [servers, setServers] = useState([]);
  const [error, setError] = useState(null);
  useEffect(() => {
    let alive = true;
    api.get('/api/storage-servers')
      .then((data) => {
        if (alive) {
          setServers(data || []);
          setError(null);
        }
      })
      .catch((cause) => alive && setError(cause.message));
    return () => {
      alive = false;
    };
  }, [reloadKey]);
  return [servers, error];
}

const STATUS_LABELS = {
  CONNECTED: ['연결됨', 'ok'],
  FAILED: ['실패', 'error'],
  UNTESTED: ['미확인', 'muted'],
  UPLOADED: ['스토리지만', 'warn'],
  SAVED: ['DB 저장됨', 'ok'],
};

export function StatusBadge({ status }) {
  if (!status) return <span className="badge badge-muted">미등록</span>;
  const [label, tone] = STATUS_LABELS[status] || [status, 'muted'];
  return <span className={`badge badge-${tone}`}>{label}</span>;
}

export function ServerSelect({ servers, value, onChange, allowAll = false }) {
  return (
    <select value={value} onChange={(event) => onChange(event.target.value)}>
      {allowAll ? <option value="">전체 스토리지</option> : <option value="">스토리지 선택</option>}
      {servers.map((server) => (
        <option key={server.id} value={server.id}>
          {server.name} · {server.type === 'MINIO_CONSOLE' ? 'MinIO 콘솔' : server.type} · {server.bucket}
        </option>
      ))}
    </select>
  );
}
