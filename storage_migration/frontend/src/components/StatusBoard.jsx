import { useCallback, useEffect, useState } from 'react';
import { api, formatBytes, formatDate } from '../api.js';
import { ACTION_LABELS } from '../pages/AuditLogPage.jsx';
import StatChart from './StatChart.jsx';

const STATUS_NAMES = { CONNECTED: '연결됨', FAILED: '실패', UNTESTED: '미확인' };
const TYPE_NAMES = { MINIO_CONSOLE: 'MinIO 콘솔', MINIO: 'MinIO S3 API', S3: 'AWS S3' };
const TARGET_NAMES = { STORED_FILE: '파일', STORAGE_OBJECT: '스토리지 파일', STORAGE_SERVER: '스토리지 서버', DB_CONNECTION: 'DB 연결' };

const countBy = (items, key) => items.reduce((acc, item) => {
  const value = typeof key === 'function' ? key(item) : item[key];
  acc[value] = (acc[value] || 0) + 1;
  return acc;
}, {});

const latest = (items, key) => items.map((item) => item[key]).filter(Boolean).sort().at(-1);

const breakdown = (counts, names) => Object.entries(counts)
  .map(([key, value]) => `${names[key] || key} ${value}`)
  .join(' · ') || '-';

const fixedBars = (counts, names) => Object.keys(names).map((key) => ({ label: names[key], value: counts[key] || 0 }));

function lastSevenDays(items, key) {
  const days = Array.from({ length: 7 }, (_, index) => {
    const day = new Date();
    day.setHours(0, 0, 0, 0);
    day.setDate(day.getDate() - (6 - index));
    return day;
  });
  return days.map((day) => {
    const next = new Date(day);
    next.setDate(day.getDate() + 1);
    const value = items.filter((item) => {
      const at = item[key] ? new Date(item[key]) : null;
      return at && at >= day && at < next;
    }).length;
    return { label: `${day.getMonth() + 1}/${day.getDate()}`, value };
  });
}

