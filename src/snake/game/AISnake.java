package snake.game;

import java.awt.Point;
import java.util.LinkedList;
import java.util.Random;

public class AISnake {
    private LinkedList<Point> body;
    private char direction;
    private char nextDirection;
    private final Random random = new Random();
    private final GameState state;
    private int aggression;
    private int hp;
    private int maxHp;

    public AISnake(GameState state, Point startHead, int initialLength, int difficulty) {
        this.state = state;
        this.aggression = difficulty;
        body = new LinkedList<>();
        body.add(startHead);
        for (int i = 1; i < initialLength; i++) {
            body.add(new Point(startHead.x - i * GameState.UNIT_SIZE, startHead.y));
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
    public void takeDamage(int amount) { hp -= amount; if (hp < 0) hp = 0; }

    public void decideDirection() {
        LinkedList<Point> targets = new LinkedList<>();
        targets.addAll(state.getFoods());
        targets.addAll(state.getMiniFoods());
        if (state.hasCoin() && state.getCoin() != null) targets.add(state.getCoin());

        Point playerHead = state.getSnake().getFirst();
        Point head = getHead();
        int bestScore = -1;
        char bestDir = direction;

        for (char d : new char[]{'U', 'D', 'L', 'R'}) {
            if ((direction == 'U' && d == 'D') || (direction == 'D' && d == 'U') ||
                    (direction == 'L' && d == 'R') || (direction == 'R' && d == 'L')) continue;
            int newX = head.x, newY = head.y;
            switch (d) {
                case 'U': newY -= GameState.UNIT_SIZE; break;
                case 'D': newY += GameState.UNIT_SIZE; break;
                case 'L': newX -= GameState.UNIT_SIZE; break;
                case 'R': newX += GameState.UNIT_SIZE; break;
            }
            if (newX < 0 || newX >= GameState.BOARD_WIDTH || newY < 0 || newY >= GameState.BOARD_HEIGHT) continue;
            if (!isValidMove(newX, newY)) continue;

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
        if (bestScore != -1) nextDirection = bestDir;
        else randomDirection();
    }

    private boolean isValidMove(int x, int y) {
        Point newHead = new Point(x, y);
        if (state.getStones().contains(newHead)) return false;
        if (state.getSnake().contains(newHead)) return false;
        for (AISnake other : state.getAiSnakes()) {
            if (other == this) {
                for (int i = 1; i < other.getBody().size(); i++) {
                    if (other.getBody().get(i).equals(newHead)) return false;
                }
            } else {
                if (other.getBody().contains(newHead)) return false;
            }
        }
        return true;
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
                case 'U': newY -= GameState.UNIT_SIZE; break;
                case 'D': newY += GameState.UNIT_SIZE; break;
                case 'L': newX -= GameState.UNIT_SIZE; break;
                case 'R': newX += GameState.UNIT_SIZE; break;
            }
            if (newX >= 0 && newX < GameState.BOARD_WIDTH && newY >= 0 && newY < GameState.BOARD_HEIGHT && isValidMove(newX, newY)) {
                valid.add(d);
            }
        }
        if (!valid.isEmpty()) nextDirection = valid.get(random.nextInt(valid.size()));
        else nextDirection = direction;
    }

    public void move() {
        direction = nextDirection;
        Point head = getHead();
        int newX = head.x, newY = head.y;
        switch (direction) {
            case 'U': newY -= GameState.UNIT_SIZE; break;
            case 'D': newY += GameState.UNIT_SIZE; break;
            case 'L': newX -= GameState.UNIT_SIZE; break;
            case 'R': newX += GameState.UNIT_SIZE; break;
        }
        if (newX < 0 || newX >= GameState.BOARD_WIDTH || newY < 0 || newY >= GameState.BOARD_HEIGHT) {
            body.addFirst(new Point(newX, newY));
            return;
        }
        Point newHead = new Point(newX, newY);

        boolean ateFood = false;
        Point eatenFood = null;
        for (Point f : state.getFoods()) {
            if (newHead.equals(f)) { ateFood = true; eatenFood = f; break; }
        }
        boolean ateMini = false;
        Point eatenMini = null;
        for (Point mf : state.getMiniFoods()) {
            if (newHead.equals(mf)) { ateMini = true; eatenMini = mf; break; }
        }
        boolean ateCoin = state.hasCoin() && newHead.equals(state.getCoin());

        body.addFirst(newHead);
        if (!ateFood && !ateMini) body.removeLast();
        else {
            if (ateFood) state.getFoods().remove(eatenFood);
            else if (ateMini) state.getMiniFoods().remove(eatenMini);
        }
        if (ateCoin) state.setHasCoin(false);
    }
}