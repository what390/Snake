import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameMainFrame extends JFrame {
    private GamePanel gamePanel;
    private RankingPanel rankingPanel;
    private JLabel scoreLabel, lengthLabel, coinsLabel, nicknameLabel;
    private JButton restartButton;
    private String playerName;

    public GameMainFrame(String playerName) {
        this.playerName = playerName;
        setTitle("贪吃蛇游戏 - 玩家: " + playerName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        // 1. 创建右侧面板及其所有组件（顺序重要）
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(220, 600));
        rightPanel.setBackground(new Color(240, 240, 240));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        nicknameLabel = new JLabel("昵称: " + playerName);
        nicknameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        nicknameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        scoreLabel = new JLabel("得分: 0");
        lengthLabel = new JLabel("长度: 3");
        coinsLabel = new JLabel("金币: 0");
        Font infoFont = new Font("微软雅黑", Font.PLAIN, 14);
        scoreLabel.setFont(infoFont);
        lengthLabel.setFont(infoFont);
        coinsLabel.setFont(infoFont);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        lengthLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        coinsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        rankingPanel = new RankingPanel();
        rankingPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rankingPanel.setBorder(BorderFactory.createTitledBorder("成绩排行榜"));

        restartButton = new JButton("重新开始");
        restartButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        restartButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(nicknameLabel);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(scoreLabel);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(lengthLabel);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(coinsLabel);
        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(rankingPanel);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(restartButton);
        rightPanel.add(Box.createVerticalStrut(10));

        // 2. 创建游戏面板（此时右侧组件已存在）
        gamePanel = new GamePanel(this, playerName);

        // 3. 添加按钮监听器（在 gamePanel 创建之后）
        restartButton.addActionListener(e -> {
            gamePanel.resetGame();
            // 重置后立即将焦点还给游戏面板
            gamePanel.requestFocusInWindow();
        });

        // 4. 将面板添加到窗口
        add(gamePanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // 5. 设置焦点策略：确保游戏面板是第一个获得焦点的组件
        setFocusTraversalPolicy(new FocusTraversalPolicy() {
            @Override
            public Component getComponentAfter(Container aContainer, Component aComponent) {
                return gamePanel;
            }
            @Override
            public Component getComponentBefore(Container aContainer, Component aComponent) {
                return gamePanel;
            }
            @Override
            public Component getFirstComponent(Container aContainer) {
                return gamePanel;
            }
            @Override
            public Component getLastComponent(Container aContainer) {
                return gamePanel;
            }
            @Override
            public Component getDefaultComponent(Container aContainer) {
                return gamePanel;
            }
        });

        // 6. 窗口激活时，强制将焦点还给游戏面板
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                gamePanel.requestFocusInWindow();
            }
        });

        // 7. 延迟请求焦点，确保窗口完全显示后焦点生效
        SwingUtilities.invokeLater(() -> gamePanel.requestFocusInWindow());
    }

    public void updateGameInfo(int score, int length, int coins) {
        scoreLabel.setText("得分: " + score);
        lengthLabel.setText("长度: " + length);
        coinsLabel.setText("金币: " + coins);
    }

    public void refreshRanking() {
        rankingPanel.refreshRanking();
    }

    public String getPlayerName() {
        return playerName;
    }
}  