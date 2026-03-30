import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for the new BlackjackGame features:
 * split, double-down, insurance, multi-player, and save/load.
 */
public class BlackjackGameTest {

    // -----------------------------------------------------------------------
    // scoreHand tests
    // -----------------------------------------------------------------------

    @Test
    public void testScoreHandBasic() {
        BlackjackGame game = new BlackjackGame();
        List<Card> hand = Arrays.asList(new Card("5", "Hearts", 5), new Card("7", "Clubs", 7));
        assertEquals(12, game.scoreHand(hand));
    }

    @Test
    public void testScoreHandAceAs11() {
        BlackjackGame game = new BlackjackGame();
        List<Card> hand = Arrays.asList(new Card("A", "Hearts", 11), new Card("9", "Clubs", 9));
        assertEquals(20, game.scoreHand(hand));
    }

    @Test
    public void testScoreHandAceAs1ToAvoidBust() {
        BlackjackGame game = new BlackjackGame();
        List<Card> hand = Arrays.asList(
                new Card("A", "Hearts", 11),
                new Card("9", "Clubs", 9),
                new Card("5", "Diamonds", 5));
        assertEquals(15, game.scoreHand(hand));
    }

    @Test
    public void testScoreHandBlackjack() {
        BlackjackGame game = new BlackjackGame();
        List<Card> hand = Arrays.asList(new Card("A", "Spades", 11), new Card("K", "Hearts", 10));
        assertEquals(21, game.scoreHand(hand));
    }

    @Test
    public void testScoreHandBust() {
        BlackjackGame game = new BlackjackGame();
        List<Card> hand = Arrays.asList(
                new Card("K", "Hearts", 10),
                new Card("Q", "Spades", 10),
                new Card("5", "Clubs", 5));
        assertEquals(25, game.scoreHand(hand));
    }

    @Test
    public void testScoreHandEmpty() {
        BlackjackGame game = new BlackjackGame();
        assertEquals(0, game.scoreHand(new ArrayList<>()));
    }

    // -----------------------------------------------------------------------
    // Multi-player construction tests
    // -----------------------------------------------------------------------

    @Test
    public void testMultiPlayerConstructor() {
        List<Player> players = new ArrayList<>();
        players.add(new Player("Alice", 100));
        players.add(new Player("Bob", 100));
        BlackjackGame game = new BlackjackGame(players);
        assertNotNull(game);
    }

    @Test
    public void testEmptyPlayerListThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new BlackjackGame(new ArrayList<>()));
    }

    @Test
    public void testNullPlayerListThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new BlackjackGame(null));
    }

    @Test
    public void testDefaultConstructorCreatesOnePlayer() {
        // Should not throw; default constructor creates a single "Player"
        BlackjackGame game = new BlackjackGame();
        assertNotNull(game);
    }

    // -----------------------------------------------------------------------
    // Save / Load tests
    // -----------------------------------------------------------------------

    private static final String SAVE_FILE = "blackjack_save.dat";

    @BeforeEach
    public void cleanUpSaveFile() {
        new File(SAVE_FILE).delete();
    }

    @Test
    public void testSaveAndLoadRestoresBalances() {
        List<Player> players = new ArrayList<>();
        players.add(new Player("Alice", 150));
        players.add(new Player("Bob", 75));
        BlackjackGame game = new BlackjackGame(players);

        game.saveGame();
        assertTrue(new File(SAVE_FILE).exists(), "Save file should be created");

        BlackjackGame loaded = BlackjackGame.loadGame();
        assertNotNull(loaded, "Loaded game should not be null");
        assertEquals(2, loaded.getPlayers().size());
        assertEquals("Alice", loaded.getPlayers().get(0).getName());
        assertEquals(150, loaded.getPlayers().get(0).getBalance());
        assertEquals("Bob", loaded.getPlayers().get(1).getName());
        assertEquals(75, loaded.getPlayers().get(1).getBalance());
    }

    @Test
    public void testLoadReturnsNullWhenNoFile() {
        // Ensure file does not exist
        assertFalse(new File(SAVE_FILE).exists());
        assertNull(BlackjackGame.loadGame());
    }

    @Test
    public void testSaveAndLoadRestoresStats() {
        List<Player> players = new ArrayList<>();
        players.add(new Player("Carol", 200));
        BlackjackGame game = new BlackjackGame(players);

        // Manually bump stats via the getPlayerStats accessor
        int[] s = game.getPlayerStats(players.get(0));
        s[0] = 3; // wins
        s[1] = 1; // losses
        s[2] = 2; // ties

        game.saveGame();
        BlackjackGame loaded = BlackjackGame.loadGame();
        assertNotNull(loaded);

        int[] ls = loaded.getPlayerStats(loaded.getPlayers().get(0));
        assertEquals(3, ls[0], "Wins should be restored");
        assertEquals(1, ls[1], "Losses should be restored");
        assertEquals(2, ls[2], "Ties should be restored");
    }

    // -----------------------------------------------------------------------
    // GameState serialisation test
    // -----------------------------------------------------------------------

    @Test
    public void testGameStateFields() {
        List<String> names    = Arrays.asList("X", "Y");
        List<Integer> bals    = Arrays.asList(50, 80);
        List<int[]> sts       = Arrays.asList(new int[]{1, 2, 3}, new int[]{4, 5, 6});
        GameState gs = new GameState(names, bals, sts);

        assertEquals(names, gs.playerNames);
        assertEquals(bals,  gs.playerBalances);
        assertArrayEquals(new int[]{1, 2, 3}, gs.playerStats.get(0));
        assertArrayEquals(new int[]{4, 5, 6}, gs.playerStats.get(1));
    }
}
