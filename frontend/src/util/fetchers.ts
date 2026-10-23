const createHeaders = (jwt: string | null): Record<string, string> => {
  const headers: Record<string, string> = {
    Accept: "application/json",
    "Content-Type": "application/json",
  };
  if (jwt && jwt.trim() !== "" && jwt !== "null") {
    headers["Authorization"] = `Bearer ${jwt}`;
  }
  return headers;
};

const API_PREFIX = "/api/";

function toApiUrl(path: string): string {
  const url = new URL(path, window.location.origin);
  if (url.origin !== window.location.origin || !url.pathname.startsWith(API_PREFIX)) {
    throw new TypeError("URL outside the allowed API");

  }
  return url.href; // pathname already normalized: ../ and %2e%2e are resolved before the check
}

export function buildSafeUrl(base: string, id?: string | number): string {
  if (id === undefined) return toApiUrl(base);
  const segment = encodeURIComponent(String(id));
  if (segment === "." || segment === "..") throw new TypeError("ID not valid");
  const prefix = base.endsWith("/") ? base.slice(0, -1) : base; // without regex
  return toApiUrl(`${prefix}/${segment}`);
}

const request = async (method: string, uri: string, jwt: string | null, data?: unknown) =>
  fetch(toApiUrl(uri), {
    method,
    headers: createHeaders(jwt),
    body: JSON.stringify(data),
  });

export const get = (uri: string, jwt: string | null) => request("GET", uri, jwt);
export const patch = (uri: string, jwt: string | null, data?: unknown) => request("PATCH", uri, jwt, data);
export const put = (uri: string, jwt: string | null, data?: unknown) => request("PUT", uri, jwt, data);
export const post = (uri: string, jwt: string | null, data?: unknown) => request("POST", uri, jwt, data);

export const deleteEntity = (uri: string, jwt: string | null, id: number) =>
  fetch(buildSafeUrl(uri, id), { method: "DELETE", headers: createHeaders(jwt) });