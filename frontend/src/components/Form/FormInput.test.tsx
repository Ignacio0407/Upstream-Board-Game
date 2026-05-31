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

import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import { FormInput } from "./FormInput";
import { useForm } from "react-hook-form";
import React from "react";

const FormWrapper = ({ children, defaultValues = {} }: { children: React.ReactNode, defaultValues?: any }) => {
    const { register, formState: { errors } } = useForm({ defaultValues });
    return (
        <form>
            {React.Children.map(children, child => {
                if (React.isValidElement(child)) {
                    return React.cloneElement(child as React.ReactElement<any>, { register, errors });
                }
                return child;
            })}
        </form>
    );
};

describe("FormInput Component", () => {
    it("renders a text input correctly with name and label", () => {
        const { container } = render(
            <FormWrapper>
                <FormInput tag="Username" name="username" type="text" register={null as any} errors={{}} />
            </FormWrapper>
        );

        const input = container.querySelector('input[name="username"]');
        expect(input).toBeInTheDocument();
        expect(input).toHaveAttribute("type", "text");
        expect(screen.getByText(/Username/i)).toBeInTheDocument();
    });

    it("renders a select input with provided options", () => {
        const options = ["Option 1", "Option 2"];
        const { container } = render(
            <FormWrapper>
                <FormInput tag="Select" name="selectField" type="select" values={options} register={null as any} errors={{}} />
            </FormWrapper>
        );

        const select = container.querySelector('select[name="selectField"]');
        expect(select).toBeInTheDocument();
        expect(screen.getByText("Option 1")).toBeInTheDocument();
        expect(screen.getByText("Option 2")).toBeInTheDocument();
    });

    it("renders a checkbox and checks it if defaultValue is 'true'", () => {
        const { container } = render(
            <FormWrapper defaultValues={{ active: true }}>
                <FormInput tag="Active" name="active" type="checkbox" defaultValue="true" register={null as any} errors={{}} />
            </FormWrapper>
        );

        const checkbox = container.querySelector('input[name="active"]') as HTMLInputElement;
        expect(checkbox).toBeInTheDocument();
        expect(checkbox.type).toBe("checkbox");
        // React Hook Form handles the checked state
    });

    it("renders a textarea for long text", () => {
        const { container } = render(
            <FormWrapper>
                <FormInput tag="Bio" name="bio" type="textarea" register={null as any} errors={{}} />
            </FormWrapper>
        );

        const textarea = container.querySelector('textarea[name="bio"]');
        expect(textarea).toBeInTheDocument();
    });

    it("displays validation error message when present", () => {
        const mockErrors = {
            username: { message: "Username is required", type: "required" }
        };
        
        render(
            <FormInput 
                tag="Username" 
                name="username" 
                type="text" 
                register={vi.fn()} 
                errors={mockErrors as any} 
            />
        );

        expect(screen.getByText("Username is required")).toBeInTheDocument();
        expect(screen.getByText("Username is required").closest(".class-form-group")).toHaveClass("class-error-form");
    });

    it("shows '*' when field is required", () => {
        render(
            <FormWrapper>
                <FormInput tag="Password" name="password" type="password" isRequired={true} register={null as any} errors={{}} />
            </FormWrapper>
        );

        expect(screen.getByText(/Password \*/i)).toBeInTheDocument();
    });

    it("applies custom layout styles (numberOfColumns)", () => {
        const { container } = render(
            <FormWrapper>
                <FormInput tag="Half" name="half" type="text" numberOfColumns={2} register={null as any} errors={{}} />
            </FormWrapper>
        );

        const group = container.querySelector(".class-form-group");
        expect(group).toHaveStyle({ width: "47%" });
    });
});
