import * as React from 'react';
import * as ReactDOM from 'react-dom';

require('Styles/app.less');
require('Styles/login.less');

window.addEventListener('load', () => {
    const chatComponent = React.createElement();

    // Запуск рендеринга компонента для демонстрации древовидного списка.
    ReactDOM.render(
        chatComponent,
        document.getElementById('chat-container')
    );
});
