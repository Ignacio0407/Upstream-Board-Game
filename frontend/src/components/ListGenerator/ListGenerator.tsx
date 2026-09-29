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

import { useState, useMemo, JSX, useEffect } from "react";
import { Link, useNavigate  } from "react-router-dom";
import { Button } from "reactstrap";
import ListPagination from "./ListPagination.tsx";
import ListSortBar from "./ListSortBar.tsx";
import { BaseUser } from '../../interfaces/User.ts';
import "./listGenerator.css";
import { renderData, useCtrlAToSelectOrDeselect, useExitMassEditOnEscape } from "./ListGeneratorHelper.tsx";
import { navigateToUnauthorized } from "../DoNotHackMyApp/DoNotHackMyAppHelper.ts";
import SearchBar from "../SearchBar/SearchBar.tsx";
import MassEditWizard from "./MassEditWizard.tsx";

interface ListGeneratorProps<T extends { id: any }> {data: T[], columns?: (keyof T)[], readonly idKey?: keyof T | string, readonly actualUser: BaseUser,
  readonly customRender?: (item: T) => JSX.Element, readonly requiredAuth?: string, readonly checkAuth?: Function,
  readonly onUnauthorizedRedirect?: string, readonly deactivateSecurity?: boolean, readonly createButton?: string, readonly createButtonPosition?:string,
  readonly editCondition?: boolean, readonly editButton?: (item: T) => JSX.Element, readonly deleteCondition?: boolean, readonly deleteButton?: (item: T) => void,
  readonly massEditButton?: JSX.Element, readonly massEditUri?: string, readonly massDeleteButton?: (items: T[]) => void, readonly massRequiredAuth?:string, 
  readonly cssClasses?: readonly string[], readonly pageSize?: number, readonly searchBarPlaceholder?: string | null, readonly customLabels?: Record<string, string>}

/* This component generates a list of items with pagination, sorting, and mass editing capabilities.
sortedData
   ↓ (pagination)
paginatedData
   ↓ (search)
visibleData
   ↓
renderedData ← Unique render
*/

