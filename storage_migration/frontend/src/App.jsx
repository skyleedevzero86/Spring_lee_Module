import { Suspense, lazy, useCallback, useEffect, useState } from 'react';
import { api, getWorker, saveWorker } from './api.js';
import DbConnectionPage from './pages/DbConnectionPage.jsx';
import StorageServerPage from './pages/StorageServerPage.jsx';
import FilePage from './pages/FilePage.jsx';
import StorageBrowserPage from './pages/StorageBrowserPage.jsx';
import AuditLogPage from './pages/AuditLogPage.jsx';

const ApiDocsPage = lazy(() => import('./pages/ApiDocsPage.jsx'));

const TABS = [
  { id: 'db', label: 'DB 연결' },
  { id: 'storage', label: '스토리지 서버' },
  { id: 'files', label: '파일 관리' },
  { id: 'browser', label: '스토리지 탐색' },
  { id: 'audit', label: '작업 로그' },
  { id: 'docs', label: 'API 문서' },
];

export default function App() {
  const [tab, setTab] = useState('db');
  const [worker, setWorker] = useState(getWorker());
  const [activeDb, setActiveDb] = useState(null);
  const [dbVersion, setDbVersion] = useState(0);

  const refreshActive = useCallback(() => {
    api.get('/api/db-connections/active')
      .then((data) => setActiveDb(data || null))
      .catch(() => setActiveDb(null));
  }, []);

  useEffect(() => {
    refreshActive();
  }, [refreshActive, dbVersion]);

  const changeWorker = (value) => {
    setWorker(value);
    saveWorker(value.trim());
  };

  const dbChanged = () => setDbVersion((value) => value + 1);

  return (
    <div className="app">
      <header className="topbar">
        <h1><button type="button" className="home-link" onClick={() => setTab('db')}>Storage Migration</button></h1>
        <div className={`db-indicator ${activeDb ? 'on' : 'off'}`}>
          <span className="dot" />
          {activeDb ? `DB 연결됨 · ${activeDb.name} · ${activeDb.vendor}` : 'DB 미연결'}
        </div>
        <label className="worker">
          작업자
          <input value={worker} onChange={(event) => changeWorker(event.target.value)} placeholder="이름 또는 사번" />
        </label>
      </header>
      <nav className="tabs">
        {TABS.map((item) => (
          <button key={item.id} className={tab === item.id ? 'active' : ''} onClick={() => setTab(item.id)}>
            {item.label}
          </button>
        ))}
      </nav>
      <h2 className="page-title">{TABS.find((item) => item.id === tab)?.label}</h2>
      <main>
        {!worker.trim() && <div className="notice notice-warn">작업자를 입력하면 작업 로그에 이름이 남습니다.<br />비워 두면 익명으로 기록됩니다.</div>}
        {tab === 'db' && <DbConnectionPage onChanged={dbChanged} />}
        {tab === 'storage' && <StorageServerPage dbVersion={dbVersion} />}
        {tab === 'files' && <FilePage dbVersion={dbVersion} />}
        {tab === 'browser' && <StorageBrowserPage dbVersion={dbVersion} />}
        {tab === 'audit' && <AuditLogPage dbVersion={dbVersion} />}
        {tab === 'docs' && (
          <Suspense fallback={<p className="empty">API 문서를 불러오는 중…</p>}>
            <ApiDocsPage />
          </Suspense>
        )}
      </main>
    </div>
  );
}
