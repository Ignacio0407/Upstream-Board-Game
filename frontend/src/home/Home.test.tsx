import { render, screen } from "../test-utils";
import { describe, it, expect, vi, beforeEach } from "vitest";
import Home from "./home";
import tokenService from "../services/token.service.ts";

// --- Mocks ---

vi.mock("../services/token.service.ts");

describe("Home Component", () => {

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("shows login links when user is not authenticated", () => {
        vi.mocked(tokenService.getLocalAccessToken).mockReturnValue("");
        
        render(<Home />);
        
        const links = screen.getAllByRole("link");
        expect(links[0]).toHaveAttribute("href", "/login");
        expect(links[1]).toHaveAttribute("href", "/login");
    });

    it("shows dashboard and create game links when user is authenticated", () => {
        vi.mocked(tokenService.getLocalAccessToken).mockReturnValue("fake-jwt");
        
        render(<Home />);
        
        const links = screen.getAllByRole("link");
        expect(links[0]).toHaveAttribute("href", "/dashboard");
        expect(links[1]).toHaveAttribute("href", "/creategame/");
    });

    it("renders the main logo", () => {
        render(<Home />);
        const logo = screen.getByRole("img");
        expect(logo).toHaveAttribute("src", "/images/logoCentralUPS.jpg");
    });
});
