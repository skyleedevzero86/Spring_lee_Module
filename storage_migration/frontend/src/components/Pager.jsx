import { useEffect, useState } from 'react';

export function usePaged(items, size = 10) {
  const [page, setPage] = useState(1);
  const totalPages = Math.max(1, Math.ceil(items.length / size));
  const current = Math.min(page, totalPages);

  useEffect(() => {
    if (page > totalPages) setPage(totalPages);
  }, [page, totalPages]);

  return {
    page: current,
    totalPages,
    total: items.length,
    pageItems: items.slice((current - 1) * size, current * size),
    go: setPage,
  };
}

export default function Pager({ paged }) {
  const { page, totalPages, total, go } = paged;
  if (total === 0) return null;
  const start = Math.max(1, Math.min(page - 2, totalPages - 4));
  const end = Math.min(totalPages, start + 4);
  const numbers = Array.from({ length: end - start + 1 }, (_, index) => start + index);
  const move = (target) => {
    if (target >= 1 && target <= totalPages && target !== page) go(target);
  };

  return (
    <nav className="pager" aria-label="쪽 이동">
      <span className="pager-total">전체 {total}건 · {page} / {totalPages}쪽</span>
      <div className="pager-buttons">
        <button type="button" disabled={page <= 1} aria-label="첫 쪽" onClick={() => move(1)}>«</button>
        <button type="button" disabled={page <= 1} aria-label="이전 쪽" onClick={() => move(page - 1)}>‹</button>
        {numbers.map((number) => (
          <button key={number} type="button" className={number === page ? 'current' : ''} aria-current={number === page ? 'page' : undefined} onClick={() => move(number)}>
            {number}
          </button>
        ))}
        <button type="button" disabled={page >= totalPages} aria-label="다음 쪽" onClick={() => move(page + 1)}>›</button>
        <button type="button" disabled={page >= totalPages} aria-label="마지막 쪽" onClick={() => move(totalPages)}>»</button>
      </div>
    </nav>
  );
}
