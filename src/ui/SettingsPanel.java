package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import ai.StrategyFactory;
import engine.GameController;
import util.Constants;

//Allows the user to configure game settings and difficulty.
public class SettingsPanel extends JPanel {

    private final GameController controller;

    private final JLabel title = new JLabel("Settings");

    private final JSpinner scoreLimitSp = new JSpinner(new SpinnerNumberModel(Constants.DEFAULT_SCORE_LIMIT, 10, 999, 5));
    private final JSpinner maxRoundsSp = new JSpinner(new SpinnerNumberModel(Constants.DEFAULT_MAX_ROUNDS, 1, 999, 1));

    private final JSpinner initialHandSp = new JSpinner(new SpinnerNumberModel(Constants.DEFAULT_INITIAL_HAND_SIZE, 1, 20, 1));
    private final JSpinner maxHandSp = new JSpinner(new SpinnerNumberModel(Constants.DEFAULT_MAX_HAND_SIZE, 1, 30, 1));

    private final JSpinner initialEnergySp = new JSpinner(new SpinnerNumberModel(Constants.DEFAULT_INITIAL_ENERGY, 0, 99, 1));
    private final JSpinner energyGainSp = new JSpinner(new SpinnerNumberModel(Constants.DEFAULT_ENERGY_GAIN_PER_ROUND, 0, 20, 1));

    private final JComboBox<StrategyFactory.Difficulty> difficultyCb =
            new JComboBox<>(new StrategyFactory.Difficulty[]{
                    StrategyFactory.Difficulty.EASY,
                    StrategyFactory.Difficulty.NORMAL,
                    StrategyFactory.Difficulty.HARD
            });

    private final JButton applyBtn = new JButton("Apply");
    private final JButton defaultsBtn = new JButton("Reset Defaults");
    private final JButton backBtn = new JButton("Back");

    private Runnable onBack;

    public SettingsPanel(GameController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(12, 12));

        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        add(form, BorderLayout.CENTER);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(6, 6, 6, 6);

        addRow(form, gc, "Score Limit:", scoreLimitSp);
        addRow(form, gc, "Max Rounds:", maxRoundsSp);

        addRow(form, gc, "Initial Hand Size:", initialHandSp);
        addRow(form, gc, "Max Hand Size:", maxHandSp);

        addRow(form, gc, "Initial Energy:", initialEnergySp);
        addRow(form, gc, "Energy Gain / Round:", energyGainSp);

        addRow(form, gc, "Difficulty:", difficultyCb);

        if (controller != null && controller.getDifficulty() != null) {
            difficultyCb.setSelectedItem(controller.getDifficulty());
        } else {
            difficultyCb.setSelectedIndex(Constants.DEFAULT_DIFFICULTY_INDEX);
        }

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottom.add(defaultsBtn);
        bottom.add(applyBtn);
        bottom.add(backBtn);
        add(bottom, BorderLayout.SOUTH);

        defaultsBtn.addActionListener(e -> resetDefaults());
        applyBtn.addActionListener(e -> apply());
        backBtn.addActionListener(e -> {
            if (onBack != null) onBack.run();
        });
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    private void addRow(JPanel form, GridBagConstraints gc, String label, JComponent comp) {
        gc.gridx = 0;
        gc.weightx = 0;
        form.add(new JLabel(label), gc);

        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        comp.setPreferredSize(new java.awt.Dimension(160, comp.getPreferredSize().height));
        form.add(comp, gc);

        gc.gridy++;
        gc.fill = GridBagConstraints.NONE;
    }

    private void resetDefaults() {
        scoreLimitSp.setValue(Constants.DEFAULT_SCORE_LIMIT);
        maxRoundsSp.setValue(Constants.DEFAULT_MAX_ROUNDS);
        initialHandSp.setValue(Constants.DEFAULT_INITIAL_HAND_SIZE);
        maxHandSp.setValue(Constants.DEFAULT_MAX_HAND_SIZE);
        initialEnergySp.setValue(Constants.DEFAULT_INITIAL_ENERGY);
        energyGainSp.setValue(Constants.DEFAULT_ENERGY_GAIN_PER_ROUND);

        difficultyCb.setSelectedIndex(Constants.DEFAULT_DIFFICULTY_INDEX);
    }

    private void apply() {
        if (controller == null) return;

        int scoreLimit = (Integer) scoreLimitSp.getValue();
        int maxRounds = (Integer) maxRoundsSp.getValue();
        int initialHand = (Integer) initialHandSp.getValue();
        int maxHand = (Integer) maxHandSp.getValue();
        int initialEnergy = (Integer) initialEnergySp.getValue();
        int energyGain = (Integer) energyGainSp.getValue();

        StrategyFactory.Difficulty diff = (StrategyFactory.Difficulty) difficultyCb.getSelectedItem();
        if (diff == null) diff = StrategyFactory.Difficulty.NORMAL;

        controller.setScoreLimit(scoreLimit);
        controller.setMaxRounds(maxRounds);
        controller.setInitialHandSize(initialHand);
        controller.setMaxHandSize(maxHand);
        controller.setInitialEnergy(initialEnergy);
        controller.setEnergyGainPerRound(energyGain);

        controller.setDifficulty(diff);

        controller.notifyStateChanged();
        Dialogs.info(this, "Settings", "Applied.");
    }
}
