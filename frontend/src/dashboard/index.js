import React, { useState, useEffect } from 'react';
import { Table, Button } from "reactstrap";
import useFetchState from "../util/useFetchState"; 
import tokenService from '../services/token.service'
import jwt_decode from "jwt-decode";
import '../static/css/dashboard/dashb.css'
   
export default function Dashboard() { 
    const [username, setUsername] = useState("");
    const jwt = tokenService.getLocalAccessToken();
    const [numberOfPlayers, setNumberOfPlayers] = useState(2);
    const [nombrePartida, setNombrePartida] = useState("");
    const [crearPartida, setCrearPartida] = useState(2)

    useEffect(() => {
        if (jwt) {
            setUsername(jwt_decode(jwt).sub);
        }
    }, [jwt])

    return ( 
        <> 
        <div>
            <div className="prueba"> 
                <h1>Welcome {username}!</h1>
                <Button color="success" onClick={() => setNumberOfPlayers(numberOfPlayers+1)}>+</Button>
                <Button color="danger" onClick={() => setNumberOfPlayers(numberOfPlayers-1)}>-</Button>
                
            </div>
        </div> 
        </> 
); 
} 