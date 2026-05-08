package ai;

//Creates AI strategies based on the selected difficulty level.
public class StrategyFactory {

    public enum Difficulty {
        EASY,
        NORMAL,
        HARD
    }

    public ComputerStrategy create(Difficulty difficulty) {
        if (difficulty == null) return new NormalStrategy();

        switch (difficulty) {
            case EASY:
                return new EasyStrategy();
            case HARD:
                return new HardStrategy();
            case NORMAL:
            default:
                return new NormalStrategy();
        }
    }
}
