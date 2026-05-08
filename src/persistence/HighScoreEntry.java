package persistence;

import java.time.LocalDateTime;
import java.util.Objects;

// Representation of entries for high scores
public class HighScoreEntry 
{
    private final String playerName;
    private final int score;
    private final LocalDateTime dateTime;

    public HighScoreEntry(String playerName, int score, LocalDateTime dateTime) 
    {
        this.playerName = sanitize(Objects.requireNonNull(playerName, "playerName"));
        this.score = Math.max(0, score);
        this.dateTime = (dateTime == null) ? LocalDateTime.now() : dateTime;
    }

    public String getPlayerName() 
    {
        return playerName;
    }

    public int getScore() 
    {
        return score;
    }

    public LocalDateTime getDateTime() 
    {
        return dateTime;
    }

    private static String sanitize(String s) 
    {
        String t = s.trim();
        if (t.isEmpty()) t = "Player";
        t = t.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ');
        return t;
    }
}
