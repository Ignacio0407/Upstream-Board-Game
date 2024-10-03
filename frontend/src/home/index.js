import React, { useEffect }  from 'react';
import '../App.css';
import '../static/css/home/home.css'; 
import logo from '../static/images/logoCentralUPS.jpg'
import tokenService from '../services/token.service';
import botonPlay from '../util/botonplay';
 
export default function Home(){

    const jwt = tokenService.getLocalAccessToken(); 
    useEffect(() => {

    }, [jwt])

    return(
        <div className="home-page-container">
            <div className="hero-div">
                <h1></h1>
                <img src={logo}/>
                <h1></h1>                
                {!jwt ? botonPlay("success", "/login", 'Play') : botonPlay("success", "/dashboard", 'Play')}
            </div>
        </div> // Línea 18 y 20 son un espacio en la pantalla
    );
}