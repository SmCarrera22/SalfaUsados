import {
    InteractionRequiredAuthError,
} from '@azure/msal-browser';

import {
    loginRequest,
    msalInstance,
} from '../config/msal';

const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL ??
    'http://localhost:8080';

export class ApiError extends Error {
    constructor(message, status, data = null) {
        super(message);

        this.name = 'ApiError';
        this.status = status;
        this.data = data;
    }
}

async function getAccessToken() {
    const accounts =
        msalInstance.getAllAccounts();

    const account = accounts[0];

    if (!account) {
        throw new ApiError(
            'No existe una sesión activa.',
            401,
        );
    }

    try {
        const response =
            await msalInstance.acquireTokenSilent({
                ...loginRequest,
                account,
            });

        return response.accessToken;
    } catch (error) {
        if (
            error instanceof
            InteractionRequiredAuthError
        ) {
            await msalInstance.acquireTokenRedirect({
                ...loginRequest,
                account,
            });

            return null;
        }

        throw error;
    }
}

async function parseResponse(response) {
    if (response.status === 204) {
        return null;
    }

    const contentType =
        response.headers.get('content-type') ?? '';

    if (
        contentType.includes(
            'application/json',
        )
    ) {
        return response.json();
    }

    const text = await response.text();

    return text || null;
}

export async function apiRequest(
    path,
    options = {},
) {
    const accessToken =
        await getAccessToken();

    if (!accessToken) {
        return null;
    }

    const headers = new Headers(
        options.headers ?? {},
    );

    headers.set(
        'Authorization',
        `Bearer ${accessToken}`,
    );

    if (
        options.body &&
        !headers.has('Content-Type')
    ) {
        headers.set(
            'Content-Type',
            'application/json',
        );
    }

    const response = await fetch(
        `${API_BASE_URL}${path}`,
        {
            ...options,
            headers,
        },
    );

    const data =
        await parseResponse(response);

    if (!response.ok) {
        let message =
            `Error HTTP ${response.status}`;

        if (
            data &&
            typeof data === 'object'
        ) {
            message =
                data.message ??
                data.error ??
                message;
        } else if (
            typeof data === 'string' &&
            data.trim()
        ) {
            message = data;
        }

        throw new ApiError(
            message,
            response.status,
            data,
        );
    }

    return data;
}

export const vehicleApi = {
    getAll() {
        return apiRequest(
            '/api/vehicles',
        );
    },

    getById(id) {
        return apiRequest(
            `/api/vehicles/${id}`,
        );
    },

    create(vehicle) {
        return apiRequest(
            '/api/vehicles',
            {
                method: 'POST',
                body: JSON.stringify(vehicle),
            },
        );
    },

    update(id, vehicle) {
        return apiRequest(
            `/api/vehicles/${id}`,
            {
                method: 'PUT',
                body: JSON.stringify(vehicle),
            },
        );
    },

    remove(id) {
        return apiRequest(
            `/api/vehicles/${id}`,
            {
                method: 'DELETE',
            },
        );
    },
};

export const userApi = {
    getAll() {
        return apiRequest(
            '/api/users',
        );
    },

    getById(id) {
        return apiRequest(
            `/api/users/${id}`,
        );
    },

    create(user) {
        return apiRequest(
            '/api/users',
            {
                method: 'POST',
                body: JSON.stringify(user),
            },
        );
    },

    update(id, user) {
        return apiRequest(
            `/api/users/${id}`,
            {
                method: 'PUT',
                body: JSON.stringify(user),
            },
        );
    },

    setActive(id, active) {
        return apiRequest(
            `/api/users/${id}/status?active=${active}`,
            {
                method: 'PATCH',
            },
        );
    },
};