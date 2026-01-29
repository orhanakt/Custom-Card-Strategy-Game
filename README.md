Game With AI-Generated Images: https://drive.google.com/file/d/1rXr-1iLOdwnRwOvIxqKh8ah7eRjk4nkr/view?usp=drive_link


Game Description 

Custom Card Strategy Game is a turn-based card battle between a human Player and a Computer. Each card has ATK (attack), DEF (defense), and COST (energy) values. Players manage their energy to play cards, win rounds, and gain score. The computer uses different strategies depending on the selected difficulty (Easy/Normal/Hard), and its turn can be executed with a short timer delay for a controlled game flow.

Game Rules 

•	Goal: Reach the score limit first, or have the higher score when max rounds ends. The game can also end if the deck is empty and both hands are empty.

•	Setup: Both sides start with initial energy and receive an initial hand from the deck. A starting turn is chosen (can be random).

•	Cards: Each card has ATK, DEF, and COST. COST is paid from energy to play the card.

•	On your turn you can:

1.	Play a card (only if you can pay its COST),

2.	Draw a card (if the deck isn’t empty and your hand isn’t full),

3.	End turn.

•	Round resolution: Each side plays one card (or plays none).

Damage = max(0, ATK – opponent DEF). Higher damage wins; winner gains points equal to their damage (draw = no points).
•	After each round: Both players gain energy and may draw a card if possible (up to max hand size).

Class Explanation

App package

Main: Main is the application entry point (public static void main). It creates the MainFrame and starts the Swing GUI on the Event Dispatch Thread.

Ai package

ComputerPlayer (extends Player): ComputerPlayer is the AI-controlled opponent. It holds a ComputerStrategy and uses it to select a playable card from its hand based on the current GameState.

ComputerStrategy (interface): ComputerStrategy defines how the AI chooses a card. Implementations decide which card to play using the current GameState and the AI hand.

EasyStrategy: EasyStrategy is a simple AI that picks randomly among playable cards.

NormalStrategy: NormalStrategy selects the best card using a lightweight evaluation (favoring higher ATK/DEF while penalizing COST), with random tie-breaks.

HardStrategy: HardStrategy adapts its choice using the score difference, playing more aggressive when behind and more defensive when ahead, while still considering COST.

StrategyFactory: StrategyFactory returns the correct strategy implementation for the selected Difficulty (EASY/NORMAL/HARD).

Engine package

GameController: GameController controls the full game flow and processes UI actions (start, play, draw, end turn). It resolves rounds with RoundResolver, runs post-round upkeep, checks game-over, handles save/load, updates high scores, and schedules the AI move using javax.swing.Timer for a controlled turn delay.

GameState: GameState stores the live in-game data: players, deck, RNG, round number, current turn, and phase (MENU/PLAYING/GAME_OVER).

RoundResolver: RoundResolver calculates a round result from the played cards, applies energy/score updates, and produces a RoundResult for the UI.

GameRules: GameRules contains the core combat calculations (effective defense and damage formulas) in one place.

GameSerializer: GameSerializer converts GameState to a text format for saving and rebuilds it back when loading.

Model package

Card: Card represents a playable card with id, name, type, ATK, DEF, COST, and imagePath.

CardType (enum): CardType defines the element types: FIRE, WATER, EARTH, AIR, LIGHT, DARK.

Deck: Deck represents the draw pile and supports reset, shuffle, and draw operations.

Hand: Hand is a small wrapper for managing a player’s hand safely (add/remove/size/clear/list access).

Player: Player represents a participant with a hand, score, and energy, and supports drawing and playing cards with affordability checks.

RoundResult: RoundResult is the last-round summary used by the UI (played cards, outcome, score gains, and message).

GameResult (enum): GameResult represents the round outcome: WIN, LOSE, DRAW.

Phase (enum): Phase represents the game phase: MENU, PLAYING, GAME_OVER.

Turn (enum): Turn indicates whose turn it is: PLAYER or COMPUTER.

Ui package

MainFrame (extends JFrame): MainFrame is the main window that switches between screens using CardLayout and refreshes the visible panel based on GameState.

