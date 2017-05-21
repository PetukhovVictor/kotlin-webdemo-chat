import * as React from 'react';
import {findDOMNode} from 'react-dom';
import {remove, reverse} from 'lodash';
import classNames from 'classNames';
import moment from 'moment';

import {ChatServices} from '../Services'

moment.locale('ru');

/**
 * Максимальная позиция скролла в блоке с сообщениями,
 * при которой осуществляется подгрузка предыдущей порции сообщений.
 *
 * @type {number}
 */
const MAX_SCROLL_POSITION_FOR_MESSAGE_LOADING = 10;

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
     * dialogOnTop - callback-функция для перемещения диалога в списке даилогов наверх после отправки/приема сообщения.
     * onSearch - callback-функция для активации режима поиска.
     */
    propTypes: {
        dialog: React.PropTypes.object,
        user: React.PropTypes.object,
        dialogOnTop: React.PropTypes.func,
        onSearch: React.PropTypes.func
    },

    /**
     * Начальное состояние компонента.
     *
     * allMessagesLoaded - флаг показывающий, требуется ли подгрузка сообщений.
     *      Устанавливает в true, когда последний запрос на подгрузку вернул пустой результат.
     * activeAdditionalRequest - активный запрос (его наличие говорит о продолжающейся подгрузке порции сообщений).
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
            allMessagesLoaded: false,
            activeAdditionalRequest: null,
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
     * @param lastMessageId Идентификатор последнего загруженного сообщения (определяет порцию подгружаемых собщений).
     */
    loadMessages (dialogId, lastMessageId) {
        lastMessageId = lastMessageId || 0;
        const isAdditionalLoading = lastMessageId !== 0;
        const targetRequest = lastMessageId === 0 ? 'activeRequest' : 'activeAdditionalRequest';
        const request = ChatServices.loadMessages(dialogId, lastMessageId).then(messages => {
            let stateObj = {
                [targetRequest]: null,
                messages: isAdditionalLoading ? reverse(messages).concat(this.state.messages) : reverse(messages)
            };
            if (messages.length === 0) {
                stateObj.allMessagesLoaded = true;
            }
            if (isAdditionalLoading) {
                const currentScrollPosition = this.getScrollBottom();
                this.setState(stateObj, () => this.setScrollBottom(currentScrollPosition));
            } else {
                // Скроллим блок к низу только если осущетвлялась первая загрузка сообщений.
                this.setState(stateObj, this.setScrollBottom);
            }
            const sendMessageInputElement = findDOMNode(this.refs['dialog-send-message-input']);
            sendMessageInputElement.focus();
        }).catch(() => {
            this.setState({
                [targetRequest]: null
            });
        });

        this.setState({ [targetRequest]: request });
    },

    /**
     * Обработчик получения новых props.
     * При выборе нового диалога отменяем активный запрос на получение списка сообщения (если имеется)
     * и отправляем другой для только что выбранного диалога, а также возвращаем state к исходному состоянию.
     *
     * @param nextProps Новые props компонента.
     */
    componentWillReceiveProps(nextProps) {
        const {activeRequest, activeAdditionalRequest} = this.state;

        if (nextProps.dialog !== this.props.dialog) {
            activeRequest !== null && activeRequest.terminate();
            activeAdditionalRequest !== null && activeAdditionalRequest.terminate();
            this.setState(this.getInitialState());
            this.loadMessages(nextProps.dialog.id);
            ChatServices.subscribeMessages(nextProps.dialog.id, this.receiveMessage);
        }
    },

    /**
     * Получение позиции скролла относительно нижней границы.
     *
     * @returns {number} Позиция скролла относительно нижней границы.
     */
    getScrollBottom() {
        const dialogMessagesWrapElement = findDOMNode(this.refs['dialog-messages-wrap']);
        return dialogMessagesWrapElement.scrollHeight - dialogMessagesWrapElement.scrollTop;
    },

    /**
     * Осуществляем прокрутку списка сообщений в заданную позицию относительно нижней границы.
     * Прокрутка осуществляется при выборе диалога и при отправке/получении сообщения.
     * 
     * @param scrollPosition Позиция скролла относительно нижней границы.
     */
    setScrollBottom(scrollPosition) {
        scrollPosition = scrollPosition || 0;
        const dialogMessagesWrapElement = findDOMNode(this.refs['dialog-messages-wrap']);
        dialogMessagesWrapElement.scrollTop = dialogMessagesWrapElement.scrollHeight - scrollPosition;
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
        this.setState(
            { messages, sendingMessages },
            () => this.setScrollBottom()
        );
        this.props.dialogOnTop();
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
        this.setState(
            { unsentMessages, sendingMessages },
            () => this.setScrollBottom()
        );
    },

    /**
     * Приём сообщения: добавление сообщения в список сообщений.
     *
     * @param messageObj Объект сообщения.
     */
    receiveMessage(messageObj) {
        let {messages} = this.state;
        const {user: {id: userId}, dialogOnTop} = this.props;

        if (messageObj.authorId === userId) {
            return;
        }
        messages.push(messageObj);
        this.setState(
            { messages },
            () => this.setScrollBottom()
        );
        dialogOnTop();
    },

    /**
     * Отправка сообщения.
     *
     * @param message Текст сообщения.
     */
    sendMessage(message) {
        const {sendingMessages, sendingMessagesCounter} = this.state;
        const {user} = this.props;
        const pseudoId = -(sendingMessagesCounter + 1);
        // Формируем временный объект сообщения для мгновенного отображения в списке сообщений.
        const messageObj = {
            // Псведо-идентификатор. Он равен отрицательному числу, по модулю равному номеру сообщения в очереди на отправку.
            id: pseudoId,
            message: message,
            authorId: user.id,
            authorName: user.name,
            authorPicture: user.picture
        };

        const sendMessageInputElement = findDOMNode(this.refs['dialog-send-message-input']);
        sendMessageInputElement.value = '';

        this.setState({
            isMessageSending: true,
            sendingMessages: [
                ...sendingMessages,
                messageObj
            ],
            sendingMessagesCounter: sendingMessagesCounter + 1
            // По окончании рендеринга вслед за обновлением state скроллим блок с сообщениями к низу.
        }, () => this.setScrollBottom());

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
     * Обработка клика по кнопке отправки сообщения.
     *
     * @param event Объект события Click.
     */
    handleClickSendMessageButton(event) {
        const message = findDOMNode(this.refs['dialog-send-message-input']).value;
        this.sendMessage(message);
        event.preventDefault();
    },

    /**
     * Обработка клика по ссылке создания нового диалога.
     *
     * @param event Объект события Click.
     */
    handleClickNewDialog(event) {
        this.props.onSearch();
        event.preventDefault();
    },

    /**
     * Обработка скроллирования блока с сообщениями.
     *
     * @param event Объект события Scroll.
     */
    handleScrollMessageBlock(event) {
        const scrollPosition = event.target.scrollTop;

        if (scrollPosition <= MAX_SCROLL_POSITION_FOR_MESSAGE_LOADING) {
            const {activeAdditionalRequest, messages, allMessagesLoaded} = this.state;
            if (activeAdditionalRequest === null && messages.length !== 0 && !allMessagesLoaded) {
                const lastMessageId = messages[0].id;
                this.loadMessages(this.props.dialog.id, lastMessageId);
            }
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
            <div className="dialog-empty">
                Для начала переписки выберите диалог или <a href="#" onClick={this.handleClickNewDialog}>поищите собеседников</a>.
            </div>
        );
    },

    /**
     * Рендеринг информации о диалоге (выводится в шапке окна диалога).
     */
    renderDialogInfo() {
        const {dialog} = this.props;

        return (
            <div className="dialog-description" key="dialog-info">
                {dialog.interlocutorName}
            </div>
        )
    },

    /**
     * Рендеринг блока для отправки сообщения.
     */
    renderSendMessageBlock() {
        return (
            <div className="dialog-send-message" key="send-message">
                <textarea
                    placeholder="Сообщение..."
                    onInput={this.handleInputMessage}
                    onKeyDown={this.handleKeyDownMessage}
                    ref="dialog-send-message-input"
                />
                <button type="submit" onClick={this.handleClickSendMessageButton}>Отправить</button>
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
            "own-message": user.id === messageObj.authorId
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
                        <span>{date.format("HH:mm")}</span>
                        <span>{date.format("D MMMM YYYY")}</span>
                    </span>
                )
        }

        return (
            <div className={classes} key={`message${messageObj.id}`}>
                <div className="dialog-message-user-avatar">
                    <img src={messageObj.authorPicture} alt="" />
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
        const numberMessages = messages.length + sendingMessages.length + unsentMessages.length;
        let messageElements = [];
        const renderMessage = (message, type) => {
            messageElements.push(this.renderMessage(message, type));
        };

        messages.map(message => renderMessage(message));
        sendingMessages.map(message => renderMessage(message, 'sending'));
        unsentMessages.map(message => renderMessage(message, 'unsent'));

        return (
            <div className="dialog-messages-wrap" onScroll={this.handleScrollMessageBlock} ref="dialog-messages-wrap" key="messages">
                { numberMessages === 0 ? (
                    <div className="dialog-messages-empty">
                        Сообщений пока нет.
                    </div>
                ) : (
                        <div className="dialog-messages">
                            {messageElements}
                        </div>
                    )
                }
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
