import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Go Fish card game with a Swing GUI.
 *
 * <p>Rules: Each player is dealt 7 cards. On your turn, ask the opponent for a rank.
 * If they have it, they hand over all matching cards; otherwise "Go Fish" (draw from
 * the pool). Collecting all 4 cards of a rank forms a "book" and is placed aside.
 * The player with the most books when the pool is empty wins.</p>
 *
 * <p>Difficulty levels affect the CPU opponent's strategy:
 * <ul>
 *   <li>EASY   – CPU asks for a random rank regardless of its hand.</li>
 *   <li>MEDIUM – CPU asks only for ranks it already holds.</li>
 *   <li>HARD   – CPU asks for ranks it holds, and also remembers what the player
 *                asked for in previous turns to make educated guesses.</li>
 * </ul>
 * </p>
 *
 * @author Tom Burchell
 * @version 1.0
 */
public class GoFishGame extends JFrame {

    private static final String[] RANKS =
        {"2","3","4","5","6","7","8","9","10","J","Q","K","A"};

    private final Difficulty difficulty;
    private final Random random = new Random();

    // Game state
    final List<Card> playerHand = new ArrayList<>();
    final List<Card> cpuHand    = new ArrayList<>();
    final List<Card> pool       = new ArrayList<>();
    final List<String> playerBooks = new ArrayList<>();
    final List<String> cpuBooks    = new ArrayList<>();

    /** Ranks the CPU has seen the player request (used by HARD AI). */
    private final Set<String> playerAskedRanks = new HashSet<>();

    // UI
    private JPanel playerHandPanel;
    private JLabel cpuHandLabel;
    private JLabel poolLabel;
    private JTextArea logArea;
    private JLabel booksLabel;
    private JButton goFishButton;

    /**
     * Creates a new Go Fish game window.
     *
     * @param difficulty The difficulty level
     */
    public GoFishGame(Difficulty difficulty) {
        this.difficulty = difficulty;
        setTitle("Go Fish  —  " + difficulty.getLabel());
        setSize(700, 560);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
        startGame();
    }

    // ── UI ────────────────────────────────────────────────────────────

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(new Color(35, 100, 50));
        root.setBorder(new EmptyBorder(12, 16, 12, 16));
        setContentPane(root);

