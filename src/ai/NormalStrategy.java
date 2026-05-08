package ai;

import java.util.List;
import java.util.Random;

import engine.GameState;
import model.Card;

//Implements a balanced AI strategy based on card statistics.
public class NormalStrategy implements ComputerStrategy{

    private final Random fallbackRng = new Random();

    @Override
    public Card chooseCard(GameState state, List<Card> hand) {
        if (hand == null || hand.isEmpty()) return null;

        ComputerPlayer computer = (state == null) ? null : state.getComputer();
        Random rng = (state != null && state.getRng() != null) ? state.getRng() : fallbackRng;

        Card best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Card c : hand) {
            if (c == null) continue;
            if (computer != null && !computer.canAfford(c)) continue;

            double s = 0.0;
            s += Math.max(0, c.getAttack()) * 1.00;
            s += Math.max(0, c.getDefense()) * 0.60;
            s -= Math.max(0, c.getCost()) * 0.90;

            if (best == null || s > bestScore) {
                best = c;
                bestScore = s;
            } else if (s == bestScore) {
                if (rng.nextBoolean()) best = c;
            }
        }

        return best;
    }
}
