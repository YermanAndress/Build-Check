package co.edu.uceva.buildcheck.modules.movimientos.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import co.edu.uceva.buildcheck.modules.usuario_proyecto.model.UsuarioProyecto;
import co.edu.uceva.buildcheck.modules.usuario_proyecto.repository.IUsuarioProyectoRepository;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TelegramService {

    @Value("${telegram.bot.token}")
    private String botToken;

    private final IUsuarioProyectoRepository usuarioProyectoRepository;

    // RestTemplate NO puede ser final con @RequiredArgsConstructor si se inicializa inline,
    // hay que declararlo así para evitar conflicto con Lombok
    private final RestTemplate restTemplate = new RestTemplate();

    public void enviarAlertaStockBajo(
            Long proyectoId,
            String nombreProyecto,
            String materialNombre,
            Double stockActual,
            Double stockReferencia,
            String unidadMedida,
            String mensajeStock
    ) {

System.out.println("===== ALERTA STOCK =====");
    System.out.println("ProyectoId: " + proyectoId);
        // Usamos findByProyectoIdWithUsuarioEager que ya existe en el repositorio real
        // y hace JOIN FETCH para cargar el Usuario (necesario para leer telegramChatId)
        List<UsuarioProyecto> miembros =
                usuarioProyectoRepository.findByProyectoIdWithUsuarioEager(proyectoId);

        if (miembros.isEmpty()) {
            System.out.println("No hay miembros en el proyecto");
            return;
        }

        double porcentaje = (stockActual / stockReferencia) * 100;

        String diaSemana = LocalDate.now()
                .getDayOfWeek()
                .getDisplayName(TextStyle.FULL, new Locale("es", "CO"));

        String mensaje =
            "⚠️ ALERTA DE STOCK BAJO\n" +
            "📅 " + diaSemana + "\n" +
            " ━━━━━━━━━━━━━━━━━━━━\n\n" +
            "🏗️ Proyecto: " + nombreProyecto + "\n" +
            "🔴 Material: " + materialNombre + "\n\n" +
            "📦 Stock actual: " + stockActual + " " + unidadMedida + "\n" +
            "🎯 Umbral mínimo: " + stockReferencia + " " + unidadMedida + "\n" +
            "📉 Nivel: " + String.format("%.1f", porcentaje) + "% del umbral\n" +
            "💬 " + mensajeStock + "\n\n" +
            "━━━━━━━━━━━━━━━━━━━━\n" +
            "🏗️ BuildCheck — Alertas automáticas";

        for (UsuarioProyecto up : miembros) {
            String chatId = up.getUsuario().getTelegramChatId();
            System.out.println("Usuario: " + up.getUsuario().getCorreo());
            System.out.println("Chat id: " + chatId);
            if (chatId != null && !chatId.isBlank()) {
                enviarMensaje(chatId, mensaje);
            }
        }
    }

    // Sin modificador = package-protected, accesible por AlertaStockService
    // (mismo package: co.edu.uceva.buildcheck.modules.movimientos.service)
    void enviarMensaje(String chatId, String mensaje) {
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        TelegramMessageRequest body = new TelegramMessageRequest(chatId, mensaje, "");

        try {
            restTemplate.postForObject(url, body, String.class);
        } catch (Exception e) {
            System.out.println("Error enviando Telegram a chatId " + chatId + ": " + e.getMessage());
        }
    }

    record TelegramMessageRequest(
            String chat_id,
            String text,
            String parse_mode
    ) {}
}