package edu.sustech.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

public class WhiteBall extends Ball {

    private static final Logger logger = LoggerFactory.getLogger(WhiteBall.class);

    public WhiteBall(Color color, int xSpeed, int ySpeed, int ballSize) {
        super(color, xSpeed, ySpeed, ballSize);
    }

    @Override
    public void update(char keyChar) {
        switch (keyChar) {
            case 'a' -> this.setXSpeed(-8);
            case 'd' -> this.setXSpeed(8);
            case 'w' -> this.setYSpeed(-8);
            case 's' -> this.setYSpeed(8);
            default -> logger.warn("Unsupported key: {}", keyChar);
        }
        logger.info("Whiteball {}: {}, {}", this, getXSpeed(), getYSpeed());
    }

}
