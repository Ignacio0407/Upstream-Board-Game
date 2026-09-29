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

  test("Shows ReviewModal when clicking Review button on last item and confirms changes", async () => {
    const mockOnClose = vi.fn();
    render(
      <MassEditWizard items={items} massEditUri="/uri" renderItem={vi.fn()} actualUser={{}} massRequiredAuth="auth" onClose={mockOnClose} />
    );

    // Navigate to last item
    fireEvent.click(screen.getByText("→"));
    expect(screen.getByText("Editing 2 / 2")).toBeInTheDocument();

    // Click Review button
    fireEvent.click(screen.getByText("Review"));
    expect(screen.getByLabelText("Review Modal")).toBeInTheDocument();

    // Click Confirm all without edits -> onClose(null)
    fireEvent.click(screen.getByText("Confirm all"));
    expect(mockOnClose).toHaveBeenCalledWith(null);
  });

  test("Executes put API call and onClose when confirmAll is called with edited items", async () => {
    const mockOnClose = vi.fn();
    render(
      <MassEditWizard items={items} massEditUri="/uri" renderItem={vi.fn()} actualUser={{}} massRequiredAuth="auth" onClose={mockOnClose} />
    );

    // Submit edit on first item
    fireEvent.click(screen.getByText("Submit"));
    // On second item, click Review
    fireEvent.click(screen.getByText("Review"));

    // Click Confirm all
    fireEvent.click(screen.getByText("Confirm all"));

    await vi.waitFor(() => {
      expect(mockOnClose).toHaveBeenCalledWith([{ id: 1, name: "Charlie edited" }]);
    });
  });

});
