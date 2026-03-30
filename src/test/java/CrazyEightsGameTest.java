import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CrazyEightsGame logic (static methods only – no Swing required).
 */
public class CrazyEightsGameTest {

    @Test
    public void testEightIsAlwaysLegal() {
        Card eight = new Card("8", "Clubs", 8);
        assertTrue(CrazyEightsGame.isLegalStatic(eight, "Hearts", "5"),
            "An 8 should always be a legal play.");
    }

    @Test
    public void testMatchingRankIsLegal() {
        Card card = new Card("5", "Spades", 5);
        assertTrue(CrazyEightsGame.isLegalStatic(card, "Hearts", "5"),
            "Card with matching rank should be legal.");
    }

    @Test
    public void testMatchingSuitIsLegal() {
        Card card = new Card("2", "Hearts", 2);
        assertTrue(CrazyEightsGame.isLegalStatic(card, "Hearts", "K"),
            "Card with matching suit should be legal.");
    }

    @Test
    public void testMismatchedCardIsIllegal() {
        Card card = new Card("3", "Spades", 3);
        assertFalse(CrazyEightsGame.isLegalStatic(card, "Hearts", "K"),
            "Card with neither matching suit nor rank should be illegal.");
    }

    @Test
    public void testNullTopRankAllowsAnyCard() {
        Card card = new Card("Q", "Clubs", 10);
        assertTrue(CrazyEightsGame.isLegalStatic(card, null, null),
            "Any card is legal when there is no top card.");
    }

    @Test
    public void testCpuChooseCardEasyReturnsLegalCard() {
        List<Card> cpuHand = new ArrayList<>();
        cpuHand.add(new Card("5", "Spades",  5));
        cpuHand.add(new Card("Q", "Hearts", 10));

        List<Card> legal = new ArrayList<>(cpuHand);
        Card chosen = CrazyEightsGame.cpuChooseCard(legal, cpuHand, Difficulty.EASY, new Random(0));
        assertTrue(legal.contains(chosen));
    }

    @Test
    public void testCpuChooseCardHardPreservesEights() {
        List<Card> cpuHand = new ArrayList<>();
        cpuHand.add(new Card("8",  "Hearts",   8));
        cpuHand.add(new Card("5",  "Diamonds", 5));
        cpuHand.add(new Card("K",  "Diamonds", 10));

        List<Card> legal = new ArrayList<>();
        legal.add(new Card("8", "Hearts",   8));
        legal.add(new Card("5", "Diamonds", 5));

        Card chosen = CrazyEightsGame.cpuChooseCard(legal, cpuHand, Difficulty.HARD, new Random(0));
        assertNotEquals("8", chosen.getRank(),
            "HARD difficulty should prefer non-8 cards when other legal plays exist.");
    }

    @Test
    public void testCpuChooseCardHardForcedEight() {
        List<Card> cpuHand = new ArrayList<>();
        cpuHand.add(new Card("8", "Hearts", 8));

        List<Card> legal = new ArrayList<>();
        legal.add(new Card("8", "Hearts", 8));

        Card chosen = CrazyEightsGame.cpuChooseCard(legal, cpuHand, Difficulty.HARD, new Random(0));
        assertEquals("8", chosen.getRank(), "CPU must play the 8 when it is the only legal card.");
    }
}
