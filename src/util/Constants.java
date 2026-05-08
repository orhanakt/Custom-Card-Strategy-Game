package util;

// Constants for default settings, names, paths
public final class Constants 
{

    public static final String APP_TITLE = "Custom Card Strategy Game";
    public static final String HIGHSCORE_FILE = "highscores.txt";
    public static final String SAVEGAME_FILE = "savegame.txt";
    public static final String CARD_IMAGES_DIR = "/images/cards/";
    
    public static final int MIN_DECK_SIZE = 20;
    public static final int DEFAULT_INITIAL_HAND_SIZE = 5;
    public static final int DEFAULT_MAX_HAND_SIZE = 7;
    public static final int DEFAULT_INITIAL_ENERGY = 5;
    public static final int DEFAULT_ENERGY_GAIN_PER_ROUND = 1;
    public static final int DEFAULT_SCORE_LIMIT = 50;
    public static final int DEFAULT_MAX_ROUNDS = 20;
    public static final int DEFAULT_TURN_DELAY_MS = 600;
    public static final int DEFAULT_DIFFICULTY_INDEX = 1;

    private Constants() 
    {
    	
    }
}
