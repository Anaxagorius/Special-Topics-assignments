import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

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

    // -----------------------------------------------------------------------
    // Split tests
    // -----------------------------------------------------------------------

    /**
     * Helper that creates a BlackjackGame with a simulated Scanner so that
     * playPlayerTurn can be exercised without real user input.
     */
    private BlackjackGame gameWithInput(Player player, String inputLines) {
        Scanner sc = new Scanner(new ByteArrayInputStream(inputLines.getBytes()));
        return new BlackjackGame(Collections.singletonList(player), sc);
    }

    @Test
    public void testSplitCreatesSecondHand() {
        // Player has $80 remaining balance (simulating $20 already bet from $100 starting balance)
        Player player = new Player("Alice", 80);
        assertEquals(80, player.getBalance(), "Starting balance for this test should be $80");
        // Input: "p" = split, "s" = stand hand 1, "s" = stand hand 2
        BlackjackGame game = gameWithInput(player, "p\ns\ns\n");

        Card card1 = new Card("8", "Hearts", 8);
        Card card2 = new Card("8", "Spades", 8);
        List<Card> hand = new ArrayList<>(Arrays.asList(card1, card2));
        List<List<Card>> hands = new ArrayList<>(Collections.singletonList(hand));
        List<Integer> handBets = new ArrayList<>(Collections.singletonList(20));

        game.playPlayerTurn(player, hands, handBets);

        assertEquals(2, hands.size(), "Split should produce two hands");
        assertEquals(2, hands.get(0).size(), "First hand should have 2 cards");
        assertEquals(2, hands.get(1).size(), "Second hand should have 2 cards");
        // card1 (hand.get(0)) goes to splitHand1 and card2 (hand.get(1)) goes to splitHand2
        assertSame(card1, hands.get(0).get(0), "card1 should be the first card of hand 1");
        assertSame(card2, hands.get(1).get(0), "card2 should be the first card of hand 2");
        assertEquals(60, player.getBalance(), "Extra bet should be deducted from balance");
        assertEquals(20, (int) handBets.get(0), "Hand 1 bet should equal original bet");
        assertEquals(20, (int) handBets.get(1), "Hand 2 bet should equal original bet");
    }

    @Test
    public void testSplitNotOfferedForDifferentRanks() {
        Player player = new Player("Bob", 80);
        // Input: "p" should be rejected as invalid; "s" stands
        BlackjackGame game = gameWithInput(player, "p\ns\n");

        List<Card> hand = new ArrayList<>(Arrays.asList(
                new Card("7", "Hearts", 7),
                new Card("8", "Spades", 8)));
        List<List<Card>> hands = new ArrayList<>(Collections.singletonList(hand));
        List<Integer> handBets = new ArrayList<>(Collections.singletonList(20));

        game.playPlayerTurn(player, hands, handBets);

        assertEquals(1, hands.size(), "No split should occur with different-rank cards");
        assertEquals(2, hand.size(), "Hand should still have the original 2 cards");
    }

    @Test
    public void testSplitNotOfferedWhenInsufficientBalance() {
        // Player has exactly $0 left; cannot afford the split
        Player player = new Player("Carol", 0);
        // Input: "p" rejected (canSplit=false), then "s" stands
        BlackjackGame game = gameWithInput(player, "p\ns\n");

        List<Card> hand = new ArrayList<>(Arrays.asList(
                new Card("9", "Hearts", 9),
                new Card("9", "Spades", 9)));
        List<List<Card>> hands = new ArrayList<>(Collections.singletonList(hand));
        List<Integer> handBets = new ArrayList<>(Collections.singletonList(20));

        game.playPlayerTurn(player, hands, handBets);

        assertEquals(1, hands.size(), "No split should occur without sufficient balance");
    }

    // -----------------------------------------------------------------------
    // Double-down tests
    // -----------------------------------------------------------------------

    @Test
    public void testDoubleDownDoublesBetAndDealsExactlyOneCard() {
        Player player = new Player("Dave", 80);
        // Input: "d" = double down
        BlackjackGame game = gameWithInput(player, "d\n");

        List<Card> hand = new ArrayList<>(Arrays.asList(
                new Card("5", "Hearts", 5),
                new Card("6", "Spades", 6)));
        List<List<Card>> hands = new ArrayList<>(Collections.singletonList(hand));
        List<Integer> handBets = new ArrayList<>(Collections.singletonList(20));

        game.playPlayerTurn(player, hands, handBets);

        assertEquals(3, hand.size(), "Double down should deal exactly one additional card");
        assertEquals(40, (int) handBets.get(0), "Bet should be doubled");
        assertEquals(60, player.getBalance(), "Extra bet should be deducted from balance");
    }

    @Test
    public void testDoubleDownNotOfferedWhenInsufficientBalance() {
        // Player has $0 left; cannot cover the double-down bet
        Player player = new Player("Eve", 0);
        // Input: "d" rejected (canDouble=false), then "s" stands
        BlackjackGame game = gameWithInput(player, "d\ns\n");

        List<Card> hand = new ArrayList<>(Arrays.asList(
                new Card("5", "Hearts", 5),
                new Card("6", "Spades", 6)));
        List<List<Card>> hands = new ArrayList<>(Collections.singletonList(hand));
        List<Integer> handBets = new ArrayList<>(Collections.singletonList(20));

        game.playPlayerTurn(player, hands, handBets);

        assertEquals(2, hand.size(), "No double down when balance is insufficient");
        assertEquals(20, (int) handBets.get(0), "Bet should remain unchanged");
    }

    @Test
    public void testDoubleDownNotOfferedAfterHit() {
        Player player = new Player("Frank", 80);
        // Input: "h" = hit first, then "d" rejected (not first action), then "s" stands
        BlackjackGame game = gameWithInput(player, "h\nd\ns\n");

        List<Card> hand = new ArrayList<>(Arrays.asList(
                new Card("2", "Hearts", 2),
                new Card("3", "Spades", 3)));
        List<List<Card>> hands = new ArrayList<>(Collections.singletonList(hand));
        List<Integer> handBets = new ArrayList<>(Collections.singletonList(20));

        game.playPlayerTurn(player, hands, handBets);

        // Hand should have 3 cards (2 original + 1 hit), no double-down card
        assertEquals(3, hand.size(), "After a hit, double down should not be available");
        assertEquals(20, (int) handBets.get(0), "Bet should remain unchanged when double down is unavailable");
    }

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
