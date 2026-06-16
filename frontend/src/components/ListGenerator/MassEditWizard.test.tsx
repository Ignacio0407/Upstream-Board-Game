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
import MassEditWizard from "./MassEditWizard";

// Mocking external dependencies
vi.mock("../Form/EditFormGenerator.tsx", () => ({
  EditFormGenerator: ({ entityToEdit, onSubmit, extraBeforeSubmit }: any) => (
    <div data-testid="form-gen">
      <span data-testid="current-name">{entityToEdit.name}</span>
      {extraBeforeSubmit}
      <button onClick={() => onSubmit({ ...entityToEdit, name: entityToEdit.name + " edited" })}>
        Submit
      </button>
    </div>
  )
}));

vi.mock("./ReviewModal.tsx", () => ({
  ReviewModal: ({ onConfirm }: any) => (
    <div role="dialog" aria-label="Review Modal">
      <button onClick={onConfirm}>Confirm all</button>
    </div>
  )
}));

vi.mock("../../util/fetchers.ts", () => ({
  put: vi.fn(() => Promise.resolve({ json: () => Promise.resolve([{ id: 1, name: "Charlie edited" }]) }))
}));

vi.mock("../../services/token.service.ts", () => ({
  default: { getLocalAccessToken: () => "fake-jwt" }
}));

const items = [
  { id: 1, name: "Charlie" },
  { id: 2, name: "Alpha" }
];

describe("MassEditWizard — History and Navigation", () => {

  test("Initializes with first item", () => {
    render(
      <MassEditWizard 
        items={items} 
        massEditUri="/uri" 
        renderItem={(i) => <span>{i.name}</span>} 
        actualUser={{}} 
        massRequiredAuth="auth" 
        onClose={vi.fn()} 
      />
    );
    expect(screen.getByText("Editing 1 / 2")).toBeInTheDocument();
    expect(screen.getByTestId("current-name")).toHaveTextContent("Charlie");
  });

  test("Navigates between items using arrows", () => {
    render(
      <MassEditWizard items={items} massEditUri="/uri" renderItem={vi.fn()} actualUser={{}} massRequiredAuth="auth" onClose={vi.fn()} />
    );
    
    fireEvent.click(screen.getByText("→"));
    expect(screen.getByText("Editing 2 / 2")).toBeInTheDocument();
    expect(screen.getByTestId("current-name")).toHaveTextContent("Alpha");

    fireEvent.click(screen.getByText("←"));
    expect(screen.getByText("Editing 1 / 2")).toBeInTheDocument();
    expect(screen.getByTestId("current-name")).toHaveTextContent("Charlie");
  });

  test("Undo and Redo logic", () => {
    render(
      <MassEditWizard items={items} massEditUri="/uri" renderItem={vi.fn()} actualUser={{}} massRequiredAuth="auth" onClose={vi.fn()} />
    );

    // Initial state
    let undoBtn; let redoBtn;
    undoBtn = screen.getByText("Undo");
    redoBtn = screen.getByText("Redo");
    expect(undoBtn).toBeDisabled();
    expect(redoBtn).toBeDisabled();

    // Perform edit
    fireEvent.click(screen.getByText("Submit")); // Charlie -> Charlie edited
    // It auto-advances to index 1 (Alpha), let's go back to Charlie (index 0)
    fireEvent.click(screen.getByText("←"));

    // Verify it's back on Charlie and edited
    expect(screen.getByTestId("current-name")).toHaveTextContent("Charlie edited");
    
    // Undo
    undoBtn = screen.getByText("Undo");
    expect(undoBtn).not.toBeDisabled();
    fireEvent.click(undoBtn);
    expect(screen.getByTestId("current-name")).toHaveTextContent("Charlie");
    
    // Redo
    redoBtn = screen.getByText("Redo");
    expect(redoBtn).not.toBeDisabled();
    fireEvent.click(redoBtn);
    expect(screen.getByTestId("current-name")).toHaveTextContent("Charlie edited");
  });

  test("Shows ReviewModal on last item submit", () => {
    render(
      <MassEditWizard items={items} massEditUri="/uri" renderItem={vi.fn()} actualUser={{}} massRequiredAuth="auth" onClose={vi.fn()} />
    );

    // Edit first item (Charlie) -> advances to Alpha
    fireEvent.click(screen.getByText("Submit"));
    
    // Edit second item (Alpha) -> triggers review
    fireEvent.click(screen.getByText("Submit"));

    expect(screen.getByLabelText("Review Modal")).toBeInTheDocument();
  });

});
