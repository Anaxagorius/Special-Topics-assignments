import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight REST API server for multiplayer card-game management.
 *
 * <h2>Endpoints</h2>
 * <pre>
 * Player profiles (backed by {@link PlayerRepository})
 *   GET    /api/players           – list all profiles
 *   POST   /api/players           – create profile  body: {"name":"…","balance":1000}
 *   GET    /api/players/{id}      – get profile by id
 *   PUT    /api/players/{id}      – update profile
 *   DELETE /api/players/{id}      – delete profile
 *
 * Multiplayer game sessions (in-memory)
 *   GET    /api/games             – list all sessions
 *   POST   /api/games             – create session  body: {"gameName":"…","hostPlayer":"…"}
 *   GET    /api/games/{id}        – get session by id
 *   POST   /api/games/{id}/join   – join session     body: {"playerName":"…"}
 *   POST   /api/games/{id}/start  – start session
 *   POST   /api/games/{id}/finish – finish session
 * </pre>
 *
 * <p>All responses are UTF-8 JSON. Errors are returned as
 * {@code {"error":"message"}}.
 *
 * @author Tom Burchell
 * @version 1.0
 */
public class GameApiServer {

    private final int              port;
    private final PlayerRepository playerRepo;
    private final List<GameSession> sessions  = new ArrayList<>();
    private final AtomicInteger    nextId     = new AtomicInteger(1);
    private HttpServer             server;

    // ── JSON field patterns ───────────────────────────────────────────────────

