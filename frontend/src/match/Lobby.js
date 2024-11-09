import React, { useState, useEffect } from 'react';
import tokenService from '../services/token.service'
import useFetchState from "../util/useFetchState";
import '../static/css/game/lobby.css'
import PlayerCard from './PlayerCard';
import { Button, Table } from 'reactstrap';
import { useNavigate } from "react-router-dom";
import ColorPickerModal from '../util/ColorPickerModal';
import { ColorToRgb } from '../util/ColorParser';

function Lobby({match}){
    const jwt = tokenService.getLocalAccessToken();
    const user = tokenService.getUser()
    const [finalUser,setUser] = useFetchState([],`/api/v1/users/${user.id}`,jwt)
    const [players,setPlayers] = useFetchState([],`/api/v1/players`,jwt)
    const [filteredPlayers,setFilteredPlayers] = useState([])
    const [userPlayer,setUserPlayer] = useState(null);
    const [showColorPicker, setShowColorPicker] = useState(true); // Empieza en false
    const navigate = useNavigate();
    const [takenColors, setTakenColors] = useState([]);
    useEffect(() => {
        const playersFiltered = players.filter(player => player.partida === match.id);
        const userPlayer = playersFiltered.find(player => player.usuario.id === user.id);
        console.log(filteredPlayers);
        setUserPlayer(userPlayer);
        setFilteredPlayers(playersFiltered);
        const colorsUsed = playersFiltered.map(player => ColorToRgb(player.color));
        console.log(colorsUsed);
        setTakenColors(colorsUsed);
        
    }, [players, match.id,user.id]);


    useEffect(() => {
        // Función para obtener los jugadores desde la API
        const fetchPlayers = async () => {
            const response = await fetch(`/api/v1/players`, {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${jwt}`,
                    Accept: "application/json",
                    "Content-Type": "application/json",
                },
            });
            const data = await response.json();
            setPlayers([])
            setPlayers(data); // Actualiza el estado con los nuevos jugadores
        };

        // Inicializa el fetch de jugadores
        fetchPlayers();

        // Establece un intervalo para actualizar los jugadores cada 10 segundos
        const intervalId = setInterval(fetchPlayers, 3000); // Actualiza cada 10 segundos

        // Limpia el intervalo cuando el componente se desmonta
        return () => clearInterval(intervalId);
    }, [jwt]);

/*
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
        const matchTiles = {
            match: match.id,
            tile: 1,
            coordinate: {x: 0, y: 0},
            orientation: 0,
            capacity: 5
        }
        fetch("/api/v1/matches/"+ match.id, {

            method: "PUT",
            headers: {
                Authorization: `Bearer ${jwt}`,
                Accept: "application/json",
                "Content-Type": "application/json",
              },
              body: JSON.stringify(putData),
        }).then(fetch("/api/v1/matchTiles", {
            method: "POST",
            headers: {
                Authorization: `Bearer ${jwt}`,
                Accept: "application/json",
                "Content-Type": "application/json"
            },
            body: JSON.stringify(matchTiles),
        })).then( 
            window.location.reload(true))
    }
*/
    const startGame = async () => {
        const tileConfigs = [
            { tile: 1, count: 7, capacity: 5 },
            { tile: 2, count: 5, capacity: 4 },
            { tile: 3, count: 5, capacity: 5 },
            { tile: 4, count: 3, capacity: 5 },
            { tile: 5, count: 5, capacity: 5 },
            { tile: 6, count: 4, capacity: 5 },
            { tile: 7, count: 1, capacity: 99 },
            { tile: 8, count: 1, capacity: 99 }
        ];

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
    
        let x = 0;
        let y = 0;
        let orientation = 0;
    
        const requests = tileConfigs.flatMap(({ tile, count, capacity }) =>
            Array.from({ length: count }, () => {
                const matchTile = {
                    match: match.id,
                    tile: tile,
                    coordinate: { x, y },
                    orientation: orientation,
                    capacity: capacity
                };
    
                return fetch("/api/v1/matches/"+ match.id, {

                    method: "PUT",
                    headers: {
                        Authorization: `Bearer ${jwt}`,
                        Accept: "application/json",
                        "Content-Type": "application/json",
                      },
                      body: JSON.stringify(putData),
                }).then(fetch("/api/v1/matchTiles", {
                    method: "POST",
                    headers: {
                        Authorization: `Bearer ${jwt}`,
                        Accept: "application/json",
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify(matchTile)
                }));
            })
        );
    
        try {
            await Promise.all(requests);
            console.log("Todas las MatchTiles han sido creadas.");
        } catch (error) {
            console.error("Error al crear algunas MatchTiles:", error);
        }

        (window.location.reload(true))
    };
    

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

    
    function handleColorChange(color) {
        const emptyPlayer = {
            name: finalUser.username,
            color: color,
            orden: 0,
            vivo: true,
            puntos: 0,
            usuario: finalUser.id,
            partida: match.id,
        }
        console.log(JSON.stringify(emptyPlayer))
        console.log("color " + color) // Para depuración, muestra el objeto en consola)
        fetch(`/api/v1/players`, {  // Usa el ID del usuario actual
            method: "POST",
            headers: {
                Authorization: `Bearer ${jwt}`,
                Accept: "application/json",
                "Content-Type": "application/json",
            },
            body: JSON.stringify(emptyPlayer),
        }).then(() => {
            setShowColorPicker(false); // Oculta el modal después de seleccionar el color
            
        });

    }
    
    return(
        <div className='lobbyContainer'>
        {showColorPicker &&
        <ColorPickerModal onColorSelect={handleColorChange} takenColors = {takenColors} />
        }
        <h1 className='lobbyTitleContainer'>
            {match.name}
        </h1>
        <div className='lobbyMainContainer'>
        
        <Table className='lobbyPlayerContainer'>
        <thead> 
            <h2>
            Jugadores 
            </h2>
        </thead>
        {playerList}
        </Table>
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