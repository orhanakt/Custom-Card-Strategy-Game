package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import engine.GameController;
import engine.GameState;
import model.Card;
import model.Phase;
import model.RoundResult;

//Main gameplay screen that displays the battlefield, hand, and controls.
public class GamePanel extends JPanel {

    private final GameController controller;

    private final JLabel roundLabel = new JLabel("Round: -");
    private final JLabel turnLabel = new JLabel("Turn: -");
    private final JLabel deckLabel = new JLabel("Deck: -");

    private final JLabel playerStatsLabel = new JLabel("Player: -");
    private final JLabel computerStatsLabel = new JLabel("Computer: -");

    private final JLabel lastRoundLabel = new JLabel(" ");

    private final CardView playerPlayedView = new CardView();
    private final CardView computerPlayedView = new CardView();

    private final JPanel handPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
    private final JScrollPane handScroll = new JScrollPane(handPanel);

    private final JButton playBtn = new JButton("Play Card");
    private final JButton drawBtn = new JButton("Draw Card");
    private final JButton endTurnBtn = new JButton("End Turn");

    private final GameActionHandlers handlers;

    private int selectedIndex = -1;

    private final JButton saveBtn = new JButton("Save");
    private final JButton loadBtn = new JButton("Load");

    private final DefaultListModel<String> historyModel = new DefaultListModel<>();
    private final JList<String> historyList = new JList<>(historyModel);
    private int lastLoggedRound = -1;

    public GamePanel(GameController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new BorderLayout(12, 6));

        roundLabel.setFont(roundLabel.getFont().deriveFont(Font.BOLD, 16f));
        turnLabel.setFont(turnLabel.getFont().deriveFont(Font.BOLD, 16f));
        deckLabel.setFont(deckLabel.getFont().deriveFont(Font.BOLD, 16f));

        JPanel leftInfo = new JPanel();
        leftInfo.setLayout(new BoxLayout(leftInfo, BoxLayout.Y_AXIS));
        leftInfo.add(roundLabel);
        leftInfo.add(Box.createVerticalStrut(4));
        leftInfo.add(playerStatsLabel);

        JPanel midInfo = new JPanel();
        midInfo.setLayout(new BoxLayout(midInfo, BoxLayout.Y_AXIS));
        turnLabel.setAlignmentX(0.5f);
        midInfo.add(turnLabel);
        midInfo.add(Box.createVerticalStrut(4));
        lastRoundLabel.setFont(lastRoundLabel.getFont().deriveFont(Font.PLAIN, 13f));
        lastRoundLabel.setHorizontalAlignment(SwingConstants.CENTER);
        midInfo.add(lastRoundLabel);

        JPanel rightInfo = new JPanel();
        rightInfo.setLayout(new BoxLayout(rightInfo, BoxLayout.Y_AXIS));
        deckLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        computerStatsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        rightInfo.add(deckLabel);
        rightInfo.add(Box.createVerticalStrut(4));
        rightInfo.add(computerStatsLabel);

