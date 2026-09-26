import { PublicClientApplication } from '@azure/msal-browser';

export const msalConfig = {
    auth: {
        clientId: import.meta.env.VITE_ENTRA_CLIENT_ID,

        authority:
            `https://login.microsoftonline.com/${import.meta.env.VITE_ENTRA_TENANT_ID}`,

        redirectUri: import.meta.env.VITE_ENTRA_REDIRECT_URI,

        postLogoutRedirectUri:
        import.meta.env.VITE_ENTRA_REDIRECT_URI,
    },

    cache: {
        cacheLocation: 'sessionStorage',
    },
};

export const loginRequest = {
    scopes: [
        `api://${import.meta.env.VITE_ENTRA_API_CLIENT_ID}/access_as_user`,
    ],
};

export const msalInstance =
    new PublicClientApplication(msalConfig);