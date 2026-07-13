// MesaService.java
package com.yakusabor.backend.services;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yakusabor.backend.dto.MesaEstadoResponse;
import com.yakusabor.backend.models.Mesa;
import com.yakusabor.backend.models.Pedido;
import com.yakusabor.backend.models.Usuario;
import com.yakusabor.backend.repositories.MesaRepository;
import com.yakusabor.backend.repositories.PedidoRepository;
import com.yakusabor.backend.repositories.UsuarioRepository;

@Service
public class MesaService {

    private static final List<String> ESTADOS_VALIDOS =
            List.of("libre", "ocupada", "reservada", "fuera_servicio");
    private static final List<String> ESTADOS_PEDIDO_ABIERTOS =
            List.of("nuevo", "en_preparacion", "listo", "entregado");

    @Autowired private MesaRepository mesaRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    public List<MesaEstadoResponse> obtenerEstadoMesas() {
        return mesaRepository.findAll().stream()
                .sorted(Comparator.comparing(Mesa::getId))
                .map(this::toResponse)
                .toList();
    }

    public MesaEstadoResponse crearMesa(Map<String, String> body) {
        String codigo = body.getOrDefault("codigo", "").trim();
        String ubicacion = body.getOrDefault("ubicacion", "interior").trim();

        if (codigo.isEmpty()) {
            throw new IllegalArgumentException("El campo 'codigo' es obligatorio.");
        }

        boolean existe = mesaRepository.findAll().stream()
                .anyMatch(m -> m.getCodigo().equalsIgnoreCase(codigo));
        if (existe) {
            throw new IllegalArgumentException("Ya existe una mesa con el código: " + codigo);
        }

        Mesa nueva = new Mesa();
        nueva.setCodigo(codigo.toUpperCase());
        nueva.setUbicacion(ubicacion.toLowerCase());
        nueva.setEstado("libre");

        return toResponse(mesaRepository.save(nueva));
    }

    public MesaEstadoResponse actualizarEstado(Integer id, Map<String, String> body) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada."));

        String nuevoEstado = body.getOrDefault("estado", "").trim().toLowerCase();
        if (!ESTADOS_VALIDOS.contains(nuevoEstado)) {
            throw new IllegalArgumentException("Estado inválido. Permitidos: " + ESTADOS_VALIDOS);
        }

        if ("libre".equals(nuevoEstado)) {
            List<Pedido> pedidosAbiertos = pedidoRepository.findByMesa_Id(id).stream()
                    .filter(p -> ESTADOS_PEDIDO_ABIERTOS.contains(p.getEstado()))
                    .toList();

            pedidosAbiertos.forEach(p -> {
                p.setEstado("cancelado");
                pedidoRepository.save(p);
            });

            // Al liberar la mesa, queda disponible para que cualquier mozo la tome de nuevo.
            mesa.setMesero(null);
        }

        if ("fuera_servicio".equals(nuevoEstado)) {
            mesa.setMesero(null);
        }

        mesa.setEstado(nuevoEstado);
        return toResponse(mesaRepository.save(mesa));
    }

    /**
     * Asigna (o libera) el mozo responsable de una mesa.
     *
     * - Un ADMINISTRADOR puede asignar cualquier mozo a cualquier mesa (o liberarla, enviando meseroId=null).
     * - Un MESERO solo puede:
     *      a) tomar una mesa que no tiene mozo asignado (para "elegir qué mesa atender"), o
     *      b) liberar la mesa que él mismo tiene asignada.
     *   No puede asignarse una mesa que ya está siendo atendida por otro mozo,
     *   ni asignar la mesa a un tercero.
     */
    public MesaEstadoResponse asignarMesero(Integer mesaId, Integer meseroIdSolicitado, Usuario actor) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada."));

        if ("fuera_servicio".equalsIgnoreCase(mesa.getEstado())) {
            throw new IllegalArgumentException("La mesa está fuera de servicio.");
        }

        boolean esAdmin = esAdministrador(actor);

        if (!esAdmin) {
            // El mozo solo puede operar sobre sí mismo.
            boolean estaLiberando = meseroIdSolicitado == null;
            boolean mesaSinDueño = mesa.getMesero() == null;
            boolean mesaEsSuya = mesa.getMesero() != null && mesa.getMesero().getId().equals(actor.getId());

            if (!estaLiberando && !actor.getId().equals(meseroIdSolicitado)) {
                throw new IllegalArgumentException("Un mozo solo puede asignarse a sí mismo.");
            }
            if (!estaLiberando && !mesaSinDueño && !mesaEsSuya) {
                throw new IllegalArgumentException(
                        "Esta mesa ya está siendo atendida por " + mesa.getMesero().getNombre() + ".");
            }
            if (estaLiberando && !mesaEsSuya) {
                throw new IllegalArgumentException("Solo puedes liberar una mesa que tú mismo atiendes.");
            }
        }

        if (meseroIdSolicitado == null) {
            mesa.setMesero(null);
        } else {
            Usuario mesero = usuarioRepository.findById(meseroIdSolicitado)
                    .orElseThrow(() -> new IllegalArgumentException("El mozo seleccionado no existe."));
            if (mesero.getRol() == null || !"mesero".equalsIgnoreCase(mesero.getRol().getNombre())) {
                throw new IllegalArgumentException("El usuario seleccionado no tiene rol de Mozo.");
            }
            mesa.setMesero(mesero);
        }

        return toResponse(mesaRepository.save(mesa));
    }

    public void eliminarMesa(Integer id) {
        if (!mesaRepository.existsById(id)) {
            throw new IllegalArgumentException("Mesa no encontrada.");
        }
        mesaRepository.deleteById(id);
    }

    public static boolean esAdministrador(Usuario usuario) {
        return usuario != null && usuario.getRol() != null
                && "administrador".equalsIgnoreCase(usuario.getRol().getNombre());
    }

    private MesaEstadoResponse toResponse(Mesa mesa) {
        Usuario mesero = mesa.getMesero();
        return new MesaEstadoResponse(
                mesa.getId(), mesa.getCodigo(), mesa.getUbicacion(),
                mesa.getEstado(), "libre".equalsIgnoreCase(mesa.getEstado()),
                mesero != null ? mesero.getId() : null,
                mesero != null ? mesero.getNombre() : null);
    }
}
