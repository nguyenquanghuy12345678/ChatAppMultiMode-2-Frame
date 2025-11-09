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
        FILE_TRANSFER,   // Gửi file/ảnh
        SCREENSHOT,      // Gửi screenshot
        MESSAGE_REACTION,// Reaction vào tin nhắn
        VIDEO_CALL_REQUEST,   // Yêu cầu video call
        VIDEO_CALL_ACCEPT,    // Chấp nhận video call
        VIDEO_CALL_REJECT,    // Từ chối video call
        VIDEO_CALL_END,       // Kết thúc video call
        VIDEO_FRAME,          // Frame video data
        AUDIO_FRAME,          // Frame audio data
        SUCCESS,         // Thành công
        ERROR            // Lỗi
    }
    
    private MessageType type;
    private String sender;
    private String receiver;  // Cho private chat hoặc room name
    private String content;
    private String timestamp;
    private Object data;      // Dữ liệu bổ sung (danh sách user, room...)
    
    // File transfer fields
    private String fileName;
    private byte[] fileData;
    private long fileSize;
    
    // Reaction fields
    private String messageId;      // ID của tin nhắn được react
    private String reactionType;   // Loại reaction (❤️, 👍, 😂, etc.)
    
    // Video call fields
    private String callId;         // ID của cuộc gọi
    private boolean videoEnabled;  // Bật/tắt video
    private boolean audioEnabled;  // Bật/tắt audio
    
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
    
    // File transfer getters and setters
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }
    
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    
    // Reaction getters and setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    
    public String getReactionType() { return reactionType; }
    public void setReactionType(String reactionType) { this.reactionType = reactionType; }
    
    // Video call getters and setters
    public String getCallId() { return callId; }
    public void setCallId(String callId) { this.callId = callId; }
    
    public boolean isVideoEnabled() { return videoEnabled; }
    public void setVideoEnabled(boolean videoEnabled) { this.videoEnabled = videoEnabled; }
    
    public boolean isAudioEnabled() { return audioEnabled; }
    public void setAudioEnabled(boolean audioEnabled) { this.audioEnabled = audioEnabled; }
    
    @Override
    public String toString() {
        return String.format("[%s] %s -> %s: %s", timestamp, sender, receiver, content);
    }
}
