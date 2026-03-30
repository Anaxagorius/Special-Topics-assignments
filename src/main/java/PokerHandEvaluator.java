import java.util.*;
import java.util.stream.Collectors;

/**
 * Evaluates 5-card poker hands and returns a comparable rank score.
 *
 * <p>Hand rankings (highest to lowest):
 * <ol>
 *   <li>Royal Flush</li>
 *   <li>Straight Flush</li>
 *   <li>Four of a Kind</li>
 *   <li>Full House</li>
 *   <li>Flush</li>
 *   <li>Straight</li>
 *   <li>Three of a Kind</li>
 *   <li>Two Pair</li>
 *   <li>One Pair</li>
 *   <li>High Card</li>
 * </ol>
 * </p>
 *
 * @author Tom Burchell
 * @version 1.0
 */
public class PokerHandEvaluator {

    /** Numeric rank order for cards (2–A). */
    private static final Map<String, Integer> RANK_VALUE = new LinkedHashMap<>();

    static {
        String[] order = {"2","3","4","5","6","7","8","9","10","J","Q","K","A"};
        for (int i = 0; i < order.length; i++) {
            RANK_VALUE.put(order[i], i + 2); // 2..14
        }
    }

    // Hand category base scores (multiplied so tiebreakers fit within gaps)
    private static final int HIGH_CARD      = 0;
    private static final int ONE_PAIR       = 1_000_000;
    private static final int TWO_PAIR       = 2_000_000;
    private static final int THREE_OF_KIND  = 3_000_000;
    private static final int STRAIGHT       = 4_000_000;
    private static final int FLUSH          = 5_000_000;
    private static final int FULL_HOUSE     = 6_000_000;
    private static final int FOUR_OF_KIND   = 7_000_000;
    private static final int STRAIGHT_FLUSH = 8_000_000;
    private static final int ROYAL_FLUSH    = 9_000_000;

    /**
     * Evaluates a 5-card hand and returns a numeric score.
     * Higher score = better hand.
     *
     * @param hand Exactly 5 cards
     * @return Numeric hand score
     */
    public static int evaluate(List<Card> hand) {
        if (hand == null || hand.size() != 5) {
            throw new IllegalArgumentException("Hand must contain exactly 5 cards.");
        }

        List<Integer> values = hand.stream()
            .map(c -> rankValue(c.getRank()))
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());

        Map<Integer, Long> freq = hand.stream()
            .collect(Collectors.groupingBy(c -> rankValue(c.getRank()), Collectors.counting()));

        boolean isFlush    = hand.stream().map(Card::getSuit).distinct().count() == 1;
        boolean isStraight = isStraight(values);

        // Royal / Straight flush
        if (isFlush && isStraight) {
            if (values.get(0) == 14) return ROYAL_FLUSH;
            return STRAIGHT_FLUSH + values.get(0);
        }

        // Four of a kind
        Optional<Map.Entry<Integer, Long>> quad = freq.entrySet().stream()
            .filter(e -> e.getValue() == 4).findFirst();
        if (quad.isPresent()) {
            int kicker = values.stream().filter(v -> !v.equals(quad.get().getKey()))
                .findFirst().orElse(0);
            return FOUR_OF_KIND + quad.get().getKey() * 20 + kicker;
        }

        // Full house
        boolean hasTrips = freq.values().stream().anyMatch(v -> v == 3);
        boolean hasPair  = freq.values().stream().anyMatch(v -> v == 2);
        if (hasTrips && hasPair) {
            int tripsRank = freq.entrySet().stream()
                .filter(e -> e.getValue() == 3).mapToInt(Map.Entry::getKey).sum();
            return FULL_HOUSE + tripsRank;
        }

        if (isFlush) {
            return FLUSH + tiebreaker(values);
        }

        if (isStraight) {
            return STRAIGHT + values.get(0);
        }

        if (hasTrips) {
            int tripsRank = freq.entrySet().stream()
                .filter(e -> e.getValue() == 3).mapToInt(Map.Entry::getKey).sum();
            return THREE_OF_KIND + tripsRank * 400 + tiebreaker(
                values.stream().filter(v -> v != tripsRank).collect(Collectors.toList()));
        }

        // Two pair
        List<Integer> pairRanks = freq.entrySet().stream()
            .filter(e -> e.getValue() == 2)
            .map(Map.Entry::getKey)
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());

        if (pairRanks.size() == 2) {
            int kicker = values.stream()
                .filter(v -> !pairRanks.contains(v))
                .findFirst().orElse(0);
            return TWO_PAIR + pairRanks.get(0) * 400 + pairRanks.get(1) * 20 + kicker;
        }

        // One pair
        if (pairRanks.size() == 1) {
            int pairRank = pairRanks.get(0);
            int kicker = tiebreaker(
                values.stream().filter(v -> v != pairRank).collect(Collectors.toList()));
            return ONE_PAIR + pairRank * 5000 + kicker;
        }

        // High card
        return HIGH_CARD + tiebreaker(values);
    }

    /**
     * Returns the human-readable name of the hand.
     *
     * @param hand Exactly 5 cards
     * @return Hand name string
     */
    public static String handName(List<Card> hand) {
        int score = evaluate(hand);
        if (score >= ROYAL_FLUSH)      return "Royal Flush";
        if (score >= STRAIGHT_FLUSH)   return "Straight Flush";
        if (score >= FOUR_OF_KIND)     return "Four of a Kind";
        if (score >= FULL_HOUSE)       return "Full House";
        if (score >= FLUSH)            return "Flush";
        if (score >= STRAIGHT)         return "Straight";
        if (score >= THREE_OF_KIND)    return "Three of a Kind";
        if (score >= TWO_PAIR)         return "Two Pair";
        if (score >= ONE_PAIR)         return "One Pair";
        return "High Card";
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private static boolean isStraight(List<Integer> sortedDesc) {
        // Normal straight
        boolean normal = true;
        for (int i = 0; i < sortedDesc.size() - 1; i++) {
            if (sortedDesc.get(i) - sortedDesc.get(i + 1) != 1) {
                normal = false;
                break;
            }
        }
        if (normal) return true;

        // Wheel (A-2-3-4-5): Ace acts as 1
        List<Integer> wheel = Arrays.asList(14, 5, 4, 3, 2);
        return new HashSet<>(sortedDesc).equals(new HashSet<>(wheel));
    }

    /** Compresses a list of values into a single tiebreaker integer. */
    private static int tiebreaker(List<Integer> sortedDesc) {
        // Multiplier 15 keeps max value (14*15^4+…) well under 1_000_000 gap
        int score = 0;
        for (int v : sortedDesc) {
            score = score * 15 + v;
        }
        return score;
    }

    /**
     * Returns the numeric rank value for a card rank string.
     *
     * @param rank Card rank string
     * @return Numeric value (2–14)
     */
    public static int rankValue(String rank) {
        return RANK_VALUE.getOrDefault(rank, 0);
    }
}
