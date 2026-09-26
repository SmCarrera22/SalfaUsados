import {
    createContext,
    useCallback,
    useContext,
    useEffect,
    useMemo,
    useState,
} from 'react';

import { useMsal } from '@azure/msal-react';

import {
    InteractionRequiredAuthError,
    InteractionStatus,
} from '@azure/msal-browser';

import {
    loginRequest,
} from '../config/msal.js';

const AuthContext = createContext(null);

function decodeJwtPayload(token) {
    if (!token) {
        return null;
    }

    try {
        const parts = token.split('.');

        if (parts.length !== 3) {
            return null;
        }

        const base64Url = parts[1];

        const base64 = base64Url
            .replace(/-/g, '+')
            .replace(/_/g, '/');

        const padded = base64.padEnd(
            base64.length +
            ((4 - (base64.length % 4)) % 4),
            '=',
        );

        const binary = atob(padded);

        const bytes = Uint8Array.from(
            binary,
            (character) =>
                character.charCodeAt(0),
        );

        const json = new TextDecoder().decode(
            bytes,
        );

        return JSON.parse(json);
    } catch (error) {
        console.error(
            'No fue posible leer los claims del token:',
            error,
        );

        return null;
    }
}

export function AuthProvider({ children }) {
    const {
        instance,
        accounts,
        inProgress,
    } = useMsal();

    const account = accounts[0] ?? null;

    const [roles, setRoles] =
        useState([]);

    const [rolesLoading, setRolesLoading] =
        useState(false);

    const getAccessToken = useCallback(
        async () => {
            if (!account) {
                return null;
            }

            try {
                const response =
                    await instance.acquireTokenSilent({
                        ...loginRequest,
                        account,
                    });

                return response.accessToken;
            } catch (error) {
                if (
                    error instanceof
                    InteractionRequiredAuthError
                ) {
                    await instance.acquireTokenRedirect({
                        ...loginRequest,
                        account,
                    });

                    return null;
                }

                throw error;
            }
        },
        [account, instance],
    );

    useEffect(() => {
        async function loadRoles() {
            if (
                !account ||
                inProgress !== InteractionStatus.None
            ) {
                setRoles([]);
                return;
            }

            setRolesLoading(true);

            try {
                const accessToken =
                    await getAccessToken();

                if (!accessToken) {
                    setRoles([]);
                    return;
                }

                const claims =
                    decodeJwtPayload(accessToken);

                const tokenRoles =
                    Array.isArray(claims?.roles)
                        ? claims.roles
                        : [];

                setRoles(tokenRoles);
            } catch (error) {
                console.error(
                    'No fue posible cargar los roles:',
                    error,
                );

                setRoles([]);
            } finally {
                setRolesLoading(false);
            }
        }

        loadRoles();
    }, [
        account,
        getAccessToken,
        inProgress,
    ]);

    const login = useCallback(
        async () => {
            if (
                inProgress !==
                InteractionStatus.None
            ) {
                return;
            }

            await instance.loginRedirect(
                loginRequest,
            );
        },
        [inProgress, instance],
    );

    const logout = useCallback(
        async () => {
            if (
                inProgress !==
                InteractionStatus.None
            ) {
                return;
            }

            await instance.logoutRedirect({
                account,
            });
        },
        [
            account,
            inProgress,
            instance,
        ],
    );

    const value = useMemo(
        () => ({
            account,
            roles,
            rolesLoading,

            isAuthenticated:
                Boolean(account),

            isAdmin:
                roles.includes('ADMIN'),

            isOperator:
                roles.includes('OPERATOR'),

            isAnalyst:
                roles.includes('ANALYST'),

            hasRole: (role) =>
                roles.includes(role),

            getAccessToken,
            login,
            logout,
        }),
        [
            account,
            roles,
            rolesLoading,
            getAccessToken,
            login,
            logout,
        ],
    );

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context =
        useContext(AuthContext);

    if (!context) {
        throw new Error(
            'useAuth debe utilizarse dentro de AuthProvider.',
        );
    }

    return context;
}