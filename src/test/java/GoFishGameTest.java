import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GoFishGame logic (static methods only – no Swing required).
 */
public class GoFishGameTest {

    @Test
    public void testTakeCardsRemovesMatchingRank() {
        List<Card> hand = new ArrayList<>();
        hand.add(new Card("7", "Hearts",   7));
        hand.add(new Card("7", "Spades",   7));
        hand.add(new Card("A", "Diamonds", 11));

        List<Card> taken = GoFishGame.takeCards(hand, "7");
        assertEquals(2, taken.size());
        assertEquals(1, hand.size());
        assertEquals("A", hand.get(0).getRank());
    }

    @Test
    public void testTakeCardsReturnsEmptyWhenRankAbsent() {
        List<Card> hand = new ArrayList<>();
        hand.add(new Card("K", "Hearts", 10));
        List<Card> taken = GoFishGame.takeCards(hand, "Q");
        assertTrue(taken.isEmpty());
        assertEquals(1, hand.size());
    }

    @Test
    public void testTakeCardsEmptyHand() {
        List<Card> hand = new ArrayList<>();
        List<Card> taken = GoFishGame.takeCards(hand, "A");
        assertTrue(taken.isEmpty());
    }

    @Test
    public void testTakeCardsAllFourOfRank() {
        List<Card> hand = new ArrayList<>();
        hand.add(new Card("Q", "Hearts",   10));
        hand.add(new Card("Q", "Diamonds", 10));
        hand.add(new Card("Q", "Clubs",    10));
        hand.add(new Card("Q", "Spades",   10));

        List<Card> taken = GoFishGame.takeCards(hand, "Q");
        assertEquals(4, taken.size());
        assertTrue(hand.isEmpty());
    }

    @Test
    public void testCheckBooksDetectsFourOfAKindViaStaticLists() {
        List<Card> hand  = new ArrayList<>();
        List<String> books = new ArrayList<>();

        hand.add(new Card("9", "Hearts",   9));
        hand.add(new Card("9", "Diamonds", 9));
        hand.add(new Card("9", "Clubs",    9));
        hand.add(new Card("9", "Spades",   9));

        // Use the static checkBooksStatic helper for headless testing
        GoFishGame.checkBooksStatic(hand, books);
        assertEquals(1, books.size());
        assertEquals("9", books.get(0));
        assertTrue(hand.isEmpty());
    }

    @Test
    public void testCheckBooksDoesNotTriggerWithThreeOfAKind() {
        List<Card> hand  = new ArrayList<>();
        List<String> books = new ArrayList<>();

        hand.add(new Card("5", "Hearts",   5));
        hand.add(new Card("5", "Diamonds", 5));
        hand.add(new Card("5", "Clubs",    5));

        GoFishGame.checkBooksStatic(hand, books);
        assertTrue(books.isEmpty());
        assertEquals(3, hand.size());
    }

    @Test
    public void testCheckBooksMultipleBooks() {
        List<Card> hand  = new ArrayList<>();
        List<String> books = new ArrayList<>();

        for (String suit : new String[]{"Hearts","Diamonds","Clubs","Spades"}) {
            hand.add(new Card("2", suit, 2));
            hand.add(new Card("A", suit, 11));
        }

        GoFishGame.checkBooksStatic(hand, books);
        assertEquals(2, books.size());
        assertTrue(hand.isEmpty());
    }
}
