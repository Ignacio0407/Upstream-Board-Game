import { useState, useEffect } from "react";
import { Alert, Button } from "reactstrap";
import tokenService from "../../services/token.service.ts";
import useFetchState from "../../util/useFetchState.ts";
import deleteFromList from "../../util/deleteFromList.ts";
import "./achievement.css";
import ListGenerator from "../../components/ListGenerator/ListGenerator.tsx";
import Achievement from "../../interfaces/Achievement.ts";
import User, { Authority } from "../../interfaces/User.ts";
import { buildSafeUrl, get } from "../../util/fetchers.ts";
import ButtonLink from "../../components/ButtonLink/ButtonLink.tsx";
import { useNavigate } from "react-router-dom";
import deleteSomeFromList from "../../util/deleteSomeFromList.ts";

const imgnotfound = "/images/achievements/notFoundImage.png";

function renderEditButton(a: Achievement) {
  return <ButtonLink outline={false} color="warning" direction={`/achievements/${a.id}`} state={{ achievement: a }} text="Edit" />;
}

export default function AchievementList() {

  const jwt = tokenService.getLocalAccessToken();
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [achievements, setAchievements] = useState<Achievement[]>([])
  useEffect(() => {get(`/api/v1/achievements`, jwt).then(res => res.json()).then(data => setAchievements(data))}, [jwt]);
  const user = tokenService.getUser();
  const [finalUser] = useFetchState<User>({} as User, buildSafeUrl('/api/v1/users/', user.id), jwt);
  const [userAchievements] = useFetchState<Achievement[]>([], `/api/v1/achievements/user/${encodeURIComponent(String(user.id))}`, jwt);
  const navigate = useNavigate();
  const isAdmin = user?.roles?.[0] === Authority.ADMIN;

  function renderAchievement(a: Achievement) {
    const achievedClass = userAchievements.some(ua => ua.id === a.id) ? "achieved" : "text-center table-cell";
    return (
      <>
        <td className={achievedClass}>{a.name}</td>
        <td className={achievedClass}>{a.description}</td>
        <td className={achievedClass}>
          <img src={a.badgeImage ? a.badgeImage : imgnotfound} alt={a.name} width="50px" />
        </td>
        <td className={achievedClass}>{a.threshold}</td>
        <td className={achievedClass}>{a.metric}</td>
      </>
    );
  }

  return (
    <div className="achievement-container">

      <div className="playercard-container">
      <h2 className="title-playercard">{finalUser.username}</h2>
        <div className="playercard-stats">
          <div className="playercard-row"> Total wins: {finalUser.victories} </div>
          <div className="playercard-row"> Total games: {finalUser.playedgames} </div>
          <div className="playercard-row"> Total points: {finalUser.totalpoints} </div>
        </div>
      </div>

      <div className="achievement-card">
        <h1 className="text-center">Achievements</h1>
        <h6 className="text-center">You will see a green Achievement if you have completed it!</h6>

        <ListGenerator<Achievement>
          data={achievements}
          actualUser={{ id: user.id, username: user.username, roles: user.roles }}
          columns={["name","description","badgeImage","threshold","metric"]}
          idKey="id"
          customRender={renderAchievement}
          requiredAuth={Authority.PLAYER}
          createButton={isAdmin ? "/achievements/new" : undefined}
          createButtonPosition="bottom"
          editCondition={isAdmin}
          editButton={renderEditButton}
          deleteCondition={isAdmin}
          deleteButton={(a) => deleteFromList(`/api/v1/achievements`, a.id, [achievements, setAchievements], [alerts, setAlerts])}
          customLabels={{ badgeImage: "Badge Image" }}
          massEditButton={<Button outline={false} color="warning" style={{ color: "#fff" }}>Edit</Button>}
          massEditUri="/api/v1/achievements/some"
          massDeleteButton={(items: Achievement[]) => deleteSomeFromList({url: "/api/v1/achievements", items,
            requiredAuth: Authority.ADMIN, state: achievements, setState: setAchievements, navigate, 
            options: { confirmField: "name" }
          })}
          searchBarPlaceholder={"Search achievements"}
          massRequiredAuth={Authority.ADMIN}
        />
      </div>
    </div>
  );
}