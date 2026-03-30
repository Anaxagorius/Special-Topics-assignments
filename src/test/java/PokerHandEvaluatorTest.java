import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PokerHandEvaluator.
 */
public class PokerHandEvaluatorTest {

    // Helper: build a 5-card hand from rank/suit pairs
    private static List<Card> hand(String... spec) {
        // spec: "AS", "KH", "QD", "JC", "10S" etc.
        Card[] cards = new Card[spec.length];
        for (int i = 0; i < spec.length; i++) {
            cards[i] = parseCard(spec[i]);
        }
        return Arrays.asList(cards);
    }

    private static Card parseCard(String spec) {
        String rank;
        String suit;
        if (spec.startsWith("10")) {
            rank = "10";
            suit = suitFromChar(spec.charAt(2));
        } else {
            rank = String.valueOf(spec.charAt(0));
            suit = suitFromChar(spec.charAt(1));
        }
        int value = PokerHandEvaluator.rankValue(rank);
        return new Card(rank, suit, value);
    }

    private static String suitFromChar(char c) {
        return switch (c) {
            case 'H' -> "Hearts";
            case 'D' -> "Diamonds";
            case 'C' -> "Clubs";
            case 'S' -> "Spades";
            default  -> throw new IllegalArgumentException("Unknown suit: " + c);
        };
    }

    @Test
    public void testRoyalFlush() {
        List<Card> h = hand("AH","KH","QH","JH","10H");
        assertEquals("Royal Flush", PokerHandEvaluator.handName(h));
    }

    @Test
    public void testStraightFlush() {
        List<Card> h = hand("9S","8S","7S","6S","5S");
        assertEquals("Straight Flush", PokerHandEvaluator.handName(h));
    }

    @Test
    public void testFourOfAKind() {
        List<Card> h = hand("AC","AH","AD","AS","2C");
        assertEquals("Four of a Kind", PokerHandEvaluator.handName(h));
    }

    @Test
    public void testFullHouse() {
        List<Card> h = hand("KC","KH","KD","2S","2H");
        assertEquals("Full House", PokerHandEvaluator.handName(h));
    }

    @Test
    public void testFlush() {
        List<Card> h = hand("2D","5D","7D","9D","JD");
        assertEquals("Flush", PokerHandEvaluator.handName(h));
    }

    @Test
    public void testStraight() {
        List<Card> h = hand("5C","6H","7D","8S","9C");
        assertEquals("Straight", PokerHandEvaluator.handName(h));
    }

    @Test
    public void testThreeOfAKind() {
        List<Card> h = hand("7C","7H","7D","2S","3C");
        assertEquals("Three of a Kind", PokerHandEvaluator.handName(h));
    }

    @Test
    public void testTwoPair() {
        List<Card> h = hand("8C","8H","3D","3S","AC");
        assertEquals("Two Pair", PokerHandEvaluator.handName(h));
    }

    @Test
    public void testOnePair() {
        List<Card> h = hand("JC","JH","2D","5S","9C");
        assertEquals("One Pair", PokerHandEvaluator.handName(h));
    }

    @Test
    public void testHighCard() {
        List<Card> h = hand("2C","5H","7D","9S","JC");
        assertEquals("High Card", PokerHandEvaluator.handName(h));
    }

    @Test
    public void testRoyalFlushBeatsStraightFlush() {
        int rf = PokerHandEvaluator.evaluate(hand("AH","KH","QH","JH","10H"));
        int sf = PokerHandEvaluator.evaluate(hand("9S","8S","7S","6S","5S"));
        assertTrue(rf > sf);
    }

    @Test
    public void testFourOfAKindBeatsFullHouse() {
        int four = PokerHandEvaluator.evaluate(hand("AC","AH","AD","AS","2C"));
        int full = PokerHandEvaluator.evaluate(hand("KC","KH","KD","2S","2H"));
        assertTrue(four > full);
    }

    @Test
    public void testWheelStraight() {
        // A-2-3-4-5 is the lowest straight
        List<Card> h = hand("AC","2H","3D","4S","5C");
        assertEquals("Straight", PokerHandEvaluator.handName(h));
    }

    @Test
    public void testEvaluateRequiresFiveCards() {
        List<Card> h = hand("AC","KH","QD");
        assertThrows(IllegalArgumentException.class, () -> PokerHandEvaluator.evaluate(h));
    }

    @Test
    public void testHigherPairWins() {
        int aces  = PokerHandEvaluator.evaluate(hand("AC","AH","2D","3S","4C"));
        int twos  = PokerHandEvaluator.evaluate(hand("2C","2H","3D","4S","5C"));
        assertTrue(aces > twos);
    }
}
