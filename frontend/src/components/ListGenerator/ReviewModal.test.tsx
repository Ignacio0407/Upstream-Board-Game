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

import { render, screen } from "@testing-library/react";
import { ReviewModal } from "./ReviewModal";
import { vi } from "vitest";

const originals = [{ id: 1, name: "Charlie" }, { id: 2, name: "Alpha" }];
const updated = [undefined, { id: 2, name: "Alpha Edited" }];

describe("ReviewModal — UI", () => {

  test("Shows 'Not updated' for items with NO changes", () => {
    render(
      <ReviewModal 
        originals={originals} 
        updated={updated} 
        renderItem={(i) => <span>{i.name}</span>} 
        onConfirm={() => {}} 
      />
    );
    expect(screen.getByText("Not updated")).toBeInTheDocument();
  });

  test("Shows 'Old' and 'New' for items WITH changes", () => {
    render(
      <ReviewModal 
        originals={originals} 
        updated={updated} 
        renderItem={(i: any) => <span>{i.name}</span>} 
        onConfirm={() => {}} 
      />
    );

    // Using a matcher function to find text broken across multiple elements
    const oldTextFound = screen.getByText((_content, element) => {
      return element?.textContent === "Old Alpha";
    });
    const newTextFound = screen.getByText((_content, element) => {
      return element?.textContent === "New Alpha Edited";
    });

    expect(oldTextFound).toBeInTheDocument();
    expect(newTextFound).toBeInTheDocument();
  });

  test("Calls onConfirm when clicking confirm button", () => {
    const confirm = vi.fn();
    render(
      <ReviewModal originals={originals} updated={updated} renderItem={vi.fn()} onConfirm={confirm} />
    );
    screen.getByText("Confirm all").click();
    expect(confirm).toHaveBeenCalled();
  });

});
