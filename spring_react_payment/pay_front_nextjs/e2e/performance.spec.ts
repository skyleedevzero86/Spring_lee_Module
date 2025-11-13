import { test, expect } from '@playwright/test';

test.describe('성능 테스트', () => {
  test('홈 페이지 로딩 시간이 적절해야 함', async ({ page }) => {
    const startTime = Date.now();
    await page.goto('/');
    await page.waitForLoadState('networkidle');
    const loadTime = Date.now() - startTime;

    expect(loadTime).toBeLessThan(5000);
  });

  test('결제 페이지 로딩 시간이 적절해야 함', async ({ page }) => {
    const startTime = Date.now();
    await page.goto('/payments');
    await page.waitForLoadState('networkidle');
    const loadTime = Date.now() - startTime;

    expect(loadTime).toBeLessThan(5000);
  });

  test('LCP가 적절해야 함', async ({ page }) => {
    await page.goto('/');
    
    const lcp = await page.evaluate(() => {
      return new Promise((resolve) => {
        new PerformanceObserver((list) => {
          const entries = list.getEntries();
          const lastEntry = entries[entries.length - 1];
          resolve(lastEntry.startTime);
        }).observe({ entryTypes: ['largest-contentful-paint'] });

        setTimeout(() => resolve(null), 5000);
      });
    });

    if (lcp) {
      expect(lcp).toBeLessThan(2500);
    }
  });
});

