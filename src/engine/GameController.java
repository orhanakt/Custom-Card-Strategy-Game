package engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.Timer;

import ai.ComputerStrategy;
import ai.StrategyFactory;
import model.Card;
import model.CardType;
import ai.ComputerPlayer;
import model.Deck;
import model.Phase;
import model.Player;
import model.RoundResult;
import model.Turn;
import persistence.GameSaveFileStore;
import persistence.HighScoreFileStore;
import persistence.HighScoreManager;
import util.Constants;

//Controls the overall game flow, turn handling, and player actions.
public class GameController {

    private final Random rng = new Random();
    
    private int computerTurnDelayMs = Constants.DEFAULT_TURN_DELAY_MS;
    private Timer aiTimer;
    private boolean aiMoveScheduled;

    private GameState state;
    private final RoundResolver roundResolver = new RoundResolver(new GameRules());

    private HighScoreManager highScoreManager;

    private int initialHandSize = 5;
    private int maxHandSize = 7;

    private int initialEnergy = 5;
    private int energyGainPerRound = 1;

    private int scoreLimit = 50;
    private int maxRounds = 20;

    private StrategyFactory.Difficulty difficulty = StrategyFactory.Difficulty.NORMAL;

    private Card pendingPlayerCard;
    private Card pendingComputerCard;

    private RoundResult lastRoundResult;

    private transient Runnable onStateChanged;

    private final GameSerializer serializer = new GameSerializer();
    private final GameSaveFileStore saveStore = new GameSaveFileStore(Constants.SAVEGAME_FILE);

    
    public GameController() {
        this.highScoreManager = new HighScoreManager(new HighScoreFileStore(Constants.HIGHSCORE_FILE));
        this.highScoreManager.load();
        initEmptyState();
    }

    public void setComputerTurnDelayMs(int ms) {
        this.computerTurnDelayMs = Math.max(0, ms);
    }
    
    public boolean hasSavedGame() {
        String t = saveStore.load();
        return t != null && !t.trim().isEmpty();
    }

    public boolean saveGame() {
        if (state == null) return false;
        String text = serializer.serialize(state);
        return saveStore.save(text);
    }

    public boolean loadGame() {
    	cancelAiMove();
        String text = saveStore.load();
        if (text == null || text.trim().isEmpty()) return false;

        GameState loaded = serializer.deserialize(text);
        if (loaded == null) return false;
        if (loaded.getComputer() != null) {
            loaded.getComputer().setStrategy(createStrategyForCurrentDifficulty());
        }

        state = loaded;
        pendingPlayerCard = null;
        pendingComputerCard = null;
        lastRoundResult = null;

        fireStateChanged();
        requestComputerMoveIfNeeded();
        return true;
    }

    public boolean clearSavedGame() {
        return saveStore.save("");
    }

    
    public void setOnStateChanged(Runnable onStateChanged) {
        this.onStateChanged = onStateChanged;
    }

    public GameState getState() {
        return state;
    }

    public HighScoreManager getHighScoreManager() {
        return highScoreManager;
    }

    public void setHighScoreManager(HighScoreManager highScoreManager) {
        if (highScoreManager != null) {
            this.highScoreManager = highScoreManager;
            this.highScoreManager.load();
        }
    }

    public RoundResult getLastRoundResult() {
        return lastRoundResult;
    }

    public Card getPendingPlayerCard() {
        return pendingPlayerCard;
    }

    public Card getPendingComputerCard() {
        return pendingComputerCard;
    }

    public void setScoreLimit(int scoreLimit) {
        this.scoreLimit = Math.max(1, scoreLimit);
    }

    public void setMaxRounds(int maxRounds) {
        this.maxRounds = Math.max(1, maxRounds);
    }

    public void setInitialHandSize(int initialHandSize) {
        this.initialHandSize = Math.max(1, initialHandSize);
    }

    public void setMaxHandSize(int maxHandSize) {
        this.maxHandSize = Math.max(1, maxHandSize);
    }

    public void setInitialEnergy(int initialEnergy) {
        this.initialEnergy = Math.max(0, initialEnergy);
    }

    public void setEnergyGainPerRound(int energyGainPerRound) {
        this.energyGainPerRound = Math.max(0, energyGainPerRound);
    }

    public StrategyFactory.Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(StrategyFactory.Difficulty difficulty) {
        this.difficulty = (difficulty == null) ? StrategyFactory.Difficulty.NORMAL : difficulty;

        if (state != null && state.getComputer() != null) {
            state.getComputer().setStrategy(createStrategyForCurrentDifficulty());
        }

        fireStateChanged();
    }

