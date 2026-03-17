/* 
jest-dom adds custom jest matchers for asserting on DOM nodes.
Allows you to do things like: expect(element).toHaveTextContent(/react/i)
learn more: https://github.com/testing-library/jest-dom
*/
import '@testing-library/jest-dom/vitest';
import { server } from './mocks/server';
import structuredClone from '@ungap/structured-clone';

// Start MSW before all tests
beforeAll(() => server.listen());

// Polyfill for structuredClone
(globalThis as any).structuredClone = structuredClone;

// Reset handlers after each test
afterEach(() => server.resetHandlers());

// Close server after all tests
afterAll(() => server.close());