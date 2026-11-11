import { useEffect, useRef, useState } from 'react';

const BAR = 'var(--chart-series-1)';

function scale(value) {
  const step = Math.max(1, Math.ceil(value / 4));
  return { max: step * 4, ticks: [0, step * 2, step * 4] };
}

function useWidth(fallback) {
  const ref = useRef(null);
  const [width, setWidth] = useState(fallback);
  useEffect(() => {
    if (!ref.current) return undefined;
    const observer = new ResizeObserver(([entry]) => setWidth(Math.max(280, Math.round(entry.contentRect.width))));
    observer.observe(ref.current);
    return () => observer.disconnect();
  }, []);
  return [ref, width];
}

function HorizontalBars({ data, unit }) {
  const [hover, setHover] = useState(null);
  const { max, ticks } = scale(Math.max(...data.map((item) => item.value), 0));
  const [ref, width] = useWidth(640);
  const top = data.reduce((best, item, index) => (item.value > data[best].value ? index : best), 0);
  const rowHeight = 34;
  const labelWidth = Math.min(150, Math.round(width * 0.3));
  const plotWidth = width - labelWidth - 56;
  const height = data.length * rowHeight + 28;

  return (
    <div className="chart-frame" ref={ref}>
      <svg viewBox={`0 0 ${width} ${height}`} role="img" aria-hidden="true" onMouseLeave={() => setHover(null)}>
        {ticks.map((tick) => {
          const x = labelWidth + (tick / max) * plotWidth;
          return (
            <g key={tick}>
              <line x1={x} x2={x} y1={0} y2={height - 22} className="chart-grid" />
              <text x={x} y={height - 6} textAnchor="middle" className="chart-axis">{tick}</text>
            </g>
          );
        })}
        {data.map((item, index) => {
          const y = index * rowHeight + 6;
          const barWidth = max ? (item.value / max) * plotWidth : 0;
          return (
            <g key={item.label} onMouseEnter={() => setHover(index)}>
              <rect x={0} y={y - 4} width={width} height={rowHeight} fill="transparent" />
              <text x={labelWidth - 10} y={y + 15} textAnchor="end" className="chart-label">{item.label}</text>
              {barWidth > 0 && (
                <path
                  d={`M${labelWidth},${y + 4} h${Math.max(barWidth - 4, 0)} a4,4 0 0 1 4,4 v10 a4,4 0 0 1 -4,4 h-${Math.max(barWidth - 4, 0)} z`}
                  fill={BAR}
                  opacity={hover === null || hover === index ? 1 : 0.45}
                />
              )}
              {index === top && item.value > 0 && (
                <text x={labelWidth + barWidth + 8} y={y + 17} className="chart-value">{item.value}{unit}</text>
              )}
            </g>
          );
        })}
      </svg>
      {hover !== null && (
        <div className="chart-tip" style={{ top: `${hover * rowHeight}px`, left: `${labelWidth + 12}px` }}>
          <strong>{data[hover].label}</strong> {data[hover].value}{unit}
        </div>
      )}
    </div>
  );
}

function Columns({ data, unit }) {
  const [hover, setHover] = useState(null);
  const { max, ticks } = scale(Math.max(...data.map((item) => item.value), 0));
  const [ref, width] = useWidth(640);
  const height = 230;
  const left = 36;
  const bottom = 26;
  const top = 30;
  const plotHeight = height - bottom - top;
  const slot = (width - left) / data.length;
  const barWidth = Math.min(36, slot - 12);
  const last = data.length - 1;

  return (
    <div className="chart-frame" ref={ref}>
      <svg viewBox={`0 0 ${width} ${height}`} role="img" aria-hidden="true" onMouseLeave={() => setHover(null)}>
        {ticks.map((tick) => {
          const y = top + plotHeight - (tick / max) * plotHeight;
          return (
            <g key={tick}>
              <line x1={left} x2={width} y1={y} y2={y} className="chart-grid" />
              <text x={left - 8} y={y + 4} textAnchor="end" className="chart-axis">{tick}</text>
            </g>
          );
        })}
        {data.map((item, index) => {
          const x = left + slot * index + (slot - barWidth) / 2;
          const barHeight = max ? (item.value / max) * plotHeight : 0;
          const baseY = top + plotHeight;
          return (
            <g key={item.label} onMouseEnter={() => setHover(index)}>
              <rect x={left + slot * index} y={0} width={slot} height={height} fill="transparent" />
              {barHeight > 0 && (
                <path
                  d={`M${x},${baseY} v-${Math.max(barHeight - 4, 0)} a4,4 0 0 1 4,-4 h${barWidth - 8} a4,4 0 0 1 4,4 v${Math.max(barHeight - 4, 0)} z`}
                  fill={BAR}
                  opacity={hover === null || hover === index ? 1 : 0.45}
                />
              )}
              {index === last && item.value > 0 && (
                <text x={x + barWidth / 2} y={baseY - barHeight - 8} textAnchor="middle" className="chart-value">{item.value}</text>
              )}
              <text x={x + barWidth / 2} y={height - 8} textAnchor="middle" className="chart-axis">{item.label}</text>
            </g>
          );
        })}
      </svg>
      {hover !== null && (
        <div className="chart-tip" style={{ top: '2px', left: `${Math.min(left + slot * hover, width - 140)}px` }}>
          <strong>{data[hover].label}</strong> {data[hover].value}{unit}
        </div>
      )}
    </div>
  );
}

export default function StatChart({ chart }) {
  const [showTable, setShowTable] = useState(false);
  if (!chart || chart.data.length === 0) return null;
  const empty = chart.data.every((item) => item.value === 0);

  return (
    <figure className="stat-chart" aria-label={chart.title}>
      <figcaption>
        <span>{chart.title}</span>
        <button type="button" className="chart-toggle" onClick={() => setShowTable((value) => !value)}>
          {showTable ? '차트로 보기' : '표로 보기'}
        </button>
      </figcaption>
      {empty && !showTable && <p className="chart-empty">아직 집계할 자료가 없습니다.</p>}
      {!empty && !showTable && (chart.type === 'columns'
        ? <Columns data={chart.data} unit={chart.unit || ''} />
        : <HorizontalBars data={chart.data} unit={chart.unit || ''} />)}
      {showTable && (
        <table className="chart-table">
          <thead><tr><th>{chart.labelHeader || '구분'}</th><th>{chart.valueHeader || '건수'}</th></tr></thead>
          <tbody>
            {chart.data.map((item) => <tr key={item.label}><td>{item.label}</td><td>{item.value}{chart.unit || ''}</td></tr>)}
          </tbody>
        </table>
      )}
    </figure>
  );
}
