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