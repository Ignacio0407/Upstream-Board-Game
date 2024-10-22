import { Table, Button } from "reactstrap";
import tokenService from "../services/token.service";
import useFetchState from "../util/useFetchState";
import { Link } from "react-router-dom";
import deleteFromList from "./../util/deleteFromList";
import { useState, useEffect } from "react";
import jwt_decode from "jwt-decode";
import getErrorModal from "./../util/getErrorModal";
import '../static/css/achievement/achievement.css'
const imgnotfound = "https://cdn-icons-png.flaticon.com/512/5778/5778223.png";

export default function AchievementList() {

    const jwt = tokenService.getLocalAccessToken();
    const [message, setMessage] = useState(null);
    const [visible, setVisible] = useState(false);
    const [alerts, setAlerts] = useState([]);
    const [achievements, setAchievements] = useFetchState([],`/api/v1/achievements`,jwt);
    const user = tokenService.getUser()
    const [finalUser,setUser] = useFetchState([],`/api/v1/users/${user.id}`,jwt)

  
    let roles = []
    if (jwt) {
      roles = getRolesFromJWT(jwt);
    }


  
    function getRolesFromJWT(jwt) {
      return jwt_decode(jwt).authorities;
    }

    const achievementList =
    achievements.map((a) => {
        return (
            <tr key={a.id}>
            <td className="text-center">{a.name}</td>
            <td className="text-center"> {a.description} </td>
            <td className="text-center">
            <img src={a.badgeImage ? a.badgeImage : imgnotfound } alt={a.name}
            width="50px"/>
            </td>
            <td className="text-center"> {a.threshold} </td>
            <td className="text-center"> {a.metric} </td>
            <td className="text-center">
            {roles[0] === "ADMIN" && <Button outline color="warning" >
                    <Link
                        to={`/achievements/`+a.id} className="btn sm"
                        style={{ textDecoration: "none" }}>Edit</Link>
                </Button> }
            </td>
            {roles[0] === "ADMIN" &&<Button outline color="danger"
                    onClick={() =>
                    deleteFromList(
                    `/api/v1/achievements/${a.id}`,
                    a.id,
                    [achievements, setAchievements],
                    [alerts, setAlerts],
                    setMessage,
                    setVisible)}>
                    Delete
                </Button> }
            </tr>
        );
    });
        const modal = getErrorModal(setVisible, visible, message);
            return (
            <body className="achievement-container">
                <div className="playercard-container" >
                        <Table>
                                <h2 className="title-playercard">
                                    {finalUser.username}
                                </h2>
                            <tr className="text-playercard">
                                Victorias totales: {finalUser.victorias}
                            </tr>
                            <tr className="text-playercard">
                                Partidas totales: {finalUser.partidasjugadas}
                            </tr>
                            <tr className="text-playercard">
                                Puntos totales: {finalUser.puntostotales}
                            </tr>

                        </Table>

                </div>
                <div className="achievement-card">
                <h1 className="text-center">Achievements</h1>
                    <div>
                    <Table aria-label="achievements" className="mt-4">
                    <thead>
                        <tr>
                        <th className="text-center">Name</th>
                        <th className="text-center">Description</th>
                        <th className="text-center">Image</th>
                        <th className="text-center">Threshold</th>
                        <th className="text-center">Metric</th>
                        {roles[0] === "ADMIN" &&<th className="text-center">Actions</th>}
                        </tr>
                    </thead>
                    <tbody>{achievementList}</tbody>

                    {roles[0] === "ADMIN" && <Button outline color="success" >
                        <Link
                            to={`/achievements/new`} className="btn sm"
                            style={{ textDecoration: "none" }}>Create achievement</Link>
                    </Button>}
                    </Table>
                    </div>
                </div>
            </body>
            );
}