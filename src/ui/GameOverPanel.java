package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import engine.GameController;
import engine.GameState;
import model.Phase;

//Displays the game over screen and final results.
public class GameOverPanel extends JPanel {

    private final GameController controller;

    private final JLabel titleLbl = new JLabel("Game Over", SwingConstants.CENTER);
    private final JLabel winnerLbl = new JLabel("Winner: -", SwingConstants.CENTER);
    private final JLabel scoreLbl = new JLabel("Final Score — Player: - | Computer: -", SwingConstants.CENTER);
    private final JLabel hintLbl = new JLabel("High score saved (if applicable).", SwingConstants.CENTER);

    private final JButton restartBtn = new JButton("Restart Game");
    private final JButton menuBtn = new JButton("Return to Main Menu");

    private Runnable onRestart;
    private Runnable onMenu;

    public GameOverPanel(GameController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        add(buildCenterCard(), BorderLayout.CENTER);
        add(buildBottomButtons(), BorderLayout.SOUTH);

        restartBtn.addActionListener(e -> {
            if (onRestart != null) onRestart.run();
        });
        menuBtn.addActionListener(e -> {
            if (onMenu != null) onMenu.run();
        });

        refresh();
    }

    private JPanel buildCenterCard() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(wrap.getBackground().darker(), 1, true),
                BorderFactory.createEmptyBorder(22, 22, 22, 22)
        ));

        titleLbl.setFont(titleLbl.getFont().deriveFont(Font.BOLD, 44f));

        winnerLbl.setFont(winnerLbl.getFont().deriveFont(Font.BOLD, 22f));
        scoreLbl.setFont(scoreLbl.getFont().deriveFont(Font.PLAIN, 18f));
        hintLbl.setFont(hintLbl.getFont().deriveFont(Font.PLAIN, 14f));

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        center.add(titleLbl);
        center.add(Box.createVerticalStrut(18));
        center.add(winnerLbl);
        center.add(Box.createVerticalStrut(10));
        center.add(scoreLbl);
        center.add(Box.createVerticalStrut(14));
        center.add(hintLbl);

        wrap.add(center, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildBottomButtons() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 6));

        Dimension d = new Dimension(190, 38);
        restartBtn.setPreferredSize(d);
        menuBtn.setPreferredSize(d);
        restartBtn.setFocusPainted(false);
        menuBtn.setFocusPainted(false);
        restartBtn.setFont(restartBtn.getFont().deriveFont(Font.BOLD, 13.5f));
        menuBtn.setFont(menuBtn.getFont().deriveFont(Font.BOLD, 13.5f));

        bottom.add(restartBtn);
        bottom.add(menuBtn);
        return bottom;
    }

    public void setOnRestart(Runnable onRestart) {
        this.onRestart = onRestart;
    }

    public void setOnMenu(Runnable onMenu) {
        this.onMenu = onMenu;
    }

    public void refresh() {
        GameState state = controller == null ? null : controller.getState();
        if (state == null) return;

        if (state.getPhase() != Phase.GAME_OVER) {
            winnerLbl.setText("Winner: -");
            scoreLbl.setText("Final Score — Player: - | Computer: -");
            hintLbl.setText(" ");
            return;
        }

        int ps = state.getPlayer().getScore();
        int cs = state.getComputer().getScore();

        String winner;
        if (ps > cs) winner = "Player Wins!";
        else if (cs > ps) winner = "Computer Wins!";
        else winner = "Draw!";

        winnerLbl.setText("Winner: " + winner);
        scoreLbl.setText("Final Score — Player: " + ps + " | Computer: " + cs);

        hintLbl.setText("High score saved (if applicable). You can restart or return to menu.");
    }
}
