import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GameSession}.
 */
public class GameSessionTest {

    @Test
    public void testInitialState() {
        GameSession gs = new GameSession(1, "Blackjack", "Alice");
        assertEquals(1,                    gs.getId());
        assertEquals("Blackjack",          gs.getGameName());
        assertEquals("Alice",              gs.getHostPlayer());
        assertEquals(GameSession.Status.WAITING, gs.getStatus());
        assertEquals(1,                    gs.getPlayers().size());
        assertTrue(gs.getPlayers().contains("Alice"));
    }

    @Test
    public void testAddPlayer() {
        GameSession gs = new GameSession(1, "Poker", "Host");
        gs.addPlayer("Bob");
        assertEquals(2, gs.getPlayers().size());
        assertTrue(gs.getPlayers().contains("Bob"));
    }

    @Test
    public void testAddPlayerToStartedSessionThrows() {
        GameSession gs = new GameSession(1, "Poker", "Host");
        gs.start();
        assertThrows(IllegalStateException.class, () -> gs.addPlayer("Late"));
    }

    @Test
    public void testAddBlankPlayerNameThrows() {
        GameSession gs = new GameSession(1, "Poker", "Host");
        assertThrows(IllegalArgumentException.class, () -> gs.addPlayer("  "));
    }

    @Test
    public void testAddNullPlayerNameThrows() {
        GameSession gs = new GameSession(1, "Poker", "Host");
        assertThrows(IllegalArgumentException.class, () -> gs.addPlayer(null));
    }

    @Test
    public void testMaxPlayersEnforced() {
        GameSession gs = new GameSession(1, "War", "P1");
        for (int i = 2; i <= GameSession.MAX_PLAYERS; i++) {
            gs.addPlayer("P" + i);
        }
        assertThrows(IllegalStateException.class, () -> gs.addPlayer("Extra"));
    }

    @Test
    public void testStartTransitionsToInProgress() {
        GameSession gs = new GameSession(1, "GoFish", "Host");
        gs.start();
        assertEquals(GameSession.Status.IN_PROGRESS, gs.getStatus());
    }

    @Test
    public void testStartAlreadyStartedThrows() {
        GameSession gs = new GameSession(1, "GoFish", "Host");
        gs.start();
        assertThrows(IllegalStateException.class, gs::start);
    }

    @Test
    public void testFinishTransitionsToFinished() {
        GameSession gs = new GameSession(1, "CrazyEights", "Host");
        gs.finish();
        assertEquals(GameSession.Status.FINISHED, gs.getStatus());
    }

    @Test
    public void testFinishAlreadyFinishedThrows() {
        GameSession gs = new GameSession(1, "CrazyEights", "Host");
        gs.finish();
        assertThrows(IllegalStateException.class, gs::finish);
    }

    @Test
    public void testToJsonContainsExpectedFields() {
        GameSession gs = new GameSession(7, "Blackjack", "Alice");
        gs.addPlayer("Bob");
        String json = gs.toJson();
        assertTrue(json.contains("\"id\":7"));
        assertTrue(json.contains("\"gameName\":\"Blackjack\""));
        assertTrue(json.contains("\"hostPlayer\":\"Alice\""));
        assertTrue(json.contains("\"status\":\"WAITING\""));
        assertTrue(json.contains("\"Alice\""));
        assertTrue(json.contains("\"Bob\""));
    }

    @Test
    public void testPlayersListIsUnmodifiable() {
        GameSession gs = new GameSession(1, "War", "Host");
        assertThrows(UnsupportedOperationException.class,
            () -> gs.getPlayers().add("Intruder"));
    }
}
