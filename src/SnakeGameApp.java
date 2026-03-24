import javax.swing.*;
public class SnakeGameApp {
        public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> {
                String nickname = JOptionPane.showInputDialog(null, "请输入你的昵称:", "玩家信息", JOptionPane.PLAIN_MESSAGE);
                if (nickname == null || nickname.trim().isEmpty()) {
                    nickname = "匿名玩家";

                }
                new GameMainFrame(nickname.trim());
            });
        }
    }
