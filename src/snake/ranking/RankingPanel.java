package snake.ranking;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RankingPanel extends JPanel {
    private JList<String> rankingList;
    private DefaultListModel<String> listModel;

    public RankingPanel() {
        setLayout(new BorderLayout());
        listModel = new DefaultListModel<>();
        rankingList = new JList<>(listModel);
        rankingList.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        rankingList.setFixedCellHeight(20);
        rankingList.setBackground(new Color(250, 250, 250));
        JScrollPane scrollPane = new JScrollPane(rankingList);
        scrollPane.setPreferredSize(new Dimension(200, 350));
        add(scrollPane, BorderLayout.CENTER);
        refreshRanking();
    }

    public void refreshRanking() {
        listModel.clear();
        List<RankEntry> topEntries = RankingManager.getInstance().getTopN(10);
        int rank = 1;
        for (RankEntry entry : topEntries) {
            listModel.addElement(String.format("%d. %s  %d分", rank++, entry.getNickname(), entry.getScore()));
        }
        if (topEntries.isEmpty()) {
            listModel.addElement("暂无成绩");
        }
    }
}