package common;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum MessageType {
        LOGIN,           // Đăng nhập
        LOGOUT,          // Đăng xuất
        USER_LIST,       // Danh sách user online
        PRIVATE_MSG,     // Chat 1-1
        ROOM_MSG,        // Chat trong room
        BROADCAST_MSG,   // Chat tất cả
        JOIN_ROOM,       // Tham gia room
        LEAVE_ROOM,      // Rời room
        ROOM_LIST,       // Danh sách rooms
        CREATE_ROOM,     // Tạo room mới
        SUCCESS,         // Thành công
        ERROR            // Lỗi
    }
    
    private MessageType type;
    private String sender;
    private String receiver;  // Cho private chat hoặc room name
    private String content;
    private String timestamp;
    private Object data;      // Dữ liệu bổ sung (danh sách user, room...)
    
    public Message(MessageType type, String sender, String content) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy")
        );
    }
    
    // Getters and Setters
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
    
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    
    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
    
    @Override
    public String toString() {
        return String.format("[%s] %s -> %s: %s", timestamp, sender, receiver, content);
    }
}
