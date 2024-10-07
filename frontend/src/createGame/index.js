import React, { useState, useEffect } from 'react';
import { Table, Button } from "reactstrap";
import useFetchState from "../util/useFetchState"; 
import tokenService from '../services/token.service'
import jwt_decode from "jwt-decode";
import '../static/css/createGame/createGame.css'
import BotonLink from '../util/BotonLink';
import { Link } from "react-router-dom";

export default function CreateGame() { 
    const [username, setUsername] = useState("");
    const jwt = tokenService.getLocalAccessToken();

    useEffect(() => {
        if (jwt) {
            setUsername(jwt_decode(jwt).sub);
        }
    }, [jwt])

    


    return ( 
        <> 
        <div className='createGame-page-container'>
            <h1 className='title-creategame'>
                Create a Game
            </h1>

        <div className='inputs-table'>
            <td className='td2'>
                Game's name: 
            </td>   
            <td><input type= 'text' className='input' placeholder={`Sala de ${username}`} ></input></td>
            <tr>
                <td className='td2'>
                Password: 
               
                </td>
                <td>
                <input className='input' type='password'></input>
                </td>
            </tr>
        </div>
        <div className='botonCreate'>
            
        <Button color={'success'}> 
        <Link 
          to={""} className="btn sm"                
          style={{ textDecoration: "none", color: "white", width: 300,height:50, fontSize:20, textAlign:'center'}}>
            {"Create"}
        </Link> 
        </Button>

        </div>
        </div> 
        </> 
); 



}