import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player (or dealer) in a card game.
 * Manages the player's hand, score calculation, and balance.
 * 
 * @author Tom Burchell
 * @version 1.1
 */
public class Player {
    private final String name;
    private final List<Card> hand;
    private int balance;

    /**
     * Constructor for Player class with no initial balance.
     * 
     * @param name The name of the player
     */
    public Player(String name) {
        this(name, 0);
    }

    /**
     * Constructor for Player class with an initial balance.
     * 
     * @param name           The name of the player
     * @param initialBalance The player's starting balance
     */
    public Player(String name, int initialBalance) {
        this.name = name;
        this.hand = new ArrayList<>();
        this.balance = initialBalance;
    }

    /**
     * Gets the player's current balance.
     * 
     * @return The player's balance
     */
    public int getBalance() {
        return balance;
    }

    /**
     * Adds an amount to the player's balance.
     * 
     * @param amount The amount to add (must be positive)
     */
    public void addBalance(int amount) {
        if (amount > 0) {
            balance += amount;
        }
    }

    /**
     * Deducts an amount from the player's balance.
     * 
     * @param amount The amount to deduct (must be positive and not exceed balance)
     * @return true if the deduction was successful, false otherwise
     */
    public boolean deductBalance(int amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            return true;
        }
        return false;
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