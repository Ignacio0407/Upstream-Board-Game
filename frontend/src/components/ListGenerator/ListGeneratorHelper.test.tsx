import { vi } from 'vitest';
import { render, renderHook, screen, fireEvent } from "@testing-library/react";
import { renderData, useCtrlAToSelectOrDeselect, useExitMassEditOnEscape } from "./ListGeneratorHelper.tsx";
import { BrowserRouter } from "react-router-dom";

const dataset = [
  { id: 1, property1: "A", property2: "B" },
  { id: 2, property1: "C", property2: "D" },
];


describe("renderData —  Basic rendering", () => {

  test("Renders each row's data by default", () => {
    const component = renderData(dataset, new Set(), "id", vi.fn(), ["property1", "property2"],
      undefined, undefined, vi.fn(), vi.fn(), {});
    render(<BrowserRouter>{component}</BrowserRouter>);
    expect(screen.getByText("A")).toBeInTheDocument();
    expect(screen.getByText("D")).toBeInTheDocument();
  });

  test("Uses customRender when available", () => {
    const component = renderData(dataset, new Set(), "id", vi.fn(), ["property1"],
      undefined, undefined, vi.fn(), vi.fn(), {},
      (item) => <td>{item.property1}-custom</td>);
    render(<BrowserRouter>{component}</BrowserRouter>);
    expect(screen.getByText("A-custom")).toBeInTheDocument();
    expect(screen.getByText("C-custom")).toBeInTheDocument();
  });

  test("Renders both Edit and Delete buttons when available", () => {
    const component = renderData(dataset, new Set(), "id", vi.fn(), ["property1"],
      vi.fn(() => <button>Edit</button>), vi.fn(), vi.fn(), vi.fn(), {});
    render(<BrowserRouter>{component}</BrowserRouter>);
    expect(screen.getAllByText("Edit")).toHaveLength(2);
    expect(screen.getAllByText("Delete")).toHaveLength(2);
  });

  test.each([
    ["None",         undefined,                              undefined,  false],
    ["Only editButton", vi.fn(() => <button>Edit</button>),  undefined,  true ],
    ["Only deleteButton", undefined,                           vi.fn(),  true ],
  ])("Actions column with %s", (_label, editBtn, delBtn, shouldExist) => {
    const component = renderData(dataset, new Set(), "id", vi.fn(), ["property1"],
      editBtn as any, delBtn as any, vi.fn(), vi.fn(), {});
    render(<BrowserRouter>{component}</BrowserRouter>);
    const query = screen.queryByText("Actions");
    shouldExist ? expect(query).toBeInTheDocument() : expect(query).not.toBeInTheDocument();
  });

});


describe("renderData — customLabels", () => {

  test("Capitalizes property name (key) when there is NO customLabel", () => {
    const component = renderData(dataset, new Set(), "id", vi.fn(), ["property1"],
      undefined, undefined, vi.fn(), vi.fn(), {});
    render(<BrowserRouter>{component}</BrowserRouter>);
    expect(screen.getByText("Property1")).toBeInTheDocument();
  });

  test("Uses customLabel when available and omits capitalized key", () => {
    const component = renderData(dataset, new Set(), "id", vi.fn(), ["property1"],
      undefined, undefined, vi.fn(), vi.fn(), { property1: "Nombre" });
    render(<BrowserRouter>{component}</BrowserRouter>);
    expect(screen.getByText("Nombre")).toBeInTheDocument();
    expect(screen.queryByText("Property1")).not.toBeInTheDocument();
  });

});


describe("renderData — checkboxes", () => {

  test("Master checkbox is only checked when ALL items are selected", () => {
    const allSelected = renderData(dataset, new Set([1, 2]), "id", vi.fn(), ["property1"],
      undefined, undefined, vi.fn(), vi.fn(), {});
    const { unmount } = render(<BrowserRouter>{allSelected}</BrowserRouter>);
    expect(screen.getAllByRole("checkbox")[0]).toBeChecked();
    unmount();

    const noneSelected = renderData(dataset, new Set(), "id", vi.fn(), ["property1"],
      undefined, undefined, vi.fn(), vi.fn(), {});
    render(<BrowserRouter>{noneSelected}</BrowserRouter>);
    expect(screen.getAllByRole("checkbox")[0]).not.toBeChecked();
  });

  test("Row checkboxes reflect selectedIds state", () => {
    const component = renderData(dataset, new Set([1]), "id", vi.fn(), ["property1"],
      undefined, undefined, vi.fn(), vi.fn(), {});
    render(<BrowserRouter>{component}</BrowserRouter>);
    const boxes = screen.getAllByRole("checkbox");
    expect(boxes[1]).toBeChecked();
    expect(boxes[2]).not.toBeChecked();
  });

  test("Calls toggleSelectAll when clicking master checkbox", () => {
    const toggleAll = vi.fn();
    const component = renderData(dataset, new Set(), "id", toggleAll, ["property1"],
      undefined, undefined, vi.fn(), vi.fn(), {});
    render(<BrowserRouter>{component}</BrowserRouter>);
    fireEvent.click(screen.getAllByRole("checkbox")[0]);
    expect(toggleAll).toHaveBeenCalledTimes(1);
  });

  test("Calls toggleSelectOne with correct id when clicking row checkbox", () => {
    const toggleOne = vi.fn();
    const component = renderData(dataset, new Set(), "id", vi.fn(), ["property1"],
      undefined, undefined, toggleOne, vi.fn(), {});
    render(<BrowserRouter>{component}</BrowserRouter>);
    fireEvent.click(screen.getAllByRole("checkbox")[1]);
    expect(toggleOne).toHaveBeenCalledWith(1);
  });

});


