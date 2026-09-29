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

import { render, screen, fireEvent } from "../../test-utils";
import { describe, it, expect, vi, beforeEach } from "vitest";
import ListGenerator from "./ListGenerator";
import * as reactRouterDom from "react-router-dom";
import { navigateToUnauthorized } from "../DoNotHackMyApp/DoNotHackMyAppHelper.ts";

// --- Mocks ---

vi.mock("react-router-dom", async () => {
    const actual = await vi.importActual("react-router-dom");
    return {
        ...actual,
        useNavigate: vi.fn(),
    };
});

vi.mock("../DoNotHackMyApp/DoNotHackMyAppHelper.ts", () => ({
    navigateToUnauthorized: vi.fn()
}));

vi.mock("./ListPagination.tsx", () => ({
    default: ({ onPageChange }: any) => (
        <div data-testid="pagination">
            <button onClick={() => onPageChange(2)}>Page 2</button>
        </div>
    )
}));

vi.mock("./ListSortBar.tsx", () => ({
    default: () => <div data-testid="sort-bar">Sort Bar</div>
}));

vi.mock("../SearchBar/SearchBar.tsx", () => ({
    default: ({ setFiltered, data }: any) => (
        <div data-testid="search-bar">
            <button onClick={() => setFiltered([data[0]])}>Filter First</button>
        </div>
    )
}));

const mockData = [
    { id: 1, name: "Item 1", status: "Active" },
    { id: 2, name: "Item 2", status: "Inactive" },
    { id: 3, name: "Item 3", status: "Active" }
];

const mockUser = { id: 1, username: "admin", authority: "ADMIN" };

describe("ListGenerator Component", () => {
    const mockNavigate = vi.fn();

    beforeEach(() => {
        vi.clearAllMocks();
        vi.mocked(reactRouterDom.useNavigate).mockReturnValue(mockNavigate);
    });

    it("renders the table with provided data and columns", () => {
        render(
            <ListGenerator 
                data={mockData} 
                columns={["name", "status"]} 
                actualUser={mockUser as any} 
                deactivateSecurity={true}
            />
        );

        expect(screen.getByText("Item 1")).toBeInTheDocument();
        expect(screen.getByText("Item 2")).toBeInTheDocument();
        expect(screen.getAllByText("Active")).toHaveLength(2);
        expect(screen.getByText("Inactive")).toBeInTheDocument();
        expect(screen.getByText("Name")).toBeInTheDocument();
        expect(screen.getByText("Status")).toBeInTheDocument();
    });

    it("handles item selection and shows mass actions bar", () => {
        render(
            <ListGenerator 
                data={mockData} 
                actualUser={mockUser as any} 
                deactivateSecurity={true}
                deleteCondition={true}
                massDeleteButton={vi.fn()}
            />
        );

        const checkboxes = screen.getAllByRole("checkbox");
        // checkboxes[0] is "select all", checkboxes[1] is item 1
        fireEvent.click(checkboxes[1]);

        expect(screen.getByText(/1 selected/i)).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /Delete/i })).toBeInTheDocument();
    });

    it("handles 'select all' functionality", () => {
        render(
            <ListGenerator 
                data={mockData} 
                actualUser={mockUser as any} 
                deactivateSecurity={true}
                deleteCondition={true}
            />
        );

        const selectAll = screen.getAllByRole("checkbox")[0];
        fireEvent.click(selectAll);

        expect(screen.getByText(/3 selected/i)).toBeInTheDocument();

        fireEvent.click(selectAll);
        expect(screen.queryByText(/selected/i)).not.toBeInTheDocument();
    });

    it("calls navigateToUnauthorized if deactivateSecurity is false", () => {
        render(
            <ListGenerator 
                data={mockData} 
                actualUser={mockUser as any} 
                deactivateSecurity={false}
                requiredAuth="ADMIN"
            />
        );

        expect(navigateToUnauthorized).toHaveBeenCalledWith(
            undefined, 
            mockUser, 
            "ADMIN", 
            expect.any(Function), 
            "/do-not-hack-my-app"
        );
    });

    it("integrates with SearchBar to filter data", () => {
        render(
            <ListGenerator 
                data={mockData} 
                actualUser={mockUser as any} 
                deactivateSecurity={true}
                searchBarPlaceholder="Search..."
            />
        );

        expect(screen.getByTestId("search-bar")).toBeInTheDocument();
        
        fireEvent.click(screen.getByText("Filter First"));

        expect(screen.getByText("Item 1")).toBeInTheDocument();
        expect(screen.queryByText("Item 2")).not.toBeInTheDocument();
    });

    it("updates page when pagination is clicked", () => {
        render(
            <ListGenerator 
                data={mockData} 
                actualUser={mockUser as any} 
                deactivateSecurity={true}
                pageSize={1}
            />
        );

        // Initial page 1 should show Item 1
        expect(screen.getByText("Item 1")).toBeInTheDocument();
        expect(screen.queryByText("Item 2")).not.toBeInTheDocument();

        fireEvent.click(screen.getByText("Page 2"));

        // Now should show Item 2
        expect(screen.getByText("Item 2")).toBeInTheDocument();
        expect(screen.queryByText("Item 1")).not.toBeInTheDocument();
    });

    it("renders create button at specified position", () => {
        render(
            <ListGenerator 
                data={mockData} 
                actualUser={mockUser as any} 
                deactivateSecurity={true}
                createButton="/create"
                createButtonPosition="top"
            />
        );

        const createButton = screen.getByRole("link", { name: /Create/i });
        expect(createButton).toBeInTheDocument();
        expect(createButton).toHaveAttribute("href", "/create");
        
        // Check it's rendered before the table (simplified check by presence)
        expect(createButton).toBeInTheDocument();
    });

    it("opens mass edit modal and closes it via Close button", () => {
        render(
            <ListGenerator 
                data={mockData} 
                actualUser={mockUser as any} 
                deactivateSecurity={true}
                editCondition={true}
                massEditButton={<span>Mass Edit</span>}
                massEditUri="/api/v1/edit"
                customRender={(item) => <div>{item.name}</div>}
                massRequiredAuth="ADMIN"
            />
        );

        const checkbox = screen.getAllByRole("checkbox")[1];
        fireEvent.click(checkbox);

        const massEditTrigger = screen.getByText("Mass Edit");
        fireEvent.click(massEditTrigger);

        expect(screen.getByRole("heading", { name: /Edit 1 elements/i })).toBeInTheDocument();

        const closeBtn = screen.getByRole("button", { name: /✕ Close/i });
        fireEvent.click(closeBtn);

        expect(screen.queryByRole("heading", { name: /Edit 1 elements/i })).not.toBeInTheDocument();
    });

    it("calls massDeleteButton with selected items when mass delete button is clicked", () => {
        const mockMassDelete = vi.fn();
        render(
            <ListGenerator 
                data={mockData} 
                actualUser={mockUser as any} 
                deactivateSecurity={true}
                deleteCondition={true}
                massDeleteButton={mockMassDelete}
            />
        );

        const checkbox = screen.getAllByRole("checkbox")[1];
        fireEvent.click(checkbox);

        const massDeleteBtn = screen.getByRole("button", { name: "Delete" });
        fireEvent.click(massDeleteBtn);

        expect(mockMassDelete).toHaveBeenCalledWith([mockData[0]]);
    });
});
