import { describe, it, expect } from "vitest";
import { ColorToRgb, RgbToColor } from "./ColorParser";

describe("ColorParser", () => {
  describe("ColorToRgb", () => {
    it("converts all supported color names to RGB hex codes", () => {
      expect(ColorToRgb("WHITE")).toBe("#FFFFFF");
      expect(ColorToRgb("PURPLE")).toBe("#572364");
      expect(ColorToRgb("YELLOW")).toBe("#FFFF00");
      expect(ColorToRgb("RED")).toBe("#FF0000");
      expect(ColorToRgb("GREEN")).toBe("#008F39");
    });

    it("returns empty string for undefined or unknown colors", () => {
      expect(ColorToRgb(undefined)).toBe("");
      expect(ColorToRgb("BLUE")).toBe("");
    });
  });

  describe("RgbToColor", () => {
    it("converts RGB hex codes to color names", () => {
      expect(RgbToColor("#ffffff")).toBe("WHITE");
      expect(RgbToColor("#572364")).toBe("PURPLE");
      expect(RgbToColor("#FFFF00")).toBe("YELLOW");
      expect(RgbToColor("#FF0000")).toBe("RED");
      expect(RgbToColor("#008F39")).toBe("GREEN");
    });

    it("returns undefined for unknown RGB codes", () => {
      expect(RgbToColor("#123456")).toBeUndefined();
    });
  });
});
