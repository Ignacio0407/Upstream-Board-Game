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

import React, { useEffect, useMemo, useRef, useState } from "react";
import useSearchEngine from "./useSearchEngine.ts";
import { LogicalOperator, MatchMode } from "./searchEngine.ts";
import "@fortawesome/fontawesome-free/css/all.min.css";
import "./SearchBar.css";

export interface SearchBarProps<T> {
  data: T[];
  setFiltered: (data: T[]) => void;
  placeholder?: string;
  attributeMap?: Record<string, string>;
  cacheCapacity?: number;
  debounceMs?: number;

  defaultOperator?: LogicalOperator;      // AND | OR
  defaultCaseSensitive?: boolean;         // true = strict case/diacritics/symbols
  defaultMatchMode?: MatchMode;           // "exact" | "contains" | "startsWith"
}

const SearchBar = <T extends object>({
  data,
  setFiltered,
  placeholder = "Buscar...",
  attributeMap = {},
  cacheCapacity = 200,
  debounceMs = 300,
  defaultOperator = "AND",
  defaultCaseSensitive = true,
  defaultMatchMode = "exact",
}: SearchBarProps<T>) => {
  // Search state
  const [searchTerm, setSearchTerm] = useState("");
  const [operator, setOperator] = useState<LogicalOperator>(defaultOperator);
  const [caseSensitive, setCaseSensitive] = useState<boolean>(defaultCaseSensitive);
  const [matchMode, setMatchMode] = useState<MatchMode>(defaultMatchMode);

  // UI state (dropdowns)
  const [showAttrDropdown, setShowAttrDropdown] = useState(false);
  const [showOpDropdown, setShowOpDropdown] = useState(false);
  const [showModeDropdown, setShowModeDropdown] = useState(false);

  // Refs
  const containerRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  // Engine options
  const options = useMemo(
    () => ({ attributeMap, operator, caseSensitive, matchMode }),
    [attributeMap, operator, caseSensitive, matchMode]
  );
  const { search } = useSearchEngine<T>(data, options, cacheCapacity);

  // Discover searchable attributes (explicit map + top-level keys)
  const searchableAttributes = useMemo(() => {
    const explicit = Object.keys(attributeMap);
    const dataAttrs = data[0] ? Object.keys(data[0] as object) : [];
    return Array.from(new Set([...explicit, ...dataAttrs.filter(a => !a.includes("."))]))
      .filter(a => a.toLowerCase() !== "id");
  }, [data, attributeMap]);

  // Debounced search
  useEffect(() => {
    const id = setTimeout(() => {
      setFiltered(searchTerm ? search(searchTerm) : data);
    }, debounceMs);
    return () => clearTimeout(id);
  }, [searchTerm, data, search, setFiltered, debounceMs]);

  // Close any open dropdown when clicking outside
  useEffect(() => {
    const onDocClick = (e: MouseEvent) => {
      if (!containerRef.current) return;
      if (!containerRef.current.contains(e.target as Node)) {
        setShowAttrDropdown(false);
        setShowOpDropdown(false);
        setShowModeDropdown(false);
      }
    };
    document.addEventListener("mousedown", onDocClick);
    return () => document.removeEventListener("mousedown", onDocClick);
  }, []);

  // Insert "attr:" into the search bar
  const handleAttributeClick = (attr: string) => {
    setSearchTerm(prev => (prev ? `${prev} ${attr}:` : `${attr}:`));
    setShowAttrDropdown(false);
    inputRef.current?.focus();
  };

  // Execute search immediately (enter key or button)
  const runSearchNow = () => setFiltered(search(searchTerm));

  // Labels for match mode
  const modeLabel: Record<MatchMode, string> = {
    exact: "Exact",
    contains: "Contains",
    startsWith: "Starts",
  };

  return (
    <div className="search-component" ref={containerRef}>
      <div className="search-container">
        {/* Controls row */}
        <div className="search-controls">
          {/* AND/OR pill with chevron dropdown */}
          <div className="pill-dropdown-container">
            <button
              type="button"
              className={`pill-toggle ${showOpDropdown ? "active" : ""}`}
              aria-haspopup="listbox"
              aria-expanded={showOpDropdown}
              aria-label="Select logical operator AND/OR"
              onClick={() => {
                setShowOpDropdown(s => !s);
                setShowModeDropdown(false);
                setShowAttrDropdown(false);
              }}
            >
              <span className="pill-toggle-label">{operator}</span>
              <i className="fa fa-chevron-down" aria-hidden="true" />
            </button>

            {showOpDropdown && (
              <div className="pill-menu" role="listbox" aria-label="Logical operator">
                <button
                  type="button"
                  role="option"
                  aria-selected={operator === "AND"}
                  className={`pill-option ${operator === "AND" ? "selected" : ""}`}
                  onClick={() => {
                    setOperator("AND");
                    setShowOpDropdown(false);
                  }}
                >
                  AND
                </button>
                <button
                  type="button"
                  role="option"
                  aria-selected={operator === "OR"}
                  className={`pill-option ${operator === "OR" ? "selected" : ""}`}
                  onClick={() => {
                    setOperator("OR");
                    setShowOpDropdown(false);
                  }}
                >
                  OR
                </button>
              </div>
            )}
          </div>

          {/* Case sensitive pill (toggle) */}
          <div className="pill-toggle-single">
            <button
              type="button"
              className={`pill-toggle ${caseSensitive ? "active" : ""}`}
              aria-pressed={caseSensitive}
              aria-label="Toggle case sensitivity"
              title={
                caseSensitive
                  ? "Strict: case/diacritics/symbols are respected"
                  : "Normalized: lowercased, diacritics/symbols removed"
              }
              onClick={() => setCaseSensitive(s => !s)}
            >
              <span className="pill-toggle-label">
                Case sensitive: {caseSensitive ? "ON" : "OFF"}
              </span>
            </button>
          </div>

          {/* Match mode pill with chevron dropdown */}
          <div className="pill-dropdown-container">
            <button
              type="button"
              className={`pill-toggle ${showModeDropdown ? "active" : ""}`}
              aria-haspopup="listbox"
              aria-expanded={showModeDropdown}
              aria-label="Select match mode"
              onClick={() => {
                setShowModeDropdown(s => !s);
                setShowOpDropdown(false);
                setShowAttrDropdown(false);
              }}
            >
              <span className="pill-toggle-label">{modeLabel[matchMode]}</span>
              <i className="fa fa-chevron-down" aria-hidden="true" />
            </button>

            {showModeDropdown && (
              <div className="pill-menu" role="listbox" aria-label="Match mode">
                <button
                  type="button"
                  role="option"
                  aria-selected={matchMode === "exact"}
                  className={`pill-option ${matchMode === "exact" ? "selected" : ""}`}
                  onClick={() => {
                    setMatchMode("exact");
                    setShowModeDropdown(false);
                  }}
                >
                  Exact
                </button>
                <button
                  type="button"
                  role="option"
                  aria-selected={matchMode === "contains"}
                  className={`pill-option ${matchMode === "contains" ? "selected" : ""}`}
                  onClick={() => {
                    setMatchMode("contains");
                    setShowModeDropdown(false);
                  }}
                >
                  Contains
                </button>
                <button
                  type="button"
                  role="option"
                  aria-selected={matchMode === "startsWith"}
                  className={`pill-option ${matchMode === "startsWith" ? "selected" : ""}`}
                  onClick={() => {
                    setMatchMode("startsWith");
                    setShowModeDropdown(false);
                  }}
                >
                  Starts
                </button>
              </div>
            )}
          </div>
        </div>

        {/* Attribute dropdown (chevron-only trigger) */}
        <div className="search-dropdown-container">
          <button
            type="button"
            className={`search-dropdown-toggle ${showAttrDropdown ? "active" : ""}`}
            aria-haspopup="listbox"
            aria-expanded={showAttrDropdown}
            aria-label="Show searchable attributes"
            onClick={() => {
              setShowAttrDropdown(s => !s);
              setShowOpDropdown(false);
              setShowModeDropdown(false);
            }}
          >
            <i className="fa fa-chevron-down" aria-hidden="true" />
          </button>

          {showAttrDropdown && (
            <div className="search-dropdown">
              {searchableAttributes.map(attr => (
                <button
                  type="button"
                  key={attr}
                  className="search-dropdown-item"
                  onClick={() => handleAttributeClick(attr)}
                >
                  {attr}
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Input + search button */}
        <input
          ref={inputRef}
          type="text"
          className="search-bar"
          placeholder={placeholder}
          aria-label="Search input"
          value={searchTerm}
          onChange={e => setSearchTerm(e.target.value)}
          onKeyDown={e => {
            if (e.ctrlKey && e.key.toLowerCase() === "a") {
              e.stopPropagation(); // prevents it from reaching ListGenerator
              return; 
            }

            if (e.key === "Enter") runSearchNow();
          }}
        />
        <button
          type="button"
          className="search-button"
          aria-label="Run search"
          onClick={runSearchNow}
          title="Search"
        >
          <i className="fa fa-search" aria-hidden="true" />
        </button>
      </div>
    </div>
  );
};

export default React.memo(SearchBar) as typeof SearchBar;