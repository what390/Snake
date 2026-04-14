package snake.game;

import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.util.function.BooleanSupplier;

public class ItemGenerator {
    private final GameState state;
    private final Random random = new Random();
    private Timer coinTimer, stoneGenTimer, stoneLifetimeChecker, invincibleGenTimer;
    private Timer normalFoodTimer, healthTimer, shieldTimer, miniFoodTimer;

    public ItemGenerator(GameState state) {
        this.state = state;
    }

    public void startCoinGenerator(Runnable onRefresh) {
        if (coinTimer != null) coinTimer.stop();
        coinTimer = new Timer(GameState.COIN_GEN_DELAY, e -> {
            if (state.isRunning() && !state.hasCoin() && !state.isDead()) {
                generateCoin();
                onRefresh.run();
            }
        });
        coinTimer.start();
    }

    public void generateCoin() {
        if (state.hasCoin()) return;
        for (int attempt = 0; attempt < 1000; attempt++) {
            int x = random.nextInt(GameState.BOARD_WIDTH / GameState.UNIT_SIZE) * GameState.UNIT_SIZE;
            int y = random.nextInt(GameState.BOARD_HEIGHT / GameState.UNIT_SIZE) * GameState.UNIT_SIZE;
            Point p = new Point(x, y);
            if (isPositionFree(p, true)) {
                state.setCoin(p);
                state.setHasCoin(true);
                return;
            }
        }
    }

    public void startStoneGenerator(Runnable onRefresh) {
        stoneGenTimer = new Timer(GameState.STONE_GEN_INTERVAL, e -> {
            if (state.isRunning() && !state.isDead()) {
                int num = GameState.MIN_STONES_PER_BATCH + random.nextInt(GameState.MAX_STONES_PER_BATCH - GameState.MIN_STONES_PER_BATCH + 1);
                if (state.getStones().size() >= GameState.MAX_STONES_TOTAL) return;
                int generated = 0, attempts = 0;
                while (generated < num && attempts < 1000 && state.getStones().size() < GameState.MAX_STONES_TOTAL) {
                    int x = random.nextInt(GameState.BOARD_WIDTH / GameState.UNIT_SIZE) * GameState.UNIT_SIZE;
                    int y = random.nextInt(GameState.BOARD_HEIGHT / GameState.UNIT_SIZE) * GameState.UNIT_SIZE;
                    Point p = new Point(x, y);
                    if (isPositionFree(p, false)) {
                        state.getStones().add(p);
                        state.getStoneBirthTime().put(p, System.currentTimeMillis());
                        generated++;
                    }
                    attempts++;
                }
                onRefresh.run();
            }
        });
        stoneGenTimer.start();

        stoneLifetimeChecker = new Timer(500, e -> {
            long now = System.currentTimeMillis();
            var toRemove = state.getStones().stream()
                    .filter(s -> state.getStoneBirthTime().get(s) != null &&
                            (now - state.getStoneBirthTime().get(s)) >= GameState.STONE_LIFE)
                    .toList();
            state.getStones().removeAll(toRemove);
            toRemove.forEach(s -> state.getStoneBirthTime().remove(s));
            if (!toRemove.isEmpty()) onRefresh.run();
        });
        stoneLifetimeChecker.start();
    }

    public void startInvincibleItemGenerator(Runnable onRefresh) {
        invincibleGenTimer = new Timer(15000, e -> {
            if (state.isRunning() && !state.isDead() && !state.hasInvincibleItem()) {
                generateInvincibleItem();
                onRefresh.run();
            }
        });
        invincibleGenTimer.start();
    }

    private void generateInvincibleItem() {
        for (int attempt = 0; attempt < 1000; attempt++) {
            int x = random.nextInt(GameState.BOARD_WIDTH / GameState.UNIT_SIZE) * GameState.UNIT_SIZE;
            int y = random.nextInt(GameState.BOARD_HEIGHT / GameState.UNIT_SIZE) * GameState.UNIT_SIZE;
            Point p = new Point(x, y);
            if (isPositionFree(p, false)) {
                state.setInvincibleItem(p);
                state.setHasInvincibleItem(true);
                return;
            }
        }
    }