        // Title
        JLabel title = new JLabel("GO FISH ♣", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 26));
        title.setForeground(new Color(212, 175, 55));
        root.add(title, BorderLayout.NORTH);

        // Centre: info + log
        JPanel centre = new JPanel(new BorderLayout(6, 6));
        centre.setOpaque(false);

        // Status row
        JPanel statusRow = new JPanel(new GridLayout(1, 3, 8, 0));
        statusRow.setOpaque(false);

        cpuHandLabel = infoLabel("CPU hand: 7");
        poolLabel    = infoLabel("Pool: 38");
        booksLabel   = infoLabel("Books  You: 0 | CPU: 0");

        statusRow.add(cpuHandLabel);
        statusRow.add(poolLabel);
        statusRow.add(booksLabel);
        centre.add(statusRow, BorderLayout.NORTH);

        // Log
        logArea = new JTextArea(8, 40);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(new Color(20, 60, 30));
        logArea.setForeground(Color.WHITE);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(logArea);
        centre.add(scroll, BorderLayout.CENTER);

        root.add(centre, BorderLayout.CENTER);

        // Bottom: player hand + ask button
        JPanel bottom = new JPanel(new BorderLayout(6, 6));
        bottom.setOpaque(false);

        JLabel handTitle = new JLabel("Your hand – click a card to ask for that rank:", SwingConstants.CENTER);
        handTitle.setForeground(Color.WHITE);
        handTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        bottom.add(handTitle, BorderLayout.NORTH);

        playerHandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        playerHandPanel.setOpaque(false);
        bottom.add(playerHandPanel, BorderLayout.CENTER);

        goFishButton = new JButton("New Game");
        goFishButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        goFishButton.setFocusPainted(false);
        goFishButton.setEnabled(false);
        goFishButton.addActionListener(e -> startGame());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setOpaque(false);
        btnRow.add(goFishButton);
        bottom.add(btnRow, BorderLayout.SOUTH);

        root.add(bottom, BorderLayout.SOUTH);
    }

    private JLabel infoLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setForeground(new Color(212, 175, 55));
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        return lbl;
    }

    // ── Game logic ────────────────────────────────────────────────────

    /**
     * Starts (or restarts) the game.
     */
    void startGame() {
        playerHand.clear();
        cpuHand.clear();
        pool.clear();
        playerBooks.clear();
        cpuBooks.clear();
        playerAskedRanks.clear();
        goFishButton.setEnabled(false);

        // Build and shuffle a full deck
        String[] suits  = {"Hearts", "Diamonds", "Clubs", "Spades"};
        int[]    values = {2,3,4,5,6,7,8,9,10,10,10,10,11};
        for (String suit : suits) {
            for (int i = 0; i < RANKS.length; i++) {
                pool.add(new Card(RANKS[i], suit, values[i]));
            }
        }
        Collections.shuffle(pool);

        // Deal 7 cards each
        for (int i = 0; i < 7; i++) {
            playerHand.add(pool.remove(0));
            cpuHand.add(pool.remove(0));
        }

        checkBooks(playerHand, playerBooks, "You");
        checkBooks(cpuHand, cpuBooks, "CPU");

        log("=== New Game (" + difficulty.getLabel() + ") ===");
        log("7 cards dealt to each player. Pool has " + pool.size() + " cards.");
        refreshUI();
    }

    /**
     * Handles the player asking for a specific rank.
     *
     * @param rank The rank the player is asking for
     */
    void playerAsk(String rank) {
        if (playerHand.isEmpty()) return;

        playerAskedRanks.add(rank);
        log("You ask: \"Does anyone have a " + rank + "?\"");

        List<Card> given = takeCards(cpuHand, rank);
        if (!given.isEmpty()) {
            playerHand.addAll(given);
            log("CPU hands over " + given.size() + " " + rank + "(s)!");
            checkBooks(playerHand, playerBooks, "You");
        } else {
            log("CPU says: Go Fish!");
            if (!pool.isEmpty()) {
                Card drawn = pool.remove(0);
                playerHand.add(drawn);
                log("You draw: " + drawn);
                checkBooks(playerHand, playerBooks, "You");
            } else {
                log("(Pool is empty – no card to draw)");
            }
        }

        refreshUI();

        if (isGameOver()) {
            endGame();
            return;
        }

        // CPU's turn
        SwingUtilities.invokeLater(this::cpuTurn);
    }

    /**
     * CPU takes its turn.
     */
    private void cpuTurn() {
        if (cpuHand.isEmpty()) {
            endGame();
            return;
        }

        String rank = cpuChooseRank();
        log("CPU asks: \"Do you have a " + rank + "?\"");

        List<Card> given = takeCards(playerHand, rank);
        if (!given.isEmpty()) {
            cpuHand.addAll(given);
            log("You hand over " + given.size() + " " + rank + "(s)!");
            checkBooks(cpuHand, cpuBooks, "CPU");
        } else {
            log("You say: Go Fish!");
            if (!pool.isEmpty()) {
                Card drawn = pool.remove(0);
                cpuHand.add(drawn);
                log("CPU draws a card.");
                checkBooks(cpuHand, cpuBooks, "CPU");
            } else {
                log("(Pool is empty – no card to draw)");
            }
        }

        refreshUI();

        if (isGameOver()) {
            endGame();
        }
    }

    /**
     * The CPU chooses a rank to ask for, based on difficulty.
     */
    String cpuChooseRank() {
        if (cpuHand.isEmpty()) return RANKS[random.nextInt(RANKS.length)];

        switch (difficulty) {
            case EASY -> {
                // Ask for a random rank from all remaining ranks
                return RANKS[random.nextInt(RANKS.length)];
            }
            case MEDIUM -> {
                // Ask for a rank the CPU already holds
                return cpuHand.get(random.nextInt(cpuHand.size())).getRank();
            }
            case HARD -> {
                // Prefer ranks the player has previously asked for (and CPU holds)
                Set<String> cpuRanks = cpuHand.stream()
                    .map(Card::getRank).collect(Collectors.toSet());

                List<String> priorityRanks = playerAskedRanks.stream()
                    .filter(cpuRanks::contains).collect(Collectors.toList());

                if (!priorityRanks.isEmpty()) {
                    return priorityRanks.get(random.nextInt(priorityRanks.size()));
                }
                // Fallback: rank CPU holds with most cards
                return cpuRanks.stream()
                    .max(Comparator.comparingLong(r ->
                        cpuHand.stream().filter(c -> c.getRank().equals(r)).count()))
                    .orElse(cpuHand.get(0).getRank());
            }
            default -> {
                return cpuHand.get(random.nextInt(cpuHand.size())).getRank();
            }
        }
    }

    /**
     * Removes and returns all cards of the given rank from a hand.
     */
    static List<Card> takeCards(List<Card> hand, String rank) {
        List<Card> taken = hand.stream()
            .filter(c -> c.getRank().equals(rank))
            .collect(Collectors.toList());
        hand.removeAll(taken);
        return taken;
    }

    /**
     * Checks for complete books (4-of-a-kind) in the given hand, moves them to
     * the books list, and logs each book found.
     */
    void checkBooks(List<Card> hand, List<String> books, String owner) {
        int before = books.size();
        checkBooksStatic(hand, books);
        for (int i = before; i < books.size(); i++) {
            log(owner + " completes a book of " + books.get(i) + "s!");
        }
    }

    /**
     * Static helper: checks for complete books (4-of-a-kind) in the hand and
     * moves the rank name into books. Does not depend on any instance state.
     *
     * @param hand  The hand to examine (modified in-place)
     * @param books The list to append book ranks to
     */
    static void checkBooksStatic(List<Card> hand, List<String> books) {
        for (String rank : RANKS) {
            long count = hand.stream().filter(c -> c.getRank().equals(rank)).count();
            if (count == 4) {
                hand.removeIf(c -> c.getRank().equals(rank));
                books.add(rank);
            }
        }
    }

    private boolean isGameOver() {
        return (playerHand.isEmpty() && pool.isEmpty())
            || (cpuHand.isEmpty() && pool.isEmpty())
            || (pool.isEmpty() && playerHand.isEmpty() && cpuHand.isEmpty());
    }

    private void endGame() {
        int pBooks = playerBooks.size();
        int cBooks = cpuBooks.size();
        String result;
        if (pBooks > cBooks) {
            result = "🏆 You win with " + pBooks + " books vs CPU's " + cBooks + "!";
        } else if (cBooks > pBooks) {
            result = "CPU wins with " + cBooks + " books vs your " + pBooks + ". Better luck next time!";
        } else {
            result = "It's a tie! Both have " + pBooks + " books.";
        }
        log("=== Game Over ===");
        log(result);
        JOptionPane.showMessageDialog(this, result, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        goFishButton.setEnabled(true);
        goFishButton.setText("New Game");
    }

    // ── UI helpers ────────────────────────────────────────────────────

    private void refreshUI() {
        cpuHandLabel.setText("CPU hand: " + cpuHand.size());
        poolLabel.setText("Pool: " + pool.size());
        booksLabel.setText("Books  You: " + playerBooks.size() + " | CPU: " + cpuBooks.size());

        playerHandPanel.removeAll();
        // Group player's hand by rank for display
        Map<String, List<Card>> grouped = new LinkedHashMap<>();
        for (String r : RANKS) {
            for (Card c : playerHand) {
                if (c.getRank().equals(r)) {
                    grouped.computeIfAbsent(r, k -> new ArrayList<>()).add(c);
                }
            }
        }

        for (Map.Entry<String, List<Card>> entry : grouped.entrySet()) {
            String rank = entry.getKey();
            int count = entry.getValue().size();
            JButton cardBtn = new JButton("<html><center><b>" + rank + "</b><br>×" + count + "</center></html>");
            cardBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
            cardBtn.setPreferredSize(new Dimension(58, 56));
            cardBtn.setBackground(Color.WHITE);
            cardBtn.setFocusPainted(false);
            cardBtn.addActionListener(e -> playerAsk(rank));
            playerHandPanel.add(cardBtn);
        }

        playerHandPanel.revalidate();
        playerHandPanel.repaint();
    }

    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
