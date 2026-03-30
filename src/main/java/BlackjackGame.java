/**
 * BlackjackGame class
 * Tom Burchell
 * W0516036
 * Date: 2026-02-13
 *
 * Features:
 *   - Multi-player support (1–4 human players)
 *   - Split: split two equal-rank cards into separate hands
 *   - Double Down: double the bet and receive exactly one more card
 *   - Insurance: side bet offered when dealer shows an Ace
 *   - Save / Load: persist session state between runs
 */
import java.io.*;
import java.util.*;

public class BlackjackGame {
    // Starting balance for a new player
    static final int STARTING_BALANCE = 100;

    // Maximum number of human players allowed at the table
    private static final int MAX_PLAYERS = 4;

    // File used to persist session state
    private static final String SAVE_FILE = "blackjack_save.dat";

    // Deck shared by all players at the table
    private final Deck deck;

    // Human players and the dealer
    private final List<Player> players;
    private final Player dealer;

    // Scanner for all user input
    private final Scanner scanner;

    // Per-player statistics: [wins, losses, ties]
    private final Map<Player, int[]> stats;

    // -----------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------

    /**
     * Creates a game with a single player using the default starting balance.
     * Intended for backwards-compatible use and the {@code main} entry point.
     */
    public BlackjackGame() {
        this(Collections.singletonList(new Player("Player", STARTING_BALANCE)));
    }

