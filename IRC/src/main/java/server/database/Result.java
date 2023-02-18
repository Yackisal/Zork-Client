package server.database;

public class Result {
    public boolean success = false;
    public String data = "";
    public String tag = "";

    public Result(boolean success, String data) {
        this.success = success;
        this.data = data;
    }

    public Result(boolean success, String data, String tag) {
        this.success = success;
        this.data = data;
        this.tag = tag;
    }
}
