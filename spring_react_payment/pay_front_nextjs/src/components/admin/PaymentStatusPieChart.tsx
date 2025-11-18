'use client';

import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from 'recharts';

interface PaymentStatusPieChartProps {
  data: {
    completed: number;
    pending: number;
    failed: number;
    cancelled: number;
  };
}

const COLORS = {
  completed: '#10b981',
  pending: '#f59e0b',
  failed: '#ef4444',
  cancelled: '#6b7280',
};

export function PaymentStatusPieChart({ data }: PaymentStatusPieChartProps) {
  const chartData = [
    { name: '완료', value: data.completed, color: COLORS.completed },
    { name: '대기', value: data.pending, color: COLORS.pending },
    { name: '실패', value: data.failed, color: COLORS.failed },
    { name: '취소', value: data.cancelled, color: COLORS.cancelled },
  ].filter((item) => item.value > 0);

  return (
    <ResponsiveContainer width="100%" height={300}>
      <PieChart>
        <Pie
          data={chartData}
          cx="50%"
          cy="50%"
          labelLine={false}
          label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
          outerRadius={100}
          fill="#8884d8"
          dataKey="value"
        >
          {chartData.map((entry, index) => (
            <Cell key={`cell-${index}`} fill={entry.color} />
          ))}
        </Pie>
        <Tooltip />
        <Legend />
      </PieChart>
    </ResponsiveContainer>
  );
}


