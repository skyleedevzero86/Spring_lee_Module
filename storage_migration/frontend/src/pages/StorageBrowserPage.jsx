import { useState } from 'react';
import { api, formatBytes, formatDate, toQuery } from '../api.js';
import { Notice, ServerSelect, StatusBadge, useNotice, useStorageServers } from '../hooks.jsx';
import Pager, { usePaged } from '../components/Pager.jsx';

export default function StorageBrowserPage({ dbVersion }) {
  const [servers] = useStorageServers(dbVersion);
  const [serverId, setServerId] = useState('');
  const [prefix, setPrefix] = useState('');
  const [objects, setObjects] = useState([]);
  const [checked, setChecked] = useState([]);
  const [notice, run] = useNotice();
  const paged = usePaged(objects);

  const load = async (id = serverId) => {
    if (!id) return;
    const data = await run(() => api.get(`/api/files/storage-objects${toQuery({ storageServerId: id, prefix })}`));
    if (data) {
      setObjects(data);
      setChecked([]);
    }
  };

  const changeServer = (id) => {
    setServerId(id);
    setObjects([]);
    setChecked([]);
    load(id);
  };

  const toggle = (key) => setChecked((keys) => (keys.includes(key) ? keys.filter((value) => value !== key) : [...keys, key]));
  const toggleAll = (event) => setChecked(event.target.checked ? objects.map((object) => object.key) : []);

  const importSelected = async () => {
    const result = await run(
      () => api.post('/api/files/storage-objects/import', { storageServerId: serverId, keys: checked }),
      (data) => `${data.length}개를 DB에 등록했습니다. 파일 관리에서 DB 저장을 누르면 파일 내용까지 저장됩니다.`,
    );
    if (result) load();
  };

  const deleteSelected = async () => {
    if (!window.confirm(`선택한 ${checked.length}개를 스토리지에서 삭제합니다.
DB에 등록된 기록도 함께 삭제됩니다.`)) return;
    const result = await run(
      () => api.del('/api/files/storage-objects', { storageServerId: serverId, keys: checked }),
      (data) => `${data.deleted}개 삭제했습니다.`,
    );
    if (result) load();
  };

  return (
    <section className="page">
      <div className="panel">
        <h2>스토리지 파일 목록</h2>
        <Notice notice={notice} />
        <div className="toolbar">
          <ServerSelect servers={servers} value={serverId} onChange={changeServer} />
          <input value={prefix} onChange={(event) => setPrefix(event.target.value)} onKeyDown={(event) => event.key === 'Enter' && load()} placeholder="찾을 폴더 이름, 예: uploads/2026/" />
          <button onClick={() => load()} disabled={!serverId}>조회</button>
        </div>
        <table>
          <thead>
            <tr>
              <th><input type="checkbox" checked={objects.length > 0 && checked.length === objects.length} onChange={toggleAll} /></th>
              <th>파일</th><th>크기</th><th>수정 시각</th><th>DB 상태</th>
            </tr>
          </thead>
          <tbody>
            {paged.pageItems.map((object) => (
              <tr key={object.key}>
                <td><input type="checkbox" checked={checked.includes(object.key)} onChange={() => toggle(object.key)} /></td>
                <td>{object.name}<div className="sub mono">{object.key}</div></td>
                <td>{formatBytes(object.size)}</td>
                <td>{formatDate(object.lastModified)}</td>
                <td><StatusBadge status={object.status} /></td>
              </tr>
            ))}
            {objects.length === 0 && <tr><td colSpan="5" className="empty">{serverId ? '파일이 없습니다.' : '스토리지를 선택하세요.'}</td></tr>}
          </tbody>
        </table>
        <Pager paged={paged} />
        <div className="toolbar end">
          <span className="sub">선택 {checked.length}개</span>
          <button className="primary" onClick={importSelected} disabled={checked.length === 0}>선택 파일 DB에 등록</button>
          <button className="danger" onClick={deleteSelected} disabled={checked.length === 0}>선택 파일을 스토리지와 DB에서 삭제</button>
        </div>
      </div>
    </section>
  );
}
