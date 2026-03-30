import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 5-Card Draw Poker game with a Swing GUI.
 *
 * <p>Rules: Each player is dealt 5 cards. A betting round takes place (Call, Raise,
 * Fold). Each player then discards up to 3 cards and draws replacements. A final
 * betting round follows. Best 5-card hand wins the pot.</p>
 *
 * <p>Difficulty levels affect the CPU opponent's strategy:
 * <ul>
 *   <li>EASY   – CPU bets and discards randomly.</li>
 *   <li>MEDIUM – CPU evaluates its hand to decide whether to call/fold and which
 *                cards to keep.</li>
 *   <li>HARD   – CPU plays near-optimally: raises with strong hands, bluffs
 *                occasionally, and keeps cards strategically.</li>
 * </ul>
 * </p>
 *
 * @author Tom Burchell
 * @version 1.0
 */
public class PokerGame extends JFrame {

    private static final int STARTING_CHIPS = 200;
    private static final int ANTE           = 5;

    private final Difficulty difficulty;
    private final Random random = new Random();

    // Game state
    private Deck deck;
    private final List<Card> playerHand = new ArrayList<>();
    private final List<Card> cpuHand    = new ArrayList<>();
    private int pot         = 0;
    private int playerChips = STARTING_CHIPS;
    private int cpuChips    = STARTING_CHIPS;
    private int currentBet  = 0;
    private boolean playerFolded = false;
    private final Set<Integer> selectedForDiscard = new HashSet<>();

    // Game phases
    private enum Phase { DEAL, FIRST_BET, DRAW, SECOND_BET, SHOWDOWN, GAME_OVER }
    private Phase phase = Phase.DEAL;

    // UI components
    private JPanel playerHandPanel;
    private JPanel cpuHandPanel;
    private JLabel potLabel;
    private JLabel chipsLabel;
    private JLabel cpuChipsLabel;
    private JLabel statusLabel;
    private JLabel handNameLabel;
    private JTextArea logArea;
    private JButton actionBtn1;   // Call / Check / New Game
    private JButton actionBtn2;   // Raise / Discard
    private JButton actionBtn3;   // Fold

    /**
     * Creates a new Poker game window.
     *
     * @param difficulty The difficulty level
     */
    public PokerGame(Difficulty difficulty) {
        this.difficulty = difficulty;
        setTitle("Poker (5-Card Draw)  —  " + difficulty.getLabel());
        setSize(760, 580);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
        startGame();
    }

    // ── UI construction ───────────────────────────────────────────────

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(new Color(35, 100, 50));
        root.setBorder(new EmptyBorder(10, 14, 10, 14));
        setContentPane(root);

