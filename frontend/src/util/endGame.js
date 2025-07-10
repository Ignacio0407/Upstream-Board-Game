const endGame = async ({ match, players, user, jwt, spectatorIds, navigate, numjug, setMatches }) => {
    
    if (spectatorIds.find((p) => p === user.id)) {
        navigate("/dashboard");
        return;
    }

    let numJugadores = numjug - 1;

    if (match.matchCreator === user.id) {
        numJugadores = 0;
    }

    const playerId = players.find((p) => p.userPlayer === user.id)?.id;

    if (!playerId) {
        console.error("Player ID no encontrado.");
        return;
    }

    match.playersNum = numJugadores;

    if (numJugadores === 0) {
        match.state = "FINALIZED";
    } else {
        match.state = "WATIING";
    }

    try {
        await fetch(`/api/v1/players/${playerId}`, {
            method: "DELETE",
            headers: {
                Authorization: `Bearer ${jwt}`,
                Accept: "application/json",
                "Content-Type": "application/json",
            },
        });

        await fetch(`/api/v1/matches/${match.id}`, {
            method: "PUT",
            headers: {
                Authorization: `Bearer ${jwt}`,
                Accept: "application/json",
                "Content-Type": "application/json",
            },
            body: JSON.stringify(match),
        });

        setMatches(match);
        navigate("/dashboard");
    } catch (error) {
        console.error("Error al salir de la partida:", error);
    }
};

export default endGame;
