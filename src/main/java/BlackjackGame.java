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
    // Starting balance for a new player
    private static final int STARTING_BALANCE = 100;

    // Deck used for dealing cards
    private final Deck deck;
    // Player and dealer instances
    private final Player player;
    private final Player dealer;
    // Scanner for reading user input
    private final Scanner scanner;

    // Current bet placed by the player for this round
    private int currentBet;

    // Statistics: rounds won, lost, and tied
    private int wins;
    private int losses;
    private int ties;

    /**
     * Constructor for BlackjackGame class.
     * Initializes the deck, player, dealer, and scanner for input.
     */
    public BlackjackGame() {
        deck = new Deck();
        player = new Player("Player", STARTING_BALANCE);
        dealer = new Player("Dealer");
        scanner = new Scanner(System.in);
        currentBet = 0;
        wins = 0;
        losses = 0;
        ties = 0;
    }

    /**
     * Main game loop.
     * Runs the Blackjack game, handling rounds and user input for playing again.
     */
    public void play() {
        System.out.println("Welcome to Blackjack!");
        System.out.println("You start with $" + player.getBalance() + ". Good luck!");

        // Continue playing rounds until the user chooses to stop or runs out of money
        while (player.getBalance() > 0) {
            // Reset hands and shuffle deck for a new round
            player.clearHand();
            dealer.clearHand();
            deck.shuffle();

            // Player places a bet before cards are dealt
            placeBet();

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

            // Determine and display the winner, and settle the bet
            determineWinner();

            // Stop if player is out of money
            if (player.getBalance() == 0) {
                System.out.println("You're out of money. Game over!");
                break;
            }

            // Ask if the user wants to play another round
            System.out.print("\nPlay again? (y/n): ");
            String response = scanner.nextLine().trim().toLowerCase();
            if (!response.equals("y")) {
                break;
            }
        }

        // Show session statistics
        printStats();
        System.out.println("Thanks for coming out!");
        scanner.close();
    }

    /**
     * Prompts the player to place a bet for the current round.
     * Validates that the bet is within the player's available balance.
     */
    private void placeBet() {
        while (true) {
            System.out.print("\nYour balance: $" + player.getBalance() + ". Place your bet: $");
            String input = scanner.nextLine().trim();
            try {
                int bet = Integer.parseInt(input);
                if (bet <= 0) {
                    System.out.println("Bet must be greater than $0.");
                } else if (bet > player.getBalance()) {
                    System.out.println("You don't have enough money. Max bet: $" + player.getBalance());
                } else {
                    currentBet = bet;
                    player.deductBalance(bet);
                    System.out.println("Bet placed: $" + currentBet);
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    /**
     * Manages the player's turn.
     * Allows the player to hit or stand until they bust or stand.
     */
    private void playerTurn() {
        while (player.calculateScore() <= 21) {
            System.out.print("Do you want to hit or stand? (h/s): ");
            String choice = scanner.nextLine().trim().toLowerCase();
            if (choice.equals("h")) {
                player.addCard(deck.deal());
                System.out.println("Your hand: " + player.getHand() + " (Score: " + player.calculateScore() + ")");
            } else if (choice.equals("s")) {
                break; // Player stands, end turn
            } else {
                System.out.println("Invalid input. Please enter 'h' to hit or 's' to stand.");
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
     * Settles the current bet by adjusting the player's balance.
     */
    private void determineWinner() {
        int playerScore = player.calculateScore();
        int dealerScore = dealer.calculateScore();

        System.out.println("\nFinal Scores -> You: " + playerScore + ", Dealer: " + dealerScore);

        // Check win/lose conditions and update balance and stats
        if (playerScore > 21) {
            System.out.println("Bust! You lose $" + currentBet + ".");
            losses++;
        } else if (dealerScore > 21) {
            System.out.println("Dealer busts! You win $" + currentBet + "!");
            player.addBalance(currentBet * 2);
            wins++;
        } else if (playerScore > dealerScore) {
            System.out.println("You win $" + currentBet + "!");
            player.addBalance(currentBet * 2);
            wins++;
        } else if (dealerScore > playerScore) {
            System.out.println("Dealer wins. You lose $" + currentBet + ".");
            losses++;
        } else {
            System.out.println("It's a tie! Your bet is returned.");
            player.addBalance(currentBet);
            ties++;
        }
    }

    /**
     * Prints session statistics: total rounds and win/loss/tie breakdown.
     */
    private void printStats() {
        int total = wins + losses + ties;
        System.out.println("\n--- Session Statistics ---");
        System.out.println("Rounds played: " + total);
        System.out.println("Wins:   " + wins);
        System.out.println("Losses: " + losses);
        System.out.println("Ties:   " + ties);
        System.out.println("Final balance: $" + player.getBalance());
        System.out.println("--------------------------");
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
