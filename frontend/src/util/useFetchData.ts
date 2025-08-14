import { useEffect, useState } from "react";
import { get } from "./fetchers.ts";

/**
 * Custom React hook to fetch data from a specified URL and manage it in the component's state.
 * This hook supports JWT-based authentication.
 * @param {string} url - The URL from which to fetch the data. The fetch request will not be initiated if the URL is not provided.
 * @param {string} jwt - The JSON Web Token (JWT) for authorization. The token will be included in the request headers as `Authorization: Bearer <jwt>`.
 * @returns {Array} - The state variable `data` containing the fetched data, initialized as an empty array.
 * @example
 * const data = useFetchData('https://api.example.com/data', jwtToken);
 */

export default function useFetchData<T>(url: string, jwt: string): T {
  const [data, setData] = useState<T>(() => {
    // Initialize as empty object or array based on generic
    if (Array.isArray([] as unknown as T)) return [] as T;
    return {} as T;
  });

  useEffect(() => {
    if (url) {
      let ignore = false;

      get(url, jwt)
        .then((response) => response.json())
        .then((json) => {
          if (!ignore) setData(json);
        })
        .catch((err) => {
          console.error("Fetch error:", err);
          alert("Failed to fetch data.");
        });

      return () => {
        ignore = true;
      };
    }
  }, [url, jwt]);

  return data;
}
