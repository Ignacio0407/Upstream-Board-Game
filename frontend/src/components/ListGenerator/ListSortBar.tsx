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

import { useState } from "react";

interface Props<T> {data: T[], onSort: (sorted: T[]) => void}

export default function ListSortBar<T>({ data, onSort }: Props<T>) {
  const keys = data.length > 0 ? Object.keys(data[0] as object).filter(k => k !== "id") : [];
  const [selectedKey, setSelectedKey] = useState<string>("");

  const handleSort = (order: "asc" | "desc") => {
    if (!selectedKey) return;
    const sorted = [...data].sort((a, b) => {
      const valA = a[selectedKey as keyof T];
      const valB = b[selectedKey as keyof T];
      if (typeof valA === "number" && typeof valB === "number") {
        return order === "asc" ? valA - valB : valB - valA;
      }
      return order === "asc"
        ? String(valA).localeCompare(String(valB))
        : String(valB).localeCompare(String(valA));
    });
    onSort(sorted);
  };

  return (
    <div className="sort-bar">
      <select value={selectedKey} onChange={(e) => setSelectedKey(e.target.value)}>
        <option value="">Sort by...</option>
        {keys.map((key) => (<option key={key} value={key}> {key}</option>))}
      </select>
      <button type="button" onClick={() => handleSort("asc")}>↑</button>
      <button type="button" onClick={() => handleSort("desc")}>↓</button>
      <button type="button" onClick={() => { setSelectedKey(""); onSort(data); }}>✖</button>
    </div>
  );
}