function ColorToRgb(color) {
    let colorReturn = ''
    if(color === "BLANCO")
    {
        colorReturn = '#FFFFFF';
    }else if(color === "MORADO"){
        colorReturn = '#572364';
    }else if(color === "AMARILLO"){
        colorReturn = '#FFFF00';
    }else if(color === "ROJO"){
        colorReturn = '#FF0000';
    }else if(color === "VERDE"){
        colorReturn = '#008F39';
    } 

    return colorReturn;

}

function RgbToColor(color) {
    let colorReturn = 'MORADO'
    if(color === "#FFFFFF")
    {
        colorReturn = 'BLANCO';
    }else if(color === "FFFF00"){
        colorReturn = 'AMARILLO';
    }else if(color === "#FF0000"){
        colorReturn = 'ROJO';
    }else if(color === "#008F39"){
        colorReturn = 'VERDE';
    }

    return colorReturn;

}

export { ColorToRgb, RgbToColor }