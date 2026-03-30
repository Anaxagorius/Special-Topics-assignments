import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * War card game with a Swing GUI.
 *
 * <p>Rules: The deck is split between two players. Each round both flip their top
 * card; the higher card wins both. Ties trigger "war" (3 face-down cards each, then
 * another flip). The player who collects all 52 cards wins.</p>
 *
 * <p>Difficulty levels:
 * <ul>
 *   <li>EASY   – Player receives the higher-value half of the deck.</li>
 *   <li>MEDIUM – Deck is split randomly (standard rules).</li>
 *   <li>HARD   – Computer receives the higher-value half of the deck.</li>
 * </ul>
 * </p>
 *
 * @author Tom Burchell
 * @version 1.0
 */
public class WarGame extends JFrame {

    // ── Card-rank order for War (Aces high) ──────────────────────────
    private static final String[] WAR_ORDER =
        {"2","3","4","5","6","7","8","9","10","J","Q","K","A"};

    private final Difficulty difficulty;

    final List<Card> playerDeck = new ArrayList<>();
    final List<Card> cpuDeck    = new ArrayList<>();

    private int roundsPlayed = 0;

    // ── UI components ─────────────────────────────────────────────────
    private JLabel playerCardLabel;
    private JLabel cpuCardLabel;
    private JLabel statusLabel;
    private JLabel scoreLabel;
    private JButton drawButton;
    private JButton newGameButton;

    /**
     * Creates a new War game window.
     *
     * @param difficulty The difficulty level
     */
    public WarGame(Difficulty difficulty) {
        this.difficulty = difficulty;
        setTitle("War  —  " + difficulty.getLabel());
        setSize(520, 420);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
        dealCards();
    }

    // ── UI construction ───────────────────────────────────────────────

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(new Color(35, 100, 50));
        root.setBorder(new EmptyBorder(16, 20, 16, 20));
        setContentPane(root);

        // Title
        JLabel title = new JLabel("WAR", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setForeground(new Color(212, 175, 55));
        root.add(title, BorderLayout.NORTH);

        // Card area
        JPanel cardArea = new JPanel(new GridLayout(2, 2, 10, 10));
        cardArea.setOpaque(false);

        cpuCardLabel    = makeCardLabel("CPU");
        playerCardLabel = makeCardLabel("You");

        JLabel cpuName    = sideLabel("Computer");
        JLabel playerName = sideLabel("You");

        cardArea.add(cpuName);
        cardArea.add(cpuCardLabel);
        cardArea.add(playerName);
        cardArea.add(playerCardLabel);

        root.add(cardArea, BorderLayout.CENTER);

        // Bottom area
        JPanel bottom = new JPanel(new BorderLayout(4, 4));
        bottom.setOpaque(false);

        statusLabel = new JLabel("Click \"Draw\" to start!", SwingConstants.CENTER);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        scoreLabel = new JLabel(scoreText(), SwingConstants.CENTER);
        scoreLabel.setForeground(new Color(212, 175, 55));
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setOpaque(false);

        drawButton    = makeButton("Draw ▶");
        newGameButton = makeButton("New Game");
        newGameButton.setEnabled(false);

        drawButton.addActionListener(e -> playRound());
        newGameButton.addActionListener(e -> {
            dealCards();
            statusLabel.setText("New game started – click Draw!");
            cpuCardLabel.setText("?");
            playerCardLabel.setText("?");
            drawButton.setEnabled(true);
            newGameButton.setEnabled(false);
        });

        btnRow.add(drawButton);
        btnRow.add(newGameButton);

        bottom.add(statusLabel, BorderLayout.NORTH);
        bottom.add(scoreLabel,  BorderLayout.CENTER);
        bottom.add(btnRow,      BorderLayout.SOUTH);

        root.add(bottom, BorderLayout.SOUTH);
    }

