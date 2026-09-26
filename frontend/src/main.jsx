import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { MsalProvider } from '@azure/msal-react';

import App from './App.jsx';
import { msalInstance } from './config/msal.js';

import './index.css';

async function bootstrap() {
    await msalInstance.initialize();

    createRoot(document.getElementById('root')).render(
        <StrictMode>
            <MsalProvider instance={msalInstance}>
                <App />
            </MsalProvider>
        </StrictMode>,
    );
}

bootstrap();