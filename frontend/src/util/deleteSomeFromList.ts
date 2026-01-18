import { NavigateFunction } from "react-router-dom";
import tokenService from "../services/token.service.ts";
import { deleteEntity } from "./fetchers.ts";
import { navigateToUnauthorized } from "../components/DoNotHackMyApp/DoNotHackMyAppHelper.ts";

interface DeleteSomeOptions<T> {confirmField?: keyof T}

export default function deleteSomeFromList<T extends { id: any }>(url: string, items:any[], requiredAuth: string,
  checkAuth: Function | undefined, deactivateSecurity: boolean = false, state: T[],
  setState: React.Dispatch<React.SetStateAction<T[]>>, navigate: NavigateFunction, options: DeleteSomeOptions<T> = {} ): void {

  const user = tokenService.getUser();

  if (!deactivateSecurity) {
    navigateToUnauthorized(checkAuth, user, requiredAuth, navigate);
  }

  const confirmField = options.confirmField ?? ("name" as keyof T);
  const labelList = items.map(item => String(item[confirmField])).join(", ");

  const confirmed = window.confirm(`Are you sure you want to delete the following items?\n\n${labelList}`);

  if (!confirmed) return;

  const ids = items.map(i => i.id);
  const jwt = tokenService.getLocalAccessToken();

  Promise.all(ids.map(id => deleteEntity(url, jwt, id)))
    .then(() => {
      setState(prev => prev.filter(item => !ids.includes(item.id)));
    })
    .catch(() => alert("Error deleting selected items"));
}