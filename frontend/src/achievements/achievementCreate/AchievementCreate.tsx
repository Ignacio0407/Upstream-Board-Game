import { useNavigate } from 'react-router-dom';
import { post } from '../../util/fetchers.ts';
import Achievement, { emptyAchievement, Metric } from '../../interfaces/Achievement.ts';
import { EditFormGenerator } from '../../components/Form/EditFormGenerator.tsx';
import { SubmitHandler } from 'react-hook-form';
import tokenService from '../../services/token.service.ts';
import { Authority } from '../../interfaces/User.ts';

export default function AchievementCreate() {
  const jwt = tokenService.getLocalAccessToken();
  const achievement = emptyAchievement;
  const user = tokenService.getUser();

  const navigate = useNavigate();

  const onSubmit: SubmitHandler<Achievement> = async (data) => {
    post(`/api/v1/achievements`, jwt, data).then((response) => response.json())
      .then((updated) => {
        navigate("/achievements", { state: { updated } });
      })
      .catch((error) => alert(error instanceof Error ? error.message : String(error)));
  };

  return (
    <div className="auth-page-container">
      <h2 className="text-center">Create Achievement</h2>
      <div className="auth-form-container">
        <EditFormGenerator<Achievement> entityToEdit={achievement} onSubmit={onSubmit} submitText="Save" loadingText="Saving..."
          selectOptions={{ metric: Object.values(Metric) }}
          actualUser={user} requiredAuth={Authority.ADMIN}
        />
      </div>
    </div>
  );
}
