export default function FilePick({ files, multiple = false, accept, onChange }) {
  const name = files.length === 0
    ? '선택한 파일이 없습니다'
    : files.length === 1
      ? files[0].name
      : `${files[0].name} 외 ${files.length - 1}개`;

  return (
    <span className="file-pick">
      <input type="file" multiple={multiple} accept={accept} onChange={(event) => onChange(Array.from(event.target.files || []))} />
      <span className="file-pick-button">파일 선택</span>
      <span className="file-pick-name">{name}</span>
    </span>
  );
}