    public void startNewGame(String playerName) {
    	cancelAiMove();
        startNewGame(playerName, createStrategyForCurrentDifficulty(), true);
    }

    public void startNewGame(String playerName, ComputerStrategy strategy) {
        startNewGame(playerName, strategy, true);
    }

    public void startNewGame(String playerName, ComputerStrategy strategy, boolean randomFirstTurn) {
        if (strategy == null) throw new IllegalArgumentException("strategy cannot be null");

        Player player = new Player(playerName == null ? "Player" : playerName.trim());
        ComputerPlayer computer = new ComputerPlayer("Computer", strategy);

        Deck deck = new Deck(rng);
        deck.reset(createDefaultDeck());

        state = new GameState(player, computer, deck, rng);
        state.setPhase(Phase.PLAYING);

        player.setScore(0);
        computer.setScore(0);
        player.setEnergy(initialEnergy);
        computer.setEnergy(initialEnergy);

        player.clearHand();
        computer.clearHand();
        dealInitialHands();

        pendingPlayerCard = null;
        pendingComputerCard = null;
        lastRoundResult = null;

        state.setRoundNumber(1);
        state.setTurn(Turn.PLAYER);     
        cancelAiMove();

        pendingPlayerCard = null;
        pendingComputerCard = null;
        lastRoundResult = null;

        fireStateChanged();   
    }

    public void restartGame() {
    	cancelAiMove();
        if (state == null) {
            initEmptyState();
            return;
        }
        startNewGame(state.getPlayer().getName());
    }

    public void returnToMenu() {
    	cancelAiMove();
        if (state != null) {
            state.setPhase(Phase.MENU);
            pendingPlayerCard = null;
            pendingComputerCard = null;
            lastRoundResult = null;
            fireStateChanged();
        }
    }

    public boolean playerDraw() {
        if (!isPlayerInputAllowed()) return false;

        if (state.getDeck() == null || state.getDeck().isEmpty()) return false;
        if (state.getPlayer().handSize() >= maxHandSize) return false;

        state.getPlayer().drawFrom(state.getDeck());
        fireStateChanged();
        return true;
    }

    public RoundResult playerPlayCardAt(int index) {
        if (!isPlayerInputAllowed()) return null;

        Player player = state.getPlayer();
        List<Card> hand = player.getHand();
        if (hand == null || hand.isEmpty()) return null;
        if (index < 0 || index >= hand.size()) return null;

        Card chosen = hand.get(index);
        if (chosen == null) return null;
        if (!player.canAfford(chosen)) return null;

        Card played = player.playCardAt(index);
        if (played == null) return null;
        if (pendingComputerCard != null) {
            RoundResult rr = resolveRound(played, pendingComputerCard);
            pendingComputerCard = null;

            postRoundUpkeep();
            if (state.getPhase() == Phase.PLAYING) {
                state.setTurn(Turn.PLAYER);
            }

            fireStateChanged();
            requestComputerMoveIfNeeded();
            return rr;
        }
        scheduleSimultaneousRoundResolve(played);
        return null;
    }



    public RoundResult endTurn() {
        if (state == null || state.getPhase() != Phase.PLAYING) return null;
        if (state.getTurn() != Turn.PLAYER) return null;
        if (pendingComputerCard != null) {
            RoundResult rr = resolveRound(null, pendingComputerCard);
            pendingComputerCard = null;

            postRoundUpkeep();
            if (state.getPhase() == Phase.PLAYING) {
                state.setTurn(Turn.PLAYER);
            }

            fireStateChanged();
            requestComputerMoveIfNeeded();
            return rr;
        }
        scheduleSimultaneousRoundResolve(null);
        return null;
    }

    public void notifyStateChanged() {
        fireStateChanged();
    }
    
    private void cancelAiMove() {
        aiMoveScheduled = false;
        if (aiTimer != null) {
            aiTimer.stop();
            aiTimer = null;
        }
    }

