import { test, expect } from '@playwright/test';

test.describe('네비게이션', () => {
  test('홈 페이지가 표시되어야 함', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByText('토스 페이먼츠 결제 시스템')).toBeVisible();
  });

  test('회원가입 링크가 작동해야 함', async ({ page }) => {
    await page.goto('/');
    await page.getByRole('link', { name: /회원가입/i }).click();
    await expect(page).toHaveURL(/\/register/);
  });

  test('결제 관리 링크가 작동해야 함', async ({ page }) => {
    await page.goto('/');
    await page.getByRole('link', { name: /결제 관리/i }).click();
    await expect(page).toHaveURL(/\/payments/);
  });
});



