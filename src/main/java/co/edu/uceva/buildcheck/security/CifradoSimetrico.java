package co.edu.uceva.buildcheck.security;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CifradoSimetrico {

    @Value("${simetrico.key}")
    private String KEY;

    @Value("${simetrico.salt}")
    private String SALT_BITS;
    
    public String cifrar(String texto) {
        if (texto == null) {
            return null;
        }
        byte[] textoBytes = texto.getBytes(StandardCharsets.UTF_8);
        StringBuilder bin = new StringBuilder();
        for (byte b : textoBytes) {
            bin.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        String keyBits = buildKeyBits(KEY, bin.length());
        StringBuilder xor = new StringBuilder();
        for (int i = 0; i < bin.length(); i++) {
            char bit = bin.charAt(i);
            char keyBit = keyBits.charAt(i);
            xor.append(bit == keyBit ? '0' : '1');
        }
        String conSalt = xor + SALT_BITS;
        return new StringBuilder(conSalt).reverse().toString();
    }

// DESENCRIPTAR
    public String descifrar(String textoCifrado) {
        if (textoCifrado == null || textoCifrado.length() < SALT_BITS.length()) {
            return null;
        }
        String reversed = new StringBuilder(textoCifrado).reverse().toString();
        if (!reversed.endsWith(SALT_BITS)) {
            throw new IllegalArgumentException("Texto cifrado no válido");
        }
        String sinSalt = reversed.substring(0, reversed.length() - SALT_BITS.length());
        String keyBits = buildKeyBits(KEY, sinSalt.length());
        StringBuilder BitsOriginal = new StringBuilder();
        for (int i = 0; i < sinSalt.length(); i++) {
            char bit = sinSalt.charAt(i);
            char keyBit = keyBits.charAt(i);
            BitsOriginal.append(bit == keyBit ? '0' : '1');
        }
        int len = BitsOriginal.length();
        byte[] textoBytes = new byte[len / 8];
        for (int i = 0; i < len; i += 8) {
            String byteStr = BitsOriginal.substring(i, i + 8);
            int val = Integer.parseInt(byteStr, 2);
            textoBytes[i / 8] = (byte) val;
        }
        return new String(textoBytes, StandardCharsets.UTF_8);
    }

    private String buildKeyBits(String key, int length) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        StringBuilder keyBits = new StringBuilder();
        for (byte b : keyBytes) {
            keyBits.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        StringBuilder result = new StringBuilder();
        while (result.length() < length) {
            result.append(keyBits);
        }
        return result.substring(0, length);
    }
}