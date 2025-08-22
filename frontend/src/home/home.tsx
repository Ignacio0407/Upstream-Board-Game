import { useEffect }  from 'react';
import '../App.css';
import './home.css'; 
import tokenService from '../services/token.service.ts';
import ButtonLink from '../components/ButtonLink/ButtonLink.tsx';
 
export default function Home(){
    const jwt = tokenService.getLocalAccessToken(); 
    useEffect(() => {
    }, [jwt])

    return(
        <div className="home-page-container">
            <div className="pantallaInicio">
                <img  className="imagenInicio"  src={'/images/logoCentralUPS.jpg'}/>
                <div className='botonesPantallaInicio'>
                {!jwt ? <ButtonLink color={"success"} direction={"/login"} text={"Game List"} /> : 
                <ButtonLink color={"success"} direction={"/dashboard"} text={"Game List"} />}
                {!jwt ? <ButtonLink color={"success"} direction={"/login"} text={"Create Game"} /> : 
                <ButtonLink color={"success"} direction={"/creategame/"} text={"Create Game"} />}
                </div>
            </div>
        </div>
    );
}