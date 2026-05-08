package engine;

import model.Card;
import ai.ComputerPlayer;
import model.GameResult;
import model.Player;
import model.RoundResult;

//Resolves a round by comparing played cards and calculating results.
public class RoundResolver {

    private final GameRules rules;

    public RoundResolver() {
        this(new GameRules());
    }

    public RoundResolver(GameRules rules) {
        this.rules = (rules == null) ? new GameRules() : rules;
    }

    public RoundResult resolve(GameState state, Card playerCard, Card computerCard) {
        if (state == null) return null;

        Player player = state.getPlayer();
        ComputerPlayer computer = state.getComputer();

        Card pCard = playerCard;
        Card cCard = computerCard;

        if (pCard != null && !player.canAfford(pCard)) pCard = null;
        if (cCard != null && !computer.canAfford(cCard)) cCard = null;

        if (pCard != null) player.spendEnergy(pCard.getCost());
        if (cCard != null) computer.spendEnergy(cCard.getCost());
        
        int pEffDef = rules.effectiveDefense(pCard);
        int cEffDef = rules.effectiveDefense(cCard);

        int pDamage = rules.damage(pCard, cEffDef);
        int cDamage = rules.damage(cCard, pEffDef);

        GameResult outcome;
        if (pCard == null && cCard == null) outcome = GameResult.DRAW;
        else if (pCard == null) outcome = GameResult.LOSE;
        else if (cCard == null) outcome = GameResult.WIN;
        else if (pDamage > cDamage) outcome = GameResult.WIN;
        else if (pDamage < cDamage) outcome = GameResult.LOSE;
        else outcome = GameResult.DRAW;

        int pScoreGained = 0;
        int cScoreGained = 0;

        if (outcome == GameResult.WIN) {
            pScoreGained = rules.applyMultiplier(rules.basePointsFromDamage(pDamage));
        } else if (outcome == GameResult.LOSE) {
            cScoreGained = rules.applyMultiplier(rules.basePointsFromDamage(cDamage));
        }

        if (pScoreGained > 0) player.addScore(pScoreGained);
        if (cScoreGained > 0) computer.addScore(cScoreGained);
        String msg;
        if (outcome == GameResult.WIN) msg = "Player wins the round.";
        else if (outcome == GameResult.LOSE) msg = "Computer wins the round.";
        else msg = "Round is a draw.";
        RoundResult rr = new RoundResult(
                state.getRoundNumber(),
                pCard,
                cCard,
                pDamage,
                cDamage,
                outcome,
                pScoreGained,
                cScoreGained,
                msg
        );

        state.nextRound();

        return rr;
    }
}
