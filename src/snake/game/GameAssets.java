package snake.game;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 游戏资源类，负责生成所有贴图（无需外部文件）
 */
public class GameAssets {
    private static final int BOARD_WIDTH = 600;
    private static final int BOARD_HEIGHT = 600;
    private static final int UNIT_SIZE = 25;

    private BufferedImage backgroundImage;
    private BufferedImage snakeHeadImage;
    private BufferedImage snakeBodyImage;
    private BufferedImage foodImage;
    private BufferedImage coinImage;
    private BufferedImage stoneImage;
    private BufferedImage invincibleItemImage;

    public GameAssets() {
        generateImages();
    }

    private void generateImages() {
        backgroundImage = createDefaultBackground();   // 美观的背景
        snakeHeadImage = createDefaultSnakeHead();
        snakeBodyImage = createDefaultSnakeBody();
        foodImage = createDefaultFood();
        coinImage = createDefaultCoin();
        stoneImage = createDefaultStone();
        invincibleItemImage = createDefaultInvincibleItem();
    }

    // 美观的渐变背景（蓝色渐变 + 随机星星 + 半透明网格）
    private BufferedImage createDefaultBackground() {
        BufferedImage img = new BufferedImage(BOARD_WIDTH, BOARD_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();

        // 1. 绘制渐变背景（从上到下从深蓝到浅蓝）
        GradientPaint gradient = new GradientPaint(0, 0, new Color(10, 30, 80),
                0, BOARD_HEIGHT, new Color(70, 130, 200));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        // 2. 绘制星星（随机白色圆点）
        g2d.setColor(Color.WHITE);
        int starCount = 150;
        for (int i = 0; i < starCount; i++) {
            int x = (int)(Math.random() * BOARD_WIDTH);
            int y = (int)(Math.random() * BOARD_HEIGHT);
            int size = (int)(Math.random() * 3) + 1;
            g2d.fillOval(x, y, size, size);
        }

        // 3. 绘制淡淡的光晕（半透明白色圆点）
        g2d.setColor(new Color(255, 255, 200, 30));
        for (int i = 0; i < 30; i++) {
            int x = (int)(Math.random() * BOARD_WIDTH);
            int y = (int)(Math.random() * BOARD_HEIGHT);
            int r = (int)(Math.random() * 15) + 5;
            g2d.fillOval(x, y, r, r);
        }

        // 4. 绘制网格线（浅蓝色半透明，不干扰视觉）
        g2d.setColor(new Color(200, 200, 255, 80));
        for (int i = 0; i <= BOARD_WIDTH / UNIT_SIZE; i++) {
            g2d.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, BOARD_HEIGHT);
            g2d.drawLine(0, i * UNIT_SIZE, BOARD_WIDTH, i * UNIT_SIZE);
        }

        g2d.dispose();
        return img;
    }

    // 蛇头（绿色方块 + 眼睛）
    private BufferedImage createDefaultSnakeHead() {
        BufferedImage img = new BufferedImage(UNIT_SIZE, UNIT_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.GREEN);
        g2d.fillRect(0, 0, UNIT_SIZE, UNIT_SIZE);
        g2d.setColor(Color.BLACK);
        g2d.fillOval(UNIT_SIZE / 4, UNIT_SIZE / 4, UNIT_SIZE / 6, UNIT_SIZE / 6);
        g2d.fillOval(UNIT_SIZE * 3 / 4 - UNIT_SIZE / 6, UNIT_SIZE / 4, UNIT_SIZE / 6, UNIT_SIZE / 6);
        g2d.dispose();
        return img;
    }

    // 蛇身（深绿 + 浅绿圆点）
    private BufferedImage createDefaultSnakeBody() {
        BufferedImage img = new BufferedImage(UNIT_SIZE, UNIT_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(new Color(45, 180, 0));
        g2d.fillRect(0, 0, UNIT_SIZE, UNIT_SIZE);
        g2d.setColor(Color.GREEN);
        g2d.fillOval(UNIT_SIZE / 3, UNIT_SIZE / 3, UNIT_SIZE / 3, UNIT_SIZE / 3);
        g2d.dispose();
        return img;
    }

    // 食物（红色圆）
    private BufferedImage createDefaultFood() {
        BufferedImage img = new BufferedImage(UNIT_SIZE, UNIT_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.RED);
        g2d.fillOval(0, 0, UNIT_SIZE, UNIT_SIZE);
        g2d.setColor(Color.ORANGE);
        g2d.drawOval(0, 0, UNIT_SIZE - 1, UNIT_SIZE - 1);
        g2d.dispose();
        return img;
    }

    // 金币（黄色圆 + "$"）
    private BufferedImage createDefaultCoin() {
        BufferedImage img = new BufferedImage(UNIT_SIZE, UNIT_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(0, 0, UNIT_SIZE, UNIT_SIZE);
        g2d.setColor(Color.ORANGE);
        g2d.drawOval(0, 0, UNIT_SIZE - 1, UNIT_SIZE - 1);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, UNIT_SIZE / 2));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "$";
        int x = (UNIT_SIZE - fm.stringWidth(text)) / 2;
        int y = (UNIT_SIZE - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(text, x, y);
        g2d.dispose();
        return img;
    }

    // 石头（灰色方块）
    private BufferedImage createDefaultStone() {
        BufferedImage img = new BufferedImage(UNIT_SIZE, UNIT_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.GRAY);
        g2d.fillRect(0, 0, UNIT_SIZE, UNIT_SIZE);
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(2, 2, UNIT_SIZE - 4, UNIT_SIZE - 4);
        g2d.dispose();
        return img;
    }

    // 无敌道具（紫色圆 + 星号）
    private BufferedImage createDefaultInvincibleItem() {
        BufferedImage img = new BufferedImage(UNIT_SIZE, UNIT_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.MAGENTA);
        g2d.fillOval(0, 0, UNIT_SIZE, UNIT_SIZE);
        g2d.setColor(Color.WHITE);
        g2d.drawString("★", UNIT_SIZE / 3, UNIT_SIZE * 2 / 3);
        g2d.dispose();
        return img;
    }

    // Getter 方法
    public BufferedImage getBackgroundImage() { return backgroundImage; }
    public BufferedImage getSnakeHeadImage() { return snakeHeadImage; }
    public BufferedImage getSnakeBodyImage() { return snakeBodyImage; }
    public BufferedImage getFoodImage() { return foodImage; }
    public BufferedImage getCoinImage() { return coinImage; }
    public BufferedImage getStoneImage() { return stoneImage; }
    public BufferedImage getInvincibleItemImage() { return invincibleItemImage; }
}