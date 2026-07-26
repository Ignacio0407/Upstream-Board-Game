import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { get, patch, put, post, deleteEntity, buildSafeUrl } from './fetchers';

describe('API Fetchers', () => {
  const uri = 'http://localhost:3000/api/test';
  const jwt = 'fake-jwt';
  const data = { id: 1, name: 'Paco' };
  
  let fetchSpy: any;

  beforeEach(() => {
    // Spay and mock fetch safely
    fetchSpy = vi.spyOn(globalThis, 'fetch').mockImplementation(() =>
      Promise.resolve({
        ok: true,
        json: () => Promise.resolve({}),
        clone: function () { return this; }
      } as unknown as Response)
    );
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('get performs a GET request', async () => {
    await get(uri, jwt);
    expect(fetchSpy).toHaveBeenCalledWith(uri, expect.objectContaining({ method: 'GET' }));
  });

  it('patch performs a PATCH request with body', async () => {
    await patch(uri, jwt, data);
    expect(fetchSpy).toHaveBeenCalledWith(uri, expect.objectContaining({ 
        method: 'PATCH', 
        body: JSON.stringify(data) 
    }));
  });

  it('put performs a PUT request with body', async () => {
    await put(uri, jwt, data);
    expect(fetchSpy).toHaveBeenCalledWith(uri, expect.objectContaining({ 
        method: 'PUT', 
        body: JSON.stringify(data) 
    }));
  });

  it('post performs a POST request with body', async () => {
    await post(uri, jwt, data);
    expect(fetchSpy).toHaveBeenCalledWith(uri, expect.objectContaining({ 
        method: 'POST', 
        body: JSON.stringify(data) 
    }));
  });

  it('deleteEntity performs a DELETE request and appends ID to URI', async () => {
    await deleteEntity(uri, jwt, 42);
    expect(fetchSpy).toHaveBeenCalledWith(`${uri}/42`, expect.objectContaining({ method: 'DELETE' }));
  });

  it('buildSafeUrl creates a safe URL with or without id', () => {
    const urlWithId = buildSafeUrl('/api/v1/users/', 42);
    expect(urlWithId).toBe('http://localhost:3000/api/v1/users/42');

    const urlWithoutId = buildSafeUrl('/api/v1/users');
    expect(urlWithoutId).toBe('http://localhost:3000/api/v1/users');
  });
});