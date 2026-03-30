import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for {@link PlayerRepository} using an in-memory H2 database.
 * Each test class invocation uses a unique database name so tests are
 * completely isolated and can run in parallel.
 */
public class PlayerRepositoryTest {

    private static final AtomicInteger DB_COUNTER = new AtomicInteger(0);

    private PlayerRepository repo;

    @BeforeEach
    public void setUp() {
        // Each test gets its own in-memory database to ensure isolation.
        String url = "jdbc:h2:mem:test_" + DB_COUNTER.incrementAndGet()
                   + ";DB_CLOSE_DELAY=-1";
        repo = new PlayerRepository(url, "sa", "");
    }

    // ── save ─────────────────────────────────────────────────────────────────

    @Test
    public void testSaveAssignsId() {
        PlayerProfile p = new PlayerProfile("Alice", 500);
        repo.save(p);
        assertTrue(p.getId() > 0, "save() should assign a positive id");
    }

    @Test
    public void testSavePreservesFields() {
        PlayerProfile p = new PlayerProfile("Bob", 750);
        p.setWins(2);
        p.setLosses(1);
        p.setTies(3);
        repo.save(p);

        PlayerProfile fetched = repo.findById(p.getId());
        assertNotNull(fetched);
        assertEquals("Bob", fetched.getName());
        assertEquals(750,   fetched.getBalance());
        assertEquals(2,     fetched.getWins());
        assertEquals(1,     fetched.getLosses());
        assertEquals(3,     fetched.getTies());
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    public void testFindByIdReturnsNullWhenMissing() {
        assertNull(repo.findById(999));
    }

    @Test
    public void testFindByIdReturnsCorrectProfile() {
        PlayerProfile p = new PlayerProfile("Carol", 300);
        repo.save(p);
        PlayerProfile found = repo.findById(p.getId());
        assertNotNull(found);
        assertEquals("Carol", found.getName());
        assertEquals(300,     found.getBalance());
    }

    // ── findByName ────────────────────────────────────────────────────────────

    @Test
    public void testFindByNameReturnsProfile() {
        repo.save(new PlayerProfile("Dave", 100));
        PlayerProfile found = repo.findByName("Dave");
        assertNotNull(found);
        assertEquals("Dave", found.getName());
    }

    @Test
    public void testFindByNameReturnsNullWhenMissing() {
        assertNull(repo.findByName("NoSuchPlayer"));
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    public void testFindAllEmptyDatabase() {
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    public void testFindAllReturnsAllProfiles() {
        repo.save(new PlayerProfile("Eve",   100));
        repo.save(new PlayerProfile("Frank", 200));
        repo.save(new PlayerProfile("Grace", 300));

        List<PlayerProfile> all = repo.findAll();
        assertEquals(3, all.size());
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    public void testUpdateChangesFields() {
        PlayerProfile p = new PlayerProfile("Heidi", 400);
        repo.save(p);

        p.setBalance(999);
        p.setWins(5);
        boolean updated = repo.update(p);
        assertTrue(updated);

        PlayerProfile refreshed = repo.findById(p.getId());
        assertNotNull(refreshed);
        assertEquals(999, refreshed.getBalance());
        assertEquals(5,   refreshed.getWins());
    }

    @Test
    public void testUpdateReturnsFalseForMissingId() {
        PlayerProfile ghost = new PlayerProfile("Ghost", 0);
        ghost.setId(99999);
        assertFalse(repo.update(ghost));
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    public void testDeleteRemovesProfile() {
        PlayerProfile p = new PlayerProfile("Ivan", 200);
        repo.save(p);
        int id = p.getId();

        assertTrue(repo.delete(id));
        assertNull(repo.findById(id));
    }

    @Test
    public void testDeleteReturnsFalseForMissingId() {
        assertFalse(repo.delete(99999));
    }

    // ── multiple profiles ─────────────────────────────────────────────────────

    @Test
    public void testMultipleProfilesAreIndependent() {
        PlayerProfile p1 = new PlayerProfile("Judy",  100);
        PlayerProfile p2 = new PlayerProfile("Karl",  200);
        repo.save(p1);
        repo.save(p2);

        assertNotEquals(p1.getId(), p2.getId());

        p1.setBalance(500);
        repo.update(p1);

        // p2 should be unchanged
        assertEquals(200, repo.findById(p2.getId()).getBalance());
    }
}
