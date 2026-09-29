/**
 * -----------------------------------------------------------------------------
 * Autor / Author: Ignacio Gutiérrez Serrera
 * GitHub: https://github.com/Ignacio0407 (@Ignacio0407)
 * Contact / Email: ignaciogutierrezserrera@gmail.com
 * 
 * 🇬🇧 NON-COMMERCIAL & EDUCATIONAL LICENSE:
 * This software was developed by Ignacio Gutiérrez Serrera.
 * Permission is granted to use, copy, modify, and study this software for 
 * non-commercial, personal, academic, and educational purposes, provided 
 * that this copyright notice, author attribution, and contact details are 
 * preserved in all copies or substantial portions of the software.
 * 
 * Any commercial use, integration into proprietary software, or closed-source 
 * distribution is strictly prohibited without prior written permission and an 
 * express commercial license granted by the author.
 * 
 * 🇪🇸 LICENCIA PARA FINES NO LUCRATIVOS Y EDUCATIVOS:
 * Este software ha sido desarrollado por Ignacio Gutiérrez Serrera.
 * Se autoriza su uso, copia, modificación y estudio exclusivamente para 
 * fines personales, académicos, de investigación y educativos no lucrativos, 
 * con la condición obligatoria de mantener intacto este aviso de copyright, 
 * la atribución de autoría y los datos de contacto en todas las copias.
 * 
 * Queda terminantemente prohibido cualquier uso comercial, integración en 
 * software privativo o distribución con fines lucrativos sin la autorización 
 * previa por escrito del autor y la adquisición de una licencia comercial.
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
