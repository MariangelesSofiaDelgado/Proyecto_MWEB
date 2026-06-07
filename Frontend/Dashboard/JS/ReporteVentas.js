const REPORT_URL = "http://localhost:8080/api/pedidos/reporte-ventas";
const reportBody = document.getElementById("reportTableBody");
const reportStatus = document.getElementById("reportStatus");
const btnReloadReport = document.getElementById("btnReloadReport");

function authHeaders() {
    const token = localStorage.getItem("token");
    return {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
    };
}

function setReportStatus(text, error = false) {
    reportStatus.textContent = text;
    reportStatus.style.color = error ? "#c0392b" : "#5f6d7a";
}

function renderReport(rows) {
    if (!rows.length) {
        reportBody.innerHTML = `<tr><td colspan="3" class="text-center py-4">No hay ventas registradas.</td></tr>`;
        return;
    }

    reportBody.innerHTML = rows
        .map(row => `
            <tr>
                <td>${row.meseroNombre || `ID ${row.meseroId}`}</td>
                <td>${row.turno || "Sin turno"}</td>
                <td class="text-end">${row.numVentas}</td>
            </tr>
        `)
        .join("");
}

async function loadReport() {
    reportBody.innerHTML = `<tr><td colspan="3" class="text-center py-4">Cargando reporte…</td></tr>`;
    setReportStatus("");

    try {
        const res = await fetch(REPORT_URL, { headers: authHeaders() });
        if (!res.ok) {
            const errText = await res.text();
            throw new Error(errText || `Error ${res.status}`);
        }
        const data = await res.json();
        renderReport(data);
        setReportStatus(`Reporte cargado. ${data.length} fila${data.length !== 1 ? "s" : ""}.`);
    } catch (err) {
        reportBody.innerHTML = `<tr><td colspan="3" class="text-center py-4">No se pudo cargar el reporte.</td></tr>`;
        setReportStatus(`Error al cargar reporte: ${err.message}`, true);
    }
}

window.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("token");
    if (!token) {
        window.location.href = "Dashboard.html";
        return;
    }

    btnReloadReport.addEventListener("click", loadReport);
    loadReport();
});
