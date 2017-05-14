const DIALOGS_REST = "/rest/dialogs";
const DIALOG_MESSAGES_REST = "/rest/dialog/messages";

export const ChatServices = {
    loadDialogs () {
        return fetch(DIALOGS_REST, {
            method: 'GET',
            credentials: 'include'
        }).then(
            response => {
                return response.json();
            },
            () => {
                throw 'Error dialogs loading!';
            }
        )
    },

    loadMessages (dialogId) {
        return fetch(DIALOG_MESSAGES_REST, {
            method: 'POST',
            credentials: 'include',
            body: JSON.stringify({ dialogId })
        }).then(
            response => {
                return response.json();
            },
            () => {
                throw 'Error dialog messages loading!';
            }
        )
    }
};