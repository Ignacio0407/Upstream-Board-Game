import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import ErrorPage from "./error-page";
import { useRouteError } from "react-router-dom";

vi.mock("react-router-dom", () => ({
    useRouteError: vi.fn(),
}));

describe("ErrorPage Component", () => {
    it("renders the error message correctly", () => {
        vi.mocked(useRouteError).mockReturnValue({ statusText: "Not Found" });
        
        // Suppress console.error for this test
        const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});
        
        render(<ErrorPage />);
        
        expect(screen.getByText("Oops!")).toBeInTheDocument();
        expect(screen.getByText("Not Found")).toBeInTheDocument();
        
        consoleSpy.mockRestore();
    });

    it("renders alternative error message if statusText is missing", () => {
        vi.mocked(useRouteError).mockReturnValue({ message: "Unexpected Error" });
        
        const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});
        
        render(<ErrorPage />);
        
        expect(screen.getByText("Unexpected Error")).toBeInTheDocument();
        
        consoleSpy.mockRestore();
    });
});
