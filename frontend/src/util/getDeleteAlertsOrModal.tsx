import { Dispatch, SetStateAction } from "react";
import { Alert } from "reactstrap";

/**
 * Helper function to dismiss an alert by its ID.
 * @param {Array} alerts - The current list of alerts being displayed.
 * @param {string} id - The unique identifier of the alert to be dismissed.
 * @param {React.Dispatch<React.SetStateAction<Alert[]>>} setAlerts - A function to update the list of alerts by removing the dismissed alert.
 */
function dismiss(alerts: any[], id:string, setAlerts:React.Dispatch<React.SetStateAction<Alert[]>>) {
    setAlerts(alerts.filter(a => a.id !== id))
}

/**
 * Function to handle the display of alerts or modals based on a server response.
 * If the response contains a status code, it displays a modal with a message.
 * Otherwise, it adds a dismissible alert to the list of alerts.
 *
 * @param {object} json - The JSON response object from the server. It may contain a status code and message.
 * @param {number} id - A unique identifier for the alert.
 * @param {Array} alerts - The current list of alerts being displayed.
 * @param {Function} setAlerts - A function to update the list of alerts.
 * @param {((message: string) => void) | undefined} setMessage - A function to set the message for the modal.
 * @param {((visible: boolean) => void) | undefined} setVisible - A function to control the visibility of the modal.
 *
 * @example
 * getDeleteAlertsOrModal(responseJson, "123", currentAlerts, setCurrentAlerts, setModalMessage, setModalVisible);
 */

export default function getDeleteAlertsOrModal(json: { statusCode: any; message: any }, id:number, alerts: any[], setAlerts: Dispatch<SetStateAction<Alert[]>>, setMessage: ((message:string) => void) | undefined, setVisible: ((visible: boolean) => void) | undefined) {
    if (json.statusCode) {
        if (setMessage) setMessage(json.message);
        if (setVisible)setVisible(true);
    }
    else {
        const alertId = `alert-${id}`
        setAlerts([
            ...alerts,
            {
                alert: <Alert toggle={() => dismiss(alerts, alertId, setAlerts)} key={"alert-" + id} id={alertId} color="info">
                    {json.message}
                </Alert>,
                id: alertId
            }
        ]);
    }
}

