import { NavigateFunction } from "react-router-dom";
import tokenService from "../services/token.service.ts";
import { deleteEntity } from "./fetchers.ts";
import { navigateToUnauthorized } from "../components/DoNotHackMyApp/DoNotHackMyAppHelper.ts";

interface DeleteSomeOptions<T> { confirmField?: keyof T }

export interface DeleteSomeParams<T> {readonly url: string, readonly items: any[], readonly requiredAuth: string,
  readonly checkAuth?: ((requiredAuth: string) => boolean) | boolean, readonly deactivateSecurity?: boolean, readonly state?: T[],
  readonly setState: React.Dispatch<React.SetStateAction<T[]>>, readonly navigate: NavigateFunction, 
  readonly options?: DeleteSomeOptions<T>;
}

export default function deleteSomeFromList<T extends { id: any }>({url, items, requiredAuth, checkAuth, 
  deactivateSecurity = false, setState, navigate, options = {}}: DeleteSomeParams<T>): void {

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