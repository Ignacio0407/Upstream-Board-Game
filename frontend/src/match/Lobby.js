import React, { useState, useEffect } from 'react';
import tokenService from '../services/token.service'
import useFetchState from "../util/useFetchState";
import '../static/css/game/lobby.css'
import PlayerCard from './PlayerCard';
import { Button, Table } from 'reactstrap';
import { useNavigate } from "react-router-dom";
import ColorPickerModal from '../util/ColorPickerModal';
import { ColorToRgb } from '../util/ColorParser';
import { useLocation } from "react-router-dom";

function Lobby({match}){
    const jwt = tokenService.getLocalAccessToken();
    const user = tokenService.getUser()
    const [finalUser,setUser] = useFetchState([],`/api/v1/users/${user.id}`,jwt)
    const [players,setPlayers] = useFetchState([],`/api/v1/players`,jwt)
    const [matches,setMatches] = useFetchState([],`/api/v1/matches/${match.id}`,jwt)
    const [filteredPlayers,setFilteredPlayers] = useState([])
    const [userPlayer,setUserPlayer] = useState(null);
    const [showColorPicker, setShowColorPicker] = useState(true); // Empieza en false
    const navigate = useNavigate();
    const [takenColors, setTakenColors] = useState([]);
    const [numjug, Setnumjug] = useState(match.numjugadores);
    const [loading, setLoading] = useState(false);
    const [ordenPartida, setOrdenPartida] = useState(0);
    const spectatorIds = useLocation().state?.spectatorIds||[];

    const putData = {
        name: match.name,
        contrasena: match.contrasena,
        creadorpartida: match.creadorpartida,
        estado: "EN_CURSO",
        numjugadores: 1,
        ronda: match.ronda,
        fase: "CASILLAS",
        jugadorinicial: 1,
        jugadoractual: 1,
    };

    const [reData, setReData] = useState(putData);


    useEffect(() => {
        const playersFiltered = players.filter(player => player.partida === match.id);
        const playerUser = playersFiltered.find(player => player.usuario.id === user.id);
        setOrdenPartida(0);
        setUserPlayer(playerUser);
        setFilteredPlayers(playersFiltered);
        const colorsUsed = playersFiltered.map(player => ColorToRgb(player.color));
        setTakenColors(colorsUsed);
        Setnumjug(playersFiltered.length);
        console.log("playersfiltered",match)
        if(playersFiltered.length > 0) {
            const jugInicial = playersFiltered.filter(p => p.orden === 0);            
            setReData(d => ({...d, numjugadores: playersFiltered.length , jugadorinicial: jugInicial[0].id, jugadoractual: jugInicial[0].id}))
        }
        const intervalId = setInterval(fetchPlayers, 1000);
        return () => clearInterval(intervalId);
        
    }, [players, match.id, user.id]);


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

const startGame = async () => {
    setLoading(true);

    const tileConfigs = [
        { tile: 1, count: 7, capacity: 5 },
        { tile: 2, count: 5, capacity: 4 },
        { tile: 3, count: 5, capacity: 5 },
        { tile: 4, count: 3, capacity: 5 },
        { tile: 5, count: 5, capacity: 5 },
        { tile: 6, count: 4, capacity: 5 }
    ];    

    let x = null;
    let y = null;
    let orientation = 0;

    // Generar una lista completa de todos los tiles según tileConfigs
    const allTiles = tileConfigs.flatMap(({ tile, count, capacity }) =>
        Array.from({ length: count }, () => ({
            match: match.id,
            tile,
            coordinate: { x, y },
            orientation,
            capacity
        }))
    );

    // Mezclar la lista de tiles aleatoriamente
    const shuffledTiles = allTiles.sort(() => Math.random() - 0.5);

    // Crear las solicitudes de inserción para cada tile en el orden aleatorio
    const requests = shuffledTiles.map(matchTile => 
        fetch("/api/v1/matches/" + match.id, {
            method: "PUT",
            headers: {
                Authorization: `Bearer ${jwt}`,
                Accept: "application/json",
                "Content-Type": "application/json",
            },
            body: JSON.stringify(reData),
        }).then(() => fetch("/api/v1/matchTiles", {
            method: "POST",
            headers: {
                Authorization: `Bearer ${jwt}`,
                Accept: "application/json",
                "Content-Type": "application/json"
            },
            body: JSON.stringify(matchTile)
        }))
    );

    try {
        await Promise.all(requests);
        console.log("Todas las MatchTiles han sido creadas en orden aleatorio.");
    } catch (error) {
        console.error("Error al crear algunas MatchTiles:", error);
    }
    setLoading(false);
    window.location.reload(true);
};
    

    function endGame(){
        console.log(user.id)
        if(spectatorIds.find(p => p === user.id)){
            navigate("/dashboard");
        }else{
        const numJugadores = numjug - 1;
        console.log("playerId",filteredPlayers)
        const playerId = filteredPlayers.find(p => p.usuario === user.id).id;
        
        console.log(matches)
        console.log("numjugadores",match.id)
        if(numJugadores === 0){
            matches.estado = "FINALIZADA";
            matches.numjugadores = 0;
        }
        else{
            matches.estado = "ESPERANDO";
            matches.numjugadores = numJugadores; 
        }
        console.log("match",putData)
        fetch("/api/v1/players/"+ playerId, {
            method: "DELETE",
            headers: {
                Authorization: `Bearer ${jwt}`,
                Accept: "application/json",
                "Content-Type": "application/json",
            },
                 
        }).then(()=> fetch("/api/v1/matches/"+ match.id, {

            method: "PUT",
            headers: {
                Authorization: `Bearer ${jwt}`,
                Accept: "application/json",
                "Content-Type": "application/json",
              },
              body: JSON.stringify(matches),
        }).then(
            navigate("/dashboard")
        ))
    }
    }
    
    const playerList = filteredPlayers.map((p) =>{
        return (
            <tr key={p.id} className="r">
                 <PlayerCard nombre={p.name} color={p.color}/>
            </tr>
            
        )
    })

    
    function handleColorChange(color) {
        const order = filteredPlayers.length + 1 - 1;
        const emptyPlayer = {
            name: finalUser.username,
            color: color,
            orden: order,
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
            setShowColorPicker(false); // Oculta el modal después de seleccionar el color // Incrementa el número de orden para los jugadores  
        });

    }
    
    return(
        <div className='lobbyContainer'>
        {filteredPlayers.find(p => p.usuario === user.id)===undefined && spectatorIds.find(p => p === user.id) === undefined &&(showColorPicker &&
        <ColorPickerModal onColorSelect={handleColorChange} takenColors = {takenColors} />
        )}
        <h1 className='lobbyTitleContainer'>
            {match.name}
        </h1>
        {match.contrasena !== "" && <h4 className='passwordContainer'>
            Password: {match.contrasena}
        </h4>}
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
        {spectatorIds.find(p => p === user.id) === undefined && <Button color='success' onClick={startGame}>
            Iniciar Partida
        </Button>}
        {loading && <div>Loading tiles...</div>}

        <Button color='danger' onClick={endGame}>
            Salir de la Partida
            </Button>
        </div>
        </div>
    )
}

export default Lobby