import React, { useState, useEffect } from 'react';
import tokenService from '../services/token.service'
import useFetchState from "../util/useFetchState";
import '../static/css/game/game.css'
import { ColorToRgb } from '../util/ColorParser';
import bearTile from '../static/images/tiles/bearTile.png'
import eagleTile from '../static/images/tiles/eagleTile.png'
import heronTile from '../static/images/tiles/heronTile.png'
import jumpTile from '../static/images/tiles/jumpTile.png'
import rockTile from '../static/images/tiles/rockTile.png'
import seaTile from '../static/images/tiles/seaTile.png'
import spawningTile from '../static/images/tiles/spawningTile.png'
import waterTile from '../static/images/tiles/waterTile.png'

function Game({match}){
    const jwt = tokenService.getLocalAccessToken();
    /*const [players,setPlayers] = useFetchState([],`/api/v1/matches/${match.id}/players`,jwt) // Tenemos los jugadores de la partida
    const [filteredPlayers,setFilteredPlayers] = useState([])*/
    const [tilesList,setTilesList] = useFetchState([], '/api/v1/casilla',jwt)
    const [matchTiles, setMatchTiles] = useFetchState([], `/api/v1/matchTiles/${match.id}`,jwt)
    //const playerList = players.map(player => <div color= {ColorToRgb(player.color)}> </div>)
    const matchTilesWithoutSeaOrSpawn = matchTiles.filter(mT => tilesList[mT.tile-1].tipo !== 'MAR' && tilesList[mT.tile-1].tipo !== 'DESOVE') // Quitar las casillas de mar y desove;  
    matchTilesWithoutSeaOrSpawn.sort(() => Math.random() - 0.5)
    const [selectedTile, setSelectedTile] = useState(null);
    const [grid, setGrid] = useState(Array(12).fill(null));

    console.log("TILES ",tilesList);
    console.log("MATCH TILES ", matchTiles);
    console.log("MATCH TILES WITHOUT SEA OR SPAWN ", matchTilesWithoutSeaOrSpawn);
    console.log("MATCH", match);

    const handleTileClick = (tile) => {
        setSelectedTile(tile);
    }

    const handleGridClick = (index) => {
        if (selectedTile) {
            const newGrid = [...grid];
            newGrid[index] = selectedTile;
            setGrid(newGrid);
            setSelectedTile(null);
        }
    }

    const getImage = (tileP) => {
        const realTile = tilesList[tileP.tile-1]
        switch (realTile.tipo) {
            case 'OSO':
                return bearTile;
            case 'AGUILA':
                return eagleTile;
            case 'GARZA':
                return heronTile;
            case 'SALTO':
                return jumpTile;
            case 'PIEDRA':
                return rockTile;
            case 'MAR':
                return seaTile;
            case 'DESOVE':
                return spawningTile;
            case 'AGUA':
                return waterTile;
            default:
                return null;
        }
    }   

    return(
        <div className='gamePage-container'>
            <h1 style={{
                position: 'absolute',
                marginBottom: '10px',
                marginTop: '10px',
                marginLeft: '552px',
                fontSize: '30px',
                }}>Game: {match.name}</h1>
            <div key={matchTilesWithoutSeaOrSpawn[0]}
                 style={{
                    cursor: 'pointer',
                    position: 'absolute',
                    bottom: '-900px',
                    right: '20px'
                  }}
                  onClick={() => handleTileClick(matchTilesWithoutSeaOrSpawn[0])}>
                    <h2>Next tile:</h2>
                    {<img 
                    onClick={() => handleTileClick(matchTilesWithoutSeaOrSpawn[0])}
                    src={getImage(matchTilesWithoutSeaOrSpawn[0])} alt='' style={{width: '150px'}}></img>}
            </div>
            <div className="game-container">
                {grid.map((tile, index) => (
                    <div key={index} className="grid-item" onClick={() => handleGridClick(index)}>
                        {tile ? <img src={''} alt="Grid Tile" style={{ width: '80px' }} /> : null}
                    </div>
                ))}
            </div>
        </div>
    )
}

export default Game;