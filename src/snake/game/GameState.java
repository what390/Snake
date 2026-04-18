package snake.game;

import java.awt.Point;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;

public class GameState {
    // 常量
    public static final int BOARD_WIDTH = 600;
    public static final int BOARD_HEIGHT = 600;
    public static final int UNIT_SIZE = 25;
    public static final int GAME_UNITS = (BOARD_WIDTH * BOARD_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    public static final int REVIVE_COST = 5;
    public static final int COIN_GEN_DELAY = 5000;
    public static final int SPEED_SLOW = 150;
    public static final int SPEED_NORMAL = 100;
    public static final int SPEED_FAST = 70;
    public static final int INVINCIBLE_DURATION = 5000;
    public static final int STONE_GEN_INTERVAL = 15000;
    public static final int STONE_LIFE = 10000;
    public static final int MIN_STONES_PER_BATCH = 3;
    public static final int MAX_STONES_PER_BATCH = 6;
    public static final int MAX_STONES_TOTAL = 20;
    public static final int CARNIVAL_DURATION = 2000;
    public static final float CARNIVAL_FOOD_RATIO = 0.04f;
    public static final int NORMAL_FOOD_MAX = 3;
    public static final int NORMAL_FOOD_GEN_DELAY = 3000;
    public static final int PLAYER_MAX_HEALTH = 5;
    public static final int PLAYER_START_HEALTH = 3;
    public static final int MAX_SHIELD = 3;
    public static final int MINI_FOOD_COUNT_FOR_BONUS = 5;
    public static final int AI_SNAKE_COUNT = 3;
    public static final int AI_SNAKE_INIT_LENGTH = 3;

    // 游戏状态
    private boolean isRunning = true;
    private boolean isDead = false;
    private int score = 0;
    private int coins = 0;
    private int bodyLength = 3;
    private int deadLength = 0;
    private int currentDelay = SPEED_NORMAL;
    private boolean isInvincible = false;
    private boolean isCarnival = false;
    private int currentDifficulty = 1;

    // 玩家属性
    private int playerHealth = PLAYER_START_HEALTH;
    private int playerMaxHealth = PLAYER_START_HEALTH;
    private int playerShield = 0;

    // 玩家蛇
    private final LinkedList<Point> snake = new LinkedList<>();
    private char direction = 'R';
    private char nextDirection = 'R';

    // AI蛇列表
    private final LinkedList<AISnake> aiSnakes = new LinkedList<>();

    // 食物列表
    private final LinkedList<Point> foods = new LinkedList<>();
    private Point coin;
    private boolean hasCoin = false;

    // 迷你豆豆
    private final LinkedList<Point> miniFoods = new LinkedList<>();
    private int miniFoodEatenCount = 0;

    // 石头
    private final LinkedList<Point> stones = new LinkedList<>();
    private final Map<Point, Long> stoneBirthTime = new HashMap<>();

    // 道具
    private Point healthItem;
    private boolean hasHealthItem = false;
    private Point shieldItem;
    private boolean hasShieldItem = false;
    private Point invincibleItem;
    private boolean hasInvincibleItem = false;

    public GameState(int speedLevel) {
        setSpeedLevel(speedLevel);
    }

    public void initGame() {
        snake.clear();
        foods.clear();
        miniFoods.clear();
        stones.clear();
        stoneBirthTime.clear();
        aiSnakes.clear();
        hasCoin = false;
        coin = null;
        hasHealthItem = false;
        healthItem = null;
        hasShieldItem = false;
        shieldItem = null;
        hasInvincibleItem = false;
        invincibleItem = null;
        isInvincible = false;
        isCarnival = false;
        miniFoodEatenCount = 0;
        SoundManager.getInstance().playBackgroundMusic();   // 无参，内部使用 res/bgm.wav
        int startX = BOARD_WIDTH / 2 / UNIT_SIZE * UNIT_SIZE;
        int startY = BOARD_HEIGHT / 2 / UNIT_SIZE * UNIT_SIZE;
        snake.add(new Point(startX, startY));
        snake.add(new Point(startX - UNIT_SIZE, startY));
        snake.add(new Point(startX - 2 * UNIT_SIZE, startY));
        direction = 'R';
        nextDirection = 'R';
        score = 0;
        coins = 0;
        bodyLength = snake.size();
        deadLength = 0;
        playerHealth = PLAYER_START_HEALTH;
        playerMaxHealth = PLAYER_START_HEALTH;
        playerShield = 0;
    }

    public void setSpeedLevel(int level) {
        switch (level) {
            case 0: currentDelay = SPEED_SLOW; break;
            case 1: currentDelay = SPEED_NORMAL; break;
            case 2: currentDelay = SPEED_FAST; break;
            default: currentDelay = SPEED_NORMAL;
        }
        currentDifficulty = level;
    }

    // ---------- Getters and Setters ----------
    public boolean isRunning() { return isRunning; }
    public void setRunning(boolean running) { isRunning = running; }
    public boolean isDead() { return isDead; }
    public void setDead(boolean dead) { isDead = dead; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }
    public int getBodyLength() { return bodyLength; }
    public void setBodyLength(int bodyLength) { this.bodyLength = bodyLength; }
    public int getDeadLength() { return deadLength; }
    public void setDeadLength(int deadLength) { this.deadLength = deadLength; }
    public int getCurrentDelay() { return currentDelay; }
    public boolean isInvincible() { return isInvincible; }
    public void setInvincible(boolean invincible) { isInvincible = invincible; }
    public boolean isCarnival() { return isCarnival; }
    public void setCarnival(boolean carnival) { isCarnival = carnival; }
    public int getCurrentDifficulty() { return currentDifficulty; }
    public void setCurrentDifficulty(int difficulty) { currentDifficulty = difficulty; }
    public int getPlayerHealth() { return playerHealth; }
    public void setPlayerHealth(int playerHealth) { this.playerHealth = playerHealth; }
    public int getPlayerMaxHealth() { return playerMaxHealth; }
    public void setPlayerMaxHealth(int playerMaxHealth) { this.playerMaxHealth = playerMaxHealth; }
    public int getPlayerShield() { return playerShield; }
    public void setPlayerShield(int playerShield) { this.playerShield = playerShield; }
    public LinkedList<Point> getSnake() { return snake; }
    public char getDirection() { return direction; }
    public void setDirection(char direction) { this.direction = direction; }
    public char getNextDirection() { return nextDirection; }
    public void setNextDirection(char nextDirection) { this.nextDirection = nextDirection; }
    public LinkedList<AISnake> getAiSnakes() { return aiSnakes; }
    public LinkedList<Point> getFoods() { return foods; }
    public Point getCoin() { return coin; }
    public void setCoin(Point coin) { this.coin = coin; }
    public boolean hasCoin() { return hasCoin; }
    public void setHasCoin(boolean hasCoin) { this.hasCoin = hasCoin; }
    public LinkedList<Point> getMiniFoods() { return miniFoods; }
    public int getMiniFoodEatenCount() { return miniFoodEatenCount; }
    public void setMiniFoodEatenCount(int count) { miniFoodEatenCount = count; }
    public LinkedList<Point> getStones() { return stones; }
    public Map<Point, Long> getStoneBirthTime() { return stoneBirthTime; }
    public Point getHealthItem() { return healthItem; }
    public void setHealthItem(Point healthItem) { this.healthItem = healthItem; }
    public boolean hasHealthItem() { return hasHealthItem; }
    public void setHasHealthItem(boolean hasHealthItem) { this.hasHealthItem = hasHealthItem; }
    public Point getShieldItem() { return shieldItem; }
    public void setShieldItem(Point shieldItem) { this.shieldItem = shieldItem; }
    public boolean hasShieldItem() { return hasShieldItem; }
    public void setHasShieldItem(boolean hasShieldItem) { this.hasShieldItem = hasShieldItem; }
    public Point getInvincibleItem() { return invincibleItem; }
    public void setInvincibleItem(Point invincibleItem) { this.invincibleItem = invincibleItem; }
    public boolean hasInvincibleItem() { return hasInvincibleItem; }
    public void setHasInvincibleItem(boolean hasInvincibleItem) { this.hasInvincibleItem = hasInvincibleItem; }
}