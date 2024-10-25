package dslab.util.generator;

import java.security.SecureRandom;

public class SecureStringGenerator {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private final SecureRandom random = new SecureRandom();

    public String getSecureString() {
        return getSecureString(16);
    }

    public String getSecureString(int length) {

        StringBuilder result = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARS.length());
            result.append(CHARS.charAt(index));
        }

        return result.toString();
    }
}
