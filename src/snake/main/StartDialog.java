package snake.main;
import snake.main.GameMainFrame;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 游戏启动界面：选择难度、输入昵称
 */
public class StartDialog extends JDialog {
    private JComboBox<String> difficultyCombo;
    private JTextField nicknameField;
    private JButton startButton;
    private int selectedDifficulty = 1;   // 默认中速
    private String nickname = "匿名玩家";

    public StartDialog() {
        setTitle("贪吃蛇游戏 - 启动");
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        // 主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(new Color(240, 248, 255)); // 淡蓝色背景

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // 标题
        JLabel titleLabel = new JLabel("麻辣洗的贪吃蛇");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 100, 0));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // 难度选择
        JLabel difficultyLabel = new JLabel("选择难度：");
        difficultyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(difficultyLabel, gbc);

        String[] difficulties = {"慢速", "中速", "快速"};
        difficultyCombo = new JComboBox<>(difficulties);
        difficultyCombo.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        difficultyCombo.setPreferredSize(new Dimension(120, 30));
        gbc.gridx = 1;
        mainPanel.add(difficultyCombo, gbc);

        // 昵称输入（美化框框）
        JLabel nicknameLabel = new JLabel("玩家昵称：");
        nicknameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(nicknameLabel, gbc);

        nicknameField = new JTextField(12);
        nicknameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        nicknameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 150, 200), 2),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        nicknameField.setBackground(new Color(255, 255, 240));
        nicknameField.setToolTipText("请输入您的昵称（默认：匿名玩家）");
        gbc.gridx = 1;
        mainPanel.add(nicknameField, gbc);

        // 开始按钮
        startButton = new JButton("开始游戏");
        startButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        startButton.setBackground(new Color(50, 150, 50));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 获取难度
                selectedDifficulty = difficultyCombo.getSelectedIndex(); // 0慢 1中 2快
                // 获取昵称
                String inputName = nicknameField.getText().trim();
                if (inputName.isEmpty()) {
                    nickname = "匿名玩家";
                } else {
                    nickname = inputName;
                }
                // 关闭对话框
                dispose();
                // 启动游戏主窗口
                new GameMainFrame(nickname, selectedDifficulty);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(startButton);
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public int getSelectedDifficulty() {
        return selectedDifficulty;
    }

    public String getNickname() {
        return nickname;
    }
}