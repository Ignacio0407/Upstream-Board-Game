import { useNavigate } from 'react-router-dom';
import { SubmitHandler } from 'react-hook-form';
import tokenService from '../services/token.service.ts';
import { post } from '../util/fetchers.ts';
import { FormGenerator, FieldDef } from '../components/Form/FormGenerator.tsx';
import '../static/css/createGame/createGame.css';
import '../static/css/admin/adminPage.css';

interface CreateGameFormData {name: string, password: string}

export default function CreateGame() {
  const jwt = tokenService.getLocalAccessToken();
  const user = tokenService.getUser();
  const navigate = useNavigate();

  const fields: FieldDef[] = [
    {tag: 'Name', name: 'name', type: 'text', isRequired: true, 
        validators: {
        minLength: { value: 3, message: 'Name must be at least 3 characters' },
        maxLength: { value: 50, message: 'Name must be at most 50 characters' },
        }
    },
    {tag: 'Password', name: 'password', type: 'text', isRequired: false},
  ];

  const onSubmit: SubmitHandler<CreateGameFormData> = async (data) => {
    const { name, password } = data;

    try {
      const response = await post(`/api/v1/matches/matchCreator/${user.id}`, jwt, { name, password });

      if (!response.ok) {
        const errText = await response.text();
        throw new Error(errText || 'Unknown error');
      }
      const createdMatch = await response.json();

      navigate(`/matches/${createdMatch.id}/waiting`);
    } catch (error: any) {
      console.error('Error al crear la partida:', error);
      alert(`Error al crear la partida: ${error.message}`);
    }
  };

  return (
    <div className="createGame-page-container">
      <h2 className="title-creategame">Create Game</h2>

      <div className="form-container">
        <FormGenerator<CreateGameFormData>
          fields={fields}
          defaultValues={{ name: '', password: '' }}
          onSubmit={onSubmit}
          submitText="Create"
          loadingText="Creating..."
          className="create-game-form"
          cancelButtonActivated={true}
          navigateOnCancel='/dashboard'
          actualUser={user}
          requiredAuth='PLAYER'
        />
      </div>
    </div>
  );
}