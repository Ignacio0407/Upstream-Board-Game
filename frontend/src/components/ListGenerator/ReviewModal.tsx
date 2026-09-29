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