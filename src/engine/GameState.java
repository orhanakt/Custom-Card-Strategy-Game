package engine;

import java.util.Objects;
import java.util.Random;

import ai.ComputerPlayer;
import model.Deck;
import model.Phase;
import model.Player;
import model.Turn;

//Stores the current state of the game including players, deck, and turn.
public class GameState {

    private final Player player;
    private final ComputerPlayer computer;
    private final Deck deck;
    private final Random rng;

    private int roundNumber;
    private Turn turn;
    private Phase phase;

    private boolean skipPlayerNextTurn;
    private boolean skipComputerNextTurn;

    public GameState(Player player, ComputerPlayer computer, Deck deck) {
        this(player, computer, deck, new Random());
    }

    public GameState(Player player, ComputerPlayer computer, Deck deck, Random rng) {
        this.player = Objects.requireNonNull(player, "player");
        this.computer = Objects.requireNonNull(computer, "computer");
        this.deck = Objects.requireNonNull(deck, "deck");
        this.rng = (rng == null) ? new Random() : rng;

        this.roundNumber = 1;
        this.turn = Turn.PLAYER;
        this.phase = Phase.MENU;

        this.skipPlayerNextTurn = false;
        this.skipComputerNextTurn = false;
    }

    public Player getPlayer() {
        return player;
    }

    public ComputerPlayer getComputer() {
        return computer;
    }

    public Deck getDeck() {
        return deck;
    }

    public Random getRng() {
        return rng;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = Math.max(1, roundNumber);
    }

    public void nextRound() {
        this.roundNumber++;
        if (this.roundNumber < 1) this.roundNumber = 1;
    }

    public Turn getTurn() {
        return turn;
    }

    public void setTurn(Turn turn) {
        this.turn = (turn == null) ? Turn.PLAYER : turn;
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = (phase == null) ? Phase.MENU : phase;
    }

    public boolean isSkipPlayerNextTurn() {
        return skipPlayerNextTurn;
    }

    public void setSkipPlayerNextTurn(boolean skipPlayerNextTurn) {
        this.skipPlayerNextTurn = skipPlayerNextTurn;
    }

    public boolean isSkipComputerNextTurn() {
        return skipComputerNextTurn;
    }

    public void setSkipComputerNextTurn(boolean skipComputerNextTurn) {
        this.skipComputerNextTurn = skipComputerNextTurn;
    }

    public Turn advanceTurn() {
        Turn next = (turn == Turn.PLAYER) ? Turn.COMPUTER : Turn.PLAYER;

        if (next == Turn.PLAYER && skipPlayerNextTurn) {
            skipPlayerNextTurn = false;
            next = Turn.COMPUTER;
        } else if (next == Turn.COMPUTER && skipComputerNextTurn) {
            skipComputerNextTurn = false;
            next = Turn.PLAYER;
        }

        turn = next;
        return turn;
    }
}
