/**
 * Persistent player profile stored in the H2 database.
 * Tracks a player's name, current balance, and lifetime game statistics.
 *
 * @author Tom Burchell
 * @version 1.0
 */
public class PlayerProfile {

    private int id;
    private String name;
    private int balance;
    private int wins;
    private int losses;
    private int ties;

    /** Default constructor (required for deserialization). */
    public PlayerProfile() {}

    /**
     * Creates a new profile with the given name and starting balance.
     *
     * @param name    player's display name
     * @param balance starting chip balance
     */
    public PlayerProfile(String name, int balance) {
        this.name    = name;
        this.balance = balance;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public int    getId()      { return id; }
    public void   setId(int id){ this.id = id; }

    public String getName()          { return name; }
    public void   setName(String n)  { this.name = n; }

    public int    getBalance()       { return balance; }
    public void   setBalance(int b)  { this.balance = b; }

    public int    getWins()          { return wins; }
    public void   setWins(int w)     { this.wins = w; }

    public int    getLosses()        { return losses; }
    public void   setLosses(int l)   { this.losses = l; }

    public int    getTies()          { return ties; }
    public void   setTies(int t)     { this.ties = t; }

    /**
     * Serializes this profile to a JSON object string.
     *
     * @return JSON representation of the profile
     */
    public String toJson() {
        return String.format(
            "{\"id\":%d,\"name\":\"%s\",\"balance\":%d,"
            + "\"wins\":%d,\"losses\":%d,\"ties\":%d}",
            id, escapeJson(name), balance, wins, losses, ties
        );
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public String toString() {
        return String.format("PlayerProfile{id=%d, name='%s', balance=%d, "
            + "wins=%d, losses=%d, ties=%d}", id, name, balance, wins, losses, ties);
    }
}
