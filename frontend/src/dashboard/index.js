import React, { useState, useEffect } from 'react'; 
import tokenService from '../services/token.service'
import jwt_decode from "jwt-decode";
import '../static/css/dashboard/dashb.css'
import BotonLink from '../util/BotonLink';
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

    const matchesList = 
      matches.map((match) => {
        return (
            <tr key={match.nombre} className='fila'>
                <td className='celda'>{match.nombre}</td>
                <td className='celda'>{match.jugadores}/{nJugadores}</td>
                <td className='celda'>{match.unirse}</td>
            </tr>
        );
      })

      const nJugadores = 5

      
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
                        <th className='cabeza'>Join</th>
                    </tr>
                </thead>
                <tbody>{matchesList}</tbody>
                </div>
            </div>
        </div> 
        </> 
); 
}
