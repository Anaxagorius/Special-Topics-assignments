/**
 * Represents the difficulty level for card games.
 * Controls how intelligently the AI/computer opponent plays.
 *
 * @author Tom Burchell
 * @version 1.0
 */
public enum Difficulty {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard");

    private final String label;

    Difficulty(String label) {
        this.label = label;
    }

    /**
     * Gets the human-readable label for this difficulty.
     *
     * @return The difficulty label
     */
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
