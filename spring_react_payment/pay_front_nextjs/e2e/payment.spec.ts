import { test, expect } from '@playwright/test';

test.describe('결제 관리', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/payments');
  });

  test('결제 관리 페이지가 표시되어야 함', async ({ page }) => {
    await expect(page.getByText('결제 관리')).toBeVisible();
    await expect(page.getByRole('link', { name: /결제 생성/i })).toBeVisible();
    await expect(page.getByRole('link', { name: /결제 이력/i })).toBeVisible();
  });

  test('결제 생성 폼이 표시되어야 함', async ({ page }) => {
    await expect(page.getByText('결제 생성')).toBeVisible();
    await expect(page.getByLabel(/주문번호/i)).toBeVisible();
    await expect(page.getByLabel(/상품 설명/i)).toBeVisible();
    await expect(page.getByLabel(/결제 금액/i)).toBeVisible();
  });

  test('결제 생성 폼 필수 필드 검증이 작동해야 함', async ({ page }) => {
    const submitButton = page.getByRole('button', { name: /결제 생성/i });
    await submitButton.click();

    await expect(page.getByText(/주문번호는 필수입니다/i)).toBeVisible();
  });

  test('결제 생성 페이지로 이동해야 함', async ({ page }) => {
    await page.getByRole('link', { name: /결제 생성/i }).click();
    await expect(page).toHaveURL(/\/payments\/create/);
    await expect(page.getByText('결제 생성')).toBeVisible();
  });

  test('결제 이력 페이지로 이동해야 함', async ({ page }) => {
    await page.getByRole('link', { name: /결제 이력/i }).click();
    await expect(page).toHaveURL(/\/payments\/history/);
  });
});

