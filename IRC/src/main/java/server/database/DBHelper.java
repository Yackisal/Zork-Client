package server.database;

import message.Type;
import server.LogUtil;
import utils.MD5Utils;
import utils.MessageUtil;

import java.sql.*;
import java.text.SimpleDateFormat;

public class DBHelper {
    static Connection connection;

    public static String address;
    public static String port;
    public static String name;
    public static String userName;
    public static String pwd;

    public static void init(String address, String port, String dbName, String user, String pwd) {
        DBHelper.address = address;
        DBHelper.port = port;
        DBHelper.name = dbName;
        DBHelper.userName = user;
        DBHelper.pwd = pwd;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("jdbc:mysql://" + address + ":" + port + "/" + dbName);
            connection = DriverManager.getConnection("jdbc:mysql://" + address + ":" + port + "/" + dbName + "?serverTimezone=UTC&autoReconnect=true&autoReconnectForPools=true&zeroDateTimeBehavior=convertToNull", user, pwd);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void reconnect() {
        try {
            connection.close();
            connection = DriverManager.getConnection("jdbc:mysql://" + address + ":" + port + "/" + name + "?serverTimezone=UTC&autoReconnect=true&autoReconnectForPools=true&zeroDateTimeBehavior=convertToNull", userName, pwd);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Result login(String username, String password, String hwid) {
        LogUtil.info("login: " + username + " " + password + " " + hwid);
        reconnect();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM `users` WHERE `username` = ? AND `password_md5` = ?");
            statement.setString(1, username);
            statement.setString(2, MD5Utils.getMD5(password));
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                if (rs.getString(4).equals(hwid)) {
                    String rank = rs.getString(3);
                    return new Result(true, MessageUtil.makeMessage(Type.Login_Rep, "rank=" + rank, "name=" + username), rank);
                } else {
                    Timestamp timestamp = rs.getTimestamp(5);
                    //if timestamp is null, then the user has never logged in before
                    if (timestamp == null) {
                        //update the hwid
                        PreparedStatement statement1 = connection.prepareStatement("UPDATE `users` SET `hwid` = ?, `updatetime` = ? WHERE `username` = ?");
                        statement1.setString(1, hwid);
                        statement1.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                        statement1.setString(3, username);
                        statement1.executeUpdate();
                        String rank = rs.getString(3);
                        return new Result(true, MessageUtil.makeMessage(Type.Login_Rep, "rank=" + rank, "name=" + username), rank);
                    }
                    long l = timestamp.getTime() - System.currentTimeMillis();
                    int resetTime = 24 * 60 * 60 * 1000;
                    if (l > resetTime) {//判断HWID更新时间
                        PreparedStatement st = connection.prepareStatement("UPDATE `users` SET `hwid`=?,`updatetime`=? WHERE username=?");
                        st.setString(1, hwid);
                        st.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                        st.setString(3, username);
                        st.execute();
                        LogUtil.info("User " + username + " updated HWID to " + hwid);
                        return new Result(false, MessageUtil.makeMessage(Type.ERROR, "msg=Reset your HardwareID to " + hwid));
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                        LogUtil.error("User " + username + " failed to update HWID");
                        return new Result(false, MessageUtil.makeMessage(Type.ERROR, "msg=HWID is unverified, and you can't update your HWID until " + sdf.format(rs.getTimestamp(5).getTime() + resetTime)));
                    }
                }
            }
            statement.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Result(false, MessageUtil.makeMessage(Type.ERROR, "msg=Failed to login, please check your username and password"));
    }

    public static Result register(String username, String password, String key) {
        reconnect();
        password = MD5Utils.getMD5(password);
        String rank = null;
        try {
            // 检查用户是否已存在
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `users` WHERE `username` = ?");
            preparedStatement.setString(1, username);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                return new Result(false, MessageUtil.makeMessage(Type.ERROR, "msg=User already exist"));
            }
            // 检查key是否可用
            if (!key.isEmpty()) {
                preparedStatement = connection.prepareStatement("SELECT * FROM `activekeys` WHERE `activekey` = ?");
                preparedStatement.setString(1, key);
                rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    rank = rs.getString(2);
                }
                if (rank == null) {
                    return new Result(false, MessageUtil.makeMessage(Type.ERROR, "msg=Activation key not exists."));
                }
            }

            //注册用户
            preparedStatement = connection.prepareStatement("INSERT INTO `users` (`username`, `password_md5`, `rank`, `hwid`, `updatetime`,`activekey`) VALUES ( ? , ? , ?, ?, ?,?);");
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, rank);
            preparedStatement.setString(4, "");
            preparedStatement.setLong(5, 0);
            preparedStatement.setString(6, key);
            boolean reg = preparedStatement.execute();

            //删除key
            if (!key.isEmpty()) {
                preparedStatement = connection.prepareStatement("DELETE FROM activekeys WHERE activekey = ?");
                preparedStatement.setString(1, key);
                if (preparedStatement.execute()) {
                    LogUtil.error("failed to remove key:" + key);
                }
            }

            preparedStatement.close();
            rs.close();
            if (!reg) {
                return new Result(true, MessageUtil.makeMessage(Type.MESSAGE, "msg=Failed to register."));
            } else {
                return new Result(false, MessageUtil.makeMessage(Type.MESSAGE, "msg=Register successfully."));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Result(false, MessageUtil.makeMessage(Type.ERROR, "msg=Unknown error."), rank);
    }


    public static void record(String username, String ip, String time) {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = connection.prepareStatement("INSERT INTO `ip_record` (`username`, `ip`, `time`) VALUES ( ? , ? , ?);");
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, ip);
            preparedStatement.setString(3, time);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

