(function (global) {
    function resolveApiBaseUrl() {
        const override = global.__API_BASE_URL__ || localStorage.getItem("apiBaseUrl");
        if (override) {
            return override.replace(/\/$/, "");
        }

        const { origin, hostname, protocol } = global.location;

        if (hostname.endsWith(".app.github.dev") || hostname.endsWith(".githubpreview.dev")) {
            return origin
                .replace(/-(\d+)\.app\.github\.dev$/, "-8080.app.github.dev")
                .replace(/-(\d+)\.githubpreview\.dev$/, "-8080.githubpreview.dev") + "/api";
        }

        // Despliegue en Render: el frontend y el backend viven en dominios
        // .onrender.com distintos, así que apuntamos directo al backend.
        if (hostname.endsWith(".onrender.com")) {
            return "https://yakusabor-backend.onrender.com/api";
        }

        return `${protocol}//${hostname}:8080/api`;
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