package persistence;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// For making changes in getting properties from high score entries
public class HighScoreManager 
{

    private final HighScoreFileStore store;
    private final List<HighScoreEntry> entries = new ArrayList<>();

    public HighScoreManager(HighScoreFileStore store) 
    {
        this.store = (store == null) ? new HighScoreFileStore("highscores.txt") : store;
    }

    public void load() 
    {
        entries.clear();
        entries.addAll(store.load());
        sort();
    }

    public boolean save() 
    {
        sort();
        return store.save(entries);
    }

    public void add(String playerName, int score) 
    {
        entries.add(new HighScoreEntry(playerName, score, LocalDateTime.now()));
        sort();
    }

    public List<HighScoreEntry> getAll() 
    {
        return Collections.unmodifiableList(entries);
    }

    public List<HighScoreEntry> getTop(int n) 
    {
        sort();
        int k = Math.max(0, Math.min(n, entries.size()));
        return Collections.unmodifiableList(new ArrayList<>(entries.subList(0, k)));
    }

    private void sort() 
    {
        entries.sort(Comparator
                .comparingInt(HighScoreEntry::getScore).reversed()
                .thenComparing(HighScoreEntry::getDateTime, Comparator.reverseOrder()));
    }
}
