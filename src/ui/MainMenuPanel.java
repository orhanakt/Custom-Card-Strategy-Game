package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import engine.GameController;

//Main menu screen for starting, continuing, or exiting the game.
public class MainMenuPanel extends JPanel {

    private final GameController controller;

    private final JLabel title = new JLabel("Custom Card Strategy Game");
    private final JLabel subtitle = new JLabel("Enter your name and start playing");

    private final JTextField nameTf = new JTextField(18);

    private final JButton startBtn = new JButton("Start New Game");
    private final JButton continueBtn = new JButton("Continue");

    private final JButton rulesBtn = new JButton("Game Rules");
    private final JButton scoresBtn = new JButton("High Scores");
    private final JButton settingsBtn = new JButton("Settings");
    private final JButton exitBtn = new JButton("Exit");

    private Consumer<String> onStart;
    private Runnable onContinue;
    private Runnable onShowRules;
    private Runnable onShowScores;
    private Runnable onShowSettings;
    private Runnable onExit;

    public MainMenuPanel(GameController controller) {
        this.controller = controller;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(22, 22, 22, 22));

        buildTop();
        buildCenter();
        refreshContinueEnabled();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (SwingUtilities.getRootPane(this) != null) {
            SwingUtilities.getRootPane(this).setDefaultButton(startBtn);
        }
        refreshContinueEnabled();
    }

    public void refreshContinueEnabled() {
        boolean hasSave = controller != null && controller.hasSavedGame();
        continueBtn.setEnabled(hasSave);
    }

    private void buildTop() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        title.setFont(title.getFont().deriveFont(Font.BOLD, 30f));
        title.setHorizontalAlignment(JLabel.CENTER);

        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 14f));
        subtitle.setHorizontalAlignment(JLabel.CENTER);
        subtitle.setForeground(new Color(90, 90, 90));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 6));
        titleBox.setOpaque(false);
        titleBox.add(title);
        titleBox.add(subtitle);

        top.add(titleBox, BorderLayout.CENTER);
        top.setBorder(new EmptyBorder(8, 0, 18, 0));

        add(top, BorderLayout.NORTH);
    }

    private void buildCenter() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);
        add(wrap, BorderLayout.CENTER);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setOpaque(true);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(210, 210, 210), 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));
        card.setPreferredSize(new Dimension(520, 280));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.insets = new Insets(8, 8, 8, 8);
        gc.anchor = GridBagConstraints.CENTER;

        JLabel nameLbl = new JLabel("Player Name:");
        nameLbl.setFont(nameLbl.getFont().deriveFont(Font.BOLD, 14f));

        nameTf.setFont(nameTf.getFont().deriveFont(Font.PLAIN, 14f));
        nameTf.setPreferredSize(new Dimension(240, 28));

        startBtn.setFont(startBtn.getFont().deriveFont(Font.BOLD, 14f));
        startBtn.setPreferredSize(new Dimension(220, 34));

        continueBtn.setFont(continueBtn.getFont().deriveFont(Font.BOLD, 14f));
        continueBtn.setPreferredSize(new Dimension(220, 34));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints fc = new GridBagConstraints();
        fc.gridx = 0;
        fc.gridy = 0;
        fc.insets = new Insets(0, 0, 0, 10);
        fc.anchor = GridBagConstraints.EAST;
        form.add(nameLbl, fc);

        fc.gridx = 1;
        fc.weightx = 1;
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.anchor = GridBagConstraints.WEST;
        form.add(nameTf, fc);

        JPanel actions = new JPanel(new GridLayout(2, 2, 10, 10));
        actions.setOpaque(false);

        Dimension btnSize = new Dimension(160, 32);
        rulesBtn.setPreferredSize(btnSize);
        scoresBtn.setPreferredSize(btnSize);
        settingsBtn.setPreferredSize(btnSize);
        exitBtn.setPreferredSize(btnSize);

        actions.add(rulesBtn);
        actions.add(scoresBtn);
        actions.add(settingsBtn);
        actions.add(exitBtn);

        JPanel topButtons = new JPanel(new GridLayout(1, 2, 10, 0));
        topButtons.setOpaque(false);
        topButtons.add(startBtn);
        topButtons.add(continueBtn);

        gc.gridx = 0;
        gc.gridy = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        card.add(form, gc);

        gc.gridy = 1;
        gc.insets = new Insets(14, 8, 8, 8);
        card.add(topButtons, gc);

        gc.gridy = 2;
        gc.insets = new Insets(10, 8, 0, 8);
        card.add(actions, gc);

        GridBagConstraints wc = new GridBagConstraints();
        wc.gridx = 0;
        wc.gridy = 0;
        wc.anchor = GridBagConstraints.CENTER;
        wrap.add(card, wc);

        startBtn.addActionListener(e -> fireStart());
        nameTf.addActionListener(e -> fireStart());

        continueBtn.addActionListener(e -> fireContinue());

        rulesBtn.addActionListener(e -> {
            if (onShowRules != null) onShowRules.run();
        });

        scoresBtn.addActionListener(e -> {
            if (onShowScores != null) onShowScores.run();
        });

        settingsBtn.addActionListener(e -> {
            if (onShowSettings != null) onShowSettings.run();
        });

        exitBtn.addActionListener(e -> {
            if (onExit != null) onExit.run();
        });
    }

    private void fireStart() {
        String name = nameTf.getText() == null ? "" : nameTf.getText().trim();
        if (name.isEmpty()) name = "Player";
        if (onStart != null) onStart.accept(name);
    }

    private void fireContinue() {
        refreshContinueEnabled();
        if (!continueBtn.isEnabled()) {
            Dialogs.error(this, "Continue", "No saved game found.");
            return;
        }

        boolean ok = controller != null && controller.loadGame();
        refreshContinueEnabled();

        if (!ok) {
            Dialogs.error(this, "Continue", "Save file is missing or corrupted.");
            return;
        }

        if (onContinue != null) onContinue.run();
    }

    public void setOnStart(Consumer<String> onStart) {
        this.onStart = onStart;
    }

    public void setOnContinue(Runnable onContinue) {
        this.onContinue = onContinue;
    }

    public void setOnShowRules(Runnable onShowRules) {
        this.onShowRules = onShowRules;
    }

    public void setOnShowScores(Runnable onShowScores) {
        this.onShowScores = onShowScores;
    }

    public void setOnShowSettings(Runnable onShowSettings) {
        this.onShowSettings = onShowSettings;
    }

    public void setOnExit(Runnable onExit) {
        this.onExit = onExit;
    }
}
