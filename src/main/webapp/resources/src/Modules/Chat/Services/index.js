import queryString from 'query-string';

const USER_INFO_REST = "/rest/user_info";
const DIALOGS_REST = "/rest/dialogs";
const DIALOG_MESSAGES_REST = "/rest/dialog/messages";

export const ChatServices = {
    getUserInfo () {
        return fetch(USER_INFO_REST, {
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
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: queryString.stringify({ dialogId })
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