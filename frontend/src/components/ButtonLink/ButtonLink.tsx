/**
 * -----------------------------------------------------------------------------
 * Autor / Author: Ignacio Gutiérrez Serrera
 * GitHub: https://github.com/Ignacio0407 (@Ignacio0407)
 * Contact / Email: ignaciogutierrezserrera@gmail.com
 * 
 * 🇬🇧 NON-COMMERCIAL & EDUCATIONAL LICENSE:
 * This software was developed by Ignacio Gutiérrez Serrera.
 * Permission is granted to use, copy, modify, and study this software for 
 * non-commercial, personal, academic, and educational purposes, provided 
 * that this copyright notice, author attribution, and contact details are 
 * preserved in all copies or substantial portions of the software.
 * 
 * Any commercial use, integration into proprietary software, or closed-source 
 * distribution is strictly prohibited without prior written permission and an 
 * express commercial license granted by the author.
 * 
 * 🇪🇸 LICENCIA PARA FINES NO LUCRATIVOS Y EDUCATIVOS:
 * Este software ha sido desarrollado por Ignacio Gutiérrez Serrera.
 * Se autoriza su uso, copia, modificación y estudio exclusivamente para 
 * fines personales, académicos, de investigación y educativos no lucrativos, 
 * con la condición obligatoria de mantener intacto este aviso de copyright, 
 * la atribución de autoría y los datos de contacto en todas las copias.
 * 
 * Queda terminantemente prohibido cualquier uso comercial, integración en 
 * software privativo o distribución con fines lucrativos sin la autorización 
 * previa por escrito del autor y la adquisición de una licencia comercial.
 * -----------------------------------------------------------------------------
 */

import React from "react";
import { Button } from "reactstrap";
import { Link } from "react-router-dom";

interface ButtonLinkProps {outline?: boolean; color?: string; direction: string; text: string; textColor?: string, state?: any}

/**
 * @param outline - Whether the button color is applied to the border. Default: false.
 * @param color - Button background or border color. Default: white.
 * @param direction - Path to navigate to when clicked.
 * @param text - Text to display inside the button.
 * @param textColor - Optional text color. Default: white.
 * @param state - Optional state to pass to the Link.
 * @returns a styled button that navigates to a route via React Router.
 */
const ButtonLink: React.FC<ButtonLinkProps> = ({outline = false, color = "white", direction, text, textColor = "white", state}) => {
  return (
    <Button outline={outline} color={color}>
      <Link to={direction} state={state} className="btn sm" style={{ textDecoration: "none", color: textColor }}>
        {text}
      </Link>
    </Button>
  );
};

export default ButtonLink;