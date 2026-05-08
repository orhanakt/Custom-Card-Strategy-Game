package persistence;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// For saving and loading game save file
public class GameSaveFileStore 
{
    private final String path;

    public GameSaveFileStore(String path) 
    {
        String p = (path == null || path.trim().isEmpty()) ? "savegame.txt" : path.trim();
        this.path = p;
    }

    public String getPath() 
    {
        return path;
    }

    public boolean save(String text) 
    {
        try 
        {
            Path file = Paths.get(path);
            Path parent = file.getParent();
            if (parent != null) Files.createDirectories(parent);
            Files.write(file, (text == null ? "" : text).getBytes(StandardCharsets.UTF_8));
            return true;
        } 
        catch (Exception ex) 
        {
            System.err.println("GameSaveFileStore.save failed: " + path);
            ex.printStackTrace();
            return false;
        }
    }

    public String load() 
    {
        try 
        {
            Path file = Paths.get(path);
            if (!Files.exists(file)) return "";
            return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        } 
        catch (Exception ex) 
        {
            System.err.println("GameSaveFileStore.load failed: " + path);
            ex.printStackTrace();
            return "";
        }
    }
}
