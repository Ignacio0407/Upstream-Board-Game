import tokenService from "../services/token.service.ts";
import getDeleteAlertsOrModal from "./getDeleteAlertsOrModal.tsx";
import { deleteEntity} from "./fetchers.ts"
import { Alert } from "reactstrap";
/**
 * Function to delete an item from a list via a DELETE request and update the state accordingly.
 * This function also handles alerts and modals based on the server's response.
 *
 * @param {string} url - The URL to send the DELETE request to.
 * @param {string} id - The unique identifier of the item to be deleted.
 * @param {[Array, function]} state - An array containing the current state and the state setter function.
 * @param {[Array, function]} alerts - An array containing the current list of alerts and the state setter function for alerts.
 * @param {function} setMessage - A function to set the message for the modal in case of an error or success.
 * @param {function} setVisible - A function to control the visibility of the modal.
 * @param {object} [options={}] - Optional parameters:
 *   - `date` {Date}: If provided, only items created before this date will be deleted from the state.
 *   - `filtered` {Array}: An optional filtered list that needs to be updated in addition to the main state.
 *   - `setFiltered` {function}: A function to update the `filtered` state if applicable.
 *
 * @example
 * deleteFromList('/api/items/123', '123', [items, setItems], [alerts, setAlerts], setMessage, setVisible);
 */
export default function deleteFromList(url: string, id: number,
  [state, setState]: [any[], React.Dispatch<React.SetStateAction<any[]>>],
  [alerts, setAlerts]: [Alert[], React.Dispatch<React.SetStateAction<Alert[]>>],
  setMessage?: ((message: string) => void), setVisible?: ((visible: boolean) => void),
  options: {
    date?: string | number | Date;
    filtered?: any[];
    setFiltered?: React.Dispatch<React.SetStateAction<any[]>>;
  } = {}): void {
  const jwt = tokenService.getLocalAccessToken();
  const confirmMessage = window.confirm('Are you sure you want to delete it?');

  if (confirmMessage) {
    deleteEntity(url, jwt, id)
      .then((response) => {
        if (response.status === 200 || response.status === 204) {
          if (options.date) {
            const date = options.date;
            setState(state.filter((item) => item.id !== id && item.creationDate < date));
          }
          else if (options.filtered && options.setFiltered) {
            setState(state.filter((item) => item.id !== id));
            options.setFiltered(options.filtered.filter((item) => item.id !== id));
          } else {
            setState(state.filter((item) => item.id !== id));
          }
        }
        return response.text();
      })
      .then((text) => {
        if (text !== '') {
          try {
            const json = JSON.parse(text);
            getDeleteAlertsOrModal(json, id, alerts, setAlerts, setMessage, setVisible);
          } catch (e) {
            console.error('Failed to parse response:', e);
          }
        }
      })
      .catch((err) => {
        console.error(err);
        alert('Error deleting entity');
      });
  }
}