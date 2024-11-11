import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './InitializationPage.css';

export default function InitializationPage() {
    const navigate = useNavigate();

    useEffect(() => {
        const fetchInitializationStatus = async () => {
            try {
                const response = await fetch(`${process.env.REACT_APP_BACKEND_URL}/application-setup/get-initialization-status`);
                
                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.message);
                }

                const data = await response.json();

                if (data.needsAdminInitialization) {
                    navigate("/init/initialize-admin");
                } else {
                    navigate("/auth/login");
                }

            } catch (error) {
                console.error("Error fetching initialization status:", error);
                window.alert("Something went wrong. Please try again later.")
            }
        };

        fetchInitializationStatus();
    }, []);

    return(
        <div className="initialization-loading-text">Loading...</div>
    );
}