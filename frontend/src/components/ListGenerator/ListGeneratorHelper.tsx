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