async function loadBoard(tab, activeDb) {
  if (tab === 'db') {
    const connections = (await api.get('/api/db-connections')) || [];
    const status = countBy(connections, 'status');
    return {
      cards: [
        { label: '등록된 DB', value: `${connections.length}개`, sub: breakdown(countBy(connections, 'vendor'), {}) },
        { label: '연결 상태', value: `${status.CONNECTED || 0}개 연결됨`, sub: breakdown(status, STATUS_NAMES) },
        { label: '사용 중인 DB', value: activeDb ? activeDb.name : '없음', sub: activeDb ? `${activeDb.vendor} · ${activeDb.host}:${activeDb.port}` : 'DB를 골라 이 DB 사용을 누르세요' },
        { label: '마지막 연결 확인', value: formatDate(latest(connections, 'lastCheckedAt')) || '-', sub: '연결 테스트를 누른 시각' },
      ],
      chart: { type: 'bars', title: '연결 상태별 DB 수', unit: '개', labelHeader: '상태', valueHeader: 'DB 수', data: fixedBars(status, STATUS_NAMES) },
    };
  }
  if (!activeDb) {
    return { cards: [{ label: 'DB 연결', value: '미연결', sub: 'DB를 연결하면 이 메뉴의 상태가 나옵니다' }], chart: null };
  }
  if (tab === 'storage') {
    const servers = (await api.get('/api/storage-servers')) || [];
    const status = countBy(servers, 'status');
    return {
      cards: [
        { label: '등록된 스토리지', value: `${servers.length}개`, sub: breakdown(countBy(servers, 'type'), TYPE_NAMES) },
        { label: '연결 상태', value: `${status.CONNECTED || 0}개 연결됨`, sub: breakdown(status, STATUS_NAMES) },
        { label: '버킷', value: `${new Set(servers.map((server) => server.bucket)).size}개`, sub: servers.map((server) => server.bucket).slice(0, 3).join(' · ') || '-' },
        { label: '마지막 연결 확인', value: formatDate(latest(servers, 'lastCheckedAt')) || '-', sub: '연결 테스트를 누른 시각' },
      ],
      chart: { type: 'bars', title: '종류별 스토리지 수', unit: '개', labelHeader: '종류', valueHeader: '스토리지 수', data: fixedBars(countBy(servers, 'type'), TYPE_NAMES) },
    };
  }
  if (tab === 'files') {
    const files = (await api.get('/api/files')) || [];
    const status = countBy(files, 'status');
    const totalSize = files.reduce((sum, file) => sum + (file.size || 0), 0);
    return {
      cards: [
        { label: '등록된 파일', value: `${files.length}개`, sub: `전체 크기 ${formatBytes(totalSize)}` },
        { label: 'DB에 내용까지 저장', value: `${status.SAVED || 0}개`, sub: `스토리지에만 있음 ${status.UPLOADED || 0}개` },
        { label: '작업자', value: `${new Set(files.map((file) => file.worker).filter(Boolean)).size}명`, sub: '파일을 마지막으로 다룬 사람 기준' },
        { label: '마지막 업로드', value: formatDate(latest(files, 'uploadedAt')) || '-', sub: `마지막 DB 저장 ${formatDate(latest(files, 'savedAt')) || '-'}` },
      ],
      chart: { type: 'columns', title: '최근 7일 업로드 수', unit: '개', labelHeader: '날짜', valueHeader: '업로드 수', data: lastSevenDays(files, 'uploadedAt') },
    };
  }
  if (tab === 'browser') {
    const [servers, files] = await Promise.all([api.get('/api/storage-servers'), api.get('/api/files')]);
    const list = servers || [];
    const byServer = countBy(files || [], 'storageServerId');
    const cards = list.map((server) => ({
      label: `${server.name} · ${TYPE_NAMES[server.type] || server.type}`,
      value: `DB 등록 ${byServer[server.id] || 0}개`,
      sub: `버킷 ${server.bucket} · ${STATUS_NAMES[server.status] || server.status}`,
    }));
    return {
      cards: cards.length ? cards : [{ label: '스토리지', value: '없음', sub: '스토리지 서버를 먼저 등록하세요' }],
      chart: { type: 'bars', title: '스토리지별 DB 등록 파일 수', unit: '개', labelHeader: '스토리지', valueHeader: '파일 수', data: list.map((server) => ({ label: server.name, value: byServer[server.id] || 0 })) },
    };
  }
  if (tab === 'audit') {
    const logs = (await api.get('/api/audit-logs?limit=300')) || [];
    const today = new Date().toDateString();
    const todayCount = logs.filter((log) => log.workedAt && new Date(log.workedAt).toDateString() === today).length;
    return {
      cards: [
        { label: '최근 기록', value: `${logs.length}건`, sub: '최근 300건까지 집계' },
        { label: '오늘 작업', value: `${todayCount}건`, sub: `작업자 ${new Set(logs.map((log) => log.worker).filter(Boolean)).size}명` },
        { label: '대상별', value: `${Object.keys(countBy(logs, 'targetType')).length}종류`, sub: breakdown(countBy(logs, 'targetType'), TARGET_NAMES) },
        { label: '마지막 작업', value: formatDate(latest(logs, 'workedAt')) || '-', sub: logs[0] ? `${logs[0].worker || '-'} · ${ACTION_LABELS[logs[0].action] || logs[0].action}` : '-' },
      ],
      chart: { type: 'columns', title: '최근 7일 작업 수', unit: '건', labelHeader: '날짜', valueHeader: '작업 수', data: lastSevenDays(logs, 'workedAt') },
    };
  }
  return { cards: [], chart: null };
}

export default function StatusBoard({ tab, activeDb, reloadKey }) {
  const [board, setBoard] = useState({ cards: [], chart: null });
  const [error, setError] = useState(null);
  const [checkedAt, setCheckedAt] = useState(null);

  const refresh = useCallback(async () => {
    try {
      setBoard(await loadBoard(tab, activeDb));
      setError(null);
    } catch (cause) {
      setBoard({ cards: [], chart: null });
      setError(cause.message);
    }
    setCheckedAt(new Date());
  }, [tab, activeDb]);

  useEffect(() => {
    refresh();
  }, [refresh, reloadKey]);

  if (tab === 'docs') return null;

  return (
    <section className="status-board" aria-label="현재 상태">
      <div className="status-head">
        <strong>현재 상태</strong>
        <span className="sub">{checkedAt ? `${checkedAt.toLocaleTimeString('ko-KR')} 기준` : ''}</span>
        <button type="button" onClick={refresh}>새로 고침</button>
      </div>
      {error && <div className="notice notice-error">{error}</div>}
      <div className="status-cards">
        {board.cards.map((card) => (
          <div key={card.label} className="status-card">
            <span className="status-label">{card.label}</span>
            <span className="status-value">{card.value}</span>
            <span className="status-sub">{card.sub}</span>
          </div>
        ))}
      </div>
      <StatChart key={tab} chart={board.chart} />
    </section>
  );
}
