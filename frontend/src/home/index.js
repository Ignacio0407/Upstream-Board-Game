import React from 'react';
import '../App.css';
import '../static/css/home/home.css'; 
import logo from '../static/images/logoCentralUPS.jpg'
 
export default function Home(){
    return(
        <div className="home-page-container">
            <div className="hero-div">
                <h1></h1>
                <img src={logo}/>
                <h1></h1>                
            </div>
        </div> // Línea 12 es un espacio en la pantalla
    );
}