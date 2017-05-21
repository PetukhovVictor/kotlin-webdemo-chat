import * as React from 'react';
import classNames from 'classNames';
import {remove} from 'lodash';
import {findDOMNode} from 'react-dom';


import {ChatServices} from './Services';
import {Dialog} from './Components/Dialog';

/**
 * Компонент Realtime-чата.
 */
export const Chat = React.createClass({
    displayName: 'Chat',

    /**
     * Задержка перед началом поиска после окончания ввода пользователем поисковой фразы.
     */
    searchTimeout: 500,

    /**
     * Начальное состояние компонента.
     *
     * searchResult - результаты поиска (список найденных пользователей).
     * searchMode - флаг, указывающий на то, включен ли режим поиска.
     * searchTimer - переменная для хранения таймера от окончания ввода поисковой фразы до запуска поиска.
     * isLoading - флаг, указывающий на то, загружается ли список диалогов в настоящий момент.
     * activeDialog - активный диалог (по умолчанию диалог не выбран).
     * dialogs - список диалогов.
     * user - текущий пользователь.
     */
    getInitialState () {
        return {
            searchResult: null,
            searchMode: false,
            searchTimer: null,
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
     * Перемещение диалога вверх списка (происходит при отправке или поступлении нового сообщения в диалоге).
     */
    dialogOnTop () {
        const {activeDialog: {id: dialogId}, dialogs} = this.state;
        const targetDialog = remove(dialogs, dialog => dialog.id === dialogId);

        dialogs.unshift(targetDialog[0]);
        this.setState({dialogs});
    },

    /**
     * Осуществление поиска собеседников (по имени, либо по email).
     *
     * @param value Поисковая фраза.
     */
    search (value) {
        ChatServices.search(value).then(users => {
            this.setState({ searchResult: users });
        });
    },

    /**
     * Очистка результатов поиска.
     */
    clearSearchResult () {
        this.setState({ searchResult: null });
    },

    /**
     * Создание нового диалога.
     *
     * @param interlocutorId Идентификатор собеседника.
     */
    createDialog (interlocutorId) {
        return ChatServices.createDialog(interlocutorId);
    },

    /**
     * Добавление далога в список диалогов.
     *
     * @param dialog Добавляемый диалог.
     */
    addDialog (dialog) {
        let {dialogs} = this.state;

        dialogs.unshift(dialog);
        this.setState({ dialogs });
    },

    /**
     * Обработка активации режима поиска собеседников.
     * В режиме поиска в блоке со списком диалогов вместо заголовка показывается поле для ввода поисковой фразы.
     *
     * @param event Объект события Click.
     */
    handleOnSearch (event) {
        this.setState({ searchMode: true }, () => {
            const searchInput = findDOMNode(this.refs['dialogs-search-input']);
            searchInput.focus();
        });
        event !== undefined && event.preventDefault();
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
        const {activeDialog} = this.state;
        const isPseudoDialog = dialog.id < 0;

        if (activeDialog !== dialog) {
            if (isPseudoDialog) {
                this.createDialog(Math.abs(dialog.id)).then((dialog) => {
                    this.addDialog(dialog);
                    this.setState({ activeDialog: dialog, searchMode: false });
                    this.clearSearchResult();
                });
            } else {
                this.setState({ activeDialog: dialog });
            }
        }
        event.preventDefault();
    },

    /**
     * Обработка клика по иконке для закрытия поля поиска собеседников.
     *
     * @param event Объект события Click.
     */
    handleClickCloseSearchDialogs (event) {
        this.clearSearchResult();
        this.setState({ searchMode: false });
        event.preventDefault();
    },

    /**
     * Обработка изменения значения поля поиска собеседников.
     *
     * @param event Объект события Click.
     */
    handleChangeSearchPhrase (event) {
        const {searchTimer} = this.state;
        const value = event.target.value;

        searchTimer !== null && clearTimeout(searchTimer);
        if (!value) {
            this.clearSearchResult();
            return;
        }
        const timer = setTimeout(() => this.search(value), this.searchTimeout);
        this.setState({ searchTimer: timer });
    },

    /**
     * Рендеринг диалога в списке диалогов.
     *
     * @param dialog Объект диалога, который нужно отрендерить.
     */
    renderDialogItem (dialog) {
        const classes = classNames({
            "dialog-item": true,
            "dialog-item-active": dialog === this.state.activeDialog
        });

        return (
            <div
                className={classes}
                onClick={this.handleClickDialog.bind(this, dialog)}
                key={`dialog-${dialog.id}`}
            >
                <div className="dialog-user-picture">
                    <img src={dialog.interlocutorPicture} alt="" />
                </div>
                <div className="dialog-user-name">
                    {dialog.interlocutorName}
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
     * Рендеринг результатов поиска (списка пользователей).
     */
    renderSearchResult () {
        const {searchResult} = this.state;
        let dialogElements = [];

        searchResult.map(user => {
            const pseudoDialog = {
                id: -user.id,
                interlocutorId: user.id,
                interlocutorName: user.name,
                interlocutorPicture: user.picture
            };
            dialogElements.push(this.renderDialogItem(pseudoDialog));
        });

        return searchResult.length === 0 ? (
            <div className="dialogs-empty-search-result">
                По запросу пользователей не найдено.
            </div>
        ) : dialogElements;
    },

    /**
     * Рендеринг мини-блока с информацией о пользователе.
     */
    renderUserInfo () {
        const {user} = this.state;

        return user !== null && (
            <div className="user-info">
                <div className="user-info-avatar">
                    <img src={user.picture} alt="" />
                </div>
                <div className="user-info-name">{user.name}</div>
                <div className="user-info-logout">
                    <a href="/logout">Выйти</a>
                </div>
            </div>
        )
    },

    /**
     * Рендеринг верхней части блока со списком диалогов.
     */
    renderHeader () {
        const {searchMode} = this.state;

        return searchMode ? (
            <div className="dialogs-search-container">
                <input type="text" ref="dialogs-search-input" onChange={this.handleChangeSearchPhrase} className="dialogs-search" />
                <a href="#" onClick={this.handleClickCloseSearchDialogs} className="dialogs-search-close" />
            </div>
        ) : [
            <a href="#" onClick={this.handleOnSearch} className="dialogs-search-open" />,
            <h2>Диалоги</h2>
        ];
    },

    /**
     * Рендеринг компонента чата.
     * В зависимости от состояния загрузки рендерим либо лоадер, либо список диалогов.
     */
    render () {
        const {isLoading, dialogs, activeDialog, user, searchResult} = this.state;
        let content;

        if (isLoading) {
            content = <div className="dialogs-loading">Загрузка...</div>;
        } else if (dialogs !== null) {
            content = searchResult !== null ? this.renderSearchResult() : this.renderDialogs();
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
                <h1>Kotlin webdemo chat</h1>
                {this.renderUserInfo()}
                <div className="dialogs">
                    {this.renderHeader()}
                    {content}
                </div>
                <Dialog
                    dialog={activeDialog}
                    user={user}
                    dialogOnTop={this.dialogOnTop}
                    onSearch={this.handleOnSearch}
                />
            </div>
        );
    }
});
