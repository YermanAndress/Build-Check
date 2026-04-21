package co.edu.uceva.buildcheck.modules.usuarios.login;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

@Service
public class RsaKeyService {

    private PrivateKey privateKey;
    private PublicKey  publicKey;

    // Ruta donde se guardan las llaves (relativa al proyecto)
    private static final String PRIVATE_KEY_PATH = "rsa_private.key";
    private static final String PUBLIC_KEY_PATH  = "rsa_public.key";

    @PostConstruct
    public void init() throws Exception {
        File privateKeyFile = new File(PRIVATE_KEY_PATH);
        File publicKeyFile  = new File(PUBLIC_KEY_PATH);

        if (privateKeyFile.exists() && publicKeyFile.exists()) {
            // ── Cargar llaves existentes ──────────────────────────
            System.out.println("🔑 Cargando llaves RSA existentes...");
            loadKeys();
            System.out.println("✅ Llaves RSA cargadas correctamente");
        } else {
            // ── Generar y guardar nuevas llaves ───────────────────
            System.out.println("🔑 Generando nuevas llaves RSA...");
            generateAndSaveKeys();
            System.out.println("✅ Llaves RSA generadas y guardadas");
        }
    }

    private void generateAndSaveKeys() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey  = pair.getPublic();

        // Guardar en archivos
        saveToFile(PRIVATE_KEY_PATH, privateKey.getEncoded());
        saveToFile(PUBLIC_KEY_PATH,  publicKey.getEncoded());
    }

    private void loadKeys() throws Exception {
        byte[] privateBytes = Files.readAllBytes(Paths.get(PRIVATE_KEY_PATH));
        byte[] publicBytes  = Files.readAllBytes(Paths.get(PUBLIC_KEY_PATH));

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));
        this.publicKey  = keyFactory.generatePublic(new X509EncodedKeySpec(publicBytes));
    }

    private void saveToFile(String path, byte[] data) throws IOException {
        Files.write(Paths.get(path), data);
    }

    /** Llave pública en Base64 para enviar al cliente */
    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /** Desencripta un campo cifrado con la llave privada */
    public String decrypt(String cipherBase64) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decoded = Base64.getDecoder().decode(cipherBase64);
        return new String(cipher.doFinal(decoded), "UTF-8");
    }
}