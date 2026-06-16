import { render, screen, fireEvent, waitFor } from "../test-utils";
import { describe, it, expect, vi, beforeEach } from "vitest";
import ColorHandler from "./ColorHandler";
import { post } from "./fetchers";

// --- Mocks ---

vi.mock("./fetchers");
vi.mock("./ColorPickerModal.tsx", () => ({
    default: ({ onColorSelect }: any) => (
        <button onClick={() => onColorSelect("RED")}>Select RED</button>
    )
}));

describe("ColorHandler Component", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("calls API and onColorChanged when a color is selected successfully", async () => {
        const onColorChanged = vi.fn();
        // First post for player creation
        vi.mocked(post).mockResolvedValueOnce({
            ok: true,
            json: async () => ({ id: 1 })
        } as Response);
        // Second post for salmonMatches creation
        vi.mocked(post).mockResolvedValueOnce({ ok: true } as Response);

        render(
            <ColorHandler 
                matchId={1} 
                jwt="jwt" 
                finalUserId={10} 
                takenColors={[]} 
                onColorChanged={onColorChanged} 
            />
        );

        fireEvent.click(screen.getByText("Select RED"));

        await waitFor(() => {
            expect(post).toHaveBeenCalledWith("/api/v1/players/match/1", "jwt", { color: "RED", user: 10 });
            expect(onColorChanged).toHaveBeenCalled();
        });
    });

    it("logs error and does not call onColorChanged if API fails", async () => {
        const onColorChanged = vi.fn();
        vi.mocked(post).mockResolvedValueOnce({
            ok: false,
            statusText: "Forbidden"
        } as Response);
        
        const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});

        render(
            <ColorHandler 
                matchId={1} 
                jwt="jwt" 
                finalUserId={10} 
                takenColors={[]} 
                onColorChanged={onColorChanged} 
            />
        );

        fireEvent.click(screen.getByText("Select RED"));

        await waitFor(() => {
            expect(consoleSpy).toHaveBeenCalledWith("Error creating player:", "Forbidden");
            expect(onColorChanged).not.toHaveBeenCalled();
        });
        
        consoleSpy.mockRestore();
    });
});
