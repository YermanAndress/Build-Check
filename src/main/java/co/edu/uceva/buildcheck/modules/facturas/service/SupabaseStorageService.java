package co.edu.uceva.buildcheck.modules.facturas.service;

import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
public class SupabaseStorageService {

    @Value("${SUPABASE_URL}")
    private String supabaseUrl;

    @Value("${SUPABASE_SERVICE_ROLE_KEY}")
    private String serviceRoleKey;

    @Value("${SUPABASE_BUCKET}")
    private String bucket;

    private static final int TIMEOUT_SECONDS = 15;

    public String uploadImage(byte[] imageData, String fileName) throws IOException {
        log.info("Iniciando carga de imagen a Supabase Storage. Archivo: {}", fileName);

        String uploadPath = "facturas/" + UUID.randomUUID() + "-" + fileName;
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + uploadPath;

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .writeTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .readTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();

        RequestBody body = RequestBody.create(imageData, MediaType.parse("image/jpeg"));

        Request request = new Request.Builder()
                .url(uploadUrl)
                .header("Authorization", "Bearer " + serviceRoleKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Error al subir imagen a Supabase. Status: {}. Body: {}", response.code(), response.body());
                throw new IOException("Supabase upload error: " + response.code());
            }

            String signedUrl = generateSignedUrl(uploadPath);
            log.info("Imagen cargada exitosamente. URL: {}", signedUrl);
            return signedUrl;
        }
    }

    private String generateSignedUrl(String path) throws IOException {
        log.info("Generando URL firmada para: {}", path);

        String expiryUrl = supabaseUrl + "/storage/v1/object/sign/" + bucket + "/" + path;

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .readTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();

        // Solicitar URL firmada válida por 1 hora (3600 segundos)
        String requestBody = "{\"expiresIn\": 3600}";
        
        RequestBody body = RequestBody.create(requestBody, MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(expiryUrl)
                .header("Authorization", "Bearer " + serviceRoleKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Error generando URL firmada. Status: {}. Body: {}", response.code(), response.body());
                throw new IOException("Signed URL generation error: " + response.code());
            }

            String responseBody = response.body().string();
            log.debug("Respuesta de URL firmada: {}", responseBody);
            
            // Aquí podrías parsear el JSON para obtener la URL exacta
            // Por ahora, retornamos una URL pública (para desarrollo)
            return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + path;
        }
    }

    public String getPublicUrl(String path) {
        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + path;
    }
}
