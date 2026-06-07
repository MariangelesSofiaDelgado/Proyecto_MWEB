const ORDERS_API = 'http://localhost:8080/api/pedidos';

function authHeaders() {
  const token = localStorage.getItem('token');
  return { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) };
}

function estadoLabel(s) {
  const map = { nuevo: 'Nuevo', pendiente: 'Pendiente', en_preparacion: 'En preparación', listo: 'Listo', entregado: 'Entregado', rechazado: 'Rechazado' };
  return map[s] || s;
}

async function loadPedidos() {
  const el = document.getElementById('ordersContainer');
  el.innerHTML = 'Cargando pedidos...';
  try {
    const res = await fetch(ORDERS_API, { headers: authHeaders() });
    if (!res.ok) throw new Error('Error ' + res.status);
    const pedidos = await res.json();

    if (!pedidos.length) { el.innerHTML = '<div class="alert alert-info">No hay pedidos.</div>'; return; }

    el.innerHTML = pedidos.map(p => {
      const detalles = (p.detalles || []).map(d => `
        <tr>
          <td>${d.productoNombre}</td>
          <td class="text-center">${d.cantidad}</td>
          <td class="text-center">${estadoLabel(d.estadoDetalle)}</td>
          <td class="text-end">
            ${d.estadoDetalle !== 'en_preparacion' && d.estadoDetalle !== 'listo' && d.estadoDetalle !== 'rechazado' ? `<button class="btn btn-sm btn-secondary btn-send-kitchen" data-pedido="${p.id}" data-detalle="${d.detalleId}">Enviar a cocina</button>` : ''}
          </td>
        </tr>
      `).join('');

      return `
        <div class="card mb-3">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-2">
              <div><strong>Mesa:</strong> ${p.mesaCodigo || 'N/A'} — <small>${p.createdAt || ''}</small></div>
              <div><strong>Estado:</strong> ${p.estado}</div>
            </div>
            <div class="table-responsive">
              <table class="table table-sm">
                <thead><tr><th>Plato</th><th class="text-center">Cant</th><th class="text-center">Estado</th><th></th></tr></thead>
                <tbody>${detalles}</tbody>
              </table>
            </div>
          </div>
        </div>
      `;
    }).join('');

    // attach events
    document.querySelectorAll('.btn-send-kitchen').forEach(btn => {
      btn.addEventListener('click', async () => {
        const pedidoId = btn.dataset.pedido;
        const detalleId = btn.dataset.detalle;
        try {
          const res = await fetch(`${ORDERS_API}/${pedidoId}/detalles/${detalleId}/estado`, { method: 'PUT', headers: authHeaders(), body: JSON.stringify({ estado: 'en_preparacion' }) });
          if (!res.ok) throw new Error(await res.text());
          await loadPedidos();
        } catch (err) { alert('Error: ' + err.message); }
      });
    });

  } catch (err) {
    el.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
  }
}

window.addEventListener('DOMContentLoaded', () => {
  loadPedidos();
  // poll every 8 seconds to reflect cook updates
  setInterval(loadPedidos, 8000);
});