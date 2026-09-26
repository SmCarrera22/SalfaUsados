import {
    Navigate,
    Route,
    Routes,
} from 'react-router-dom';

import ProtectedRoute
    from './auth/ProtectedRoute';

import AppLayout
    from './components/layout/AppLayout';

import DashboardPage
    from './pages/DashboardPage';

import LoginPage
    from './pages/LoginPage';

import NotFoundPage
    from './pages/NotFoundPage';

import UnauthorizedPage
    from './pages/UnauthorizedPage';

import UsersPage
    from './pages/UsersPage';

import VehiclesPage
    from './pages/VehiclesPage';

import VehicleFormPage
    from './pages/VehicleFormPage';

import UserFormPage
    from './pages/UserFormPage';

import {
    useAuth,
} from './auth/AuthContext';

function LoginRoute() {
    const {
        isAuthenticated,
    } = useAuth();

    if (isAuthenticated) {
        return (
            <Navigate
                to="/dashboard"
                replace
            />
        );
    }

    return <LoginPage />;
}

function App() {
    return (
        <Routes>
            <Route
                path="/login"
                element={<LoginRoute />}
            />

            <Route
                path="/unauthorized"
                element={<UnauthorizedPage />}
            />

            <Route
                element={
                    <ProtectedRoute>
                        <AppLayout />
                    </ProtectedRoute>
                }
            >
                <Route
                    path="/dashboard"
                    element={<DashboardPage />}
                />

                <Route
                    path="/vehicles"
                    element={<VehiclesPage />}
                />

                <Route
                    path="/vehicles/new"
                    element={
                        <ProtectedRoute
                            allowedRoles={[
                                'ADMIN',
                                'OPERATOR',
                            ]}
                        >
                            <VehicleFormPage />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/vehicles/:id/edit"
                    element={
                        <ProtectedRoute
                            allowedRoles={[
                                'ADMIN',
                                'OPERATOR',
                            ]}
                        >
                            <VehicleFormPage />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/users"
                    element={
                        <ProtectedRoute
                            allowedRoles={[
                                'ADMIN',
                            ]}
                        >
                            <UsersPage />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/users/new"
                    element={
                        <ProtectedRoute
                            allowedRoles={[
                                'ADMIN',
                            ]}
                        >
                            <UserFormPage />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/users/:id/edit"
                    element={
                        <ProtectedRoute
                            allowedRoles={[
                                'ADMIN',
                            ]}
                        >
                            <UserFormPage />
                        </ProtectedRoute>
                    }
                />
            </Route>

            <Route
                path="/"
                element={
                    <Navigate
                        to="/dashboard"
                        replace
                    />
                }
            />

            <Route
                path="*"
                element={
                    <NotFoundPage />
                }
            />
        </Routes>
    );
}

export default App;