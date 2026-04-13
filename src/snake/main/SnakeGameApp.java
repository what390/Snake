package snake.main;

import javax.swing.*;

public class SnakeGameApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StartDialog();   // 启动界面，内部会调用 GameMainFrame
        });
    }
}