    private void requestComputerSecondMoveIfNeeded() {
        if (state == null) return;
        if (state.getPhase() != Phase.PLAYING) return;

        if (state.getTurn() != Turn.COMPUTER) {
            cancelAiMove();
            return;
        }
        if (pendingPlayerCard == null || pendingComputerCard != null) return;

        if (aiMoveScheduled) return;
        aiMoveScheduled = true;

        aiTimer = new Timer(Math.max(0, computerTurnDelayMs), e -> {
            aiMoveScheduled = false;
            aiTimer = null;

            if (state == null) return;
            if (state.getPhase() != Phase.PLAYING) return;
            if (state.getTurn() != Turn.COMPUTER) return;
            if (pendingPlayerCard == null || pendingComputerCard != null) return;

            Card computerCard = playComputerCard();
            pendingComputerCard = computerCard;

            resolveRound(pendingPlayerCard, pendingComputerCard);
            pendingPlayerCard = null;
            pendingComputerCard = null;

            postRoundUpkeep();
            requestComputerMoveIfNeeded(); 
            fireStateChanged();
        });

        aiTimer.setRepeats(false);
        aiTimer.start();
    }
    private void requestComputerMoveIfNeeded() {
        if (state == null) return;
        if (state.getPhase() != Phase.PLAYING) return;
        if (state.getTurn() != Turn.COMPUTER) {
            cancelAiMove();
            return;
        }
        if (pendingPlayerCard != null || pendingComputerCard != null) return;
        if (aiMoveScheduled) return;

        aiMoveScheduled = true;

        aiTimer = new Timer(Math.max(0, computerTurnDelayMs), e -> {
            aiMoveScheduled = false;
            aiTimer = null;
            if (state == null) return;
            if (state.getPhase() != Phase.PLAYING) return;
            if (state.getTurn() != Turn.COMPUTER) return;
            if (pendingPlayerCard != null || pendingComputerCard != null) return;
            Card c = playComputerCard();

            if (c == null) 
            {
                resolveRound(null, null);
                postRoundUpkeep();
                pendingPlayerCard = null;
                pendingComputerCard = null;
                state.setTurn(Turn.PLAYER);
            } 
            else 
            {
                pendingComputerCard = c;
                state.setTurn(Turn.PLAYER);
            }

            fireStateChanged();
        });

        aiTimer.setRepeats(false);
        aiTimer.start();
    }


    private boolean isPlayerInputAllowed() 
    {
        if (state == null) return false;
        if (state.getPhase() != Phase.PLAYING) return false;
        if (state.getTurn() != Turn.PLAYER) return false;
        return true;
    }


    private Card playComputerCard() {
        ComputerPlayer computer = state.getComputer();
        if (computer == null) return null;

        if (computer.handSize() == 0 && state.getDeck() != null && !state.getDeck().isEmpty() && computer.handSize() < maxHandSize) {
            computer.drawFrom(state.getDeck());
        }

        Card chosen = computer.chooseCard(state);
        if (chosen == null) return null;

        if (!computer.canAfford(chosen)) {
            int idx = findAnyAffordableIndex(computer.getHand(), computer);
            if (idx < 0) return null;
            return computer.playCardAt(idx);
        }

        int idx = computer.getHand().indexOf(chosen);
        if (idx < 0) idx = 0;
        return computer.playCardAt(idx);
    }

    private int findAnyAffordableIndex(List<Card> hand, Player p) {
        if (hand == null || hand.isEmpty() || p == null) return -1;
        for (int i = 0; i < hand.size(); i++) {
            Card c = hand.get(i);
            if (c != null && p.canAfford(c)) return i;
        }
        return -1;
    }

    private RoundResult resolveRound(Card playerCard, Card computerCard) {
        lastRoundResult = roundResolver.resolve(state, playerCard, computerCard);
        checkGameOverAndPersistIfNeeded();
        return lastRoundResult;
    }

    private void scheduleSimultaneousRoundResolve(Card playerCardOrNull) {
        if (state == null) return;
        if (state.getPhase() != Phase.PLAYING) return;

      
        if (aiMoveScheduled) return;

        cancelAiMove();
        aiMoveScheduled = true;

       
        state.setTurn(Turn.COMPUTER);

        
        lastRoundResult = null;
        pendingPlayerCard = null;
        pendingComputerCard = null;

        fireStateChanged();

        final int roundNo = state.getRoundNumber();

        aiTimer = new Timer(Math.max(0, computerTurnDelayMs), e -> {
            aiMoveScheduled = false;
            aiTimer = null;
            if (state == null) return;
            if (state.getPhase() != Phase.PLAYING) return;
            
            Card computerCard = playComputerCard();
          
            RoundResult rr = resolveRound(playerCardOrNull, computerCard);

            postRoundUpkeep();
            if (state.getPhase() == Phase.PLAYING) {
                state.setTurn(Turn.PLAYER);
            }

            fireStateChanged();
        });

        aiTimer.setRepeats(false);
        aiTimer.start();
    }

    
    private void postRoundUpkeep() {
        if (state == null) return;
        if (state.getPhase() != Phase.PLAYING) return;

        state.getPlayer().addEnergy(energyGainPerRound);
        state.getComputer().addEnergy(energyGainPerRound);


    }

