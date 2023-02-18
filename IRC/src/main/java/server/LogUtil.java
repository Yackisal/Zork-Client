package server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {
    private final static String LOG_PATH = System.getProperty("user.dir") + "/latest.log";

    public static void log(String content) {
        SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String log = "[" + sdt.format(new Date()) + "] " + content;
        System.out.println(log);
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(LOG_PATH,true));
            out.write(log + "\n");
            out.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void warning(String content) {
        log("[Warning] " + content);
    }

    public static void error(String content) {
        log("[Error] " + content);
    }

    public static void info(String content) {
        log("[Info] " + content);
    }

}
