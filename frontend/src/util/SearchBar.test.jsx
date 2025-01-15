import React from "react";
import SearchBar from './SearchBar';
import { render, screen, fireEvent } from "@testing-library/react";
import "@testing-library/jest-dom";
import { fetchById, fetchByName, fetchByNames } from "../util/fetchers";

// Mock de las funciones fetchers
jest.mock("../util/fetchers", () => ({
  fetchById: jest.fn(),
  fetchByName: jest.fn(),
  fetchByNames: jest.fn(),
}));

describe("SearchBar Component", () => {
  const mockSetter = jest.fn();
  const mockUri = "test-uri";
  const mockData = [{ id: 1, name: "Test Item" }];

  const setup = () => {
    render(<SearchBar setter={mockSetter} uri={mockUri} data={mockData} />);
    const input = screen.getByPlaceholderText(`Search ${mockUri}`);
    const button = screen.getByRole("button");
    return { input, button };
  };

  afterEach(() => {
    jest.clearAllMocks();
    mockSetter.mockClear();
  });

  describe("Rendering", () => {
    it("renders input and button", () => {
      const { input, button } = setup();
      expect(input).toBeInTheDocument();
      expect(button).toBeInTheDocument();
    });

    it("renders placeholder with the correct URI", () => {
      const { input } = setup();
      expect(input).toHaveAttribute("placeholder", `Search ${mockUri}`);
    });
  });

  describe("Functionality", () => {
    it("calls setter with original data when input is empty", () => {
      const { input } = setup();
      fireEvent.change(input, { target: { value: "" } });
      expect(mockSetter).toHaveBeenCalledWith(mockData);
    });

    it("calls fetchById for numeric input", async () => {
      const { input, button } = setup();
      fetchById.mockResolvedValue({ data: { id: 123, name: "Numeric Result" } });

      fireEvent.change(input, { target: { value: "123" } });
      fireEvent.click(button);

      expect(fetchById).toHaveBeenCalledWith("123", mockUri);
      await screen.findByText(/Numeric Result/);
      expect(mockSetter).toHaveBeenCalledWith([{ id: 123, name: "Numeric Result" }]);
    });

    it("calls fetchByName for single text input", async () => {
      const { input, button } = setup();
      fetchByName.mockResolvedValue({ data: { id: 1, name: "Name Result" } });

      fireEvent.change(input, { target: { value: "Test" } });
      fireEvent.click(button);

      expect(fetchByName).toHaveBeenCalledWith("Test", mockUri);
      await screen.findByText(/Name Result/);
      expect(mockSetter).toHaveBeenCalledWith([{ id: 1, name: "Name Result" }]);
    });

    it("calls fetchByNames for comma-separated names", async () => {
      const { input, button } = setup();
      fetchByNames.mockResolvedValue({ data: [{ id: 1, name: "Name1" }, { id: 2, name: "Name2" }] });

      fireEvent.change(input, { target: { value: "Name1, Name2" } });
      fireEvent.click(button);

      expect(fetchByNames).toHaveBeenCalledWith(["Name1", "Name2"], mockUri);
      await screen.findByText(/Name1/);
      expect(mockSetter).toHaveBeenCalledWith([
        { id: 1, name: "Name1" },
        { id: 2, name: "Name2" },
      ]);
    });
  });

  describe("Edge Cases", () => {
    it("handles invalid numeric input gracefully", async () => {
      const { input, button } = setup();
      fetchById.mockRejectedValue(new Error("Not Found"));

      fireEvent.change(input, { target: { value: "999" } });
      fireEvent.click(button);

      expect(fetchById).toHaveBeenCalledWith("999", mockUri);
      await new Promise((r) => setTimeout(r, 100)); // Esperar a que la promesa rechace
      expect(mockSetter).not.toHaveBeenCalledWith(expect.anything());
    });

    it("handles invalid text input gracefully", async () => {
      const { input, button } = setup();
      fetchByName.mockRejectedValue(new Error("Not Found"));

      fireEvent.change(input, { target: { value: "Nonexistent" } });
      fireEvent.click(button);

      expect(fetchByName).toHaveBeenCalledWith("Nonexistent", mockUri);
      await new Promise((r) => setTimeout(r, 100));
      expect(mockSetter).not.toHaveBeenCalledWith(expect.anything());
    });

    it("ignores empty comma-separated input", async () => {
      const { input, button } = setup();
      fetchByNames.mockResolvedValue({ data: [] });

      fireEvent.change(input, { target: { value: "," } });
      fireEvent.click(button);

      expect(fetchByNames).toHaveBeenCalledWith([""], mockUri);
      await screen.findByText(/No results/);
      expect(mockSetter).toHaveBeenCalledWith([]);
    });
  });
});