export default function ListGenerator<T extends { id: any }>({data, columns, idKey = "id", actualUser, customRender,
  requiredAuth = "authenticated", checkAuth, onUnauthorizedRedirect = "/do-not-hack-my-app", deactivateSecurity = false,
  createButton, createButtonPosition = "bottom",
  editCondition = false, editButton, massEditButton, massEditUri,
  deleteCondition = false, deleteButton, massDeleteButton, massRequiredAuth,
  cssClasses = [], pageSize = 10, searchBarPlaceholder = null, customLabels = {}}: Readonly<ListGeneratorProps<T>>) {

  const navigate = useNavigate();
  if (!deactivateSecurity) navigateToUnauthorized(checkAuth, actualUser, requiredAuth, navigate, onUnauthorizedRedirect);

  const keys = columns ?? (data.length > 0 ? (Object.keys(data[0] as object) as (keyof T)[]) : []);
  const [page, setPage] = useState(1);
  const [sortedData, setSortedData] = useState<T[]>(data);
  const [selectedIds, setSelectedIds] = useState<Set<any>>(new Set());
  const [filtered, setFiltered] = useState<T[]>(data);
  const [showMassEdit, setShowMassEdit] = useState(false);

  useEffect(() => { setSortedData(data); setFiltered(data); }, [data]);

  // Reset page when filtering changes to avoid being stuck on a page that doesn't exist in the filtered results
  useEffect(() => {
    setPage(1);
  }, [filtered]);

  const filteredData = useMemo(() => {
    return (searchBarPlaceholder && filtered) ? filtered : sortedData;
  }, [filtered, sortedData, searchBarPlaceholder]);

  const paginatedData = useMemo(() => {
    if (!Array.isArray(filteredData)) return [];
    const start = (page - 1) * pageSize;
    return filteredData.slice(start, start + pageSize);
  }, [filteredData, page, pageSize]);

  const visibleData = paginatedData;
  
  const selectedItems = visibleData.filter(item => selectedIds.has(item[idKey as keyof T]));

  const handlePageChange = (newPage: number) => { setSelectedIds(new Set()); setPage(newPage); };

  useCtrlAToSelectOrDeselect(paginatedData, idKey as keyof T, selectedIds, setSelectedIds, showMassEdit);

  useExitMassEditOnEscape(setShowMassEdit);

  const onClose = (updated?: T[] | null) => {
    setShowMassEdit(false);
    setSelectedIds(new Set());
    
    if (!updated?.length) return;
    setSortedData(prev =>
      prev.map(item => {
        const u = updated.find(x => x.id === item.id);
        return u ?? item;
      })
    );
  };

  const toggleSelectAll = () => {
    const allSelected = visibleData.every(item => selectedIds.has(item[idKey as keyof T]));
    const newSet = new Set(selectedIds);
    if (allSelected) visibleData.forEach(item => newSet.delete(item[idKey as keyof T]));
    else visibleData.forEach(item => newSet.add(item[idKey as keyof T]));
    setSelectedIds(newSet);
  };

  const toggleSelectOne = (id: any) => {
    const newSet = new Set(selectedIds); newSet.has(id) ? newSet.delete(id) : newSet.add(id); setSelectedIds(newSet);
  };

  const renderedData = useMemo(() => {
    return renderData({
      paginatedData: visibleData,
      selectedIds,
      idKey,
      toggleSelectAll,
      keys,
      editButton: editCondition ? editButton : undefined,
      deleteButton: deleteCondition ? deleteButton : undefined,
      toggleSelectOne,
      customLabels,
      customRender
    });
  }, [visibleData, selectedIds, idKey, keys, editCondition, editButton, deleteCondition, deleteButton,
      toggleSelectAll, toggleSelectOne, customLabels, customRender]);

  return (
    <div className={`list-generator-container ${cssClasses.join(" ")}`}>
      <ListSortBar data={data} onSort={setSortedData} />

      {selectedItems.length > 0 && (editCondition || deleteCondition) && (
        <div className="mass-actions-bar">
          <span className="selected-counter">{selectedItems.length} selected</span>

          {editCondition && massEditButton && (
            <button
              type="button"
              style={{ background: 'transparent', border: 'none', padding: 0 }}
              onClick={() => setShowMassEdit(true)}
            >
              {massEditButton}
            </button>
          )}

            {deleteCondition && massDeleteButton &&
              <Button color="danger" onClick={() => massDeleteButton(selectedItems)}>Delete</Button>}
        </div>
      )}

      {showMassEdit && (
        <div className="mass-edit-overlay">
          <button
            type="button"
            aria-label="Close mass edit modal"
            style={{ position: 'fixed', inset: 0, width: '100%', height: '100%', background: 'transparent', border: 'none', cursor: 'default' }}
            onClick={() => setShowMassEdit(false)}
          />
          <div className="mass-edit-modal" style={{ position: 'relative', zIndex: 1 }}>
            {/* Prevent closing when clicking inside the modal */}
            <div className="mass-edit-content">
              <div className="mass-edit-header">
                <h2>Edit {selectedItems.length} elements</h2>
                <Button color="danger" onClick={() => { setShowMassEdit(false); setSelectedIds(new Set()); }}> ✕ Close </Button>
              </div>

              <MassEditWizard items={selectedItems} massEditUri={massEditUri!} renderItem={customRender!}
                actualUser={actualUser} massRequiredAuth={massRequiredAuth!} onClose={(updated) => onClose(updated)}/>
            </div>
          </div>
        </div>
      )}

      {searchBarPlaceholder && <SearchBar data={sortedData} setFiltered={setFiltered} placeholder={searchBarPlaceholder} defaultCaseSensitive={true}/>}

      {createButtonPosition === "top" && createButton &&
        <Button color="success" tag={Link} to={createButton} className="mt-3">Create</Button>}

      {renderedData}

      <ListPagination totalItems={filteredData.length} pageSize={pageSize} currentPage={page} onPageChange={handlePageChange}/>

      {createButtonPosition === "bottom" && createButton &&
        <Button color="success" tag={Link} to={createButton} className="mt-3">Create</Button>}
    </div>
  );
}