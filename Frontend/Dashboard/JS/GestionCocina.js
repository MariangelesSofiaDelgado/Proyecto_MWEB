// ==========================================
// GestionCocina.js — Vista del chef
// Carga pedidos activos agrupados por mesa,
// permite marcar cada plato individualmente.
// ==========================================

const API_BASE   = "http://localhost:8080/api";
const PEDIDOS_URL = `${API_BASE}/pedidos`;

const mesasContainer = document.getElementById("mesasContainer");
const sinPedidos     = document.getElementById("sinPedidos");
const ultimaAct      = document.getElementById("ultimaActualizacion");

let actualizando = false;

// ── Helpers ────────────────────────────────────────────────────────────────

function authHeaders() {
    const token = localStorage.getItem("token");
    return {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
    };
}

function etiquetaEstado(estado) {
    const mapa = {
        pendiente:      "Pendiente",
        en_preparacion: "Preparando",
        listo:          "Listo ✓",
        entregado:      "Entregado",
    };
    return mapa[estado] || estado;
}

function claseBadge(estado) {
    return `estado-badge estado-${estado}`;
}

// ── Renderizado ─────────────────────────────────────────────────────────────

function renderCocina(pedidos) {
    // Solo pedidos que aún no fueron cancelados ni facturados
    const activos = pedidos.filter(
        (p) => !["cancelado", "facturado"].includes(p.estado)
    );

    if (activos.length === 0) {
        mesasContainer.innerHTML = "";
        sinPedidos.style.display = "block";
        return;
    }
    sinPedidos.style.display = "none";

    // Agrupar detalles por mesa (o Delivery si no hay mesa)
    const grupos = {};
    activos.forEach((pedido) => {
        const key = pedido.mesaCodigo
            ? `Mesa ${pedido.mesaCodigo}`
            : pedido.direccionDelivery
                ? `🛵 Delivery`
                : `Pedido #${pedido.id}`;

        if (!grupos[key]) grupos[key] = [];
        (pedido.detalles || []).forEach((detalle) => {
            grupos[key].push({ pedido, detalle });
        });
    });

    mesasContainer.innerHTML = Object.entries(grupos)
        .map(([mesaLabel, filas]) => {
            const totalItems = filas.reduce((s, f) => s + f.detalle.cantidad, 0);
            const todoListo  = filas.every((f) =>
                ["listo", "entregado"].includes(f.detalle.estadoDetalle)
            );

            const filaHTML = filas.map(({ pedido, detalle }) => `
                <tr>
                    <td>${detalle.productoNombre}</td>
                    <td class="text-center">${detalle.cantidad}</td>
                    <td class="text-center">
                        <span class="${claseBadge(detalle.estadoDetalle || "pendiente")}">
                            ${etiquetaEstado(detalle.estadoDetalle || "pendiente")}
                        </span>
                    </td>
                    <td>
                        <div class="kitchen-actions">
                            <button class="btn btn-warning btn-accion"
                                data-detalle-id="${detalle.detalleId}"
                                data-pedido-id="${pedido.id}"
                                data-estado="en_preparacion"
                                ${detalle.estadoDetalle === "en_preparacion" ? "disabled" : ""}>
                                Preparar
                            </button>
                            <button class="btn btn-success btn-accion"
                                data-detalle-id="${detalle.detalleId}"
                                data-pedido-id="${pedido.id}"
                                data-estado="listo"
                                ${detalle.estadoDetalle === "listo" ? "disabled" : ""}>
                                Listo
                            </button>
                            <button class="btn btn-primary btn-accion"
                                data-detalle-id="${detalle.detalleId}"
                                data-pedido-id="${pedido.id}"
                                data-estado="entregado"
                                ${detalle.estadoDetalle === "entregado" ? "disabled" : ""}>
                                Entregado
                            </button>
                        </div>
                    </td>
                </tr>
            `).join("");

            return `
                <div class="mesa-card">
                    <div class="mesa-card-title">
                        🪑 ${mesaLabel}
                        <span class="badge ${todoListo ? "bg-success" : "bg-warning text-dark"}">
                            ${todoListo ? "Todo listo" : `${totalItems} item${totalItems !== 1 ? "s" : ""} pendientes`}
                        </span>
                    </div>
                    <div class="table-responsive">
                        <table class="table table-sm table-hover kitchen-table">
                            <thead class="table-dark">
                                <tr>
                                    <th>Plato</th>
                                    <th class="text-center">Cantidad</th>
                                    <th class="text-center">Estado</th>
                                    <th>Acción</th>
                                </tr>
                            </thead>
                            <tbody>${filaHTML}</tbody>
                        </table>
                    </div>
                </div>
            `;
        })
        .join("");
}

// ── Carga de datos ──────────────────────────────────────────────────────────

async function cargarPedidos(silencioso = false) {
    if (actualizando) return;
    actualizando = true;

    if (!silencioso) {
        mesasContainer.innerHTML = `<div class="text-center text-muted py-4">Cargando pedidos...</div>`;
    }

    try {
        const res = await fetch(PEDIDOS_URL, { headers: authHeaders() });
        if (!res.ok) throw new Error(`Error ${res.status}`);
        renderCocina(await res.json());
        ultimaAct.textContent = `Última actualización: ${new Date().toLocaleTimeString("es-PE")}`;
    } catch (err) {
        mesasContainer.innerHTML = `
            <div class="alert alert-danger">
                No se pudo conectar con el backend. ${err.message}
            </div>`;
    } finally {
        actualizando = false;
    }
}

// ── Eventos ─────────────────────────────────────────────────────────────────

// Cambio de estado de un plato individual
mesasContainer.addEventListener("click", async (e) => {
    const btn = e.target.closest(".btn-accion");
    if (!btn || btn.disabled) return;

    const { detalleId, pedidoId, estado } = btn.dataset;
    btn.disabled = true;
    const textoOriginal = btn.textContent;
    btn.textContent = "...";

    try {
        const res = await fetch(
            `${PEDIDOS_URL}/${pedidoId}/detalles/${detalleId}/estado`,
            {
                method: "PUT",
                headers: authHeaders(),
                body: JSON.stringify({ estado }),
            }
        );
        if (!res.ok) throw new Error(await res.text());
        await cargarPedidos(true); // recarga sin spinner
    } catch (err) {
        alert("Error al actualizar: " + err.message);
        btn.disabled = false;
        btn.textContent = textoOriginal;
    }
});

document.getElementById("btnRefresh")
    .addEventListener("click", () => cargarPedidos());

// ── Inicialización ───────────────────────────────────────────────────────────

cargarPedidos();
setInterval(() => cargarPedidos(true), 15000); // auto-refresh cada 15 s