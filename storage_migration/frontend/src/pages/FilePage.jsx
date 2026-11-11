import { useCallback, useEffect, useState } from 'react';
import { api, formatBytes, formatDate, toQuery } from '../api.js';
import { Notice, ServerSelect, StatusBadge, useNotice, useStorageServers } from '../hooks.jsx';
import FilePick from '../components/FilePick.jsx';
import Pager, { usePaged } from '../components/Pager.jsx';

export default function FilePage({ dbVersion }) {
  const [servers] = useStorageServers(dbVersion);
  const [serverId, setServerId] = useState('');
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState('');
  const [files, setFiles] = useState([]);
  const [checked, setChecked] = useState([]);
  const [selected, setSelected] = useState(null);
  const [uploads, setUploads] = useState([]);
  const [uploadServerId, setUploadServerId] = useState('');
  const [saveOnUpload, setSaveOnUpload] = useState(true);
  const [deleteFromStorage, setDeleteFromStorage] = useState(false);
  const [notice, run] = useNotice();
  const paged = usePaged(files);

  const search = useCallback(async () => {
    const data = await run(() => api.get(`/api/files${toQuery({ keyword, status, storageServerId: serverId })}`));
    if (data) {
      setFiles(data);
      setChecked((ids) => ids.filter((id) => data.some((file) => file.id === id)));
    }
  }, [run, keyword, status, serverId]);

  useEffect(() => {
    search();
  }, [dbVersion]);

  const reloadQuietly = async () => {
    const data = await api.get(`/api/files${toQuery({ keyword, status, storageServerId: serverId })}`).catch(() => null);
    if (data) setFiles(data);
  };

  useEffect(() => {
    if (uploadServerId && servers.some((server) => server.id === uploadServerId)) return;
    const preferred = servers.find((server) => server.status === 'CONNECTED') || servers[0];
    setUploadServerId(preferred ? preferred.id : '');
  }, [servers, uploadServerId]);

  const upload = async () => {
    if (!uploadServerId) {
      await run(() => Promise.reject(new Error('올릴 스토리지가 없습니다. 스토리지 서버 탭에서 먼저 등록하세요.')));
      return;
    }
    const form = new FormData();
    uploads.forEach((file) => form.append('files', file));
    const result = await run(
      () => api.upload(`/api/files${toQuery({ storageServerId: uploadServerId, saveToDb: saveOnUpload })}`, form),
      (data) => `${data.length}개를 올렸습니다.${saveOnUpload ? ' 파일 내용도 DB에 저장했습니다.' : ''}`,
    );
    if (result) {
      setUploads([]);
      reloadQuietly();
    }
  };

  const remove = async () => {
    const message = deleteFromStorage
      ? `선택한 ${checked.length}개를 DB와 스토리지에서 모두 삭제합니다.`
      : `선택한 ${checked.length}개의 DB 기록을 삭제합니다.
스토리지 파일은 남습니다.`;
    if (!window.confirm(message)) return;
    const result = await run(() => api.del('/api/files', { ids: checked, deleteFromStorage }), (data) => `${data.deleted}개 삭제했습니다.`);
    if (result) {
      if (selected && checked.includes(selected.id)) setSelected(null);
      setChecked([]);
      reloadQuietly();
    }
  };

  const toggle = (id) => setChecked((ids) => (ids.includes(id) ? ids.filter((value) => value !== id) : [...ids, id]));
  const toggleAll = (event) => setChecked(event.target.checked ? files.map((file) => file.id) : []);

  const refreshSelected = (file) => {
    setSelected(file);
    setFiles((list) => list.map((item) => (item.id === file.id ? file : item)));
  };

  return (
    <section className="page two-column">
      <div className="panel">
        <h2>파일 목록</h2>
        <Notice notice={notice} />
        <div className="toolbar">
          <ServerSelect servers={servers} value={serverId} onChange={setServerId} allowAll />
          <input value={keyword} onChange={(event) => setKeyword(event.target.value)} onKeyDown={(event) => event.key === 'Enter' && search()} placeholder="파일명·제목·태그·설명 검색" />
          <select value={status} onChange={(event) => setStatus(event.target.value)}>
            <option value="">전체 상태</option>
            <option value="UPLOADED">스토리지만</option>
            <option value="SAVED">DB 저장됨</option>
          </select>
          <button onClick={search}>검색</button>
        </div>
        <div className="toolbar upload-row">
          <label className="upload-target">올릴 스토리지
            <ServerSelect servers={servers} value={uploadServerId} onChange={setUploadServerId} />
          </label>
          <FilePick files={uploads} multiple onChange={setUploads} />
          <label className="check"><input type="checkbox" checked={saveOnUpload} onChange={(event) => setSaveOnUpload(event.target.checked)} /> 파일 내용도 DB에 저장</label>
          <button className="primary" onClick={upload} disabled={uploads.length === 0 || !uploadServerId}>올리기</button>
        </div>
        {servers.length === 0 && <p className="hint">올릴 스토리지가 없습니다. 스토리지 서버 탭에서 먼저 등록하세요.</p>}
        <table>
          <thead>
            <tr>
              <th><input type="checkbox" checked={files.length > 0 && checked.length === files.length} onChange={toggleAll} /></th>
              <th>파일명</th><th>크기</th><th>상태</th><th>작업자</th><th>수정 시각</th>
            </tr>
          </thead>
          <tbody>
            {paged.pageItems.map((file) => (
              <tr key={file.id} className={selected?.id === file.id ? 'selected' : ''}>
                <td><input type="checkbox" checked={checked.includes(file.id)} onChange={() => toggle(file.id)} /></td>
                <td><button className="link" onClick={() => setSelected(file)}>{file.originalName}</button>{file.title && <div className="sub">{file.title}</div>}</td>
                <td>{formatBytes(file.size)}</td>
                <td><StatusBadge status={file.status} /></td>
                <td>{file.worker || '-'}</td>
                <td>{formatDate(file.updatedAt)}</td>
              </tr>
            ))}
            {files.length === 0 && <tr><td colSpan="6" className="empty">파일이 없습니다.</td></tr>}
          </tbody>
        </table>
        <Pager paged={paged} />
        <div className="toolbar end">
          <label className="check"><input type="checkbox" checked={deleteFromStorage} onChange={(event) => setDeleteFromStorage(event.target.checked)} /> 스토리지 파일도 함께 삭제</label>
          <button className="danger" onClick={remove} disabled={checked.length === 0}>선택한 {checked.length}개 삭제</button>
        </div>
      </div>
      <div className="panel">
        {selected ? <FileDetail key={selected.id} file={selected} onChanged={refreshSelected} /> : <p className="empty">파일명을 누르면 상세 정보가 나옵니다.</p>}
      </div>
    </section>
  );
}

