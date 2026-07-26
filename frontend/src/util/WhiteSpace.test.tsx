import { render } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import WhiteSpace from "./WhiteSpace";

describe("WhiteSpace", () => {
  it("renders with default props (horizontal, 30px)", () => {
    const { container } = render(<WhiteSpace />);
    const div = container.firstChild as HTMLDivElement;
    expect(div).toHaveStyle("display: inline-block");
    expect(div).toHaveStyle("width: 30px");
    expect(div).toHaveStyle("height: 1px");
  });

  it("renders with vertical orientation and custom size", () => {
    const { container } = render(<WhiteSpace size={50} orientation="vertical" />);
    const div = container.firstChild as HTMLDivElement;
    expect(div).toHaveStyle("display: block");
    expect(div).toHaveStyle("width: 1px");
    expect(div).toHaveStyle("height: 50px");
  });
});
