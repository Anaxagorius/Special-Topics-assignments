import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a deck of playing cards.
 * Manages card creation, shuffling, and dealing.
 * 
 * @author Tom Burchell
 * @version 1.0
 */
public class Deck {
    private final List<Card> cards;
    private int currentCardIndex;

    /**
     * Constructor for Deck class.
     * Initializes a standard 52-card deck.
     */
    public Deck() {
        cards = new ArrayList<>();
        currentCardIndex = 0;
        initializeDeck();
    }

    /**
     * Initializes the deck with all 52 cards.
     * Creates cards for all ranks and suits.
     */
    private void initializeDeck() {
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        int[] values = {2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10, 11};

        for (String suit : suits) {
            for (int i = 0; i < ranks.length; i++) {
                cards.add(new Card(ranks[i], suit, values[i]));
            }
        }
    }

    /**
     * Shuffles the deck using Collections.shuffle().
     * Resets the current card index to the beginning.
     */
    public void shuffle() {
        Collections.shuffle(cards);
        currentCardIndex = 0;
    }

    /**
     * Deals one card from the deck.
     * 
     * @return The next card from the deck
     * @throws IllegalStateException if the deck is empty
     */
    public Card deal() {
        if (currentCardIndex >= cards.size()) {
            throw new IllegalStateException("No more cards in the deck!");
        }
        return cards.get(currentCardIndex++);
    }

    /**
     * Gets the number of cards remaining in the deck.
     * 
     * @return The number of undealt cards
     */
    public int cardsRemaining() {
        return cards.size() - currentCardIndex;
    }

    /**
     * Resets the deck to deal from the beginning without reshuffling.
     */
    public void reset() {
        currentCardIndex = 0;
    }
}