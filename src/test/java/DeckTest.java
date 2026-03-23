import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Deck class.
 */
public class DeckTest {

    private Deck deck;

    @BeforeEach
    public void setUp() {
        deck = new Deck();
    }

    @Test
    public void testDeckHas52Cards() {
        assertEquals(52, deck.cardsRemaining());
    }

    @Test
    public void testDealDecreasesCardsRemaining() {
        deck.deal();
        assertEquals(51, deck.cardsRemaining());
    }

    @Test
    public void testDealReturnsCard() {
        Card card = deck.deal();
        assertNotNull(card);
    }

    @Test
    public void testDealAllCards() {
        for (int i = 0; i < 52; i++) {
            deck.deal();
        }
        assertEquals(0, deck.cardsRemaining());
    }

    @Test
    public void testDealThrowsWhenEmpty() {
        for (int i = 0; i < 52; i++) {
            deck.deal();
        }
        assertThrows(IllegalStateException.class, () -> deck.deal());
    }

    @Test
    public void testShuffleResetsIndex() {
        deck.deal();
        deck.deal();
        deck.shuffle();
        assertEquals(52, deck.cardsRemaining());
    }

    @Test
    public void testResetRestoresAllCards() {
        deck.deal();
        deck.deal();
        deck.deal();
        deck.reset();
        assertEquals(52, deck.cardsRemaining());
    }

}
