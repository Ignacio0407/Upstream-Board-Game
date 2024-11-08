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
                <div className='botonesPantallaInicio'>
                {!jwt ? <BotonLink color={"success"} direction={"/login"} text={"Game List"} /> : 
                <BotonLink color={"success"} direction={"/dashboard"} text={"Game List"} />}
                {!jwt ? <BotonLink color={"success"} direction={"/login"} text={"Create Game"} /> : 
                <BotonLink color={"success"} direction={"/creategame/"} text={"Create Game"} />}
                </div>
            </div>
        </div>
    );
}