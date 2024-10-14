import React, { useState, useEffect } from 'react';
import tokenService from '../services/token.service'
import jwt_decode from "jwt-decode";
import '../static/css/dashboard/dashb.css'
import BotonLink from '../util/BotonLink';
   
export default function Dashboard() { 
    const [username, setUsername] = useState("");
    const jwt = tokenService.getLocalAccessToken();

    useEffect(() => {
        if (jwt) {
            setUsername(jwt_decode(jwt).sub);
        }
    }, [jwt])

    const games = [
        {
          nombre: "Partida 1",
          jugadores: 2,
          unirse: <td className='botones-juntos'>
          <BotonLink color={"success"} direction={""} text={"Join"}></BotonLink>
          <BotonLink color={"danger"} direction={""} text={"Spectate"}></BotonLink>
          </td>,
          estado: "Esperando"
        },
        {
          nombre: "Partida 2",
          jugadores: 4,
          unirse: <td className='botones-juntos'>
          <BotonLink color={"success"} direction={""} text={"Join"}></BotonLink>
          <BotonLink color={"danger"} direction={""} text={"Spectate"}></BotonLink>
          </td>,
          estado: "En curso"
        },
        {
          nombre: "Partida 3",
          jugadores: 3,
          unirse: <td className='botones-juntos'>
          <BotonLink color={"success"} direction={""} text={"Join"}></BotonLink>
          <BotonLink color={"danger"} direction={""} text={"Spectate"}></BotonLink>
          </td>,
          estado: "En curso"
        },
        {
          nombre: "Partida 4",
          jugadores: 5,
          unirse: <td className='botones-juntos'>
          <BotonLink color={"success"} direction={""} text={"Join"}></BotonLink>
          <BotonLink color={"danger"} direction={""} text={"Spectate"}></BotonLink>
          </td>,
          estado: "En curso"
        },
        {
          nombre: "Partida 5",
          jugadores: 1,
          unirse: <td className='botones-juntos'>
          <BotonLink color={"success"} direction={""} text={"Join"}></BotonLink>
          <BotonLink color={"danger"} direction={""} text={"Spectate"}></BotonLink>
          </td>,
          estado: "Esperando"
        },
        {
          nombre: "Partida 6",
          jugadores: 4,
          unirse: <td className='botones-juntos'>
            <BotonLink color={"success"} direction={""} text={"Join"}></BotonLink>
            <BotonLink color={"danger"} direction={""} text={"Spectate"}></BotonLink>
            </td>,
          estado: "Esperando"
        },
        {
          nombre: "Partida 7",
          jugadores: 2,
          unirse: <td className='botones-juntos'>
          <BotonLink color={"success"} direction={""} text={"Join"}></BotonLink>
          <BotonLink color={"danger"} direction={""} text={"Spectate"}></BotonLink>
          </td>,
          estado: "En curso"
        },
        {
          nombre: "Partida 8",
          jugadores: 3,
          unirse: <td className='botones-juntos'>
          <BotonLink color={"success"} direction={""} text={"Join"}></BotonLink>
          <BotonLink color={"danger"} direction={""} text={"Spectate"}></BotonLink>
          </td>,
          estado: "Esperando"
        }
      ];



      const nJugadores = 5

      const gamesList = 
      games.map((d) => {
        return (
            <tr key={d.nombre} className='fila'>
                <td className='celda'>{d.nombre}</td>
                <td className='celda'>{d.jugadores}/{nJugadores}</td>
                <td className='celda'>{d.estado}</td>
                <td className='celda'>{d.unirse}</td>
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
                        <th className='cabeza'></th>
                    </tr>
                </thead>
                <tbody>{gamesList}</tbody>
                </div>
            </div>
        </div> 
        </> 
); 
} 