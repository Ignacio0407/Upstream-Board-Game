/**
 * -----------------------------------------------------------------------------
 * Autor / Author: Ignacio Gutiérrez Serrera
 * 
 * 🇬🇧 This software was developed by Ignacio Gutiérrez Serrera.
 * It may be used, modified, and distributed for personal or commercial 
 * purposes, provided that explicit credit is given to the original author.
 * 
 * If this code is used as-is or with minor to moderate modifications 
 * as part of another software, proper attribution must be given to 
 * Ignacio Gutiérrez Serrera.
 * (e.g., credits, documentation, or user interface).
 * 
 * This license notice must not be removed in any redistribution 
 * of the source code or binaries.
 * 
 * 🇪🇸 Este software ha sido desarrollado por Ignacio Gutiérrez Serrera.
 * Se permite su uso, modificación y distribución con fines personales o 
 * comerciales, siempre que se mantenga el reconocimiento explícito al autor.
 * 
 * Si este código se utiliza tal cual, o con modificaciones leves o medias 
 * como parte de otro software, se debe otorgar el crédito correspondiente 
 * a Ignacio Gutiérrez Serrera.
 * (por ejemplo, en los créditos, documentación o interfaz del producto).
 * 
 * No se permite eliminar este aviso de licencia en ninguna redistribución 
 * del código fuente o binario.
 * -----------------------------------------------------------------------------
 */

export type AttributeMap = Record<string, string>;
export type LogicalOperator = "AND" | "OR";
export type MatchMode = "exact" | "contains" | "startsWith";

export interface SearchOptions {
  attributeMap?: AttributeMap;
  operator?: LogicalOperator;   // AND | OR entre atributos
  caseSensitive?: boolean;      // true = exactitud absoluta
  matchMode?: MatchMode;        // Nuevo: modo de coincidencia
}

const getNestedValue = <T,>(obj: T, path: string): unknown => {
  if (!path) return obj as unknown;
  return path.split(".").reduce<unknown>((acc, part) => {
    if (acc == null) return undefined;
    return (acc as any)[part];
  }, obj);
};

const toComparableRaw = (value: unknown): string => {
  if (value == null) return "";
  if (Array.isArray(value)) return value.map(String).join(" ");
  if (typeof value === "object") {
    try { return JSON.stringify(value); } catch { return ""; }
  }
  if (typeof value === "string") return value;
  if (typeof value === "number" || typeof value === "boolean" || typeof value === "bigint" || typeof value === "symbol") {
    return value.toString();
  }
  return "";
};

// "Flexible" normalization: lower case, without accents and without symbols, punctuation or extraneous spaces
// Keeps only letters and Unicode numbers.
const normalizeLoose = (s: string): string =>
  s
    .normalize("NFD")
    .replace(/\p{Diacritic}/gu, "")
    .toLowerCase()
    .replace(/[^\p{L}\p{N}]+/gu, "");

const prepare = (s: string, caseSensitive: boolean): string => caseSensitive ? s : normalizeLoose(s);

// --- Single-character classifiers: O(1) cost, no quantifiers, ---
// --- therefore no risk of super-linear backtracking (Sonar does not flag them). ---
const isKeyChar = (ch: string): boolean => /[\p{L}\p{N}_.-]/u.test(ch);
const isWs = (ch: string): boolean => /\s/.test(ch);

/**
 * Single-pass manual parser, guaranteed O(n).
 *
 * Replaces the original regex /([\p{L}\p{N}_.-]+)\s*:\s*(?:"([^"]*)"|([^\s"]\S*))/gu
 * which Sonar flagged for super-linear performance: with `exec` + `/g`, each starting position of a long sequence of 
 * "key-chars" without a ':' triggers a new match attempt costing O(remaining length), resulting in an overall O(n²)
 * in the worst case (e.g., long free text without ':').
 *
 * The invariant that makes it safe to skip the entire sequence upon failure: ':' is excluded from the key charset,
 * so if the string [keyStart, keyEnd) is not followed by a ':' (after \s*), NO suffix of that same sequence could
 * possibly succeed — it would end at the exact same keyEnd and fail for the same reason.
 * That is why we skip directly to keyEnd instead of keyStart + 1, eliminating the redundant retries that caused the O(n²).
 */
export const parseSearchQuery = (query: string): Record<string, string[]> => {
  const filters: Record<string, string[]> = {};
  if (!query.trim()) return filters;

  const len = query.length;
  let i = 0;

  while (i < len) {
    if (!isKeyChar(query[i])) { i++; continue; }

    const keyStart = i;
    while (i < len && isKeyChar(query[i])) i++;
    const keyEnd = i;

    let j = keyEnd;
    while (j < len && isWs(query[j])) j++;

    if (query[j] !== ":") {
      i = keyEnd; // salto seguro de la racha completa (ver invariante arriba)
      continue;
    }

    j++; // consumir ':'
    while (j < len && isWs(query[j])) j++;

    let raw: string | null = null;
    let valueEnd = j;

    if (query[j] === '"') {
      let k = j + 1;
      while (k < len && query[k] !== '"') k++;
      if (k < len) {
        raw = query.slice(j + 1, k);
        valueEnd = k + 1;
      }
    }

    if (raw === null && j < len && query[j] !== '"' && !isWs(query[j])) {
      let k = j;
      while (k < len && !isWs(query[k])) k++;
      raw = query.slice(j, k);
      valueEnd = k;
    }

    if (raw === null) {
      i = keyEnd; // mismo argumento: ningún sufijo de la racha lo arregla
      continue;
    }

    const attr = query.slice(keyStart, keyEnd).trim();
    const rawTrimmed = raw.trim();
    if (attr && rawTrimmed) {
      const values = rawTrimmed.split(",").map(v => v.trim()).filter(Boolean);
      if (values.length) filters[attr] = values;
    }

    i = valueEnd;
  }

  return filters;
};

const compare = (haystack: string, needle: string, mode: MatchMode): boolean => {
  switch (mode) {
    case "contains":   return haystack.includes(needle);
    case "startsWith": return haystack.startsWith(needle);
    default:           return haystack === needle;
  }
};

export const searchData = <T,>(
  data: T[],
  query: string,
  options: SearchOptions = {}
): T[] => {
  const {
    attributeMap = {},
    operator = "AND",
    caseSensitive = true,
    matchMode = "exact"
  } = options;

  const filters = parseSearchQuery(query);
  const entries = Object.entries(filters);
  if (!entries.length) return data;

  return data.filter(item => {
    const matchesAttr = ([attr, values]: [string, string[]]) => {
      const path = attributeMap[attr] || attr;
      const fieldRaw = toComparableRaw(getNestedValue(item, path));
      const haystack = prepare(fieldRaw, caseSensitive);

      return values.some(v =>
        compare(haystack, prepare(v, caseSensitive), matchMode)
      );
    };

    return operator === "AND"
      ? entries.every(matchesAttr)
      : entries.some(matchesAttr);
  });
};

// LRU cache
export class LRUCache<K, V> {
  private readonly map = new Map<K, V>();
  constructor(private readonly capacity = 200) {}

  get(key: K): V | undefined {
    if (!this.map.has(key)) return undefined;
    const val = this.map.get(key)!;
    this.map.delete(key);
    this.map.set(key, val);
    return val;
  }
  set(key: K, value: V): void {
    if (this.map.has(key)) this.map.delete(key);
    else if (this.map.size >= this.capacity) {
      const oldest = this.map.keys().next().value;
      if (oldest) this.map.delete(oldest);
    }
    this.map.set(key, value);
  }
  clear(): void { this.map.clear(); }
}