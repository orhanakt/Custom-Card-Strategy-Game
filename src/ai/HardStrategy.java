package ai;

import java.util.List;
import java.util.Random;

import engine.GameState;
import model.Card;
import model.Player;

//Implements an advanced AI strategy that adapts to the score difference.
public class HardStrategy implements ComputerStrategy{

    private final Random fallbackRng = new Random();

    @Override
    public Card chooseCard(GameState state, List<Card> hand) {
        if (hand == null || hand.isEmpty()) return null;

        ComputerPlayer computer = (state == null) ? null : state.getComputer();
        Player player = (state == null) ? null : state.getPlayer();
        Random rng = (state != null && state.getRng() != null) ? state.getRng() : fallbackRng;

        int cScore = (computer == null) ? 0 : computer.getScore();
        int pScore = (player == null) ? 0 : player.getScore();
        int scoreDiff = cScore - pScore;

        Card best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Card c : hand) {
            if (c == null) continue;
            if (computer != null && !computer.canAfford(c)) continue;

            double s = 0.0;

            s += Math.max(0, c.getAttack()) * 1.05;
            s += Math.max(0, c.getDefense()) * 0.60;
            s -= Math.max(0, c.getCost()) * 0.95;

            if (scoreDiff < 0) {
                s += Math.max(0, c.getAttack()) * 0.35;
            } 
            else if (scoreDiff > 0) {
                s += Math.max(0, c.getDefense()) * 0.25;
            }

            if (best == null || s > bestScore) {
                best = c;
                bestScore = s;
            } 
            else if (s == bestScore) {
                int cc = c.getCost();
                int bc = best.getCost();
                if (cc < bc) best = c;
                else if (cc == bc && rng.nextBoolean()) best = c;
            }
        }

        return best;
    }
}
