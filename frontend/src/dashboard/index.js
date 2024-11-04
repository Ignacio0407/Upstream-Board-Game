import React, { useState, useEffect } from 'react'; 
import tokenService from '../services/token.service'
import jwt_decode from "jwt-decode";
import '../static/css/dashboard/dashb.css'
import BotonLink from "../util/BotonLink";
import useFetchState from '../util/useFetchState';
import SearchBar from '../util/SearchBar';
import { fetchById, fetchByName, fetchByNames } from '../util/fetchers';
   
export default function Dashboard() { 
    const [username, setUsername] = useState("");
    const jwt = tokenService.getLocalAccessToken();
    const [matches, setMatches] = useFetchState([],'/api/v1/matches',jwt);
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
                <td className='celda'>{match.name}</td>
                <td className='celda'>{match.numjugadores}</td>
                <td className='celda'>{match.estado}</td>
                <td className='celda'>{match.estado === 'ESPERANDO' &&
                <BotonLink color={"success"} direction={'/matches/'+match.id} text={"Join game"}
                />}</td>
                <td className='celda'>{(match.estado === 'EN_CURSO' || match.estado === 'ESPERANDO') &&
                <BotonLink color={"warning"} direction={'/matches/'+match.id} text={"Spectate game"}
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
            <SearchBar
            fetchById={(id) => fetchById(`/partidas/${id}`)}
            fetchByName={(name) => fetchByName(`/partidas/name/${name}`)}
            fetchByNames={(names) => fetchByNames(`/partidas/names/${names.join(",")}`)}
            />
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
