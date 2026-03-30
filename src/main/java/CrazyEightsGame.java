import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Crazy Eights card game with a Swing GUI.
 *
 * <p>Rules: 5 cards are dealt to each player; one card is placed face-up to start
 * the discard pile. On your turn, play a card that matches the top discard's suit
 * OR rank. Eights are wild – when you play an 8 you choose the next suit. If you
 * cannot play, draw from the draw pile. First to empty their hand wins.</p>
 *
 * <p>Difficulty levels affect the CPU opponent's strategy:
 * <ul>
 *   <li>EASY   – CPU plays a random legal card.</li>
 *   <li>MEDIUM – CPU prefers to play cards it has the most of (empties fastest).</li>
 *   <li>HARD   – CPU tries to play an 8 only when it will benefit most, and
 *                chooses the suit that maximises future plays.</li>
 * </ul>
 * </p>
 *
 * @author Tom Burchell
 * @version 1.0
 */
public class CrazyEightsGame extends JFrame {

    private static final String[] SUITS = {"Hearts", "Diamonds", "Clubs", "Spades"};
    private static final String[] RANKS =
        {"2","3","4","5","6","7","8","9","10","J","Q","K","A"};

    private final Difficulty difficulty;
    private final Random random = new Random();

    // Game state
    final List<Card> drawPile    = new ArrayList<>();
    final List<Card> discardPile = new ArrayList<>();
    final List<Card> playerHand  = new ArrayList<>();
    final List<Card> cpuHand     = new ArrayList<>();

    /** The "active" suit when an 8 is on top. */
    String currentSuit = null;

    // UI components
    private JLabel topCardLabel;
    private JLabel currentSuitLabel;
    private JLabel drawPileLabel;
    private JLabel cpuHandLabel;
    private JPanel playerHandPanel;
    private JTextArea logArea;
    private JButton drawButton;
    private JButton newGameButton;

    /**
     * Creates a new Crazy Eights game window.
     *
     * @param difficulty The difficulty level
     */
    public CrazyEightsGame(Difficulty difficulty) {
        this.difficulty = difficulty;
        setTitle("Crazy Eights  —  " + difficulty.getLabel());
        setSize(720, 540);
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

        JLabel title = new JLabel("CRAZY EIGHTS ♦", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 26));
        title.setForeground(new Color(212, 175, 55));
        root.add(title, BorderLayout.NORTH);

        // Centre layout
        JPanel centre = new JPanel(new BorderLayout(8, 8));
        centre.setOpaque(false);

