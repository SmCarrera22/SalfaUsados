import { useState } from 'react';
import { useMsal } from '@azure/msal-react';
import {
    InteractionRequiredAuthError,
} from '@azure/msal-browser';

import { loginRequest } from './config/msal';

function App() {
    const { instance, accounts } = useMsal();
    const account = accounts[0];

    const [vehicles, setVehicles] = useState([]);
    const [users, setUsers] = useState([]);
    const [message, setMessage] = useState('');
    const [testVehicleId, setTestVehicleId] = useState('');

    async function handleLogin() {
        await instance.loginRedirect(loginRequest);
    }

    async function handleLogout() {
        await instance.logoutRedirect({
            account,
        });
    }

    async function getAccessToken() {
        if (!account) {
            throw new Error('No existe una sesión activa.');
        }

        try {
            const tokenResponse = await instance.acquireTokenSilent({
                ...loginRequest,
                account,
            });

            return tokenResponse.accessToken;
        } catch (error) {
            if (error instanceof InteractionRequiredAuthError) {
                await instance.acquireTokenRedirect({
                    ...loginRequest,
                    account,
                });

                return null;
            }

            throw error;
        }
    }

    async function handleLoadVehicles() {
        try {
            setMessage('');

            const accessToken = await getAccessToken();

            if (!accessToken) {
                return;
            }

            const response = await fetch(
                'http://localhost:8080/api/vehicles',
                {
                    headers: {
                        Authorization: `Bearer ${accessToken}`,
                    },
                },
            );

            console.log(
                'GET vehicles status:',
                response.status,
            );

            if (!response.ok) {
                setVehicles([]);

                setMessage(
                    `GET /api/vehicles → HTTP ${response.status}`,
                );

                return;
            }

            const data = await response.json();

            setVehicles(data);

            setMessage(
                `GET /api/vehicles → HTTP ${response.status}`,
            );
        } catch (error) {
            console.error(error);

            setMessage(
                `Error: ${error.message}`,
            );
        }
    }

    async function handleLoadUsers() {
        try {
            setMessage('');

            const accessToken = await getAccessToken();

            if (!accessToken) {
                return;
            }

            const response = await fetch(
                'http://localhost:8080/api/users',
                {
                    headers: {
                        Authorization: `Bearer ${accessToken}`,
                    },
                },
            );

            console.log(
                'GET users status:',
                response.status,
            );

            if (!response.ok) {
                setUsers([]);

                setMessage(
                    `GET /api/users → HTTP ${response.status}`,
                );

                return;
            }

            const data = await response.json();

            setUsers(data);

            setMessage(
                `GET /api/users → HTTP ${response.status}`,
            );
        } catch (error) {
            console.error(error);

            setMessage(
                `Error: ${error.message}`,
            );
        }
    }

    async function handleCreateTestVehicle() {
        try {
            setMessage('');

            const accessToken = await getAccessToken();

            if (!accessToken) {
                return;
            }

            const testVehicle = {
                vin: 'TESTSALFA36000001',
                plate: 'TEST02',
                brand: 'Chevrolet',
                model: 'Tracker',
                version: 'Test RBAC',
                year: 2025,
                mileage: 10000,
                color: 'Blanco',
                fuelType: 'Gasolina',
                branch: 'Movicenter',
                status: 'AVAILABLE',
            };

            const response = await fetch(
                'http://localhost:8080/api/vehicles',
                {
                    method: 'POST',
                    headers: {
                        Authorization: `Bearer ${accessToken}`,
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(testVehicle),
                },
            );

            console.log(
                'POST vehicle status:',
                response.status,
            );

            if (!response.ok) {
                setMessage(
                    `POST /api/vehicles → HTTP ${response.status}`,
                );

                return;
            }

            const data = await response.json();

            console.log(
                'Vehículo de prueba creado:',
                data,
            );

            setMessage(
                `POST /api/vehicles → HTTP ${response.status}`,
            );
        } catch (error) {
            console.error(error);

            setMessage(
                `Error: ${error.message}`,
            );
        }
    }

    async function handleDeleteTestVehicle() {
        try {
            setMessage('');

            if (!testVehicleId) {
                setMessage('Debes ingresar el ID del vehículo de prueba.');
                return;
            }

            const accessToken = await getAccessToken();

            if (!accessToken) {
                return;
            }

            const response = await fetch(
                `http://localhost:8080/api/vehicles/${testVehicleId}`,
                {
                    method: 'DELETE',
                    headers: {
                        Authorization: `Bearer ${accessToken}`,
                    },
                },
            );

            console.log(
                'DELETE vehicle status:',
                response.status,
            );

            setMessage(
                `DELETE /api/vehicles/${testVehicleId} → HTTP ${response.status}`,
            );

            if (response.ok) {
                setVehicles((currentVehicles) =>
                    currentVehicles.filter(
                        (vehicle) =>
                            String(vehicle.id) !== String(testVehicleId),
                    ),
                );
            }
        } catch (error) {
            console.error(error);

            setMessage(
                `Error: ${error.message}`,
            );
        }
    }

    if (!account) {
        return (
            <main>
                <h1>Salfa360</h1>

                <p>
                    Plataforma de gestión de vehículos usados
                </p>

                <button onClick={handleLogin}>
                    Iniciar sesión con Microsoft
                </button>
            </main>
        );
    }

    return (
        <main>
            <h1>Salfa360</h1>

            <p>
                Sesión iniciada correctamente.
            </p>

            <p>
                Usuario: {account.username}
            </p>

            <div>
                <button onClick={handleLoadVehicles}>
                    Cargar vehículos desde BFF
                </button>

                <button onClick={handleLoadUsers}>
                    Cargar usuarios desde BFF
                </button>

                <button onClick={handleCreateTestVehicle}>
                    Crear vehículo de prueba
                </button>

                <button onClick={handleLogout}>
                    Cerrar sesión
                </button>
            </div>

            <div>
                <input
                    type="number"
                    placeholder="ID vehículo de prueba"
                    value={testVehicleId}
                    onChange={(event) =>
                        setTestVehicleId(event.target.value)
                    }
                />

                <button onClick={handleDeleteTestVehicle}>
                    Eliminar vehículo de prueba
                </button>
            </div>

            {message && (
                <p>
                    <strong>{message}</strong>
                </p>
            )}

            {vehicles.length > 0 && (
                <>
                    <h2>Vehículos</h2>

                    <pre>
            {JSON.stringify(vehicles, null, 2)}
          </pre>
                </>
            )}

            {users.length > 0 && (
                <>
                    <h2>Usuarios</h2>

                    <pre>
            {JSON.stringify(users, null, 2)}
          </pre>
                </>
            )}
        </main>
    );
}

export default App;