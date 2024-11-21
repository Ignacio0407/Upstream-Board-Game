import React, { useState, useEffect } from 'react';
import tokenService from '../services/token.service'
import useFetchState from "../util/useFetchState";
import "../static/css/game/playerCard.css"
import { Table } from 'reactstrap';

function PlayerCard({nombre,color}){
    
    return(
        <Table className='playerCardContainer'>
            <tr>
            <td className='column-card'>
                {nombre}
            </td>
            <td className='column-card'>
                {color}
            </td>
            </tr>

        </Table>
    )

}


export default PlayerCard