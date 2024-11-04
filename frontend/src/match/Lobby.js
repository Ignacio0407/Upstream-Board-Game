import React, { useState, useEffect } from 'react';
import tokenService from '../services/token.service'
import useFetchState from "../util/useFetchState";
import '../static/css/game/lobby.css'
import PlayerCard from './PlayerCard';
import { Button } from 'reactstrap';
import { useNavigate } from "react-router-dom";

function Lobby({match}){
    const jwt = tokenService.getLocalAccessToken();
    const [players,setPlayers] = useFetchState([],`/api/v1/players`,jwt)
    const [filteredPlayers,setFilteredPlayers] = useState([])
    const navigate = useNavigate();
    useEffect(() => {
        const playersFiltered = players.filter(player => player.partida === match.id);
        console.log(playersFiltered); 
        setFilteredPlayers(playersFiltered);
    }, [players, match.id]);


    function startGame(){
        const putData =  {
            name: match.name,
            contrasena: match.contrasena,
            estado: "EN_CURSO",
            numjugadores: match.numjugadores,
            ronda: match.ronda,
            fase: "CASILLAS",
            jugador_inicial: 1,
            jugador_actual: 1,
        }
        fetch("/api/v1/matches/"+ match.id, {

            method: "PUT",
            headers: {
                Authorization: `Bearer ${jwt}`,
                Accept: "application/json",
                "Content-Type": "application/json",
              },
              body: JSON.stringify(putData),
        }).then( 
            window.location.reload(true))
    }

    function endGame(){
        const putData =  {
            name: match.name,
            contrasena: match.contrasena,
            estado: "FINALIZADA",
            numjugadores: match.numjugadores,
            ronda: match.ronda,
            fase: "CASILLAS",
            jugador_inicial: 1,
            jugador_actual: 1,
        }
        fetch("/api/v1/matches/"+ match.id, {

            method: "PUT",
            headers: {
                Authorization: `Bearer ${jwt}`,
                Accept: "application/json",
                "Content-Type": "application/json",
              },
              body: JSON.stringify(putData),
        }).then(navigate("/dashboard"))
    }
    
    const playerList = filteredPlayers.map((p) =>{
        return (
            <tr key={p.id} className="r">
                 <PlayerCard nombre={p.name} color={p.color}/>
            </tr>
            
           
        
        )
    })
           
    return(
        <div className='lobbyContainer'>
        <h1 className='lobbyTitleContainer'>
            {match.name}
        </h1>
        <div className='lobbyMainContainer'>
        
        <div className='lobbyPlayerContainer'>
        <h2> Jugadores </h2>
        {playerList}
        </div>
        <div className='lobbyUtilContainer'>
        </div>
        <Button color='success' onClick={startGame}>
            Iniciar Partida
        </Button>

        <Button color='danger' onClick={endGame}>
            Cancelar Partida
        </Button>
        </div>
        </div>
    )
}

export default Lobby