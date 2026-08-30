import { Link } from "react-router-dom";
import "../../static/css/auth/authButton.css";
import "../../static/css/auth/authPage.css";
import tokenService from "../../services/token.service.ts";

function sendLogoutRequest() {
  const jwt = window.localStorage.getItem("jwt");
  if (jwt || jwt === undefined) {
    tokenService.removeUser();
    window.location.href = "/";
  } else {
    alert("There is no user logged in");
  }
}

export default function Logout () {
  return (
    <div className="auth-page-container">
      <div className="auth-form-container">
        <h2 className="text-center text-md">
          Are you sure you want to log out?
        </h2>
        <div className="options-row">
          <Link className="auth-button" to="/" style={{textDecoration: "none"}}>
            No
          </Link>
          <button type="button" className="auth-button" onClick={() => sendLogoutRequest()}>
            Yes
          </button>
        </div>
      </div>
    </div>
  );
};