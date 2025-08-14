import { useState } from 'react';
import tokenService from '../services/token.service.ts';
import { Link, useNavigate } from 'react-router-dom';
import { Form, Input, Label } from 'reactstrap';
import getErrorModal from '../util/getErrorModal.tsx';
import getIdFromUrl from '../util/getIdFromUrl.ts';
import useFetchState from '../util/useFetchState.ts';
import { get } from '../util/fetchers.ts';
import Achievement, {emptyAchievement} from '../interfaces/Achievement.ts';

const jwt = tokenService.getLocalAccessToken();

export default function AchievementEdit() {
  const id:string = getIdFromUrl(2);

  const [message, setMessage] = useState<string | undefined>(undefined);
  const [visible, setVisible] = useState(false);
  const [achievement, setAchievement] = useFetchState<Achievement>(emptyAchievement, `/api/v1/achievements/${id}`, jwt, setMessage,
    setVisible, id);

  const modal = getErrorModal(setVisible, visible, message);
  const navigate = useNavigate();

  function handleSubmit(event: React.FormEvent) {
    event.preventDefault();

    get(`/api/v1/achievements${achievement.id ? '/' + achievement.id : ''}`, jwt)
      .then((response) => response.text())
      .then((data) => {
        if (data === '') {
          navigate('/achievements');
        } else {
          try {
            const json = JSON.parse(data);
            if (json.message) {
              setMessage(json.message);
              setVisible(true);
            } else {
              navigate('/achievements');
            }
          } catch (e) {
            navigate('/achievements');
          }
        }
      })
      .catch((error) => alert(error instanceof Error ? error.message : String(error)));
  }

  function handleChange(event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
    const target = event.target;
    const value = target.value;
    const name = target.name;
    setAchievement({ ...achievement, [name]: value });
  }

  return (
    <div className="auth-page-container">
      <h2 className="text-center">
        {achievement.id ? 'Edit Achievement' : 'Add Achievement'}
      </h2>
      <div className="auth-form-container">
        {modal}
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
              value={achievement.name || ''}
              onChange={handleChange}
              className="custom-input"
            />
          </div>
          <div className="custom-form-input">
            <Label for="description" className="custom-form-input-label">
              Description
            </Label>
            <Input
              type="text"
              required
              name="description"
              id="description"
              value={achievement.description || ''}
              onChange={handleChange}
              className="custom-input"
            />
          </div>
          <div className="custom-form-input">
            <Label for="badgeImage" className="custom-form-input-label">
              Badge Image Url:
            </Label>
            <Input
              type="text"
              required
              name="badgeImage"
              id="badgeImage"
              value={achievement.badgeImage || ''}
              onChange={handleChange}
              className="custom-input"
            />
          </div>
          <div className="custom-form-input">
            <Label for="metric" className="custom-form-input-label">
              Metric
            </Label>
            <Input
              type="select"
              required
              name="metric"
              id="metric"
              value={achievement.metric || ''}
              onChange={handleChange}
              className="custom-input"
            >
              <option value="">None</option>
              <option value="GAMES_PLAYED">GAMES_PLAYED</option>
              <option value="VICTORIES">VICTORIES</option>
              <option value="TOTAL_PLAY_TIME">TOTAL_PLAY_TIME</option>
            </Input>
          </div>
          <div className="custom-form-input">
            <Label for="threshold" className="custom-form-input-label">
              Threshold value:
            </Label>
            <Input
              type="number"
              required
              name="threshold"
              id="threshold"
              value={achievement.threshold || ''}
              onChange={handleChange}
              className="custom-input"
            />
          </div>
          <div className="custom-button-row">
            <button className="auth-button">Save</button>
            <Link to={`/achievements`} className="auth-button" style={{ textDecoration: 'none' }}>
              Cancel
            </Link>
          </div>
        </Form>
      </div>
    </div>
  );
}