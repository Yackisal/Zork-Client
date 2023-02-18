package server;

import server.database.DBHelper;

import java.net.InetSocketAddress;

public class ServerMain {
    static int port = 5557;
    public static String dbAddress, dbPort, dbName, dbUserName, dbPWD;
    public static boolean debug = false;


    public static void main(String[] args) {
        System.out.println("  _____   _____     _____ \n" +
                " |_   _| |  __ \\   / ____|\n" +
                "   | |   | |__) | | |     \n" +
                "   | |   |  _  /  | |     \n" +
                "  _| |_  | | \\ \\  | |____ \n" +
                " |_____| |_|  \\_\\  \\_____|");
        try {
            if (!args[0].isEmpty()) {
                port = Integer.parseInt(args[0]);
            }
            if (args[1].equals("on")) {
                debug = true;
            }
            dbAddress = args[2];
            dbPort = args[3];
            dbName = args[4];
            dbUserName = args[5];
            dbPWD = args[6];
        } catch (Exception e) {
            System.out.println("Error! Arguments: [port] [debug(on/off)] [dbAddress] [dbPort] [dbName] [dbUserName] [dbPassword]");
            System.exit(0);
        }
        DBHelper.init(dbAddress, dbPort, dbName, dbUserName, dbPWD);

        WebSocketServer webSocketServer = new WebSocketServer(new InetSocketAddress(port));
        webSocketServer.start();
        System.out.println("Server started on port " + port);
    }
}
