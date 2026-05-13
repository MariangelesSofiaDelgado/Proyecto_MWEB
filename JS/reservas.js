const TOTAL_MESAS_MOCK = 4;

const API_CONFIG = {
    baseUrl: window.RESERVAS_API_BASE_URL || "http://localhost:8080/api",
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
    Array.from({ length: TOTAL_MESAS_MOCK }, (_, index) => ({
        id: index + 1,
        codigo: `M0${index + 1}`,
        ubicacion: index < 2 ? "interior" : "exterior",
        estado: index === 1 ? "ocupada" : "libre",
        libre: Math.random() > 0.35
    }));

const normalizeMesasEstado = (payload) => {
    if (!Array.isArray(payload)) {
        return getMockMesasEstado();
    }

    const normalized = payload
        .map((mesa) => ({
            id: Number(mesa.id),
            codigo: mesa.codigo || `M${String(mesa.id).padStart(2, "0")}`,
            ubicacion: mesa.ubicacion || "",
            estado: mesa.estado || (mesa.libre ? "libre" : "ocupada"),
            libre: Boolean(mesa.libre)
        }))
        .filter((mesa) => Number.isInteger(mesa.id) && mesa.id >= 1)
        .sort((a, b) => a.id - b.id);

    return normalized.length > 0 ? normalized : getMockMesasEstado();
};

const fetchMesasEstado = async () => {
    const response = await fetch(`${API_CONFIG.baseUrl}${API_CONFIG.estadoMesasPath}`);
    if (!response.ok) {
        throw new Error("No se pudo obtener el estado de las mesas.");
    }
    const data = await response.json();
    return normalizeMesasEstado(data);
};

const enviarReserva = async (mesaId) => {
    const response = await fetch(`${API_CONFIG.baseUrl}${API_CONFIG.reservarPath}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ mesaId })
    });

    if (!response.ok) {
        const errorMessage = await response.text();
        throw new Error(errorMessage || "No se pudo registrar la reserva.");
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
        btn.textContent = mesa.codigo || `Mesa ${mesa.id}`;
        btn.title = `${mesa.ubicacion || "sin ubicación"} - ${mesa.estado || ""}`;
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
        } catch (error) {
            mostrarFeedback(
                error.message || "No se pudo confirmar la reserva. Verifica que el backend esté encendido.",
                "warning"
            );
            mesaSeleccionada = null;
            actualizarSeleccionUI();
        } finally {
            confirmarReservaBtn.disabled = false;
        }
    });
}
