import * as React from 'react';
import { findDOMNode } from 'react-dom';
import classNames from 'classNames';

import {ChatServices} from '../Services'

export const Dialog = React.createClass({
    displayName: 'Chat.Dialog',

    propTypes: {
        dialog: React.PropTypes.object,
        user: React.PropTypes.object
    },

    getInitialState () {
        return {
            activeRequest: null,
            messages: null
        };
    },

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

    componentWillReceiveProps(nextProps) {
        if (nextProps.dialog !== this.props.dialog) {
            if (this.state.activeRequest !== null) {
                this.state.activeRequest.terminate();
            }
            this.loadMessages(nextProps.dialog.id);
        }

    },

    setScrollToBottom() {
        const el = findDOMNode(this.refs['dialog-messages-wrap']);
        el.scrollTop = el.scrollHeight;
    },

    renderNotSelectedDialog() {
        return (
            <div className="dialog-empty">Для начала переписки выберите диалог или <a href="">создайте новый</a>.</div>
        );
    },

    handleInputMessage(e) {
        e.target.style.height = 'auto';
        e.target.style.height = `${e.target.scrollHeight}px`;
    },

    renderDialogLoading() {
        return (
            <div className="dialog-loading">Загрузка...</div>
        );
    },

    renderDialogInfo() {
        const {dialog} = this.props;
        const participant = dialog.participants[0];

        return (
            <div className="dialog-description" key="dialog-info">
                {participant.name}
            </div>
        )
    },

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

    renderMessage(messageObj) {
        const {user} = this.props;
        const classes = classNames({
            "dialog-message-item": true,
            "own-message": user.id === messageObj.author.id
        });

        return (
            <div className={classes} key={`message${messageObj.id}`}>
                <div className="dialog-message-user-avatar">
                    <img src={messageObj.author.picture} alt="" />
                </div>
                <div className="dialog-message-text">{messageObj.message}</div>
            </div>
        );
    },

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

    renderDialog() {
        const isLoading = this.state.activeRequest !== null;

        return isLoading ? this.renderDialogLoading() : [
            this.renderDialogInfo(),
            this.renderMessages(),
            this.renderSendMessageBlock()
        ];
    },

    /**
     * @return {JSX.Element}
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
