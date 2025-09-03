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
import { MemoryRouter } from "react-router-dom";
import ButtonLink from "./ButtonLink.tsx";
import "@testing-library/jest-dom";

describe("ButtonLink Component", () => {
  const defaultProps = {
    direction: "/test-route",
    text: "Test Button",
  };

  const setup = (props = {}) => {
    const utils = render(
      <MemoryRouter>
        <ButtonLink {...defaultProps} {...props} />
      </MemoryRouter>
    );
    const button = screen.getByRole("button");
    const link = button.querySelector("a");
    return {
      button,
      link,
      ...utils,
    };
  };

  describe("Rendering", () => {
    it("renders with default props", () => {
      const { button, link } = setup();
      expect(button).toBeInTheDocument();
      expect(link).toBeInTheDocument();
      expect(link).toHaveAttribute("href", "/test-route");
      expect(button).toHaveClass("btn", "btn-white");
    });

    it("applies correct className to the link", () => {
      const { link } = setup();
      expect(link).toHaveClass("btn", "sm");
    });

    it("renders with different text content", () => {
      const { link } = setup({ text: "Different Text" });
      expect(link).toHaveTextContent("Different Text");
    });
  });

  describe("Props validation", () => {
    it("handles empty direction prop", () => {
      const { link } = setup({ direction: "" });
      expect(link).toHaveAttribute("href", "/");
    });

    it("handles empty text prop", () => {
      const { link } = setup({ text: "" });
      expect(link).toHaveTextContent("");
    });
  });

  describe("Style and Appearance", () => {
    it("applies custom color class to button", () => {
      const { button } = setup({ color: "red" });
      expect(button).toHaveClass("btn", "btn-red");
    });

    it("applies outline class when outline is true", () => {
      const { button } = setup({ color: "blue", outline: true });
      expect(button).toHaveClass("btn", "btn-outline-blue");
    });

    it("maintains text decoration none", () => {
      const { link } = setup();
      expect(link).toHaveStyle({
        textDecoration: "none",
      });
    });
  });

  describe("Edge cases", () => {
    it("handles very long text", () => {
      const longText = "A".repeat(100);
      const { link } = setup({ text: longText });
      expect(link).toHaveTextContent(longText);
    });

    it("handles special characters in text", () => {
      const specialText = "!@#$%^&*()_+";
      const { link } = setup({ text: specialText });
      expect(link).toHaveTextContent(specialText);
    });

    it("handles special characters in direction", () => {
      const specialDirection = "/test/route/with/special/chars/!@#$";
      const { link } = setup({ direction: specialDirection });
      expect(link).toHaveAttribute("href", specialDirection);
    });
  });
});