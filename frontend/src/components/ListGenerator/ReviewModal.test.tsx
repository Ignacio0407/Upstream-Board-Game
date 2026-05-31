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
