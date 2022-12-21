package edu.sustech.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

public class BlueBall extends Ball {

    private static final Logger logger = LoggerFactory.getLogger(BlueBall.class);

    public BlueBall(Color color, int xSpeed, int ySpeed, int ballSize) {
        super(color, xSpeed, ySpeed, ballSize);
    }

    @Override
    public void update(char keyChar) {
        this.setXSpeed(-this.getXSpeed());
        this.setYSpeed(-this.getYSpeed());
        logger.info("Blueball {}: {}, {}", this, getXSpeed(), getYSpeed());
    }

    @Override
    public void update(WhiteRandomBall whiteRandomBall) {
        this.setVisible(isIntersectWith(whiteRandomBall));
    }

}
