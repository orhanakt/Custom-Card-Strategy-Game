package ui;

import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;

import engine.GameController;
import engine.GameState;
import model.Phase;
import util.Constants;

//Main application window that manages panel navigation.
public class MainFrame extends JFrame {

    private final GameController controller;

    private final CardLayout layout = new CardLayout();
    private final JPanel root = new JPanel(layout);

    private final MainMenuPanel menuPanel;
    private final GamePanel gamePanel;
    private final GameOverPanel gameOverPanel;
    private final HighScoresPanel highScoresPanel;
    private final SettingsPanel settingsPanel;
    private final RulesPanel rulesPanel;

    public MainFrame() {
        super(Constants.APP_TITLE);

        controller = new GameController();

        menuPanel = new MainMenuPanel(controller);
        gamePanel = new GamePanel(controller);
        gameOverPanel = new GameOverPanel(controller);
        highScoresPanel = new HighScoresPanel(controller);
        settingsPanel = new SettingsPanel(controller);
        rulesPanel = new RulesPanel();

        root.add(menuPanel, "MENU");
        root.add(gamePanel, "GAME");
        root.add(gameOverPanel, "GAME_OVER");
        root.add(highScoresPanel, "SCORES");
        root.add(settingsPanel, "SETTINGS");
        root.add(rulesPanel, "RULES");

        setContentPane(root);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(900, 650));
        pack();
        setLocationRelativeTo(null);

        wireMenuActions();
        controller.setOnStateChanged(this::refreshFromState);

        refreshFromState();
    }

    private void wireMenuActions() {
        menuPanel.setOnStart(playerName -> {
            controller.startNewGame(playerName); 
            show("GAME");
        });

        menuPanel.setOnShowRules(() -> show("RULES"));
        rulesPanel.setOnBack(() -> show("MENU"));

        menuPanel.setOnShowScores(() -> {
            highScoresPanel.refresh();
            show("SCORES");
        });

        menuPanel.setOnShowSettings(() -> show("SETTINGS"));
        menuPanel.setOnExit(() -> dispose());

        gameOverPanel.setOnRestart(() -> {
            controller.restartGame();
            show("GAME");
        });

        gameOverPanel.setOnMenu(() -> {
            controller.returnToMenu();
            show("MENU");
        });

        settingsPanel.setOnBack(() -> show("MENU"));
        highScoresPanel.setOnBack(() -> show("MENU"));
        menuPanel.setOnContinue(() -> show("GAME"));
    }


    private void refreshFromState() {
        GameState state = controller.getState();
        if (state == null) {
            show("MENU");
            return;
        }

        if (state.getPhase() == Phase.MENU) {
            show("MENU");
        } else if (state.getPhase() == Phase.PLAYING) {
            gamePanel.refresh();
            show("GAME");
        } else if (state.getPhase() == Phase.GAME_OVER) {
            gameOverPanel.refresh();
            show("GAME_OVER");
        } else {
            show("MENU");
        }
    }

    private void show(String key) {
        layout.show(root, key);
    }
}
