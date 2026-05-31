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
