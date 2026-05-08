package ai;

import java.util.List;
import java.util.Objects;

import engine.GameState;
import model.Card;
import model.Player;

//Represents the computer-controlled player using an AI strategy.
public class ComputerPlayer extends Player {

    private ComputerStrategy strategy;

    public ComputerPlayer(String name, ComputerStrategy strategy) {
        super(name);
        setStrategy(strategy);
    }

    public ComputerStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(ComputerStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "strategy");
    }

    public Card chooseCard(GameState state) {
        List<Card> hand = getHand();
        if (hand == null || hand.isEmpty()) return null;

        Card chosen = strategy.chooseCard(state, hand);

        if (chosen == null || !canAfford(chosen)) {
            for (Card c : hand) if (c != null && canAfford(c)) return c;
            return null;
        }

        int idx = hand.indexOf(chosen);
        if (idx < 0) {
            for (Card c : hand) if (c != null && canAfford(c)) return c;
            return null;
        }

        return chosen;
    }

    public Card playCard(GameState state) {
        List<Card> hand = getHand();
        if (hand == null || hand.isEmpty()) return null;

        Card chosen = chooseCard(state);
        if (chosen == null) return null;

        int idx = hand.indexOf(chosen);
        if (idx < 0) idx = 0;

        return playCardAt(idx);
    }

    @Override
    public Card playCard() {
        return playCard(null);
    }
}
