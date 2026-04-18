package snake.game;

import snake.main.GameMainFrame;
import snake.ranking.RankingManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private final GameMainFrame parentFrame;
    private final GameState state;
    private final ItemGenerator itemGenerator;
    private final AIManager aiManager;
    private final GameLogic logic;
    private final GameRenderer renderer;
    private Timer gameTimer;
    private Timer invincibleTimer, carnivalTimer;

    public GamePanel(GameMainFrame parent, String playerName, int speedLevel) {
        this.parentFrame = parent;
        this.state = new GameState(speedLevel);
        this.itemGenerator = new ItemGenerator(state);
        this.aiManager = new AIManager(state, itemGenerator);
        this.logic = new GameLogic(state);
        this.renderer = new GameRenderer(state);

        setPreferredSize(new Dimension(GameState.BOARD_WIDTH, GameState.BOARD_HEIGHT));
        setBackground(Color.black);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        addKeyListener(this);
        initGame();
        startGame();
        startAllGenerators();
    }

    private void initGame() {
        state.initGame();
        aiManager.initAISnakes();
        for (int i = 0; i < GameState.NORMAL_FOOD_MAX; i++) itemGenerator.generateOneFood();
        updateInfoDisplay();
        // 使用正确的相对路径（项目根目录下的 res 文件夹）
        SoundManager.getInstance().playBackgroundMusic("res/bgm.wav");
    }

    private void startGame() {
        if (gameTimer != null) gameTimer.stop();
        gameTimer = new Timer(state.getCurrentDelay(), this);
        gameTimer.start();
    }

    private void startAllGenerators() {
        itemGenerator.startCoinGenerator(() -> repaint());
        itemGenerator.startStoneGenerator(() -> repaint());
        itemGenerator.startInvincibleItemGenerator(() -> repaint());
        itemGenerator.startNormalFoodGenerator(() -> repaint(), () -> state.isRunning() && !state.isDead() && !state.isCarnival());
        itemGenerator.startHealthItemGenerator(() -> repaint(), () -> state.getPlayerHealth() < state.getPlayerMaxHealth());
        itemGenerator.startShieldItemGenerator(() -> repaint(), () -> state.getPlayerShield() < GameState.MAX_SHIELD);
        itemGenerator.startMiniFoodGenerator(() -> repaint());
    }

    private void damagePlayer(int amount) {
        if (state.isInvincible()) return;
        int shield = state.getPlayerShield();
        if (shield > 0) {
            state.setPlayerShield(shield - 1);
        } else {
            int health = state.getPlayerHealth() - amount;
            state.setPlayerHealth(health);
            if (health <= 0) {
                state.setPlayerHealth(0);
                state.setDeadLength(state.getSnake().size() - 1);
                state.setRunning(false);
                state.setDead(true);
                if (gameTimer != null) gameTimer.stop();
                int option = JOptionPane.showConfirmDialog(this,
                        "你死了！消耗 " + GameState.REVIVE_COST + " 金币复活吗？\n当前金币: " + state.getCoins(),
                        "复活确认", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) attemptRevive();
                else gameOver();
            }
        }
        updateInfoDisplay();
        repaint();
    }

    private void attemptRevive() {
        if (state.getCoins() >= GameState.REVIVE_COST) {
            state.setCoins(state.getCoins() - GameState.REVIVE_COST);
            int savedScore = state.getScore();
            int savedCoins = state.getCoins();
            state.getSnake().clear();
            int startX = GameState.BOARD_WIDTH / 2 / GameState.UNIT_SIZE * GameState.UNIT_SIZE;
            int startY = GameState.BOARD_HEIGHT / 2 / GameState.UNIT_SIZE * GameState.UNIT_SIZE;
            for (int i = 0; i < state.getDeadLength(); i++) {
                state.getSnake().add(new Point(startX - i * GameState.UNIT_SIZE, startY));
            }
            state.setScore(savedScore);
            state.setCoins(savedCoins);
            state.setBodyLength(state.getSnake().size());
            state.setDirection('R');
            state.setNextDirection('R');
            state.setPlayerHealth(GameState.PLAYER_START_HEALTH);
            state.setPlayerMaxHealth(GameState.PLAYER_START_HEALTH);
            state.setPlayerShield(0);
            state.setMiniFoodEatenCount(0);
            updateInfoDisplay();
            itemGenerator.generateOneFood();
            if (state.hasCoin() && state.getCoin() != null && state.getSnake().contains(state.getCoin())) {
                state.setHasCoin(false);
                state.setCoin(null);
                itemGenerator.generateCoin();
            }
            state.setDead(false);
            state.setRunning(true);
            startGame();
            repaint();
        } else {
            gameOver();
        }
    }

    private void gameOver() {
        state.setRunning(false);
        state.setDead(false);
        RankingManager.getInstance().addScore(parentFrame.getPlayerName(), state.getScore());
        parentFrame.refreshRanking();
        if (gameTimer != null) gameTimer.stop();
        repaint();
    }

    private void updateInfoDisplay() {
        parentFrame.updateGameInfo(state.getScore(), state.getSnake().size(), state.getCoins());
        parentFrame.setTitle("贪吃蛇 - " + parentFrame.getPlayerName() +
                "  血量: " + state.getPlayerHealth() + "/" + state.getPlayerMaxHealth() +
                "  护盾: " + state.getPlayerShield());
    }

    public void setSpeed(int speedLevel) {
        state.setSpeedLevel(speedLevel);
        if (gameTimer != null && state.isRunning() && !state.isDead()) {
            gameTimer.setDelay(state.getCurrentDelay());
        }
        aiManager.setAllAggression(speedLevel);
    }

    public void resetGame() {
        initGame();
        startGame();
        repaint();
        requestFocusInWindow();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (state.isRunning()) {
            logic.movePlayer();
            if (logic.checkPlayerCollision()) damagePlayer(1);
            aiManager.updateAndMove();
            // 注意：这里不要调用音效，音效已在 GameLogic 中的 eatCoin 处调用
            // 狂欢时刻动态维持食物数量（每帧检查，若少于目标则补充）
            if (state.isCarnival()) {
                int target = (int)(GameState.GAME_UNITS * GameState.CARNIVAL_FOOD_RATIO);
                while (state.getFoods().size() < target) {
                    itemGenerator.generateOneFood();
                }
            }

            // 触发狂欢时刻（蛇长达到20时）
            if (!state.isCarnival() && state.getSnake().size() >= 20) {
                state.setCarnival(true);
                state.getFoods().clear();
                int target = (int)(GameState.GAME_UNITS * GameState.CARNIVAL_FOOD_RATIO);
                for (int i = 0; i < target; i++) itemGenerator.generateOneFood();
                if (carnivalTimer != null) carnivalTimer.stop();
                carnivalTimer = new Timer(GameState.CARNIVAL_DURATION, ev -> endCarnival());
                carnivalTimer.setRepeats(false);
                carnivalTimer.start();
            }

            if (state.getSnake().size() == GameState.GAME_UNITS) gameOver();
        }
        repaint();
    }

    private void endCarnival() {
        state.setCarnival(false);
        state.getFoods().clear();
        itemGenerator.generateOneFood();
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderer.draw(g);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        char dir = state.getDirection();
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP: if (dir != 'D') state.setNextDirection('U'); break;
            case KeyEvent.VK_DOWN: if (dir != 'U') state.setNextDirection('D'); break;
            case KeyEvent.VK_LEFT: if (dir != 'R') state.setNextDirection('L'); break;
            case KeyEvent.VK_RIGHT: if (dir != 'L') state.setNextDirection('R'); break;
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}