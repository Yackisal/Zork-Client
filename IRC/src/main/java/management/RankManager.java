package management;

public class RankManager {


    public enum Ranks {
        Admin("\2474[Admin]"),
        Beta("\2471[Beta]"),
        Moderator("\2476[Moderator]"),
        User("\2477"),
        Zork("\2475[Zork]"),
        ZorkTester("\247a[Zork-Tester]"),

        Backer("\247b[Backer]");

        public final String inGame;

        Ranks(String inGame) {
            this.inGame = inGame;
        }
    }
}
