import { useEffect, useState }  from 'react';
import '../App.css';
import '../static/css/home/home.css'; 
import logo from '../static/images/logoCentralUPS.jpg'
import tokenService from '../services/token.service';
import BotonLink from '../util/BotonLink';
import { get } from '../util/fetchers'
 
export default function Home(){

    const jwt = tokenService.getLocalAccessToken();
    const [tile, setTile] = useState(null);

    useEffect(() => {
    async function fetchTile() {
      try {
        const response = await get("/api/v1/tiles/3", jwt);
        const data = await response.json();
        console.log("tile", data);
        setTile(data);
      } catch (error) {
        console.error("Error fetching tile:", error);
      }
    }

    if (jwt) {
      fetchTile();
    }
  }, [jwt]);

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
            {/*tile?.image && <img src={`/${tile.image}`} alt="Tile" />*/}
            </div>
        </div>
    );
}