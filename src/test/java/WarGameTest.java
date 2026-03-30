import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for WarGame logic (static methods only – no Swing required).
 */
public class WarGameTest {

    @Test
    public void testWarRankAceHighest() {
        Card ace  = new Card("A", "Spades", 11);
        Card king = new Card("K", "Hearts", 10);
        assertTrue(WarGame.warRank(ace) > WarGame.warRank(king));
    }

    @Test
    public void testWarRankTwoLowest() {
        Card two   = new Card("2", "Hearts", 2);
        Card three = new Card("3", "Clubs",  3);
        assertTrue(WarGame.warRank(two) < WarGame.warRank(three));
    }

    @Test
    public void testWarRankOrder() {
        Card two   = new Card("2",  "Hearts",   2);
        Card ten   = new Card("10", "Spades",  10);
        Card jack  = new Card("J",  "Clubs",   10);
        Card queen = new Card("Q",  "Diamonds",10);
        Card king  = new Card("K",  "Hearts",  10);
        Card ace   = new Card("A",  "Spades",  11);

        assertTrue(WarGame.warRank(two)   < WarGame.warRank(ten));
        assertTrue(WarGame.warRank(ten)   < WarGame.warRank(jack));
        assertTrue(WarGame.warRank(jack)  < WarGame.warRank(queen));
        assertTrue(WarGame.warRank(queen) < WarGame.warRank(king));
        assertTrue(WarGame.warRank(king)  < WarGame.warRank(ace));
    }

    @Test
    public void testWarRankNineBeforeJack() {
        Card nine = new Card("9", "Clubs",  9);
        Card jack = new Card("J", "Hearts", 10);
        assertTrue(WarGame.warRank(nine) < WarGame.warRank(jack));
    }

    @Test
    public void testWarRankSameRank() {
        Card h = new Card("5", "Hearts",   5);
        Card s = new Card("5", "Spades",   5);
        assertEquals(WarGame.warRank(h), WarGame.warRank(s));
    }

    @Test
    public void testWarRankQueenHigherThanTen() {
        Card ten   = new Card("10", "Clubs",  10);
        Card queen = new Card("Q",  "Spades", 10);
        assertTrue(WarGame.warRank(queen) > WarGame.warRank(ten));
    }
}
