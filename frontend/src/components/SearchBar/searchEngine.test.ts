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

import { describe, it, expect } from "vitest";
import { 
  searchData, 
  parseSearchQuery, 
  LRUCache, 
  SearchOptions 
} from "./searchEngine";

describe("searchEngine.ts - Unit Tests", () => {
  
  describe("parseSearchQuery", () => {
    it("parses complex queries including quotes and multiple values", () => {
      const query = 'status:active,pending type:"Full Time" name:John';
      const result = parseSearchQuery(query);
      
      expect(result).toEqual({
        status: ["active", "pending"],
        type: ["Full Time"],
        name: ["John"]
      });
    });

    it("handles empty or malformed queries gracefully", () => {
      expect(parseSearchQuery("")).toEqual({});
      expect(parseSearchQuery("invalid-query")).toEqual({});
      expect(parseSearchQuery("key:")).toEqual({});
    });
  });

  describe("searchData", () => {
    const mockData = [
      { id: 1, info: { name: "Árbol", tags: ["nature", "green"] }, category: "plant" },
      { id: 2, info: { name: "Casa", tags: ["urban"] }, category: "building" },
    ];

    it("supports nested path searches and Unicode normalization (accents/case)", () => {
      const options: SearchOptions = {
        attributeMap: { n: "info.name" },
        caseSensitive: false,
        matchMode: "exact"
      };

      // "arbol" should match "Árbol" due to normalization
      expect(searchData(mockData, "n:arbol", options)).toHaveLength(1);
      expect(searchData(mockData, "n:ÁRBOL", options)).toHaveLength(1);
    });

    it("respects match modes: exact, contains, startsWith", () => {
      const data = [{ val: "Testing" }];
      
      expect(searchData(data, "val:Test", { matchMode: "exact" })).toHaveLength(0);
      expect(searchData(data, "val:Test", { matchMode: "startsWith" })).toHaveLength(1);
      expect(searchData(data, "val:stin", { matchMode: "contains" })).toHaveLength(1);
    });

    it("handles logical operators AND/OR correctly", () => {
      const data = [
        { color: "red", size: "small" },
        { color: "red", size: "large" },
        { color: "blue", size: "large" }
      ];

      const query = "color:red size:large";

      expect(searchData(data, query, { operator: "AND", matchMode: "exact" })).toHaveLength(1);
      expect(searchData(data, query, { operator: "OR", matchMode: "exact" })).toHaveLength(3);
    });

    it("handles array values and object serialization in search", () => {
      const options: SearchOptions = { attributeMap: { t: "info.tags" }, matchMode: "contains" };
      expect(searchData(mockData, "t:nature", options)).toHaveLength(1);
    });
  });

  describe("LRUCache", () => {
    it("evicts the least recently used item when capacity is reached", () => {
      const cache = new LRUCache<string, number>(2);
      
      cache.set("a", 1);
      cache.set("b", 2);
      cache.get("a"); // "a" becomes most recent
      cache.set("c", 3); // "b" should be evicted

      expect(cache.get("a")).toBe(1);
      expect(cache.get("b")).toBeUndefined();
      expect(cache.get("c")).toBe(3);
    });

    it("clears all items", () => {
      const cache = new LRUCache<string, number>(10);
      cache.set("a", 1);
      cache.clear();
      expect(cache.get("a")).toBeUndefined();
    });
  });
});
