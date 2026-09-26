import {
    LayoutDashboard,
    CarFront,
    Users,
    LogOut,
} from 'lucide-react';

import {
    NavLink,
} from 'react-router-dom';

import { useAuth } from '../../auth/AuthContext';

export default function Sidebar() {
    const {
        isAdmin,
        logout,
    } = useAuth();

    return (
        <aside className="sidebar">
            <div className="sidebar-brand">
                <div className="brand-mark brand-mark-light">
                    S
                </div>

                <div>
                    <strong>Salfa360</strong>
                    <span>Usados</span>
                </div>
            </div>

            <nav className="sidebar-nav">
        <span className="sidebar-section">
          PRINCIPAL
        </span>

                <NavLink
                    to="/dashboard"
                    className={({ isActive }) =>
                        `nav-item ${
                            isActive ? 'active' : ''
                        }`
                    }
                >
                    <LayoutDashboard size={19} />
                    Dashboard
                </NavLink>

                <NavLink
                    to="/vehicles"
                    className={({ isActive }) =>
                        `nav-item ${
                            isActive ? 'active' : ''
                        }`
                    }
                >
                    <CarFront size={19} />
                    Inventario
                </NavLink>

                {isAdmin && (
                    <>
            <span className="sidebar-section">
              ADMINISTRACIÓN
            </span>

                        <NavLink
                            to="/users"
                            className={({ isActive }) =>
                                `nav-item ${
                                    isActive ? 'active' : ''
                                }`
                            }
                        >
                            <Users size={19} />
                            Usuarios
                        </NavLink>
                    </>
                )}
            </nav>

            <div className="sidebar-footer">
                <button
                    className="sidebar-logout"
                    onClick={logout}
                >
                    <LogOut size={19} />
                    Cerrar sesión
                </button>
            </div>
        </aside>
    );
}