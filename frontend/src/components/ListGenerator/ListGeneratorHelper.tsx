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

import { NavigateFunction } from "react-router-dom";
import { JSX, useEffect } from "react";
import { Button, ButtonGroup, Table } from "reactstrap";

export function useCtrlAToSelectOrDeselect<T>(paginatedData: T[], idKey: keyof T, selectedIds: Set<any>, setSelectedIds: Function,
  isUnabled: boolean) {
  useEffect(() => {
    if (isUnabled === true) return;
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === "a") {
        e.preventDefault();
        const allSelected = paginatedData.every(item => selectedIds.has(item[idKey]));
        const newSet = new Set(selectedIds);
        if (allSelected) paginatedData.forEach(item => newSet.delete(item[idKey]));
        else paginatedData.forEach(item => newSet.add(item[idKey]));
        setSelectedIds(newSet);
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [paginatedData, idKey, setSelectedIds, isUnabled]);
}

export function useExitMassEditOnEscape(setShowMassEdit:React.Dispatch<React.SetStateAction<boolean>>) {
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => e.key === "Escape" && setShowMassEdit(false);
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, []);
}


export interface RenderDataConfig<T> {paginatedData: T[]; selectedIds: Set<any>; idKey: string | keyof T;
  toggleSelectAll: () => void; keys: (keyof T)[]; 
  editButton?: (item: T) => JSX.Element | undefined; deleteButton?: (item: T) => void;
  toggleSelectOne: (id: any) => void; navigate?: NavigateFunction;
  customLabels?: Record<string, string>; customRender?: (item: T) => JSX.Element;
}

export function renderData<T>(config: RenderDataConfig<T>) {
  const {paginatedData, selectedIds, idKey, toggleSelectAll, keys, 
    editButton, deleteButton, toggleSelectOne, customLabels = {}, customRender} = config;

  return (
  <Table className="mt-4">
    <thead>
      <tr>
        <th>
          <input type="checkbox"
            checked={paginatedData.length > 0 && paginatedData.every(item => selectedIds.has(item[idKey as keyof T]))}
            onChange={toggleSelectAll}/>
        </th>
        {keys.map((key) => {
          const keyStr = String(key);
          const label = customLabels[keyStr] || (keyStr.charAt(0).toUpperCase() + keyStr.slice(1));
          return <th key={keyStr}>{label}</th>;
        })}
        {(editButton !== undefined || deleteButton !== undefined) && <th>Actions</th>}
      </tr>
    </thead>
    <tbody>
      {paginatedData.map((item, idx) => (
        <tr key={String(item[idKey as keyof T] ?? idx)}>
          <td>
            <input type="checkbox"
              checked={selectedIds.has(item[idKey as keyof T])}
              onChange={() => toggleSelectOne(item[idKey as keyof T])}/>
          </td>

          {customRender ? customRender(item) : keys.map((key) => (<td key={String(key)}>{String(item[key])}</td>))
          }

          {(editButton || deleteButton) && (
            <td>
              <ButtonGroup>
                {editButton?.(item)}
                {deleteButton !== undefined && (
                  <Button size="sm" color="danger" onClick={() => deleteButton(item)}>Delete</Button>
                )}
              </ButtonGroup>
            </td>
          )}
        </tr>
      ))}
    </tbody>
  </Table>
  );
}