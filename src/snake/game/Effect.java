package snake.game;

import java.awt.*;

public class Effect {
    public static final int TYPE_HIT = 0;
    public static final int TYPE_EXPLOSION = 1;

    private int x, y;
    private int type;
    private int life; // 剩余帧数
    private int radius;

    public Effect(int x, int y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.life = 10;
        if (type == TYPE_EXPLOSION) radius = 100;
    }

    public void update() {
        life--;
    }

    public boolean isExpired() {
        return life <= 0;
    }

    public void draw(Graphics g) {
        if (type == TYPE_HIT) {
            g.setColor(Color.WHITE);
            g.drawOval(x, y, GamePanel.UNIT_SIZE, GamePanel.UNIT_SIZE);
        } else if (type == TYPE_EXPLOSION) {
            g.setColor(new Color(255, 0, 0, 100));
            g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
            g.setColor(Color.RED);
            g.drawOval(x - radius, y - radius, radius * 2, radius * 2);
        }
    }
}