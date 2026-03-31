import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    // 常量
    private static final int BOARD_WIDTH = 600;
    private static final int BOARD_HEIGHT = 600;
    private static final int UNIT_SIZE = 25;
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

    // 游戏状态
    private boolean isRunning = true;
    private boolean isDead = false;
    private Timer timer;
    private Timer coinTimer;
    private Timer invincibleTimer;
    private Timer carnivalTimer;
    private int score = 0;
    private int coins = 0;
    private int bodyLength = 3;
    private int deadLength = 0;
    private int currentDelay = SPEED_NORMAL;
    private boolean isInvincible = false;
    private boolean isCarnival = false;

    // 蛇
    private final LinkedList<Point> snake = new LinkedList<>();
    private char direction = 'R';
    private char nextDirection = 'R';

    // 食物列表
    private final LinkedList<Point> foods = new LinkedList<>();
    private Point coin;
    private boolean hasCoin = false;

    // 石头
    private final LinkedList<Point> stones = new LinkedList<>();
    private final Map<Point, Long> stoneBirthTime = new HashMap<>();

    // 无敌道具
    private Point invincibleItem;
    private boolean hasInvincibleItem = false;

    private final Random random = new Random();
    private final GameMainFrame parentFrame;
    private final GameAssets assets;   // 图片资源

    // 构造函数
    public GamePanel(GameMainFrame parent, String playerName, int speedLevel) {
        this.parentFrame = parent;
        this.assets = new GameAssets(); // 生成图片资源
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
    }

    // ---------- 游戏初始化 ----------
    public void initGame() {
        snake.clear();
        foods.clear();
        stones.clear();
        stoneBirthTime.clear();
        hasCoin = false;
        coin = null;
        hasInvincibleItem = false;
        invincibleItem = null;
        isInvincible = false;
        isCarnival = false;
        if (invincibleTimer != null) invincibleTimer.stop();
        if (carnivalTimer != null) carnivalTimer.stop();

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
        updateInfoDisplay();
        generateFood();
        isRunning = true;
        isDead = false;
        if (timer != null && timer.isRunning()) timer.stop();
    }

    private void startGame() {
        if (timer != null) timer.stop();
        timer = new Timer(currentDelay, this);
        timer.start();
    }

    public void setSpeed(int speedLevel) {
        switch (speedLevel) {
            case 0: currentDelay = SPEED_SLOW; break;
            case 1: currentDelay = SPEED_NORMAL; break;
            case 2: currentDelay = SPEED_FAST; break;
            default: currentDelay = SPEED_NORMAL;
        }
        if (timer != null && isRunning && !isDead) {
            timer.setDelay(currentDelay);
        }
    }

    // ---------- 金币生成 ----------
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
            if (!snake.contains(newCoin) && !foods.contains(newCoin) && !stones.contains(newCoin) &&
                    (invincibleItem == null || !newCoin.equals(invincibleItem))) {
                coin = newCoin;
                hasCoin = true;
                repaint();
                return;
            }
        }
    }

    // ---------- 石头生成（批量，每个存活10秒）----------
    private void startStoneGenerator() {
        Timer stoneGenTimer = new Timer(STONE_GEN_INTERVAL, e -> {
            if (isRunning && !isDead) {
                int numToGenerate = MIN_STONES_PER_BATCH + random.nextInt(MAX_STONES_PER_BATCH - MIN_STONES_PER_BATCH + 1);
                if (stones.size() >= MAX_STONES_TOTAL) return;
                int generated = 0;
                int attempts = 0;
                while (generated < numToGenerate && attempts < 1000 && stones.size() < MAX_STONES_TOTAL) {
                    int x = random.nextInt(BOARD_WIDTH / UNIT_SIZE) * UNIT_SIZE;
                    int y = random.nextInt(BOARD_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
                    Point newStone = new Point(x, y);
                    if (!snake.contains(newStone) && !foods.contains(newStone) &&
                            (coin == null || !newStone.equals(coin)) && !stones.contains(newStone) &&
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

        // 石头消失检查
        Timer stoneLifetimeChecker = new Timer(500, e -> {
            long now = System.currentTimeMillis();
            boolean removed = false;
            LinkedList<Point> toRemove = new LinkedList<>();
            for (Point s : stones) {
                Long birth = stoneBirthTime.get(s);
                if (birth != null && (now - birth) >= STONE_LIFE) {
                    toRemove.add(s);
                    removed = true;
                }
            }
            stones.removeAll(toRemove);
            for (Point s : toRemove) stoneBirthTime.remove(s);
            if (removed) repaint();
        });
        stoneLifetimeChecker.start();
    }

    // ---------- 无敌道具生成 ----------
    private void startInvincibleItemGenerator() {
        Timer invincibleGenTimer = new Timer(15000, e -> {
            if (isRunning && !isDead && !hasInvincibleItem) {
                generateInvincibleItem();
            }
        });
        invincibleGenTimer.start();
    }

    private void generateInvincibleItem() {
        int maxAttempts = 1000;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = random.nextInt(BOARD_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            int y = random.nextInt(BOARD_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
            Point newItem = new Point(x, y);
            if (!snake.contains(newItem) && !foods.contains(newItem) &&
                    (coin == null || !newItem.equals(coin)) && !stones.contains(newItem)) {
                invincibleItem = newItem;
                hasInvincibleItem = true;
                repaint();
                return;
            }
        }
    }

    // ---------- 食物生成（多食物支持）----------
    private void generateFood() {
        generateFood(1);
    }

    private void generateFood(int count) {
        int totalCells = GAME_UNITS;
        if (snake.size() >= totalCells) {
            gameOver();
            return;
        }
        int generated = 0;
        int attempts = 0;
        while (generated < count && attempts < 2000) {
            int x = random.nextInt(BOARD_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            int y = random.nextInt(BOARD_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
            Point newFood = new Point(x, y);
            if (!snake.contains(newFood) && !foods.contains(newFood) &&
                    (coin == null || !newFood.equals(coin)) && !stones.contains(newFood) &&
                    (invincibleItem == null || !newFood.equals(invincibleItem))) {
                foods.add(newFood);
                generated++;
            }
            attempts++;
        }
        repaint();
    }

    // ---------- 狂欢时刻 ----------
    private void startCarnival() {
        if (isCarnival) return;
        isCarnival = true;
        foods.clear();
        int totalCells = GAME_UNITS;
        int targetFoodCount = (int) (totalCells * CARNIVAL_FOOD_RATIO);
        int generated = 0;
        int attempts = 0;
        while (generated < targetFoodCount && attempts < 10000) {
            int x = random.nextInt(BOARD_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            int y = random.nextInt(BOARD_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
            Point newFood = new Point(x, y);
            if (!snake.contains(newFood) && !foods.contains(newFood) &&
                    (coin == null || !newFood.equals(coin)) && !stones.contains(newFood) &&
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
        repaint();
    }

    private void checkCarnival() {
        if (!isCarnival && snake.size() >= 20) {
            startCarnival();
        }
    }

    // ---------- 游戏流程控制 ----------
    public void resetGame() {
        initGame();
        startGame();
        repaint();
        requestFocusInWindow();
    }

    private void attemptRevive() {
        if (coins >= REVIVE_COST) {
            coins -= REVIVE_COST;
            int savedScore = score;
            int savedCoins = coins;
            snake.clear();
            int startX = BOARD_WIDTH / 2 / UNIT_SIZE * UNIT_SIZE;
            int startY = BOARD_HEIGHT / 2 / UNIT_SIZE * UNIT_SIZE;
            for (int i = 0; i < deadLength; i++) {
                snake.add(new Point(startX - i * UNIT_SIZE, startY));
            }
            score = savedScore;
            coins = savedCoins;
            bodyLength = snake.size();
            direction = 'R';
            nextDirection = 'R';
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

    // ---------- 移动逻辑 ----------
    private void move() {
        direction = nextDirection;
        Point head = snake.getFirst();
        int newX = head.x;
        int newY = head.y;
        switch (direction) {
            case 'U': newY -= UNIT_SIZE; break;
            case 'D': newY += UNIT_SIZE; break;
            case 'L': newX -= UNIT_SIZE; break;
            case 'R': newX += UNIT_SIZE; break;
        }
        Point newHead = new Point(newX, newY);

        // 检查食物
        boolean ateFood = false;
        Point eatenFood = null;
        for (Point f : foods) {
            if (newHead.equals(f)) {
                ateFood = true;
                eatenFood = f;
                break;
            }
        }
        boolean ateCoin = hasCoin && newHead.equals(coin);
        boolean ateInvincible = hasInvincibleItem && newHead.equals(invincibleItem);
        boolean hitStone = false;
        for (Point s : stones) {
            if (newHead.equals(s)) {
                hitStone = true;
                break;
            }
        }

        // 移动蛇身
        snake.addFirst(newHead);
        if (!ateFood) {
            snake.removeLast();
        } else {
            score++;
            bodyLength = snake.size();
            updateInfoDisplay();
            foods.remove(eatenFood);
            if (isCarnival) {
                int target = (int) (GAME_UNITS * CARNIVAL_FOOD_RATIO);
                if (foods.size() < target) {
                    generateFood(1);
                }
            } else {
                generateFood();
            }
        }

        if (ateCoin) {
            coins++;
            hasCoin = false;
            coin = null;
            updateInfoDisplay();
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

        // 碰撞检测
        boolean hit = false;
        if (newX < 0 || newX >= BOARD_WIDTH || newY < 0 || newY >= BOARD_HEIGHT) {
            hit = true;
        }
        for (int i = 1; i < snake.size(); i++) {
            if (snake.get(i).equals(newHead)) {
                hit = true;
                break;
            }
        }
        if (hitStone && !isInvincible) {
            hit = true;
        }

        if (hit) {
            deadLength = snake.size() - 1;
            isRunning = false;
            isDead = true;
            if (timer != null) timer.stop();
            int option = JOptionPane.showConfirmDialog(this,
                    "你死了！消耗 " + REVIVE_COST + " 金币复活吗？\n当前金币: " + coins,
                    "复活确认",
                    JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                attemptRevive();
            } else {
                gameOver();
            }
            repaint();
            return;
        }

        // 如果无敌且撞到石头，移除该石头
        if (hitStone && isInvincible) {
            stones.removeIf(s -> s.equals(newHead));
            stoneBirthTime.remove(newHead);
        }

        checkCarnival();

        if (snake.size() == GAME_UNITS) {
            gameOver();
        }
    }

    private void updateInfoDisplay() {
        bodyLength = snake.size();
        parentFrame.updateGameInfo(score, bodyLength, coins);
    }

    // ---------- 绘制 ----------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage bg = assets.getBackgroundImage();
        if (bg != null) {
            g.drawImage(bg, 0, 0, BOARD_WIDTH, BOARD_HEIGHT, this);
        } else {
            g.setColor(Color.black);
            g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        }
        drawFoods(g);
        drawCoin(g);
        drawStones(g);
        drawInvincibleItem(g);
        drawSnake(g);
        drawGameStatus(g);
        drawEffects(g);
    }

    private void drawFoods(Graphics g) {
        BufferedImage foodImg = assets.getFoodImage();
        for (Point f : foods) {
            if (foodImg != null) {
                g.drawImage(foodImg, f.x, f.y, UNIT_SIZE, UNIT_SIZE, this);
            }
        }
    }

    private void drawCoin(Graphics g) {
        if (hasCoin && coin != null) {
            BufferedImage coinImg = assets.getCoinImage();
            if (coinImg != null) {
                g.drawImage(coinImg, coin.x, coin.y, UNIT_SIZE, UNIT_SIZE, this);
            }
        }
    }

    private void drawStones(Graphics g) {
        BufferedImage stoneImg = assets.getStoneImage();
        for (Point s : stones) {
            if (stoneImg != null) {
                g.drawImage(stoneImg, s.x, s.y, UNIT_SIZE, UNIT_SIZE, this);
            }
        }
    }

    private void drawInvincibleItem(Graphics g) {
        if (hasInvincibleItem && invincibleItem != null) {
            BufferedImage invImg = assets.getInvincibleItemImage();
            if (invImg != null) {
                g.drawImage(invImg, invincibleItem.x, invincibleItem.y, UNIT_SIZE, UNIT_SIZE, this);
            }
        }
    }

    private void drawSnake(Graphics g) {
        BufferedImage headImg = assets.getSnakeHeadImage();
        BufferedImage bodyImg = assets.getSnakeBodyImage();
        for (int i = 0; i < snake.size(); i++) {
            Point p = snake.get(i);
            if (i == 0) {
                if (headImg != null) {
                    g.drawImage(headImg, p.x, p.y, UNIT_SIZE, UNIT_SIZE, this);
                }
            } else {
                if (bodyImg != null) {
                    g.drawImage(bodyImg, p.x, p.y, UNIT_SIZE, UNIT_SIZE, this);
                }
            }
            g.setColor(Color.BLACK);
            g.drawRect(p.x, p.y, UNIT_SIZE, UNIT_SIZE);
        }
    }

    private void drawGameStatus(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        if (!isRunning && !isDead) {
            g.drawString("游戏结束! 得分: " + score, BOARD_WIDTH / 2 - 80, BOARD_HEIGHT / 2);
        } else if (isDead) {
            g.drawString("等待复活...", BOARD_WIDTH / 2 - 60, BOARD_HEIGHT / 2);
        }
    }

    private void drawEffects(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, 14));
        if (isInvincible) {
            g.setColor(Color.MAGENTA);
            g.drawString("无敌状态!", 10, 30);
        }
        if (isCarnival) {
            g.setColor(Color.ORANGE);
            g.drawString("狂欢时刻!", 10, 55);
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
            case KeyEvent.VK_UP:
                if (direction != 'D') nextDirection = 'U';
                break;
            case KeyEvent.VK_DOWN:
                if (direction != 'U') nextDirection = 'D';
                break;
            case KeyEvent.VK_LEFT:
                if (direction != 'R') nextDirection = 'L';
                break;
            case KeyEvent.VK_RIGHT:
                if (direction != 'L') nextDirection = 'R';
                break;
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}