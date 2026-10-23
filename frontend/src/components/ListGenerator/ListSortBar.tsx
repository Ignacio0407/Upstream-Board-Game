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

import { useState, ChangeEvent } from "react";
import "./listGenerator.css";

interface Props<T> {data: T[], readonly onSort: (sorted: T[]) => void}

export default function ListSortBar<T>({ data, onSort }: Readonly<Props<T>>) {
  const keys = data.length > 0 ? Object.keys(data[0] as object).filter(k => k !== "id") : [];
  const [selectedKey, setSelectedKey] = useState<string>("");
  const [sortOrder, setSortOrder] = useState<"asc" | "desc" | null>(null);

  const handleSort = (order: "asc" | "desc") => {
    if (!selectedKey) return;
    setSortOrder(order);
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

  const handleReset = () => {
    setSelectedKey("");
    setSortOrder(null);
    onSort(data);
  };

  const handleKeyChange = (e: ChangeEvent<HTMLSelectElement>) => {
    setSelectedKey(e.target.value);
    setSortOrder(null);
  };

  return (
    <div className="sort-bar">
      <div className="sort-bar-select-wrapper">
        <select 
          className="sort-bar-select"
          value={selectedKey} 
          onChange={handleKeyChange}
        >
          <option value="">Sort by...</option>
          {keys.map((key) => (<option key={key} value={key}> {key}</option>))}
        </select>
      </div>
      <div className="sort-bar-buttons">
        <button 
          type="button" 
          className={`sort-bar-btn sort-bar-btn-asc ${sortOrder === "asc" ? "active" : ""}`}
          onClick={() => handleSort("asc")}
          title="Sort ascending"
        >
          ↑
        </button>
        <button 
          type="button" 
          className={`sort-bar-btn sort-bar-btn-desc ${sortOrder === "desc" ? "active" : ""}`}
          onClick={() => handleSort("desc")}
          title="Sort descending"
        >
          ↓
        </button>
        <button 
          type="button" 
          className="sort-bar-btn sort-bar-btn-reset"
          onClick={handleReset}
          title="Clear sorting"
        >
          ✖
        </button>
      </div>
    </div>
  );
}
