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
