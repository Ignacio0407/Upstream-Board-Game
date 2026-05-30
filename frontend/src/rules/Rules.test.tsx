import { render, screen } from "../test-utils";
import { describe, it, expect } from "vitest";
import Rules from "./rules";

describe("Rules Component", () => {
    it("renders the rules PDF in an iframe", () => {
        render(<Rules />);
        const iframe = screen.getByTitle("Rules");
        expect(iframe).toBeInTheDocument();
        expect(iframe).toHaveAttribute("src", "/rules_ES.pdf");
    });
});
