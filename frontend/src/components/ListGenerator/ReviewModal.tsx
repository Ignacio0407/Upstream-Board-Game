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

import { JSX } from "react";
import { Button } from "reactstrap";
import "./listGenerator.css";

interface ReviewModalProps<T> {readonly originals: readonly T[], readonly updated: readonly (T | undefined)[], readonly renderItem: (original: T, updated?: T) => JSX.Element,
  readonly onConfirm: () => void}

export function ReviewModal<T>({originals, updated, renderItem, onConfirm}: Readonly<ReviewModalProps<T>>) {
  return (
    <div className="mass-edit-modal">
      <h2>Review changes</h2>

      {originals.map((orig, i) => (
        <div key={(orig as { id?: any })?.id ?? (orig as { name?: any })?.name ?? `review-${i}`} className="review-block">
          {updated[i] ? (
            <>
              <div className="review-original">Old {renderItem(orig)}</div>
              <div className="review-divider"/>
              <div className="review-updated">New {renderItem(updated[i]!)}</div>
            </>
            )
          : <p><strong>Not updated</strong></p>}
        </div>
      ))}

      <Button color="success" onClick={onConfirm}> Confirm all </Button>
    </div>
  );
}