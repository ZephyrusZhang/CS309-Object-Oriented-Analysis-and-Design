package edu.sustech.observer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class WhiteRandomBall extends Ball implements Subject<Ball> {
    private final List<Ball> observers = new ArrayList<>();

    public WhiteRandomBall(Color color, int xSpeed, int ySpeed, int ballSize) {
        super(color, xSpeed, ySpeed, ballSize);
    }


    @Override
    public void registerObserver(Ball ball) {
        observers.add(ball);
    }

    @Override
    public void removeObserver(Ball ball) {
        observers.remove(ball);
    }

    @Override
    public void notifyObservers(char keyChar) {
    }

    @Override
    public void notifyObservers() {
        for (Ball ball : observers) {
            ball.update(this);
        }
    }

    @Override
    public void move() {
        super.move();
        notifyObservers();
    }
}
