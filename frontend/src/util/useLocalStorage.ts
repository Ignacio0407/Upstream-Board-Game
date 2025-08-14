import { useState, useEffect } from "react";

/**
 * Custom hook to persist state using `localStorage`.
 * @param key - Key used to store the value in `localStorage`
 * @param defaultValue - Value used if none is stored
 * @returns A stateful value and a setter function
 */
function useLocalState<T>(key: string, defaultValue: T): [T, (val: T) => void] {
  const [value, setValue] = useState<T>(() => {
    const stored = window.localStorage.getItem(key);
    return stored !== null ? JSON.parse(stored) as T : defaultValue;
  });

  useEffect(() => {
    window.localStorage.setItem(key, JSON.stringify(value));
  }, [key, value]);

  return [value, setValue];
}

export { useLocalState };