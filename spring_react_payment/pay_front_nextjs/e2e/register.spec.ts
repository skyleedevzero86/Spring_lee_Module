import { test, expect } from '@playwright/test';

test.describe('회원가입', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/register');
  });

  test('회원가입 폼이 표시되어야 함', async ({ page }) => {
    await expect(page.getByText('회원가입')).toBeVisible();
    await expect(page.getByLabel(/이메일/i)).toBeVisible();
    await expect(page.getByLabel(/비밀번호/i)).toBeVisible();
    await expect(page.getByLabel(/이름/i)).toBeVisible();
  });

  test('필수 필드 검증이 작동해야 함', async ({ page }) => {
    const submitButton = page.getByRole('button', { name: /회원가입/i });
    await submitButton.click();

    await expect(page.getByText(/이메일은 필수입니다/i)).toBeVisible();
  });

  test('유효한 이메일 형식 검증이 작동해야 함', async ({ page }) => {
    await page.getByLabel(/이메일/i).fill('invalid-email');
    await page.getByLabel(/비밀번호/i).fill('password123');
    await page.getByLabel(/이름/i).fill('테스트');

    const submitButton = page.getByRole('button', { name: /회원가입/i });
    await submitButton.click();

    await expect(page.getByText(/유효한 이메일 형식이 아닙니다/i)).toBeVisible();
  });

  test('비밀번호 최소 길이 검증이 작동해야 함', async ({ page }) => {
    await page.getByLabel(/이메일/i).fill('test@example.com');
    await page.getByLabel(/비밀번호/i).fill('12345');
    await page.getByLabel(/이름/i).fill('테스트');

    const submitButton = page.getByRole('button', { name: /회원가입/i });
    await submitButton.click();

    await expect(page.getByText(/비밀번호는 최소 6자 이상이어야 합니다/i)).toBeVisible();
  });
});



