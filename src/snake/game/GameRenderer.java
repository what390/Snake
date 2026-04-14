package snake.game;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

public class GameRenderer {
    private final GameState state;
    private final GameAssets assets;
    private static final Color HEALTH_BAR_BG = Color.RED;
    private static final Color HEALTH_BAR_FG = Color.GREEN;
    private static final Color UI_TEXT = Color.WHITE;

    public GameRenderer(GameState state) {
        this.state = state;
        this.assets = new GameAssets();
    }

    public void draw(Graphics g) {
        BufferedImage bg = assets.getBackgroundImage();
        if (bg != null) {
            g.drawImage(bg, 0, 0, GameState.BOARD_WIDTH, GameState.BOARD_HEIGHT, null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, GameState.BOARD_WIDTH, GameState.BOARD_HEIGHT);
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
        for (Point f : state.getFoods()) {
            if (foodImg != null) g.drawImage(foodImg, f.x, f.y, GameState.UNIT_SIZE, GameState.UNIT_SIZE, null);
        }
    }

    private void drawMiniFoods(Graphics g) {
        g.setColor(Color.GREEN);
        for (Point mf : state.getMiniFoods()) {
            g.fillOval(mf.x + GameState.UNIT_SIZE/3, mf.y + GameState.UNIT_SIZE/3,
                    GameState.UNIT_SIZE/3, GameState.UNIT_SIZE/3);
        }
    }

    private void drawCoin(Graphics g) {
        if (state.hasCoin() && state.getCoin() != null) {
            BufferedImage coinImg = assets.getCoinImage();
            if (coinImg != null) g.drawImage(coinImg, state.getCoin().x, state.getCoin().y,
                    GameState.UNIT_SIZE, GameState.UNIT_SIZE, null);
        }
    }

    private void drawStones(Graphics g) {
        BufferedImage stoneImg = assets.getStoneImage();
        for (Point s : state.getStones()) {
            if (stoneImg != null) g.drawImage(stoneImg, s.x, s.y, GameState.UNIT_SIZE, GameState.UNIT_SIZE, null);
        }
    }

    private void drawHealthItem(Graphics g) {
        if (state.hasHealthItem() && state.getHealthItem() != null) {
            Point p = state.getHealthItem();
            g.setColor(Color.PINK);
            g.fillRect(p.x, p.y, GameState.UNIT_SIZE, GameState.UNIT_SIZE);
            g.setColor(Color.RED);
            g.drawString("❤", p.x + GameState.UNIT_SIZE/3, p.y + GameState.UNIT_SIZE*2/3);
        }
    }

    private void drawShieldItem(Graphics g) {
        if (state.hasShieldItem() && state.getShieldItem() != null) {
            Point p = state.getShieldItem();
            g.setColor(Color.CYAN);
            g.fillRect(p.x, p.y, GameState.UNIT_SIZE, GameState.UNIT_SIZE);
            g.setColor(Color.BLUE);
            g.drawString("🛡", p.x + GameState.UNIT_SIZE/3, p.y + GameState.UNIT_SIZE*2/3);
        }
    }

    private void drawInvincibleItem(Graphics g) {
        if (state.hasInvincibleItem() && state.getInvincibleItem() != null) {
            BufferedImage invImg = assets.getInvincibleItemImage();
            if (invImg != null) g.drawImage(invImg, state.getInvincibleItem().x, state.getInvincibleItem().y,
                    GameState.UNIT_SIZE, GameState.UNIT_SIZE, null);
        }
    }

    private void drawSnake(Graphics g) {
        BufferedImage headImg = assets.getSnakeHeadImage();
        BufferedImage bodyImg = assets.getSnakeBodyImage();
        LinkedList<Point> snake = state.getSnake();
        for (int i = 0; i < snake.size(); i++) {
            Point p = snake.get(i);
            if (i == 0) {
                if (headImg != null) g.drawImage(headImg, p.x, p.y, GameState.UNIT_SIZE, GameState.UNIT_SIZE, null);
            } else {
                if (bodyImg != null) g.drawImage(bodyImg, p.x, p.y, GameState.UNIT_SIZE, GameState.UNIT_SIZE, null);
            }
            g.setColor(Color.BLACK);
            g.drawRect(p.x, p.y, GameState.UNIT_SIZE, GameState.UNIT_SIZE);
        }
    }

    private void drawAISnakes(Graphics g) {
        for (AISnake ai : state.getAiSnakes()) {
            LinkedList<Point> body = ai.getBody();
            for (int i = 0; i < body.size(); i++) {
                Point p = body.get(i);
                Color color = (i == 0) ? Color.ORANGE : new Color(200, 100, 0);
                g.setColor(color);
                g.fillRect(p.x, p.y, GameState.UNIT_SIZE, GameState.UNIT_SIZE);
                g.setColor(Color.BLACK);
                g.drawRect(p.x, p.y, GameState.UNIT_SIZE, GameState.UNIT_SIZE);
            }
            // 血条
            Point head = ai.getHead();
            int barWidth = GameState.UNIT_SIZE;
            int barHeight = 4;
            g.setColor(HEALTH_BAR_BG);
            g.fillRect(head.x, head.y - barHeight, barWidth, barHeight);
            g.setColor(HEALTH_BAR_FG);
            g.fillRect(head.x, head.y - barHeight, barWidth * ai.getHp() / ai.getMaxHp(), barHeight);
        }
    }

    private void drawGameStatus(Graphics g) {
        g.setFont(new Font("微软雅黑", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        if (!state.isRunning() && !state.isDead()) {
            g.drawString("游戏结束! 得分: " + state.getScore(),
                    GameState.BOARD_WIDTH/2 - 80, GameState.BOARD_HEIGHT/2);
        } else if (state.isDead()) {
            g.drawString("等待复活...", GameState.BOARD_WIDTH/2 - 60, GameState.BOARD_HEIGHT/2);
        }
    }

    private void drawUI(Graphics g) {
        g.setFont(new Font("微软雅黑", Font.BOLD, 14));
        g.setColor(UI_TEXT);
        g.drawString("血量: " + state.getPlayerHealth() + "/" + state.getPlayerMaxHealth(), 10, 20);
        g.drawString("护盾: " + state.getPlayerShield(), 10, 40);
        g.drawString("迷你豆豆计数: " + state.getMiniFoodEatenCount() + "/" + GameState.MINI_FOOD_COUNT_FOR_BONUS, 10, 60);
        if (state.isInvincible()) {
            g.setColor(Color.MAGENTA);
            g.drawString("无敌状态!", 10, 80);
        }
        if (state.isCarnival()) {
            g.setColor(Color.ORANGE);
            g.drawString("狂欢时刻!", 10, 100);
        }
    }
}