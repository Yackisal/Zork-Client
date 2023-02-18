package management;

public class User {
    public String username;
    public String password;
    public String hwid;

    public RankManager.Ranks rank;
    public String gameID;


    public User(String username, String password, String hwid, String rank) {
        this.username = username;
        this.password = password;
        this.hwid = hwid;
        this.rank = RankManager.Ranks.valueOf(rank);
    }

}
