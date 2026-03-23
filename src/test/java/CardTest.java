import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Card class.
 */
public class CardTest {

    @Test
    public void testGetRank() {
        Card card = new Card("A", "Hearts", 11);
        assertEquals("A", card.getRank());
    }

    @Test
    public void testGetSuit() {
        Card card = new Card("K", "Spades", 10);
        assertEquals("Spades", card.getSuit());
    }

    @Test
    public void testGetValue() {
        Card card = new Card("7", "Clubs", 7);
        assertEquals(7, card.getValue());
    }

    @Test
    public void testToString() {
        Card card = new Card("Q", "Diamonds", 10);
        assertEquals("Q of Diamonds", card.toString());
    }

    @Test
    public void testAceValue() {
        Card ace = new Card("A", "Hearts", 11);
        assertEquals(11, ace.getValue());
    }

    @Test
    public void testFaceCardValue() {
        Card king = new Card("K", "Clubs", 10);
        assertEquals(10, king.getValue());
    }
}
