import * as React from 'react';

import {ChatServices} from '../Services'

export const Dialog = React.createClass({
    displayName: 'Chat.Dialog',

    propTypes: {
        id: React.PropTypes.number
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
        }).catch(() => {
            this.setState({
                activeRequest: null
            });
        });

        this.setState({ activeRequest: request });
    },

    componentWillReceiveProps(nextProps) {
        if (this.props.id !== nextProps.id) {
            if (this.state.activeRequest !== null) {
                this.state.activeRequest.terminate();
            }
            this.loadMessages(nextProps.id);
        }

    },

    renderNotSelectedDialog() {
        return (
            <div className="dialog-empty">Для начала переписки выберите диалог или <a href="">создайте новый</a>.</div>
        );
    },

    renderDialogLoading() {
        return (
            <div className="dialog-loading">Загрузка...</div>
        );
    },

    renderMessages() {
        const isLoading = this.state.activeRequest !== null;

        return 'messages';
    },

    renderDialog() {
        const isLoading = this.state.activeRequest !== null;

        return isLoading ? this.renderDialogLoading() : this.renderMessages();
    },

    /**
     * @return {JSX.Element}
     */
    render () {
        const dialogSelected = this.props.id !== 0;

        return (
            <div className="dialog">
                { dialogSelected ? this.renderDialog() : this.renderNotSelectedDialog() }
            </div>
        );
    }
});
