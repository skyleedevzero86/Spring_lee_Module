import type { ChartConfiguration, ChartType } from 'chart.js'

export type ChartSeries = {
  labels: string[]
  values: number[]
  secondary?: number[]
  tertiary?: number[]
}

export interface ChartStrategy {
  readonly type: ChartType
  build(series: ChartSeries, title: string): ChartConfiguration
}

const palette = ['#2f6f8f', '#c45c26', '#1f6b45', '#6b4f9a', '#b08900', '#8b3a3a', '#355f7a', '#5a6b4f']

export class BarChartStrategy implements ChartStrategy {
  readonly type = 'bar' as const

  build(series: ChartSeries, title: string): ChartConfiguration {
    return {
      type: 'bar',
      data: {
        labels: series.labels,
        datasets: [
          {
            label: title,
            data: series.values,
            backgroundColor: series.labels.map((_, i) => palette[i % palette.length]),
            borderRadius: 8,
          },
        ],
      },
      options: cartesianOptions(title),
    }
  }
}

export class LineChartStrategy implements ChartStrategy {
  readonly type = 'line' as const

  build(series: ChartSeries, title: string): ChartConfiguration {
    const datasets = [
      {
        label: '건수',
        data: series.values,
        borderColor: '#2f6f8f',
        backgroundColor: 'rgba(47,111,143,0.15)',
        fill: true,
        tension: 0.35,
      },
    ]
    if (series.secondary) {
      datasets.push({
        label: '긴급',
        data: series.secondary,
        borderColor: '#c45c26',
        backgroundColor: 'rgba(196,92,38,0.12)',
        fill: false,
        tension: 0.35,
      })
    }
    if (series.tertiary) {
      datasets.push({
        label: '캐시 적중',
        data: series.tertiary,
        borderColor: '#1f6b45',
        backgroundColor: 'rgba(31,107,69,0.12)',
        fill: false,
        tension: 0.35,
      })
    }
    return {
      type: 'line',
      data: {
        labels: series.labels,
        datasets,
      },
      options: cartesianOptions(title),
    }
  }
}

export class DoughnutChartStrategy implements ChartStrategy {
  readonly type = 'doughnut' as const

  build(series: ChartSeries, title: string): ChartConfiguration {
    return {
      type: 'doughnut',
      data: {
        labels: series.labels,
        datasets: [
          {
            label: title,
            data: series.values,
            backgroundColor: series.labels.map((_, i) => palette[i % palette.length]),
            borderWidth: 0,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          title: {
            display: true,
            text: title,
            color: '#171512',
            font: { size: 14, weight: 600 as const },
          },
          legend: {
            position: 'bottom',
            labels: { color: '#3d3a34' },
          },
        },
      },
    }
  }
}

export class ChartContext {
  constructor(private strategy: ChartStrategy) {}

  setStrategy(strategy: ChartStrategy): void {
    this.strategy = strategy
  }

  configure(series: ChartSeries, title: string): ChartConfiguration {
    return this.strategy.build(series, title)
  }
}

const cartesianOptions = (title: string) => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    title: {
      display: true,
      text: title,
      color: '#171512',
      font: { size: 14, weight: 600 as const },
    },
    legend: {
      labels: { color: '#3d3a34' },
    },
  },
  scales: {
    x: {
      ticks: { color: '#6c675d' },
      grid: { color: 'rgba(0,0,0,0.05)' },
    },
    y: {
      beginAtZero: true,
      ticks: { color: '#6c675d', precision: 0 },
      grid: { color: 'rgba(0,0,0,0.05)' },
    },
  },
})

export const mapToSeries = (map: Record<string, number>): ChartSeries => ({
  labels: Object.keys(map),
  values: Object.values(map),
})

export const timelineToSeries = (
  timeline: Array<{ bucket: string; count: number; urgentCount: number; cacheHits: number }>,
): ChartSeries => ({
  labels: timeline.map((t) => t.bucket),
  values: timeline.map((t) => t.count),
  secondary: timeline.map((t) => t.urgentCount),
  tertiary: timeline.map((t) => t.cacheHits),
})
