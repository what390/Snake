package snake.game;

import java.awt.*;
import java.util.LinkedList;
import java.util.Random;

public class AISnake {
    private LinkedList<Point> body;
    private char direction;
    private char nextDirection;
    private final Random random = new Random();
    private final GamePanel gamePanel;
    private int aggression;
    private int hp;
    private int maxHp;

    public AISnake(GamePanel panel, Point startHead, int initialLength, int difficulty) {
        this.gamePanel = panel;
        this.aggression = difficulty;
        body = new LinkedList<>();
        body.add(startHead);
        for (int i = 1; i < initialLength; i++) {
            body.add(new Point(startHead.x - i * GamePanel.UNIT_SIZE, startHead.y));
        }
        direction = 'R';
        nextDirection = 'R';
        maxHp = 3;
        hp = maxHp;
    }

    public void setAggression(int aggression) { this.aggression = aggression; }
    public LinkedList<Point> getBody() { return body; }
    public Point getHead() { return body.getFirst(); }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }

    public void takeDamage(int amount) {
        hp -= amount;
        if (hp < 0) hp = 0;
    }

    public void decideDirection() {
        LinkedList<Point> targets = new LinkedList<>();
        targets.addAll(gamePanel.getFoods());
        targets.addAll(gamePanel.getMiniFoods());
        if (gamePanel.hasCoin() && gamePanel.getCoin() != null) targets.add(gamePanel.getCoin());

        Point playerHead = gamePanel.getSnake().getFirst();
        Point head = getHead();
        int bestScore = -1;
        char bestDir = direction;

        for (char d : new char[]{'U', 'D', 'L', 'R'}) {
            if ((direction == 'U' && d == 'D') || (direction == 'D' && d == 'U') ||
                    (direction == 'L' && d == 'R') || (direction == 'R' && d == 'L')) continue;
            int newX = head.x, newY = head.y;
            switch (d) {
                case 'U': newY -= GamePanel.UNIT_SIZE; break;
                case 'D': newY += GamePanel.UNIT_SIZE; break;
                case 'L': newX -= GamePanel.UNIT_SIZE; break;
                case 'R': newX += GamePanel.UNIT_SIZE; break;
            }
            if (newX < 0 || newX >= GamePanel.BOARD_WIDTH || newY < 0 || newY >= GamePanel.BOARD_HEIGHT) continue;
            if (!gamePanel.isValidMoveForAI(newX, newY, this)) continue;

            double foodScore = 0;
            if (!targets.isEmpty()) {
                double minDist = Double.MAX_VALUE;
                for (Point t : targets) {
                    double dx = newX - t.x, dy = newY - t.y;
                    double dist = Math.sqrt(dx*dx + dy*dy);
                    if (dist < minDist) minDist = dist;
                }
                foodScore = -minDist;
            }
            double playerScore = 0;
            if (aggression > 0) {
                double dx = newX - playerHead.x, dy = newY - playerHead.y;
                double distToPlayer = Math.sqrt(dx*dx + dy*dy);
                playerScore = -distToPlayer;
            }
            double totalScore = foodScore + playerScore * (aggression * 2);
            if (totalScore > bestScore) {
                bestScore = (int)totalScore;
                bestDir = d;
            }
        }
        if (bestScore != -1) {
            nextDirection = bestDir;
        } else {
            randomDirection();
        }
    }

    private void randomDirection() {
        char[] dirs = {'U', 'D', 'L', 'R'};
        LinkedList<Character> valid = new LinkedList<>();
        Point head = getHead();
        for (char d : dirs) {
            if ((direction == 'U' && d == 'D') || (direction == 'D' && d == 'U') ||
                    (direction == 'L' && d == 'R') || (direction == 'R' && d == 'L')) continue;
            int newX = head.x, newY = head.y;
            switch (d) {
                case 'U': newY -= GamePanel.UNIT_SIZE; break;
                case 'D': newY += GamePanel.UNIT_SIZE; break;
                case 'L': newX -= GamePanel.UNIT_SIZE; break;
                case 'R': newX += GamePanel.UNIT_SIZE; break;
            }
            if (newX >= 0 && newX < GamePanel.BOARD_WIDTH && newY >= 0 && newY < GamePanel.BOARD_HEIGHT &&
                    gamePanel.isValidMoveForAI(newX, newY, this)) {
                valid.add(d);
            }
        }
        if (!valid.isEmpty()) {
            nextDirection = valid.get(random.nextInt(valid.size()));
        } else {
            nextDirection = direction;
        }
    }

    public void move() {
        direction = nextDirection;
        Point head = getHead();
        int newX = head.x, newY = head.y;
        switch (direction) {
            case 'U': newY -= GamePanel.UNIT_SIZE; break;
            case 'D': newY += GamePanel.UNIT_SIZE; break;
            case 'L': newX -= GamePanel.UNIT_SIZE; break;
            case 'R': newX += GamePanel.UNIT_SIZE; break;
        }
        if (newX < 0 || newX >= GamePanel.BOARD_WIDTH || newY < 0 || newY >= GamePanel.BOARD_HEIGHT) {
            // 边界死亡，将由 GamePanel 处理
            body.addFirst(new Point(newX, newY));
            return;
        }
        Point newHead = new Point(newX, newY);

        boolean ateFood = false;
        Point eatenFood = null;
        for (Point f : gamePanel.getFoods()) {
            if (newHead.equals(f)) {
                ateFood = true;
                eatenFood = f;
                break;
            }
        }
        boolean ateMiniFood = false;
        Point eatenMini = null;
        for (Point mf : gamePanel.getMiniFoods()) {
            if (newHead.equals(mf)) {
                ateMiniFood = true;
                eatenMini = mf;
                break;
            }
        }
        boolean ateCoin = gamePanel.hasCoin() && newHead.equals(gamePanel.getCoin());

        body.addFirst(newHead);
        if (!ateFood && !ateMiniFood) {
            body.removeLast();
        } else {
            if (ateFood) gamePanel.removeFood(eatenFood);
            else if (ateMiniFood) gamePanel.removeMiniFood(eatenMini);
        }
        if (ateCoin) gamePanel.removeCoin();
    }
}