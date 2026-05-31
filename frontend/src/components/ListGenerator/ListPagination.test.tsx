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

import { vi } from 'vitest';
import { render, screen, fireEvent } from "@testing-library/react";
import ListPagination from "./ListPagination.tsx";


describe("ListPagination — Visibility", () => {

  test("Renders pagination when there is more than 1 page", () => {
    render(<ListPagination totalItems={11} pageSize={10} currentPage={1} onPageChange={() => {}} />);
    expect(screen.getByText("1")).toBeInTheDocument();
    expect(screen.getByText("2")).toBeInTheDocument();
  });

  test.each([
    ["totalItems < pageSize",    5,  10],
    ["totalItems === 0",         0,  10],
    ["totalItems === pageSize", 10,  10],
  ])("Returns null when %s", (_label, totalItems, pageSize) => {
    const { container } = render(
      <ListPagination totalItems={totalItems} pageSize={pageSize} currentPage={1} onPageChange={() => {}} />
    );
    expect(container.firstChild).toBeNull();
  });

});


describe("ListPagination — Page number generated - Math.ceil(totalItems / pageSize)", () => {

  test.each([
    // [descripción,          totalItems, pageSize, expectedPages]
    ["Exact division",              30,       10,             3],
    ["resto > 0 (ceil)",            21,       10,             3],
    ["Extra item",                  11,       10,             2],
    ["pageSize other than 10",      10,        5,             2],
    ["pageSize=1",                   3,        1,             3],
  ])("%s: %i ítems / pageSize %i → %i pages", (_label, totalItems, pageSize, expectedPages) => {
    render(
      <ListPagination totalItems={totalItems} pageSize={pageSize} currentPage={1} onPageChange={() => {}} />
    );
    expect(screen.getByText(String(expectedPages))).toBeInTheDocument();
    expect(screen.queryByText(String(expectedPages + 1))).not.toBeInTheDocument();
  });

});


describe("ListPagination — Active page", () => {

  test("Only page corresponding to currentPage has 'active' class", () => {
    render(<ListPagination totalItems={30} pageSize={10} currentPage={2} onPageChange={() => {}} />);

    expect(screen.getByText("2").closest("li")).toHaveClass("active");
    expect(screen.getByText("1").closest("li")).not.toHaveClass("active");
    expect(screen.getByText("3").closest("li")).not.toHaveClass("active");
  });

});


describe("ListPagination — Callback onPageChange", () => {

  test("Calls onPageChange with correct number when clicking on each page", () => {
    const handler = vi.fn();
    render(<ListPagination totalItems={30} pageSize={10} currentPage={1} onPageChange={handler} />);

    fireEvent.click(screen.getByText("2"));
    expect(handler).toHaveBeenCalledWith(2);

    fireEvent.click(screen.getByText("3"));
    expect(handler).toHaveBeenCalledWith(3);
  });

  test("Calls onPageChange even if active page is clicked", () => {
    const handler = vi.fn();
    render(<ListPagination totalItems={30} pageSize={10} currentPage={2} onPageChange={handler} />);
    fireEvent.click(screen.getByText("2"));
    expect(handler).toHaveBeenCalledWith(2);
  });

  test("Does NOT call onPageChange if no click is performed", () => {
    const handler = vi.fn();
    render(<ListPagination totalItems={30} pageSize={10} currentPage={1} onPageChange={handler} />);
    expect(handler).not.toHaveBeenCalled();
  });

});