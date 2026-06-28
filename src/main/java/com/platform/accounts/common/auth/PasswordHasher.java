package com.platform.accounts.common.auth;

import jakarta.enterprise.context.ApplicationScoped;
import org.mindrot.jbcrypt.BCrypt;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

@ApplicationScoped
public class PasswordHasher {

    private static final int BCRYPT_LOG_ROUNDS = 12;
    private static final int MAXIMUM_DJANGO_ITERATIONS = 2_000_000;

    public String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_LOG_ROUNDS));
    }

    public boolean verify(String password, String encodedHash) {
        if (encodedHash == null || encodedHash.startsWith("!")) {
            return false;
        }
        if (encodedHash.startsWith("$2")) {
            try {
                return BCrypt.checkpw(password, encodedHash);
            } catch (IllegalArgumentException exception) {
                return false;
            }
        }
        if (encodedHash.startsWith("pbkdf2_sha256$")) {
            return verifyDjangoHash(password, encodedHash);
        }
        return false;
    }

    public boolean needsRehash(String encodedHash) {
        return encodedHash != null && encodedHash.startsWith("pbkdf2_sha256$");
    }

    private boolean verifyDjangoHash(String password, String encodedHash) {
        String[] components = encodedHash.split("\\$", -1);
        if (components.length != 4) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(components[1]);
            if (iterations <= 0 || iterations > MAXIMUM_DJANGO_ITERATIONS) {
                return false;
            }
            byte[] expectedHash = Base64.getDecoder().decode(components[3]);
            PBEKeySpec keySpec = new PBEKeySpec(
                password.toCharArray(),
                components[2].getBytes(StandardCharsets.UTF_8),
                iterations,
                expectedHash.length * Byte.SIZE
            );
            byte[] calculatedHash = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(keySpec)
                .getEncoded();
            keySpec.clearPassword();
            return MessageDigest.isEqual(expectedHash, calculatedHash);
        } catch (
            IllegalArgumentException
            | NoSuchAlgorithmException
            | InvalidKeySpecException exception
        ) {
            return false;
        }
    }
}
