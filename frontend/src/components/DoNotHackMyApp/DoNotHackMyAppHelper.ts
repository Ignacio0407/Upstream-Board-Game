import { NavigateFunction } from "react-router-dom";
import { BaseUser } from "../../interfaces/User";

function checkAuthDefault (required: string, actualUser: BaseUser) {
  if (actualUser.roles && actualUser.roles[0] !== "ADMIN") return required === actualUser.roles[0];
  else return true
  //else return required === actualUser.authority.authority;
}

export function navigateToUnauthorized(checkAuth: Function | undefined | boolean, actualUser: BaseUser | undefined, requiredAuth:string | undefined, navigate: NavigateFunction, onUnauthorizedRedirect: string = "/do-not-hack-my-app") {
  let userHasAuth = null;
  if (typeof checkAuth === "function" && requiredAuth) userHasAuth = checkAuth(requiredAuth);
  else if (requiredAuth && actualUser && checkAuth === undefined) userHasAuth = checkAuthDefault(requiredAuth, actualUser);
  else if (requiredAuth && checkAuth === true) userHasAuth = true;
  if (!userHasAuth) {
    navigate(onUnauthorizedRedirect);
    return null;
  }
}