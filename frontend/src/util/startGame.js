const startGame = async ({ matchId, jwt, stompClient, finalUser, setLoading }) => {
    setLoading(true);
    
    const requests = async () => {
        await fetch(`/api/v1/matches/${matchId}/startGame`, {
            method: "PATCH",
            headers: {
                Authorization: `Bearer ${jwt}`,
                Accept: "application/json",
                "Content-Type": "application/json",
            },
        });
        await fetch(`/api/v1/matchTiles/createMatchTiles/${matchId}`, {
            method: "POST",
            headers: {
                Authorization: `Bearer ${jwt}`,
                Accept: "application/json",
                "Content-Type": "application/json",
            },
        });
    };

    try {
        await requests();
        stompClient.publish({
            destination: "/app/start",
            body: JSON.stringify({ action: "colorChanged", userId: finalUser.id }),
        });
        console.log("Todas las MatchTiles han sido creadas en orden aleatorio.");
    } catch (error) {
        console.error("Error al crear algunas MatchTiles:", error);
    } finally {
        setLoading(false);
    }
    
    window.location.reload(true);
};

export default startGame;