describe("renderData — deleteButton callback", () => {

  test.each([
    [0, dataset[0]],
    [1, dataset[1]],
  ])("Calls deleteButton with item of index %i upon clicking", (btnIndex, expectedItem) => {
    const del = vi.fn();
    const component = renderData(dataset, new Set(), "id", vi.fn(), ["property1"],
      undefined, del, vi.fn(), vi.fn(), {});
    render(<BrowserRouter>{component}</BrowserRouter>);
    fireEvent.click(screen.getAllByText("Delete")[btnIndex]);
    expect(del).toHaveBeenCalledWith(expectedItem);
  });

});


describe("renderData — Empty dataset", () => {

  test("Renders table with master checkbox unchecked with NO rows", () => {
    const component = renderData([], new Set(), "id", vi.fn(), [],
      undefined, undefined, vi.fn(), vi.fn(), {});
    render(<BrowserRouter>{component}</BrowserRouter>);
    expect(screen.getByRole("table")).toBeInTheDocument();
    expect(screen.getByRole("checkbox")).not.toBeChecked();
  });

});


describe("useCtrlAToSelectOrDeselect", () => {

  afterEach(() => vi.clearAllMocks());

  test("Selects all upon pressing Ctrl+A when none is selected", () => {
    const setSelected = vi.fn();
    renderHook(() => useCtrlAToSelectOrDeselect([{ id: 1 }, { id: 2 }], "id", new Set(), setSelected, false));
    fireEvent.keyDown(window, { key: "a", ctrlKey: true });
    expect(setSelected).toHaveBeenCalledTimes(1);
  });

  test("Deselects all upon pressing Ctrl+A when none is selected", () => {
    const setSelected = vi.fn();
    renderHook(() => useCtrlAToSelectOrDeselect(
      [{ id: 1 }, { id: 2 }], "id", new Set([1, 2]), setSelected, false));
    fireEvent.keyDown(window, { key: "a", ctrlKey: true });
    expect(setSelected).toHaveBeenCalledTimes(1);
  });

  test.each([
    ["Ctrl+A",  { key: "a", ctrlKey: true  }],
    ["Meta+A",  { key: "a", metaKey: true  }],
  ])("Executes with %s", (_label, event) => {
    const setSelected = vi.fn();
    renderHook(() => useCtrlAToSelectOrDeselect([{ id: 1 }], "id", new Set(), setSelected, false));
    fireEvent.keyDown(window, event);
    expect(setSelected).toHaveBeenCalledTimes(1);
  });

  test("Does nothing if isUnabled=true", () => {
    const setSelected = vi.fn();
    renderHook(() => useCtrlAToSelectOrDeselect([{ id: 1 }], "id", new Set(), setSelected, true));
    fireEvent.keyDown(window, { key: "a", ctrlKey: true });
    expect(setSelected).not.toHaveBeenCalled();
  });

  test("Does NOT react to other keys", () => {
    const setSelected = vi.fn();
    renderHook(() => useCtrlAToSelectOrDeselect([{ id: 1 }], "id", new Set(), setSelected, false));
    fireEvent.keyDown(window, { key: "b", ctrlKey: true });
    expect(setSelected).not.toHaveBeenCalled();
  });

  test("Cleans listener upons dissasembling", () => {
    const removeEventListener = vi.spyOn(window, "removeEventListener");
    const { unmount } = renderHook(() =>
      useCtrlAToSelectOrDeselect([{ id: 1 }], "id", new Set(), vi.fn(), false));
    unmount();
    expect(removeEventListener).toHaveBeenCalledWith("keydown", expect.any(Function));
  });

});


describe("useExitMassEditOnEscape", () => {

  test("Calls setter with false upong pressing Esc", () => {
    const setter = vi.fn();
    renderHook(() => useExitMassEditOnEscape(setter));
    fireEvent.keyDown(window, { key: "Escape" });
    expect(setter).toHaveBeenCalledWith(false);
  });

  test("Does NOT call setter with other keys", () => {
    const setter = vi.fn();
    renderHook(() => useExitMassEditOnEscape(setter));
    fireEvent.keyDown(window, { key: "Enter" });
    expect(setter).not.toHaveBeenCalled();
  });

  test("Cleans listener upon dissasembling", () => {
    const removeEventListener = vi.spyOn(window, "removeEventListener");
    const { unmount } = renderHook(() => useExitMassEditOnEscape(vi.fn()));
    unmount();
    expect(removeEventListener).toHaveBeenCalledWith("keydown", expect.any(Function));
  });

});