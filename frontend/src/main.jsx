import { StrictMode } from 'react';

import {
    createRoot,
} from 'react-dom/client';

import {
    MsalProvider,
} from '@azure/msal-react';

import {
    BrowserRouter,
} from 'react-router-dom';

import App from './App.jsx';

import {
    AuthProvider,
} from './auth/AuthContext.jsx';

import {
    msalInstance,
} from './config/msal.js';

import './index.css';
import './styles/app.css';

async function bootstrap() {
    await msalInstance.initialize();

    createRoot(
        document.getElementById('root'),
    ).render(
        <StrictMode>
            <MsalProvider
                instance={msalInstance}
            >
                <AuthProvider>
                    <BrowserRouter>
                        <App />
                    </BrowserRouter>
                </AuthProvider>
            </MsalProvider>
        </StrictMode>,
    );
}

bootstrap();