package edu.sustech.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

import static edu.sustech.observer.util.RandomUtil.*;

public class RedBall extends Ball {

    private static final Logger logger = LoggerFactory.getLogger(RedBall.class);

    public RedBall(Color color, int xSpeed, int ySpeed, int ballSize) {
        super(color, xSpeed, ySpeed, ballSize);

    }

    @Override
    public void update(char keyChar) {
        switch (keyChar) {
            case 'a' -> this.setXSpeed(-randomInt(20) - 1);
            case 'd' -> this.setXSpeed(randomInt(20) + 1);
            case 'w' -> this.setYSpeed(-randomInt(20) - 1);
            case 's' -> this.setYSpeed(randomInt(20) + 1);
            default -> logger.warn("Unsupported key: {}", keyChar);
        }
        logger.info(" Redball {}: {}, {}", this, getXSpeed(), getYSpeed());
    }

    @Override
    public void update(WhiteRandomBall whiteRandomBall) {
        this.setVisible(isIntersectWith(whiteRandomBall));
    }

}
