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
import ListSortBar from "./ListSortBar.tsx";

const dataset = [
  { id: 1, property1: "Charlie", property2: 3 },
  { id: 2, property1: "Alpha",   property2: 1 },
  { id: 3, property1: "Bravo",   property2: 2 },
];


describe("ListSortBar — Basic render", () => {

  test("Renders select, order and reset buttons", () => {
    render(<ListSortBar data={dataset} onSort={vi.fn()} />);
    expect(screen.getByRole("combobox")).toBeInTheDocument();
    expect(screen.getByText("↑")).toBeInTheDocument();
    expect(screen.getByText("↓")).toBeInTheDocument();
    expect(screen.getByText("✖")).toBeInTheDocument();
  });

  test("Select starts empty", () => {
    render(<ListSortBar data={dataset} onSort={vi.fn()} />);
    expect(screen.getByRole("combobox")).toHaveValue("");
  });

  test("'id' is not included in select options", () => {
    render(<ListSortBar data={dataset} onSort={vi.fn()} />);
    expect(screen.queryByRole("option", { name: "id" })).not.toBeInTheDocument();
  });

  test("Includes non-id properties as options", () => {
    render(<ListSortBar data={dataset} onSort={vi.fn()} />);
    expect(screen.getByRole("option", { name: /property1/i })).toBeInTheDocument();
    expect(screen.getByRole("option", { name: /property2/i })).toBeInTheDocument();
  });

  test("Does NOT render options with empty dataset", () => {
    render(<ListSortBar data={[]} onSort={vi.fn()} />);
    expect(screen.getAllByRole("option")).toHaveLength(1); // only "Sort by..."
  });

});


describe("ListSortBar — Sorting (strings y números, asc y desc)", () => {

  test.each([
    // [description, key, button, expectedOrder]
    ["string asc",  "property1", "↑", [...dataset].sort((a, b) => a.property1.localeCompare(b.property1))],
    ["string desc", "property1", "↓", [...dataset].sort((a, b) => b.property1.localeCompare(a.property1))],
    ["number asc",  "property2", "↑", [...dataset].sort((a, b) => a.property2 - b.property2)],
    ["number desc", "property2", "↓", [...dataset].sort((a, b) => b.property2 - a.property2)],
  ])("%s", (_label, key, button, expected) => {
    const onSort = vi.fn();
    render(<ListSortBar data={dataset} onSort={onSort} />);
    fireEvent.change(screen.getByRole("combobox"), { target: { value: key } });
    fireEvent.click(screen.getByText(button));
    expect(onSort).toHaveBeenCalledWith(expected);
  });

  test.each(["↑", "↓"])(
    "Does NOT call onSort upon clicking '%s' without selected property",
    (button) => {
      const onSort = vi.fn();
      render(<ListSortBar data={dataset} onSort={onSort} />);
      fireEvent.click(screen.getByText(button));
      expect(onSort).not.toHaveBeenCalled();
    }
  );

});


describe("ListSortBar — Reset (✖)", () => {

  test("Restores the original dataset and cleans select", () => {
    const onSort = vi.fn();
    render(<ListSortBar data={dataset} onSort={onSort} />);
    fireEvent.change(screen.getByRole("combobox"), { target: { value: "property1" } });
    fireEvent.click(screen.getByText("✖"));
    expect(onSort).toHaveBeenCalledWith(dataset);
    expect(screen.getByRole("combobox")).toHaveValue("");
  });

});


describe("ListSortBar — Consecutive sorts", () => {

  test("Each ordenation produces the correct result independently", () => {
    const onSort = vi.fn();
    render(<ListSortBar data={dataset} onSort={onSort} />);

    fireEvent.change(screen.getByRole("combobox"), { target: { value: "property1" } });
    fireEvent.click(screen.getByText("↑"));

    fireEvent.change(screen.getByRole("combobox"), { target: { value: "property2" } });
    fireEvent.click(screen.getByText("↓"));

    expect(onSort).toHaveBeenCalledTimes(2);
    expect(onSort.mock.calls[1][0]).toEqual(
      [...dataset].sort((a, b) => b.property2 - a.property2)
    );
  });

  test("sort → reset → sort produces consistent results", () => {
    const onSort = vi.fn();
    render(<ListSortBar data={dataset} onSort={onSort} />);

    fireEvent.change(screen.getByRole("combobox"), { target: { value: "property1" } });
    fireEvent.click(screen.getByText("↑"));
    fireEvent.click(screen.getByText("✖"));
    fireEvent.change(screen.getByRole("combobox"), { target: { value: "property2" } });
    fireEvent.click(screen.getByText("↑"));

    expect(onSort.mock.calls[onSort.mock.calls.length - 1][0]).toEqual(
      [...dataset].sort((a, b) => a.property2 - b.property2)
    );
  });

});


describe("ListSortBar — Inmutability of original dataset", () => {

  test("Does NOT mutate the original array when sorting", () => {
    const original = [
      { id: 1, property1: "Z", property2: 9 },
      { id: 2, property1: "A", property2: 1 },
    ];
    const snapshot = [...original];
    render(<ListSortBar data={original} onSort={vi.fn()} />);
    fireEvent.change(screen.getByRole("combobox"), { target: { value: "property1" } });
    fireEvent.click(screen.getByText("↑"));
    expect(original).toEqual(snapshot);
  });

});