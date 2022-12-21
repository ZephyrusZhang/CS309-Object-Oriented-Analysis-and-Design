package edu.sustech.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainPanel extends JPanel implements KeyListener, Subject<Ball> {
    private List<Ball> paintingBallList = new ArrayList<>();//后续会添加红球，蓝球
    private final List<Ball> observers = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(MainPanel.class);

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
        for (Ball ball : observers) {
            ball.update(keyChar);
        }
    }

    @Override
    public void notifyObservers() {

    }

    enum GameStatus {PREPARING, START, STOP}

    protected static GameStatus gameStatus;
    private int score;
    private Ball whiteBall;
    private WhiteRandomBall whiteBallRandom;
    Random random = new Random();
    Timer t;

    public MainPanel() {
        super();
        setLayout(null);
        setSize(590, 590);
        setFocusable(true);
        this.addKeyListener(this);
        t = new Timer(50, e -> moveBalls());
        restartGame();
    }


    public void startGame() {
        gameStatus = GameStatus.START;
        this.whiteBall.setVisible(true);
        this.whiteBallRandom.setVisible(true);
    }

    public void stopGame() {
        gameStatus = GameStatus.STOP;
        this.t.stop();
        paintingBallList.forEach(b -> {
            if (isIntersect(b, whiteBall)) {
                if (b.getColor() == Color.RED) {
                    scoreIncrement(80);
                } else if (b.getColor() == Color.BLUE) {
                    scoreIncrement(-80);
                }
            }
        });
        repaint();
    }

    public boolean isIntersect(Ball a, Ball b) {
        int dis = (this.getWidth() + b.getWidth()) / 2;
        double diffXPow = Math.pow(a.convertToCentralX() - b.convertToCentralX(), 2);
        double diffYPow = Math.pow(a.convertToCentralY() - b.convertToCentralY(), 2);
        return diffXPow + diffYPow < dis * dis;
    }

    public void restartGame() {
        gameStatus = GameStatus.PREPARING;
        if (this.paintingBallList.size() > 0) {
            paintingBallList.forEach(this::remove);//使所有的ball与panel解绑
        }
        this.paintingBallList = new ArrayList<>();
        Ball.setCount(0);
        this.score = 100;
        if (this.whiteBall != null)
            this.whiteBall.setVisible(false);
        if (this.whiteBallRandom != null)
            this.whiteBallRandom.setVisible(false);

        this.t.start();
        repaint();
    }

    public void setWhiteBall(Ball whiteBall) {
        this.whiteBall = whiteBall;
        this.whiteBall.setVisible(false);
        add(whiteBall);
        registerObserver(whiteBall);
    }

    public void setWhiteBallRandom(WhiteRandomBall whiteBallRandom) {
        this.whiteBallRandom = whiteBallRandom;
        add(whiteBallRandom);
        this.whiteBallRandom.setVisible(false);
    }

    public WhiteRandomBall getWhiteBallRandom() {
        return whiteBallRandom;
    }

    public void moveBalls() {
        paintingBallList.forEach(Ball::move);
        if (gameStatus == GameStatus.START) {
            score--;
            whiteBall.move();
            whiteBallRandom.move();

        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setFont(new Font("Arial", Font.PLAIN, 30));
        g.setColor(Color.BLACK);
        g.drawString("Score: " + score, 20, 40);

        if (gameStatus == GameStatus.START) {
            this.setBackground(Color.WHITE);
        }

        if (gameStatus == GameStatus.STOP) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 45));
            g.drawString("Game Over!", 200, 200);
            g.setFont(new Font("", Font.BOLD, 40));
            g.drawString("Your score is " + score, 190, 280);
        }
    }

    public void scoreIncrement(int increment) {
        this.score += increment;
    }


    public void addBallToPanel(Ball ball) {
        paintingBallList.add(ball);
        this.add(ball);
        registerObserver(ball);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        char keyChar = e.getKeyChar();
//        System.out.println("Press: " + keyChar);
        logger.info("Press key {}", keyChar);
        notifyObservers(keyChar);

//        for (Ball ball : paintingBallList) {
//            if (ball.getColor() == Color.RED) {
//                switch (keyChar) {
//                    case 'a':
//                        ball.setXSpeed(-random.nextInt(3) - 1);
//                        break;
//                    case 'd':
//                        ball.setXSpeed(random.nextInt(3) + 1);
//                        break;
//                    case 'w':
//                        ball.setYSpeed(-random.nextInt(3) - 1);
//                        break;
//                    case 's':
//                        ball.setYSpeed(random.nextInt(3) + 1);
//                }
//            } else if (ball.getColor() == Color.BLUE) {
//                ball.setXSpeed(-1 * ball.getXSpeed());
//                ball.setYSpeed(-1 * ball.getYSpeed());
//            }
//        }
//
//        if (gameStatus == GameStatus.START) {
//            switch (keyChar) {
//                case 'a':
//                    whiteBall.setXSpeed(-8);
//                    break;
//                case 'd':
//                    whiteBall.setXSpeed(8);
//                    break;
//                case 'w':
//                    whiteBall.setYSpeed(-8);
//                    break;
//                case 's':
//                    whiteBall.setYSpeed(8);
//                    break;
//            }
//        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
