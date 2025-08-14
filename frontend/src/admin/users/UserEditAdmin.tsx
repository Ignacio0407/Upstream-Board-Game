import { useState } from "react";
import { Link } from "react-router-dom";
import { Form, Input, Label } from "reactstrap";
import tokenService from "../../services/token.service.ts";
import "../../static/css/admin/adminPage.css";
import getErrorModal from "../../util/getErrorModal.tsx";
import getIdFromUrl from "../../util/getIdFromUrl.ts";
import useFetchData from "../../util/useFetchData.ts";
import useFetchState from "../../util/useFetchState.ts";
import { put, post} from '../../util/fetchers.ts'
import {UserList, emptyUserList} from '../../interfaces/User.ts'
import Authority, {emptyAuthority} from '../../interfaces/Authority.ts'

const jwt = tokenService.getLocalAccessToken();

export default function UserEditAdmin() {
  const id = getIdFromUrl(2);
  const [message, setMessage] = useState<string>("");
  const [visible, setVisible] = useState<boolean>(false);
  const [user, setUser] = useFetchState<UserList>(emptyUserList, `/api/v1/users/${id}`, jwt, setMessage, setVisible, id);
  const auths = useFetchData<Authority[]>(`/api/v1/users/authorities`, jwt);

  function handleChange(event: React.ChangeEvent<HTMLInputElement>) {
    const target = event.target;
    const value = target.value;
    const name = target.name;
    if (name === "authority") {
      const auth = auths.find((a) => a.id === Number(value));
      if (auth)
        setUser({ ...user, authority: auth });
    } else setUser({ ...user, [name]: value });
  }

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    user.id ? 
    put("/api/v1/users" + (user.id ? "/" + user.id : ""), jwt, JSON.stringify(user)) : 
    post("/api/v1/users" + (user.id ? "/" + user.id : ""), jwt, JSON.stringify(user))
      .then((response) => response.json())
      .then((json) => {
        if (json.message) {
          setMessage(json.message);
          setVisible(true);
        } else window.location.href = "/users";
      })
      .catch((message) => alert(message));
  }

  const modal = getErrorModal(setVisible, visible, message);
  const authOptions = auths.map((auth) => (
    <option key={auth.id} value={auth.id}>
      {auth.authority}
    </option>
  ));

  return (
    <div className="auth-page-container">
      {<h2>{user.id ? "Edit User" : "Add User"}</h2>}
      {modal}
      <div className="auth-form-container">
        <Form onSubmit={handleSubmit}>
          <div className="custom-form-input">
            <Label for="username" className="custom-form-input-label">
              Username
            </Label>
            <Input
              type="text"
              required
              name="username"
              id="username"
              value={user.username || ""}
              onChange={handleChange}
              className="custom-input"
            />
          </div>
          <div className="custom-form-input">
            <Label for="lastName" className="custom-form-input-label">
              Password
            </Label>
            <Input
              type="password"
              required
              name="password"
              id="password"
              value={user.password || ""}
              onChange={handleChange}
              className="custom-input"
            />
          </div>
          <Label for="authority" className="custom-form-input-label">
            Authority
          </Label>
          <div className="custom-form-input">
            {user.id ? (
              <Input
                type="select"
                disabled
                name="authority"
                id="authority"
                value={user.authority?.id || ""}
                onChange={handleChange}
                className="custom-input"
              >
                <option value="">None</option>
                {authOptions}
              </Input>
            ) : (
              <Input
                type="select"
                required
                name="authority"
                id="authority"
                value={user.authority?.id || ""}
                onChange={handleChange}
                className="custom-input"
              >
                <option value="">None</option>
                {authOptions}
              </Input>
            )}
          </div>
          <div className="custom-button-row">
            <button className="auth-button">Save</button>
            <Link
              to={`/users`}
              className="auth-button"
              style={{ textDecoration: "none" }}
            >
              Cancel
            </Link>
          </div>
        </Form>
      </div>
    </div>
  );
}
