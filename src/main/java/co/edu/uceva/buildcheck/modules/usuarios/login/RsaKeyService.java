package co.edu.uceva.buildcheck.modules.usuarios.login;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;
import java.security.*;
import java.util.Base64;

@Service
public class RsaKeyService {

    private PrivateKey privateKey;
    private PublicKey  publicKey;

    @PostConstruct
    public void init() throws NoSuchAlgorithmException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey  = pair.getPublic();
    }

    /** Llave pública en Base64 para enviar al cliente */
    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /** Desencripta un campo cifrado con la llave pública */
    public String decrypt(String cipherBase64) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decoded = Base64.getDecoder().decode(cipherBase64);
        return new String(cipher.doFinal(decoded), "UTF-8");
    }
}