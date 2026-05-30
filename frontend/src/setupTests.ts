import '@testing-library/jest-dom/vitest';
import { beforeAll, afterEach, afterAll, vi } from 'vitest';
import { server } from './mocks/server';

// MSW setup for integration testing
beforeAll(() => server.listen({ onUnhandledRequest: 'warn' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

// Global mocks to reduce boilerplate
vi.mock("react-router-dom", async () => {
    const actual = await vi.importActual("react-router-dom");
    return {
        ...actual,
        useNavigate: vi.fn(() => vi.fn()),
        useParams: vi.fn(() => ({})),
        useLocation: vi.fn(() => ({ pathname: "/", state: {} })),
    };
});

vi.mock("./services/token.service.ts", () => ({
    default: {
        getLocalAccessToken: vi.fn(() => "mock-jwt"),
        getUser: vi.fn(() => ({ id: 1, username: "admin", authority: { authority: "ADMIN" } })),
        setUser: vi.fn(),
        removeUser: vi.fn(),
        updateLocalAccessToken: vi.fn(),
    },
}));
// Mock jwt-decode
vi.mock("jwt-decode", () => ({
    default: vi.fn(() => ({ authorities: ["ADMIN"] })),
}));

