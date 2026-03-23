import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player (or dealer) in a card game.
 * Manages the player's hand and score calculation.
 * 
 * @author Tom Burchell
 * @version 1.0
 */
public class Player {
    private final String name;
    private final List<Card> hand;

    /**
     * Constructor for Player class.
     * 
     * @param name The name of the player
     */
    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
    }

    /**
     * Gets the player's name.
     * 
     * @return The player's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the player's current hand.
     * 
     * @return List of cards in the player's hand
     */
    public List<Card> getHand() {
        return hand;
    }

    /**
     * Adds a card to the player's hand.
     * 
     * @param card The card to add
     */
    public void addCard(Card card) {
        hand.add(card);
    }

    /**
     * Clears all cards from the player's hand.
     */
    public void clearHand() {
        hand.clear();
    }

    /**
     * Calculates the total score of the player's hand.
     * Handles Ace values intelligently (11 or 1) to avoid busting when possible.
     * 
     * @return The total score of the hand
     */
    public int calculateScore() {
        int score = 0;
        int aceCount = 0;

        // First pass: sum all card values and count Aces
        for (Card card : hand) {
            score += card.getValue();
            if (card.getRank().equals("A")) {
                aceCount++;
            }
        }

        // Adjust for Aces: convert from 11 to 1 if needed to avoid busting
        while (score > 21 && aceCount > 0) {
            score -= 10; // Convert an Ace from 11 to 1
            aceCount--;
        }

        return score;
    }

    /**
     * Returns a string representation of the player and their hand.
     * 
     * @return String with player name and hand
     */
    @Override
    public String toString() {
        return name + "'s hand: " + hand + " (Score: " + calculateScore() + ")";
    }
}