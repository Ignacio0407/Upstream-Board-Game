import { render, screen, fireEvent } from "../test-utils";
import { describe, it, expect, vi } from "vitest";
import ColorPickerModal from "./ColorPickerModal";

describe("ColorPickerModal Component", () => {
    it("renders available colors and handles selection", () => {
        const onColorSelect = vi.fn();
        render(<ColorPickerModal onColorSelect={onColorSelect} takenColors={[]} />);
        
        expect(screen.getByText(/Pick a color:/i)).toBeInTheDocument();
        
        // Find the yellow button (first in the list)
        const buttons = screen.getAllByRole("button");
        const yellowButton = buttons.find(b => b.style.backgroundColor === 'rgb(255, 255, 0)');
        
        if (yellowButton) {
            fireEvent.click(yellowButton);
        }
        
        fireEvent.click(screen.getByText("Accept"));
        
        expect(onColorSelect).toHaveBeenCalledWith("YELLOW");
        expect(screen.queryByText(/Pick a color:/i)).not.toBeInTheDocument();
    });

    it("disables taken colors and prevents selection", () => {
        const onColorSelect = vi.fn();
        // Take yellow
        render(<ColorPickerModal onColorSelect={onColorSelect} takenColors={['#FFFF00']} />);
        
        const buttons = screen.getAllByRole("button");
        const yellowButton = buttons.find(b => b.style.backgroundColor === 'rgb(255, 255, 0)');
        
        expect(yellowButton).toBeDisabled();
        expect(screen.getByText("✖")).toBeInTheDocument();
    });

    it("does not call onColorSelect if no color is selected", () => {
        const onColorSelect = vi.fn();
        render(<ColorPickerModal onColorSelect={onColorSelect} takenColors={[]} />);
        
        fireEvent.click(screen.getByText("Accept"));
        
        expect(onColorSelect).not.toHaveBeenCalled();
        expect(screen.getByText(/Pick a color:/i)).toBeInTheDocument();
    });
});
