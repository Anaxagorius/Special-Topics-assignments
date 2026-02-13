/**
 *
 * BlackjackGame class
 * Tom Burchell
 * W0516036
 * Date: 2026-02-13
 *
 */
import java.util.Scanner;

public class BlackjackGame {
    // Deck used for dealing cards
    private final Deck deck;
    // Player and dealer instances
    private final Player player;
    private final Player dealer;
    // Scanner for reading user input
    private final Scanner scanner;

    /**
     * Constructor for BlackjackGame class.
     * Initializes the deck, player, dealer, and scanner for input.
     */
    public BlackjackGame() {
        deck = new Deck();
        player = new Player("Player");
        dealer = new Player("Dealer");
        scanner = new Scanner(System.in);
    }

    /**
     * Main game loop.
     * Runs the Blackjack game, handling rounds and user input for playing again.
     */
    public void play() {
        System.out.println("Welcome to Blackjack!");

        // Continue playing rounds until the user chooses to stop
        while (true) {
            // Reset hands and shuffle deck for a new round
            player.clearHand();
            dealer.clearHand();
            deck.shuffle();

            // Deal two cards to player and dealer
            player.addCard(deck.deal());
            dealer.addCard(deck.deal());
            player.addCard(deck.deal());
            dealer.addCard(deck.deal());

            // Display initial hands
            System.out.println("\nYour hand: " + player.getHand() + " (Score: " + player.calculateScore() + ")");
            System.out.println("Dealer's visible card: " + dealer.getHand().get(0));

            // Handle player's turn
            playerTurn();

            // Handle dealer's turn if player didn't bust
            if (player.calculateScore() <= 21) {
                dealerTurn();
            }

            // Determine and display the winner
            determineWinner();

            // Ask if the user wants to play another round
            System.out.println("\nPlay again? (y/n): ");
            String response = scanner.nextLine().trim().toLowerCase();
            if (!response.equals("y")) {
                break;
            }
        }

        System.out.println("Thanks for coming out!");
        scanner.close();
    }

    /**
     * Manages the player's turn.
     * Allows the player to hit or stand until they bust or stand.
     */
    private void playerTurn() {
        while (player.calculateScore() <= 21) {
            System.out.println("Do you want to hit or stand? (h/s): ");
            String choice = scanner.nextLine().trim().toLowerCase();
            if (choice.equals("h")) {
                player.addCard(deck.deal());
                System.out.println("Your hand: " + player.getHand() + " (Score: " + player.calculateScore() + ")");
            } else if (choice.equals("s")) {
                break; // Player stands, end turn
            }
        }
    }

    /**
     * Manages the dealer's turn.
     * Dealer hits until their score is at least 17.
     */
    private void dealerTurn() {
        System.out.println("\nDealer's hand: " + dealer.getHand() + " (Score: " + dealer.calculateScore() + ")");
        while (dealer.calculateScore() < 17) {
            dealer.addCard(deck.deal());
            System.out.println("Dealer draws: " + dealer.getHand().get(dealer.getHand().size() - 1));
            System.out.println("Dealer's hand: " + dealer.getHand() + " (Score: " + dealer.calculateScore() + ")");
        }
    }

    /**
     * Determines the winner based on final scores.
     * Compares player and dealer scores, accounting for busts.
     */
    private void determineWinner() {
        int playerScore = player.calculateScore();
        int dealerScore = dealer.calculateScore();

        System.out.println("\nFinal Scores -> You: " + playerScore + ", Dealer: " + dealerScore);

        // Check win/lose conditions
        if (playerScore > 21) {
            System.out.println("Haha you bust! Ima always win.");
        } else if (dealerScore > 21) {
            System.out.println("You got lucky! You win");
        } else if (playerScore > dealerScore) {
            System.out.println("You win.....whatever");
        } else if (dealerScore > playerScore) {
            System.out.println("Dealer wins. ITS THE DEALERS WORLD!");
        } else {
            System.out.println("It's a tie.......but...dealer still wins");
        }
    }

    /**
     * Main method to start the game.
     * Creates a new BlackjackGame instance and runs it.
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        BlackjackGame game = new BlackjackGame();
        game.play();
    }
}

/*
 * Citations:
 * Kevin's Guides. (n.d.). Console Blackjack in Java (21). https://kevinsguides.com/guides/code/java/blackjack
 * JetBrains. (2026). IntelliJ IDEA Ultimate [Computer software]. JetBrains s.r.o. https://www.jetbrains.com/idea/
 * Oracle. (2026). Java Platform, Standard Edition 17 [Computer software]. Oracle Corporation. https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
 */
