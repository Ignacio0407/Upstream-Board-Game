import { render, screen, fireEvent } from "../../test-utils";
import { describe, it, expect, vi, beforeEach } from "vitest";
import Logout from "./Logout";
import tokenService from "../../services/token.service.ts";

vi.mock("../../services/token.service.ts");

// Mock window.location
const originalLocation = window.location;

describe("Logout Component", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        window.localStorage.clear();
        // Setup location mock
        delete (window as any).location;
        window.location = { ...originalLocation, href: "" } as any;
    });

    afterEach(() => {
        // Restore original location
        Object.defineProperty(window, 'location', {
            value: originalLocation,
            writable: true,
            configurable: true,
        });
    })

    it("logs out successfully when 'Yes' is clicked", () => {
        window.localStorage.setItem("jwt", "fake-jwt");
        render(<Logout />);
        
        fireEvent.click(screen.getByRole("button", { name: /Yes/i }));
        
        expect(tokenService.removeUser).toHaveBeenCalled();
        expect(window.location.href).toBe("/");
    });

    it("shows alert if no user is logged in", () => {
        vi.spyOn(window, "alert").mockImplementation(() => {});
        render(<Logout />);
        
        fireEvent.click(screen.getByRole("button", { name: /Yes/i }));
        
        expect(window.alert).toHaveBeenCalledWith("There is no user logged in");
        expect(tokenService.removeUser).not.toHaveBeenCalled();
    });

    it("navigates home when 'No' is clicked", () => {
        render(<Logout />);
        const noLink = screen.getByRole("link", { name: /No/i });
        expect(noLink).toHaveAttribute("href", "/");
    });
});
