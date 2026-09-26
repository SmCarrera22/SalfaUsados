import { Navigate } from 'react-router-dom';

import { useAuth } from './AuthContext';

export default function ProtectedRoute({
                                           children,
                                           allowedRoles,
                                       }) {
    const {
        isAuthenticated,
        roles,
    } = useAuth();

    if (!isAuthenticated) {
        return (
            <Navigate
                to="/login"
                replace
            />
        );
    }

    if (
        allowedRoles &&
        !allowedRoles.some((role) =>
            roles.includes(role),
        )
    ) {
        return (
            <Navigate
                to="/unauthorized"
                replace
            />
        );
    }

    return children;
}