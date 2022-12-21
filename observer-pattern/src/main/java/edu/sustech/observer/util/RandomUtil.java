package edu.sustech.observer.util;

import java.util.Random;

public class RandomUtil {

    private static final Random random = new Random();

    public static int randomInt(int bound) {
        return random.nextInt(bound);
    }

}
