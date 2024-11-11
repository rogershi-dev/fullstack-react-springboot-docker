import { useRoutes, Navigate } from 'react-router-dom';
import InitializationPage from './Initialization/InitializationPage';
import InitializeAdmin from './Initialization/InitializeAdmin';
import LoginPage from './auth/LoginPage';
import HomePage from './Home/HomePage';

export default function Router() {

    const routes = useRoutes([
        {
            path: '/',
            element: <InitializationPage />
        },
        {
            path: "/init/initialize-admin",
            element: <InitializeAdmin />
        },
        {
            path: "/auth/login",
            element: <LoginPage />
        },
        {
            path: "/home",
            element: <HomePage />
        },
        {
            path: "*",
            element: <Navigate to="/" replace />
        }
    ]);

    return routes;

}