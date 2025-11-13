interface PerformanceMetric {
  name: string;
  value: number;
  rating: 'good' | 'needs-improvement' | 'poor';
  timestamp: number;
}

class PerformanceMonitor {
  private metrics: PerformanceMetric[] = [];

  init(): void {
    if (typeof window === 'undefined') return;

    this.observeWebVitals();
    this.observeApiPerformance();
    this.observeResourceLoading();
  }

  private observeWebVitals(): void {
    if (typeof window === 'undefined') return;

    try {
      import('web-vitals').then(({ onCLS, onINP, onFCP, onLCP, onTTFB }) => {
        onCLS((metric) => this.recordMetric('CLS', metric.value));
        onINP((metric) => this.recordMetric('INP', metric.value));
        onFCP((metric) => this.recordMetric('FCP', metric.value));
        onLCP((metric) => this.recordMetric('LCP', metric.value));
        onTTFB((metric) => this.recordMetric('TTFB', metric.value));
      });
    } catch {
    }
  }

  private observeApiPerformance(): void {
    if (typeof window === 'undefined') return;

    const originalFetch = window.fetch;
    window.fetch = async (...args) => {
      const startTime = performance.now();
      const url = typeof args[0] === 'string' ? args[0] : args[0].url;

      try {
        const response = await originalFetch(...args);
        const endTime = performance.now();
        const duration = endTime - startTime;

        this.recordMetric(`API_${url}`, duration, {
          url,
          method: typeof args[0] === 'string' ? 'GET' : args[0].method || 'GET',
          status: response.status,
        });

        return response;
      } catch (error) {
        const endTime = performance.now();
        const duration = endTime - startTime;

        this.recordMetric(`API_ERROR_${url}`, duration, {
          url,
          error: error instanceof Error ? error.message : '알 수 없는 오류',
        });

        throw error;
      }
    };
  }

  private observeResourceLoading(): void {
    if (typeof window === 'undefined' || !window.performance) return;

    window.addEventListener('load', () => {
      const navigation = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming;
      if (navigation) {
        this.recordMetric('DOMContentLoaded', navigation.domContentLoadedEventEnd - navigation.domContentLoadedEventStart);
        this.recordMetric('Load', navigation.loadEventEnd - navigation.loadEventStart);
      }

      const resources = performance.getEntriesByType('resource') as PerformanceResourceTiming[];
      resources.forEach((resource) => {
        const duration = resource.responseEnd - resource.startTime;
        if (duration > 1000) {
          this.recordMetric(`RESOURCE_${resource.name}`, duration, {
            type: resource.initiatorType,
            size: resource.transferSize,
          });
        }
      });
    });
  }

  private recordMetric(name: string, value: number, context?: Record<string, unknown>): void {
    const rating = this.getRating(name, value);
    const metric: PerformanceMetric = {
      name,
      value,
      rating,
      timestamp: Date.now(),
    };

    this.metrics.push(metric);

    if (process.env.NEXT_PUBLIC_PERFORMANCE_MONITORING_ENABLED === 'true') {
      this.sendMetric(metric, context);
    }
  }

  private getRating(name: string, value: number): 'good' | 'needs-improvement' | 'poor' {
    const thresholds: Record<string, { good: number; poor: number }> = {
      CLS: { good: 0.1, poor: 0.25 },
      INP: { good: 200, poor: 500 },
      FCP: { good: 1800, poor: 3000 },
      LCP: { good: 2500, poor: 4000 },
      TTFB: { good: 800, poor: 1800 },
    };

    const threshold = thresholds[name];
    if (!threshold) {
      return value < 1000 ? 'good' : value < 3000 ? 'needs-improvement' : 'poor';
    }

    if (value <= threshold.good) return 'good';
    if (value <= threshold.poor) return 'needs-improvement';
    return 'poor';
  }

  private sendMetric(metric: PerformanceMetric, context?: Record<string, unknown>): void {
    if (typeof window === 'undefined') return;

    try {
      fetch('/api/metrics/performance', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...metric, context }),
      }).catch(() => {});
    } catch {
    }
  }

  getMetrics(): PerformanceMetric[] {
    return [...this.metrics];
  }

  clearMetrics(): void {
    this.metrics = [];
  }
}

export const performanceMonitor = new PerformanceMonitor();

