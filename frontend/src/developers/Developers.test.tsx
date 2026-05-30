import { render, screen, fireEvent, waitFor } from "../test-utils";
import { describe, it, expect, vi, beforeEach } from "vitest";
import DeveloperList from "./developers";
import tokenService from "../services/token.service.ts";
import useFetchState from "../util/useFetchState.ts";

// --- Mocks ---

vi.mock("../services/token.service.ts");
vi.mock("../util/useFetchState.ts");

const mockDevelopers = [
    { 
        id: 1, 
        name: "Developer 1", 
        email: "dev1@test.com", 
        url: "https://dev1.com", 
        properties: { picUrl: "https://dev1.com/pic.png" } 
    },
    { 
        id: 2, 
        name: "Developer 2", 
        email: "dev2@test.com", 
        url: "https://dev2.com", 
        properties: { picUrl: null } 
    }
];

describe("DeveloperList Component", () => {

    beforeEach(() => {
        vi.clearAllMocks();
        vi.mocked(tokenService.getLocalAccessToken).mockReturnValue("fake-jwt");
        vi.mocked(useFetchState).mockReturnValue([mockDevelopers, vi.fn()]);
    });

    it("renders the table with developers", async () => {
        render(<DeveloperList />);
        
        await waitFor(() => {
            expect(screen.getByText("Developer 1")).toBeInTheDocument();
        });
        expect(screen.getByText("dev1@test.com")).toBeInTheDocument();
        expect(screen.getByText("https://dev1.com")).toBeInTheDocument();
        
        const img = screen.getByAltText("Developer 1");
        expect(img).toHaveAttribute("src", "https://dev1.com/pic.png");
    });

    it("uses fallback image when picUrl is missing", async () => {
        render(<DeveloperList />);
        
        await waitFor(() => {
            expect(screen.getByAltText("Developer 2")).toBeInTheDocument();
        });
        const img = screen.getByAltText("Developer 2");
        expect(img).toHaveAttribute("src", "/images/achievements/NotFoundImage.png");
    });
});
