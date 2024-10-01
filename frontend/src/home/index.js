import React, { useState, useEffect, useFetchState }  from 'react';
import '../App.css';
import '../static/css/home/home.css'; 
import logo from '../static/images/logoCentralUPS.jpg'
import { Button } from "reactstrap"; 
import { Link } from "react-router-dom"; 
import jwt_decode from "jwt-decode";
import tokenService from '../services/token.service';


 
export default function Home(){
    let botonPlay= <></>

    const jwt = tokenService.getLocalAccessToken(); 
    useEffect(() => {

    }, [jwt])


    if(!jwt){
        botonPlay = (
            <>
            <Button color="success" > 
          
            <Link 
              to={"/login"} className="btn sm"                
              style={{ textDecoration: "none" }}>Play
            </Link> 
          </Button> 
          </>
        )
    }else{
        botonPlay = (
        <>
        <Button color="success" > 
      
        <Link 
          to={"/dashboard"} className="btn sm"                
          style={{ textDecoration: "none" }}>Play
        </Link> 
      </Button> 
      </>
        )
    }


    return(
        <div className="home-page-container">
            <div className="hero-div">
                <h1></h1>
                <img src={logo}/>
                <h1></h1>                
            {botonPlay}
            </div>
        </div> // Línea 12 es un espacio en la pantalla
    );
}