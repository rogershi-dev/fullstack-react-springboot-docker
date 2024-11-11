import './InitializeAdmin.css';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import {  useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Alert from '@mui/material/Alert';


export default function InitializeAdmin() {
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

                if (!data.needsAdminInitialization) {
                    navigate("/auth/login");
                }

            } catch (error) {
                console.error("Error fetching initialization status:", error);
            }
        };

        fetchInitializationStatus();
    }, []);

    const [username, setUsername] = useState("");
    const [usernameError, setUsernameError] = useState("");
    const handleValidateUsername = (e) => {
        let usernameInput = e.target.value;
        setUsername(usernameInput);

        const regexp = /^[a-zA-Z0-9]{4,50}$/;
        if (!regexp.test(usernameInput)) {
            setUsernameError("Username must be 4-50 characters, letters and digits only.");
        } else {
            setUsernameError("");
        }
    };

    const [password, setPassword] = useState("");
    const [passwordError, setPasswordError] = useState("");
    const handleValidatePassword = (e) => {
        let passwordInput = e.target.value;
        setPassword(passwordInput);

        const regexp = /^[a-zA-Z0-9]{8,50}$/;
        if (!regexp.test(passwordInput)) {
            setPasswordError("Password must be 8-50 characters, letters and digits only.");
        } else {
            setPasswordError("");
        }

        if (passwordInput !== confirmedPassword) {
            setConfirmedPasswordError("Passwords do not match.");
        } else {
            setConfirmedPasswordError("");
        }
    };

    const [confirmedPassword, setConfirmedPassword] = useState("");
    const [confirmedPasswordError, setConfirmedPasswordError] = useState("");
    const handleValidateConfirmedPassword = (e) => {
        let confirmedPasswordInput = e.target.value;
        setConfirmedPassword(confirmedPasswordInput);

        if (confirmedPasswordInput !== password) {
            setConfirmedPasswordError("Passwords do not match.");
        } else {
            setConfirmedPasswordError("");
        }
    };

    const [alertMessage, setAlertMessage] = useState("");
    const [alertSeverity, setAlertSeverity] = useState("");
    const handleClearFormData = () => {
        setUsername("");
        setPassword("");
        setConfirmedPassword("");
        setUsernameError("");
        setPasswordError("");
        setConfirmedPasswordError("");
    };

    const handleNavigateToLoginPage = () => {
        setTimeout(() => {
            navigate("/auth/login");
        }, 1200);
    };

    const handleRegisterAdmin =  async () => {
        const payload = {
            "username": username,
            "rawPassword": confirmedPassword
        };

        const requestOptions = {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        };

        try {
            const response = await fetch(`${process.env.REACT_APP_BACKEND_URL}/application-setup/initialize-admin`, requestOptions);
            
            if (!response.ok) {
                setAlertSeverity("error");
                const errorData = await response.json();
                if (errorData.error) {
                    switch (errorData.error) {
                        case "ADMIN_ALREADY_INITIALIZED":
                            setAlertMessage("Admin already initialized.");
                            break;
                        case "USERNAME_ALREADY_EXISTS":
                            setAlertMessage("Admin username is taken.");
                            break;
                        case "FIELD_VALIDATION_FAILED":
                            setAlertMessage("Input error, please check your input.");
                            break;
                        case "DATA_INTEGRITY_VIOLATION":
                            setAlertMessage("Input error, please check your input.");
                            break;
                        default:
                            setAlertMessage("An unknown error occurred.");
                        
                    }

                } else {
                    setAlertMessage("An unknown error occurred.");
                }
                return;
            }

            setAlertSeverity("success");
            setAlertMessage("Admin initialized successfully");
            const data = await response.text();
            console.log("Admin initialized successfully: ", data);
            
            handleClearFormData();
            handleNavigateToLoginPage();

        } catch (error) {
            setAlertSeverity("error");
            console.error("An unknown error occurred: ", error);
            setAlertMessage("An unknown error occurred");
        }
    };


    return(
        <div className="init-admin-container">
            <Alert severity={alertSeverity}>{alertMessage}</Alert>
            <div className="init-admin-title">Admin account initialization</div>
            <TextField 
                label="username"
                margin="normal"
                fullWidth
                autoFocus
                value={username}
                onChange={handleValidateUsername}
                helperText={usernameError}
                error={!!usernameError}
            />

            <TextField 
                label="password"
                margin="normal"
                fullWidth
                type="password"
                value={password}
                onChange={handleValidatePassword}
                helperText={passwordError}
                error={!!passwordError}
            />

            <TextField 
                label="Confirm Password"
                margin="normal"
                fullWidth
                type="password"
                value={confirmedPassword}
                onChange={handleValidateConfirmedPassword}
                helperText={confirmedPasswordError}
                error={!!confirmedPasswordError}
            />

            <div className="init-admin-register-button-container">
            <Button 
                    variant="contained"
                    disabled={
                        username === ""
                        || usernameError !== ""
                        || password === ""
                        || passwordError !== ""
                        || confirmedPassword === ""
                        || confirmedPasswordError !== ""
                    }
                    onClick={handleRegisterAdmin}
                >
                    Register
                </Button>
            </div>
        </div>
    );
}
