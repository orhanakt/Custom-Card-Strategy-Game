package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

//Displays the game rules and instructions.
public class RulesPanel extends JPanel {

    private final JTextArea text = new JTextArea();
    private final JButton backBtn = new JButton("Back");

    private Runnable onBack;

    public RulesPanel() {
        setLayout(new BorderLayout(10, 10));

        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);

        text.setText(buildRulesText());

        add(new JScrollPane(text), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottom.add(backBtn);
        add(bottom, BorderLayout.SOUTH);

        backBtn.addActionListener(e -> {
            if (onBack != null) onBack.run();
        });
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    private String buildRulesText() {
        return ""
                + "Custom Card Strategy Game Rules\n"
                + "\n"
                + "Goal:\n"
                + "- Beat the computer by reaching the score limit first (or having the higher score when rounds end).\n"
                + "\n"
                + "Cards:\n"
                + "- Each card has ATK, DEF, and COST.\n"
                + "- COST consumes energy when you play the card.\n"
                + "\n"
                + "Turns:\n"
                + "- The game is turn-based.\n"
                + "- On your turn, you may play a card, draw a card (if the deck is not empty), or end your turn.\n"
                + "\n"
                + "Round Resolution:\n"
                + "- Both sides play one card in a round.\n"
                + "- Damage = ATK - opponent effective DEF (minimum 0).\n"
                + "- Higher damage wins the round.\n"
                + "- Winner gains points equal to their damage (double score ability can multiply it).\n"
                + "\n"
                + "Win Conditions:\n"
                + "- First to reach the score limit wins, or\n"
                + "- When maximum rounds are reached, the higher score wins.\n";
    }
}
