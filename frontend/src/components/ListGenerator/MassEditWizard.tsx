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

import { JSX, useState } from "react";
import { Button } from "reactstrap";
import { EditFormGenerator } from "../Form/EditFormGenerator.tsx";
import { ReviewModal } from "./ReviewModal.tsx";
import { put } from "../../util/fetchers.ts";
import tokenService from "../../services/token.service.ts";

interface EditTuple<T> {
  original: T;
  versions: T[];
  activeVersion: number;
}

interface MassEditWizardProps<T extends { id: any }> {
  items: T[];
  massEditUri: string;
  renderItem: (item: T) => JSX.Element;
  actualUser: any;
  massRequiredAuth: string;
  onClose: (updated: T[] | null) => void;
}

export default function MassEditWizard<T extends { id: any }>({items, massEditUri, renderItem, actualUser, massRequiredAuth,
  onClose}: MassEditWizardProps<T>) {

  const jwt = tokenService.getLocalAccessToken();
  const [editHistory, setEditHistory] = useState<EditTuple<T>[]>(
    items.map(i => ({
      original: structuredClone(i),
      versions: [],
      activeVersion: -1
    }))
  );

  const [activeIndex, setActiveIndex] = useState(0);
  const [showReview, setShowReview] = useState(false);

  const goPrev = () => activeIndex > 0 && setActiveIndex(i => i - 1);
  const goNext = () => activeIndex < editHistory.length - 1 && setActiveIndex(i => i + 1);

  const current = editHistory[activeIndex];
  const currentEntity =
    current.activeVersion >= 0
      ? current.versions[current.activeVersion]
      : current.original;

  function hasChanges<T extends object>(original: T, edited: T): boolean {
    return (Object.keys(original) as (keyof T)[]).some(key => original[key] !== edited[key]);
  }

  const applyUpdate = (updated: T) => {
    if (hasChanges(currentEntity, updated)) {
      setEditHistory(h => {
      const c = [...h];
      const t = c[activeIndex];
      const v = [...t.versions.slice(0, t.activeVersion + 1), structuredClone(updated)];
      c[activeIndex] = { ...t, versions: v, activeVersion: v.length - 1 };
      return c;
    });
    }
    setActiveIndex(i => Math.min(i + 1, editHistory.length - 1));
    if (activeIndex === items.length - 1) setShowReview(true);
  };


  const undo = () =>
    setEditHistory(h => {
      const c = [...h];
      if (c[activeIndex].activeVersion >= 0) c[activeIndex].activeVersion--;
      return c;
    });

  const redo = () =>
    setEditHistory(h => {
      const c = [...h];
      const t = c[activeIndex];
      if (t.activeVersion < t.versions.length - 1) t.activeVersion++;
      return c;
    });

  const confirmAll = async () => {
    const changed = editHistory
      .filter(t => t.activeVersion >= 0 && t.versions.length > 0)
      .map(t => ({
        id: t.original.id,
        entity: t.versions[t.activeVersion]
      }));

    if (!changed.length) return onClose(null);

    const updated = await put(massEditUri, jwt, {
      ids: changed.map(c => c.id),
      entities: changed.map(c => c.entity)
    }).then(r => r.json());

    onClose(updated);
  };

  return (
    <div className="auth-page-container">
      <h3>Editing {activeIndex + 1} / {items.length}</h3>

      <EditFormGenerator<T> key={`${current.original.id}-${current.activeVersion}`} entityToEdit={currentEntity} 
        onSubmit={applyUpdate} submitText="Update & Next" actualUser={actualUser} requiredAuth={massRequiredAuth}
        cancelButtonActivated={false}
        extraBeforeSubmit={
          <div style={{ display: "flex", gap: 10 }}>
            <Button disabled={!activeIndex} onClick={goPrev}>←</Button>
            <Button disabled={activeIndex === editHistory.length - 1} onClick={goNext}>→</Button>
            <Button disabled={current.activeVersion < 0} onClick={undo}>Undo</Button>
            <Button disabled={current.activeVersion >= current.versions.length - 1} onClick={redo}>Redo</Button>
            {activeIndex === editHistory.length - 1 &&
              <Button color="success" type="button" onClick={() => setShowReview(true)}> Review </Button>}
          </div>
        }
      />

      {showReview &&
        <ReviewModal originals={editHistory.map(t => t.original)} renderItem={renderItem} onConfirm={confirmAll}
          updated={editHistory.map(t =>
            t.versions.length ? t.versions[t.activeVersion] : undefined
          )}
        />
      }
    </div>
  );
}