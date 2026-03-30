import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Integration tests for {@link GameApiServer}.
 *
 * <p>Each test class invocation starts the server on an OS-assigned port with
 * its own in-memory H2 database, exercises the REST endpoints via
 * {@link HttpURLConnection}, and shuts the server down after all tests finish.
 */
@TestMethodOrder(OrderAnnotation.class)
public class GameApiServerTest {

    private static final AtomicInteger DB_COUNTER = new AtomicInteger(0);

    private static GameApiServer server;
    private static String        base;   // e.g. "http://localhost:54321"

    @BeforeAll
    static void startServer() throws Exception {
        String url = "jdbc:h2:mem:api_test_" + DB_COUNTER.incrementAndGet()
                   + ";DB_CLOSE_DELAY=-1";
        PlayerRepository repo = new PlayerRepository(url, "sa", "");
        server = new GameApiServer(0, repo);   // port 0 → OS picks a free port
        server.start();
        base = "http://localhost:" + server.getPort();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) server.stop();
    }

    // ── Helper methods ────────────────────────────────────────────────────────

    private static Response request(String method, String path, String body) throws IOException {
        URL url;
        try {
            url = new URI(base + path).toURL();
        } catch (java.net.URISyntaxException e) {
            throw new IOException("Invalid URI: " + base + path, e);
        }
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoInput(true);
        if (body != null) {
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
        }
        int status = conn.getResponseCode();
        InputStream is = status < 400 ? conn.getInputStream() : conn.getErrorStream();
        String responseBody = is == null ? "" : new String(is.readAllBytes(), StandardCharsets.UTF_8);
        return new Response(status, responseBody);
    }

    record Response(int status, String body) {}

    // ── Player endpoint tests ─────────────────────────────────────────────────

    @Test
    @Order(1)
    public void testGetPlayersInitiallyEmpty() throws Exception {
        Response r = request("GET", "/api/players", null);
        assertEquals(200, r.status());
        assertEquals("[]", r.body());
    }

    @Test
    @Order(2)
    public void testCreatePlayer() throws Exception {
        Response r = request("POST", "/api/players",
            "{\"name\":\"Alice\",\"balance\":1000}");
        assertEquals(201, r.status());
        assertTrue(r.body().contains("\"name\":\"Alice\""));
        assertTrue(r.body().contains("\"balance\":1000"));
    }

    @Test
    @Order(3)
    public void testCreatePlayerMissingNameReturns400() throws Exception {
        Response r = request("POST", "/api/players",
            "{\"balance\":500}");
        assertEquals(400, r.status());
        assertTrue(r.body().contains("error"));
    }

    @Test
    @Order(4)
    public void testGetPlayerById() throws Exception {
        // Create first
        Response created = request("POST", "/api/players",
            "{\"name\":\"Bob\",\"balance\":750}");
        assertEquals(201, created.status());
        int id = GameApiServer.extractInt(created.body(), "id", -1);
        assertTrue(id > 0);

        // Fetch by id
        Response r = request("GET", "/api/players/" + id, null);
        assertEquals(200, r.status());
        assertTrue(r.body().contains("\"name\":\"Bob\""));
    }

    @Test
    @Order(5)
    public void testGetPlayerByIdNotFound() throws Exception {
        Response r = request("GET", "/api/players/99999", null);
        assertEquals(404, r.status());
        assertTrue(r.body().contains("error"));
    }

    @Test
    @Order(6)
    public void testUpdatePlayer() throws Exception {
        // Create
        Response created = request("POST", "/api/players",
            "{\"name\":\"Carol\",\"balance\":200}");
        int id = GameApiServer.extractInt(created.body(), "id", -1);

        // Update
        Response r = request("PUT", "/api/players/" + id,
            "{\"balance\":999,\"wins\":3}");
        assertEquals(200, r.status());
        assertTrue(r.body().contains("\"balance\":999"));
        assertTrue(r.body().contains("\"wins\":3"));
    }

    @Test
    @Order(7)
    public void testDeletePlayer() throws Exception {
        // Create
        Response created = request("POST", "/api/players",
            "{\"name\":\"Dave\",\"balance\":100}");
        int id = GameApiServer.extractInt(created.body(), "id", -1);

        // Delete
        Response del = request("DELETE", "/api/players/" + id, null);
        assertEquals(200, del.status());

        // Confirm gone
        Response get = request("GET", "/api/players/" + id, null);
        assertEquals(404, get.status());
    }

    @Test
    @Order(8)
    public void testDeletePlayerNotFound() throws Exception {
        Response r = request("DELETE", "/api/players/99999", null);
        assertEquals(404, r.status());
    }

    // ── Game-session endpoint tests ───────────────────────────────────────────

    @Test
    @Order(10)
    public void testGetGamesInitiallyEmpty() throws Exception {
        Response r = request("GET", "/api/games", null);
        assertEquals(200, r.status());
        assertEquals("[]", r.body());
    }

    @Test
    @Order(11)
    public void testCreateGame() throws Exception {
        Response r = request("POST", "/api/games",
            "{\"gameName\":\"Blackjack\",\"hostPlayer\":\"Alice\"}");
        assertEquals(201, r.status());
        assertTrue(r.body().contains("\"gameName\":\"Blackjack\""));
        assertTrue(r.body().contains("\"status\":\"WAITING\""));
    }

    @Test
    @Order(12)
    public void testCreateGameMissingFieldsReturns400() throws Exception {
        Response r = request("POST", "/api/games", "{\"gameName\":\"Poker\"}");
        assertEquals(400, r.status());
        assertTrue(r.body().contains("error"));
    }

    @Test
    @Order(13)
    public void testGetGameById() throws Exception {
        Response created = request("POST", "/api/games",
            "{\"gameName\":\"Poker\",\"hostPlayer\":\"Bob\"}");
        int id = GameApiServer.extractInt(created.body(), "id", -1);
        assertTrue(id > 0);

        Response r = request("GET", "/api/games/" + id, null);
        assertEquals(200, r.status());
        assertTrue(r.body().contains("\"gameName\":\"Poker\""));
    }

    @Test
    @Order(14)
    public void testGetGameByIdNotFound() throws Exception {
        Response r = request("GET", "/api/games/99999", null);
        assertEquals(404, r.status());
    }

    @Test
    @Order(15)
    public void testJoinGame() throws Exception {
        Response created = request("POST", "/api/games",
            "{\"gameName\":\"War\",\"hostPlayer\":\"Host\"}");
        int id = GameApiServer.extractInt(created.body(), "id", -1);

        Response r = request("POST", "/api/games/" + id + "/join",
            "{\"playerName\":\"Guest\"}");
        assertEquals(200, r.status());
        assertTrue(r.body().contains("\"Guest\""));
    }

    @Test
    @Order(16)
    public void testJoinGameMissingPlayerNameReturns400() throws Exception {
        Response created = request("POST", "/api/games",
            "{\"gameName\":\"GoFish\",\"hostPlayer\":\"Host2\"}");
        int id = GameApiServer.extractInt(created.body(), "id", -1);

        Response r = request("POST", "/api/games/" + id + "/join", "{}");
        assertEquals(400, r.status());
    }

    @Test
    @Order(17)
    public void testStartGame() throws Exception {
        Response created = request("POST", "/api/games",
            "{\"gameName\":\"CrazyEights\",\"hostPlayer\":\"Host3\"}");
        int id = GameApiServer.extractInt(created.body(), "id", -1);

        Response r = request("POST", "/api/games/" + id + "/start", null);
        assertEquals(200, r.status());
        assertTrue(r.body().contains("\"status\":\"IN_PROGRESS\""));
    }

    @Test
    @Order(18)
    public void testStartAlreadyStartedGameReturns409() throws Exception {
        Response created = request("POST", "/api/games",
            "{\"gameName\":\"Blackjack\",\"hostPlayer\":\"Host4\"}");
        int id = GameApiServer.extractInt(created.body(), "id", -1);

        request("POST", "/api/games/" + id + "/start", null); // first start

        Response r = request("POST", "/api/games/" + id + "/start", null); // second start
        assertEquals(409, r.status());
    }

    @Test
    @Order(19)
    public void testFinishGame() throws Exception {
        Response created = request("POST", "/api/games",
            "{\"gameName\":\"Poker\",\"hostPlayer\":\"Host5\"}");
        int id = GameApiServer.extractInt(created.body(), "id", -1);

        Response r = request("POST", "/api/games/" + id + "/finish", null);
        assertEquals(200, r.status());
        assertTrue(r.body().contains("\"status\":\"FINISHED\""));
    }

    @Test
    @Order(20)
    public void testJoinStartedGameReturns409() throws Exception {
        Response created = request("POST", "/api/games",
            "{\"gameName\":\"War\",\"hostPlayer\":\"Host6\"}");
        int id = GameApiServer.extractInt(created.body(), "id", -1);
        request("POST", "/api/games/" + id + "/start", null);

        Response r = request("POST", "/api/games/" + id + "/join",
            "{\"playerName\":\"LateJoiner\"}");
        assertEquals(409, r.status());
    }

    @Test
    @Order(21)
    public void testListGamesReturnsAllCreated() throws Exception {
        // Cleanup from previous tests already created sessions;
        // just verify the list endpoint works and is an array
        Response r = request("GET", "/api/games", null);
        assertEquals(200, r.status());
        assertTrue(r.body().startsWith("["));
        assertTrue(r.body().endsWith("]"));
    }

    // ── JSON helper tests ─────────────────────────────────────────────────────

    @Test
    public void testExtractString() {
        assertEquals("Alice",
            GameApiServer.extractString("{\"name\":\"Alice\",\"game\":\"Poker\"}", "name"));
        assertEquals("Poker",
            GameApiServer.extractString("{\"name\":\"Alice\",\"game\":\"Poker\"}", "game"));
        assertNull(GameApiServer.extractString("{}", "missing"));
    }

    @Test
    public void testExtractInt() {
        assertEquals(500,
            GameApiServer.extractInt("{\"balance\":500}", "balance", -1));
        assertEquals(-1,
            GameApiServer.extractInt("{}", "balance", -1));
    }
}
