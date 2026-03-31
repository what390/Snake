import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameMainFrame extends JFrame {
    private GamePanel gamePanel;
    private RankingPanel rankingPanel;
    private JLabel scoreLabel, lengthLabel, coinsLabel, nicknameLabel;
    private JButton restartButton;
    private JComboBox<String> difficultyCombo;
    private String playerName;

    public GameMainFrame(String playerName, int difficulty) {
        this.playerName = playerName;
        setTitle("贪吃蛇游戏 - 玩家: " + playerName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        // 1. 创建右侧面板
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

        // 难度选择组件
        JPanel difficultyPanel = new JPanel();
        difficultyPanel.setLayout(new BoxLayout(difficultyPanel, BoxLayout.X_AXIS));
        difficultyPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel difficultyLabel = new JLabel("难度：");
        difficultyLabel.setFont(infoFont);
        String[] difficulties = {"慢速", "中速", "快速"};
        difficultyCombo = new JComboBox<>(difficulties);
        difficultyCombo.setFont(infoFont);
        difficultyCombo.setSelectedIndex(difficulty);  // 设置为传入的难度
        difficultyCombo.addActionListener(e -> changeDifficulty());
        difficultyPanel.add(difficultyLabel);
        difficultyPanel.add(difficultyCombo);

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
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(difficultyPanel);
        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(rankingPanel);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(restartButton);
        rightPanel.add(Box.createVerticalStrut(10));

        // 2. 创建游戏面板（传入难度）
        gamePanel = new GamePanel(this, playerName, difficulty);

        // 3. 按钮监听器
        restartButton.addActionListener(e -> {
            gamePanel.resetGame();
            gamePanel.requestFocusInWindow();
        });

        add(gamePanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // 焦点管理
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

        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                gamePanel.requestFocusInWindow();
            }
        });

        SwingUtilities.invokeLater(() -> gamePanel.requestFocusInWindow());
    }

    // 改变难度
    private void changeDifficulty() {
        int selected = difficultyCombo.getSelectedIndex();
        gamePanel.setSpeed(selected);
        gamePanel.requestFocusInWindow();
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