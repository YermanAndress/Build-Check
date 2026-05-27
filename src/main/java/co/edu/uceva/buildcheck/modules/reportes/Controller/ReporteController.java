package co.edu.uceva.buildcheck.modules.reportes.Controller;

import co.edu.uceva.buildcheck.modules.facturas.model.Factura;
import co.edu.uceva.buildcheck.modules.facturas.service.FacturaService;
import co.edu.uceva.buildcheck.modules.movimientos.model.Movimiento;
import co.edu.uceva.buildcheck.modules.movimientos.model.TipoMovimiento.TipoMovimientoNombre;
import co.edu.uceva.buildcheck.modules.movimientos.service.MovimientoService;
import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;
import co.edu.uceva.buildcheck.modules.proyectos.repository.IProyectoRepository;
import co.edu.uceva.buildcheck.modules.usuario_proyecto.model.UsuarioProyecto;
import co.edu.uceva.buildcheck.modules.usuario_proyecto.repository.IUsuarioProyectoRepository;
import co.edu.uceva.buildcheck.modules.usuarios.model.Roles.RolNombre;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = "*")
public class ReporteController {

    @Value("${reporte.api-key}")
    private String apiKeyEsperada;

    private final MovimientoService movimientoService;
    private final FacturaService facturaService;
    private final IProyectoRepository proyectoRepository;
    private final IUsuarioProyectoRepository usuarioProyectoRepository;

    public ReporteController(
            MovimientoService movimientoService,
            FacturaService facturaService,
            IProyectoRepository proyectoRepository,
            IUsuarioProyectoRepository usuarioProyectoRepository) {
        this.movimientoService = movimientoService;
        this.facturaService = facturaService;
        this.proyectoRepository = proyectoRepository;
        this.usuarioProyectoRepository = usuarioProyectoRepository;
    }

    @GetMapping("/semanal")
    public ResponseEntity<?> getReporteSemanal(
            @RequestHeader("X-Api-Key") String apiKey) {

        if (!apiKeyEsperada.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "API Key inválida"));
        }

        LocalDateTime hasta = LocalDateTime.now();
        LocalDateTime desde = hasta.minusDays(7);
        LocalDateTime desdeAnterior = hasta.minusDays(14);

        List<Proyecto> proyectos = proyectoRepository.findAll();
        List<Map<String, Object>> reportes = new ArrayList<>();

        for (Proyecto proyecto : proyectos) {

            // 1. Buscar directores de obra con telegramChatId vinculado
            List<UsuarioProyecto> miembros = usuarioProyectoRepository.findByProyectoAndRolProyecto(
                    proyecto, RolNombre.ROLE_DIRECTOR_OBRA);

            List<String> chatIds = miembros.stream()
                    .map(m -> m.getUsuario() != null ? m.getUsuario().getTelegramChatId() : null)
                    .filter(id -> id != null && !id.isBlank())
                    .distinct()
                    .toList();

            // Sin directores vinculados a Telegram → saltar proyecto rápido (Fail First)
            if (chatIds.isEmpty()) {
                continue;
            }

            Long proyectoId = proyecto.getId();

            // 2. Movimientos semana actual
            List<Movimiento> movActual = movimientoService
                    .findByProyectoId(proyectoId).stream()
                    .filter(m -> m.getFechaCreacion() != null
                            && !m.getFechaCreacion().isBefore(desde)
                            && !m.getFechaCreacion().isAfter(hasta))
                    .toList();

            // 3. Facturas semana actual
            List<Factura> factActual = facturaService
                    .findByProyectoId(proyectoId).stream()
                    .filter(f -> f.getFechaCreacion() != null
                            && !f.getFechaCreacion().isBefore(desde)
                            && !f.getFechaCreacion().isAfter(hasta))
                    .toList();

            // 4. Facturas semana anterior (para variación)
            List<Factura> factAnterior = facturaService
                    .findByProyectoId(proyectoId).stream()
                    .filter(f -> f.getFechaCreacion() != null
                            && !f.getFechaCreacion().isBefore(desdeAnterior)
                            && f.getFechaCreacion().isBefore(desde))
                    .toList();

            // 5. Totales (Evitando NullPointerException en sumas)
            double totalActual = factActual.stream()
                    .mapToDouble(f -> f.getValorTotal() != null ? f.getValorTotal() : 0.0)
                    .sum();
            double totalAnterior = factAnterior.stream()
                    .mapToDouble(f -> f.getValorTotal() != null ? f.getValorTotal() : 0.0)
                    .sum();

            // 6. Top 3 materiales más consumidos (SOLO SALIDAS)
            // SOLUCIÓN AL ADVERTENCIA DE NULL SAFETY MEDIANTE LAMBDA EXPLICITA
            Map<String, Double> consumo = new HashMap<>();
            movActual.stream()
                    .filter(m -> m.getTipoMovimiento() == TipoMovimientoNombre.SALIDA)
                    .filter(m -> m.getMaterial() != null && m.getMaterial().getNombre() != null
                            && m.getCantidad() != null)
                    .forEach(m -> consumo.merge(
                            m.getMaterial().getNombre(),
                            m.getCantidad(),
                            (v1, v2) -> v1 + v2));

            List<Map<String, Object>> top3 = consumo.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(3)
                    .map(e -> Map.<String, Object>of(
                            "material", e.getKey(),
                            "cantidad", e.getValue()))
                    .toList();

            // 7. Estructurar el reporte del proyecto
            Map<String, Object> reporte = new HashMap<>();
            reporte.put("proyectoNombre", proyecto.getNombre());
            reporte.put("chatIds", chatIds);
            reporte.put("totalGastadoActual", totalActual);
            reporte.put("totalGastadoAnterior", totalAnterior);
            reporte.put("cantidadMovimientos", movActual.size());
            reporte.put("cantidadFacturas", factActual.size());
            reporte.put("top3", top3);
            reporte.put("sinDatos", movActual.isEmpty() && factActual.isEmpty());
            reporte.put("fechaDesde", desde.toLocalDate().toString());
            reporte.put("fechaHasta", hasta.toLocalDate().toString());

            reportes.add(reporte);
        }

        return ResponseEntity.ok(Map.of("reportes", reportes));
    }
}