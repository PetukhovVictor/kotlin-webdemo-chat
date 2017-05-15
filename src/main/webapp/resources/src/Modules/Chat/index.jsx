import * as React from 'react';
import classNames from 'classNames';

import {ChatServices} from './Services';
import {Dialog} from './Components/Dialog';

export const Chat = React.createClass({
    displayName: 'Chat',

    /**
     * Начальное состояние компонента.
     *
     * isLoading - флаг, указывающий на то, загружается ли список диалогов в настоящий момент.
     * activeDialog - активный диалог (по умолчания не выбрано ниодного диалога).
     * dialogs - список диалогов.
     */
    getInitialState () {
        return {
            isLoading: true,
            activeDialog: null,
            dialogs: null,
            user: null
        };
    },

    /**
     * После монтирования компонента стартуем загрузку списка диалогов.
     * По окочании загрузки - записываем результат в state (и далее отображаем диалоги).
     */
    componentDidMount () {
        const userInfoPromise = ChatServices.getUserInfo();
        const dialogsPromise = ChatServices.loadDialogs();

        Promise.all([userInfoPromise, dialogsPromise]).then(result => {
            const userInfo = result[0];
            const dialogs = result[1];
            this.setState({
                isLoading: false,
                dialogs: dialogs,
                user: userInfo
            });
        }).catch(() => {
            this.setState({
                isLoading: false
            });
        });
    },

    handleClickDialog (dialog) {
        this.setState({ activeDialog: dialog });
    },

    renderDialogItem (dialog) {
        const participant = dialog.participants[0];
        const classes = classNames({
            "dialog-item": true,
            "dialog-item-active": dialog === this.state.activeDialog
        });

        return (
            <div
                className={classes}
                onClick={this.handleClickDialog.bind(this, dialog)}
                key={`dialog${dialog.id}`}
            >
                <div className="dialog-user-picture">
                    <img src={participant.picture} alt="" />
                </div>
                <div className="dialog-user-name">
                    {participant.name}
                </div>
            </div>
        )
    },

    renderDialogs () {
        const {dialogs} = this.state;
        let dialogElements = [];

        dialogs.map(dialog => {
            dialogElements.push(this.renderDialogItem(dialog));
        });

        return dialogElements;
    },

    /**
     * В зависимости от состояния загрузки рендерим либо лоадер, либо компонент чата.
     *
     * @return {JSX.Element}
     */
    render () {
        const {isLoading, activeDialog, user} = this.state;

        return (
            <div className="chat">
                <div className="dialogs">
                    <h1>Диалоги</h1>
                    {
                        isLoading ?
                            <div className="dialogs-loading">Загрузка...</div> :
                            this.renderDialogs()
                    }
                </div>
                <Dialog dialog={activeDialog} user={user} />
            </div>
        );
    }
});
