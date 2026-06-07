const ORDERS_API = 'http://localhost:8080/api/pedidos';
const INVOICES_API = 'http://localhost:8080/api/facturas';

function authHeaders() {
  const token = localStorage.getItem('token');
  return { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) };
}

function estadoLabel(s) {
  const map = { nuevo: 'Nuevo', pendiente: 'Pendiente', en_preparacion: 'En preparación', listo: 'Listo', entregado: 'Entregado', rechazado: 'Rechazado' };
  return map[s] || s;
}

function estadoClass(s) {
  return `estado-${s.replace(' ', '_')}`;
}

async function loadReadyOrders() {
  const container = document.getElementById('mesasContainer');
  const sinPedidos = document.getElementById('sinPedidos');
  
  try {
    const res = await fetch(ORDERS_API, { headers: authHeaders() });
    if (!res.ok) throw new Error('Error ' + res.status);
    const pedidos = await res.json();

    // Filter orders that are ready or in preparation
    const readyOrders = pedidos.filter(p => 
      ['en_preparacion', 'listo', 'entregado', 'facturado'].includes(p.estado)
    );

    if (!readyOrders.length) { 
      container.innerHTML = '';
      sinPedidos.style.display = 'block';
      return; 
    }

    sinPedidos.style.display = 'none';

    // Agrupar por mesa
    const porMesa = {};
    readyOrders.forEach(p => {
      const key = p.mesaCodigo || 'SIN MESA';
      if (!porMesa[key]) porMesa[key] = [];
      porMesa[key].push(p);
    });

    container.innerHTML = Object.entries(porMesa).map(([mesaCod, pedidosMesa]) => {
      const detallesHTML = pedidosMesa.flatMap(p => {
        const todosListos = (p.detalles || []).every(d => 
          ['listo', 'entregado', 'rechazado'].includes(d.estadoDetalle)
        );

        return (p.detalles || []).map(d => `
          <tr>
            <td>${d.productoNombre}</td>
            <td class="text-center">${d.cantidad}</td>
            <td><span class="estado-badge ${estadoClass(d.estadoDetalle)}">${estadoLabel(d.estadoDetalle)}</span></td>
            <td class="text-end">
              <div class="kitchen-actions">
                ${todosListos && p.estado === 'en_preparacion' ? 
                  `<button class="btn btn-sm btn-success btn-generate-invoice" data-pedido="${p.id}" data-total="${p.total}">🧾 Generar Factura</button>` : 
                  p.estado === 'facturado' ? 
                  `<span class="badge bg-info">Facturado</span>` : ''}
              </div>
            </td>
          </tr>
        `);
      }).join('');

      const totalPedidos = pedidosMesa.reduce((sum, p) => sum + parseFloat(p.total || 0), 0).toFixed(2);

      return `
        <div class="mesa-card">
          <div class="mesa-card-title">
            <span>Mesa ${mesaCod}</span>
            <span class="badge bg-info">${pedidosMesa.length} pedido(s)</span>
            <span class="total-amount">S/. ${totalPedidos}</span>
          </div>
          <div class="table-responsive">
            <table class="table table-sm kitchen-table">
              <thead><tr><th>Plato</th><th class="text-center">Cant</th><th>Estado</th><th></th></tr></thead>
              <tbody>${detallesHTML}</tbody>
            </table>
          </div>
        </div>
      `;
    }).join('');

    // Attach event listeners for invoice generation
    document.querySelectorAll('.btn-generate-invoice').forEach(btn => {
      btn.addEventListener('click', async () => {
        const pedidoId = btn.dataset.pedido;
        const total = btn.dataset.total;
        
        try {
          const tipo = prompt('Tipo de comprobante (boleta/factura):', 'boleta');
          if (!tipo) return;

          const payload = { pedidoId: parseInt(pedidoId), tipo };
          if (tipo === 'factura') {
            payload.ruc = prompt('RUC:');
            payload.razonSocial = prompt('Razón Social:');
          }

          const res = await fetch(INVOICES_API, {
            method: 'POST',
            headers: authHeaders(),
            body: JSON.stringify(payload)
          });

          if (!res.ok) {
            const err = await res.text();
            throw new Error(err);
          }

          const factura = await res.json();
          alert(`✅ ${tipo.toUpperCase()} generada ID: ${factura.id}`);
          await loadReadyOrders();
        } catch (err) {
          alert('Error al generar comprobante: ' + err.message);
        }
      });
    });

    updateTimestamp();

  } catch (err) {
    container.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
  }
}

function updateTimestamp() {
  const now = new Date();
  document.getElementById('ultimaActualizacion').textContent = 
    `Última actualización: ${now.toLocaleTimeString('es-ES')}`;
}

document.getElementById('btnRefresh').addEventListener('click', loadReadyOrders);

window.addEventListener('DOMContentLoaded', () => {
  loadReadyOrders();
  setInterval(loadReadyOrders, 8000);
});
