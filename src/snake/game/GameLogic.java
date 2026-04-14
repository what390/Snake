package snake.game;

import java.awt.Point;

public class GameLogic {
    private final GameState state;

    public GameLogic(GameState state) {
        this.state = state;
    }

    private Point getWrappedPosition(Point head, int deltaX, int deltaY) {
        int newX = head.x + deltaX;
        int newY = head.y + deltaY;
        if (newX < 0) newX = GameState.BOARD_WIDTH - GameState.UNIT_SIZE;
        else if (newX >= GameState.BOARD_WIDTH) newX = 0;
        if (newY < 0) newY = GameState.BOARD_HEIGHT - GameState.UNIT_SIZE;
        else if (newY >= GameState.BOARD_HEIGHT) newY = 0;
        return new Point(newX, newY);
    }

    public void movePlayer() {
        char direction = state.getDirection();
        state.setDirection(state.getNextDirection());
        Point head = state.getSnake().getFirst();
        int deltaX = 0, deltaY = 0;
        switch (state.getDirection()) {
            case 'U': deltaY = -GameState.UNIT_SIZE; break;
            case 'D': deltaY = GameState.UNIT_SIZE; break;
            case 'L': deltaX = -GameState.UNIT_SIZE; break;
            case 'R': deltaX = GameState.UNIT_SIZE; break;
        }
        Point newHead = getWrappedPosition(head, deltaX, deltaY);

        // 检查食物
        boolean ateFood = false;
        Point eatenFood = null;
        for (Point f : state.getFoods()) {
            if (newHead.equals(f)) {
                ateFood = true;
                eatenFood = f;
                break;
            }
        }
        boolean ateMini = false;
        Point eatenMini = null;
        for (Point mf : state.getMiniFoods()) {
            if (newHead.equals(mf)) {
                ateMini = true;
                eatenMini = mf;
                break;
            }
        }
        boolean ateCoin = state.hasCoin() && newHead.equals(state.getCoin());
        boolean ateHealth = state.hasHealthItem() && newHead.equals(state.getHealthItem());
        boolean ateShield = state.hasShieldItem() && newHead.equals(state.getShieldItem());
        boolean ateInvincible = state.hasInvincibleItem() && newHead.equals(state.getInvincibleItem());

        // 移动
        state.getSnake().addFirst(newHead);
        if (!ateFood && !ateMini) {
            state.getSnake().removeLast();
        } else {
            if (ateFood) {
                state.setScore(state.getScore() + 1);
                state.getFoods().remove(eatenFood);
                // 狂欢时刻补充食物由外部调用，此处简单生成一个
                if (state.isCarnival()) {
                    // 由调用方补充
                } else {
                    // 普通模式生成一个食物
                }
            } else if (ateMini) {
                state.setScore(state.getScore() + 1);
                int count = state.getMiniFoodEatenCount() + 1;
                if (count >= GameState.MINI_FOOD_COUNT_FOR_BONUS) {
                    // 长度增加已在外部处理，这里只重置计数
                    count = 0;
                }
                state.setMiniFoodEatenCount(count);
                state.getMiniFoods().remove(eatenMini);
            }
        }
        if (ateCoin) {
            state.setCoins(state.getCoins() + 1);
            state.setHasCoin(false);
            state.setCoin(null);
        }
        if (ateHealth && state.getPlayerHealth() < state.getPlayerMaxHealth()) {
            state.setPlayerHealth(state.getPlayerHealth() + 1);
            state.setHasHealthItem(false);
            state.setHealthItem(null);
        }
        if (ateShield && state.getPlayerShield() < GameState.MAX_SHIELD) {
            state.setPlayerShield(state.getPlayerShield() + 1);
            state.setHasShieldItem(false);
            state.setShieldItem(null);
        }
        if (ateInvincible) {
            state.setInvincible(true);
            state.setHasInvincibleItem(false);
            state.setInvincibleItem(null);
            // 无敌定时器由GamePanel管理
        }
    }

    public boolean checkPlayerCollision() {
        Point head = state.getSnake().getFirst();
        // 自身碰撞
        for (int i = 1; i < state.getSnake().size(); i++) {
            if (state.getSnake().get(i).equals(head)) return true;
        }
        // 石头碰撞（无敌时忽略）
        if (state.getStones().contains(head) && !state.isInvincible()) return true;
        // AI蛇碰撞（无敌时忽略）
        if (!state.isInvincible()) {
            for (AISnake ai : state.getAiSnakes()) {
                if (ai.getBody().contains(head)) return true;
            }
        }
        return false;
    }

    public void checkCarnival() {
        if (!state.isCarnival() && state.getSnake().size() >= 20) {
            state.setCarnival(true);
        }
    }
}