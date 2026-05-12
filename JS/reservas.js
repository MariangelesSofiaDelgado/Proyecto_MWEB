const TOTAL_MESAS = 12;

const API_CONFIG = {
    // agregar conexion con backend aqui: configurar URL real del servicio de reservas en producción
    baseUrl: window.RESERVAS_API_BASE_URL || "http://localhost:3000/api",
    estadoMesasPath: "/mesas/estado",
    reservarPath: "/reservas"
};

const reservaModal = document.getElementById("reservaMesasModal");
const mesasGrid = document.getElementById("mesasGrid");
const mesaSeleccionadaTexto = document.getElementById("mesaSeleccionadaTexto");
const confirmarReservaBtn = document.getElementById("confirmarReservaBtn");
const reservaFeedback = document.getElementById("reservaFeedback");

let mesasEstado = [];
let mesaSeleccionada = null;

const getMockMesasEstado = () =>
    // agregar conexion con backend aqui: este estado es temporal mientras no responda la API de mesas
    Array.from({ length: TOTAL_MESAS }, (_, index) => ({
        id: index + 1,
        libre: Math.random() > 0.35
    }));

const normalizeMesasEstado = (payload) => {
    if (!Array.isArray(payload)) {
        return getMockMesasEstado();
    }

    const normalized = payload
        .map((mesa) => ({
            id: Number(mesa.id),
            libre: Boolean(mesa.libre)
        }))
        .filter((mesa) => Number.isInteger(mesa.id) && mesa.id >= 1 && mesa.id <= TOTAL_MESAS);

    if (normalized.length === TOTAL_MESAS) {
        return normalized.sort((a, b) => a.id - b.id);
    }

    const map = new Map(normalized.map((mesa) => [mesa.id, mesa.libre]));
    return Array.from({ length: TOTAL_MESAS }, (_, index) => {
        const id = index + 1;
        return {
            id,
            libre: map.has(id) ? map.get(id) : true
        };
    });
};

const fetchMesasEstado = async () => {
    // agregar conexion con la API de mesas aqui: consumir disponibilidad en tiempo real
    const response = await fetch(`${API_CONFIG.baseUrl}${API_CONFIG.estadoMesasPath}`);
    if (!response.ok) {
        throw new Error("No se pudo obtener el estado de las mesas.");
    }
    const data = await response.json();
    return normalizeMesasEstado(data);
};

const enviarReserva = async (mesaId) => {
    // agregar conexion con la API de reservas aqui: registrar cliente, fecha y turno seleccionado
    const response = await fetch(`${API_CONFIG.baseUrl}${API_CONFIG.reservarPath}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ mesaId })
    });

    if (!response.ok) {
        throw new Error("No se pudo registrar la reserva.");
    }

    return response.json();
};

const actualizarSeleccionUI = () => {
    const mesaBotones = mesasGrid.querySelectorAll(".mesa-btn");
    mesaBotones.forEach((boton) => {
        const mesaId = Number(boton.dataset.mesaId);
        boton.classList.toggle("mesa-seleccionada", mesaSeleccionada === mesaId);
    });

    if (mesaSeleccionada) {
        mesaSeleccionadaTexto.textContent = `Mesa seleccionada: ${mesaSeleccionada}`;
        confirmarReservaBtn.disabled = false;
    } else {
        mesaSeleccionadaTexto.textContent = "Ninguna mesa seleccionada.";
        confirmarReservaBtn.disabled = true;
    }
};

const renderMesas = () => {
    mesasGrid.innerHTML = "";

    mesasEstado.forEach((mesa) => {
        const btn = document.createElement("button");
        btn.type = "button";
        btn.className = `mesa-btn ${mesa.libre ? "mesa-libre" : "mesa-ocupada"}`;
        btn.dataset.mesaId = mesa.id;
        btn.textContent = `Mesa ${mesa.id}`;
        btn.disabled = !mesa.libre;

        btn.addEventListener("click", () => {
            mesaSeleccionada = mesaSeleccionada === mesa.id ? null : mesa.id;
            actualizarSeleccionUI();
        });

        mesasGrid.appendChild(btn);
    });

    actualizarSeleccionUI();
};

const mostrarFeedback = (texto, tipo = "info") => {
    reservaFeedback.className = `alert alert-${tipo} py-2 mb-3`;
    reservaFeedback.textContent = texto;
    reservaFeedback.classList.remove("d-none");
};

const ocultarFeedback = () => {
    reservaFeedback.className = "alert alert-info py-2 d-none mb-3";
    reservaFeedback.textContent = "";
};

const cargarEstadoMesas = async () => {
    ocultarFeedback();
    mesaSeleccionada = null;

    try {
        mesasEstado = await fetchMesasEstado();
    } catch {
        mesasEstado = getMockMesasEstado();
        mostrarFeedback("Backend no conectado: mostrando datos locales temporales.", "warning");
    }

    renderMesas();
};

if (reservaModal && mesasGrid && mesaSeleccionadaTexto && confirmarReservaBtn) {
    reservaModal.addEventListener("show.bs.modal", cargarEstadoMesas);

    confirmarReservaBtn.addEventListener("click", async () => {
        if (!mesaSeleccionada) return;

        confirmarReservaBtn.disabled = true;

        try {
            await enviarReserva(mesaSeleccionada);
            mostrarFeedback(`Reserva confirmada para la mesa ${mesaSeleccionada}.`, "success");
            const mesaActual = mesasEstado.find((mesa) => mesa.id === mesaSeleccionada);
            if (mesaActual) {
                mesaActual.libre = false;
            }
            mesaSeleccionada = null;
            renderMesas();
        } catch {
            mostrarFeedback(
                "No se pudo confirmar en el backend. La interfaz quedó lista para conectarse cuando el API esté activo.",
                "warning"
            );
            mesaSeleccionada = null;
            actualizarSeleccionUI();
        } finally {
            confirmarReservaBtn.disabled = false;
        }
    });
}
