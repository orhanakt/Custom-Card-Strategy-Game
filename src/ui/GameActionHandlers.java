package ui;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import engine.GameState;
import model.Card;
import model.Phase;
import model.Player;
import model.Turn;

//Centralizes button actions and synchronizes UI controls with game state.
public class GameActionHandlers {

    private final JComponent root;
    private final JButton playBtn;
    private final JButton drawBtn;
    private final JButton endTurnBtn;
    private final IntSupplier selectedIndexSupplier;

    private Supplier<GameState> stateSupplier;
    private IntConsumer onPlayIndex;
    private Runnable onDraw;
    private Runnable onEndTurn;

    private boolean listenersInstalled;

    private int maxHandSize = 7;

    public GameActionHandlers(JComponent root, JButton playBtn, JButton drawBtn, JButton endTurnBtn, IntSupplier selectedIndexSupplier) {
        this.root = root;
        this.playBtn = playBtn;
        this.drawBtn = drawBtn;
        this.endTurnBtn = endTurnBtn;
        this.selectedIndexSupplier = selectedIndexSupplier;

        installKeyBindings();
        installButtonListenersOnce();
    }

    public void setMaxHandSize(int maxHandSize) {
        this.maxHandSize = Math.max(1, maxHandSize);
        sync();
    }

    public void bind(Supplier<GameState> stateSupplier, IntConsumer onPlayIndex, Runnable onDraw, Runnable onEndTurn) {
        this.stateSupplier = stateSupplier;
        this.onPlayIndex = onPlayIndex;
        this.onDraw = onDraw;
        this.onEndTurn = onEndTurn;
        sync();
    }

    public void sync() {
        GameState state = (stateSupplier == null) ? null : stateSupplier.get();
        applyEnabledState(state);
    }

    public void applyEnabledState(GameState state) {
        boolean playing = state != null && state.getPhase() == Phase.PLAYING;

        if (!playing) {
            if (playBtn != null) playBtn.setEnabled(false);
            if (drawBtn != null) drawBtn.setEnabled(false);
            if (endTurnBtn != null) endTurnBtn.setEnabled(false);
            return;
        }

        boolean playerTurn = state.getTurn() == Turn.PLAYER;

        Player player = state.getPlayer();
        List<Card> hand = (player == null) ? null : player.getHand();

        int idx = (selectedIndexSupplier == null) ? -1 : selectedIndexSupplier.getAsInt();
        Card selected = (hand != null && idx >= 0 && idx < hand.size()) ? hand.get(idx) : null;

        boolean canPlay = playerTurn && selected != null && player != null && player.canAfford(selected);

        boolean deckHasCards = state.getDeck() != null && !state.getDeck().isEmpty();
        boolean handNotFull = player != null && player.handSize() < maxHandSize;
        boolean canDraw = playerTurn && deckHasCards && handNotFull;

        boolean canEndTurn = playerTurn;

        if (playBtn != null) playBtn.setEnabled(canPlay);
        if (drawBtn != null) drawBtn.setEnabled(canDraw);
        if (endTurnBtn != null) endTurnBtn.setEnabled(canEndTurn);
    }

    private void handlePlay(ActionEvent e) {
        GameState state = (stateSupplier == null) ? null : stateSupplier.get();
        if (state == null || state.getPhase() != Phase.PLAYING || state.getTurn() != Turn.PLAYER) {
            sync();
            return;
        }

        Player player = state.getPlayer();
        List<Card> hand = (player == null) ? null : player.getHand();
        int idx = (selectedIndexSupplier == null) ? -1 : selectedIndexSupplier.getAsInt();

        if (hand == null || idx < 0 || idx >= hand.size()) {
            sync();
            return;
        }

        Card c = hand.get(idx);
        if (c == null || !player.canAfford(c)) {
            sync();
            return;
        }

        if (onPlayIndex != null) onPlayIndex.accept(idx);
        sync();
    }

    private void handleDraw(ActionEvent e) {
        GameState state = (stateSupplier == null) ? null : stateSupplier.get();
        if (state == null || state.getPhase() != Phase.PLAYING || state.getTurn() != Turn.PLAYER) {
            sync();
            return;
        }
        if (state.getDeck() == null || state.getDeck().isEmpty()) {
            sync();
            return;
        }

        if (onDraw != null) onDraw.run();
        sync();
    }

    private void handleEndTurn(ActionEvent e) {
        GameState state = (stateSupplier == null) ? null : stateSupplier.get();
        if (state == null || state.getPhase() != Phase.PLAYING || state.getTurn() != Turn.PLAYER) {
            sync();
            return;
        }

        if (onEndTurn != null) onEndTurn.run();
        sync();
    }

    private void installButtonListenersOnce() {
        if (listenersInstalled) return;
        listenersInstalled = true;

        if (playBtn != null) playBtn.addActionListener(this::handlePlay);
        if (drawBtn != null) drawBtn.addActionListener(this::handleDraw);
        if (endTurnBtn != null) endTurnBtn.addActionListener(this::handleEndTurn);
    }

    private void installKeyBindings() {
        if (root == null) return;

        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "play");
        root.getActionMap().put("play", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (playBtn != null && playBtn.isEnabled()) playBtn.doClick();
            }
        });

        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('D'), "draw");
        root.getActionMap().put("draw", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (drawBtn != null && drawBtn.isEnabled()) drawBtn.doClick();
            }
        });

        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SPACE"), "endTurn");
        root.getActionMap().put("endTurn", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (endTurnBtn != null && endTurnBtn.isEnabled()) endTurnBtn.doClick();
            }
        });
    }
}
