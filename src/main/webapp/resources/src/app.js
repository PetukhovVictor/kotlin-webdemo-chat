import * as React from 'react';
import * as ReactDOM from 'react-dom';

import {Chat} from 'Modules/Chat'

require('Styles/app.less');
require('Styles/login.less');

window.addEventListener('load', () => {
    // Запуск рендеринга компонента чата.
    ReactDOM.render(
        React.createElement(Chat),
        document.getElementById('chat-container')
    );
});
