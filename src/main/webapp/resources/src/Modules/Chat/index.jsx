import * as React from 'react';
import PropTypes from 'prop-types';

import {ChatServices} from './Services';
import {Dialog} from './Components/Dialog';

export const Chat = React.createClass({
    displayName: 'Chat',

    /**
     * Начальное состояние компонента.
     *
     * isLoading - флаг, указывающий на то, загружается ли список диалогов в настоящий момент.
     * activeDialog - ID активного диалога (по умолчания не выбрано ниодного диалога).
     * dialogs - список диалогов.
     */
    getInitialState () {
        return {
            isLoading: true,
            activeDialog: 0,
            dialogs: null
        };
    },

    /**
     * После монтирования компонента стартуем загрузку списка диалогов.
     * По окочании загрузки - записываем результат в state (и далее отображаем диалоги).
     */
    componentDidMount () {
        ChatServices.loadDialogs().then(dialogs => {
            this.setState({
                isLoading: false,
                dialogs: dialogs
            });
        }).catch(() => {
            this.setState({
                isLoading: false
            });
        });
    },

    /**
     * В зависимости от состояния загрузки рендерим либо лоадер, либо компонент чата.
     *
     * @return {JSX.Element}
     */
    render () {
        const {isLoading, dialogs} = this.state;
        return (
            <div className="chat">
                <div className="dialogs">
                    <h1>Диалоги</h1>
                    {
                        isLoading ?
                            <div className="dialogs-loading">Загрузка...</div> :
                            "dialogs..."
                    }
                </div>
                <Dialog />
            </div>
        );
    }
});
