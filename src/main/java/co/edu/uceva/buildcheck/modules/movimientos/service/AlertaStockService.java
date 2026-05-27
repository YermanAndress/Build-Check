package co.edu.uceva.buildcheck.modules.movimientos.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import co.edu.uceva.buildcheck.modules.materiales.model.Material;
import co.edu.uceva.buildcheck.modules.materiales.repository.MaterialRepository;
import co.edu.uceva.buildcheck.modules.usuario_proyecto.model.UsuarioProyecto;
import co.edu.uceva.buildcheck.modules.usuario_proyecto.repository.IUsuarioProyectoRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertaStockService {

    private final MaterialRepository materialRepository;
    // Reemplaza UsuarioRepository (no tiene findByProyectoId válido)
    // por IUsuarioProyectoRepository que ya tiene findByProyectoIdWithUsuarioEager
    private final IUsuarioProyectoRepository usuarioProyectoRepository;
    private final TelegramService telegramService;

    public void verificarStockBajo(Long materialId) {

        Material material = materialRepository.findById(materialId).orElse(null);
        if (material == null) {
            return;
        }

        // Stock normal → sin alerta
        if (material.getStockActual() >= material.getStockReferencia()) {
            return;
        }

        Long proyectoId = material.getProyecto().getId();

        // JOIN FETCH garantiza que usuario.telegramChatId esté disponible sin LazyInitializationException
        List<UsuarioProyecto> miembros =
                usuarioProyectoRepository.findByProyectoIdWithUsuarioEager(proyectoId);

        double porcentaje =
                (material.getStockActual() / material.getStockReferencia()) * 100;

        String mensaje =
                "⚠️ *ALERTA DE STOCK BAJO*\n" +
                "━━━━━━━━━━━━━━━━━━━━\n\n" +
                "🔴 *" + material.getNombre() + "*\n" +
                "📦 Stock actual: *" + material.getStockActual() + " " + material.getUnidadMedida() + "*\n" +
                "🎯 Umbral mínimo: " + material.getStockReferencia() + " " + material.getUnidadMedida() + "\n" +
                "📉 Nivel: " + String.format("%.1f", porcentaje) + "% del umbral\n\n" +
                "🏗️ Proyecto: *" + material.getProyecto().getNombre() + "*";

        for (UsuarioProyecto up : miembros) {
            String chatId = up.getUsuario().getTelegramChatId();
            if (chatId != null && !chatId.isBlank()) {
                // enviarMensaje es package-protected en TelegramService,
                // accesible porque están en el mismo package
                telegramService.enviarMensaje(chatId, mensaje);
            }
        }
    }
}