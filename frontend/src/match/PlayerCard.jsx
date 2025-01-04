import React, { useState, useEffect } from 'react';
import "../static/css/game/playerCard.css"
import { Table } from 'reactstrap';
import { ColorToRgb } from '../util/ColorParser';

export default function PlayerCard({nombre,color}){
    
    return(
        <Table className='playerCardContainer'>
            <tr className='rowContainer'>
            <td className='column-card-name'>
                {nombre}
            </td>
            <td className='column-card-color' style={{background: ColorToRgb(color)}}>
            </td>
            </tr>
        </Table>
    )
}