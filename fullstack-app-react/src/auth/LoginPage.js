import './LoginPage.css';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import {  useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Alert from '@mui/material/Alert';
import { verifySession } from './SessionManagement';

export default function LoginPage() {
    const navigate = useNavigate();

    useEffect(() => {
        const checkSession = async () => {
            const isSessionValid = await verifySession();
            if (isSessionValid) {
                navigate("/home");
            }
        };

        checkSession();
    }, []);

    const [alertMessage, setAlertMessage] = useState("");
    const [alertSeverity, setAlertSeverity] = useState("");

    const [username, setUsername] = useState("");
    const [usernameError, setUsernameError] = useState("");
    const handleValidateUsername = (e) => {
        let usernameInput = e.target.value;
        setUsername(usernameInput);

        if (usernameInput === "" || usernameInput.trim() === "") {
            setUsernameError("Username cannot be empty or spaces only.");
        } else {
            setUsernameError("");
        }
    };

    const [password, setPassword] = useState("");
    const [passwordError, setPasswordError] = useState("");
    const handleValidatePassword = (e) => {
        let passwordInput = e.target.value;
        setPassword(passwordInput);

        if (passwordInput === "" || passwordInput.trim() === "") {
            setPasswordError("Password cannot be empty or spaces only.");
        } else {
            setPasswordError("");
        }
    };

    const handleClearFormData = () => {
        setUsername("");
        setPassword("");
        setUsernameError("");
        setPasswordError("");
    };

    const handleNavigateToHomePage = () => {
        setTimeout(() => {
            navigate("/home");
        }, 1500);
    };

    const handleLogin = async () => {
        const payload = {
            "username": username,
            "rawPassword": password
        };

        const requestOptions = {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        };

        try {
            const response = await fetch(`${process.env.REACT_APP_BACKEND_URL}/auth/login`, requestOptions);
            if (!response.ok) {
                setAlertSeverity("error");
                const errorData = await response.json();
                if (errorData.error) {
                    switch(errorData.error) {
                        case "INTERNAL_SERVER_ERROR":
                            setAlertMessage("Server is busy, try again later.");
                            break;
                        default:
                            setAlertMessage("Username or password invalid.");
                    }
                } else {
                    setAlertMessage("Server is busy, try again later.");
                }
                return;
            }

            setAlertSeverity("success");
            setAlertMessage("Logged in successfully.");
            const data = await response.json();
            localStorage.setItem("userData", JSON.stringify(data));

            handleClearFormData();
            handleNavigateToHomePage();

        } catch (error) {
            setAlertSeverity("error");
            setAlertMessage("Server is busy, try again later.");
        }
    };

    return(
        <div className="login-page-container">
            <Alert severity={alertSeverity}>{alertMessage}</Alert>
            <div className="login-page-title">Log In</div>
            <TextField 
                label="username"
                autoFocus
                fullWidth
                margin="normal"
                value={username}
                onChange={handleValidateUsername}
                helperText={usernameError}
                error={!!usernameError}
            />

            <TextField 
                label="password"
                fullWidth
                margin="normal"
                type="password"
                value={password}
                onChange={handleValidatePassword}
                helperText={passwordError}
                error={!!passwordError}
            />

            <div className="login-page-login-button-container">
                <Button
                    variant="contained"
                    disabled={
                        username === ""
                        || password === ""
                        || usernameError !== ""
                        || passwordError !== ""
                    }
                    onClick={handleLogin}
                >
                    Log In
                </Button>
            </div>

        </div>
    );
}