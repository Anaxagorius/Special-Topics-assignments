import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Home GUI for the Card Game Hub.
 * Allows the player to choose a game and difficulty level.
 *
 * @author Tom Burchell
 * @version 1.0
 */
public class HomeGUI extends JFrame {

    private static final Color FELT_GREEN = new Color(35, 100, 50);
    private static final Color GOLD = new Color(212, 175, 55);
    private static final Color CARD_WHITE = new Color(245, 245, 245);

    private Difficulty selectedDifficulty = Difficulty.MEDIUM;

    /**
     * Constructor: builds the home screen.
     */
    public HomeGUI() {
        setTitle("Card Game Hub");
        setSize(620, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(FELT_GREEN);
        root.setBorder(new EmptyBorder(24, 32, 24, 32));
        setContentPane(root);

        // ── Title ──────────────────────────────────────────────────────
        JLabel title = new JLabel("♠  Card Game Hub  ♥", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 38));
        title.setForeground(GOLD);
        root.add(title, BorderLayout.NORTH);

        // ── Game buttons ───────────────────────────────────────────────
        JPanel gamesPanel = new JPanel(new GridLayout(3, 2, 14, 14));
        gamesPanel.setOpaque(false);
        gamesPanel.setBorder(new EmptyBorder(18, 0, 14, 0));

        String[][] games = {
            {"♦ Blackjack",     "Blackjack"},
            {"♠ Poker",         "Poker"},
            {"♣ Go Fish",       "GoFish"},
            {"♥ War",           "War"},
            {"♦ Crazy Eights",  "CrazyEights"}
        };

        for (String[] game : games) {
            JButton btn = makeGameButton(game[0], game[1]);
            gamesPanel.add(btn);
        }

        // Empty placeholder for the 6th grid cell
        JPanel placeholder = new JPanel();
        placeholder.setOpaque(false);
        gamesPanel.add(placeholder);

        root.add(gamesPanel, BorderLayout.CENTER);

        // ── Difficulty selector ────────────────────────────────────────
        JPanel diffPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 4));
        diffPanel.setOpaque(false);

        JLabel diffLabel = new JLabel("Difficulty:");
        diffLabel.setForeground(CARD_WHITE);
        diffLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        diffPanel.add(diffLabel);

        ButtonGroup bg = new ButtonGroup();
        for (Difficulty d : Difficulty.values()) {
            JRadioButton rb = new JRadioButton(d.getLabel());
            rb.setOpaque(false);
            rb.setForeground(CARD_WHITE);
            rb.setFont(new Font("SansSerif", Font.PLAIN, 14));
            if (d == Difficulty.MEDIUM) {
                rb.setSelected(true);
            }
            final Difficulty diff = d;
            rb.addActionListener(e -> selectedDifficulty = diff);
            bg.add(rb);
            diffPanel.add(rb);
        }

        root.add(diffPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates a styled game-launch button.
     */
    private JButton makeGameButton(String label, String gameId) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("SansSerif", Font.BOLD, 17));
        btn.setBackground(new Color(255, 255, 255, 230));
        btn.setForeground(new Color(30, 60, 30));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GOLD, 2, true),
            new EmptyBorder(10, 8, 10, 8)
        ));
        btn.addActionListener(e -> launchGame(gameId));
        return btn;
    }

    /**
     * Launches the selected game with the current difficulty.
     */
    private void launchGame(String gameId) {
        switch (gameId) {
            case "Blackjack" -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Blackjack will open in the console.\nClick OK to continue.",
                    "Blackjack", JOptionPane.OK_CANCEL_OPTION);
                if (confirm == JOptionPane.OK_OPTION) {
                    new Thread(() -> new BlackjackGame().play()).start();
                }
            }
            case "Poker"       -> SwingUtilities.invokeLater(() ->
                new PokerGame(selectedDifficulty).setVisible(true));
            case "GoFish"      -> SwingUtilities.invokeLater(() ->
                new GoFishGame(selectedDifficulty).setVisible(true));
            case "War"         -> SwingUtilities.invokeLater(() ->
                new WarGame(selectedDifficulty).setVisible(true));
            case "CrazyEights" -> SwingUtilities.invokeLater(() ->
                new CrazyEightsGame(selectedDifficulty).setVisible(true));
            default -> JOptionPane.showMessageDialog(this, "Unknown game: " + gameId);
        }
    }

    /**
     * Application entry point.
     *
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }
            new HomeGUI().setVisible(true);
        });
    }
}
