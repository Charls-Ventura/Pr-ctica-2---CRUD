package edu.pucmm.icc352.util;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class RememberMeUtil {

    // 🔒 Cambia esta clave a algo tuyo (y no la compartas)
    private static final String SECRET = "ICC352-REMEMBER-ME-CHANGE-ME";

    private static final StandardPBEStringEncryptor ENC = new StandardPBEStringEncryptor();

    static {
        ENC.setPassword(SECRET);
        ENC.setAlgorithm("PBEWithMD5AndDES");
    }

    public static String buildToken(String username, long expiresAtMillis) {
        String payload = username + "|" + expiresAtMillis;   // NO password
        String encrypted = ENC.encrypt(payload);

        // Cookie-safe
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(encrypted.getBytes(StandardCharsets.UTF_8));
    }

    public static Decoded decodeToken(String token) {
        try {
            String encrypted = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String payload = ENC.decrypt(encrypted);

            String[] parts = payload.split("\\|");
            if (parts.length != 2) return null;

            String username = parts[0];
            long exp = Long.parseLong(parts[1]);

            return new Decoded(username, exp);
        } catch (Exception e) {
            return null;
        }
    }

    public static class Decoded {
        public final String username;
        public final long expiresAtMillis;

        public Decoded(String username, long expiresAtMillis) {
            this.username = username;
            this.expiresAtMillis = expiresAtMillis;
        }
    }
}
