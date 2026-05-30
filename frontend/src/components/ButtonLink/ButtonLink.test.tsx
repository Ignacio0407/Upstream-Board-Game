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
import { describe, it, expect } from "vitest";
import ButtonLink from "./ButtonLink";
import { MemoryRouter } from "react-router-dom";

describe("ButtonLink Component", () => {
    it("renders correctly with given text and direction", () => {
        render(
            <MemoryRouter>
                <ButtonLink direction="/test" text="Click Me" color="primary" />
            </MemoryRouter>
        );

        const link = screen.getByRole("link", { name: /Click Me/i });
        expect(link).toBeInTheDocument();
        expect(link).toHaveAttribute("href", "/test");
    });

    it("applies the correct color class to the button", () => {
        render(
            <MemoryRouter>
                <ButtonLink direction="/test" text="Click Me" color="danger" />
            </MemoryRouter>
        );

        const button = screen.getByRole("button");
        expect(button).toHaveClass("btn-danger");
    });
});
