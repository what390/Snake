import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private static final int BOARD_WIDTH = 600;
    private static final int BOARD_HEIGHT = 600;
    private static final int UNIT_SIZE = 25;
    private static final int GAME_UNITS = (BOARD_WIDTH * BOARD_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    private static final int DELAY = 100;
    private static final int REVIVE_COST = 5;
    private static final int COIN_GEN_DELAY = 5000; // 金币生成间隔（毫秒）

    private boolean isRunning = true;
    private boolean isDead = false;
    private Timer timer;
    private Timer coinTimer;
    private int score = 0;
    private int coins = 0;
    private int bodyLength = 3;
    private int deadLength = 0; // 死亡时的长度，用于复活时恢复长度

    private final LinkedList<Point> snake = new LinkedList<>();
    private char direction = 'R';
    private char nextDirection = 'R';
    private Point food;
    private Point coin;
    private boolean hasCoin = false;
    private final Random random = new Random();
    private final GameMainFrame parentFrame;

    public GamePanel(GameMainFrame parent, String playerName) {
        this.parentFrame = parent;
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setBackground(Color.black);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        addKeyListener(this);
        initGame();
        startGame();
        startCoinGenerator();
    }

    // 初始化游戏数据（全新开始）
    public void initGame() {
        snake.clear();
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
        // 初始不生成金币，等定时器生成
        hasCoin = false;
        coin = null;
        isRunning = true;
        isDead = false;
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
    }

    private void startGame() {
        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(DELAY, this);
        timer.start();
    }

    // 金币生成器（独立定时器）
    private void startCoinGenerator() {
        if (coinTimer != null && coinTimer.isRunning()) {
            coinTimer.stop();
        }
        coinTimer = new Timer(COIN_GEN_DELAY, e -> {
            if (isRunning && !hasCoin && !isDead) {
                generateCoin();
            }
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
            if (!snake.contains(newCoin) && (food == null || !newCoin.equals(food))) {
                coin = newCoin;
                hasCoin = true;
                repaint();
                return;
            }
        }
    }

    public void resetGame() {
        initGame();
        startGame();
        repaint();
        requestFocusInWindow();
    }

    // 复活：消耗金币，保留长度、分数、金币，重置蛇的位置到中央（长度不变）
    private void attemptRevive() {
        if (coins >= REVIVE_COST) {
            coins -= REVIVE_COST;           // 消耗金币
            // 保存当前得分和长度（死亡时已保存长度 deadLength）
            int savedScore = score;
            int savedCoins = coins;
            // 根据死亡时的长度重新生成蛇（位置中央，长度不变）
            snake.clear();
            int startX = BOARD_WIDTH / 2 / UNIT_SIZE * UNIT_SIZE;
            int startY = BOARD_HEIGHT / 2 / UNIT_SIZE * UNIT_SIZE;
            for (int i = 0; i < deadLength; i++) {
                snake.add(new Point(startX - i * UNIT_SIZE, startY));
            }
            // 恢复分数、金币、长度显示
            score = savedScore;
            coins = savedCoins;
            bodyLength = snake.size();
            direction = 'R';
            nextDirection = 'R';
            updateInfoDisplay();

            // 重新生成食物（确保不与蛇重叠）
            generateFood();
            // 如果有金币存在则保留，否则重新生成
            if (hasCoin && coin != null && snake.contains(coin)) {
                hasCoin = false;
                coin = null;
                generateCoin(); // 立即重新生成一个
            }

            isDead = false;
            isRunning = true;
            startGame();
            repaint();
        } else {
            // 金币不足，游戏结束
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

    private void generateFood() {
        int totalCells = (BOARD_WIDTH / UNIT_SIZE) * (BOARD_HEIGHT / UNIT_SIZE);
        if (snake.size() >= totalCells) {
            gameOver();
            return;
        }
        while (true) {
            int x = random.nextInt(BOARD_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            int y = random.nextInt(BOARD_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
            Point newFood = new Point(x, y);
            if (!snake.contains(newFood) && (!hasCoin || !newFood.equals(coin))) {
                food = newFood;
                break;
            }
        }
    }

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

        boolean ateFood = newHead.equals(food);
        boolean ateCoin = hasCoin && newHead.equals(coin);

        // 移动蛇身
        snake.addFirst(newHead);
        if (!ateFood) {
            snake.removeLast();
        } else {
            // 吃到食物：增加得分，长度增加
            score++;
            bodyLength = snake.size();
            updateInfoDisplay();
            generateFood();
        }

        if (ateCoin) {
            // 吃到金币：增加金币数量，移除金币
            coins++;
            hasCoin = false;
            coin = null;
            updateInfoDisplay();
            // 不用重新生成，等待下次定时器生成
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

        if (hit) {
            // 死亡：记录当前长度
            deadLength = snake.size();
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

        // 胜利条件
        if (snake.size() == GAME_UNITS) {
            gameOver();
        }
    }

    private void updateInfoDisplay() {
        bodyLength = snake.size();
        parentFrame.updateGameInfo(score, bodyLength, coins);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGrid(g);
        drawFood(g);
        drawCoin(g);
        drawSnake(g);
        drawGameStatus(g);
    }

    private void drawGrid(Graphics g) {
        g.setColor(Color.DARK_GRAY);
        for (int i = 0; i <= BOARD_WIDTH / UNIT_SIZE; i++) {
            g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, BOARD_HEIGHT);
            g.drawLine(0, i * UNIT_SIZE, BOARD_WIDTH, i * UNIT_SIZE);
        }
    }

    private void drawFood(Graphics g) {
        if (food != null) {
            g.setColor(Color.RED);
            g.fillOval(food.x, food.y, UNIT_SIZE, UNIT_SIZE);
        }
    }

    private void drawCoin(Graphics g) {
        if (hasCoin && coin != null) {
            g.setColor(Color.YELLOW);
            g.fillOval(coin.x, coin.y, UNIT_SIZE, UNIT_SIZE);
            g.setColor(Color.ORANGE);
            g.drawOval(coin.x, coin.y, UNIT_SIZE, UNIT_SIZE);
        }
    }

    private void drawSnake(Graphics g) {
        for (int i = 0; i < snake.size(); i++) {
            Point p = snake.get(i);
            if (i == 0) {
                g.setColor(Color.GREEN);
            } else {
                g.setColor(new Color(45, 180, 0));
            }
            g.fillRect(p.x, p.y, UNIT_SIZE, UNIT_SIZE);
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

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isRunning) {
            move();
        }
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