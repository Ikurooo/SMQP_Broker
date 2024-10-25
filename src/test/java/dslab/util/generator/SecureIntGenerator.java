package dslab.util.generator;

import java.security.SecureRandom;

public class SecureIntGenerator {

    private final SecureRandom random = new SecureRandom();

    public int getInt(int lowerBound, int upperBound) {
        return random.nextInt(lowerBound, upperBound);
    }
}

