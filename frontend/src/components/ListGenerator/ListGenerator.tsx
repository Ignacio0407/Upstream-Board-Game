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

interface ListGeneratorProps<T extends { id: any }> {data: T[], columns?: (keyof T)[], idKey?: keyof T | string, actualUser: BaseUser,
  customRender?: (item: T) => JSX.Element, requiredAuth?: string, checkAuth?: Function,
  onUnauthorizedRedirect?: string, deactivateSecurity?: boolean, createButton?: string, createButtonPosition?:string,
  editCondition?: boolean, editButton?: (item: T) => JSX.Element, deleteCondition?: boolean, deleteButton?: (item: T) => void,
  massEditButton?: JSX.Element, massEditUri?: string, massDeleteButton?: (items: T[]) => void, massRequiredAuth?:string, 
  cssClasses?: string[], pageSize?: number, searchBarPlaceholder?: string | null, customLabels?: Record<string, string>}

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
  cssClasses = [], pageSize = 10, searchBarPlaceholder = null, customLabels = {}}: ListGeneratorProps<T>) {

  const navigate = useNavigate();
  if (!deactivateSecurity) navigateToUnauthorized(checkAuth, actualUser, requiredAuth, navigate, onUnauthorizedRedirect);

  const keys = columns ?? (data.length > 0 ? (Object.keys(data[0] as object) as (keyof T)[]) : []);
  const [page, setPage] = useState(1);
  const [sortedData, setSortedData] = useState<T[]>(data);
  const [selectedIds, setSelectedIds] = useState<Set<any>>(new Set());
  const [filtered, setFiltered] = useState<T[]>(data);
  const [showMassEdit, setShowMassEdit] = useState(false);
  const [outsideClicks, setOutsideClicks] = useState(0);

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
    return renderData(visibleData, selectedIds, idKey, toggleSelectAll, keys,
      editCondition ? editButton : undefined,
      deleteCondition ? deleteButton : undefined,
      toggleSelectOne, navigate, customLabels, customRender);
  }, [visibleData, selectedIds, idKey, keys, editCondition, editButton, deleteCondition, deleteButton,
      toggleSelectAll, toggleSelectOne, navigate, customLabels, customRender]);

  return (
    <div className={`list-generator-container ${cssClasses.join(" ")}`}>
      <ListSortBar data={data} onSort={setSortedData} />

      {selectedItems.length > 0 && (editCondition || deleteCondition) && (
        <div className="mass-actions-bar">
          <span className="selected-counter">{selectedItems.length} selected</span>

          {editCondition && massEditButton && (<div onClick={() => {setShowMassEdit(true)}}> {massEditButton} </div>)}

            {deleteCondition && massDeleteButton &&
              <Button color="danger" onClick={() => massDeleteButton(selectedItems)}>Delete</Button>}
        </div>
      )}

      {showMassEdit && (
        <div className="mass-edit-overlay" 
          role="dialog"
          aria-modal="true"
          aria-label="Mass edit modal"
          onClick={() => {
            setOutsideClicks(c => {
              if (c + 1 >= 2) {
                setShowMassEdit(false);
                return 0;
              }
              return c + 1;
            });
          }} >
          
          <div className="mass-edit-modal" onClick={e => {e.stopPropagation(); setOutsideClicks(0)}}>
            {/* Prevent closing when clicking inside the modal */}
            <div className="mass-edit-content">
              <div className="mass-edit-header">
                <h2>Edit {selectedItems.length} elements</h2>
                <Button color="danger" onClick={() => { setShowMassEdit(false); setSelectedIds(new Set()) }}> ✕ Close </Button>
              </div>

              <MassEditWizard items={selectedItems} massEditUri={massEditUri!} renderItem={customRender!} actualUser={actualUser}
                massRequiredAuth={massRequiredAuth!} onClose={(updated) => onClose(updated)}/>
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