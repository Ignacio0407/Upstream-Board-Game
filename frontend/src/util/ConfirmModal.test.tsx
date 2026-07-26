import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import ConfirmModal from "./ConfirmModal";

describe("ConfirmModal", () => {
  it("renders with default props when visible is true", () => {
    const setVisible = vi.fn();
    const onConfirm = vi.fn();

    render(
      <ConfirmModal
        visible={true}
        setVisible={setVisible}
        onConfirm={onConfirm}
      />
    );

    expect(screen.getByText("Move salmons")).toBeInTheDocument();
    expect(
      screen.getByText("Continue and move salmons or undo moves?")
    ).toBeInTheDocument();
    expect(screen.getByText("Confirm")).toBeInTheDocument();
    expect(screen.getByText("Cancel")).toBeInTheDocument();
  });

  it("renders custom titles and messages", () => {
    const setVisible = vi.fn();
    const onConfirm = vi.fn();

    render(
      <ConfirmModal
        visible={true}
        setVisible={setVisible}
        onConfirm={onConfirm}
        title="Custom Title"
        message="Custom Message"
        confirmText="Yes Do It"
        cancelText="No Stop"
      />
    );

    expect(screen.getByText("Custom Title")).toBeInTheDocument();
    expect(screen.getByText("Custom Message")).toBeInTheDocument();
    expect(screen.getByText("Yes Do It")).toBeInTheDocument();
    expect(screen.getByText("No Stop")).toBeInTheDocument();
  });

  it("calls setVisible when toggle or cancel or close button is clicked", () => {
    const setVisible = vi.fn();
    const onConfirm = vi.fn();

    render(
      <ConfirmModal
        visible={true}
        setVisible={setVisible}
        onConfirm={onConfirm}
      />
    );

    const cancelBtn = screen.getByText("Cancel");
    fireEvent.click(cancelBtn);
    expect(setVisible).toHaveBeenCalledWith(false);

    const closeBtn = screen.getByText("×");
    fireEvent.click(closeBtn);
    expect(setVisible).toHaveBeenCalledWith(false);
  });

  it("calls onConfirm and setVisible(false) when confirm button is clicked", () => {
    const setVisible = vi.fn();
    const onConfirm = vi.fn();

    render(
      <ConfirmModal
        visible={true}
        setVisible={setVisible}
        onConfirm={onConfirm}
      />
    );

    const confirmBtn = screen.getByText("Confirm");
    fireEvent.click(confirmBtn);

    expect(onConfirm).toHaveBeenCalledTimes(1);
    expect(setVisible).toHaveBeenCalledWith(false);
  });
});
