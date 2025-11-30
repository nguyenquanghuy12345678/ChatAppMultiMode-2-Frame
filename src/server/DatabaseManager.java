package server;

import common.Message;
import java.sql.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private Connection connection;
    private final String DB_URL = "jdbc:sqlite:chat_data.db";

    public DatabaseManager() {
        connect();
        if (connection != null) createTables();
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException cnf) {
            System.err.println("[DatabaseManager] SQLite JDBC driver not found. Add sqlite-jdbc jar to classpath (lib\\sqlite-jdbc-<version>.jar)." );
            cnf.printStackTrace();
            return;
        }
        try {
            boolean isNew = !new File("chat_data.db").exists();
            connection = DriverManager.getConnection(DB_URL);
            if (isNew) System.out.println("⚠️ Created new DB.");
            System.out.println("✅ Database Connected.");
        } catch (SQLException se) {
            System.err.println("[DatabaseManager] Failed to connect to SQLite database at " + DB_URL);
            se.printStackTrace();
        }
    }

    private void createTables() {
        if (connection == null) return;
        try (Statement stmt = connection.createStatement()) {
            // Bảng Users
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "username TEXT PRIMARY KEY, password TEXT NOT NULL)");
            
            // Bảng Messages (Thêm cột type để biết là file hay text)
            stmt.execute("CREATE TABLE IF NOT EXISTS messages (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "sender TEXT, receiver TEXT, content TEXT, " +
                    "timestamp TEXT, msg_type TEXT)");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public boolean registerUser(String u, String p) {
        try (PreparedStatement pst = connection.prepareStatement("INSERT INTO users VALUES(?, ?)")) {
            pst.setString(1, u); pst.setString(2, p);
            pst.executeUpdate(); return true;
        } catch (SQLException e) { return false; }
    }

    public boolean checkLogin(String u, String p) {
        try (PreparedStatement pst = connection.prepareStatement("SELECT * FROM users WHERE username=? AND password=?")) {
            pst.setString(1, u); pst.setString(2, p);
            return pst.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    // --- LƯU TIN NHẮN ---
    public void saveMessage(String sender, String receiver, String content, String timestamp, String type) {
        String sql = "INSERT INTO messages(sender, receiver, content, timestamp, msg_type) VALUES(?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, sender);
            pst.setString(2, receiver);
            pst.setString(3, content);
            pst.setString(4, timestamp);
            pst.setString(5, type);
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- LẤY LỊCH SỬ ---
    public List<Message> getChatHistory(String u1, String u2) {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE (sender=? AND receiver=?) OR (sender=? AND receiver=?) ORDER BY id ASC";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, u1); pst.setString(2, u2);
            pst.setString(3, u2); pst.setString(4, u1);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String type = rs.getString("msg_type");
                Message msg;
                
                // Nếu là file, ta chỉ hiện thông báo (vì không lưu file BLOB vào DB để tránh nặng)
                if ("FILE".equals(type)) {
                    msg = new Message(Message.MessageType.FILE_TRANSFER, rs.getString("sender"), "File History");
                    msg.setFileName(rs.getString("content")); // Content lưu tên file
                    msg.setFileData(null); // File cũ không tải lại data để nhẹ máy
                } else {
                    msg = new Message(Message.MessageType.PRIVATE_MSG, rs.getString("sender"), rs.getString("content"));
                }
                
                msg.setReceiver(rs.getString("receiver"));
                msg.setTimestamp(rs.getString("timestamp"));
                list.add(msg);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}