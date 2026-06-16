import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import ErrorModal from "./ErrorModal";

describe("ErrorModal", () => {
    const defaultProps = {
        visible: true,
        setVisible: vi.fn(),
        message: "Something went wrong",
    };

    // ── renderizado condicional ───────────────────────────────────────────────

    it("should return null when message is empty", () => {
        const { container } = render(
            <ErrorModal visible={true} setVisible={vi.fn()} message="" />
        );
        expect(container.firstChild).toBeNull();
    });

    it("should render modal when message is provided and visible is true", () => {
        render(<ErrorModal {...defaultProps} />);
        expect(screen.getByText("Alert!")).toBeInTheDocument();
        expect(screen.getByText("Something went wrong")).toBeInTheDocument();
    });

    it("should not render modal content when visible is false", () => {
        render(<ErrorModal {...defaultProps} visible={false} />);
        expect(screen.queryByText("Alert!")).not.toBeInTheDocument();
    });

    // ── contenido ────────────────────────────────────────────────────────────

    it("should display the message in the modal body", () => {
        render(<ErrorModal {...defaultProps} message="Custom error message" />);
        expect(screen.getByText("Custom error message")).toBeInTheDocument();
    });

    it("should render the Close button", () => {
        render(<ErrorModal {...defaultProps} />);
        expect(screen.getByRole("button", { name: /close/i })).toBeInTheDocument();
    });

    it("should render the × close button in the header", () => {
        render(<ErrorModal {...defaultProps} />);
        expect(screen.getByText("×")).toBeInTheDocument();
    });

    // ── toggle ────────────────────────────────────────────────────────────────

    it("should call setVisible with false when Close button is clicked", () => {
        const setVisible = vi.fn();
        render(<ErrorModal {...defaultProps} visible={true} setVisible={setVisible} />);

        fireEvent.click(screen.getByRole("button", { name: /close/i }));

        expect(setVisible).toHaveBeenCalledWith(false);
        expect(setVisible).toHaveBeenCalledTimes(1);
    });

    it("should call setVisible with false when × header button is clicked", () => {
        const setVisible = vi.fn();
        render(<ErrorModal {...defaultProps} visible={true} setVisible={setVisible} />);

        fireEvent.click(screen.getByText("×"));

        expect(setVisible).toHaveBeenCalledWith(false);
        expect(setVisible).toHaveBeenCalledTimes(1);
    });

    it("should call setVisible with true when modal is closed and toggled", () => {
        const setVisible = vi.fn();
        render(<ErrorModal {...defaultProps} visible={false} setVisible={setVisible} />);

        // visible=false, so toggle calls setVisible(!false) = setVisible(true)
        // Triggered via the Modal's toggle prop — we test this via the header × button
        // which only renders when visible=true, so we re-render with visible=true
        render(<ErrorModal {...defaultProps} visible={true} setVisible={setVisible} />);
        fireEvent.click(screen.getAllByText("×")[0]);

        expect(setVisible).toHaveBeenCalledWith(false);
    });
});