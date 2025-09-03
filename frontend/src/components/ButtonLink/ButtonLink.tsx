/**
 * -----------------------------------------------------------------------------
 * Autor / Author: Ignacio Gutiérrez Serrera
 * 
 * 🇬🇧 This software was developed by Ignacio Gutiérrez Serrera.
 * It may be used, modified, and distributed for personal or commercial 
 * purposes, provided that explicit credit is given to the original author.
 * 
 * If this code is used as-is or with minor to moderate modifications 
 * as part of another software, proper attribution must be given to 
 * Ignacio Gutiérrez Serrera.
 * (e.g., credits, documentation, or user interface).
 * 
 * This license notice must not be removed in any redistribution 
 * of the source code or binaries.
 * 
 * 🇪🇸 Este software ha sido desarrollado por Ignacio Gutiérrez Serrera.
 * Se permite su uso, modificación y distribución con fines personales o 
 * comerciales, siempre que se mantenga el reconocimiento explícito al autor.
 * 
 * Si este código se utiliza tal cual, o con modificaciones leves o medias 
 * como parte de otro software, se debe otorgar el crédito correspondiente 
 * a Ignacio Gutiérrez Serrera.
 * (por ejemplo, en los créditos, documentación o interfaz del producto).
 * 
 * No se permite eliminar este aviso de licencia en ninguna redistribución 
 * del código fuente o binario.
 * -----------------------------------------------------------------------------
 */

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