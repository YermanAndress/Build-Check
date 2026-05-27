package co.edu.uceva.buildcheck.modules.facturas.service;

import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
public class SupabaseStorageService {

    private final String supabaseUrl;
    private final String serviceRoleKey;
    private final String bucket;
    private final OkHttpClient httpClient;

    private static final int TIMEOUT_SECONDS = 15;
    private static final Gson gson = new Gson();
    private static final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    // Inyección limpia por constructor (Recomendado en Spring)
    public SupabaseStorageService(
            @Value("${SUPABASE_URL}") String supabaseUrl,
            @Value("${SUPABASE_SERVICE_ROLE_KEY}") String serviceRoleKey,
            @Value("${SUPABASE_BUCKET}") String bucket) {
        this.supabaseUrl = supabaseUrl;
        this.serviceRoleKey = serviceRoleKey;
        this.bucket = bucket;

        // El cliente se instancia una sola vez y se reutiliza
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .writeTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .readTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
    }

    public String uploadImage(byte[] imageData, String fileName) throws IOException {
        log.info("Iniciando carga de imagen a Supabase Storage. Archivo: {}", fileName);

        String uploadPath = "facturas/" + UUID.randomUUID() + "-" + fileName;
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + uploadPath;

        RequestBody body = RequestBody.create(imageData, MEDIA_TYPE_JPEG);

        Request request = new Request.Builder()
                .url(uploadUrl)
                .header("Authorization", "Bearer " + serviceRoleKey)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Error al subir imagen a Supabase. Status: {}. Body: {}", response.code(),
                        response.body() != null ? response.body().string() : "null");
                throw new IOException("Supabase upload error: " + response.code());
            }
            log.info("Imagen cargada exitosamente. Path: {}", uploadPath);
            return uploadPath;
        }
    }

    public String getSignedUrl(String path) throws IOException {
        // CORREGIDO: Se cambió 'uploadPath' por el parámetro real 'path'
        String signedUrl = generateSignedUrl(path);
        log.info("URL firmada obtenida exitosamente: {}", signedUrl);
        return signedUrl;
    }

    private String generateSignedUrl(String path) throws IOException {
        log.info("Generando URL firmada para: {}", path);

        String expiryUrl = supabaseUrl + "/storage/v1/object/sign/" + bucket + "/" + path;
        String requestBody = "{\"expiresIn\": 3600}";

        RequestBody body = RequestBody.create(requestBody, MEDIA_TYPE_JSON);

        Request request = new Request.Builder()
                .url(expiryUrl)
                .header("Authorization", "Bearer " + serviceRoleKey)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Error generando URL firmada. Status: {}. Body: {}", response.code(),
                        response.body() != null ? response.body().string() : "null");
                throw new IOException("Signed URL generation error: " + response.code());
            }

            String responseBody = response.body().string();
            log.debug("Respuesta de URL firmada: {}", responseBody);

            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            String signedUrl = (json != null && json.has("signedURL"))
                    ? json.get("signedURL").getAsString()
                    : null;

            if (signedUrl == null || signedUrl.isEmpty()) {
                throw new IOException("Signed URL missing in response");
            }

            // CORREGIDO: Se eliminó el código inalcanzable.
            if (signedUrl.startsWith("http")) {
                return signedUrl;
            }

            return supabaseUrl + signedUrl;
        }
    }

    public String getPublicUrl(String path) {
        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + path;
    }
}