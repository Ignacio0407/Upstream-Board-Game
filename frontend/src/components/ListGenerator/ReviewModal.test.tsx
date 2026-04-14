import { render, screen } from "@testing-library/react";
import { ReviewModal } from "./ReviewModal";
import { vi } from "vitest";

const originals = [{ id: 1, name: "Charlie" }, { id: 2, name: "Alpha" }];
const updated = [undefined, { id: 2, name: "Alpha Edited" }];

describe("ReviewModal — UI", () => {

  test("Shows 'Not updated' for items with NO changes", () => {
    render(
      <ReviewModal 
        originals={originals} 
        updated={updated} 
        renderItem={(i) => <span>{i.name}</span>} 
        onConfirm={() => {}} 
      />
    );
    expect(screen.getByText("Not updated")).toBeInTheDocument();
  });

  test("Shows 'Old' and 'New' for items WITH changes", () => {
    render(
      <ReviewModal 
        originals={originals} 
        updated={updated} 
        renderItem={(i: any) => <span>{i.name}</span>} 
        onConfirm={() => {}} 
      />
    );

    // Using a matcher function to find text broken across multiple elements
    const oldTextFound = screen.getByText((_content, element) => {
      return element?.textContent === "Old Alpha";
    });
    const newTextFound = screen.getByText((_content, element) => {
      return element?.textContent === "New Alpha Edited";
    });

    expect(oldTextFound).toBeInTheDocument();
    expect(newTextFound).toBeInTheDocument();
  });

  test("Calls onConfirm when clicking confirm button", () => {
    const confirm = vi.fn();
    render(
      <ReviewModal originals={originals} updated={updated} renderItem={vi.fn()} onConfirm={confirm} />
    );
    screen.getByText("Confirm all").click();
    expect(confirm).toHaveBeenCalled();
  });

});
