import { useEffect }  from 'react';
import '../App.css';
import '../static/css/home/home.css'; 
import tokenService from '../services/token.service.ts';
import BotonLink from '../util/BotonLink.tsx';
 
export default function Home(){
    const jwt = tokenService.getLocalAccessToken(); 
    useEffect(() => {
    }, [jwt])

    return(
        <div className="home-page-container">
            <div className="pantallaInicio">
                <img  className="imagenInicio"  src={'/images/logoCentralUPS.jpg'}/>
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