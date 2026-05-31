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

import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import SearchBar from "./SearchBar";

const mockData = [
    { name: "Apple", type: "Fruit" },
    { name: "Banana", type: "Fruit" },
    { name: "Carrot", type: "Vegetable" }
];

describe("SearchBar Component", () => {
    it("renders with placeholder", () => {
        render(<SearchBar data={mockData} setFiltered={vi.fn()} placeholder="Search items..." />);
        expect(screen.getByPlaceholderText("Search items...")).toBeInTheDocument();
    });

    it("filters data based on input correctly", async () => {
        const setFiltered = vi.fn();
        render(<SearchBar data={mockData} setFiltered={setFiltered} placeholder="Search" defaultMatchMode="contains" debounceMs={0} defaultCaseSensitive={false} />);

        const input = screen.getByPlaceholderText("Search");
        
        fireEvent.change(input, { target: { value: "name:app" } });
        await waitFor(() => {
            expect(setFiltered).toHaveBeenLastCalledWith([{ name: "Apple", type: "Fruit" }]);
        }, { timeout: 1000 });

        fireEvent.change(input, { target: { value: "type:fruit" } });
        await waitFor(() => {
            expect(setFiltered).toHaveBeenLastCalledWith([
                { name: "Apple", type: "Fruit" },
                { name: "Banana", type: "Fruit" }
            ]);
        }, { timeout: 1000 });
    });

    it("handles case sensitivity correctly when enabled", async () => {
        const setFiltered = vi.fn();
        render(<SearchBar data={mockData} setFiltered={setFiltered} defaultCaseSensitive={true} defaultMatchMode="contains" debounceMs={0} />);

        const input = screen.getByRole("textbox");
        
        fireEvent.change(input, { target: { value: "name:apple" } });
        await waitFor(() => {
            expect(setFiltered).toHaveBeenLastCalledWith([]); // No match for "apple" (case sensitive)
        }, { timeout: 1000 });

        fireEvent.change(input, { target: { value: "name:Apple" } });
        await waitFor(() => {
            expect(setFiltered).toHaveBeenLastCalledWith([{ name: "Apple", type: "Fruit" }]);
        }, { timeout: 1000 });
    });

    it("returns all data when input is cleared", async () => {
        const setFiltered = vi.fn();
        render(<SearchBar data={mockData} setFiltered={setFiltered} defaultMatchMode="contains" debounceMs={0} />);

        const input = screen.getByRole("textbox");
        
        fireEvent.change(input, { target: { value: "something" } });
        fireEvent.change(input, { target: { value: "" } });
        
        await waitFor(() => {
            expect(setFiltered).toHaveBeenLastCalledWith(mockData);
        }, { timeout: 1000 });
    });

    it("toggles logical operator AND/OR", () => {
        const setFiltered = vi.fn();
        render(<SearchBar data={mockData} setFiltered={setFiltered} defaultOperator="AND" />);

        const opButton = screen.getByLabelText(/Select logical operator AND\/OR/i);
        expect(opButton).toHaveTextContent("AND");

        fireEvent.click(opButton);
        const orOption = screen.getByRole("option", { name: "OR" });
        fireEvent.click(orOption);

        expect(opButton).toHaveTextContent("OR");
    });

    it("toggles case sensitivity", () => {
        const setFiltered = vi.fn();
        render(<SearchBar data={mockData} setFiltered={setFiltered} defaultCaseSensitive={true} />);

        const caseButton = screen.getByLabelText(/Toggle case sensitivity/i);
        expect(caseButton).toHaveTextContent("Case sensitive: ON");

        fireEvent.click(caseButton);
        expect(caseButton).toHaveTextContent("Case sensitive: OFF");
    });

    it("toggles match mode", () => {
        const setFiltered = vi.fn();
        render(<SearchBar data={mockData} setFiltered={setFiltered} defaultMatchMode="exact" />);

        const modeButton = screen.getByLabelText(/Select match mode/i);
        expect(modeButton).toHaveTextContent("Exact");

        fireEvent.click(modeButton);
        const containsOption = screen.getByRole("option", { name: "Contains" });
        fireEvent.click(containsOption);

        expect(modeButton).toHaveTextContent("Contains");
    });

    it("inserts attribute prefix when attribute is selected from dropdown", () => {
        render(<SearchBar data={mockData} setFiltered={vi.fn()} />);

        const dropdownButton = screen.getByLabelText(/Show searchable attributes/i);
        fireEvent.click(dropdownButton);

        const nameOption = screen.getByRole("option", { name: "name" });
        fireEvent.click(nameOption);

        const input = screen.getByRole("textbox") as HTMLInputElement;
        expect(input.value).toBe("name:");
    });

    it("closes dropdowns when clicking outside", () => {
        render(<SearchBar data={mockData} setFiltered={vi.fn()} />);

        const opButton = screen.getByLabelText(/Select logical operator AND\/OR/i);
        fireEvent.click(opButton);
        expect(screen.getByRole("option", { name: "OR" })).toBeInTheDocument();

        fireEvent.mouseDown(document.body);
        expect(screen.queryByRole("option", { name: "OR" })).not.toBeInTheDocument();
    });

    it("executes search immediately when Enter is pressed", () => {
        const setFiltered = vi.fn();
        render(<SearchBar data={mockData} setFiltered={setFiltered} debounceMs={1000} />); // Large debounce

        const input = screen.getByRole("textbox");
        fireEvent.change(input, { target: { value: "name:Apple" } });
        fireEvent.keyDown(input, { key: "Enter", code: "Enter" });

        expect(setFiltered).toHaveBeenCalledWith([{ name: "Apple", type: "Fruit" }]);
    });

    it("executes search immediately when search button is clicked", () => {
        const setFiltered = vi.fn();
        render(<SearchBar data={mockData} setFiltered={setFiltered} debounceMs={1000} />);

        const input = screen.getByRole("textbox");
        fireEvent.change(input, { target: { value: "name:Apple" } });
        
        const searchButton = screen.getByLabelText(/Run search/i);
        fireEvent.click(searchButton);

        expect(setFiltered).toHaveBeenCalledWith([{ name: "Apple", type: "Fruit" }]);
    });
});
