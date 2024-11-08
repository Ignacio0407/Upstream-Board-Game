import React, { useState, useEffect } from 'react';
import { Form, Input, Label } from "reactstrap"; 
import tokenService from '../services/token.service'
import jwt_decode from "jwt-decode";
// import '../static/css/createGame/createGame.css'
import { Link } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import useFetchState from '../util/useFetchState';
import '../static/css/createGame/createGame.css'
import '../static/css/admin/adminPage.css'

export default function CreateGame() { 
    
    const [username, setUsername] = useState("");
    const jwt = tokenService.getLocalAccessToken();
    const user = tokenService.getUser()
    const [finalUser,setUser] = useFetchState([],`/api/v1/users/${user.id}`,jwt)
    const emptyMatch = {
        name: "",
        contrasena: "",
        estado: "ESPERANDO",
        numjugadores: 1,
        ronda: 0,
        fase: "CASILLAS",
        jugador_inicial: 1,
        jugador_actual: 1,
    }
   const [match,setMatch] = useState(emptyMatch)    
   const navigate = useNavigate();

    useEffect(() => {
        if (jwt) {
            setUsername(jwt_decode(jwt).sub);
        }
    }, [jwt])

    let matchId;
    function handleSubmit(event){
        event.preventDefault();
        console.log(match);
        fetch("/api/v1/matches", {

            method: "POST",
            headers: {
                Authorization: `Bearer ${jwt}`,
                Accept: "application/json",
                "Content-Type": "application/json",
              },
              body: JSON.stringify(match),
        }).then((response) => response.text())
        .then(
        data => {
          const matchCreada = JSON.parse(data)
          matchId = matchCreada.id;
          navigate(`/matches/${matchId}`)
      })
      .catch(error => console.error("Error:", error));
  }
        

function handleChange(event) {
    const target = event.target;
    const value = target.value;
    const name = target.name;
    setMatch({ ...match, [name]: value });
  }

    return(
        <div className='createGame-page-container'>
            <h2 className="title-creategame">
                Create Game
            </h2>

            <div className="auth-form-container">
            <Form onSubmit={handleSubmit}>
            <div className="custom-form-input">
            <Label for="name" className="custom-form-input-label">
              Name
            </Label>

            <Input
              type="text"
              required
              name="name"
              id="name"
              value={match.name || ""}
              onChange={handleChange}
              className="input-table"
            />
            </div>
            
            <div className="custom-form-input">
            <Label for="contrasena" className="custom-form-input-label">
              Contraseña
            </Label>
            <Input
              type="text"
              required
              name="contrasena"
              id="contrasena"
              value={match.contrasena || ""}
              onChange={handleChange}
              className="input-table"
            />
          </div>
         

            <div className="custom-button-row">
            <button className="auth-button">Save</button>
            <Link
              to={`/achievements`}
              className="auth-button"
              style={{ textDecoration: "none" }}
            >
              Cancel
            </Link>

          
          </div>
          </Form>
          </div>
        </div>
    )

}