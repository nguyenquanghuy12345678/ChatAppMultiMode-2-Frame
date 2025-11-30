package common;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum MessageType {
        LOGIN,           // Đăng nhập
        REGISTER,        // <-- MỚI: Đăng ký
        LOGOUT,          
        USER_LIST,       
        PRIVATE_MSG,     
        ROOM_MSG,        
        BROADCAST_MSG,   
        JOIN_ROOM,       
        LEAVE_ROOM,      
        ROOM_LIST,       
        CREATE_ROOM,     
        FILE_TRANSFER,   
        SCREENSHOT,      
        MESSAGE_REACTION,
        VIDEO_CALL_REQUEST,   
        VIDEO_CALL_ACCEPT,    
        VIDEO_CALL_REJECT,    
        VIDEO_CALL_END,       
        VIDEO_FRAME,          
        AUDIO_DATA,      // <-- MỚI: Dữ liệu âm thanh
        SUCCESS,
        GET_HISTORY,
        ERROR            
    }
    
    private MessageType type;
    private String sender;
    private String receiver;
    private String content;
    private String timestamp;
    private Object data;
    
    // --- MỚI: Password cho DB ---
    private String password;

    // File transfer fields
    private String fileName;
    private byte[] fileData;
    private long fileSize;
    
    // Reaction fields
    private String messageId;
    private String reactionType;
    
    // Video/Audio call fields
    private String callId;
    private boolean videoEnabled;
    private boolean audioEnabled;
    
    public Message(MessageType type, String sender, String content) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy")
        );
    }
    
    // Getters and Setters MỚI
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // Các Getters and Setters CŨ (Giữ nguyên)
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
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getReactionType() { return reactionType; }
    public void setReactionType(String reactionType) { this.reactionType = reactionType; }
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