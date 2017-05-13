import * as React from 'react';
import * as ReactDOM from 'react-dom';

import {Chat} from 'Modules/Chat'

require('Styles/app.less');
require('Styles/login.less');

window.addEventListener('load', () => {
    const chatComponent = React.createElement(Chat);

    // Запуск рендеринга компонента чата.
    ReactDOM.render(
        chatComponent,
        document.getElementById('chat-container')
    );
});
