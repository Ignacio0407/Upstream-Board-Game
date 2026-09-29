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

import { useCallback, useEffect, useRef } from "react";
import { LRUCache, SearchOptions, searchData } from "./searchEngine.ts";

const useSearchEngine = <T,> (
  data: T[],
  options: SearchOptions,
  cacheCapacity = 200
) => {
  const cacheRef = useRef(new LRUCache<string, T[]>(cacheCapacity));

  const search = useCallback(
    (query: string): T[] => {
      const trimmed = query.trim();
      if (!trimmed) return data;

      const cacheKey = JSON.stringify({ q: trimmed, o: options });
      const cached = cacheRef.current.get(cacheKey);
      if (cached) return cached;

      const results = searchData<T>(data, trimmed, options);
      cacheRef.current.set(cacheKey, results);
      return results;
    },
    [data, options]
  );

  useEffect(() => { cacheRef.current.clear(); }, [data, options]);

  return { search };
};

export default useSearchEngine;