    public void startNormalFoodGenerator(Runnable onRefresh, BooleanSupplier condition) {
        if (normalFoodTimer != null) normalFoodTimer.stop();
        normalFoodTimer = new Timer(GameState.NORMAL_FOOD_GEN_DELAY, e -> {
            if (condition.getAsBoolean()) {
                while (state.getFoods().size() < GameState.NORMAL_FOOD_MAX &&
                        state.getSnake().size() < GameState.GAME_UNITS) {
                    generateOneFood();
                }
                onRefresh.run();
            }
        });
        normalFoodTimer.start();
    }

    public void generateOneFood() {
        for (int attempt = 0; attempt < 2000; attempt++) {
            int x = random.nextInt(GameState.BOARD_WIDTH / GameState.UNIT_SIZE) * GameState.UNIT_SIZE;
            int y = random.nextInt(GameState.BOARD_HEIGHT / GameState.UNIT_SIZE) * GameState.UNIT_SIZE;
            Point p = new Point(x, y);
            if (isPositionFree(p, false)) {
                state.getFoods().add(p);
                return;
            }
        }
    }

    public void startHealthItemGenerator(Runnable onRefresh, BooleanSupplier condition) {
        healthTimer = new Timer(10000, e -> {
            if (condition.getAsBoolean() && !state.hasHealthItem()) {
                generateHealthItem();
                onRefresh.run();
            }
        });
        healthTimer.start();
    }

    private void generateHealthItem() {
        for (int attempt = 0; attempt < 1000; attempt++) {
            int x = random.nextInt(GameState.BOARD_WIDTH / GameState.UNIT_SIZE) * GameState.UNIT_SIZE;
            int y = random.nextInt(GameState.BOARD_HEIGHT / GameState.UNIT_SIZE) * GameState.UNIT_SIZE;
            Point p = new Point(x, y);
            if (isPositionFree(p, false)) {
                state.setHealthItem(p);
                state.setHasHealthItem(true);
                return;
            }
        }
    }

    public void startShieldItemGenerator(Runnable onRefresh, BooleanSupplier condition) {
        shieldTimer = new Timer(15000, e -> {
            if (condition.getAsBoolean() && !state.hasShieldItem()) {
                generateShieldItem();
                onRefresh.run();
            }
        });
        shieldTimer.start();
    }

    private void generateShieldItem() {
        for (int attempt = 0; attempt < 1000; attempt++) {
            int x = random.nextInt(GameState.BOARD_WIDTH / GameState.UNIT_SIZE) * GameState.UNIT_SIZE;
            int y = random.nextInt(GameState.BOARD_HEIGHT / GameState.UNIT_SIZE) * GameState.UNIT_SIZE;
            Point p = new Point(x, y);
            if (isPositionFree(p, false)) {
                state.setShieldItem(p);
                state.setHasShieldItem(true);
                return;
            }
        }
    }

    public void startMiniFoodGenerator(Runnable onRefresh) {
        miniFoodTimer = new Timer(3000, e -> {
            if (state.isRunning() && !state.isDead() && !state.isCarnival()) {
                if (state.getMiniFoods().size() < 5) {
                    generateMiniFood();
                    onRefresh.run();
                }
            }
        });
        miniFoodTimer.start();
    }

    private void generateMiniFood() {
        for (int attempt = 0; attempt < 1000; attempt++) {
            int x = random.nextInt(GameState.BOARD_WIDTH / GameState.UNIT_SIZE) * GameState.UNIT_SIZE;
            int y = random.nextInt(GameState.BOARD_HEIGHT / GameState.UNIT_SIZE) * GameState.UNIT_SIZE;
            Point p = new Point(x, y);
            if (isPositionFree(p, false)) {
                state.getMiniFoods().add(p);
                return;
            }
        }
    }

    private boolean isPositionFree(Point p, boolean includeCoin) {
        if (state.getSnake().contains(p)) return false;
        if (state.getFoods().contains(p)) return false;
        if (state.getMiniFoods().contains(p)) return false;
        if (state.getStones().contains(p)) return false;
        if (includeCoin && state.hasCoin() && p.equals(state.getCoin())) return false;
        if (state.hasHealthItem() && p.equals(state.getHealthItem())) return false;
        if (state.hasShieldItem() && p.equals(state.getShieldItem())) return false;
        if (state.hasInvincibleItem() && p.equals(state.getInvincibleItem())) return false;
        return true;
    }
}