    /**
     * Creates a game with the given list of players.
     *
     * @param players the human players who will participate
     */
    public BlackjackGame(List<Player> players) {
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("At least one player is required.");
        }
        this.players  = new ArrayList<>(players);
        this.deck     = new Deck();
        this.dealer   = new Player("Dealer");
        this.scanner  = new Scanner(System.in);
        this.stats    = new LinkedHashMap<>();
        for (Player p : this.players) {
            stats.put(p, new int[3]); // index 0=wins, 1=losses, 2=ties
        }
    }

    // -----------------------------------------------------------------------
    // Main game loop
    // -----------------------------------------------------------------------

    /**
     * Runs the full Blackjack session.
     * Continues round-by-round until all players are out of money or
     * decline to play another round.
     */
    public void play() {
        System.out.println("Welcome to Blackjack!");
        for (Player p : players) {
            System.out.println(p.getName() + " starts with $" + p.getBalance() + ".");
        }

        while (true) {
            // Remove players who have run out of money
            players.removeIf(p -> {
                if (p.getBalance() == 0) {
                    System.out.println(p.getName() + " is out of money and has left the table.");
                    return true;
                }
                return false;
            });

            if (players.isEmpty()) {
                System.out.println("All players are out of money. Game over!");
                break;
            }

            playRound();

            // Offer save after every round
            System.out.print("\nSave game? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                saveGame();
            }

            // Ask remaining players if they want to continue
            System.out.print("Play another round? (y/n): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
                break;
            }
        }

        printStats();
        System.out.println("Thanks for coming out!");
        scanner.close();
    }

    // -----------------------------------------------------------------------
    // Round orchestration
    // -----------------------------------------------------------------------

    /**
     * Runs one complete round: betting, dealing, insurance, player turns,
     * dealer turn, and settlement.
     */
    private void playRound() {
        // Reset all hands and reshuffle
        for (Player p : players) {
            p.clearHand();
        }
        dealer.clearHand();
        deck.shuffle();

        // ---- Betting phase ------------------------------------------------
        Map<Player, Integer> bets = new LinkedHashMap<>();
        for (Player p : players) {
            bets.put(p, placeBet(p));
        }

        // ---- Initial deal: 2 cards each -----------------------------------
        for (Player p : players) {
            p.addCard(deck.deal());
        }
        dealer.addCard(deck.deal());
        for (Player p : players) {
            p.addCard(deck.deal());
        }
        dealer.addCard(deck.deal());

        // Show initial state
        System.out.println("\nDealer's visible card: " + dealer.getHand().get(0));
        for (Player p : players) {
            System.out.println(p.getName() + "'s hand: " + p.getHand()
                    + " (Score: " + p.calculateScore() + ")");
        }

        // ---- Insurance phase (dealer shows Ace) ---------------------------
        Map<Player, Integer> insuranceBets = new LinkedHashMap<>();
        if (dealer.getHand().get(0).getRank().equals("A")) {
            for (Player p : players) {
                int ins = offerInsurance(p, bets.get(p));
                if (ins > 0) {
                    insuranceBets.put(p, ins);
                }
            }
        }

        // ---- Resolve insurance immediately if dealer has blackjack --------
        boolean dealerBlackjack = isDealerBlackjack();
        if (!insuranceBets.isEmpty()) {
            resolveInsurance(insuranceBets, dealerBlackjack);
        }

        // ---- If dealer has blackjack, end round early ---------------------
        if (dealerBlackjack) {
            System.out.println("\nDealer has Blackjack! " + dealer.getHand()
                    + " (Score: " + dealer.calculateScore() + ")");
            for (Player p : players) {
                int playerScore = p.calculateScore();
                int[] s = stats.get(p);
                if (playerScore == 21 && p.getHand().size() == 2) {
                    System.out.println(p.getName() + " also has Blackjack — it's a tie!");
                    p.addBalance(bets.get(p));
                    s[2]++;
                } else {
                    System.out.println(p.getName() + " loses $" + bets.get(p) + ".");
                    s[1]++;
                }
            }
            return;
        }

        // ---- Player turns -------------------------------------------------
        // Each player may have multiple hands after splitting.
        // We represent split hands as a List<List<Card>> per player, with a
        // corresponding list of bet amounts.
        Map<Player, List<List<Card>>> allHands = new LinkedHashMap<>();
        Map<Player, List<Integer>> allBets = new LinkedHashMap<>();

        for (Player p : players) {
            // Seed with the player's initial hand
            List<Card> initialHand = new ArrayList<>(p.getHand());
            List<List<Card>> hands = new ArrayList<>();
            hands.add(initialHand);
            allHands.put(p, hands);

            List<Integer> handBets = new ArrayList<>();
            handBets.add(bets.get(p));
            allBets.put(p, handBets);

            playPlayerTurn(p, hands, handBets);
        }

        // ---- Dealer turn --------------------------------------------------
        // Dealer plays only if at least one player hand is still alive (not bust)
        boolean anyAlive = false;
        for (Player p : players) {
            for (List<Card> hand : allHands.get(p)) {
                if (scoreHand(hand) <= 21) {
                    anyAlive = true;
                    break;
                }
            }
        }
        if (anyAlive) {
            dealerTurn();
        }

        // ---- Settlement ---------------------------------------------------
        int dealerScore = dealer.calculateScore();
        System.out.println("\nDealer's final hand: " + dealer.getHand()
                + " (Score: " + dealerScore + ")");

        for (Player p : players) {
            List<List<Card>> hands = allHands.get(p);
            List<Integer> handBets = allBets.get(p);
            for (int i = 0; i < hands.size(); i++) {
                String label = (hands.size() > 1) ? p.getName() + " (Hand " + (i + 1) + ")" : p.getName();
                settleHand(p, label, hands.get(i), handBets.get(i), dealerScore);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Betting helpers
    // -----------------------------------------------------------------------

    /**
     * Prompts a specific player to place a bet for the current round.
     *
     * @param p the player placing the bet
     * @return the validated bet amount (already deducted from balance)
     */
    private int placeBet(Player p) {
        while (true) {
            System.out.print("\n" + p.getName() + "'s balance: $" + p.getBalance()
                    + ". Place your bet: $");
            String input = scanner.nextLine().trim();
            try {
                int bet = Integer.parseInt(input);
                if (bet <= 0) {
                    System.out.println("Bet must be greater than $0.");
                } else if (bet > p.getBalance()) {
                    System.out.println("Not enough money. Max bet: $" + p.getBalance());
                } else {
                    p.deductBalance(bet);
                    System.out.println("Bet placed: $" + bet);
                    return bet;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    /**
     * Offers insurance to a player when the dealer is showing an Ace.
     * The player may wager up to half of their original bet.
     *
     * @param p           the player being offered insurance
     * @param originalBet the player's main bet for this round
     * @return the insurance amount wagered (0 if declined)
     */
    private int offerInsurance(Player p, int originalBet) {
        int maxInsurance = originalBet / 2;
        if (maxInsurance == 0 || p.getBalance() == 0) {
            return 0;
        }
        System.out.println("\nDealer shows an Ace. " + p.getName()
                + ", would you like insurance? (Max: $" + maxInsurance + ")");
        System.out.print("Enter insurance amount (0 to decline): $");
        while (true) {
            String input = scanner.nextLine().trim();
            try {
                int amount = Integer.parseInt(input);
                if (amount < 0) {
                    System.out.print("Enter a non-negative amount: $");
                } else if (amount == 0) {
                    System.out.println(p.getName() + " declines insurance.");
                    return 0;
                } else if (amount > maxInsurance) {
                    System.out.print("Max insurance is $" + maxInsurance + ". Try again: $");
                } else if (amount > p.getBalance()) {
                    System.out.print("Not enough balance. Max you can bet: $"
                            + Math.min(maxInsurance, p.getBalance()) + ": $");
                } else {
                    p.deductBalance(amount);
                    System.out.println(p.getName() + " takes insurance for $" + amount + ".");
                    return amount;
                }
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Enter a number: $");
            }
        }
    }

    /**
     * Resolves all insurance bets once it is known whether the dealer has blackjack.
     *
     * @param insuranceBets map of players to their insurance bet amounts
     * @param dealerHasBlackjack whether the dealer holds blackjack
     */
    private void resolveInsurance(Map<Player, Integer> insuranceBets, boolean dealerHasBlackjack) {
        System.out.println("\n--- Insurance Resolution ---");
        for (Map.Entry<Player, Integer> entry : insuranceBets.entrySet()) {
            Player p = entry.getKey();
            int ins = entry.getValue();
            if (dealerHasBlackjack) {
                // Insurance pays 2:1 (player gets back 3× the insurance bet)
                int payout = ins * 3;
                p.addBalance(payout);
                System.out.println(p.getName() + " wins insurance! +" + (ins * 2)
                        + " (balance: $" + p.getBalance() + ")");
            } else {
                System.out.println(p.getName() + " loses insurance bet of $" + ins + ".");
            }
        }
    }

    // -----------------------------------------------------------------------
    // Player turn
    // -----------------------------------------------------------------------

    /**
     * Manages a single player's turn, including split and double-down actions.
     * After a split the player plays each resulting hand sequentially.
     *
     * @param p        the player whose turn it is
     * @param hands    mutable list of that player's active hands (starts with one)
     * @param handBets mutable list of bets corresponding to each hand
     */
    private void playPlayerTurn(Player p, List<List<Card>> hands, List<Integer> handBets) {
        int handIndex = 0;
        while (handIndex < hands.size()) {
            List<Card> hand = hands.get(handIndex);
            int bet = handBets.get(handIndex);
            boolean firstAction = true;

            System.out.println("\n" + p.getName()
                    + (hands.size() > 1 ? " (Hand " + (handIndex + 1) + ")" : "")
                    + "'s turn:");

            while (scoreHand(hand) <= 21) {
                System.out.println("  Hand: " + hand + " (Score: " + scoreHand(hand) + ")");

                // Build the menu of available actions
                List<String> options = new ArrayList<>(Arrays.asList("h", "s"));
                StringBuilder prompt = new StringBuilder("  (h)it, (s)tand");

                // Double down: only on the first action with exactly 2 cards and enough balance
                boolean canDouble = firstAction && hand.size() == 2 && p.getBalance() >= bet;
                if (canDouble) {
                    options.add("d");
                    prompt.append(", (d)ouble down");
                }

                // Split: only on first action, 2 cards of same rank, enough balance
                boolean canSplit = firstAction && hand.size() == 2
                        && hand.get(0).getRank().equals(hand.get(1).getRank())
                        && p.getBalance() >= bet;
                if (canSplit) {
                    options.add("p");
                    prompt.append(", s(p)lit");
                }

                System.out.print(prompt + ": ");
                String choice = scanner.nextLine().trim().toLowerCase();

                if (choice.equals("h")) {
                    hand.add(deck.deal());
                    firstAction = false;
                } else if (choice.equals("s")) {
                    break;
                } else if (choice.equals("d") && canDouble) {
                    // Double down: double the bet, deal exactly one card, then stand
                    p.deductBalance(bet);
                    handBets.set(handIndex, bet * 2);
                    hand.add(deck.deal());
                    System.out.println("  Doubled down! New bet: $" + (bet * 2)
                            + " | Hand: " + hand + " (Score: " + scoreHand(hand) + ")");
                    break; // Automatically stand after double down
                } else if (choice.equals("p") && canSplit) {
                    // Split: create two new hands, each with one original card
                    Card card1 = hand.get(0);
                    Card card2 = hand.get(1);

                    List<Card> splitHand1 = new ArrayList<>(Arrays.asList(card1, deck.deal()));
                    List<Card> splitHand2 = new ArrayList<>(Arrays.asList(card2, deck.deal()));

                    // Deduct the extra bet for the second hand
                    p.deductBalance(bet);

                    // Replace the current hand with the two split hands
                    hands.set(handIndex, splitHand1);
                    hands.add(handIndex + 1, splitHand2);
                    handBets.set(handIndex, bet);
                    handBets.add(handIndex + 1, bet);

                    System.out.println("  Split! Playing Hand 1 first.");
                    // Re-read the (now replaced) hand and restart the loop for this index
                    hand = hands.get(handIndex);
                    bet  = handBets.get(handIndex);
                    firstAction = true;
                    // Continue looping for the current hand index
                } else {
                    System.out.println("  Invalid choice. Try again.");
                }
            }

            if (scoreHand(hand) > 21) {
                System.out.println("  " + p.getName()
                        + (hands.size() > 1 ? " (Hand " + (handIndex + 1) + ")" : "")
                        + " busts with " + scoreHand(hand) + "!");
            }

            handIndex++;
        }
    }

    // -----------------------------------------------------------------------
    // Dealer turn
    // -----------------------------------------------------------------------

    /**
     * Manages the dealer's turn.
     * The dealer reveals the hidden card and hits until reaching at least 17.
     */
    private void dealerTurn() {
        System.out.println("\nDealer reveals: " + dealer.getHand()
                + " (Score: " + dealer.calculateScore() + ")");
        while (dealer.calculateScore() < 17) {
            dealer.addCard(deck.deal());
            System.out.println("Dealer draws: " + dealer.getHand().get(dealer.getHand().size() - 1));
            System.out.println("Dealer's hand: " + dealer.getHand()
                    + " (Score: " + dealer.calculateScore() + ")");
        }
    }

    // -----------------------------------------------------------------------
    // Settlement
    // -----------------------------------------------------------------------

    /**
     * Settles a single hand against the dealer's final score.
     *
     * @param p           the player who owns this hand
     * @param label       display name (includes hand number for split hands)
     * @param hand        the cards in this hand
     * @param bet         the bet amount for this hand
     * @param dealerScore the dealer's final score
     */
    private void settleHand(Player p, String label, List<Card> hand, int bet, int dealerScore) {
        int handScore = scoreHand(hand);
        int[] s = stats.get(p);

        System.out.println("\n" + label + ": " + hand + " (Score: " + handScore + ") vs Dealer: " + dealerScore);

        if (handScore > 21) {
            System.out.println(label + " busts — loses $" + bet + ".");
            s[1]++;
        } else if (dealerScore > 21) {
            System.out.println(label + " wins $" + bet + " (dealer bust)!");
            p.addBalance(bet * 2);
            s[0]++;
        } else if (handScore > dealerScore) {
            System.out.println(label + " wins $" + bet + "!");
            p.addBalance(bet * 2);
            s[0]++;
        } else if (dealerScore > handScore) {
            System.out.println(label + " loses $" + bet + ".");
            s[1]++;
        } else {
            System.out.println(label + " ties — bet returned.");
            p.addBalance(bet);
            s[2]++;
        }
    }

    // -----------------------------------------------------------------------
    // Utility helpers
    // -----------------------------------------------------------------------

    /**
     * Calculates the Blackjack score of an arbitrary list of cards,
     * using the same Ace adjustment logic as {@link Player#calculateScore()}.
     *
     * @param hand the cards to score
     * @return the best score that does not exceed 21 (or the lowest bust value)
     */
    int scoreHand(List<Card> hand) {
        int score = 0;
        int aceCount = 0;
        for (Card c : hand) {
            score += c.getValue();
            if (c.getRank().equals("A")) {
                aceCount++;
            }
        }
        while (score > 21 && aceCount > 0) {
            score -= 10;
            aceCount--;
        }
        return score;
    }

    /**
     * Returns {@code true} if the dealer holds a natural Blackjack
     * (exactly Ace + 10-value card with exactly two cards).
     *
     * @return whether the dealer has blackjack
     */
    private boolean isDealerBlackjack() {
        List<Card> dHand = dealer.getHand();
        return dHand.size() == 2 && dealer.calculateScore() == 21;
    }

    // -----------------------------------------------------------------------
    // Statistics
    // -----------------------------------------------------------------------

    /**
     * Prints end-of-session statistics for every player.
     */
    private void printStats() {
        System.out.println("\n===== Session Statistics =====");
        for (Player p : players) {
            int[] s = stats.get(p);
            // Stats may also contain entries for players who left mid-session
            if (s == null) continue;
            int total = s[0] + s[1] + s[2];
            System.out.println(p.getName() + ":");
            System.out.println("  Rounds : " + total);
            System.out.println("  Wins   : " + s[0]);
            System.out.println("  Losses : " + s[1]);
            System.out.println("  Ties   : " + s[2]);
            System.out.println("  Balance: $" + p.getBalance());
        }
        System.out.println("==============================");
    }

    // -----------------------------------------------------------------------
    // Accessors (package-visible for tests)
    // -----------------------------------------------------------------------

    /**
     * Returns an unmodifiable view of the current list of players.
     *
     * @return list of players in this game
     */
    List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    /**
     * Returns the win/loss/tie statistics for the given player as an int array
     * of length 3: {@code [wins, losses, ties]}.
     *
     * @param p the player whose stats are requested
     * @return stat array, or {@code null} if the player is not in this game
     */
    int[] getPlayerStats(Player p) {
        return stats.get(p);
    }

    // -----------------------------------------------------------------------
    // Save / Load
    // -----------------------------------------------------------------------

    /**
     * Serializes the current session state to {@value #SAVE_FILE}.
     * Saves player names, balances, and statistics.
     */
    void saveGame() {
        List<String> names    = new ArrayList<>();
        List<Integer> balances = new ArrayList<>();
        List<int[]> statsList  = new ArrayList<>();

        for (Player p : players) {
            names.add(p.getName());
            balances.add(p.getBalance());
            int[] s = stats.get(p);
            statsList.add(new int[]{s[0], s[1], s[2]});
        }

        GameState state = new GameState(names, balances, statsList);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(state);
            System.out.println("Game saved to " + SAVE_FILE + ".");
        } catch (IOException e) {
            System.out.println("Could not save game: " + e.getMessage());
        }
    }

    /**
     * Attempts to load a previously saved session from {@value #SAVE_FILE}.
     *
     * @return a restored {@link BlackjackGame} instance, or {@code null} if loading fails
     */
    static BlackjackGame loadGame() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            GameState state = (GameState) ois.readObject();
            List<Player> loadedPlayers = new ArrayList<>();
            for (int i = 0; i < state.playerNames.size(); i++) {
                loadedPlayers.add(new Player(state.playerNames.get(i), state.playerBalances.get(i)));
            }
            BlackjackGame game = new BlackjackGame(loadedPlayers);
            for (int i = 0; i < loadedPlayers.size(); i++) {
                int[] src = state.playerStats.get(i);
                int[] dst = game.stats.get(loadedPlayers.get(i));
                dst[0] = src[0];
                dst[1] = src[1];
                dst[2] = src[2];
            }
            System.out.println("Game loaded from " + SAVE_FILE + ".");
            return game;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Could not load save file: " + e.getMessage());
            return null;
        }
    }

    // -----------------------------------------------------------------------
    // Entry point
    // -----------------------------------------------------------------------

    /**
     * Prompts for number of players (or offers to load a saved game),
     * then starts the game loop.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Offer to load an existing save
        File saveFile = new File(SAVE_FILE);
        if (saveFile.exists()) {
            System.out.print("A saved game was found. Load it? (y/n): ");
            if (sc.nextLine().trim().equalsIgnoreCase("y")) {
                BlackjackGame loaded = loadGame();
                if (loaded != null) {
                    loaded.play();
                    return;
                }
                System.out.println("Starting a new game instead.");
            }
        }

        // Ask for number of players
        int numPlayers = 1;
        System.out.print("How many players? (1–" + MAX_PLAYERS + "): ");
        while (true) {
            String input = sc.nextLine().trim();
            try {
                numPlayers = Integer.parseInt(input);
                if (numPlayers >= 1 && numPlayers <= MAX_PLAYERS) {
                    break;
                }
                System.out.print("Please enter a number between 1 and " + MAX_PLAYERS + ": ");
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Enter a number: ");
            }
        }

        // Collect player names
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= numPlayers; i++) {
            System.out.print("Enter name for Player " + i + ": ");
            String name = sc.nextLine().trim();
            if (name.isEmpty()) {
                name = "Player " + i;
            }
            players.add(new Player(name, STARTING_BALANCE));
        }

        new BlackjackGame(players).play();
    }
}

/*
 * Citations:
 * Kevin's Guides. (n.d.). Console Blackjack in Java (21). https://kevinsguides.com/guides/code/java/blackjack
 * JetBrains. (2026). IntelliJ IDEA Ultimate [Computer software]. JetBrains s.r.o. https://www.jetbrains.com/idea/
 * Oracle. (2026). Java Platform, Standard Edition 17 [Computer software]. Oracle Corporation. https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
 */
