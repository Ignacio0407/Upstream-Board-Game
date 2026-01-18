import { Link, useLocation, useNavigate } from 'react-router-dom';
import { put } from '../util/fetchers.ts';
import Achievement from '../interfaces/Achievement.ts';
import { EditFormGenerator } from '../components/Form/EditFormGenerator.tsx';
import { SubmitHandler } from 'react-hook-form';
import tokenService from '../services/token.service.ts';

const jwt = tokenService.getLocalAccessToken();

export default function AchievementEdit() {
  const { state } = useLocation();
  const achievement = state?.achievement as Achievement;
  const user = tokenService.getUser();

  const navigate = useNavigate();

  const onSubmit: SubmitHandler<Achievement> = async (data) => {
    put(`/api/v1/achievements/${achievement.id}`, jwt, data)
      .then((response) => response.json())
      .then((updated) => {
        navigate("/achievements", { state: { updated } });
      })
      .catch((error) => alert(error instanceof Error ? error.message : String(error)));
  };

  return (
    <div className="auth-page-container">
      <h2 className="text-center">Edit Achievement</h2>
      <div className="auth-form-container">
        <EditFormGenerator<Achievement> entityToEdit={achievement} onSubmit={onSubmit} submitText="Save" loadingText="Saving..."
          selectOptions={{ metric: ['GAMES_PLAYED', 'VICTORIES', 'TOTAL_PLAY_TIME', 'EXPLORER', 'TOTAL_POINTS'] }}
          actualUser={user} requiredAuth="ADMIN"
        />
      </div>
    </div>
  );
}