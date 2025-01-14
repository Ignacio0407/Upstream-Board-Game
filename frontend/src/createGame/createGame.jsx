import React, { useState, useEffect } from 'react';
import { Form, Input, Label, Button } from "reactstrap"; 
import tokenService from '../services/token.service'
import jwt_decode from "jwt-decode";
// import '../static/css/createGame/createGame.css'
import { Link } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import useFetchState from '../util/useFetchState';
import '../static/css/createGame/createGame.css'
import '../static/css/admin/adminPage.css'
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export default function CreateGame() { 
    const [name, setName] = useState("");
    const [password, setPassword] = useState("");
    const jwt = tokenService.getLocalAccessToken();
    const user = tokenService.getUser()
    const [finalUser,setUser] = useFetchState([],`/api/v1/users/${user.id}`,jwt)
     
   const navigate = useNavigate();

   const socket = new SockJS('http://localhost:8080/ws-upstream');
   const stompClient = new Client({
    webSocketFactory: () => socket,
    debug: (str) => {
        //console.log(str);
    },
    connectHeaders: {
        Authorization: `Bearer ${jwt}`
    },
    onConnect: (frame) => {
        console.log('Connected: ' + frame);
    },
    onStompError: (frame) => {
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
    },
    onWebSocketError: (error) => {
        console.error('Error with websocket', error);
    }
    });

    stompClient.activate();

    function handleSubmit(event) {
      event.preventDefault();

      fetch(`/api/v1/matches/matchCreator/${finalUser.id}`, {
          method: "POST",
          headers: {
              Authorization: `Bearer ${jwt}`,
              Accept: "application/json",
              "Content-Type": "application/json",
          },
          body: JSON.stringify({ name, password }), // Solo los valores que se pueden modificar
      })
      .then(response => {
          if (!response.ok) {
              return response.text().then(err => {
                  throw new Error(`Nombre no válido.`);
              });
          }
          return response.json();
      })
      .then(matchCreada => {
          console.log("Match creado:", matchCreada);

          try {
              stompClient.publish({
                  destination: "/app/dash",
                  body: JSON.stringify({ action: "colorChanged", matchId: matchCreada.id }),
              });
          } catch (e) {
              console.log("Error al publicar en WebSocket:", e);
          }

          navigate(`/matches/${matchCreada.id}`);
      })
      .catch(error => {
          console.error("Error al crear la partida:", error);
          alert(`Error al crear la partida: ${error.message}`);
      });
  }

  return (
    <div className='createGame-page-container'>
        <h2 className="title-creategame">
            Create Game
        </h2>

        <div className="form-container">
            <Form onSubmit={handleSubmit}>
                <div className="">
                    <Label for="name" className="label-input">
                        Name
                    </Label>
                    <Input
                        type="text"
                        required
                        name="name"
                        placeholder='Mínimo 3 caracteres, máximo 50 caracteres'
                        id="name"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        className="input-table"
                    />
                </div>

                <div className="password-container">
                    <Label for="password" className="label-input">
                        Password
                    </Label>
                    <Input
                        type="text"
                        name="password"
                        id="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className="input-table"
                    />
                </div>

                <div className="custom-button-row">
                <Button color='success' onClick={handleSubmit} className='button-create'>
                         Crear
                </Button>
                <Button color='danger' onClick={() => navigate('/dashboard')} className='button-cancel'>
                         Cancelar
                </Button>
                </div>
            </Form>
        </div>
    </div>
);

}