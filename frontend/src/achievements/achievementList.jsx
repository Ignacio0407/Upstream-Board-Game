import { Table, Button } from "reactstrap";
import tokenService from "../services/token.service";
import useFetchState from "../util/useFetchState";
import deleteFromList from "../util/deleteFromList";
import { useState } from "react";
import jwt_decode from "jwt-decode";
import getErrorModal from "../util/getErrorModal";
import '../static/css/achievement/achievement.css'
import BotonLink from "../util/BotonLink";
import SearchBar from "../util/SearchBar";
const imgnotfound = "https://cdn-icons-png.flaticon.com/512/5778/5778223.png";

export default function AchievementList() {

    const jwt = tokenService.getLocalAccessToken();
    const [message, setMessage] = useState(null);
    const [visible, setVisible] = useState(false);
    const [alerts, setAlerts] = useState([]);
    const [achievements, setAchievements] = useFetchState([],`/api/v1/achievements`,jwt);
    const [filtered, setFiltered] = useState([]);
    const user = tokenService.getUser()
    const [finalUser,setUser] = useFetchState([],`/api/v1/users/${user.id}`,jwt)
    const [userAchievements,setUserAchievements] = useFetchState([],`/api/v1/users/${user.id}/achievements`,jwt)
  
    let roles = []
    if (jwt) {
      roles = getRolesFromJWT(jwt);
    }
  
    function getRolesFromJWT(jwt) {
      return jwt_decode(jwt).authorities;
    }

    function isAchievedByUser(achievementId) {
        return userAchievements.some(userAchievement => userAchievement.id === achievementId);
    }

    function achievementList(achievementsToList) {
    return achievementsToList.map((a) => {
        return (
            <tr key={a.id} className="table-row">
            <td className={isAchievedByUser(a.id) ? 'achieved' : 'text-center table-cell'}>{a.name}</td>
            <td className={isAchievedByUser(a.id) ? 'achieved' : 'text-center table-cell'}> {a.description} </td>
            <td className={isAchievedByUser(a.id) ? 'achieved' : 'text-center table-cell'}>
            <img src={a.badgeImage ? a.badgeImage : imgnotfound } alt={a.name}
            width="50px"/>
            </td>
            <td className={isAchievedByUser(a.id) ? 'achieved' : 'text-center table-cell'}> {a.threshold} </td>
            <td className={isAchievedByUser(a.id) ? 'achieved' : 'text-center table-cell'}> {a.metric} </td>
            <td className="table-cell">
            {roles[0] === "ADMIN" && 
                <BotonLink outline={true} color={"warning"} direction={'/achievements/'+a.id} text={"Edit"}/>}
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
    }

            return (
            <body className="achievement-container">
                <div className="playercard-container" >
                        <Table>
                                <h2 className="title-playercard">
                                    {finalUser.username}
                                </h2>
                            <tr className="text-playercard">
                                Total wins: {finalUser.victories}
                            </tr>
                            <tr className="text-playercard">
                                Total games: {finalUser.playedgames}
                            </tr>
                            <tr className="text-playercard">
                                Total points: {finalUser.totalpoints}
                            </tr>

                        </Table>

                </div>
                <SearchBar data={achievements} setFiltered={setFiltered} placeholder={"Search achievements"} />
                <div className="achievement-card">
                <h1 className="text-center">Achievements</h1>
                <h6 className="text-center">You will see a green Achievement if you have completed it!</h6>
                {<h1></h1>/*Espacio en blanco*/} 
                    <div>
                    
                    <thead>
                        <tr>
                        <th className="text-center table-row">Name</th>
                        <th className="text-center table-row">Description</th>
                        <th className="text-center table-row">Image</th>
                        <th className="text-center table-row">Threshold</th>
                        <th className="text-center table-row">Metric</th>
                        {roles[0] === "ADMIN" && <th className="text-center table-row">Actions</th>}
                        </tr>
                    </thead>
                    <tbody>{filtered ? achievementList(filtered) : achievementList(achievements)}</tbody>

                    {roles[0] === "ADMIN" && 
                    <BotonLink outline={true} color={"success"} direction={'/achievements/new'} text={"Create achievement"}/>}

                    </div>
                </div>
            </body>
            );
}