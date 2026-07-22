import { render, screen, fireEvent, waitFor } from "../test-utils";
import { describe, it, expect, vi, beforeEach, Mock } from "vitest";
import ColorHandler from "./ColorHandler";
import { post, buildSafeUrl } from "../util/fetchers";

// --- Mocks ---
vi.mock("../util/fetchers", () => ({
    post: vi.fn(),
    buildSafeUrl: vi.fn((base, path) => `${base}${path}`)
}));

// Mock the modal to isolate the ColorHandler test. We add a div to test that it correctly receives the takenColors prop.
vi.mock("./ColorPickerModal.tsx", () => ({
    default: ({ onColorSelect, takenColors }: any) => (
        <div>
            <button onClick={() => onColorSelect("RED")}>Select RED</button>
            <div data-testid="taken-colors">{takenColors.join(",")}</div>
        </div>
    )
}));

describe("ColorHandler Component", () => {
    const mockMatchId = 1;
    const mockJwt = "fake-jwt";
    const mockUserId = 10;
    let onColorChangedMock: ReturnType<typeof vi.fn>;

    beforeEach(() => {
        vi.clearAllMocks();
        onColorChangedMock = vi.fn();
    });

    it("Positive Case: Calls both APIs sequentially and executes onColorChanged", async () => {
        // Setup the first call (create player) and the second (salmonMatch)
        (post as Mock)
            .mockResolvedValueOnce({
                ok: true,
                json: async () => ({ id: 42 }) // Created player ID
            })
            .mockResolvedValueOnce({
                ok: true
            });

        render(
            <ColorHandler 
                matchId={mockMatchId} 
                jwt={mockJwt} 
                userId={mockUserId} 
                takenColors={[]} 
                onColorChanged={onColorChangedMock} 
            />
        );

        fireEvent.click(screen.getByText("Select RED"));

        await waitFor(() => {
            expect(post).toHaveBeenNthCalledWith(1, `/api/v1/players/match/${mockMatchId}`, mockJwt, { color: "RED", user: mockUserId });
            
            expect(buildSafeUrl).toHaveBeenCalledWith('/api/v1/salmonMatches/player/', 42);
            expect(post).toHaveBeenNthCalledWith(2, '/api/v1/salmonMatches/player/42', mockJwt);
            
            // 3. Verify the final callback is invoked
            expect(onColorChangedMock).toHaveBeenCalledTimes(1);
        });
    });

    it("Negative Case: Logs an error and stops the flow if the first API returns ok: false (e.g., 403 Forbidden)", async () => {
        const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});
        
        (post as Mock).mockResolvedValueOnce({
            ok: false,
            statusText: "Forbidden"
        });

        render(
            <ColorHandler 
                matchId={mockMatchId} jwt={mockJwt} userId={mockUserId} 
                takenColors={[]} onColorChanged={onColorChangedMock} 
            />
        );

        fireEvent.click(screen.getByText("Select RED"));

        await waitFor(() => {
            expect(consoleSpy).toHaveBeenCalledWith("Error creating player:", "Forbidden");
            expect(post).toHaveBeenCalledTimes(1); // The second call must not occur
            expect(onColorChangedMock).not.toHaveBeenCalled();
        });

        consoleSpy.mockRestore();
    });

    it("Negative Case: Enters the catch block if there is a network error in the first request", async () => {
        const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});
        const networkError = new Error("Network Down");
        
        // Simulate a fetch exception (e.g., server down)
        (post as Mock).mockRejectedValueOnce(networkError);

        render(
            <ColorHandler 
                matchId={mockMatchId} jwt={mockJwt} userId={mockUserId} 
                takenColors={[]} onColorChanged={onColorChangedMock} 
            />
        );

        fireEvent.click(screen.getByText("Select RED"));

        await waitFor(() => {
            expect(consoleSpy).toHaveBeenCalledWith("Unexpected error creating player:", networkError);
            expect(onColorChangedMock).not.toHaveBeenCalled();
        });

        consoleSpy.mockRestore();
    });

    it("Edge Case: Fails on the second request (salmonMatch) and safely enters the catch block", async () => {
        const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});
        const secondApiError = new Error("Internal Server Error on Salmon Match");

        (post as Mock)
            .mockResolvedValueOnce({
                ok: true,
                json: async () => ({ id: 42 }) // First request succeeds
            })
            .mockRejectedValueOnce(secondApiError); // Second request fails

        render(
            <ColorHandler 
                matchId={mockMatchId} jwt={mockJwt} userId={mockUserId} 
                takenColors={[]} onColorChanged={onColorChangedMock} 
            />
        );

        fireEvent.click(screen.getByText("Select RED"));

        await waitFor(() => {
            expect(post).toHaveBeenCalledTimes(2); // Both calls were attempted
            expect(consoleSpy).toHaveBeenCalledWith("Unexpected error creating player:", secondApiError);
            expect(onColorChangedMock).not.toHaveBeenCalled(); // Should not notify the parent if the flow doesn't complete
        });

        consoleSpy.mockRestore();
    });

    it("Integration Case: Correctly passes the takenColors prop to the child component", () => {
        const mockTakenColors = ["#FF0000", "#00FF00"];
        
        render(
            <ColorHandler 
                matchId={mockMatchId} jwt={mockJwt} userId={mockUserId} 
                takenColors={mockTakenColors} onColorChanged={onColorChangedMock} 
            />
        );

        // When mocking ColorPickerModal, we set it up to render takenColors inside a div
        expect(screen.getByTestId("taken-colors")).toHaveTextContent("#FF0000,#00FF00");
    });
});