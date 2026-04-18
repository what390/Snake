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

        // 检查食物、迷你豆豆、金币、道具
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

        // 移动蛇身
        state.getSnake().addFirst(newHead);
        if (!ateFood && !ateMini) {
            state.getSnake().removeLast();
        } else {
            if (ateFood) {
                state.setScore(state.getScore() + 1);
                 // 添加吃普通食物音效
                state.getFoods().remove(eatenFood);
            } else if (ateMini) {
                state.setScore(state.getScore() + 1);
                // 添加吃迷你豆豆音效
                int count = state.getMiniFoodEatenCount() + 1;
                if (count >= GameState.MINI_FOOD_COUNT_FOR_BONUS) {
                    Point tail = state.getSnake().getLast();
                    state.getSnake().addLast(tail);
                    count = 0;
                }
                state.setMiniFoodEatenCount(count);
                state.getMiniFoods().remove(eatenMini);
            }
        }
        if (ateCoin) {
            state.setCoins(state.getCoins() + 1);
              // 金币音效（可选，保留）
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
        }
    }

    public boolean checkPlayerCollision() {
        Point head = state.getSnake().getFirst();
        for (int i = 1; i < state.getSnake().size(); i++) {
            if (state.getSnake().get(i).equals(head)) return true;
        }
        if (state.getStones().contains(head) && !state.isInvincible()) return true;
        if (!state.isInvincible()) {
            for (AISnake ai : state.getAiSnakes()) {
                if (ai.getBody().contains(head)) return true;
            }
        }
        return false;
    }
}