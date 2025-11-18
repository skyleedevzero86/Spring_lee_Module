'use client';

import { useState, useMemo } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import type { PaymentHistoryResponse } from '@/domain/types/payment.types';

interface PaymentTrendLineChartProps {
  payments: PaymentHistoryResponse[];
}

type PeriodType = 'day' | 'week' | 'month';

export function PaymentTrendLineChart({ payments }: PaymentTrendLineChartProps) {
  const [periodType, setPeriodType] = useState<PeriodType>('day');

  const chartData = useMemo(() => {
    const dataMap = new Map<string, { date: string; amount: number; count: number }>();

    payments.forEach((payment) => {
      const date = new Date(payment.createdAt);
      let key: string;

      if (periodType === 'day') {
        key = date.toISOString().split('T')[0];
      } else if (periodType === 'week') {
        const weekStart = new Date(date);
        weekStart.setDate(date.getDate() - date.getDay());
        key = `Week ${weekStart.toISOString().split('T')[0]}`;
      } else {
        key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
      }

      const existing = dataMap.get(key) || { date: key, amount: 0, count: 0 };
      dataMap.set(key, {
        date: key,
        amount: existing.amount + (payment.amount || 0),
        count: existing.count + 1,
      });
    });

    return Array.from(dataMap.values())
      .sort((a, b) => a.date.localeCompare(b.date))
      .slice(-30);
  }, [payments, periodType]);

  return (
    <div className="w-full">
      <div className="mb-4 flex justify-end">
        <select
          value={periodType}
          onChange={(e) => setPeriodType(e.target.value as PeriodType)}
          className="border border-gray-300 rounded-md px-3 py-2 text-sm"
        >
          <option value="day">일별</option>
          <option value="week">주별</option>
          <option value="month">월별</option>
        </select>
      </div>
      <ResponsiveContainer width="100%" height={300}>
        <LineChart data={chartData}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="date" />
          <YAxis yAxisId="left" />
          <YAxis yAxisId="right" orientation="right" />
          <Tooltip />
          <Legend />
          <Line
            yAxisId="left"
            type="monotone"
            dataKey="amount"
            stroke="#3b82f6"
            name="결제 금액 (원)"
            strokeWidth={2}
          />
          <Line
            yAxisId="right"
            type="monotone"
            dataKey="count"
            stroke="#10b981"
            name="결제 건수"
            strokeWidth={2}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}


