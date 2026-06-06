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

// 
// MODAL ESTADO PLATILLOS
// 
const API_PRODUCTOS = "http://localhost:8080/api/productos";
 
const modalPlatillosEl = document.getElementById("modalPlatillos");
const modalPlatillos   = new bootstrap.Modal(modalPlatillosEl);
 
const catTabsEl      = document.getElementById("catTabs");
const catPanelsEl    = document.getElementById("catPanels");
const platStatusEl   = document.getElementById("platillosStatus");
const availCounterEl = document.getElementById("availCounter");
 
// ── authHeaders (idéntica a GestionCocina.js para no duplicar) ──
function authHeadersModal() {
    const token = localStorage.getItem("token");
    return {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {})
    };
}
 
// ── Helpers ─────────────────────────────────────────
const esc = v => String(v ?? "")
    .replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;")
    .replace(/"/g,"&quot;").replace(/'/g,"&#39;");
 
function setPlatStatus(msg, error = false) {
    platStatusEl.innerHTML = `<span style="color:${error ? "#c0392b" : "#6b7b8a"}">${msg}</span>`;
}
 
// ── Estado local ─────────────────────────────────────
let platillosData = [];   // todos los productos
let categoriasMap = {};   // { "Bebidas": [...], "Fondos": [...] }
 
// ── Abrir modal → cargar productos ──────────────────
document.getElementById("btnEstadoPlatillos").addEventListener("click", () => {
    modalPlatillos.show();
    cargarPlatillos();
});
 
async function cargarPlatillos() {
    // Reset UI
    catTabsEl.innerHTML   = `<div class="modal-empty"><span class="spinner-sm"></span> Cargando platillos…</div>`;
    catPanelsEl.innerHTML = "";
    availCounterEl.style.display = "none";
    setPlatStatus("");
 
    try {
        const res = await fetch(API_PRODUCTOS, { headers: authHeadersModal() });
        if (!res.ok) throw new Error(`Error ${res.status}`);
        platillosData = await res.json();
        agruparPorCategoria();
        renderModalContenido();
    } catch (err) {
        catTabsEl.innerHTML  = `<div class="modal-empty" style="color:#c0392b">No se pudo cargar el menú. Verifica la conexión con el backend.</div>`;
        setPlatStatus("Error al cargar platillos.", true);
    }
}
 
// ── Agrupar productos por categoría ─────────────────
function agruparPorCategoria() {
    categoriasMap = {};
    platillosData.forEach(p => {
        const cat = p.categoria?.nombre || "Sin categoría";
        if (!categoriasMap[cat]) categoriasMap[cat] = [];
        categoriasMap[cat].push(p);
    });
 
    // Ordenar cada categoría por nombre de producto
    Object.keys(categoriasMap).forEach(k => {
        categoriasMap[k].sort((a,b) => (a.nombre||"").localeCompare(b.nombre||"", "es"));
    });
}
 
// ── Render tabs + paneles ───────────────────────────
function renderModalContenido() {
    const cats = Object.keys(categoriasMap).sort((a,b) => a.localeCompare(b,"es"));
 
    if (!cats.length) {
        catTabsEl.innerHTML  = `<div class="modal-empty">No hay productos registrados.</div>`;
        catPanelsEl.innerHTML = "";
        return;
    }
 
    // Tabs
    catTabsEl.innerHTML = cats.map((cat, i) =>
        `<button class="cat-tab${i===0?" active":""}" data-cat="${esc(cat)}">${esc(cat)}</button>`
    ).join("");
 
    // Paneles
    catPanelsEl.innerHTML = cats.map((cat, i) => `
        <div class="cat-panel${i===0?" active":""}" data-cat-panel="${esc(cat)}">
            ${renderTablaPlatillos(categoriasMap[cat])}
        </div>
    `).join("");
 
    actualizarContador();
}
 
// ── Render tabla de una categoría ──────────────────
function renderTablaPlatillos(productos) {
    if (!productos.length) {
        return `<div class="modal-empty">No hay platillos en esta categoría.</div>`;
    }
    const filas = productos.map(p => {
        const on = Boolean(p.disponible);
        return `
        <tr data-prod-id="${p.id}">
            <td style="font-weight:600; color:#1b3a57">${esc(p.nombre || "Sin nombre")}</td>
            <td style="color:#6b7b8a; font-size:.82rem; max-width:220px">
                ${esc(p.descripcion || "—")}
            </td>
            <td style="text-align:center; white-space:nowrap">
                <label class="avail-switch" title="${on ? "Disponible — clic para deshabilitar" : "No disponible — clic para habilitar"}">
                    <input type="checkbox" class="js-avail-check form-check-input"
                           data-id="${p.id}" ${on ? "checked" : ""}>
                    <span class="${on ? "avail-label-on" : "avail-label-off"} js-avail-label">
                        ${on ? "Disponible" : "No disponible"}
                    </span>
                </label>
            </td>
        </tr>`;
    }).join("");
 
    return `
    <table class="platillos-table">
        <thead>
            <tr>
                <th>Platillo</th>
                <th>Descripción</th>
                <th style="text-align:center; width:150px">Disponibilidad</th>
            </tr>
        </thead>
        <tbody>${filas}</tbody>
    </table>`;
}
 
// ── Contador resumen ────────────────────────────────
function actualizarContador() {
    const total = platillosData.length;
    const activos = platillosData.filter(p => Boolean(p.disponible)).length;
    availCounterEl.style.display = "block";
    
}
 
// ── Cambio de tab ───────────────────────────────────
catTabsEl.addEventListener("click", e => {
    const tab = e.target.closest(".cat-tab");
    if (!tab) return;
    const cat = tab.dataset.cat;
 
    catTabsEl.querySelectorAll(".cat-tab").forEach(t => t.classList.remove("active"));
    catPanelsEl.querySelectorAll(".cat-panel").forEach(p => p.classList.remove("active"));
 
    tab.classList.add("active");
    catPanelsEl.querySelector(`[data-cat-panel="${CSS.escape(cat)}"]`)?.classList.add("active");
});
 
// ── Toggle disponibilidad ───────────────────────────
catPanelsEl.addEventListener("change", async e => {
    const check = e.target.closest(".js-avail-check");
    if (!check) return;
 
    const id       = parseInt(check.dataset.id);
    const newValue = check.checked;
    const label    = check.parentElement.querySelector(".js-avail-label");
    const row      = check.closest("tr");
 
    // Feedback inmediato en UI
    check.disabled = true;
    label.textContent = "Guardando…";
    label.className   = "js-avail-label";
 
    try {
        const res = await fetch(`${API_PRODUCTOS}/${id}/disponibilidad`, {
            method  : "PUT",
            headers : authHeadersModal(),
            body    : JSON.stringify({ disponible: newValue })
        });
 
        if (!res.ok) throw new Error(await res.text() || `Error ${res.status}`);
 
        // Actualizar estado local
        const idx = platillosData.findIndex(p => p.id === id);
        if (idx !== -1) platillosData[idx].disponible = newValue;
 
        // Actualizar UI del switch
        label.textContent = newValue ? "Disponible" : "No disponible";
        label.className   = `${newValue ? "avail-label-on" : "avail-label-off"} js-avail-label`;
 
        actualizarContador();
        setPlatStatus(`"${platillosData.find(p=>p.id===id)?.nombre || id}" marcado como ${newValue ? "disponible" : "no disponible"}.`);
 
    } catch (err) {
        // Revertir checkbox si falló
        check.checked     = !newValue;
        label.textContent = !newValue ? "Disponible" : "No disponible";
        label.className   = `${!newValue ? "avail-label-on" : "avail-label-off"} js-avail-label`;
        setPlatStatus(`Error al actualizar: ${err.message}`, true);
    } finally {
        check.disabled = false;
    }
});
 
// Recargar al re-abrir el modal por si hubo cambios externos
modalPlatillosEl.addEventListener("show.bs.modal", () => {
    cargarPlatillos();
});
