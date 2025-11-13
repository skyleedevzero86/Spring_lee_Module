import { test, expect } from '@playwright/test';

test.describe('에러 처리', () => {
  test('404 페이지가 표시되어야 함', async ({ page }) => {
    await page.goto('/non-existent-page');
    await expect(page.getByText(/404|페이지를 찾을 수 없습니다/i)).toBeVisible();
  });

  test('에러 바운더리가 작동해야 함', async ({ page }) => {
    await page.goto('/payments');
    
    await page.evaluate(() => {
      throw new Error('테스트 에러');
    });

    await expect(page.getByText(/오류 발생|에러가 발생했습니다/i)).toBeVisible();
  });
});

