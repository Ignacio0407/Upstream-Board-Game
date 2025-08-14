import { JSX } from "react";

interface WhiteSpaceProps {
  size?: number;
  orientation?: 'horizontal' | 'vertical'; // restringido a valores esperados
}

/**
 * Crea un espacio en blanco con un tamaño y una orientación.
 * @param {number} size - tamaño del espacio. Por defecto 30
 * @param {string} orientation - orientación (vertical, horizontal) del espacio. Por defecto horizontal
 */
export default function WhiteSpace({ size = 30, orientation = "horizontal" }: WhiteSpaceProps): JSX.Element {
  const styles = { 
    display: orientation === "horizontal" ? "inline-block" : "block",
    width: orientation === "horizontal" ? `${size}px` : "1px",
    height: orientation === "horizontal" ? "1px" : `${size}px`
  };

  return <div style={styles} />;
}