    private JLabel makeCardLabel(String owner) {
        JLabel lbl = new JLabel("?", SwingConstants.CENTER);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 32));
        lbl.setForeground(Color.BLACK);
        lbl.setBackground(Color.WHITE);
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        lbl.setPreferredSize(new Dimension(100, 110));
        return lbl;
    }

    private JLabel sideLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        return lbl;
    }

    private JButton makeButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        return btn;
    }

    // ── Game logic ────────────────────────────────────────────────────

    /**
     * Deals the deck to both players according to the difficulty setting.
     */
    void dealCards() {
        playerDeck.clear();
        cpuDeck.clear();
        roundsPlayed = 0;

        // Build a fresh sorted deck
        String[] suits  = {"Hearts", "Diamonds", "Clubs", "Spades"};
        String[] ranks  = {"2","3","4","5","6","7","8","9","10","J","Q","K","A"};
        int[]    values = {2,3,4,5,6,7,8,9,10,10,10,10,11};

        List<Card> full = new ArrayList<>();
        for (String suit : suits) {
            for (int i = 0; i < ranks.length; i++) {
                full.add(new Card(ranks[i], suit, values[i]));
            }
        }

        // Sort ascending by war rank
        full.sort((a, b) -> warRank(a) - warRank(b));

        switch (difficulty) {
            case EASY -> {
                // Player gets the stronger (upper) half
                playerDeck.addAll(full.subList(26, 52));
                cpuDeck.addAll(full.subList(0, 26));
                Collections.shuffle(playerDeck);
                Collections.shuffle(cpuDeck);
            }
            case HARD -> {
                // CPU gets the stronger half
                cpuDeck.addAll(full.subList(26, 52));
                playerDeck.addAll(full.subList(0, 26));
                Collections.shuffle(cpuDeck);
                Collections.shuffle(playerDeck);
            }
            default -> {
                // Random split
                Collections.shuffle(full);
                playerDeck.addAll(full.subList(0, 26));
                cpuDeck.addAll(full.subList(26, 52));
            }
        }

        if (scoreLabel != null) {
            scoreLabel.setText(scoreText());
        }
    }

    /**
     * Plays one round (or resolves a war).
     */
    private void playRound() {
        if (playerDeck.isEmpty() || cpuDeck.isEmpty()) {
            endGame();
            return;
        }

        roundsPlayed++;
        Card playerCard = playerDeck.remove(0);
        Card cpuCard    = cpuDeck.remove(0);

        playerCardLabel.setText(cardDisplay(playerCard));
        cpuCardLabel.setText(cardDisplay(cpuCard));

        // Colour red for Hearts/Diamonds
        playerCardLabel.setForeground(isRed(playerCard) ? Color.RED : Color.BLACK);
        cpuCardLabel.setForeground(isRed(cpuCard) ? Color.RED : Color.BLACK);

        int pRank = warRank(playerCard);
        int cRank = warRank(cpuCard);

        List<Card> pot = new ArrayList<>();
        pot.add(playerCard);
        pot.add(cpuCard);

        if (pRank > cRank) {
            Collections.shuffle(pot);
            playerDeck.addAll(pot);
            statusLabel.setText("You win this round! (" + playerCard.getRank() + " > " + cpuCard.getRank() + ")");
        } else if (cRank > pRank) {
            Collections.shuffle(pot);
            cpuDeck.addAll(pot);
            statusLabel.setText("CPU wins this round! (" + cpuCard.getRank() + " > " + playerCard.getRank() + ")");
        } else {
            // War!
            statusLabel.setText("WAR! Both played " + playerCard.getRank() + " – drawing 3 more each…");
            resolveWar(pot);
        }

        scoreLabel.setText(scoreText());

        if (playerDeck.isEmpty() || cpuDeck.isEmpty()) {
            endGame();
        }
    }

    /**
     * Handles a "war" tie by drawing up to 3 face-down cards then one face-up.
     */
    private void resolveWar(List<Card> pot) {
        int warCards = 3;
        for (int i = 0; i < warCards; i++) {
            if (!playerDeck.isEmpty()) pot.add(playerDeck.remove(0));
            if (!cpuDeck.isEmpty())    pot.add(cpuDeck.remove(0));
        }
        if (playerDeck.isEmpty() || cpuDeck.isEmpty()) {
            // One player ran out during war – they lose remaining pot
            if (playerDeck.isEmpty()) {
                cpuDeck.addAll(pot);
            } else {
                playerDeck.addAll(pot);
            }
            return;
        }
        // Flip one more
        Card pFinal = playerDeck.remove(0);
        Card cFinal = cpuDeck.remove(0);
        pot.add(pFinal);
        pot.add(cFinal);

        int pRank = warRank(pFinal);
        int cRank = warRank(cFinal);

        Collections.shuffle(pot);
        if (pRank > cRank) {
            playerDeck.addAll(pot);
        } else if (cRank > pRank) {
            cpuDeck.addAll(pot);
        } else {
            // Another tie – split pot evenly
            playerDeck.addAll(pot.subList(0, pot.size() / 2));
            cpuDeck.addAll(pot.subList(pot.size() / 2, pot.size()));
        }
    }

    private void endGame() {
        drawButton.setEnabled(false);
        newGameButton.setEnabled(true);
        if (playerDeck.size() > cpuDeck.size()) {
            statusLabel.setText("🏆 You win the game after " + roundsPlayed + " rounds!");
        } else if (cpuDeck.size() > playerDeck.size()) {
            statusLabel.setText("CPU wins the game after " + roundsPlayed + " rounds. Better luck next time!");
        } else {
            statusLabel.setText("It's a draw after " + roundsPlayed + " rounds!");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────

    /**
     * Returns the War rank order (0–12) of a card.
     */
    static int warRank(Card card) {
        for (int i = 0; i < WAR_ORDER.length; i++) {
            if (WAR_ORDER[i].equals(card.getRank())) return i;
        }
        return 0;
    }

    private String cardDisplay(Card card) {
        String suitSymbol = switch (card.getSuit()) {
            case "Hearts"   -> "♥";
            case "Diamonds" -> "♦";
            case "Clubs"    -> "♣";
            case "Spades"   -> "♠";
            default         -> "?";
        };
        return "<html><center>" + card.getRank() + "<br>" + suitSymbol + "</center></html>";
    }

    private boolean isRed(Card card) {
        return card.getSuit().equals("Hearts") || card.getSuit().equals("Diamonds");
    }

    private String scoreText() {
        return "You: " + playerDeck.size() + " cards  |  CPU: " + cpuDeck.size() + " cards";
    }
}
