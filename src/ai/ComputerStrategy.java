package ai;

import java.util.List;

import engine.GameState;
import model.Card;

//Defines the interface for computer decision-making strategies.
@FunctionalInterface
public interface ComputerStrategy {

    Card chooseCard(GameState state, List<Card> hand);

    default String getName() {
        return getClass().getSimpleName();
    }

    default int chooseCardIndex(GameState state, List<Card> hand) {
        if (hand == null || hand.isEmpty()) return -1;
        Card c = chooseCard(state, hand);
        if (c == null) return -1;
        return hand.indexOf(c);
    }
}
