import React, { useState, useEffect } from 'react';
import { Form, Input, Label } from "reactstrap"; 
import tokenService from '../services/token.service'
import jwt_decode from "jwt-decode";
import { Link } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import useFetchState from '../util/useFetchState';
import '../static/css/createGame/createGame.css';
import '../static/css/admin/adminPage.css';

export default function CreateGame() { 
    const [username, setUsername] = useState("");
    const jwt = tokenService.getLocalAccessToken();
    const user = tokenService.getUser()
    const [finalUser, setUser] = useFetchState([], `/api/v1/users/${user.id}`, jwt)
    const [error, setError] = useState(null);
    
    const emptyMatch = {
        name: "",
        contrasena: "",
        creadorpartida: null,
        estado: "ESPERANDO",
        numjugadores: 1,
        ronda: 0,
        fase: "CASILLAS",
        jugador_inicial: null,
        jugador_actual: null,
    }
    
    const [match, setMatch] = useState(emptyMatch)    
    const navigate = useNavigate();

    useEffect(() => {
        if (jwt) {
            setUsername(jwt_decode(jwt).sub);
        }
        if (finalUser && finalUser.id) {
            setMatch(prevMatch => ({...prevMatch, creadorpartida: finalUser.id}))
        }
    }, [jwt, finalUser])

    async function handleSubmit(event) {
        event.preventDefault();
        setError(null);
        
        try {
            const response = await fetch("/api/v1/matches", {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${jwt}`,
                    'Accept': 'application/json',
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(match),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Error al crear la partida');
            }

            const matchCreada = await response.json();
            navigate(`/matches/${matchCreada.id}`);
        } catch (error) {
            console.error("Error:", error);
            setError(error.message);
        }
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
                {error && (
                    <div className="alert alert-danger" role="alert">
                        {error}
                    </div>
                )}
                
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