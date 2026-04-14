package snake.game;

import java.awt.Point;
import java.util.LinkedList;
import java.util.Random;

public class AIManager {
    private final GameState state;
    private final Random random = new Random();
    private final ItemGenerator itemGenerator;

    public AIManager(GameState state, ItemGenerator itemGenerator) {
        this.state = state;
        this.itemGenerator = itemGenerator;
    }

    public void initAISnakes() {
        state.getAiSnakes().clear();
        for (int i = 0; i < GameState.AI_SNAKE_COUNT; i++) {
            spawnNewAISnake();
        }
    }

    public void spawnNewAISnake() {
        Point spawn = findSafeSpawn();
        if (spawn != null) {
            state.getAiSnakes().add(new AISnake(state, spawn, GameState.AI_SNAKE_INIT_LENGTH, state.getCurrentDifficulty()));
        }
    }

    private Point findSafeSpawn() {
        for (int attempt = 0; attempt < 1000; attempt++) {
            int x = random.nextInt(GameState.BOARD_WIDTH / GameState.UNIT_SIZE) * GameState.UNIT_SIZE;
            int y = random.nextInt(GameState.BOARD_HEIGHT / GameState.UNIT_SIZE) * GameState.UNIT_SIZE;
            Point p = new Point(x, y);
            if (isSafe(p)) return p;
        }
        return null;
    }

    private boolean isSafe(Point p) {
        if (state.getSnake().contains(p)) return false;
        if (state.getFoods().contains(p)) return false;
        if (state.getMiniFoods().contains(p)) return false;
        if (state.getStones().contains(p)) return false;
        if (state.hasCoin() && p.equals(state.getCoin())) return false;
        if (state.hasHealthItem() && p.equals(state.getHealthItem())) return false;
        if (state.hasShieldItem() && p.equals(state.getShieldItem())) return false;
        if (state.hasInvincibleItem() && p.equals(state.getInvincibleItem())) return false;
        for (AISnake ai : state.getAiSnakes()) {
            if (ai.getBody().contains(p)) return false;
        }
        return true;
    }

    public void updateAndMove() {
        LinkedList<AISnake> deadAI = new LinkedList<>();
        for (AISnake ai : state.getAiSnakes()) {
            ai.decideDirection();
            ai.move();
            Point head = ai.getHead();
            boolean dead = false;
            if (head.x < 0 || head.x >= GameState.BOARD_WIDTH || head.y < 0 || head.y >= GameState.BOARD_HEIGHT) dead = true;
            if (state.getStones().contains(head)) dead = true;
            if (state.getSnake().contains(head)) dead = true;
            for (AISnake other : state.getAiSnakes()) {
                if (other != ai && other.getBody().contains(head)) { dead = true; break; }
            }
            for (int i = 1; i < ai.getBody().size(); i++) {
                if (ai.getBody().get(i).equals(head)) { dead = true; break; }
            }
            if (dead || ai.getHp() <= 0) {
                itemGenerator.generateOneFood();
                deadAI.add(ai);
                state.setScore(state.getScore() + 10);
            }
        }
        state.getAiSnakes().removeAll(deadAI);
        for (int i = 0; i < deadAI.size(); i++) {
            spawnNewAISnake();
        }
    }

    public void setAllAggression(int aggression) {
        for (AISnake ai : state.getAiSnakes()) {
            ai.setAggression(aggression);
        }
    }
}