import './HomePage.css';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { verifySession } from '../auth/SessionManagement';
import SessionExpiredDialog from './SessionExpiredDialog';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import SettingsIcon from '@mui/icons-material/Settings';
import DevicesOtherIcon from '@mui/icons-material/DevicesOther';
import AddNewUserDialog from './AddNewUserDialog';
import Alert from '@mui/material/Alert';


export default function HomePage() {
    const navigate = useNavigate();

    const [openSessionExpiredDialog, setOpenSessionExpiredDialog] = useState(false);

    const handleNvigateToLoginPage = () => {
        setOpenSessionExpiredDialog(false);
        navigate("/auth/login");
    };

    useEffect(() => {
        const checkSession = async () => {
            const isSessionValid = await verifySession();
            if (!isSessionValid) {
                setOpenSessionExpiredDialog(true);
            }
        };

        checkSession();
    }, []);

    const [userData, setUserData] = useState({});
    useEffect(() => {
        try {
            const cachedUserData = localStorage.getItem("userData");
            setUserData(JSON.parse(cachedUserData));

        } catch (error) {
            console.error("Error: ", error);
        }

    }, []);

    const [openAddNewUserDialog, setOpenAddNewUserDialog] = useState(false);
    const handleCloseAddNewUserDialog = () => {
        setOpenAddNewUserDialog(false);
    };

    const [alertSeverity, setAlertSeverity] = useState("");
    const [alertMessage, setAlertMessage] = useState("");

    const handleLogout = async () => {

        try {
            const requestOptions = {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${userData.token}`
                },
            };

            const response = await fetch(`${process.env.REACT_APP_BACKEND_URL}/auth/logout`, requestOptions);
            
            if (!response.ok) {
                setAlertSeverity("error");
                setAlertMessage("An unknown error occurred");
                return;
            }

            localStorage.removeItem("userData");
            setAlertSeverity("success");
            setAlertMessage("Logged out successfully");
            
            setTimeout(() => {
                navigate("/auth/login");
            }, 1000);

        } catch (error) {
            setAlertSeverity("error");
            console.error("An unknown error occurred: ", error);
            setAlertMessage("An unknown error occurred");
        }
    };

    return(
        <div className="homepage-container">
            <SessionExpiredDialog 
                open={openSessionExpiredDialog}
                onLogin={handleNvigateToLoginPage}
            />
            <AddNewUserDialog 
                open={openAddNewUserDialog}
                onClose={handleCloseAddNewUserDialog}
            />
            <div className="homepage-account-info">
                <div className="homepage-account-info-username">{userData?.username || "Loading"}</div>
                <div className="homepage-account-info-role">
                    <span>{userData?.role || "Loading"}</span>
                    <div 
                        className="homepage-account-info-logout-button"
                        onClick={handleLogout}
                    >
                        Log out
                    </div>
                </div>
            </div>
            <Alert severity={alertSeverity}>{alertMessage}</Alert>

            <div className="homepage-operation-button-container">
                <div className="homepage-operation-button">
                    <AccountCircleIcon />
                    <span>My profile</span>
                </div>
                <div className="homepage-operation-button">
                    <DevicesOtherIcon />
                    <span>Active sessions</span>
                </div>
                {
                    userData && userData.role === "ADMIN" &&
                    <>
                        <div 
                            className="homepage-operation-button" 
                            onClick={() => setOpenAddNewUserDialog(true)}
                        >
                            <PersonAddIcon />
                            <span>Add new user</span>
                        </div>
                        <div className="homepage-operation-button">
                            <SettingsIcon />
                            <span>System settings</span>
                        </div>
                    </>
                }
            </div>
            
        </div>
    );
}
