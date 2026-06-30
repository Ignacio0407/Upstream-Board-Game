import { render, screen } from "../test-utils";
import { describe, it, expect } from "vitest";
import PlayerCard from "./PlayerCard";

describe("PlayerCard Component", () => {
    it("renders player name and color correctly", () => {
        render(<PlayerCard name="Test Player" color="RED" />);
        
        expect(screen.getByText("Test Player")).toBeInTheDocument();
        
        const colorTd = screen.getByText("Test Player").nextElementSibling;
        expect(colorTd).toHaveStyle("background: #FF0000");
    });

    it("renders with undefined name correctly", () => {
        const { container } = render(<PlayerCard name={undefined} color="GREEN" />);
        
        const nameTd = container.querySelector(".column-card-name");
        expect(nameTd).toBeInTheDocument();
        expect(nameTd?.textContent).toBe("");
        
        const colorTd = container.querySelector(".column-card-color");
        expect(colorTd).toHaveStyle("background: #008F39");
    });
});