function FileDetail({ file, onChanged }) {
  const [title, setTitle] = useState(file.title || '');
  const [description, setDescription] = useState(file.description || '');
  const [tags, setTags] = useState((file.tags || []).join(', '));
  const [notice, run] = useNotice();

  const body = () => ({
    title,
    description,
    tags: tags.split(',').map((tag) => tag.trim()).filter(Boolean),
  });

  const update = async () => {
    const result = await run(() => api.put(`/api/files/${file.id}`, body()), '상세 정보를 수정했습니다.');
    if (result) onChanged(result);
  };

  const save = async () => {
    const result = await run(() => api.post(`/api/files/${file.id}/save`, body()), (data) => (
      data.verified
        ? `DB에 저장했습니다. 원본 ${formatBytes(data.storedBytes)}, DB ${formatBytes(data.databaseBytes)}로 내용이 같습니다.`
        : `DB에 저장했지만 내용이 다릅니다. 원본 ${data.storedBytes}바이트, DB ${data.databaseBytes}바이트입니다.`
    ));
    if (result) onChanged(result.file);
  };

  const [source, setSource] = useState('storage');
  const version = `v=${encodeURIComponent(file.updatedAt || '')}`;
  const contentUrl = `/api/files/${file.id}/content?${version}`;
  const databaseUrl = `/api/files/${file.id}/db-content?${version}`;
  const saved = file.status === 'SAVED';
  const previewUrl = source === 'db' && saved ? databaseUrl : contentUrl;

  return (
    <div>
      <h2>{file.originalName}</h2>
      <Notice notice={notice} />
      <dl className="meta">
        <dt>상태</dt><dd><StatusBadge status={file.status} /></dd>
        <dt>크기</dt><dd>{formatBytes(file.size)} · {file.size}바이트</dd>
        <dt>형식</dt><dd>{file.contentType || '-'}</dd>
        <dt>버킷 / 키</dt><dd className="mono">{file.bucket} / {file.objectKey}</dd>
        <dt>파일 확인값</dt><dd className="mono">{file.checksum || '-'}</dd>
        <dt>올린 시각</dt><dd>{formatDate(file.uploadedAt)}</dd>
        <dt>DB 저장 시각</dt><dd>{formatDate(file.savedAt)}</dd>
        <dt>마지막 작업</dt><dd>{file.worker || '-'} · {file.workIp || '-'}</dd>
      </dl>
      <div className="form">
        <label>제목<input value={title} onChange={(event) => setTitle(event.target.value)} maxLength={200} /></label>
        <label>설명<textarea value={description} onChange={(event) => setDescription(event.target.value)} maxLength={2000} rows={4} /></label>
        <label>태그<input value={tags} onChange={(event) => setTags(event.target.value)} placeholder="쉼표로 나눠 적으세요" /></label>
        <div className="actions end">
          <button onClick={update}>상세 정보 수정</button>
          <button className="go" onClick={save}>정보와 파일 내용을 DB에 저장</button>
          <a className="button" href={`${contentUrl}&download=true`}>다운로드</a>
        </div>
      </div>
      <h3>내용 보기</h3>
      <div className="source-switch" role="group" aria-label="미리보기 위치">
        <button type="button" className={source === 'storage' ? 'current' : ''} aria-pressed={source === 'storage'} onClick={() => setSource('storage')}>스토리지 원본</button>
        <button type="button" className={source === 'db' && saved ? 'current' : ''} aria-pressed={source === 'db' && saved} disabled={!saved} onClick={() => setSource('db')}>DB 저장본</button>
        {saved && source === 'db' && <a className="button" href={`${databaseUrl}&download=true`}>DB 저장본 다운로드</a>}
      </div>
      <p className="hint">
        {source === 'db' && saved
          ? 'SM_STORED_FILE.CONTENT 컬럼에 저장된 내용을 그대로 보여 줍니다.'
          : saved ? '스토리지에 있는 원본을 보여 줍니다. DB 저장본을 누르면 DB에 저장된 내용을 볼 수 있습니다.' : 'DB에 저장하면 DB 저장본도 볼 수 있습니다.'}
      </p>
      <Preview key={previewUrl} file={file} url={previewUrl} />
    </div>
  );
}

