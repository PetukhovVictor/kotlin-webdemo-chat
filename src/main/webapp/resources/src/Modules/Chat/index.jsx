import * as React from 'react';
import classNames from 'classNames';

import {ChatServices} from './Services';
import {Dialog} from './Components/Dialog';

/**
 * Компонент realtime-чата.
 */
export const Chat = React.createClass({
    displayName: 'Chat',

    /**
     * Начальное состояние компонента.
     *
     * isLoading - флаг, указывающий на то, загружается ли список диалогов в настоящий момент.
     * activeDialog - активный диалог (по умолчанию диалог не выбран).
     * dialogs - список диалогов.
     * user - текущий пользователь.
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
     * После монтирования компонента стартуем загрузку списка диалогов и информации о текущем пользователе.
     * По окочании загрузки - записываем результат в state (и далее отображаем диалоги).
     */
    componentDidMount () {
        this.chatInit();
    },
    /**
     * Инициализация чата (отправка фундаментальных запросов: на получением списка диалогов и информации о текущем пользователе).
     */
    chatInit () {
        const userInfoPromise = ChatServices.getUserInfo();
        const dialogsPromise = ChatServices.loadDialogs();

        this.setState({ isLoading: true });

        Promise.all([userInfoPromise, dialogsPromise]).then(result => {
            const dialogs = result[1];
            const userInfo = result[0];
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

    /**
     * Обработка клика по ссылке повторной попытки загрузки диалогов.
     *
     * @param event Объект события Click.
     */
    handleClickTryAgainInit (event) {
        this.chatInit();
        event.preventDefault();
    },

    /**
     * Обработка клика по диалогу.
     *
     * @param dialog Выбранный диалог.
     * @param event Объект события Click.
     */
    handleClickDialog (dialog, event) {
        this.setState({ activeDialog: dialog });
        event.preventDefault();
    },

    /**
     * Рендеринг диалога в списке диалогов.
     *
     * @param dialog Объект диалога, который нужно отрендерить.
     */
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

    /**
     * Рендеринг списка диалогов.
     */
    renderDialogs () {
        const {dialogs} = this.state;
        let dialogElements = [];

        dialogs.map(dialog => {
            dialogElements.push(this.renderDialogItem(dialog));
        });

        return dialogElements;
    },

    /**
     * Рендеринг компонента чата.
     * В зависимости от состояния загрузки рендерим либо лоадер, либо список диалогов.
     */
    render () {
        const {isLoading, dialogs, activeDialog, user} = this.state;
        let content;

        if (isLoading) {
            content = <div className="dialogs-loading">Загрузка...</div>;
        } else if (dialogs !== null) {
            content = this.renderDialogs();
        } else {
            content = (
                <div className="dialogs-loading">
                    <p className="error">Ошибка при загрузке.</p>
                    <a href="#" onClick={this.handleClickTryAgainInit}>Попробовать ещё раз</a>
                </div>
            );
        }

        return (
            <div className="chat">
                <div className="dialogs">
                    <h1>Диалоги</h1>
                    {content}
                </div>
                <Dialog dialog={activeDialog} user={user} />
            </div>
        );
    }
});
