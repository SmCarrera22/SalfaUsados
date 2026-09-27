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
import { vehicleApi, userApi } from './api/apiClient';

async function bootstrap() {
    await msalInstance.initialize();

    // Utilidades para pruebas de autorización EP1
    window.salfa360Test = {
        getVehicles: () => vehicleApi.getAll(),
        getUsers: () => userApi.getAll(),
        createVehicle: (vehicle) => vehicleApi.create(vehicle),
        updateVehicle: (id, vehicle) => vehicleApi.update(id, vehicle),
        deleteVehicle: (id) => vehicleApi.remove(id),
    };

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