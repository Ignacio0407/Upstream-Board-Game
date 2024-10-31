import React, { useState, useEffect } from 'react'; 
import tokenService from '../services/token.service'
import jwt_decode from "jwt-decode";
import '../static/css/dashboard/dashb.css'
import BotonLink, { BotonLinkOutline } from "../util/BotonLink";
import useFetchState from '../util/useFetchState';
   
export default function Dashboard() { 
    const [username, setUsername] = useState("");
    const jwt = tokenService.getLocalAccessToken();
    const [matches, setMatches] = useFetchState([],`/api/v1/matches`,jwt);
    const user = tokenService.getUser()

    useEffect(() => {
        if (jwt) {
            setUsername(jwt_decode(jwt).sub);
        }
        console.log(matches)
    }, [jwt])

    const nJugadores = 5

    const matchesList = 
      matches.map((match) => {
        return (
            <tr key={d.nombre} className='fila'>
                <td className='celda'>{d.name}</td>
                <td className='celda'>{d.numJugadores}/{nJugadores}</td>
                <td className='celda'>{d.estado}</td>
                <td className='celda'>{d.estado === 'ESPERANDO' &&
                <BotonLink color={"success"} direction={'/matches/'+d.id} text={"Join game"}
                />}</td>
                <td className='celda'>{(d.estado === 'EN_CURSO' || d.estado === 'ESPERANDO') &&
                <BotonLink color={"warning"} direction={'/matches/'+d.id} text={"Spectate game"}
                />}</td>
            </tr>
        );
      })

    return ( 
        <> 
        <div className="dashboard-page-container">
            <h1 className='welcome'>
            Game Listing for {username}
            <div>
          </div>
            </h1>
            <div>
                <div className="game-table">
                <thead>
                    <tr className='fila'>
                        <th className='cabeza'>Game</th>
                        <th className='cabeza'>Players</th>
                        <th className='cabeza'>State</th>
                        <th className='cabeza'>Join</th>
                        <th className='cabeza'>Spectate</th>
                    </tr>
                </thead>
                <tbody>{matchesList}</tbody>
                </div>
            </div>
        </div> 
        </> 
); 
}
