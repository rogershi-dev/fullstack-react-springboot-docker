
export async function verifySession() {
    const cachedUserData = localStorage.getItem("userData");
    
    if (!cachedUserData) return false;
    
    const parsedUserData = JSON.parse(cachedUserData);
    const token = parsedUserData.token;
    if (!token) return false;

    try {
        const requestOptions = {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`
            }
        };

        const response = await fetch(`${process.env.REACT_APP_BACKEND_URL}/auth/verify-token`, requestOptions);

        return response.ok;
    } catch (error) {
        return false;
    }
}