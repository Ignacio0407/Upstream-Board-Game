import { render, waitFor, screen } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import SwaggerDocs from './Swagger.tsx';

// Mockeamos la librería externa para no ralentizar ni romper el test
vi.mock('swagger-ui-react', () => {
  return {
    default: () => <div data-testid="swagger-ui-mock">Mocked Swagger</div>,
  };
});

describe('SwaggerDocs Component', () => {
  beforeEach(() => {
    // Mockeamos el fetch nativo para que devuelva un JSON falso
    globalThis.fetch = vi.fn(() =>
      Promise.resolve({
        json: () => Promise.resolve({ openapi: '3.0.0', info: { title: 'Test API' } }),
      } as Response)
    );
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('fetches api docs and renders SwaggerUI on mount', async () => {
    render(<SwaggerDocs />);

    // Comprueba que hace la llamada al backend correcto
    expect(globalThis.fetch).toHaveBeenCalledWith('/v3/api-docs', expect.any(Object));

    // Comprueba que renderiza el componente Swagger (nuestro mock)
    await waitFor(() => {
      expect(screen.getByTestId('swagger-ui-mock')).toBeDefined();
    });
  });
});