        // Top: table info
        JPanel tableRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 4));
        tableRow.setOpaque(false);

        topCardLabel    = tableInfoLabel("Top card: ?");
        currentSuitLabel = tableInfoLabel("Suit: ?");
        drawPileLabel   = tableInfoLabel("Draw pile: ?");
        cpuHandLabel    = tableInfoLabel("CPU hand: ?");

        tableRow.add(topCardLabel);
        tableRow.add(currentSuitLabel);
        tableRow.add(drawPileLabel);
        tableRow.add(cpuHandLabel);
        centre.add(tableRow, BorderLayout.NORTH);

        // Log
        logArea = new JTextArea(8, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(new Color(20, 60, 30));
        logArea.setForeground(Color.WHITE);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(logArea);
        centre.add(scroll, BorderLayout.CENTER);

        root.add(centre, BorderLayout.CENTER);

        // Bottom: player hand
        JPanel bottom = new JPanel(new BorderLayout(4, 4));
        bottom.setOpaque(false);

        JLabel handTitle = new JLabel("Your hand – click a card to play it:", SwingConstants.CENTER);
        handTitle.setForeground(Color.WHITE);
        handTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        bottom.add(handTitle, BorderLayout.NORTH);

        playerHandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 4));
        playerHandPanel.setOpaque(false);
        bottom.add(playerHandPanel, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 2));
        btnRow.setOpaque(false);

        drawButton = new JButton("Draw a card");
        drawButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        drawButton.setFocusPainted(false);
        drawButton.addActionListener(e -> playerDraw());

        newGameButton = new JButton("New Game");
        newGameButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        newGameButton.setFocusPainted(false);
        newGameButton.setEnabled(false);
        newGameButton.addActionListener(e -> startGame());

        btnRow.add(drawButton);
        btnRow.add(newGameButton);
        bottom.add(btnRow, BorderLayout.SOUTH);

        root.add(bottom, BorderLayout.SOUTH);
    }

    private JLabel tableInfoLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setForeground(new Color(212, 175, 55));
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setBorder(BorderFactory.createLineBorder(new Color(212, 175, 55), 1));
        lbl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(212, 175, 55), 1),
            new EmptyBorder(2, 6, 2, 6)
        ));
        return lbl;
    }

    // ── Game logic ────────────────────────────────────────────────────

    /**
     * Starts (or restarts) the game.
     */
    void startGame() {
        drawPile.clear();
        discardPile.clear();
        playerHand.clear();
        cpuHand.clear();
        currentSuit = null;
        drawButton.setEnabled(true);
        newGameButton.setEnabled(false);

        // Build full deck
        int[] values = {2,3,4,5,6,7,8,9,10,10,10,10,11};
        for (String suit : SUITS) {
            for (int i = 0; i < RANKS.length; i++) {
                drawPile.add(new Card(RANKS[i], suit, values[i]));
            }
        }
        Collections.shuffle(drawPile);

        // Deal 5 each
        for (int i = 0; i < 5; i++) {
            playerHand.add(drawPile.remove(0));
            cpuHand.add(drawPile.remove(0));
        }

        // First discard card – must not be an 8
        Card first;
        do {
            first = drawPile.remove(0);
        } while (first.getRank().equals("8"));
        discardPile.add(first);
        currentSuit = first.getSuit();

        log("=== New Game (" + difficulty.getLabel() + ") ===");
        log("Starting card: " + first);
        refreshUI();
    }

    /**
     * Player plays a card from their hand.
     *
     * @param card The card to play
     */
    void playerPlay(Card card) {
        if (!isLegal(card)) {
            log("You can't play " + card + " – must match suit (" + currentSuit
                + ") or rank (" + topCard().getRank() + "), or play an 8.");
            return;
        }

        playerHand.remove(card);
        discardPile.add(card);
        log("You play: " + card);

        if (card.getRank().equals("8")) {
            String chosen = playerChooseSuit();
            currentSuit = chosen;
            log("You choose suit: " + currentSuit);
        } else {
            currentSuit = card.getSuit();
        }

        refreshUI();

        if (playerHand.isEmpty()) {
            endGame("You");
            return;
        }

        SwingUtilities.invokeLater(this::cpuTurn);
    }

    /**
     * Player draws a card from the draw pile.
     */
    private void playerDraw() {
        replenishIfNeeded();
        if (drawPile.isEmpty()) {
            log("Draw pile is empty – you must pass.");
        } else {
            Card drawn = drawPile.remove(0);
            playerHand.add(drawn);
            log("You draw: " + drawn);
        }
        refreshUI();
        SwingUtilities.invokeLater(this::cpuTurn);
    }

    /**
     * Prompts the player to choose a suit when playing an 8.
     */
    private String playerChooseSuit() {
        String[] options = {"Hearts ♥", "Diamonds ♦", "Clubs ♣", "Spades ♠"};
        int choice = JOptionPane.showOptionDialog(this,
            "You played an 8 – choose a suit:",
            "Choose Suit",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null, options, options[0]);
        return SUITS[Math.max(choice, 0)];
    }

    /**
     * CPU plays its turn.
     */
    private void cpuTurn() {
        if (cpuHand.isEmpty()) {
            endGame("CPU");
            return;
        }

        List<Card> legal = cpuHand.stream()
            .filter(this::isLegal)
            .collect(Collectors.toList());

        if (legal.isEmpty()) {
            replenishIfNeeded();
            if (!drawPile.isEmpty()) {
                Card drawn = drawPile.remove(0);
                cpuHand.add(drawn);
                log("CPU draws a card.");
            } else {
                log("CPU passes (empty draw pile).");
            }
        } else {
            Card chosen = cpuChooseCard(legal);
            cpuHand.remove(chosen);
            discardPile.add(chosen);
            log("CPU plays: " + chosen);

            if (chosen.getRank().equals("8")) {
                currentSuit = cpuChooseSuit();
                log("CPU changes suit to: " + currentSuit);
            } else {
                currentSuit = chosen.getSuit();
            }
        }

        refreshUI();

        if (cpuHand.isEmpty()) {
            endGame("CPU");
        }
    }

    /**
     * CPU selects a card to play based on difficulty.
     */
    Card cpuChooseCard(List<Card> legal) {
        return cpuChooseCard(legal, cpuHand, difficulty, random);
    }

    /**
     * Static helper: CPU selects a card to play.
     *
     * @param legal    Legal cards the CPU can play
     * @param cpuHand  CPU's full hand (used for MEDIUM/HARD strategy)
     * @param diff     The difficulty level
     * @param rng      Random instance
     * @return The card the CPU will play
     */
    static Card cpuChooseCard(List<Card> legal, List<Card> cpuHand, Difficulty diff, Random rng) {
        switch (diff) {
            case EASY -> {
                return legal.get(rng.nextInt(legal.size()));
            }
            case MEDIUM -> {
                // Prefer non-8s; play the rank we have the most of
                List<Card> nonEights = legal.stream()
                    .filter(c -> !c.getRank().equals("8"))
                    .collect(java.util.stream.Collectors.toList());
                List<Card> pool = nonEights.isEmpty() ? legal : nonEights;
                return pool.stream().max(java.util.Comparator.comparingLong(c ->
                    cpuHand.stream().filter(x -> x.getRank().equals(c.getRank())).count()))
                    .orElse(pool.get(0));
            }
            case HARD -> {
                // Save 8s for when we have no other option
                List<Card> nonEights = legal.stream()
                    .filter(c -> !c.getRank().equals("8"))
                    .collect(java.util.stream.Collectors.toList());
                if (!nonEights.isEmpty()) {
                    // Play the card that leaves us with the most remaining legals next turn
                    return nonEights.stream().max(java.util.Comparator.comparingLong(c ->
                        cpuHand.stream()
                            .filter(x -> !x.equals(c) && (x.getSuit().equals(c.getSuit())
                                || x.getRank().equals(c.getRank()) || x.getRank().equals("8")))
                            .count()))
                        .orElse(nonEights.get(0));
                }
                return legal.get(0); // Must play an 8
            }
            default -> {
                return legal.get(rng.nextInt(legal.size()));
            }
        }
    }

    /**
     * CPU chooses a suit when playing an 8.
     */
    private String cpuChooseSuit() {
        // Always choose the suit we have the most of
        return Arrays.stream(SUITS)
            .max(Comparator.comparingLong(s ->
                cpuHand.stream().filter(c -> c.getSuit().equals(s)).count()))
            .orElse("Hearts");
    }

    /**
     * Returns true if the card is legal to play given the current top-of-discard.
     */
    boolean isLegal(Card card) {
        if (card.getRank().equals("8")) return true;
        Card top = topCard();
        if (top == null) return true;
        return card.getSuit().equals(currentSuit) || card.getRank().equals(top.getRank());
    }

    /**
     * Static helper: returns true if {@code card} is legal to play.
     *
     * @param card        The card to test
     * @param currentSuit The active suit (may differ from top card if an 8 was played)
     * @param topRank     The rank of the top discard card (null means any card is legal)
     * @return true if the card can be played
     */
    static boolean isLegalStatic(Card card, String currentSuit, String topRank) {
        if (card.getRank().equals("8")) return true;
        if (topRank == null || currentSuit == null) return true;
        return card.getSuit().equals(currentSuit) || card.getRank().equals(topRank);
    }

    private Card topCard() {
        return discardPile.isEmpty() ? null : discardPile.get(discardPile.size() - 1);
    }

    /** Reshuffles discards into draw pile (keeping top card) if draw pile is empty. */
    private void replenishIfNeeded() {
        if (drawPile.isEmpty() && discardPile.size() > 1) {
            Card top = discardPile.remove(discardPile.size() - 1);
            drawPile.addAll(discardPile);
            discardPile.clear();
            discardPile.add(top);
            Collections.shuffle(drawPile);
            log("(Draw pile reshuffled from discards.)");
        }
    }

    private void endGame(String winner) {
        drawButton.setEnabled(false);
        newGameButton.setEnabled(true);
        String msg = winner.equals("You")
            ? "🏆 You win – your hand is empty!"
            : "CPU wins – its hand is empty. Better luck next time!";
        log("=== Game Over ===");
        log(msg);
        JOptionPane.showMessageDialog(this, msg, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── UI helpers ────────────────────────────────────────────────────

    private void refreshUI() {
        Card top = topCard();
        topCardLabel.setText("Top: " + (top != null ? top.getRank() + " " + suitSymbol(top.getSuit()) : "?"));
        currentSuitLabel.setText("Suit: " + (currentSuit != null ? currentSuit + " " + suitSymbol(currentSuit) : "?"));
        drawPileLabel.setText("Draw: " + drawPile.size());
        cpuHandLabel.setText("CPU: " + cpuHand.size() + " cards");

        playerHandPanel.removeAll();
        for (Card card : playerHand) {
            boolean legal = isLegal(card);
            JButton btn = new JButton("<html><center>"
                + card.getRank() + "<br>" + suitSymbol(card.getSuit())
                + "</center></html>");
            btn.setPreferredSize(new Dimension(56, 58));
            btn.setFont(new Font("SansSerif", Font.BOLD, 12));
            btn.setBackground(legal ? Color.WHITE : new Color(180, 180, 180));
            btn.setForeground(isRedSuit(card.getSuit()) ? Color.RED : Color.BLACK);
            btn.setFocusPainted(false);
            btn.setEnabled(legal);
            btn.addActionListener(e -> playerPlay(card));
            playerHandPanel.add(btn);
        }
        playerHandPanel.revalidate();
        playerHandPanel.repaint();
    }

    private String suitSymbol(String suit) {
        return switch (suit) {
            case "Hearts"   -> "♥";
            case "Diamonds" -> "♦";
            case "Clubs"    -> "♣";
            case "Spades"   -> "♠";
            default         -> suit;
        };
    }

    private boolean isRedSuit(String suit) {
        return suit.equals("Hearts") || suit.equals("Diamonds");
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
