import * as React from 'react';

import {ChatServices} from '../Services'

export const Dialog = React.createClass({
    displayName: 'Chat.Dialog',

    /**
     * @return {JSX.Element}
     */
    render () {
        return (
            <div className="dialog">
                <div className="dialog-empty">Для начала переписки выберите диалог или <a href="">создайте новый</a>.</div>
            </div>
        );
    }
});
