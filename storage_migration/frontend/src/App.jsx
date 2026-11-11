import { Suspense, lazy, useCallback, useEffect, useState } from 'react';
import { api, getWorker, saveWorker } from './api.js';
import DbConnectionPage from './pages/DbConnectionPage.jsx';
import StorageServerPage from './pages/StorageServerPage.jsx';
import FilePage from './pages/FilePage.jsx';
import StorageBrowserPage from './pages/StorageBrowserPage.jsx';
import AuditLogPage from './pages/AuditLogPage.jsx';
import StatusBoard from './components/StatusBoard.jsx';

const ApiDocsPage = lazy(() => import('./pages/ApiDocsPage.jsx'));

const TABS = [
  { id: 'db', label: 'DB 연결' },
  { id: 'storage', label: '스토리지 서버', needsDb: true },
  { id: 'files', label: '파일 관리', needsDb: true },
  { id: 'browser', label: '스토리지 탐색', needsDb: true },
  { id: 'audit', label: '작업 로그', needsDb: true },
  { id: 'docs', label: 'API 문서' },
];

function NeedDbConnection({ onGo }) {
  return (
    <section className="page">
      <div className="panel need-db">
        <p>연결된 DB가 없습니다.</p>
        <p>DB 연결 탭에서 DB를 등록하고 연결 테스트를 한 뒤 이 DB 사용을 누르세요.</p>
        <div className="actions">
          <button type="button" className="go" onClick={onGo}>DB 연결로 가기</button>
        </div>
      </div>
    </section>
  );
}

export default function App() {
  const [tab, setTab] = useState('db');
  const [worker, setWorker] = useState(getWorker());
  const [activeDb, setActiveDb] = useState(null);
  const [activeChecked, setActiveChecked] = useState(false);
  const [dbVersion, setDbVersion] = useState(0);

  const refreshActive = useCallback(() => {
    api.get('/api/db-connections/active')
      .then((data) => setActiveDb(data || null))
      .catch(() => setActiveDb(null))
      .finally(() => setActiveChecked(true));
  }, []);

  useEffect(() => {
    refreshActive();
  }, [refreshActive, dbVersion]);

  const changeWorker = (value) => {
    setWorker(value);
    saveWorker(value.trim());
  };

  const dbChanged = () => setDbVersion((value) => value + 1);

  const current = TABS.find((item) => item.id === tab);
  const blocked = current?.needsDb && !activeDb;

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
      <StatusBoard tab={tab} activeDb={activeDb} reloadKey={dbVersion} />
      <h2 className="page-title">{current?.label}</h2>
      <main>
        {tab === 'db' && <DbConnectionPage onChanged={dbChanged} />}
        {blocked && activeChecked && <NeedDbConnection onGo={() => setTab('db')} />}
        {!blocked && tab === 'storage' && <StorageServerPage dbVersion={dbVersion} />}
        {!blocked && tab === 'files' && <FilePage dbVersion={dbVersion} />}
        {!blocked && tab === 'browser' && <StorageBrowserPage dbVersion={dbVersion} />}
        {!blocked && tab === 'audit' && <AuditLogPage dbVersion={dbVersion} />}
        {tab === 'docs' && (
          <Suspense fallback={<p className="empty">API 문서를 불러오는 중…</p>}>
            <ApiDocsPage />
          </Suspense>
        )}
      </main>
    </div>
  );
}
