function ColorToRgb(color) {
    let colorReturn = ''
    if(color === "WHITE")
    {
        colorReturn = '#FFFFFF';
    }else if(color === "PURPLE"){
        colorReturn = '#572364';
    }else if(color === "YELLOW"){
        colorReturn = '#FFFF00';
    }else if(color === "RED"){
        colorReturn = '#FF0000';
    }else if(color === "GREEN"){
        colorReturn = '#008F39';
    } 

    return colorReturn;

}

const COLOR_MAP = {
    '#FFFF00': 'YELLOW',
    '#FF0000': 'RED', 
    '#008F39': 'GREEN',
    '#572364': 'PURPLE',
    '#FFFFFF': 'WHITE'
};

function RgbToColor(rgbColor) {
    return COLOR_MAP[rgbColor.toUpperCase()];
}

export { ColorToRgb, RgbToColor }