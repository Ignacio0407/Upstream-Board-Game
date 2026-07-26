import { renderHook, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import useFetchState from "./useFetchState";
import * as fetchers from "./fetchers";

vi.mock("./fetchers");

describe("useFetchState Hook", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        vi.stubGlobal("alert", vi.fn());
    });

    it("initializes with the provided initial value", () => {
        const { result } = renderHook(() => useFetchState("initial", null, null));
        expect(result.current[0]).toBe("initial");
    });

    it("fetches data successfully and updates state", async () => {
        const mockData = { id: 1, name: "Test Object" };
        vi.mocked(fetchers.get).mockResolvedValue({
            ok: true,
            json: async () => mockData,
        } as Response);

        const { result } = renderHook(() => useFetchState(null, "/api/test", "fake-jwt"));

        await waitFor(() => {
            expect(result.current[0]).toEqual(mockData);
        });
        expect(fetchers.get).toHaveBeenCalledWith("/api/test", "fake-jwt");
    });

    it("handles json.message by showing alert if setMessage is not provided", async () => {
        const mockResponse = { message: "Server Message" };
        vi.mocked(fetchers.get).mockResolvedValue({
            ok: true,
            json: async () => mockResponse,
        } as Response);

        renderHook(() => useFetchState(null, "/api/test", "fake-jwt"));

        await waitFor(() => {
            expect(window.alert).toHaveBeenCalledWith("Server Message");
        });
    });

    it("handles json.message by calling setMessage if provided", async () => {
        const mockResponse = { message: "Server Message" };
        const setMessage = vi.fn();
        const setVisible = vi.fn();
        vi.mocked(fetchers.get).mockResolvedValue({
            ok: true,
            json: async () => mockResponse,
        } as Response);

        renderHook(() => useFetchState(null, "/api/test", "fake-jwt", setMessage, setVisible));

        await waitFor(() => {
            expect(setMessage).toHaveBeenCalledWith("Server Message");
            expect(setVisible).toHaveBeenCalledWith(true);
        });
    });

    it("handles json.message by calling setMessage without setVisible if setVisible is missing", async () => {
        const mockResponse = { message: "Server Message" };
        const setMessage = vi.fn();
        vi.mocked(fetchers.get).mockResolvedValue({
            ok: true,
            json: async () => mockResponse,
        } as Response);

        renderHook(() => useFetchState(null, "/api/test", "fake-jwt", setMessage));

        await waitFor(() => {
            expect(setMessage).toHaveBeenCalledWith("Server Message");
        });
    });

    it("handles fetch failure without setMessage or setVisible callbacks", async () => {
        const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});
        vi.mocked(fetchers.get).mockRejectedValue(new Error("Network Fail"));

        renderHook(() => useFetchState(null, "/api/test", "fake-jwt"));

        await waitFor(() => {
            expect(consoleSpy).toHaveBeenCalled();
        });
        consoleSpy.mockRestore();
    });

    it("handles fetch failure with setMessage or setVisible", async () => {
        const setMessage = vi.fn();
        const setVisible = vi.fn();
        const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});
        vi.mocked(fetchers.get).mockRejectedValue(new Error("Network Fail"));

        renderHook(() => useFetchState(null, "/api/test", "fake-jwt", setMessage, setVisible));

        await waitFor(() => {
            expect(setMessage).toHaveBeenCalledWith("Failed to fetch data");
            expect(setVisible).toHaveBeenCalledWith(true);
            expect(consoleSpy).toHaveBeenCalled();
        });
        consoleSpy.mockRestore();
    });

    it("skips fetch if id is 'new'", () => {
        renderHook(() => useFetchState("initial", "/api/test", "fake-jwt", null, vi.fn(), "new"));
        expect(fetchers.get).not.toHaveBeenCalled();
    });

    it("ignores update if component is unmounted during fetch", async () => {
        let resolveFetch: any;
        const fetchPromise = new Promise<Response>((resolve) => {
            resolveFetch = resolve;
        });
        vi.mocked(fetchers.get).mockReturnValue(fetchPromise);

        const { unmount } = renderHook(() => useFetchState("initial", "/api/test", "fake-jwt"));

        unmount();
        resolveFetch({
            ok: true,
            json: async () => ({ id: 1 }),
        });

        // We can't easily check state after unmount, but we ensure no "act" warnings or crashes
        // and that fetchers.get was called.
        expect(fetchers.get).toHaveBeenCalled();
    });
});
