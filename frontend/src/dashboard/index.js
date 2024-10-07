import React, { useState, useEffect } from 'react';
import { Table, Button } from "reactstrap";
import useFetchState from "../util/useFetchState"; 
import tokenService from '../services/token.service'
import jwt_decode from "jwt-decode";
import '../static/css/dashboard/dashb.css'
import BotonLink from '../util/BotonLink';
   
export default function Dashboard() { 
    const [username, setUsername] = useState("");
    const jwt = tokenService.getLocalAccessToken();
import React, { useEffect } from 'react';
import '../App.css';
import '../static/css/dashboard/dashb.css';
import '../static/css/home/home.css'
import tokenService from '../services/token.service';
import BotonLink from '../util/BotonLink';

export default function Home() {
    const jwt = tokenService.getLocalAccessToken(); 

    useEffect(() => {
        // Any side effects based on jwt can be handled here
    }, [jwt]);

    return ( 
        <> 
        <div>
            <div className="prueba"> 
                <h1>Welcome {username}!</h1>
                <Button color="success" onClick={() => setNumberOfPlayers(numberOfPlayers+1)}>+</Button>
                <Button color="danger" onClick={() => setNumberOfPlayers(numberOfPlayers-1)}>-</Button>
                
            </div>
        </div>
    );
}
