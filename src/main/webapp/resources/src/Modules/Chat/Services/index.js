const DIALOGS_REST = "/rest/dialogs";

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
    }
};