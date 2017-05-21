import queryString from 'query-string';
import Stomp from 'stompjs';
import SockJS from 'sockjs-client';

const USER_INFO_REST = "/rest/user_info";
const DIALOGS_REST = "/rest/dialogs";
const DIALOGS_SEARCH_REST = "/rest/dialogs/search";
const DIALOG_CREATE = "/rest/dialog/create";
const DIALOG_MESSAGES_REST = "/rest/dialog/messages";
const DIALOG_SEND_MESSAGE_REST = "/rest/dialog/message/send";
const DIALOG_MESSAGES_ENDPOINT = "/message/send";
const DIALOG_MESSAGES_SUBSCRIBE = "/chat";

/**
 * Stomp-клиент для работы с веб-сокетами (используется для приёма сообщений).
 */
let stompClient = null;

export const ChatServices = {
    /**
     * Получение информации о пользователе.
     *
     * @returns Promise с информацией о пользователе.
     */
    getUserInfo () {
        return fetch(USER_INFO_REST, {
            method: 'GET',
            credentials: 'include'
        }).then(
            response => {
                if (!response.ok) {
                    throw 'Error user info loading!';
                }
                return response.json();
            },
            () => {
                throw 'Error user info loading!';
            }
        )
    },

    /**
     * Получение списка диалогов.
     *
     * @returns Promise со списком диалогов.
     */
    loadDialogs () {
        return fetch(DIALOGS_REST, {
            method: 'GET',
            credentials: 'include'
        }).then(
            response => {
                if (!response.ok) {
                    throw 'Error dialogs loading!';
                }
                return response.json();
            },
            () => {
                throw 'Error dialogs loading!';
            }
        )
    },

    /**
     * Получение списка сообщение диалога.
     *
     * @param dialogId ID диалога, сообщения которого необходимо загрузить.
     * @param lastMessageId Идентификатор последнего загруженного сообщения (определяет порцию подгружаемых собщений).
     *
     * @returns Promise со списком сообщений диалога.
     */
    loadMessages (dialogId, lastMessageId) {
        lastMessageId = lastMessageId || 0;
        return fetch(DIALOG_MESSAGES_REST, {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: queryString.stringify({ dialogId, lastMessageId })
        }).then(
            response => {
                if (!response.ok) {
                    throw 'Error dialogs loading!';
                }
                return response.json();
            },
            () => {
                throw 'Error dialog messages loading!';
            }
        )
    },

    /**
     * Отправка сообщения.
     *
     * @param dialogId ID диалога, в который отправляется сообщение.
     * @param message Текст сообщения.
     *
     * @returns Promise с результатом отправки сообщения.
     */
    sendMessage (dialogId, message) {
        return fetch(DIALOG_SEND_MESSAGE_REST, {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: queryString.stringify({ dialogId, message })
        }).then(
            response => {
                if (!response.ok) {
                    throw 'Error dialogs loading!';
                }
                return response.json();
            },
            () => {
                throw 'Error send message!';
            }
        )
    },

    /**
     * Осуществление поиска собеседников (по имени и по email).
     *
     * @param value Поисковая фраза.
     */
    search (value) {
        return fetch(DIALOGS_SEARCH_REST, {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: queryString.stringify({ phrase: value })
        }).then(
            response => {
                if (!response.ok) {
                    throw 'Error dialogs loading!';
                }
                return response.json();
            },
            () => {
                throw 'Error send message!';
            }
        )
    },

    /**
     * Создание диалога.
     *
     * @param interlocutorId ID собеседника, с которым необходимо создать диалог.
     */
    createDialog (interlocutorId) {
        return fetch(DIALOG_CREATE, {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: queryString.stringify({ interlocutorId })
        }).then(
            response => {
                if (!response.ok) {
                    throw 'Error dialogs loading!';
                }
                return response.json();
            },
            () => {
                throw 'Error send message!';
            }
        )
    },

    /**
     * Подписка на получение новых сообщений.
     *
     * @param dialogId ID диалога.
     * @param callback Callback-функция, вызываемая при получении нового сообщения.
     */
    subscribeMessages (dialogId, callback) {
        stompClient !== null && stompClient.disconnect();
        const socket = new SockJS(DIALOG_MESSAGES_ENDPOINT);
        stompClient = Stomp.over(socket);
        stompClient.connect({}, () => {
            stompClient.subscribe(`${DIALOG_MESSAGES_SUBSCRIBE}/${dialogId}`, response => {
                const messageObj = JSON.parse(response.body);
                callback(messageObj);
            });
        })
    }
};