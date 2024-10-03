import React, { useEffect }  from 'react';
import '../App.css';
import '../static/css/home/home.css'; 
import logo from '../static/images/logoCentralUPS.jpg'
import tokenService from '../services/token.service';
import BotonLink from '../util/BotonLink';
 
export default function Home(){

    const jwt = tokenService.getLocalAccessToken(); 
    useEffect(() => {

    }, [jwt])

    return(
        <div className="home-page-container">
            <div className="pantallaInicio">
                <img  className="imagenInicio"  src={logo}/>
                {!jwt ? <BotonLink color={"info"} direction={"/login"} text={"Play"} /> : 
                <BotonLink color={"info"} direction={"/dashboard"} text={"Play"} />}
            </div>
        </div> // Línea 18 y 20 son un espacio en la pantalla
    );
}