package ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import engine.GameState;
import model.Card;

//Implements a simple AI strategy that makes random affordable moves.
public class EasyStrategy implements ComputerStrategy {

    private final Random fallbackRng = new Random();

    @Override
    public Card chooseCard(GameState state, List<Card> hand) {
        if (hand == null || hand.isEmpty()) return null;

        Random rng = fallbackRng;
        ComputerPlayer computer = null;

        if (state != null) {
            if (state.getRng() != null) rng = state.getRng();
            computer = state.getComputer();
        }

        if (computer == null) {
            return hand.get(rng.nextInt(hand.size()));
        }

        List<Card> affordable = new ArrayList<>();
        for (Card c : hand) {
            if (c != null && computer.canAfford(c)) affordable.add(c);
        }

        if (affordable.isEmpty()) return null;
        return affordable.get(rng.nextInt(affordable.size()));
    }
}
