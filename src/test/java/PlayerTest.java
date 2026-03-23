import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Player class.
 */
public class PlayerTest {

    private Player player;

    @BeforeEach
    public void setUp() {
        player = new Player("TestPlayer");
    }

    @Test
    public void testGetName() {
        assertEquals("TestPlayer", player.getName());
    }

    @Test
    public void testInitialHandIsEmpty() {
        assertTrue(player.getHand().isEmpty());
    }

    @Test
    public void testAddCard() {
        Card card = new Card("5", "Hearts", 5);
        player.addCard(card);
        assertEquals(1, player.getHand().size());
        assertEquals(card, player.getHand().get(0));
    }

    @Test
    public void testClearHand() {
        player.addCard(new Card("5", "Hearts", 5));
        player.addCard(new Card("K", "Spades", 10));
        player.clearHand();
        assertTrue(player.getHand().isEmpty());
    }

    @Test
    public void testCalculateScoreBasic() {
        player.addCard(new Card("5", "Hearts", 5));
        player.addCard(new Card("7", "Clubs", 7));
        assertEquals(12, player.calculateScore());
    }

    @Test
    public void testCalculateScoreWithAceAs11() {
        player.addCard(new Card("A", "Hearts", 11));
        player.addCard(new Card("9", "Clubs", 9));
        // Ace counts as 11, total = 20
        assertEquals(20, player.calculateScore());
    }

    @Test
    public void testCalculateScoreWithAceAs1() {
        player.addCard(new Card("A", "Hearts", 11));
        player.addCard(new Card("9", "Clubs", 9));
        player.addCard(new Card("5", "Diamonds", 5));
        // Ace must count as 1 to avoid bust: 1 + 9 + 5 = 15
        assertEquals(15, player.calculateScore());
    }

    @Test
    public void testCalculateScoreBlackjack() {
        player.addCard(new Card("A", "Spades", 11));
        player.addCard(new Card("K", "Hearts", 10));
        // Blackjack: Ace (11) + King (10) = 21
        assertEquals(21, player.calculateScore());
    }

    @Test
    public void testCalculateScoreBust() {
        player.addCard(new Card("K", "Hearts", 10));
        player.addCard(new Card("Q", "Spades", 10));
        player.addCard(new Card("5", "Clubs", 5));
        // 10 + 10 + 5 = 25 (bust)
        assertEquals(25, player.calculateScore());
    }

    @Test
    public void testCalculateScoreMultipleAces() {
        player.addCard(new Card("A", "Hearts", 11));
        player.addCard(new Card("A", "Spades", 11));
        // Two aces: one counts as 11, one as 1 => 12
        assertEquals(12, player.calculateScore());
    }

    @Test
    public void testCalculateScoreEmptyHand() {
        assertEquals(0, player.calculateScore());
    }

    @Test
    public void testToString() {
        player.addCard(new Card("5", "Hearts", 5));
        String result = player.toString();
        assertTrue(result.contains("TestPlayer"));
        assertTrue(result.contains("Score:"));
    }
}
