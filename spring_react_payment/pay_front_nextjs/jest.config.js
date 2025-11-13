const nextJest = require('next/jest')

const createJestConfig = nextJest({
  dir: './',
})

const customJestConfig = {
  setupFilesAfterEnv: ['<rootDir>/jest.setup.js'],
  testEnvironment: 'jest-environment-jsdom',
  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/$1',
    '^@/src/(.*)$': '<rootDir>/src/$1',
  },
  testMatch: [
    '**/__tests__/**/*.[jt]s?(x)',
    '**/?(*.)+(spec|test).[jt]s?(x)',
  ],
  collectCoverageFrom: [
    'src/**/*.{js,jsx,ts,tsx}',
    'app/**/*.{js,jsx,ts,tsx}',
    'components/**/*.{js,jsx,ts,tsx}',
    'hooks/**/*.{js,jsx,ts,tsx}',
    'lib/**/*.{js,jsx,ts,tsx}',
    '!**/*.d.ts',
    '!**/node_modules/**',
    '!**/.next/**',
    '!**/types/**',
    '!**/*.test.{js,jsx,ts,tsx}',
    '!**/*.spec.{js,jsx,ts,tsx}',
  ],
  coverageThreshold: {
    global: {
      branches: 60,
      functions: 60,
      lines: 60,
      statements: 60,
    },
    'src/domain/payment-amount-calculator.ts': {
      branches: 100,
      functions: 100,
      lines: 100,
      statements: 100,
    },
    'src/domain/payment-state-machine.ts': {
      branches: 100,
      functions: 100,
      lines: 100,
      statements: 100,
    },
    'src/domain/payment-refund-calculator.ts': {
      branches: 100,
      functions: 100,
      lines: 100,
      statements: 100,
    },
    'src/domain/payment-validator.ts': {
      branches: 100,
      functions: 100,
      lines: 100,
      statements: 100,
    },
  },
  coverageReporters: ['text', 'lcov', 'html', 'json-summary'],
}

module.exports = createJestConfig(customJestConfig)