        top.add(leftInfo, BorderLayout.WEST);
        top.add(midInfo, BorderLayout.CENTER);
        top.add(rightInfo, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));

        JPanel battlefield = new JPanel(new BorderLayout());
        battlefield.setBorder(BorderFactory.createTitledBorder("Battlefield"));

        playerPlayedView.setPreferredCardSize(140, 185);
        computerPlayedView.setPreferredCardSize(140, 185);

        JPanel fightRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
        fightRow.add(wrapCardWithTitle("Computer Played", computerPlayedView));

        JLabel vs = new JLabel("VS");
        vs.setFont(vs.getFont().deriveFont(Font.BOLD, 18f));
        fightRow.add(vs);

        fightRow.add(wrapCardWithTitle("You Played", playerPlayedView));

        battlefield.add(fightRow, BorderLayout.CENTER);

        JPanel historyWrap = new JPanel(new BorderLayout(6, 6));
        historyWrap.setBorder(BorderFactory.createTitledBorder("Round History"));
        historyList.setVisibleRowCount(12);
        historyWrap.add(new JScrollPane(historyList), BorderLayout.CENTER);
        historyWrap.setPreferredSize(new Dimension(280, 360));

        JPanel playerArea = new JPanel(new BorderLayout(10, 8));
        playerArea.setBorder(BorderFactory.createTitledBorder("Your Hand"));

        handScroll.setPreferredSize(new Dimension(760, 210));
        handScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        handScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        playerArea.add(handScroll, BorderLayout.CENTER);

        center.add(battlefield, BorderLayout.CENTER);
        center.add(historyWrap, BorderLayout.EAST);
        center.add(playerArea, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        bottom.add(playBtn);
        bottom.add(drawBtn);
        bottom.add(endTurnBtn);
        bottom.add(saveBtn);
        bottom.add(loadBtn);
        add(bottom, BorderLayout.SOUTH);

        handlers = new GameActionHandlers(
                this,
                playBtn,
                drawBtn,
                endTurnBtn,
                () -> selectedIndex
        );

        handlers.bind(
                () -> this.controller == null ? null : this.controller.getState(),
                idx -> {
                    if (this.controller != null) this.controller.playerPlayCardAt(idx);
                    refresh();
                },
                () -> {
                    if (this.controller != null) this.controller.playerDraw();
                    refresh();
                },
                () -> {
                    if (this.controller != null) this.controller.endTurn();
                    refresh();
                }
        );

        saveBtn.addActionListener(e -> {
            if (controller == null) return;
            boolean ok = controller.saveGame();
            if (ok) Dialogs.info(this, "Save", "Game saved.");
            else Dialogs.error(this, "Save", "Save failed.");
        });

        loadBtn.addActionListener(e -> {
            if (controller == null) return;
            boolean ok = controller.loadGame();
            if (ok) {
                clearHistory();
                refresh();
                Dialogs.info(this, "Load", "Game loaded.");
            } else {
                Dialogs.error(this, "Load", "No valid save found.");
            }
        });

        refresh();
    }

    private JPanel wrapCardWithTitle(String title, CardView view) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setAlignmentX(0.5f);
        t.setFont(t.getFont().deriveFont(Font.BOLD, 13f));

        JPanel cardHolder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        cardHolder.add(view);

        p.add(t);
        p.add(Box.createVerticalStrut(6));
        p.add(cardHolder);

        return p;
    }

    public void refresh() {
        GameState state = (controller == null) ? null : controller.getState();

        if (state == null) {
            clearHistory();
            roundLabel.setText("Round: -");
            turnLabel.setText("Turn: -");
            deckLabel.setText("Deck: -");
            playerStatsLabel.setText("Player: -");
            computerStatsLabel.setText("Computer: -");
            lastRoundLabel.setText(" ");
            handPanel.removeAll();
            selectedIndex = -1;
            playerPlayedView.setCard(null);
            computerPlayedView.setCard(null);
            handlers.sync();
            revalidate();
            repaint();
            return;
        }

        roundLabel.setText("Round: " + state.getRoundNumber());
        turnLabel.setText("Turn: " + (state.getTurn() == null ? "-" : state.getTurn().name()));
        deckLabel.setText("Deck: " + (state.getDeck() == null ? 0 : state.getDeck().size()));

        int pScore = state.getPlayer().getScore();
        int pEnergy = state.getPlayer().getEnergy();
        int pHand = state.getPlayer().handSize();

        int cScore = state.getComputer().getScore();
        int cEnergy = state.getComputer().getEnergy();
        int cHand = state.getComputer().handSize();

        playerStatsLabel.setText("Player — Score: " + pScore + " | Energy: " + pEnergy + " | Hand: " + pHand);
        computerStatsLabel.setText("Computer — Score: " + cScore + " | Energy: " + cEnergy + " | Hand: " + cHand);

        if (state.getPhase() != Phase.PLAYING && state.getPhase() != Phase.GAME_OVER) {
            lastRoundLabel.setText(" ");
        } else {
            RoundResult rr = (controller == null) ? null : controller.getLastRoundResult();
            if (rr == null) {
                lastRoundLabel.setText(" ");
            } else {
                lastRoundLabel.setText(rr.getMessage() + "  (P+" + rr.getPlayerScoreGained() + ", C+" + rr.getComputerScoreGained() + ")");
            }
        }

        updateHistory(state);

        Card showPlayer = pickDisplayedPlayerCard();
        Card showComputer = pickDisplayedComputerCard();
        playerPlayedView.setCard(showPlayer);
        computerPlayedView.setCard(showComputer);

        rebuildHand(state);

        handlers.sync();

        revalidate();
        repaint();
    }

    private void updateHistory(GameState state) {
        if (state == null || controller == null) return;

        if (state.getPhase() == Phase.MENU) {
            clearHistory();
            return;
        }

        RoundResult rr = controller.getLastRoundResult();

        if (rr == null) {
            if (state.getRoundNumber() <= 1) clearHistory();
            return;
        }

        if (rr.getRoundNumber() != lastLoggedRound) {
            historyModel.add(0, formatHistoryLine(rr));
            lastLoggedRound = rr.getRoundNumber();

            while (historyModel.size() > 50) {
                historyModel.removeElementAt(historyModel.size() - 1);
            }
        }
    }

    private String formatHistoryLine(RoundResult rr) {
        String outcome = rr.getOutcome() == null ? "DRAW" : rr.getOutcome().name();
        return "R" + rr.getRoundNumber()
                + " — " + outcome
                + " | P dmg " + rr.getPlayerDamage()
                + ", C dmg " + rr.getComputerDamage();
    }

    private void clearHistory() {
        historyModel.clear();
        lastLoggedRound = -1;
    }

    private void rebuildHand(GameState state) {
        handPanel.removeAll();

        List<Card> hand = state.getPlayer().getHand();
        if (hand == null || hand.isEmpty()) {
            selectedIndex = -1;
            return;
        }

        if (selectedIndex < 0 || selectedIndex >= hand.size()) selectedIndex = -1;

        for (int i = 0; i < hand.size(); i++) {
            final int idx = i;
            Card c = hand.get(i);

            CardView view = new CardView();
            view.setPreferredCardSize(110, 160);
            view.setCard(c);
            view.setSelected(idx == selectedIndex);
            view.setToolTipText(buildToolTip(c));

            view.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedIndex = idx;
                    refreshSelectionOnly();
                }
            });

            handPanel.add(view);
        }
    }

    private void refreshSelectionOnly() {
        int count = handPanel.getComponentCount();
        for (int i = 0; i < count; i++) {
            if (handPanel.getComponent(i) instanceof CardView) {
                ((CardView) handPanel.getComponent(i)).setSelected(i == selectedIndex);
            }
        }
        handlers.sync();
        repaint();
    }

    private String buildToolTip(Card c) {
        if (c == null) return "";
        return "<html><b>" + safe(c.getName()) + "</b><br/>ATK: " + c.getAttack()
                + " DEF: " + c.getDefense()
                + " COST: " + c.getCost()
                + "</html>";
    }

    private Card pickDisplayedPlayerCard() {
        if (controller == null) return null;

        Card pending = controller.getPendingPlayerCard();
        if (pending != null) return pending;

        RoundResult rr = controller.getLastRoundResult();
        if (rr != null) return rr.getPlayerCard();

        return null;
    }

    private Card pickDisplayedComputerCard() {
        if (controller == null) return null;

        Card pending = controller.getPendingComputerCard();
        if (pending != null) return pending;

        RoundResult rr = controller.getLastRoundResult();
        if (rr != null) return rr.getComputerCard();

        return null;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
