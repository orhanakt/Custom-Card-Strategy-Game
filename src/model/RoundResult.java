package model;

//Stores the result of a completed round including cards played and scores gained.
public class RoundResult {

    private final int roundNumber;

    private final Card playerCard;
    private final Card computerCard;

    private final int playerDamage;
    private final int computerDamage;

    private final GameResult outcome;

    private final int playerScoreGained;
    private final int computerScoreGained;
    
    private final String message;

    public RoundResult(int roundNumber,
                       Card playerCard,
                       Card computerCard,
                       int playerDamage,
                       int computerDamage,
                       GameResult outcome,
                       int playerScoreGained,
                       int computerScoreGained,
                       String message) {

        this.roundNumber = roundNumber;
        this.playerCard = playerCard;
        this.computerCard = computerCard;
        this.playerDamage = Math.max(0, playerDamage);
        this.computerDamage = Math.max(0, computerDamage);
        this.outcome = (outcome == null) ? GameResult.DRAW : outcome;
        this.playerScoreGained = Math.max(0, playerScoreGained);
        this.computerScoreGained = Math.max(0, computerScoreGained);
        this.message = (message == null) ? "" : message;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public Card getPlayerCard() {
        return playerCard;
    }

    public Card getComputerCard() {
        return computerCard;
    }

    public int getPlayerDamage() {
        return playerDamage;
    }

    public int getComputerDamage() {
        return computerDamage;
    }

    public GameResult getOutcome() {
        return outcome;
    }

    public int getPlayerScoreGained() {
        return playerScoreGained;
    }

    public int getComputerScoreGained() {
        return computerScoreGained;
    }

    public String getMessage() {
        return message;
    }
}