    private void checkGameOverAndPersistIfNeeded() {
        if (state == null) return;

        boolean scoreReached = state.getPlayer().getScore() >= scoreLimit || state.getComputer().getScore() >= scoreLimit;
        boolean roundsReached = state.getRoundNumber() > maxRounds;

        boolean deckEmpty = state.getDeck() == null || state.getDeck().isEmpty();
        boolean handsEmpty = state.getPlayer().handSize() == 0 && state.getComputer().handSize() == 0;

        if (!(scoreReached || roundsReached || (deckEmpty && handsEmpty))) return;

        state.setPhase(Phase.GAME_OVER);

        if (highScoreManager != null) {
            highScoreManager.add(state.getPlayer().getName(), state.getPlayer().getScore());
            highScoreManager.save();
        }
    }

    private void dealInitialHands() {
        for (int i = 0; i < initialHandSize; i++) {
            if (state.getDeck().isEmpty()) break;
            state.getPlayer().drawFrom(state.getDeck());
            if (state.getDeck().isEmpty()) break;
            state.getComputer().drawFrom(state.getDeck());
        }
    }

    private void initEmptyState() {
        Player p = new Player("Player");
        ComputerPlayer c = new ComputerPlayer("Computer", createStrategyForCurrentDifficulty());
        Deck d = new Deck(rng);
        d.reset(createDefaultDeck());
        state = new GameState(p, c, d, rng);
        state.setPhase(Phase.MENU);
        state.setTurn(Turn.PLAYER);
        pendingPlayerCard = null;
        pendingComputerCard = null;
        lastRoundResult = null;
    }

    private ComputerStrategy createStrategyForCurrentDifficulty() {
        StrategyFactory f = new StrategyFactory();
        StrategyFactory.Difficulty d = (difficulty == null) ? StrategyFactory.Difficulty.NORMAL : difficulty;
        return f.create(d);
    }

    private void fireStateChanged() {
        if (onStateChanged != null) onStateChanged.run();
    }

    private List<Card> createDefaultDeck() {
        List<Card> list = new ArrayList<>();

        String cid, img;

        cid = "C001"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Ember", CardType.FIRE, 3, 2, 1, img));

        cid = "C002"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Tide", CardType.WATER, 4, 4, 2, img));

        cid = "C003"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Stone", CardType.EARTH, 5, 6, 3, img));

        cid = "C004"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Gale", CardType.AIR, 6, 2, 4, img));

        cid = "C005"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Halo", CardType.LIGHT, 7, 4, 1, img));

        cid = "C006"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Shade", CardType.DARK, 8, 6, 2, img));

        cid = "C007"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Cinder", CardType.FIRE, 3, 2, 3, img));

        cid = "C008"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Ripple", CardType.WATER, 4, 4, 4, img));

        cid = "C009"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Golem", CardType.EARTH, 5, 6, 1, img));

        cid = "C010"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Zephyr", CardType.AIR, 6, 2, 2, img));

        cid = "C011"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Dawn", CardType.LIGHT, 7, 4, 3, img));

        cid = "C012"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Void", CardType.DARK, 8, 6, 4, img));

        cid = "C013"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Blaze", CardType.FIRE, 3, 2, 1, img));

        cid = "C014"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Torrent", CardType.WATER, 4, 4, 2, img));

        cid = "C015"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Root", CardType.EARTH, 5, 6, 3, img));

        cid = "C016"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Nimbus", CardType.AIR, 6, 2, 4, img));

        cid = "C017"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Seraph", CardType.LIGHT, 7, 4, 1, img));

        cid = "C018"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Wraith", CardType.DARK, 8, 6, 2, img));

        cid = "C019"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Pyre", CardType.FIRE, 3, 2, 3, img));

        cid = "C020"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Naiad", CardType.WATER, 4, 4, 4, img));

        cid = "C021"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Titan", CardType.EARTH, 5, 6, 1, img));

        cid = "C022"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Wisp", CardType.AIR, 6, 2, 2, img));

        cid = "C023"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Beacon", CardType.LIGHT, 7, 4, 3, img));

        cid = "C024"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Eclipse", CardType.DARK, 8, 6, 4, img));

        cid = "C025"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Inferno", CardType.FIRE, 3, 2, 1, img));

        cid = "C026"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Abyss", CardType.WATER, 4, 4, 2, img));

        cid = "C027"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Briar", CardType.EARTH, 5, 6, 3, img));

        cid = "C028"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Vortex", CardType.AIR, 6, 2, 4, img));

        cid = "C029"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Radiant", CardType.LIGHT, 7, 4, 1, img));

        cid = "C030"; img = "/images/cards/" + cid + ".png";
        list.add(new Card(cid, "Dusk", CardType.DARK, 8, 6, 2, img));

        return list;
    }
}
