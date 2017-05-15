import * as React from 'react';
import { findDOMNode } from 'react-dom';
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
     */
    getInitialState () {
        return {
            activeRequest: null,
            messages: null
        };
    },

    /**
     * Загрузка списка сообщений выбранного диалога.
     *
     * @param dialogId
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
     * Обработка ввода текста в поле для отправки сообщения.
     * По мере ввода осуществляется подгон высоты поля под введенный текст (если требуется).
     *
     * @param e Объект события Input.
     */
    handleInputMessage(e) {
        e.target.style.height = 'auto';
        e.target.style.height = `${e.target.scrollHeight}px`;
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
        return (
            <div className="dialog-send-message" key="send-message">
                <textarea
                    placeholder="Сообщение..."
                    onInput={this.handleInputMessage}
                />
                <button type="submit">Отправить</button>
            </div>
        )
    },

    /**
     * Рендеринг сообщения.
     * Собственные сообщения рендерятся с классом own-message и отображаются справа, а не слева.
     *
     * @param messageObj Объект сообщения, которое нужно отрендерить.
     */
    renderMessage(messageObj) {
        const {user} = this.props;
        const classes = classNames({
            "dialog-message-item": true,
            "own-message": user.id === messageObj.author.id
        });
        const date = moment(messageObj.date);

        return (
            <div className={classes} key={`message${messageObj.id}`}>
                <div className="dialog-message-user-avatar">
                    <img src={messageObj.author.picture} alt="" />
                </div>
                <div className="dialog-message-text">
                    <span>{messageObj.message}</span>
                    <span className="dialog-message-date">
                        <span>{date.format("hh:mm")}</span>
                        <span>{date.format("D MMMM YYYY")}</span>
                    </span>
                </div>
            </div>
        );
    },

    /**
     * Рендеринг списка сообщений.
     * Для дальнейщего скроллирования блока со списком сообщений по необходимости линкуем его DOM-элемент ref-ом.
     */
    renderMessages() {
        const {messages} = this.state;
        let messageElements = [];

        messages.map(message => {
            messageElements.push(this.renderMessage(message));
        });

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
