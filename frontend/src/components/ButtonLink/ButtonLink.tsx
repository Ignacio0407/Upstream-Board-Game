import React from "react";
import { Button } from "reactstrap";
import { Link } from "react-router-dom";

interface ButtonLinkProps {outline?: boolean; color?: string; direction: string; text: string; textColor?: string}

/**
 * @param outline - Whether the button color is applied to the border. Default: false.
 * @param color - Button background or border color. Default: white.
 * @param direction - Path to navigate to when clicked.
 * @param text - Text to display inside the button.
 * @param colorTexto - Optional text color. Default: white.
 * @returns a styled button that navigates to a route via React Router.
 */
const ButtonLink: React.FC<ButtonLinkProps> = ({outline = false, color = "white", direction, text, textColor = "white"}) => {
  return (
    <Button outline={outline} color={color}>
      <Link
        to={direction}
        className="btn sm"
        style={{ textDecoration: "none", color: textColor }}
      >
        {text}
      </Link>
    </Button>
  );
};

export default ButtonLink;