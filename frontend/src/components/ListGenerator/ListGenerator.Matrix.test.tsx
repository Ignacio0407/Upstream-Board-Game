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
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import ListGenerator from "./ListGenerator";
import { BaseUser } from "../../interfaces/User";
import { navigateToUnauthorized } from "../DoNotHackMyApp/DoNotHackMyAppHelper";

vi.mock("../DoNotHackMyApp/DoNotHackMyAppHelper.ts", () => ({
  navigateToUnauthorized: vi.fn()
}));

const dataset = [
  { id: 1, name: "Charlie", value: 3 },
  { id: 2, name: "Alpha", value: 1 },
  { id: 3, name: "Bravo", value: 2 },
  { id: 4, name: "Delta", value: 4 },
  { id: 5, name: "Echo", value: 5 },
  { id: 6, name: "Foxtrot", value: 6 },
  { id: 7, name: "Golf", value: 7 }
];

const user: BaseUser = {
  id: 1,
  username: "admin",
  roles: ["ADMIN"]
};

function renderList(props = {}) {
  return render(
    <MemoryRouter>
      <ListGenerator
        data={dataset}
        actualUser={user}
        columns={["name","value"]}
        pageSize={3}
        editCondition
        deleteCondition
        massDeleteButton={vi.fn()}
        searchBarPlaceholder="Search..."
        {...props}
      />
    </MemoryRouter>
  );
}

describe("ListGenerator — Interaction Matrix", () => {

  test("Sort + Pagination interaction", async () => {
    const userEventInstance = userEvent.setup();
    renderList();

    await userEventInstance.selectOptions(
      screen.getByRole("combobox"),
      "name"
    );

    await userEventInstance.click(
      screen.getAllByRole("button", { name: "↑" })[0]
    );

    await userEventInstance.click(
      screen.getAllByRole("button", { name: "2" })[0]
    );

    expect(screen.getByText("Delta")).toBeInTheDocument();
  });

  test("Search + Sort interaction", async () => {
    const userEventInstance = userEvent.setup();
    renderList();

    await userEventInstance.type(
      screen.getByPlaceholderText(/search/i),
      "a"
    );

    await userEventInstance.selectOptions(
      screen.getByRole("combobox"),
      "name"
    );

    await userEventInstance.click(
      screen.getAllByRole("button", { name: "↑" })[0]
    );

    expect(screen.getByText("Alpha")).toBeInTheDocument();
  });

  test("Selection + Pagination interaction", async () => {
    const userEventInstance = userEvent.setup();
    renderList();

    const checkboxes = screen.getAllByRole("checkbox");

    await userEventInstance.click(checkboxes[1]);

    expect(screen.getByText(/1 selected/i)).toBeInTheDocument();

    await userEventInstance.click(
      screen.getAllByRole("button", { name: "2" })[0]
    );

    expect(screen.queryByText(/selected/i)).not.toBeInTheDocument();
  });

  test("Selection + MassDelete interaction", async () => {
    const userEventInstance = userEvent.setup();
    const massDelete = vi.fn();

    renderList({ massDeleteButton: massDelete });

    await userEventInstance.click(
      screen.getAllByRole("checkbox")[1]
    );

    await userEventInstance.click(
      screen.getAllByRole("button", { name: /delete/i })[0]
    );

    expect(massDelete).toHaveBeenCalled();
  });

  test("Selection + MassEdit modal interaction", async () => {
    const userEventInstance = userEvent.setup();

    renderList({
      massEditButton: <button>Edit</button>,
      massEditUri: "/edit"
    });

    await userEventInstance.click(
      screen.getAllByRole("checkbox")[1]
    );

    await userEventInstance.click(
      screen.getAllByRole("button", { name: /edit/i })[0]
    );

    expect(
      screen.getByText(/edit 1 elements/i)
    ).toBeInTheDocument();
  });

  test("Security check executed", () => {
    renderList();
    expect(navigateToUnauthorized).toHaveBeenCalled();
  });

  test("Edge case: empty dataset", () => {
    renderList({ data: [] });
    expect(screen.getByRole("table")).toBeInTheDocument();
  });

});