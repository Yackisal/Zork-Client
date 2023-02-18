package server;

import com.mysql.cj.log.Log;
import management.User;
import message.IRCData;
import message.Type;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import server.database.DBHelper;
import server.database.Result;
import utils.MessageUtil;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebSocketServer extends org.java_websocket.server.WebSocketServer {
    // 用户
    public static HashMap<WebSocket, User> users = new HashMap<WebSocket, User>();

    // 黑名单
    private HashMap<String, Integer> blackList = new HashMap<>();
    // 登录次数
    private HashMap<String, Integer> loginCount = new HashMap<>();
    // 重置黑名单间隔
    private int resetInterval = 3600;


    public WebSocketServer(InetSocketAddress address) {
        super(address);

        // 定时重置黑名单
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {
            blackList.clear();
        }, resetInterval, resetInterval, TimeUnit.SECONDS);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String ip = conn.getRemoteSocketAddress().getAddress().getHostAddress();
        String url = handshake.getResourceDescriptor();

        // 判断是否在黑名单中
        if (blackList.containsKey(ip)) {
            LogUtil.warning("A blacklisted ip tried to connect!");
            conn.close();
            return;
        } else {
//            LogUtil.info("new connect: " + url);
        }
        HashMap<String, String> headers = new HashMap<>();
        try {
            URI uri = new URI(url);
            String query = uri.getQuery();
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                String key = keyValue[0];
                String value = keyValue[1];
                headers.put(key, value);
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        // 记录登录次数
        if (!loginCount.containsKey(ip)) {
            loginCount.put(ip, 1);
        } else {
            int count = loginCount.get(ip) + 1;
            loginCount.put(ip, count);

            // 如果登录次数超过40次，加入黑名单
            if (count > 40) {
                blackList.put(ip, count);
                conn.close();
                return;
            }
        }

        String type = headers.get("type");
        if (type.equals("login")) {
            // 获取参数
            String username = headers.get("username");
            String password = headers.get("password");
            String hwid = headers.get("hwid");
            // 调用login方法
            Result login = DBHelper.login(username, password, hwid);
            if (login.success) {
                conn.send(login.data);
                WebSocket remove = null;
                for (Map.Entry<WebSocket, User> webSocketUserEntry : users.entrySet()) {
                    if(webSocketUserEntry.getValue().username.equals(username)){
                        webSocketUserEntry.getKey().close();
                        remove = webSocketUserEntry.getKey();
                        break;
                    }
                }
                users.remove(remove);
                users.put(conn, new User(username, password, hwid, login.tag));
                LogUtil.info("User " + username + " logged in!");
            } else {
                conn.send(login.data);
                conn.close(-1, "");
            }
        } else if (type.equals("register")) {
            String username = headers.get("username");
            String password = headers.get("password");
            String key = headers.get("key");
            // 调用register方法
            Result register = DBHelper.register(username, password, key);
            if (register.success) {
                conn.send(register.data);
                users.put(conn, new User(username, password, key, register.tag));
            } else {
                conn.send(register.data);
                conn.close(20, register.data);
            }
        }
    }


    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        // 移除用户
        users.remove(conn);
        dispatchInfo("User: " + users.get(conn).username + " had left the server!");
        LogUtil.info("User: " + users.get(conn).username + " had left the server!");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // 获取用户
        User user = users.get(conn);
        if (user != null) {
            IRCData ircData = MessageUtil.fromMessage(message);
            switch (ircData.type) {
                case MESSAGE: {
                    // 获取参数
                    String msg = ircData.attributes.get("msg");
                    if (msg.contains("\247")) {
                        conn.close(-1, "Illegal character!");
                        return;
                    }
                    // 调用sendMessage方法
                    dispatchMessage(user, msg);
                    break;
                }
                case COMMAND: {
                    // 获取参数
                    String cmd = ircData.attributes.get("command");
                    String msg = "";
                    switch (cmd) {
                        case "kick": {
                            // 获取参数
                            String target = ircData.attributes.get("target");
                            String reason = ircData.attributes.get("reason");
                            msg = user.username + " kicked " + target + " because " + reason;
                            // 调用kick方法
                            for (Map.Entry<WebSocket, User> webSocketUserEntry : users.entrySet()) {
                                if (webSocketUserEntry.getValue().username.equals(target)) {
                                    webSocketUserEntry.getKey().close(-1, "You have been kicked by " + user.username + " because " + reason);
                                    dispatchInfo(msg);
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    break;
                }
                case ERROR: {
                    break;
                }
                case UNKNOWN: {
                    user.gameID = ircData.attributes.get("id");
                    break;
                }
            }
        } else {
            conn.close(1, "Don't try to send message without login!");
        }
    }


    private void dispatchInfo(String msg) {
        for (Map.Entry<WebSocket, User> webSocketUserEntry : users.entrySet()) {
            WebSocket conn = webSocketUserEntry.getKey();
            User user1 = webSocketUserEntry.getValue();
            String text = "\2472[Skidder IRC] \247r" + msg;
            if (user1 != null) {
                conn.send(MessageUtil.makeMessage(Type.MESSAGE, "msg=" + text));
            }
        }
    }

    private void dispatchMessage(User user, String msg) {
        for (Map.Entry<WebSocket, User> webSocketUserEntry : users.entrySet()) {
            WebSocket conn = webSocketUserEntry.getKey();
            User user1 = webSocketUserEntry.getValue();
            String text = "\2472[Skidder IRC] \247r" + user.rank.inGame + user.username + "\247r: " + msg;
            if (user1 != null) {
                conn.send(MessageUtil.makeMessage(Type.MESSAGE, "msg=" + text));
            }
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        users.remove(conn);
        LogUtil.error(ex.getMessage());
    }

    @Override
    public void onStart() {

    }
}
