import java.io.Serializable;
import java.util.List;

/**
 * Serializable snapshot of a Blackjack session.
 * Stores player names, balances, and per-player win/loss/tie statistics
 * so a session can be saved to disk and resumed later.
 *
 * @author Tom Burchell
 * @version 1.0
 */
public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Names of all players in the session. */
    public final List<String> playerNames;

    /** Balances corresponding to each player (same order as {@code playerNames}). */
    public final List<Integer> playerBalances;

    /**
     * Per-player statistics stored as int arrays of length 3:
     * index 0 = wins, index 1 = losses, index 2 = ties.
     */
    public final List<int[]> playerStats;

    /**
     * Creates a new GameState snapshot.
     *
     * @param playerNames    ordered list of player names
     * @param playerBalances ordered list of player balances
     * @param playerStats    ordered list of [wins, losses, ties] arrays
     */
    public GameState(List<String> playerNames,
                     List<Integer> playerBalances,
                     List<int[]> playerStats) {
        this.playerNames    = playerNames;
        this.playerBalances = playerBalances;
        this.playerStats    = playerStats;
    }
}
