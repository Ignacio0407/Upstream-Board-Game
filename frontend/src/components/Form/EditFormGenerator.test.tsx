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

import { render, screen, fireEvent, waitFor } from "../../test-utils";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { EditFormGenerator } from "./EditFormGenerator";
import * as reactRouterDom from "react-router-dom";

// --- Mocks ---

vi.mock("react-router-dom", async () => {
    const actual = await vi.importActual("react-router-dom");
    return {
        ...actual,
        useLocation: vi.fn(),
        useNavigate: vi.fn(),
    };
});

const mockNavigate = vi.fn();
const mockLocation = { pathname: "/test-path" };

interface TestEntity {
    id: number;
    name: string;
    description: string;
    active: boolean;
}

const mockEntity: TestEntity = {
    id: 1,
    name: "Test Name",
    description: "Test Description",
    active: true
};

describe("EditFormGenerator Component", () => {

    beforeEach(() => {
        vi.clearAllMocks();
        vi.mocked(reactRouterDom.useNavigate).mockReturnValue(mockNavigate);
        vi.mocked(reactRouterDom.useLocation).mockReturnValue(mockLocation as any);
    });

    it("renders fields based on entity properties automatically", () => {
        const { container } = render(
            <EditFormGenerator<TestEntity> 
                entityToEdit={mockEntity} 
                onSubmit={vi.fn()} 
            />
        );

        expect(container.querySelector('input[name="name"]')).toBeInTheDocument();
        expect(container.querySelector('input[name="description"]')).toBeInTheDocument();
        expect(container.querySelector('input[name="active"]')).toBeInTheDocument();
        
        expect(screen.getByText(/Name/i)).toBeInTheDocument();
        expect(screen.getByText(/Description/i)).toBeInTheDocument();
        expect(screen.getByText(/Active/i)).toBeInTheDocument();
    });

    it("pre-fills form with existing entity data", () => {
        const { container } = render(
            <EditFormGenerator<TestEntity> 
                entityToEdit={mockEntity} 
                onSubmit={vi.fn()} 
            />
        );

        const nameInput = container.querySelector('input[name="name"]') as HTMLInputElement;
        const descInput = container.querySelector('input[name="description"]') as HTMLInputElement;
        const activeInput = container.querySelector('input[name="active"]') as HTMLInputElement;

        expect(nameInput.value).toBe("Test Name");
        expect(descInput.value).toBe("Test Description");
        expect(activeInput.checked).toBe(true);
    });

    it("calls onSubmit with modified data upon submission", async () => {
        const mockOnSubmit = vi.fn();
        const { container } = render(
            <EditFormGenerator<TestEntity> 
                entityToEdit={mockEntity} 
                onSubmit={mockOnSubmit} 
            />
        );

        const nameInput = container.querySelector('input[name="name"]') as HTMLInputElement;
        fireEvent.change(nameInput, { target: { value: "New Brilliance" } });
        
        fireEvent.click(screen.getByRole("button", { name: /Submit/i }));

        await waitFor(() => {
            expect(mockOnSubmit).toHaveBeenCalledWith(
                expect.objectContaining({ name: "New Brilliance", description: "Test Description", active: true }),
                expect.anything()
            );
        });
    });

    it("supports select dropdowns via selectOptions prop", () => {
        const selectOptions = { name: ["High", "Medium", "Low"] };
        const { container } = render(
            <EditFormGenerator<TestEntity> 
                entityToEdit={mockEntity} 
                onSubmit={vi.fn()} 
                selectOptions={selectOptions}
            />
        );

        const select = container.querySelector('select[name="name"]');
        expect(select).toBeInTheDocument();
        expect(screen.getByText("High")).toBeInTheDocument();
        expect(screen.getByText("Medium")).toBeInTheDocument();
        expect(screen.getByText("Low")).toBeInTheDocument();
    });
});
