package snake.game;

public class Projectile {
    public static final int OWNER_PLAYER = 0;
    public static final int OWNER_BOSS = 1;

    public int x, y;
    private int dir; // 0上 1下 2左 3右
    private int speed;
    private int owner;

    public Projectile(int x, int y, int dir, int speed, int owner) {
        this.x = x;
        this.y = y;
        this.dir = dir;
        this.speed = speed;
        this.owner = owner;
    }

    public void move() {
        switch (dir) {
            case 0: y -= speed; break;
            case 1: y += speed; break;
            case 2: x -= speed; break;
            case 3: x += speed; break;
        }
    }

    public int getOwner() { return owner; }
}