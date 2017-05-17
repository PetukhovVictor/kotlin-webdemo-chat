import * as React from 'react';
import { findDOMNode } from 'react-dom';
import { remove } from 'lodash';
import classNames from 'classNames';
import moment from 'moment';

moment.locale('ru');

import {ChatServices} from '../Services'

/**
 * Компонент диалога realtime-чата.
 * Реализует список сообщений, отправку и получение сообщений.
 */
export const Dialog = React.createClass({
    displayName: 'Chat.Dialog',

    /**
     * Свойства компонента.
     *
     * dialog - выбранный диалог.
     * user - текущий пользователь.
     */
    propTypes: {
        dialog: React.PropTypes.object,
        user: React.PropTypes.object
    },

    /**
     * Начальное состояние компонента.
     *
     * activeRequest - активный запрос (его наличие говорит о продолжающейся загрузке списка сообщений).
     * messages - список сообщений выбранного диалога.
     * sendingMessagesCounter - счетчик отправляемых сообщений (используется для временной идентификации сообщений).
     * sendingMessages - список отправляемых сообщений
     *      (либо отправляются в данных момент, либо находятся в очереди на отправку).
     *      Эти сообщения наряду с messages так же отображаются в диалоге (в конце).
     * unsentMessages - список неотправленных по причине ошибки сообщений.
     * messageSendRequestsQueue - очередь запросов на отправку сообщений
     *      (по мере завершения отправки сообщения, из очереди извлекается новое сообщение,
     *      а отправленное перемещаются в messages).
     * isMessageSending - флаг, указывающий на то, отправляется ли сообщение в данный момент.
     */
    getInitialState () {
        return {
            activeRequest: null,
            messages: null,
            sendingMessagesCounter: 0,
            sendingMessages: [],
            unsentMessages: [],
            messageSendRequestsQueue: [],
            isMessageSending: false
        };
    },

    /**
     * Загрузка списка сообщений выбранного диалога.
     *
     * @param dialogId ID диалога, сообщения которого необходимо загрузить.
     */
    loadMessages (dialogId) {
        const request = ChatServices.loadMessages(dialogId).then(messages => {
            this.setState({
                activeRequest: null,
                messages: messages
            });
            this.setScrollToBottom();
        }).catch(() => {
            this.setState({
                activeRequest: null
            });
        });

        this.setState({ activeRequest: request });
    },

    /**
     * Обработчик получения новых props.
     * При выборе нового диалога отменяем активный запрос на получение списка сообщения (если имеется)
     * и отправляем другой для только что выбранного диалога.
     *
     * @param nextProps Новые props компонента.
     */
    componentWillReceiveProps(nextProps) {
        if (nextProps.dialog !== this.props.dialog) {
            if (this.state.activeRequest !== null) {
                this.state.activeRequest.terminate();
            }
            this.loadMessages(nextProps.dialog.id);
        }
    },

    /**
     * Осуществляем прокрутку списка сообщений в самый низ (чтобы видеть самые последние сообщения).
     * Прокрутка осуществляется при выборе диалога и при отправке/получении сообщения.
     */
    setScrollToBottom() {
        const el = findDOMNode(this.refs['dialog-messages-wrap']);
        el.scrollTop = el.scrollHeight;
    },

    /**
     * Запуск отправки следующего в очереди запроса на отправку сообщения.
     * Если запросов в очереди больше нет, завершаем отправку, помечая в state флаг isMessageSending в false.
     */
    nextSendMessageRequest () {
        const {messageSendRequestsQueue, unsentMessages, sendingMessagesCounter} = this.state;
        if (messageSendRequestsQueue.length !== 0) {
            const nextRequest = messageSendRequestsQueue.shift();
            nextRequest();
            this.setState({ messageSendRequestsQueue });
        } else {
            this.setState({
                isMessageSending: false,
                sendingMessagesCounter: unsentMessages.length === 0 ? 0 : sendingMessagesCounter
            });
        }
    },

    /**
     * Добавление запроса на отправку сообщения в очередь.
     *
     * @param request callback-фукнция, в которой вызывается сервис, отправляющий запрос.
     */
    addSendMessageRequestInQueue(request) {
        const {messageSendRequestsQueue, isMessageSending} = this.state;

        if (!isMessageSending) {
            request();
        } else {
            this.setState({
                messageSendRequestsQueue: [
                    ...messageSendRequestsQueue,
                    request
                ]
            });
        }
    },

    /**
     * Перемещение сообщения из "Отправляемых" в "Отправленные".
     *
     * @param pseudoId Псевдо-идентификатор, присвоенный сообщению перед запуском отправки (или помещением в очередь).
     * @param message Объект отправленного сообщения.
     */
    messageMoveToSent(pseudoId, message) {
        const {sendingMessages, messages} = this.state;

        remove(sendingMessages, sendingMessage => sendingMessage.id === pseudoId);
        messages.push(message);
        this.setState({ messages, sendingMessages });
    },

    /**
     * Перемещение сообщения из "Отправляемых" в "Неотправленные".
     *
     * @param pseudoId Псевдо-идентификатор, присвоенный сообщению перед запуском отправки (или помещением в очередь).
     */
    messageMoveToUnsent(pseudoId) {
        let {unsentMessages, sendingMessages} = this.state;

        const unsentMessage = remove(sendingMessages, sendingMessage => sendingMessage.id === pseudoId);
        unsentMessages.push(unsentMessage[0]);
        this.setState({ unsentMessages, sendingMessages });
    },

    /**
     * Отправка сообщения.
     *
     * @param message Текст сообщения.
     */
    sendMessage(message) {
        const {sendingMessages, sendingMessagesCounter} = this.state;
        const pseudoId = -(sendingMessagesCounter + 1);
        // Формируем временный объект сообщения для мгновенного отображения в списке сообщений.
        const messageObj = {
            // Псведо-идентификатор. Он равен отрицательному числу, по модулю равному номеру сообщения в очереди на отправку.
            id: pseudoId,
            message,
            author: this.props.user
        };

        this.setState({
            isMessageSending: true,
            sendingMessages: [
                ...sendingMessages,
                messageObj
            ],
            sendingMessagesCounter: sendingMessagesCounter + 1
            // По окончании рендеринга вслед за обновлением state скроллим блок с сообщениями к низу.
        }, () => this.setScrollToBottom());

        // Добавляем запрос на отправку сообщения в очередь.
        this.addSendMessageRequestInQueue(() => {
            // По результатам выполнения перемещаем сообщение либо в "Отправленные", либо в "Неотправленные".
            ChatServices.sendMessage(this.props.dialog.id, message).then(message => {
                this.messageMoveToSent(pseudoId, message);
                this.nextSendMessageRequest();
            }).catch(() => {
                this.messageMoveToUnsent(pseudoId);
                this.nextSendMessageRequest();
            });
        });
    },

    /**
     * Подгон высоты поля ввода нового сообщения под введенный текст по мере ввода (если требуется).
     *
     * @param element DOM-элемент поля ввода нового сообщения.
     */
    resizeNewMessageInput(element) {
        element.style.height = 'auto';
        element.style.height = `${element.scrollHeight}px`;
    },

    /**
     * Удаление неотправленного сообщения.
     *
     * @param pseudoId Идентификатор неотправленного сообщения (в рамках всех сообщений - псведо-идентификатор).
     */
    removeUnsentMessage(pseudoId) {
        let {unsentMessages} = this.state;

        remove(unsentMessages, unsentMessage => unsentMessage.id === pseudoId);
        this.setState({ unsentMessages });
    },

    /**
     * Обработка клика по ссылке удаления неотправленного сообщения.
     *
     * @param messageObj Объект неотправленного сообщения.
     * @param event Объект собятия Click.
     */
    handleClickRemoveUnsentMessage(messageObj, event) {
        this.removeUnsentMessage(messageObj.id);
        event.preventDefault();
    },

    /**
     * Обработка клика по ссылке повторной отправки неотправленного сообщения.
     *
     * @param messageObj Объект неотправленного сообщения.
     * @param event Объект собятия Click.
     */
    handleClickSendMessageTryAgain(messageObj, event) {
        this.removeUnsentMessage(messageObj.id);
        this.sendMessage(messageObj.message);
        event.preventDefault();
    },

    /**
     * Обработка ввода текста (любым способом) в поле для отправки сообщения.
     *
     * @param event Объект события Input.
     */
    handleInputMessage(event) {
        this.resizeNewMessageInput(event.target);
    },

    /**
     * Обработка ввода текста (нажатием клавиши) в поле для отправки сообщения.
     *
     * @param event Объект события KeyDown.
     */
    handleKeyDownMessage(event) {
        /*
         Делаем перевод строки по нажатию на Ctrl + Enter, либо Command + Enter (для Mac)
         только если поле не пустое (исключаем переводы в начале текста);
         а отправку сообщения - по нажатию Enter.
         */
        if ((event.ctrlKey || event.metaKey) && event.keyCode === 13 && event.target.value) {
            event.target.value += '\n';
            this.resizeNewMessageInput(event.target);
        // Не отправляем сообщение, если в очереди уже есть отправляемое сообщение (не завершился запрос).
        } else if (event.keyCode === 13) {
            this.sendMessage(event.target.value);
            event.preventDefault();
        }
    },

    /**
     * Рендеринг сообщения о загрузке списка сообщений выбранного диалога.
     */
    renderDialogLoading() {
        return (
            <div className="dialog-loading">Загрузка...</div>
        );
    },

    /**
     * Рендеринг сообщения о невыбранном диалоге (рендерится по умолчанию, до выбора диалога).
     */
    renderNotSelectedDialog() {
        return (
            <div className="dialog-empty">Для начала переписки выберите диалог или <a href="">создайте новый</a>.</div>
        );
    },

    /**
     * Рендеринг информации о диалоге (выводится в шапке окна диалога).
     */
    renderDialogInfo() {
        const {dialog} = this.props;
        const participant = dialog.participants[0];

        return (
            <div className="dialog-description" key="dialog-info">
                {participant.name}
            </div>
        )
    },

    /**
     * Рендеринг блока для отправки сообщения.
     */
    renderSendMessageBlock() {
        const {isMessageSending} = this.state;
        const sendMessageText = isMessageSending ? 'Отправка...' : 'Отправить';

        return (
            <div className="dialog-send-message" key="send-message">
                <textarea
                    placeholder="Сообщение..."
                    onInput={this.handleInputMessage}
                    onKeyDown={this.handleKeyDownMessage}
                />
                <button type="submit" disabled={isMessageSending}>{sendMessageText}</button>
            </div>
        )
    },

    /**
     * Рендеринг сообщения.
     * Собственные сообщения рендерятся с классом own-message и отображаются справа, а не слева.
     *
     * @param messageObj Объект сообщения, которое нужно отрендерить.
     * @param status Статус сообщения:
     *      1) 'sending' - отправляется в данный момент,
     *      2) 'unsent' - не отправлено по причине ошибки,
     *      3) <не задано> - сообщение находится на сервере.
     */
    renderMessage(messageObj, status) {
        const {user} = this.props;
        const classes = classNames({
            "dialog-message-item": true,
            "own-message": user.id === messageObj.author.id
        });
        let footerContent;

        switch (status) {
            // Сообщение отправляется в данный момент.
            case 'sending':
                footerContent = <span className="dialog-message-footer">Отправка...</span>;
                break;
            // При отправке сообщения произошла ошибка.
            case 'unsent':
                footerContent = (
                    <span className="dialog-message-footer">
                        <span className="error">Ошибка</span>
                        <a href="#" onClick={this.handleClickSendMessageTryAgain.bind(this, messageObj)}>Попробовать снова</a>
                        <a href="#" onClick={this.handleClickRemoveUnsentMessage.bind(this, messageObj)}>Удалить</a>
                    </span>
                );
                break;
            // Сообщение находится на сервере.
            default:
                const date = moment(messageObj.date);
                footerContent = (
                    <span className="dialog-message-footer">
                        <span>{date.format("hh:mm")}</span>
                        <span>{date.format("D MMMM YYYY")}</span>
                    </span>
                )
        }

        return (
            <div className={classes} key={`message${messageObj.id}`}>
                <div className="dialog-message-user-avatar">
                    <img src={messageObj.author.picture} alt="" />
                </div>
                <div className="dialog-message-text">
                    <span>{messageObj.message}</span>
                    {footerContent}
                </div>
            </div>
        );
    },

    /**
     * Рендеринг списка сообщений.
     * Для дальнейщего скроллирования блока со списком сообщений по необходимости линкуем его DOM-элемент ref-ом.
     */
    renderMessages() {
        const {messages, sendingMessages, unsentMessages} = this.state;
        let messageElements = [];
        const renderMessage = (message, type) => {
            messageElements.push(this.renderMessage(message, type));
        };

        messages.map(message => renderMessage(message));
        sendingMessages.map(message => renderMessage(message, 'sending'));
        unsentMessages.map(message => renderMessage(message, 'unsent'));

        return (
            <div className="dialog-messages-wrap" ref="dialog-messages-wrap" key="messages">
                <div className="dialog-messages">
                    {messageElements}
                </div>
            </div>
        );
    },

    /**
     * Рендеринг блока диалога.
     */
    renderDialog() {
        const isLoading = this.state.activeRequest !== null;

        return isLoading ? this.renderDialogLoading() : [
            this.renderDialogInfo(),
            this.renderMessages(),
            this.renderSendMessageBlock()
        ];
    },

    /**
     * Корневой рендер диалога.
     * Рендерим либо диалог, либо сообщение о том, что диалог не выбран.
     */
    render () {
        const dialogSelected = this.props.dialog !== null;

        return (
            <div className="dialog">
                { dialogSelected ? this.renderDialog() : this.renderNotSelectedDialog() }
            </div>
        );
    }
});