MainMenuPanel: MainMenuPanel is the start screen where the user can start a new game, continue a saved game, open rules/scores/settings, or exit.

GamePanel: GamePanel is the gameplay screen that renders the battlefield and hand, calls GameController actions (play/draw/end turn), and refreshes the UI from the latest state.

GameActionHandlers: GameActionHandlers wires button actions and key bindings and enables/disables actions based on the current state/turn.

CardView: CardView renders a single card visually, loading images via ResourceLoader and highlighting selection.

GameOverPanel: GameOverPanel shows the final outcome and provides actions like restart and return to menu.

HighScoresPanel: HighScoresPanel displays the high score list (typically in a table) using data from HighScoreManager.

RulesPanel: RulesPanel shows the rules text and allows returning to the menu.

SettingsPanel: SettingsPanel lets the user adjust limits and difficulty, applies changes to GameController, and refreshes the UI.

Util package

Constants: Constants stores shared fixed values like default limits, file names, and the default AI delay.

ResourceLoader: ResourceLoader loads and caches images/icons from the classpath and can return scaled icons for rendering.

DateUtil: DateUtil formats/parses date-time values used in high score records.

persistence

GameSaveFileStore: GameSaveFileStore reads and writes the serialized saved-game text to a file.

HighScoreEntry: HighScoreEntry holds one score record (player name, score, and timestamp).

HighScoreFileStore: HighScoreFileStore loads/saves high score entries from/to a file, including parsing/formatting each line.

HighScoreManager: HighScoreManager keeps high scores in memory and provides operations like add, load, save, and getTop(N) for the UI.



Explanation of OOP usage

This project follows a clear object-oriented design by separating the game into models, behavior, and UI. The main game entities are represented as classes: Card stores card stats, Deck manages the draw pile, Hand 
manages a player’s cards, and GameState holds the live match data (players, deck, phase, turn, round). The gameplay flow is handled by GameController, which processes actions (play/draw/end turn), updates the 
state, and triggers UI refresh.

Inheritance is used to reuse shared player behavior. Player contains common fields and actions such as energy, score, drawing, and playing cards, while ComputerPlayer extends Player and adds AI-specific logic for 
automatically selecting a card. This keeps shared logic in one place and avoids duplication.

Polymorphism is used in the AI system via the ComputerStrategy interface. Different difficulty strategies (EasyStrategy, NormalStrategy, HardStrategy) implement the same interface, so the controller can work with a 
ComputerStrategy reference without depending on a specific difficulty. Changing difficulty only replaces the strategy instance (via StrategyFactory), while the rest of the code stays the same.

Encapsulation and separation of concerns are applied throughout: fields are kept private and accessed through methods (e.g., energy checks and spending, safe hand operations). Core formulas are isolated in 
GameRules, round resolution is handled by RoundResolver, persistence is separated into GameSerializer and file store/manager classes, and Swing UI panels focus only on rendering and user input rather than game 
logic.

Description of AI behavior and difficulty levels

The Computer opponent is implemented as a ComputerPlayer that automatically selects a card when it is the computer’s turn. The selection logic is abstracted behind the ComputerStrategy interface, so the controller 
and UI do not need to know which difficulty is active. When the computer’s turn starts, the move can be executed with a short timer delay to create a controlled game flow. The AI always tries to play a 
valid/affordable card; if the strategy’s first choice is not playable (e.g., not enough energy), it falls back to the first affordable option (otherwise it may effectively skip by playing nothing).

Difficulty affects how the computer ranks or chooses cards:

•	Easy: Chooses randomly among playable cards (simple, unpredictable, and not optimized).

•	Normal: Scores each playable card with a basic heuristic (favoring higher ATK/DEF while penalizing higher COST) and plays the best-scoring option; ties are broken randomly.

•	Hard: Adapts to the current match situation using the score difference. If behind, it prefers more aggressive choices (higher ATK bias); if ahead, it prefers safer choices (higher DEF bias). COST is still 
penalized, and tie-breaks tend to prefer cheaper cards for efficiency.
