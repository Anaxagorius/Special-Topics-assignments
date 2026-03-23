/**
 * Represents a playing card with a rank and suit.
 * 
 * @author Tom Burchell
 * @version 1.0
 */
public class Card {
    private final String rank;
    private final String suit;
    private final int value;

    /**
     * Constructor for Card class.
     * 
     * @param rank The rank of the card (e.g., "A", "2", "K")
     * @param suit The suit of the card (e.g., "Hearts", "Spades")
     * @param value The numerical value of the card for scoring
     */
    public Card(String rank, String suit, int value) {
        this.rank = rank;
        this.suit = suit;
        this.value = value;
    }

    /**
     * Gets the rank of the card.
     * 
     * @return The rank of the card
     */
    public String getRank() {
        return rank;
    }

    /**
     * Gets the suit of the card.
     * 
     * @return The suit of the card
     */
    public String getSuit() {
        return suit;
    }

    /**
     * Gets the value of the card.
     * 
     * @return The numerical value of the card
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns a string representation of the card.
     * 
     * @return String in format "Rank of Suit"
     */
    @Override
    public String toString() {
        return rank + " of " + suit;
    }
}