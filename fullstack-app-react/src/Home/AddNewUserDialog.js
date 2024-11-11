import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import TextField from '@mui/material/TextField';
import { useEffect, useState } from 'react';
import Alert from '@mui/material/Alert';


export default function AddNewUserDialog({ open, onClose }) {

    useEffect(() => {
        handleClearFormData();
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
        setAlertSeverity("");
        setAlertMessage("");
    };

    const handleCloseDialog = () => {
        handleClearFormData();
        onClose();
    
    };

    const handleRegisterUser =  async () => {

        try {
            const payload = {
                "username": username,
                "rawPassword": confirmedPassword
            };

            const userData = JSON.parse(localStorage.getItem("userData"));
            const token = userData.token;
    
            const requestOptions = {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
                body: JSON.stringify(payload)
            };


            const response = await fetch(`${process.env.REACT_APP_BACKEND_URL}/admin/add-new-user`, requestOptions);
            
            if (!response.ok) {
                setAlertSeverity("error");
                const errorData = await response.json();
                if (errorData.error) {
                    switch (errorData.error) {
                        case "USERNAME_ALREADY_EXISTS":
                            setAlertMessage("Username is taken.");
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
            setAlertMessage("User added successfully");
            const data = await response.text();
            console.log("User added successfully: ", data);
            
            setTimeout(() => {
                onClose();
            }, 1000);

            setTimeout(() => {
                handleClearFormData();
            }, 1003);
            

        } catch (error) {
            setAlertSeverity("error");
            console.error("An unknown error occurred: ", error);
            setAlertMessage("An unknown error occurred");
        }
    };

    return (
        <Dialog open={open} onClose={handleCloseDialog}>
            <Alert severity={alertSeverity}>{alertMessage}</Alert>
            <DialogTitle>Add new user</DialogTitle>
            <DialogContent>
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
            </DialogContent>
            <DialogActions>
                <Button onClick={handleCloseDialog}>Cancel</Button>
                <Button disabled={
                    username === ""
                    || usernameError !== ""
                    || password === ""
                    || passwordError !== ""
                    || confirmedPassword === ""
                    || confirmedPasswordError !== ""
                }
                onClick={handleRegisterUser}
                >Confirm</Button>
            </DialogActions>
        </Dialog>
    );
}