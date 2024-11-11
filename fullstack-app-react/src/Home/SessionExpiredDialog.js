import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';

export default function SessionExpiredDialog({
    open,
    onLogin
}) {


    return(
        <Dialog open={open} >
            <DialogTitle style={{fontWeight:"bolder"}}>Session Expired</DialogTitle>
            <DialogContent>Your session has expired for security reasons. Please log in again to continue where you left off.</DialogContent>
            <DialogActions>
                <Button onClick={onLogin}>Log in</Button>
            </DialogActions>
        </Dialog>
    );
}