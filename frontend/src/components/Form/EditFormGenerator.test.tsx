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

    it("fetches entity from API when entityToEdit is null", async () => {
        const fetchers = await import("../../util/fetchers");
        vi.spyOn(fetchers, "get").mockResolvedValue({
            json: async () => mockEntity
        } as any);

        const { container } = render(
            <EditFormGenerator<TestEntity> 
                entityToEdit={null as any} 
                onSubmit={vi.fn()} 
            />
        );

        await waitFor(() => {
            const nameInput = container.querySelector('input[name="name"]') as HTMLInputElement;
            expect(nameInput?.value).toBe("Test Name");
        });
    });
});
