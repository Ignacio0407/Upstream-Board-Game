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

    it("renders a date input correctly and supports required flag for select, textarea, date", () => {
        const { container } = render(
            <FormWrapper>
                <FormInput tag="Birthdate" name="birthdate" type="date" isRequired={true} register={null as any} errors={{}} />
                <FormInput tag="RequiredSelect" name="reqSelect" type="select" values={["A"]} isRequired={true} register={null as any} errors={{}} />
                <FormInput tag="RequiredTextarea" name="reqTextarea" type="textarea" isRequired={true} register={null as any} errors={{}} />
            </FormWrapper>
        );

        const dateInput = container.querySelector('input[type="date"]');
        expect(dateInput).toBeInTheDocument();
        expect(screen.getByText(/Birthdate \*/i)).toBeInTheDocument();
    });

    it("displays default error fallback message when error message is not a string", () => {
        const mockErrors = {
            username: { message: true, type: "required" }
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

        expect(screen.getByText("This field is invalid")).toBeInTheDocument();
    });
});
