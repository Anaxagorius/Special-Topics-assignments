import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a multiplayer game session managed by the REST API.
 * A session starts in {@code WAITING} state while players join, moves to
 * {@code IN_PROGRESS} once the host starts the game, and finally reaches
 * {@code FINISHED} when a winner is declared.
 *
 * @author Tom Burchell
 * @version 1.0
 */
public class GameSession {

    /** Maximum number of players allowed in a single session. */
    public static final int MAX_PLAYERS = 6;

    /** Possible lifecycle states for a game session. */
    public enum Status { WAITING, IN_PROGRESS, FINISHED }

    private final int    id;
    private final String gameName;
    private final String hostPlayer;
    private final List<String> players;
    private Status status;

    /**
     * Creates a new session; the host is automatically added as the first player.
     *
     * @param id         unique session identifier
     * @param gameName   name of the card game (e.g. "Blackjack", "Poker")
     * @param hostPlayer display name of the player who created the session
     */
    public GameSession(int id, String gameName, String hostPlayer) {
        this.id         = id;
        this.gameName   = gameName;
        this.hostPlayer = hostPlayer;
        this.players    = new ArrayList<>();
        this.players.add(hostPlayer);
        this.status     = Status.WAITING;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int           getId()         { return id; }
    public String        getGameName()   { return gameName; }
    public String        getHostPlayer() { return hostPlayer; }
    public List<String>  getPlayers()    { return Collections.unmodifiableList(players); }
    public Status        getStatus()     { return status; }

    // ── Mutators ─────────────────────────────────────────────────────────────

    /**
     * Adds a player to this session.
     *
     * @param playerName display name of the joining player
     * @throws IllegalStateException    if the session is not in {@code WAITING} state
     * @throws IllegalStateException    if the session has already reached {@link #MAX_PLAYERS}
     * @throws IllegalArgumentException if the player name is blank
     */
    public void addPlayer(String playerName) {
        if (status != Status.WAITING) {
            throw new IllegalStateException("Session is not accepting new players");
        }
        if (players.size() >= MAX_PLAYERS) {
            throw new IllegalStateException("Session is full");
        }
        if (playerName == null || playerName.isBlank()) {
            throw new IllegalArgumentException("Player name must not be blank");
        }
        players.add(playerName);
    }

    /**
     * Transitions the session from {@code WAITING} to {@code IN_PROGRESS}.
     *
     * @throws IllegalStateException if the session is not in {@code WAITING} state
     */
    public void start() {
        if (status != Status.WAITING) {
            throw new IllegalStateException("Session cannot be started in its current state");
        }
        status = Status.IN_PROGRESS;
    }

    /**
     * Transitions the session to {@code FINISHED}.
     *
     * @throws IllegalStateException if the session is already {@code FINISHED}
     */
    public void finish() {
        if (status == Status.FINISHED) {
            throw new IllegalStateException("Session is already finished");
        }
        status = Status.FINISHED;
    }

    /**
     * Serializes this session to a JSON object string.
     *
     * @return JSON representation of the session
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"id\":").append(id)
          .append(",\"gameName\":\"").append(escapeJson(gameName)).append("\"")
          .append(",\"hostPlayer\":\"").append(escapeJson(hostPlayer)).append("\"")
          .append(",\"status\":\"").append(status).append("\"")
          .append(",\"players\":[");
        for (int i = 0; i < players.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escapeJson(players.get(i))).append("\"");
        }
        sb.append("]}");
        return sb.toString();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public String toString() {
        return String.format("GameSession{id=%d, game='%s', host='%s', status=%s, players=%s}",
            id, gameName, hostPlayer, status, players);
    }
}
