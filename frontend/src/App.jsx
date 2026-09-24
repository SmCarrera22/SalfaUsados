import { useEffect, useState } from 'react';
import {
  fetchAuthSession,
  getCurrentUser,
  signInWithRedirect,
  signOut,
} from 'aws-amplify/auth';

function App() {
  const [user, setUser] = useState(null);
  const [claims, setClaims] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadSession();
  }, []);

  async function loadSession() {
    try {
      const currentUser = await getCurrentUser();
      const session = await fetchAuthSession();

      setUser(currentUser);
      setClaims(session.tokens?.accessToken?.payload ?? null);
    } catch {
      setUser(null);
      setClaims(null);
    } finally {
      setLoading(false);
    }
  }

  async function handleLogin() {
    await signInWithRedirect();
  }

  async function handleLogout() {
    await signOut();
  }

  if (loading) {
    return <p>Cargando...</p>;
  }

  if (!user) {
    return (
        <main>
          <h1>Salfa360</h1>
          <p>Plataforma de gestión de vehículos usados</p>

          <button onClick={handleLogin}>
            Iniciar sesión
          </button>
        </main>
    );
  }

  return (
      <main>
        <h1>Salfa360</h1>

        <p>Sesión iniciada correctamente.</p>

        <button onClick={handleLogout}>
          Cerrar sesión
        </button>

        <h2>Claims del Access Token</h2>

        <pre>
        {JSON.stringify(claims, null, 2)}
      </pre>
      </main>
  );
}

export default App;