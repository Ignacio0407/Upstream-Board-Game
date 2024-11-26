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
      password: "",
      matchCreator: null,
      state: "ESPERANDO",
      playersNum: 1,
      round: 0,
      phase: "CASILLAS",
      initialPlayer: null,
      actualPlayer: null,
  }
   const [match,setMatch] = useState(emptyMatch)    
   const navigate = useNavigate();

    useEffect(() => {
        if (jwt) {
            setUsername(jwt_decode(jwt).sub);
        }
        if (finalUser) {
        }
    }, [jwt, finalUser])

    let matchId;
    function handleSubmit(event) {
      event.preventDefault();
      fetch("/api/v1/matches", {
          method: "POST",
          headers: {
              Authorization: `Bearer ${jwt}`,
              Accept: "application/json",
              "Content-Type": "application/json",
          },
          body: JSON.stringify(match),
      })
      .then(response => {
          if (!response.ok) {
              // Lanza un error si el estado no es exitoso
              return response.text().then(err => {
                  throw new Error(`Error al crear la partida: ya existe una partida con nombre ${match.name}`);
              });
          }
          return response.text();
      })
      .then(data => {
          const matchCreada = JSON.parse(data);
          matchId = matchCreada.id;
          console.log(matchCreada);
          navigate(`/matches/${matchId}`);
      })
      .catch(error => {
          // Manejar errores aquí
          console.log("Error al crear la partida:", error);
          alert(`El nombre "${match.name}" no es válido`);
      });
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
              placeholder='Mínimo 3 caracteres, máximo 50 caracteres'
              id="name"
              value={match.name || ""}
              onChange={handleChange}
              className="input-table"
            />
            </div>
            
            <div className="custom-form-input">
            <Label for="password" className="custom-form-input-label">
              Password
            </Label>
            <Input
              type="text"
              name="password"
              id="password"
              value={match.password || ""}
              onChange={handleChange}
              className="input-table"
            />
          </div>
         

            <div className="custom-button-row">
            <button className="auth-button">Save</button>
            <Link
              to={`/dashboard`}
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