import { useState } from 'react';
import { Form, Input, Label, Button } from "reactstrap"; 
import tokenService from '../services/token.service.ts'
import { useNavigate } from "react-router-dom";
import useFetchState from '../util/useFetchState.ts';
import '../static/css/createGame/createGame.css'
import '../static/css/admin/adminPage.css'
import { post, createWebSocket, createStompClient } from '../util/fetchers.ts'
import User, { emptyUser } from '../interfaces/User.ts'

export default function CreateGame() { 
    const [name, setName] = useState<string>("");
    const [password, setPassword] = useState<string>("");
    const jwt = tokenService.getLocalAccessToken();
    const user = tokenService.getUser()
    const [finalUser, setUser] = useFetchState<User>(emptyUser,`/api/v1/users/${user.id}`,jwt)
    const navigate = useNavigate();

    const socket = createWebSocket();
    const stompClient = createStompClient(socket, jwt);
    stompClient.activate();

    async function handleSubmit(event) {
      event.preventDefault();

      await post(`/api/v1/matches/matchCreator/${finalUser.id}`, jwt, { name, password })
      .then(response => {
          if (!response.ok) {
              return response.text().then(err => {
                  throw new Error(err);
              });
          }
          return response.json();
      })
      .then(createdMatch => {
          console.log("Match creado:", createdMatch);

          try {
              stompClient.publish({
                  destination: "/app/dash",
                  body: JSON.stringify({ action: "colorChanged", matchId: createdMatch.id }),
              });
          } catch (e) {
              console.log("Error al publicar en WebSocket:", e);
          }

          navigate(`/matches/${createdMatch.id}`);
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