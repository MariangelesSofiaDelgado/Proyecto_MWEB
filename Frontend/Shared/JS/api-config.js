(function (global) {
    function resolveApiBaseUrl() {
        // FIJO para la presentación: el backend siempre vive aquí.
        return "https://yakusabor-backend.onrender.com/api";
    }

    function authHeaders() {
        const token = sessionStorage.getItem("token");
        return {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        };
    }

    function clearSession() {
        sessionStorage.removeItem("token");
        sessionStorage.removeItem("rol");
        sessionStorage.removeItem("nombre");
    }

    global.API_BASE_URL = resolveApiBaseUrl();
    global.resolveApiBaseUrl = resolveApiBaseUrl;
    global.authHeaders = authHeaders;
    global.clearSession = clearSession;
})(window);