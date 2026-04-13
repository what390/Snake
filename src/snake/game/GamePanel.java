package snake.game;

import snake.main.GameMainFrame;
import snake.ranking.RankingManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    // 常量
    public static final int BOARD_WIDTH = 600;
    public static final int BOARD_HEIGHT = 600;
    public static final int UNIT_SIZE = 25;
    private static final int GAME_UNITS = (BOARD_WIDTH * BOARD_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    private static final int REVIVE_COST = 5;
    private static final int COIN_GEN_DELAY = 5000;
    private static final int SPEED_SLOW = 150;
    private static final int SPEED_NORMAL = 100;
    private static final int SPEED_FAST = 70;
    private static final int INVINCIBLE_DURATION = 5000;
    private static final int STONE_GEN_INTERVAL = 15000;
    private static final int STONE_LIFE = 10000;
    private static final int MIN_STONES_PER_BATCH = 3;
    private static final int MAX_STONES_PER_BATCH = 6;
    private static final int MAX_STONES_TOTAL = 20;
    private static final int CARNIVAL_DURATION = 2000;
    private static final float CARNIVAL_FOOD_RATIO = 0.1f;
    private static final int NORMAL_FOOD_MAX = 3;
    private static final int NORMAL_FOOD_GEN_DELAY = 3000;
    private static final int PLAYER_MAX_HEALTH = 5;
    private static final int PLAYER_START_HEALTH = 3;
    private static final int MAX_SHIELD = 3;
    private static final int MINI_FOOD_COUNT_FOR_BONUS = 5; // 5个迷你豆豆换一个长度

    // AI蛇相关常量
    private static final int AI_SNAKE_COUNT = 3;          // 3条普通AI蛇
    private static final int AI_SNAKE_INIT_LENGTH = 3;

    // 游戏状态
    private boolean isRunning = true;
    private boolean isDead = false;
    private Timer timer;
    private Timer coinTimer;
    private Timer invincibleTimer;
    private Timer carnivalTimer;
    private Timer normalFoodTimer;
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

    // 迷你豆豆列表
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

    // 无敌道具
    private Point invincibleItem;
    private boolean hasInvincibleItem = false;

    private final Random random = new Random();
    private final GameMainFrame parentFrame;
    private final GameAssets assets;

    // 颜色常量
    private static final Color HEALTH_BAR_BG = Color.RED;
    private static final Color HEALTH_BAR_FG = Color.GREEN;
    private static final Color UI_TEXT = Color.WHITE;

    public GamePanel(GameMainFrame parent, String playerName, int speedLevel) {
        this.parentFrame = parent;
        this.assets = new GameAssets();
        this.currentDifficulty = speedLevel;
        switch (speedLevel) {
            case 0: currentDelay = SPEED_SLOW; break;
            case 1: currentDelay = SPEED_NORMAL; break;
            case 2: currentDelay = SPEED_FAST; break;
            default: currentDelay = SPEED_NORMAL;
        }
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setBackground(Color.black);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        addKeyListener(this);
        initGame();
        startGame();
        startCoinGenerator();
        startStoneGenerator();
        startInvincibleItemGenerator();
        startNormalFoodGenerator();
        startHealthItemGenerator();
        startShieldItemGenerator();
        startMiniFoodGenerator();
    }

    // ---------- 初始化 ----------
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
        if (invincibleTimer != null) invincibleTimer.stop();
        if (carnivalTimer != null) carnivalTimer.stop();

        // 玩家蛇
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
        updateInfoDisplay();
        generateFood();
        while (foods.size() < NORMAL_FOOD_MAX && !isCarnival) generateFood();

        // AI蛇：全部为普通蛇
        for (int i = 0; i < AI_SNAKE_COUNT; i++) {
            spawnNewAISnake();
        }

        isRunning = true;
        isDead = false;
        if (timer != null && timer.isRunning()) timer.stop();
    }

    // 生成一条新AI蛇（在安全位置）
    private void spawnNewAISnake() {
        Point spawn = findSafeSpawn();
        if (spawn != null) {
            aiSnakes.add(new AISnake(this, spawn, AI_SNAKE_INIT_LENGTH, currentDifficulty));
        }
    }

    private Point findSafeSpawn() {
        int maxAttempts = 1000;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = random.nextInt(BOARD_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            int y = random.nextInt(BOARD_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
            Point p = new Point(x, y);
            if (!snake.contains(p) && !foods.contains(p) && !miniFoods.contains(p) && !stones.contains(p) &&
                    (coin == null || !p.equals(coin)) && (healthItem == null || !p.equals(healthItem)) &&
                    (shieldItem == null || !p.equals(shieldItem)) && (invincibleItem == null || !p.equals(invincibleItem))) {
                // 还要避开其他AI蛇
                boolean occupiedByAI = false;
                for (AISnake ai : aiSnakes) {
                    if (ai.getBody().contains(p)) {
                        occupiedByAI = true;
                        break;
                    }
                }
                if (!occupiedByAI) return p;
            }
        }
        return null;
    }

    // 公共访问方法
    public LinkedList<Point> getFoods() { return foods; }
    public LinkedList<Point> getMiniFoods() { return miniFoods; }
    public boolean hasCoin() { return hasCoin; }
    public Point getCoin() { return coin; }
    public LinkedList<Point> getSnake() { return snake; }
    public void removeFood(Point f) { foods.remove(f); }
    public void removeMiniFood(Point f) { miniFoods.remove(f); }
    public void removeCoin() { hasCoin = false; coin = null; }
    public int getDifficulty() { return currentDifficulty; }
    public void damagePlayer(int amount) {
        if (isInvincible) return;
        if (playerShield > 0) {
            playerShield--;
        } else {
            playerHealth -= amount;
            if (playerHealth <= 0) {
                playerHealth = 0;
                deadLength = snake.size() - 1;
                isRunning = false;
                isDead = true;
                if (timer != null) timer.stop();
                int option = JOptionPane.showConfirmDialog(this,
                        "你死了！消耗 " + REVIVE_COST + " 金币复活吗？\n当前金币: " + coins,
                        "复活确认", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) attemptRevive();
                else gameOver();
            }
        }
        updateInfoDisplay();
        repaint();
    }

    public boolean isValidMoveForAI(int x, int y, AISnake aiSnake) {
        if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) return false;
        Point newHead = new Point(x, y);
        if (stones.contains(newHead)) return false;
        if (snake.contains(newHead)) return false;
        for (AISnake other : aiSnakes) {
            if (other == aiSnake) {
                for (int i = 1; i < other.getBody().size(); i++) {
                    if (other.getBody().get(i).equals(newHead)) return false;
                }
            } else {
                if (other.getBody().contains(newHead)) return false;
            }
        }
        return true;
    }

    // 生成器（与之前相同，省略部分重复代码，保持原有逻辑）
    private void startGame() {
        if (timer != null) timer.stop();
        timer = new Timer(currentDelay, this);
        timer.start();
    }

    public void setSpeed(int speedLevel) {
        currentDifficulty = speedLevel;
        switch (speedLevel) {
            case 0: currentDelay = SPEED_SLOW; break;
            case 1: currentDelay = SPEED_NORMAL; break;
            case 2: currentDelay = SPEED_FAST; break;
            default: currentDelay = SPEED_NORMAL;
        }
        if (timer != null && isRunning && !isDead) timer.setDelay(currentDelay);
        for (AISnake ai : aiSnakes) ai.setAggression(speedLevel);
    }

    // 金币生成
    private void startCoinGenerator() {
        if (coinTimer != null && coinTimer.isRunning()) coinTimer.stop();
        coinTimer = new Timer(COIN_GEN_DELAY, e -> {
            if (isRunning && !hasCoin && !isDead) generateCoin();
        });
        coinTimer.start();
    }

    private void generateCoin() {
        if (hasCoin) return;
        int maxAttempts = 1000;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = random.nextInt(BOARD_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            int y = random.nextInt(BOARD_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
            Point newCoin = new Point(x, y);
            if (!snake.contains(newCoin) && !foods.contains(newCoin) && !miniFoods.contains(newCoin) && !stones.contains(newCoin) &&
                    (healthItem == null || !newCoin.equals(healthItem)) && (shieldItem == null || !newCoin.equals(shieldItem)) &&
                    (invincibleItem == null || !newCoin.equals(invincibleItem))) {
                coin = newCoin;
                hasCoin = true;
                repaint();
                return;
            }
        }
    }

    // 石头生成（批量）
    private void startStoneGenerator() {
        Timer stoneGenTimer = new Timer(STONE_GEN_INTERVAL, e -> {
            if (isRunning && !isDead) {
                int numToGenerate = MIN_STONES_PER_BATCH + random.nextInt(MAX_STONES_PER_BATCH - MIN_STONES_PER_BATCH + 1);
                if (stones.size() >= MAX_STONES_TOTAL) return;
                int generated = 0, attempts = 0;
                while (generated < numToGenerate && attempts < 1000 && stones.size() < MAX_STONES_TOTAL) {
                    int x = random.nextInt(BOARD_WIDTH / UNIT_SIZE) * UNIT_SIZE;
                    int y = random.nextInt(BOARD_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
                    Point newStone = new Point(x, y);
                    if (!snake.contains(newStone) && !foods.contains(newStone) && !miniFoods.contains(newStone) &&
                            (coin == null || !newStone.equals(coin)) && !stones.contains(newStone) &&
                            (healthItem == null || !newStone.equals(healthItem)) && (shieldItem == null || !newStone.equals(shieldItem)) &&
                            (invincibleItem == null || !newStone.equals(invincibleItem))) {
                        stones.add(newStone);
                        stoneBirthTime.put(newStone, System.currentTimeMillis());
                        generated++;
                    }
                    attempts++;
                }
                repaint();
            }
        });
        stoneGenTimer.start();

        Timer stoneLifetimeChecker = new Timer(500, e -> {
            long now = System.currentTimeMillis();
            LinkedList<Point> toRemove = new LinkedList<>();
            for (Point s : stones) {
                Long birth = stoneBirthTime.get(s);
                if (birth != null && (now - birth) >= STONE_LIFE) toRemove.add(s);
            }
            stones.removeAll(toRemove);
            for (Point s : toRemove) stoneBirthTime.remove(s);
            if (!toRemove.isEmpty()) repaint();
        });
        stoneLifetimeChecker.start();
    }

    // 无敌道具生成
    private void startInvincibleItemGenerator() {
        Timer invincibleGenTimer = new Timer(15000, e -> {
            if (isRunning && !isDead && !hasInvincibleItem) generateInvincibleItem();
        });
        invincibleGenTimer.start();
    }

    private void generateInvincibleItem() {
        int maxAttempts = 1000;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = random.nextInt(BOARD_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            int y = random.nextInt(BOARD_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
            Point newItem = new Point(x, y);
            if (!snake.contains(newItem) && !foods.contains(newItem) && !miniFoods.contains(newItem) &&
                    (coin == null || !newItem.equals(coin)) && !stones.contains(newItem) &&
                    (healthItem == null || !newItem.equals(healthItem)) && (shieldItem == null || !newItem.equals(shieldItem))) {
                invincibleItem = newItem;
                hasInvincibleItem = true;
                repaint();
                return;
            }
        }
    }

    // 血量道具生成
    private void startHealthItemGenerator() {
        Timer healthTimer = new Timer(10000, e -> {
            if (isRunning && !isDead && !hasHealthItem && playerHealth < playerMaxHealth) {
                generateHealthItem();
            }
        });
        healthTimer.start();
    }

    private void generateHealthItem() {
        int maxAttempts = 1000;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = random.nextInt(BOARD_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            int y = random.nextInt(BOARD_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
            Point newItem = new Point(x, y);
            if (!snake.contains(newItem) && !foods.contains(newItem) && !miniFoods.contains(newItem) &&
                    (coin == null || !newItem.equals(coin)) && !stones.contains(newItem) &&
                    (invincibleItem == null || !newItem.equals(invincibleItem)) && (shieldItem == null || !newItem.equals(shieldItem))) {
                healthItem = newItem;
                hasHealthItem = true;
                repaint();
                return;
            }
        }
    }

    // 护盾道具生成
    private void startShieldItemGenerator() {
        Timer shieldTimer = new Timer(15000, e -> {
            if (isRunning && !isDead && !hasShieldItem && playerShield < MAX_SHIELD) {
                generateShieldItem();
            }
        });
        shieldTimer.start();
    }

    private void generateShieldItem() {
        int maxAttempts = 1000;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = random.nextInt(BOARD_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            int y = random.nextInt(BOARD_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
            Point newItem = new Point(x, y);
            if (!snake.contains(newItem) && !foods.contains(newItem) && !miniFoods.contains(newItem) &&
                    (coin == null || !newItem.equals(coin)) && !stones.contains(newItem) &&
                    (invincibleItem == null || !newItem.equals(invincibleItem)) && (healthItem == null || !newItem.equals(healthItem))) {
                shieldItem = newItem;
                hasShieldItem = true;
                repaint();
                return;
            }
        }
    }

    // 普通食物补充
    private void startNormalFoodGenerator() {
        if (normalFoodTimer != null) normalFoodTimer.stop();
        normalFoodTimer = new Timer(NORMAL_FOOD_GEN_DELAY, e -> {
            if (isRunning && !isDead && !isCarnival) {
                while (foods.size() < NORMAL_FOOD_MAX && snake.size() < GAME_UNITS) {
                    generateFood();
                }
            }
        });
        normalFoodTimer.start();
    }

    // 迷你豆豆生成器
    private void startMiniFoodGenerator() {
        Timer miniFoodTimer = new Timer(3000, e -> {
            if (isRunning && !isDead && !isCarnival) {
                if (miniFoods.size() < 5) {
                    generateMiniFood();
                }
            }
        });
        miniFoodTimer.start();
    }

    private void generateMiniFood() {
        int maxAttempts = 1000;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = random.nextInt(BOARD_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            int y = random.nextInt(BOARD_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
            Point newFood = new Point(x, y);
            if (!snake.contains(newFood) && !foods.contains(newFood) && !miniFoods.contains(newFood) &&
                    (coin == null || !newFood.equals(coin)) && !stones.contains(newFood) &&
                    (healthItem == null || !newFood.equals(healthItem)) && (shieldItem == null || !newFood.equals(shieldItem)) &&
                    (invincibleItem == null || !newFood.equals(invincibleItem))) {
                miniFoods.add(newFood);
                repaint();
                return;
            }
        }
    }

    private void generateFood() { generateFood(1); }
    private void generateFood(int count) {
        int totalCells = GAME_UNITS;
        if (snake.size() >= totalCells) { gameOver(); return; }
        int generated = 0, attempts = 0;
        while (generated < count && attempts < 2000) {
            int x = random.nextInt(BOARD_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            int y = random.nextInt(BOARD_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
            Point newFood = new Point(x, y);
            if (!snake.contains(newFood) && !foods.contains(newFood) && !miniFoods.contains(newFood) &&
                    (coin == null || !newFood.equals(coin)) && !stones.contains(newFood) &&
                    (healthItem == null || !newFood.equals(healthItem)) && (shieldItem == null || !newFood.equals(shieldItem)) &&
                    (invincibleItem == null || !newFood.equals(invincibleItem))) {
                foods.add(newFood);
                generated++;
            }
            attempts++;
        }
        repaint();
    }

    private void generateFoodAt(Point pos) {
        if (!snake.contains(pos) && !foods.contains(pos) && !miniFoods.contains(pos) &&
                (coin == null || !pos.equals(coin)) && !stones.contains(pos) &&
                (healthItem == null || !pos.equals(healthItem)) && (shieldItem == null || !pos.equals(shieldItem)) &&
                (invincibleItem == null || !pos.equals(invincibleItem))) {
            foods.add(pos);
        } else {
            generateFood();
        }
    }

    // 狂欢时刻
    private void startCarnival() {
        if (isCarnival) return;
        isCarnival = true;
        foods.clear();
        int totalCells = GAME_UNITS;
        int targetFoodCount = (int)(totalCells * CARNIVAL_FOOD_RATIO);
        int generated = 0, attempts = 0;
        while (generated < targetFoodCount && attempts < 10000) {
            int x = random.nextInt(BOARD_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            int y = random.nextInt(BOARD_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
            Point newFood = new Point(x, y);
            if (!snake.contains(newFood) && !foods.contains(newFood) && !miniFoods.contains(newFood) &&
                    (coin == null || !newFood.equals(coin)) && !stones.contains(newFood) &&
                    (healthItem == null || !newFood.equals(healthItem)) && (shieldItem == null || !newFood.equals(shieldItem)) &&
                    (invincibleItem == null || !newFood.equals(invincibleItem))) {
                foods.add(newFood);
                generated++;
            }
            attempts++;
        }
        if (carnivalTimer != null) carnivalTimer.stop();
        carnivalTimer = new Timer(CARNIVAL_DURATION, e -> endCarnival());
        carnivalTimer.setRepeats(false);
        carnivalTimer.start();
        repaint();
    }

    private void endCarnival() {
        isCarnival = false;
        if (!foods.isEmpty()) {
            Point oneFood = foods.getFirst();
            foods.clear();
            foods.add(oneFood);
        } else {
            generateFood();
        }
        while (foods.size() < NORMAL_FOOD_MAX && snake.size() < GAME_UNITS) generateFood();
        repaint();
    }

    private void checkCarnival() {
        if (!isCarnival && snake.size() >= 20) startCarnival();
    }

    public void resetGame() {
        initGame();
        startGame();
        repaint();
        requestFocusInWindow();
    }

    private void attemptRevive() {
        if (coins >= REVIVE_COST) {
            coins -= REVIVE_COST;
            int savedScore = score, savedCoins = coins;
            snake.clear();
            int startX = BOARD_WIDTH / 2 / UNIT_SIZE * UNIT_SIZE;
            int startY = BOARD_HEIGHT / 2 / UNIT_SIZE * UNIT_SIZE;
            for (int i = 0; i < deadLength; i++) snake.add(new Point(startX - i * UNIT_SIZE, startY));
            score = savedScore;
            coins = savedCoins;
            bodyLength = snake.size();
            direction = 'R';
            nextDirection = 'R';
            playerHealth = PLAYER_START_HEALTH;
            playerMaxHealth = PLAYER_START_HEALTH;
            playerShield = 0;
            miniFoodEatenCount = 0;
            updateInfoDisplay();
            generateFood();
            if (hasCoin && coin != null && snake.contains(coin)) {
                hasCoin = false;
                coin = null;
                generateCoin();
            }
            isDead = false;
            isRunning = true;
            startGame();
            repaint();
        } else {
            isRunning = false;
            isDead = false;
            RankingManager.getInstance().addScore(parentFrame.getPlayerName(), score);
            parentFrame.refreshRanking();
            if (timer != null) timer.stop();
            repaint();
        }
    }

    private void gameOver() {
        isRunning = false;
        isDead = false;
        RankingManager.getInstance().addScore(parentFrame.getPlayerName(), score);
        parentFrame.refreshRanking();
        if (timer != null) timer.stop();
        repaint();
    }

    private void updateInfoDisplay() {
        bodyLength = snake.size();
        parentFrame.updateGameInfo(score, bodyLength, coins);
        parentFrame.setTitle("贪吃蛇 - " + parentFrame.getPlayerName() +
                "  血量: " + playerHealth + "/" + playerMaxHealth +
                "  护盾: " + playerShield);
    }

    // 公共访问方法（供AISnake调用）
    public boolean isRunning() { return isRunning; }
    public boolean isDead() { return isDead; }

    // 穿墙移动
    private Point getWrappedPosition(Point head, int deltaX, int deltaY) {
        int newX = head.x + deltaX;
        int newY = head.y + deltaY;
        if (newX < 0) newX = BOARD_WIDTH - UNIT_SIZE;
        else if (newX >= BOARD_WIDTH) newX = 0;
        if (newY < 0) newY = BOARD_HEIGHT - UNIT_SIZE;
        else if (newY >= BOARD_HEIGHT) newY = 0;
        return new Point(newX, newY);
    }

    // ---------- 移动逻辑 ----------
    private void move() {
        // 玩家蛇移动（穿墙）
        direction = nextDirection;
        Point head = snake.getFirst();
        int deltaX = 0, deltaY = 0;
        switch (direction) {
            case 'U': deltaY = -UNIT_SIZE; break;
            case 'D': deltaY = UNIT_SIZE; break;
            case 'L': deltaX = -UNIT_SIZE; break;
            case 'R': deltaX = UNIT_SIZE; break;
        }
        Point newHead = getWrappedPosition(head, deltaX, deltaY);

        // 检查食物、金币、道具、迷你豆豆
        boolean ateFood = false;
        Point eatenFood = null;
        for (Point f : foods) {
            if (newHead.equals(f)) {
                ateFood = true;
                eatenFood = f;
                break;
            }
        }
        boolean ateMiniFood = false;
        Point eatenMini = null;
        for (Point mf : miniFoods) {
            if (newHead.equals(mf)) {
                ateMiniFood = true;
                eatenMini = mf;
                break;
            }
        }
        boolean ateCoin = hasCoin && newHead.equals(coin);
        boolean ateHealth = hasHealthItem && newHead.equals(healthItem);
        boolean ateShield = hasShieldItem && newHead.equals(shieldItem);
        boolean ateInvincible = hasInvincibleItem && newHead.equals(invincibleItem);
        boolean hitStone = stones.contains(newHead);

        // 移动
        snake.addFirst(newHead);
        if (!ateFood && !ateMiniFood) {
            snake.removeLast();
        } else {
            if (ateFood) {
                score++;
                bodyLength = snake.size();
                updateInfoDisplay();
                foods.remove(eatenFood);
                if (isCarnival) {
                    int target = (int)(GAME_UNITS * CARNIVAL_FOOD_RATIO);
                    if (foods.size() < target) generateFood(1);
                } else {
                    generateFood();
                }
            } else if (ateMiniFood) {
                score++;
                miniFoodEatenCount++;
                if (miniFoodEatenCount >= MINI_FOOD_COUNT_FOR_BONUS) {
                    bodyLength++;
                    miniFoodEatenCount = 0;
                } else {
                    bodyLength = snake.size();
                }
                updateInfoDisplay();
                miniFoods.remove(eatenMini);
            }
        }
        if (ateCoin) {
            coins++;
            hasCoin = false;
            coin = null;
            updateInfoDisplay();
        }
        if (ateHealth) {
            if (playerHealth < playerMaxHealth) {
                playerHealth++;
                if (playerHealth > playerMaxHealth) playerHealth = playerMaxHealth;
                updateInfoDisplay();
            }
            hasHealthItem = false;
            healthItem = null;
        }
        if (ateShield) {
            if (playerShield < MAX_SHIELD) {
                playerShield++;
                updateInfoDisplay();
            }
            hasShieldItem = false;
            shieldItem = null;
        }
        if (ateInvincible) {
            isInvincible = true;
            hasInvincibleItem = false;
            invincibleItem = null;
            if (invincibleTimer != null) invincibleTimer.stop();
            invincibleTimer = new Timer(INVINCIBLE_DURATION, e -> {
                isInvincible = false;
                invincibleTimer = null;
            });
            invincibleTimer.setRepeats(false);
            invincibleTimer.start();
            repaint();
        }

        // 玩家碰撞检测（自身、石头、AI蛇）
        boolean playerHit = false;
        for (int i = 1; i < snake.size(); i++) {
            if (snake.get(i).equals(newHead)) {
                playerHit = true;
                break;
            }
        }
        if (hitStone && !isInvincible) playerHit = true;
        if (!isInvincible) {
            for (AISnake ai : aiSnakes) {
                if (ai.getBody().contains(newHead)) {
                    playerHit = true;
                    break;
                }
            }
        }

        if (playerHit) {
            damagePlayer(1);
        }

        if (hitStone && isInvincible) {
            stones.removeIf(s -> s.equals(newHead));
            stoneBirthTime.remove(newHead);
        }

        // 移动AI蛇
        LinkedList<AISnake> deadAI = new LinkedList<>();
        for (AISnake ai : aiSnakes) {
            ai.decideDirection();
            ai.move();
            Point aiHead = ai.getHead();
            boolean aiDead = false;
            // 边界死亡（不穿墙）
            if (aiHead.x < 0 || aiHead.x >= BOARD_WIDTH || aiHead.y < 0 || aiHead.y >= BOARD_HEIGHT) aiDead = true;
            if (stones.contains(aiHead)) aiDead = true;
            // AI蛇碰到玩家身体（无论玩家是否无敌，AI都死亡）
            if (snake.contains(aiHead)) aiDead = true;
            for (AISnake other : aiSnakes) {
                if (other != ai && other.getBody().contains(aiHead)) { aiDead = true; break; }
            }
            for (int i = 1; i < ai.getBody().size(); i++) {
                if (ai.getBody().get(i).equals(aiHead)) { aiDead = true; break; }
            }
            if (aiDead || ai.getHp() <= 0) {
                generateFoodAt(aiHead);
                deadAI.add(ai);
                score += 10;
                updateInfoDisplay();
            }
        }
        aiSnakes.removeAll(deadAI);

        // 补充死亡的AI蛇，保持总数不变
        for (int i = 0; i < deadAI.size(); i++) {
            spawnNewAISnake();
        }

        checkCarnival();

        if (snake.size() == GAME_UNITS) gameOver();
    }

    // ---------- 绘制 ----------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage bg = assets.getBackgroundImage();
        if (bg != null) g.drawImage(bg, 0, 0, BOARD_WIDTH, BOARD_HEIGHT, this);
        else {
            g.setColor(Color.black);
            g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        }
        drawFoods(g);
        drawMiniFoods(g);
        drawCoin(g);
        drawStones(g);
        drawHealthItem(g);
        drawShieldItem(g);
        drawInvincibleItem(g);
        drawSnake(g);
        drawAISnakes(g);
        drawGameStatus(g);
        drawUI(g);
    }

    private void drawFoods(Graphics g) {
        BufferedImage foodImg = assets.getFoodImage();
        for (Point f : foods) if (foodImg != null) g.drawImage(foodImg, f.x, f.y, UNIT_SIZE, UNIT_SIZE, this);
    }

    private void drawMiniFoods(Graphics g) {
        g.setColor(Color.GREEN);
        for (Point mf : miniFoods) {
            g.fillOval(mf.x + UNIT_SIZE/3, mf.y + UNIT_SIZE/3, UNIT_SIZE/3, UNIT_SIZE/3);
        }
    }

    private void drawCoin(Graphics g) {
        if (hasCoin && coin != null) {
            BufferedImage coinImg = assets.getCoinImage();
            if (coinImg != null) g.drawImage(coinImg, coin.x, coin.y, UNIT_SIZE, UNIT_SIZE, this);
        }
    }

    private void drawStones(Graphics g) {
        BufferedImage stoneImg = assets.getStoneImage();
        for (Point s : stones) if (stoneImg != null) g.drawImage(stoneImg, s.x, s.y, UNIT_SIZE, UNIT_SIZE, this);
    }

    private void drawHealthItem(Graphics g) {
        if (hasHealthItem && healthItem != null) {
            g.setColor(Color.PINK);
            g.fillRect(healthItem.x, healthItem.y, UNIT_SIZE, UNIT_SIZE);
            g.setColor(Color.RED);
            g.drawString("❤", healthItem.x + UNIT_SIZE/3, healthItem.y + UNIT_SIZE*2/3);
        }
    }

    private void drawShieldItem(Graphics g) {
        if (hasShieldItem && shieldItem != null) {
            g.setColor(Color.CYAN);
            g.fillRect(shieldItem.x, shieldItem.y, UNIT_SIZE, UNIT_SIZE);
            g.setColor(Color.BLUE);
            g.drawString("🛡", shieldItem.x + UNIT_SIZE/3, shieldItem.y + UNIT_SIZE*2/3);
        }
    }

    private void drawInvincibleItem(Graphics g) {
        if (hasInvincibleItem && invincibleItem != null) {
            BufferedImage invImg = assets.getInvincibleItemImage();
            if (invImg != null) g.drawImage(invImg, invincibleItem.x, invincibleItem.y, UNIT_SIZE, UNIT_SIZE, this);
        }
    }

    private void drawSnake(Graphics g) {
        BufferedImage headImg = assets.getSnakeHeadImage(), bodyImg = assets.getSnakeBodyImage();
        for (int i = 0; i < snake.size(); i++) {
            Point p = snake.get(i);
            if (i == 0) { if (headImg != null) g.drawImage(headImg, p.x, p.y, UNIT_SIZE, UNIT_SIZE, this); }
            else { if (bodyImg != null) g.drawImage(bodyImg, p.x, p.y, UNIT_SIZE, UNIT_SIZE, this); }
            g.setColor(Color.BLACK);
            g.drawRect(p.x, p.y, UNIT_SIZE, UNIT_SIZE);
        }
    }

    private void drawAISnakes(Graphics g) {
        for (AISnake ai : aiSnakes) {
            LinkedList<Point> body = ai.getBody();
            for (int i = 0; i < body.size(); i++) {
                Point p = body.get(i);
                // AI蛇颜色：头部橙色，身体深橙色
                Color color = (i == 0) ? Color.ORANGE : new Color(200, 100, 0);
                g.setColor(color);
                g.fillRect(p.x, p.y, UNIT_SIZE, UNIT_SIZE);
                g.setColor(Color.BLACK);
                g.drawRect(p.x, p.y, UNIT_SIZE, UNIT_SIZE);
            }
            // 血条
            int hp = ai.getHp();
            int maxHp = ai.getMaxHp();
            if (maxHp > 0) {
                Point head = ai.getHead();
                int barWidth = UNIT_SIZE;
                int barHeight = 4;
                g.setColor(HEALTH_BAR_BG);
                g.fillRect(head.x, head.y - barHeight, barWidth, barHeight);
                g.setColor(HEALTH_BAR_FG);
                g.fillRect(head.x, head.y - barHeight, barWidth * hp / maxHp, barHeight);
            }
        }
    }

    private void drawGameStatus(Graphics g) {
        g.setFont(new Font("微软雅黑", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        if (!isRunning && !isDead) {
            g.drawString("游戏结束! 得分: " + score, BOARD_WIDTH/2 - 80, BOARD_HEIGHT/2);
        } else if (isDead) {
            g.drawString("等待复活...", BOARD_WIDTH/2 - 60, BOARD_HEIGHT/2);
        }
    }

    private void drawUI(Graphics g) {
        g.setFont(new Font("微软雅黑", Font.BOLD, 14));
        g.setColor(UI_TEXT);
        g.drawString("血量: " + playerHealth + "/" + playerMaxHealth, 10, 20);
        g.drawString("护盾: " + playerShield, 10, 40);
        g.drawString("迷你豆豆计数: " + miniFoodEatenCount + "/" + MINI_FOOD_COUNT_FOR_BONUS, 10, 60);
        if (isInvincible) {
            g.setColor(Color.MAGENTA);
            g.drawString("无敌状态!", 10, 80);
        }
        if (isCarnival) {
            g.setColor(Color.ORANGE);
            g.drawString("狂欢时刻!", 10, 100);
        }
    }

    // ---------- 事件处理 ----------
    @Override
    public void actionPerformed(ActionEvent e) {
        if (isRunning) move();
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP: if (direction != 'D') nextDirection = 'U'; break;
            case KeyEvent.VK_DOWN: if (direction != 'U') nextDirection = 'D'; break;
            case KeyEvent.VK_LEFT: if (direction != 'R') nextDirection = 'L'; break;
            case KeyEvent.VK_RIGHT: if (direction != 'L') nextDirection = 'R'; break;
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}