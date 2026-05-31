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
import { renderHook } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import useSearchEngine from "./useSearchEngine";
import * as engine from "./searchEngine";

describe("useSearchEngine.ts - Hook Tests", () => {
  const mockData = [{ id: 1, name: "Test" }];
  const options: engine.SearchOptions = { matchMode: "contains" };

  it("returns results from engine and utilizes caching", () => {
    // Spy on searchData to verify cache hits
    const searchDataSpy = vi.spyOn(engine, "searchData");
    
    const { result } = renderHook(() => useSearchEngine(mockData, options));
    
    // First search: should call engine
    const res1 = result.current.search("name:Test");
    expect(res1).toHaveLength(1);
    expect(searchDataSpy).toHaveBeenCalledTimes(1);

    // Second search (identical): should use cache
    const res2 = result.current.search("name:Test");
    expect(res2).toEqual(res1);
    expect(searchDataSpy).toHaveBeenCalledTimes(1); // Still 1

    searchDataSpy.mockRestore();
  });

  it("clears cache when data or options change", () => {
    const searchDataSpy = vi.spyOn(engine, "searchData");
    
    const { result, rerender } = renderHook(
      ({ data, opts }) => useSearchEngine(data, opts),
      { initialProps: { data: mockData, opts: options } }
    );

    result.current.search("name:Test");
    expect(searchDataSpy).toHaveBeenCalledTimes(1);

    // Rerender with new data
    rerender({ data: [...mockData, { id: 2, name: "New" }], opts: options });
    
    // Should call engine again because cache was cleared
    result.current.search("name:Test");
    expect(searchDataSpy).toHaveBeenCalledTimes(2);

    searchDataSpy.mockRestore();
  });

  it("returns full data if query is empty", () => {
    const { result } = renderHook(() => useSearchEngine(mockData, options));
    expect(result.current.search("   ")).toEqual(mockData);
  });
});
