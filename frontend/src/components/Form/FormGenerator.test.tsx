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
import { FormGenerator } from "./FormGenerator";
import * as reactRouterDom from "react-router-dom";
import { navigateToUnauthorized } from "../DoNotHackMyApp/DoNotHackMyAppHelper.ts";
import { Authority } from "../../interfaces/User.ts";

// --- Mocks ---

vi.mock("react-router-dom", async () => {
    const actual = await vi.importActual("react-router-dom");
    return {
        ...actual,
        useLocation: vi.fn(),
        useNavigate: vi.fn(),
    };
});

vi.mock("../DoNotHackMyApp/DoNotHackMyAppHelper.ts", () => ({
    navigateToUnauthorized: vi.fn()
}));

const mockNavigate = vi.fn();
const mockLocation = { pathname: "/test-path" };

const mockFields = [
    { tag: "Username", name: "username", type: "text", isRequired: true },
    { tag: "Age", name: "age", type: "number", isRequired: false },
    { tag: "Role", name: "role", type: "select", values: ["Admin", "Player"] }
];
const mockDefaultValues = { tag: "", name: "", type: 0, isRequired: false, role: "Player" };

describe("FormGenerator Component", () => {

    beforeEach(() => {
        vi.clearAllMocks();
        vi.mocked(reactRouterDom.useNavigate).mockReturnValue(mockNavigate);
        vi.mocked(reactRouterDom.useLocation).mockReturnValue(mockLocation as any);
    });

    it("renders all defined fields and the submit button", () => {
        const { container } = render(
            <FormGenerator 
                fields={mockFields} 
                defaultValues={mockDefaultValues} 
                onSubmit={vi.fn()} 
            />
        );

        expect(container.querySelector('input[name="username"]')).toBeInTheDocument();
        expect(container.querySelector('input[name="age"]')).toBeInTheDocument();
        expect(container.querySelector('select[name="role"]')).toBeInTheDocument();
        
        expect(screen.getByText(/Username \*/i)).toBeInTheDocument();
        expect(screen.getByText(/Age/i)).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /Submit/i })).toBeInTheDocument();
    });

    it("processes form data and calls onSubmit correctly", async () => {
        const mockOnSubmit = vi.fn();
        const { container } = render(
            <FormGenerator 
                fields={mockFields} 
                defaultValues={mockDefaultValues} 
                onSubmit={mockOnSubmit} 
            />
        );

        const usernameInput = container.querySelector('input[name="username"]') as HTMLInputElement;
        fireEvent.change(usernameInput, { target: { value: "brilliant_user" } });
        
        const ageInput = container.querySelector('input[name="age"]') as HTMLInputElement;
        fireEvent.change(ageInput, { target: { value: "25" } });

        fireEvent.click(screen.getByRole("button", { name: /Submit/i }));

        await waitFor(() => {
            expect(mockOnSubmit).toHaveBeenCalledWith(
                expect.objectContaining({ username: "brilliant_user", age: "25", role: "Admin" }),
                expect.anything()
            );
        });
    });

    it("manages security policies and redirects unauthorized users", () => {
        render(
            <FormGenerator 
                fields={mockFields}
                onSubmit={vi.fn()}
                deactivateSecurity={false}
                requiredAuth="ADMIN"
                actualUser={{ id: 1, authority: Authority.PLAYER } as any} defaultValues={mockDefaultValues}            />
        );

        expect(navigateToUnauthorized).toHaveBeenCalled();
    });

    it("allows access when security is deactivated or user has correct role", () => {
        render(
            <FormGenerator 
                fields={mockFields} 
                onSubmit={vi.fn()} 
                deactivateSecurity={true}
                requiredAuth="ADMIN"
                defaultValues={mockDefaultValues}
            />
        );

        expect(navigateToUnauthorized).not.toHaveBeenCalled();
    });

    it("handles the cancel button and navigates to the specified path", () => {
        render(
            <FormGenerator 
                fields={mockFields}
                onSubmit={vi.fn()}
                cancelButtonActivated={true}
                navigateOnCancel="/home" defaultValues={mockDefaultValues}            />
        );

        const cancelButton = screen.getByRole("button", { name: /Cancel/i });
        fireEvent.click(cancelButton);

        expect(mockNavigate).toHaveBeenCalledWith("/home");
    });

    it("handles the cancel button with empty navigateOnCancel slicing current pathname", () => {
        vi.mocked(reactRouterDom.useLocation).mockReturnValue({ pathname: "/parent/child" } as any);
        render(
            <FormGenerator 
                fields={mockFields}
                onSubmit={vi.fn()}
                cancelButtonActivated={true}
                defaultValues={mockDefaultValues}
            />
        );

        const cancelButton = screen.getByRole("button", { name: /Cancel/i });
        fireEvent.click(cancelButton);

        expect(mockNavigate).toHaveBeenCalledWith("/parent");
    });

    it("displays the loading text when form is submitting", async () => {
        // Mock onSubmit to be a long-running task
        const mockOnSubmit = vi.fn(() => new Promise(resolve => setTimeout(resolve, 50)));
        render(
            <FormGenerator 
                fields={mockFields}
                onSubmit={mockOnSubmit}
                loadingText="Creating..." defaultValues={mockDefaultValues}            />
        );

        fireEvent.click(screen.getByRole("button", { name: /Submit/i }));

        expect(screen.getByText(/Creating.../i)).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /Creating.../i })).toBeDisabled();
    });

    it("applies form styling based on className prop", () => {
        const { container } = render(
            <FormGenerator 
                fields={mockFields}
                onSubmit={vi.fn()}
                className="super-form" defaultValues={mockDefaultValues}            />
        );

        expect(container.querySelector("form")).toHaveClass("super-form");
    });
});
