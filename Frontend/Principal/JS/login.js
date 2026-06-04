document.addEventListener("DOMContentLoaded", () => {
    const publicHomePath = "../../Principal/HTML/index.html";

    const clearSession = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("rol");
        localStorage.removeItem("nombre");
    };

    const setupPublicSessionUI = () => {
        const navSessionButton = document.querySelector(".nav-register-btn");
        if (!navSessionButton) return;

        const token = localStorage.getItem("token");
        const nombre = localStorage.getItem("nombre");
        const label = navSessionButton.querySelector("span");

        if (!token) {
            if (label) label.textContent = "Inicia Sesión";
            navSessionButton.setAttribute("data-bs-toggle", "modal");
            navSessionButton.setAttribute("data-bs-target", "#registroModal");
            return;
        }

        if (label) {
            label.textContent = nombre ? `${nombre} | Cerrar sesión` : "Cerrar sesión";
        }

        navSessionButton.removeAttribute("data-bs-toggle");
        navSessionButton.removeAttribute("data-bs-target");
        navSessionButton.setAttribute("aria-label", "Cerrar sesión");
        navSessionButton.addEventListener("click", (event) => {
            event.preventDefault();
            clearSession();
            window.location.href = publicHomePath;
        });
    };

    const switchModal = (fromModalId, toModalId) => {
        if (typeof bootstrap === "undefined") return;

        const fromModalElement = document.getElementById(fromModalId);
        const toModalElement = document.getElementById(toModalId);

        if (!fromModalElement || !toModalElement) return;

        const fromModal = bootstrap.Modal.getOrCreateInstance(fromModalElement);
        const toModal = bootstrap.Modal.getOrCreateInstance(toModalElement);

        const handleHidden = () => {
            fromModalElement.removeEventListener("hidden.bs.modal", handleHidden);
            toModal.show();
        };

        fromModalElement.addEventListener("hidden.bs.modal", handleHidden);
        fromModal.hide();
    };

    const linkToRegister = document.getElementById("linkToRegister");
    if (linkToRegister) {
        linkToRegister.addEventListener("click", (event) => {
            event.preventDefault();
            switchModal("registroModal", "crearCuentaModal");
        });
    }

    const linkToLogin = document.getElementById("linkToLogin");
    if (linkToLogin) {
        linkToLogin.addEventListener("click", (event) => {
            event.preventDefault();
            switchModal("crearCuentaModal", "registroModal");
        });
    }

    const loginForm = document.getElementById("loginForm")
        || document.getElementById("exampleInputEmail1")?.closest("form");

    if (loginForm) {
        loginForm.addEventListener("submit", async (e) => {
            e.preventDefault(); // Evita que la página se recargue al enviar el formulario

            // 2. Capturamos los valores usando los IDs que ya tienes en tu HTML
            const email = document.getElementById("exampleInputEmail1").value;
            const password = document.getElementById("exampleInputPassword1").value;

            try {
                // 3. Hacemos la petición POST al futuro backend de Spring Boot
                // Nota: Asumimos que Spring Boot correrá en localhost:8080
                const response = await fetch("http://localhost:8080/api/auth/login", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({ email: email, password: password }),
                });

                // 4. Verificamos si la respuesta es exitosa (código 200 OK)
                if (response.ok) {
                    const data = await response.json();

                    // 5. Guardamos el Token y el Rol en el LocalStorage
                    // (Asumimos que el backend nos devolverá un token y el rol del usuario)
                    localStorage.setItem("token", data.token);
                    localStorage.setItem("rol", data.rol);
                    localStorage.setItem("nombre", data.nombre);
                    setupPublicSessionUI();

                    alert(`¡Bienvenido/a, ${data.nombre}!`);
                    const rolAsignado = data.rol ? data.rol.trim().toLowerCase() : "";
                    console.log("Rol recibido desde el backend:", rolAsignado); // <-- Para ver qué llega exactamente

                    // 6. Redirección basada en el Rol (RBAC)
                    if (rolAsignado === "cliente" || rolAsignado.includes("cliente")) {
                        // Si es cliente, se queda en la vista pública (o se recarga para actualizar el menú)
                        window.location.href = "../../Principal/HTML/index.html";
                    } else {
                        // Si es admin, mesero, cocina o caja, va al Dashboard
                        window.location.href = "../../Dashboard/HTML/Dashboard.html";
                    }
                } else {
                    // Si el backend responde con error (ej. 401 Unauthorized)
                    alert(
                        "Correo o contraseña incorrectos. Por favor, intenta de nuevo.",
                    );
                }
            } catch (error) {
                // Si el servidor de Spring Boot está apagado o hay un error de red
                console.error("Error de conexión:", error);
                alert(
                    "No se pudo conectar con el servidor. Verifica que el backend esté encendido.",
                );
            }
        });
    }

    const registerForm = document.getElementById("registerForm");

    if (registerForm) {
        registerForm.addEventListener("submit", async (e) => {
            e.preventDefault();

            const nombre = document.getElementById("regNombre").value;
            const email = document.getElementById("regEmail").value;
            const password = document.getElementById("regPassword").value;
            const confirmPassword =
                document.getElementById("regConfirmPassword").value;

            // 1. Validar que las contraseñas sean iguales
            if (password !== confirmPassword) {
                alert("Las contraseñas no coinciden. Inténtalo de nuevo.");
                return; // Detiene la ejecución
            }

            try {
                // 2. Enviar datos al futuro backend en Spring Boot
                const response = await fetch(
                    "http://localhost:8080/api/auth/registro",
                    {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json",
                        },
                        // El backend le asignará el rol 'cliente' por defecto a este nuevo usuario
                        body: JSON.stringify({
                            nombre: nombre,
                            email: email,
                            password: password,
                        }),
                    },
                );

                if (response.ok) {
                    alert("¡Cuenta creada con éxito! Ahora puedes iniciar sesión.");

                    // Limpiar formulario
                    registerForm.reset();

                    switchModal("crearCuentaModal", "registroModal");
                } else {
                    // Posibles errores: el correo ya existe, etc.
                    const errorData = await response.text();
                    alert(`Error al registrarse: ${errorData}`);
                }
            } catch (error) {
                console.error("Error de conexión:", error);
                alert("No se pudo conectar con el servidor para el registro.");
            }
        });
    }

    setupPublicSessionUI();
});
