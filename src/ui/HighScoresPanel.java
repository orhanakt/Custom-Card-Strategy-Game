package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import engine.GameController;
import persistence.HighScoreEntry;
import util.DateUtil;

//Displays the list of saved high scores.
public class HighScoresPanel extends JPanel {

    private final GameController controller;

    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"#", "Player", "Score", "Date"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);

    private final JButton refreshBtn = new JButton("Refresh");
    private final JButton backBtn = new JButton("Back");

    private Runnable onBack;

    public HighScoresPanel(GameController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(10, 10));

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottom.add(refreshBtn);
        bottom.add(backBtn);
        add(bottom, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> refresh());
        backBtn.addActionListener(e -> {
            if (onBack != null) onBack.run();
        });

        refresh();
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    public void refresh() {
        model.setRowCount(0);

        if (controller == null || controller.getHighScoreManager() == null) return;

        List<HighScoreEntry> list = controller.getHighScoreManager().getTop(25);
        for (int i = 0; i < list.size(); i++) {
            HighScoreEntry e = list.get(i);
            model.addRow(new Object[]{
                    i + 1,
                    e.getPlayerName(),
                    e.getScore(),
                    DateUtil.format(e.getDateTime())
            });
        }
    }
}