    private static final Pattern STR_PATTERN =
        Pattern.compile("\"(\\w+)\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern INT_PATTERN =
        Pattern.compile("\"(\\w+)\"\\s*:\\s*(-?\\d+)");

    // ── Construction / lifecycle ─────────────────────────────────────────────

    /**
     * Creates a server on the given port backed by the provided repository.
     *
     * @param port       TCP port to listen on (use 0 for an OS-assigned port)
     * @param playerRepo player-profile data store
     */
    public GameApiServer(int port, PlayerRepository playerRepo) {
        this.port       = port;
        this.playerRepo = playerRepo;
    }

    /**
     * Starts listening for requests.
     *
     * @throws IOException if the server socket cannot be opened
     */
    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/players", this::handlePlayers);
        server.createContext("/api/games",   this::handleGames);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    /** Stops the server immediately. */
    public void stop() {
        if (server != null) server.stop(0);
    }

    /**
     * Returns the port the server is actually listening on (useful when port 0
     * was supplied and the OS assigned a free port).
     *
     * @return bound port number
     */
    public int getPort() {
        if (server == null) return port;
        return ((InetSocketAddress) server.getAddress()).getPort();
    }

    // ── Player handler ───────────────────────────────────────────────────────

    private void handlePlayers(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath(); // /api/players[/{id}]

        String[] segments = path.split("/");
        // segments: ["", "api", "players"] or ["", "api", "players", "{id}"]

        try {
            if (segments.length == 3) {
                // /api/players
                switch (method) {
                    case "GET"  -> send(ex, 200, listToJson(playerRepo.findAll()));
                    case "POST" -> {
                        PlayerProfile p = parseProfile(readBody(ex));
                        if (p.getName() == null || p.getName().isBlank()) {
                            send(ex, 400, error("name is required"));
                            return;
                        }
                        playerRepo.save(p);
                        send(ex, 201, p.toJson());
                    }
                    default -> send(ex, 405, error("Method not allowed"));
                }
            } else if (segments.length == 4) {
                // /api/players/{id}
                int id = parseId(segments[3]);
                if (id < 0) { send(ex, 400, error("Invalid id")); return; }

                switch (method) {
                    case "GET" -> {
                        PlayerProfile p = playerRepo.findById(id);
                        if (p == null) send(ex, 404, error("Player not found"));
                        else           send(ex, 200, p.toJson());
                    }
                    case "PUT" -> {
                        PlayerProfile existing = playerRepo.findById(id);
                        if (existing == null) { send(ex, 404, error("Player not found")); return; }
                        mergeProfile(existing, readBody(ex));
                        playerRepo.update(existing);
                        send(ex, 200, existing.toJson());
                    }
                    case "DELETE" -> {
                        if (playerRepo.delete(id)) send(ex, 200, "{\"message\":\"Player deleted\"}");
                        else                        send(ex, 404, error("Player not found"));
                    }
                    default -> send(ex, 405, error("Method not allowed"));
                }
            } else {
                send(ex, 400, error("Invalid path"));
            }
        } catch (Exception e) {
            send(ex, 500, error(e.getMessage()));
        }
    }

    // ── Game-session handler ─────────────────────────────────────────────────

    private void handleGames(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();

        String[] segments = path.split("/");
        // ["", "api", "games"]
        // ["", "api", "games", "{id}"]
        // ["", "api", "games", "{id}", "join"|"start"|"finish"]

        try {
            if (segments.length == 3) {
                // /api/games
                switch (method) {
                    case "GET" -> send(ex, 200, sessionsToJson(sessions));
                    case "POST" -> {
                        String body       = readBody(ex);
                        String gameName   = extractString(body, "gameName");
                        String hostPlayer = extractString(body, "hostPlayer");
                        if (gameName == null || hostPlayer == null) {
                            send(ex, 400, error("gameName and hostPlayer are required"));
                            return;
                        }
                        GameSession session = new GameSession(nextId.getAndIncrement(), gameName, hostPlayer);
                        sessions.add(session);
                        send(ex, 201, session.toJson());
                    }
                    default -> send(ex, 405, error("Method not allowed"));
                }
            } else if (segments.length >= 4) {
                int sessionId = parseId(segments[3]);
                if (sessionId < 0) { send(ex, 400, error("Invalid session id")); return; }

                GameSession session = findSession(sessionId);

                if (segments.length == 4) {
                    // /api/games/{id}
                    if (!"GET".equals(method)) { send(ex, 405, error("Method not allowed")); return; }
                    if (session == null) send(ex, 404, error("Game not found"));
                    else                 send(ex, 200, session.toJson());

                } else if (segments.length == 5) {
                    // /api/games/{id}/{action}
                    if (!"POST".equals(method)) { send(ex, 405, error("Method not allowed")); return; }
                    if (session == null) { send(ex, 404, error("Game not found")); return; }

                    String action = segments[4];
                    switch (action) {
                        case "join" -> {
                            String body       = readBody(ex);
                            String playerName = extractString(body, "playerName");
                            if (playerName == null) {
                                send(ex, 400, error("playerName is required"));
                                return;
                            }
                            try {
                                session.addPlayer(playerName);
                                send(ex, 200, session.toJson());
                            } catch (IllegalStateException ise) {
                                send(ex, 409, error(ise.getMessage()));
                            }
                        }
                        case "start" -> {
                            try {
                                session.start();
                                send(ex, 200, session.toJson());
                            } catch (IllegalStateException ise) {
                                send(ex, 409, error(ise.getMessage()));
                            }
                        }
                        case "finish" -> {
                            try {
                                session.finish();
                                send(ex, 200, session.toJson());
                            } catch (IllegalStateException ise) {
                                send(ex, 409, error(ise.getMessage()));
                            }
                        }
                        default -> send(ex, 400, error("Unknown action: " + action));
                    }
                } else {
                    send(ex, 400, error("Invalid path"));
                }
            } else {
                send(ex, 400, error("Invalid path"));
            }
        } catch (Exception e) {
            send(ex, 500, error(e.getMessage()));
        }
    }

    // ── I/O helpers ──────────────────────────────────────────────────────────

    private static void send(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String error(String msg) {
        return "{\"error\":\"" + escapeJson(msg) + "\"}";
    }

    // ── JSON helpers ─────────────────────────────────────────────────────────

    /** Extracts the string value for a named key from a simple JSON object. */
    static String extractString(String json, String key) {
        if (json == null) return null;
        Matcher m = STR_PATTERN.matcher(json);
        while (m.find()) {
            if (m.group(1).equals(key)) return m.group(2);
        }
        return null;
    }

    /** Extracts the integer value for a named key from a simple JSON object. */
    static int extractInt(String json, String key, int defaultValue) {
        if (json == null) return defaultValue;
        Matcher m = INT_PATTERN.matcher(json);
        while (m.find()) {
            if (m.group(1).equals(key)) {
                try { return Integer.parseInt(m.group(2)); }
                catch (NumberFormatException ignored) { /* fall through */ }
            }
        }
        return defaultValue;
    }

    private static PlayerProfile parseProfile(String json) {
        PlayerProfile p = new PlayerProfile();
        p.setName(extractString(json, "name"));
        p.setBalance(extractInt(json, "balance", 1000));
        p.setWins(extractInt(json, "wins", 0));
        p.setLosses(extractInt(json, "losses", 0));
        p.setTies(extractInt(json, "ties", 0));
        return p;
    }

    private static void mergeProfile(PlayerProfile target, String json) {
        String name = extractString(json, "name");
        if (name != null && !name.isBlank()) target.setName(name);
        int balance = extractInt(json, "balance", -1);
        if (balance >= 0) target.setBalance(balance);
        int wins = extractInt(json, "wins", -1);
        if (wins >= 0) target.setWins(wins);
        int losses = extractInt(json, "losses", -1);
        if (losses >= 0) target.setLosses(losses);
        int ties = extractInt(json, "ties", -1);
        if (ties >= 0) target.setTies(ties);
    }

    private static String listToJson(List<PlayerProfile> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(list.get(i).toJson());
        }
        return sb.append("]").toString();
    }

    private static String sessionsToJson(List<GameSession> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(list.get(i).toJson());
        }
        return sb.append("]").toString();
    }

    private static int parseId(String s) {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return -1; }
    }

    private GameSession findSession(int id) {
        return sessions.stream().filter(s -> s.getId() == id).findFirst().orElse(null);
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // ── Entry point (standalone demo) ────────────────────────────────────────

    /**
     * Starts the API server on port 8080 with the default file-based database.
     *
     * @param args unused
     * @throws IOException if the server cannot start
     */
    public static void main(String[] args) throws IOException {
        PlayerRepository repo = new PlayerRepository();
        GameApiServer    api  = new GameApiServer(8080, repo);
        api.start();
        System.out.println("Game API server started on port " + api.getPort());
        System.out.println("Press Ctrl+C to stop.");
    }
}
