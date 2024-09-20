import React from 'react';
import '../App.css';
import '../static/css/home/home.css'; 
import logo from '../static/images/Upstream_Logo.jpg'

export default function Home(){
    return(
        <div className="home-page-container">
            <div className="hero-div">
                <h1>Upstream</h1>
                <h3>---</h3><img src={logo}/>
                <h3>Do you want to play?</h3>                
            </div>
        </div>
    );
}