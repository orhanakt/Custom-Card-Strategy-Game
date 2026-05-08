package persistence;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// For saving and loading high score file
public class HighScoreFileStore 
{
    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final Path filePath;

    public HighScoreFileStore(String filePath) 
    {
        String p = (filePath == null || filePath.trim().isEmpty())
                ? "highscores.txt"
                : filePath.trim();
        this.filePath = Paths.get(p);
    }

    public Path getFilePath() 
    {
        return filePath;
    }

    public List<HighScoreEntry> load() 
    {
        List<HighScoreEntry> list = new ArrayList<>();

        try {
            if (!Files.exists(filePath)) return list;

            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            for (String line : lines) 
            {
                HighScoreEntry e = parseLine(line);
                if (e != null) list.add(e);
            }
        } 
        catch (Exception ex) {
            System.err.println("HighScoreFileStore.load failed: " + filePath);
            ex.printStackTrace();
        }

        return list;
    }

    public boolean save(List<HighScoreEntry> entries) 
    {
        try 
        {
            Path parent = filePath.getParent();
            if (parent != null) Files.createDirectories(parent);

            List<String> lines = new ArrayList<>();
            if (entries != null) 
            {
                for (HighScoreEntry e : entries) 
                {
                    if (e != null) lines.add(formatLine(e));
                }
            }

            Files.write(filePath, lines, StandardCharsets.UTF_8);
            return true;
        } 
        catch (Exception ex) 
        {
            System.err.println("HighScoreFileStore.save failed: " + filePath);
            ex.printStackTrace();
            return false;
        }
    }

    private String formatLine(HighScoreEntry e) {
        if (e == null) return "";
        String name = (e.getPlayerName() == null) ? "Player" : e.getPlayerName();
        return name + "\t" + e.getScore() + "\t" + FMT.format(e.getDateTime());
    }

    private HighScoreEntry parseLine(String line) 
    {
        if (line == null) return null;
        String t = line.trim();
        if (t.isEmpty()) return null;
        String[] parts = t.split("\t");
        if (parts.length < 2) return null;
        String name = parts[0].trim();
        int score;
        try 
        {
            score = Integer.parseInt(parts[1].trim());
        } 
        catch (Exception ex) 
        {
            System.err.println("Invalid high score line (score): " + line);
            return null;
        }

        LocalDateTime dt = LocalDateTime.now();
        if (parts.length >= 3) {
            try 
            {
                dt = LocalDateTime.parse(parts[2].trim(), FMT);
            } 
            catch (Exception ex) 
            {
                System.err.println("Invalid high score line (datetime): " + line);
            }
        }

        return new HighScoreEntry(name, score, dt);
    }
}