        JLabel title = new JLabel("♠ POKER  5-Card Draw ♥", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 26));
        title.setForeground(new Color(212, 175, 55));
        root.add(title, BorderLayout.NORTH);

        // Centre: hands + log
        JPanel centre = new JPanel(new BorderLayout(8, 6));
        centre.setOpaque(false);

        // CPU hand (face-down during betting, face-up at showdown)
        JPanel cpuPanel = new JPanel(new BorderLayout(4, 2));
        cpuPanel.setOpaque(false);
        JLabel cpuLabel = new JLabel("CPU's Hand:", SwingConstants.CENTER);
        cpuLabel.setForeground(Color.WHITE);
        cpuLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        cpuHandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        cpuHandPanel.setOpaque(false);
        cpuChipsLabel = infoLabel("CPU chips: " + cpuChips);
        cpuPanel.add(cpuLabel,     BorderLayout.NORTH);
        cpuPanel.add(cpuHandPanel, BorderLayout.CENTER);
        cpuPanel.add(cpuChipsLabel, BorderLayout.SOUTH);
        centre.add(cpuPanel, BorderLayout.NORTH);

        // Log + pot info
        JPanel midPanel = new JPanel(new BorderLayout(4, 4));
        midPanel.setOpaque(false);

        JPanel infoRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 2));
        infoRow.setOpaque(false);
        potLabel    = infoLabel("Pot: $0");
        chipsLabel  = infoLabel("Your chips: $" + playerChips);
        statusLabel = infoLabel("Phase: Deal");
        infoRow.add(potLabel);
        infoRow.add(chipsLabel);
        infoRow.add(statusLabel);
        midPanel.add(infoRow, BorderLayout.NORTH);

        logArea = new JTextArea(6, 55);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(new Color(20, 60, 30));
        logArea.setForeground(Color.WHITE);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(logArea);
        midPanel.add(scroll, BorderLayout.CENTER);

        centre.add(midPanel, BorderLayout.CENTER);
        root.add(centre, BorderLayout.CENTER);

        // Player hand area
        JPanel playerArea = new JPanel(new BorderLayout(4, 4));
        playerArea.setOpaque(false);

        JPanel handLabelRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 2));
        handLabelRow.setOpaque(false);
        JLabel yourLabel = new JLabel("Your Hand:", SwingConstants.CENTER);
        yourLabel.setForeground(Color.WHITE);
        yourLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        handNameLabel = infoLabel("–");
        handLabelRow.add(yourLabel);
        handLabelRow.add(handNameLabel);
        playerArea.add(handLabelRow, BorderLayout.NORTH);

        playerHandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        playerHandPanel.setOpaque(false);
        playerArea.add(playerHandPanel, BorderLayout.CENTER);

        // Action buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        btnRow.setOpaque(false);
        actionBtn1 = makeActionBtn("Deal");
        actionBtn2 = makeActionBtn("Raise $10");
        actionBtn3 = makeActionBtn("Fold");
        btnRow.add(actionBtn1);
        btnRow.add(actionBtn2);
        btnRow.add(actionBtn3);

        playerArea.add(btnRow, BorderLayout.SOUTH);
        root.add(playerArea, BorderLayout.SOUTH);
    }

    private JLabel infoLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setForeground(new Color(212, 175, 55));
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        return lbl;
    }

    private JButton makeActionBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        return btn;
    }

    // ── Game logic ────────────────────────────────────────────────────

    /**
     * Starts (or restarts) a round.
     */
    void startGame() {
        if (playerChips <= 0 || cpuChips <= 0) {
            playerChips = STARTING_CHIPS;
            cpuChips    = STARTING_CHIPS;
        }
        playerHand.clear();
        cpuHand.clear();
        selectedForDiscard.clear();
        pot          = 0;
        currentBet   = 0;
        playerFolded = false;

        deck = new Deck();
        deck.shuffle();

        // Ante
        int pAnte = Math.min(ANTE, playerChips);
        int cAnte = Math.min(ANTE, cpuChips);
        playerChips -= pAnte;
        cpuChips    -= cAnte;
        pot         += pAnte + cAnte;

        // Deal 5 cards each
        for (int i = 0; i < 5; i++) {
            playerHand.add(deck.deal());
            cpuHand.add(deck.deal());
        }

        phase = Phase.FIRST_BET;
        log("=== New Round (" + difficulty.getLabel() + ")  |  Ante: $" + ANTE + " each ===");
        log("Pot: $" + pot);
        refreshUI();
        updateButtons();
    }

    /** Player calls / checks. */
    private void playerCall() {
        if (phase == Phase.FIRST_BET || phase == Phase.SECOND_BET) {
            if (currentBet > 0) {
                int call = Math.min(currentBet, playerChips);
                playerChips -= call;
                pot         += call;
                log("You call $" + call + ".");
            } else {
                log("You check.");
            }
            advanceAfterPlayerBet();
        }
    }

    /** Player raises by $10. */
    private void playerRaise() {
        if (phase == Phase.FIRST_BET || phase == Phase.SECOND_BET) {
            int raise = Math.min(10, playerChips);
            if (raise <= 0) { log("You don't have enough chips to raise!"); return; }
            playerChips -= raise;
            pot         += raise;
            currentBet   = raise;
            log("You raise $" + raise + "!");
            advanceAfterPlayerBet();
        } else if (phase == Phase.DRAW) {
            // "Discard" button
            performDiscard();
        }
    }

    /** Player folds. */
    private void playerFold() {
        playerFolded = true;
        log("You fold. CPU wins the pot.");
        cpuChips += pot;
        pot = 0;
        phase = Phase.SHOWDOWN;
        refreshUI();
        updateButtons();
    }

    /**
     * After the player acts in a betting phase, let the CPU respond then advance.
     */
    private void advanceAfterPlayerBet() {
        cpuBet();
        if (phase == Phase.FIRST_BET) {
            phase = Phase.DRAW;
        } else if (phase == Phase.SECOND_BET) {
            phase = Phase.SHOWDOWN;
            showdown();
            return;
        }
        currentBet = 0;
        refreshUI();
        updateButtons();
    }

    /** CPU takes its betting action. */
    private void cpuBet() {
        int score = PokerHandEvaluator.evaluate(cpuHand);

        switch (difficulty) {
            case EASY -> {
                // Random: 50% call, 30% raise, 20% fold
                int r = random.nextInt(10);
                if (r < 5) {
                    int call = Math.min(currentBet, cpuChips);
                    cpuChips -= call; pot += call;
                    log("CPU calls.");
                } else if (r < 8) {
                    int raise = Math.min(10, cpuChips);
                    cpuChips -= raise; pot += raise;
                    log("CPU raises $" + raise + ".");
                } else {
                    log("CPU folds.");
                    playerChips += pot; pot = 0;
                    phase = Phase.SHOWDOWN;
                }
            }
            case MEDIUM -> {
                int handTier = score / 1_000_000;
                if (handTier >= 3) {
                    int raise = Math.min(10, cpuChips);
                    cpuChips -= raise; pot += raise;
                    log("CPU raises $" + raise + ".");
                } else if (handTier >= 1 || currentBet == 0) {
                    int call = Math.min(currentBet, cpuChips);
                    cpuChips -= call; pot += call;
                    log(currentBet > 0 ? "CPU calls." : "CPU checks.");
                } else {
                    log("CPU folds.");
                    playerChips += pot; pot = 0;
                    phase = Phase.SHOWDOWN;
                }
            }
            case HARD -> {
                int handTier = score / 1_000_000;
                // Bluff 10% of the time
                boolean bluff = random.nextInt(10) == 0;
                if (handTier >= 4 || bluff) {
                    int raise = Math.min(20, cpuChips);
                    cpuChips -= raise; pot += raise;
                    log("CPU raises $" + raise + "!" + (bluff ? " (bluff?)" : ""));
                } else if (handTier >= 1 || currentBet == 0) {
                    int call = Math.min(currentBet, cpuChips);
                    cpuChips -= call; pot += call;
                    log(currentBet > 0 ? "CPU calls." : "CPU checks.");
                } else {
                    log("CPU folds.");
                    playerChips += pot; pot = 0;
                    phase = Phase.SHOWDOWN;
                }
            }
        }
    }

    /** Player discards selected cards and draws replacements. */
    private void performDiscard() {
        List<Card> toDiscard = new ArrayList<>();
        List<Integer> sortedIndices = selectedForDiscard.stream()
            .sorted(Comparator.reverseOrder()).collect(Collectors.toList());

        for (int idx : sortedIndices) {
            if (idx >= 0 && idx < playerHand.size()) {
                toDiscard.add(playerHand.remove(idx));
            }
        }
        log("You discard " + toDiscard.size() + " card(s).");
        while (playerHand.size() < 5 && deck.cardsRemaining() > 0) {
            playerHand.add(deck.deal());
        }
        selectedForDiscard.clear();

        // CPU discards
        cpuDiscard();

        phase = Phase.SECOND_BET;
        currentBet = 0;
        log("Draw phase complete.");
        refreshUI();
        updateButtons();
    }

    /** CPU discards cards based on difficulty. */
    private void cpuDiscard() {
        int score = PokerHandEvaluator.evaluate(cpuHand);
        int tier = score / 1_000_000;
        int discardCount = 0;

        switch (difficulty) {
            case EASY -> {
                // Discard 0–3 random cards
                discardCount = random.nextInt(4);
            }
            case MEDIUM -> {
                // Keep pairs/trips/quads; discard unpaired cards (up to 3)
                discardCount = tier >= 2 ? 0 : (tier == 1 ? 3 : 3);
                discardCount = Math.min(discardCount, 3);
            }
            case HARD -> {
                // Discard cards that don't contribute to best hand grouping
                discardCount = tier >= 3 ? 0 : (tier == 1 ? 3 : tier == 0 ? 3 : 1);
                discardCount = Math.min(discardCount, 3);
            }
        }

        List<Card> toRemove = new ArrayList<>();
        // Remove the lowest-value cards
        List<Card> sorted = cpuHand.stream()
            .sorted(Comparator.comparingInt(c -> PokerHandEvaluator.rankValue(c.getRank())))
            .collect(Collectors.toList());
        for (int i = 0; i < discardCount && i < sorted.size(); i++) {
            toRemove.add(sorted.get(i));
        }
        cpuHand.removeAll(toRemove);
        while (cpuHand.size() < 5 && deck.cardsRemaining() > 0) {
            cpuHand.add(deck.deal());
        }
        log("CPU discards " + discardCount + " card(s).");
    }

    /** Resolves the showdown and awards the pot. */
    private void showdown() {
        if (playerFolded) {
            // Already handled
        } else {
            int pScore = PokerHandEvaluator.evaluate(playerHand);
            int cScore = PokerHandEvaluator.evaluate(cpuHand);
            String pHand = PokerHandEvaluator.handName(playerHand);
            String cHand = PokerHandEvaluator.handName(cpuHand);

            log("--- Showdown ---");
            log("Your hand: " + playerHand + "  →  " + pHand);
            log("CPU's hand: " + cpuHand + "  →  " + cHand);

            if (pScore > cScore) {
                playerChips += pot;
                log("🏆 You win the pot of $" + pot + "!");
            } else if (cScore > pScore) {
                cpuChips += pot;
                log("CPU wins the pot of $" + pot + ".");
            } else {
                playerChips += pot / 2;
                cpuChips    += pot / 2;
                log("Split pot – $" + (pot / 2) + " each.");
            }
            pot = 0;
        }

        phase = Phase.GAME_OVER;
        refreshUI();
        updateButtons();
    }

    // ── UI helpers ────────────────────────────────────────────────────

    private void refreshUI() {
        potLabel.setText("Pot: $" + pot);
        chipsLabel.setText("Your chips: $" + playerChips);
        cpuChipsLabel.setText("CPU chips: $" + cpuChips);

        String phaseText = switch (phase) {
            case FIRST_BET  -> "First Bet";
            case DRAW       -> "Draw (select cards to discard, then click Discard)";
            case SECOND_BET -> "Second Bet";
            case SHOWDOWN, GAME_OVER -> "Showdown";
            default         -> "Deal";
        };
        statusLabel.setText(phaseText);

        // Player hand
        playerHandPanel.removeAll();
        for (int i = 0; i < playerHand.size(); i++) {
            final int idx = i;
            Card card = playerHand.get(i);
            boolean selected = selectedForDiscard.contains(i);
            JButton btn = cardButton(card, selected);
            if (phase == Phase.DRAW) {
                btn.addActionListener(e -> {
                    if (selectedForDiscard.contains(idx)) {
                        selectedForDiscard.remove(idx);
                    } else if (selectedForDiscard.size() < 3) {
                        selectedForDiscard.add(idx);
                    }
                    refreshUI();
                });
            }
            playerHandPanel.add(btn);
        }
        playerHandPanel.revalidate();
        playerHandPanel.repaint();

        // Update hand name label
        if (!playerHand.isEmpty() && (phase == Phase.FIRST_BET || phase == Phase.DRAW
                || phase == Phase.SECOND_BET || phase == Phase.SHOWDOWN || phase == Phase.GAME_OVER)) {
            try {
                if (playerHand.size() == 5) {
                    handNameLabel.setText(PokerHandEvaluator.handName(playerHand));
                }
            } catch (Exception ignored) { }
        }

        // CPU hand panel
        cpuHandPanel.removeAll();
        boolean showCpu = (phase == Phase.SHOWDOWN || phase == Phase.GAME_OVER) && !playerFolded;
        for (Card card : cpuHand) {
            JButton btn = showCpu ? cardButton(card, false) : backButton();
            cpuHandPanel.add(btn);
        }
        cpuHandPanel.revalidate();
        cpuHandPanel.repaint();
    }

    private void updateButtons() {
        // Remove all existing listeners before adding new ones for the current phase
        for (var l : actionBtn1.getActionListeners()) actionBtn1.removeActionListener(l);
        for (var l : actionBtn2.getActionListeners()) actionBtn2.removeActionListener(l);
        for (var l : actionBtn3.getActionListeners()) actionBtn3.removeActionListener(l);
        actionBtn1.setText("");
        actionBtn2.setText("");
        actionBtn3.setText("");
        actionBtn1.setEnabled(false);
        actionBtn2.setEnabled(false);
        actionBtn3.setEnabled(false);

        switch (phase) {
            case FIRST_BET, SECOND_BET -> {
                actionBtn1.setText(currentBet > 0 ? "Call $" + currentBet : "Check");
                actionBtn2.setText("Raise $10");
                actionBtn3.setText("Fold");
                actionBtn1.setEnabled(true);
                actionBtn2.setEnabled(playerChips >= 10);
                actionBtn3.setEnabled(true);
                actionBtn1.addActionListener(e -> playerCall());
                actionBtn2.addActionListener(e -> playerRaise());
                actionBtn3.addActionListener(e -> playerFold());
            }
            case DRAW -> {
                actionBtn1.setText("Draw / Keep All");
                actionBtn2.setText("Discard Selected (" + selectedForDiscard.size() + ")");
                actionBtn3.setText("Fold");
                actionBtn1.setEnabled(true);
                actionBtn2.setEnabled(true);
                actionBtn3.setEnabled(true);
                actionBtn1.addActionListener(e -> {
                    selectedForDiscard.clear();
                    performDiscard();
                });
                actionBtn2.addActionListener(e -> performDiscard());
                actionBtn3.addActionListener(e -> playerFold());
            }
            case GAME_OVER -> {
                actionBtn1.setText(playerChips <= 0 || cpuChips <= 0 ? "New Game (Reset)" : "Next Round");
                actionBtn1.setEnabled(true);
                actionBtn1.addActionListener(e -> startGame());
            }
            default -> { }
        }
    }

    private JButton cardButton(Card card, boolean highlighted) {
        String suitSym = switch (card.getSuit()) {
            case "Hearts"   -> "♥";
            case "Diamonds" -> "♦";
            case "Clubs"    -> "♣";
            case "Spades"   -> "♠";
            default         -> "?";
        };
        JButton btn = new JButton("<html><center><b>" + card.getRank() + "</b><br>" + suitSym + "</center></html>");
        btn.setPreferredSize(new Dimension(60, 72));
        btn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btn.setBackground(highlighted ? new Color(255, 255, 150) : Color.WHITE);
        btn.setForeground(card.getSuit().equals("Hearts") || card.getSuit().equals("Diamonds")
            ? Color.RED : Color.BLACK);
        btn.setFocusPainted(false);
        if (highlighted) {
            btn.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 3));
        }
        return btn;
    }

    private JButton backButton() {
        JButton btn = new JButton("🂠");
        btn.setPreferredSize(new Dimension(60, 72));
        btn.setFont(new Font("SansSerif", Font.BOLD, 24));
        btn.setBackground(new Color(30, 80, 180));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setEnabled(false);
        return btn;
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