function Preview({ file, url }) {
  const [text, setText] = useState(null);
  const [error, setError] = useState(null);
  const type = file.contentType || '';
  const isText = type.startsWith('text/') || type.includes('json') || type.includes('xml') || /\.(txt|csv|md|log|sql|json|xml)$/i.test(file.originalName);

  useEffect(() => {
    if (!isText) return undefined;
    let alive = true;
    fetch(url)
      .then((response) => {
        if (!response.ok) throw new Error(`응답 코드 ${response.status}`);
        return response.text();
      })
      .then((value) => alive && setText(value.length > 200000 ? `${value.slice(0, 200000)}\n…이후 내용은 생략했습니다.` : value))
      .catch((cause) => alive && setError(cause.message));
    return () => {
      alive = false;
    };
  }, [url, isText]);

  if (type.startsWith('image/')) return <img className="preview" src={url} alt={file.originalName} />;
  if (type === 'application/pdf') return <iframe className="preview frame" src={url} title={file.originalName} />;
  if (isText) {
    if (error) return <p className="notice notice-error">내용을 불러오지 못했습니다: {error}</p>;
    return <pre className="preview text">{text ?? '불러오는 중…'}</pre>;
  }
  return <p className="empty">이 형식은 미리 볼 수 없습니다.<br />다운로드해서 확인하세요.</p>;
}
