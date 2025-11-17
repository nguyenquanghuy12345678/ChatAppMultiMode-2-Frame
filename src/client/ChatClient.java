package client;

import common.ChatRoom;
import common.Message;
import common.User;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.List;

public class ChatClient {
    private String serverIP;
    private int serverPort;
    private String username;
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private ClientUI ui;
    private boolean connected;
    
    // Video call state
    private VideoCallWindow activeCallWindow = null;
    private String currentCallId = null;
    private volatile boolean isInCall = false;
    private volatile boolean isCallDialogOpen = false;
    
    public ChatClient() {
        this.connected = false;
    }
    
    public void setUI(ClientUI ui) {
        this.ui = ui;
    }
    
    public boolean connect(String serverIP, int serverPort, String username) {
        try {
            this.serverIP = serverIP;
            this.serverPort = serverPort;
            this.username = username;
            
            // Kết nối đến server
            socket = new Socket(serverIP, serverPort);
            
            // Configure socket to prevent auto-close
            socket.setKeepAlive(true);
            socket.setSoTimeout(300000); // 5 minutes timeout
            socket.setTcpNoDelay(true); // Low latency for video
            
            // Khởi tạo streams
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
            
            // Gửi thông tin đăng nhập
            Message loginMsg = new Message(Message.MessageType.LOGIN, username, "");
            sendMessage(loginMsg);
            
            // Đợi phản hồi
            Message response = (Message) input.readObject();
            
            if (response.getType() == Message.MessageType.SUCCESS) {
                connected = true;
                
                // Bắt đầu lắng nghe messages
                new Thread(this::listenForMessages).start();
                
                return true;
            } else {
                ui.showError(response.getContent());
                disconnect();
                return false;
            }
            
        } catch (IOException | ClassNotFoundException e) {
            ui.showError("Không thể kết nối đến server: " + e.getMessage());
            return false;
        }
    }
    
    private void listenForMessages() {
        try {
            while (connected && socket != null && !socket.isClosed()) {
                try {
                    Message msg = (Message) input.readObject();
                    if (msg != null) {
                        handleMessage(msg);
                    }
                } catch (java.net.SocketTimeoutException e) {
                    // Timeout is OK - socket is still alive
                    continue;
                } catch (ClassNotFoundException e) {
                    System.err.println("Unknown message type: " + e.getMessage());
                    continue;
                } catch (java.io.EOFException e) {
                    System.err.println("Server closed connection");
                    break;
                } catch (java.net.SocketException e) {
                    if (connected) {
                        System.err.println("Socket error: " + e.getMessage());
                    }
                    break;
                }
            }
        } catch (IOException e) {
            if (connected) {
                System.err.println("Connection lost: " + e.getMessage());
                javax.swing.SwingUtilities.invokeLater(() -> {
                    ui.showError("Lost connection to server!");
                });
            }
        } finally {
            if (connected) {
                disconnect();
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void handleMessage(Message msg) {
        switch (msg.getType()) {
            case USER_LIST:
                List<User> users = (List<User>) msg.getData();
                ui.updateUserList(users);
                break;
                
            case ROOM_LIST:
                List<ChatRoom> rooms = (List<ChatRoom>) msg.getData();
                ui.updateRoomList(rooms);
                break;
                
            case PRIVATE_MSG:
                ui.displayPrivateMessage(msg);
                break;
                
            case ROOM_MSG:
                ui.displayRoomMessage(msg);
                break;
                
            case BROADCAST_MSG:
                ui.displayBroadcastMessage(msg);
                break;
                
            case FILE_TRANSFER:
                handleFileReceived(msg);
                break;
                
            case SCREENSHOT:
                handleScreenshotReceived(msg);
                break;
                
            case MESSAGE_REACTION:
                handleReactionReceived(msg);
                break;
                
            case VIDEO_CALL_REQUEST:
                handleVideoCallRequest(msg);
                break;
                
            case VIDEO_CALL_ACCEPT:
                handleVideoCallAccept(msg);
                break;
                
            case VIDEO_CALL_REJECT:
                handleVideoCallReject(msg);
                break;
                
            case VIDEO_CALL_END:
                handleVideoCallEnd(msg);
                break;
                
            case VIDEO_FRAME:
                handleVideoFrame(msg);
                break;
                
            case SUCCESS:
                ui.showInfo(msg.getContent());
                break;
                
            case ERROR:
                ui.showError(msg.getContent());
                break;
                
            default:
                break;
        }
    }
    
    public void sendPrivateMessage(String receiver, String content) {
        Message msg = new Message(Message.MessageType.PRIVATE_MSG, username, content);
        msg.setReceiver(receiver);
        sendMessage(msg);
    }
    
    public void sendRoomMessage(String roomName, String content) {
        Message msg = new Message(Message.MessageType.ROOM_MSG, username, content);
        msg.setReceiver(roomName);
        sendMessage(msg);
    }
    
    public void sendBroadcastMessage(String content) {
        Message msg = new Message(Message.MessageType.BROADCAST_MSG, username, content);
        sendMessage(msg);
    }
    
    public void createRoom(String roomName) {
        Message msg = new Message(Message.MessageType.CREATE_ROOM, username, roomName);
        sendMessage(msg);
    }
    
    public void joinRoom(String roomName) {
        Message msg = new Message(Message.MessageType.JOIN_ROOM, username, roomName);
        msg.setContent(roomName);
        sendMessage(msg);
    }
    
    public void leaveRoom(String roomName) {
        Message msg = new Message(Message.MessageType.LEAVE_ROOM, username, roomName);
        msg.setContent(roomName);
        sendMessage(msg);
    }
    
    public void sendFile(File file, String receiver, String mode) {
        try {
            // Read file data
            byte[] fileData = Files.readAllBytes(file.toPath());
            
            // Create file transfer message
            Message msg = new Message(Message.MessageType.FILE_TRANSFER, username, 
                "📁 File: " + file.getName());
            msg.setFileName(file.getName());
            msg.setFileData(fileData);
            msg.setFileSize(file.length());
            
            // Set receiver based on mode
            if (mode.equals("private")) {
                msg.setReceiver(receiver);
            } else if (mode.equals("room")) {
                msg.setReceiver(receiver);
            }
            // broadcast mode: receiver is null
            
            sendMessage(msg);
            
            // Display in UI
            String displayMsg = "📤 Sent file: " + file.getName() + 
                " (" + formatFileSize(file.length()) + ")";
            Message displayMessage = new Message(
                mode.equals("broadcast") ? Message.MessageType.BROADCAST_MSG :
                mode.equals("private") ? Message.MessageType.PRIVATE_MSG :
                Message.MessageType.ROOM_MSG,
                username, displayMsg
            );
            displayMessage.setReceiver(receiver);
            
            if (mode.equals("broadcast")) {
                ui.displayBroadcastMessage(displayMessage);
            } else if (mode.equals("private")) {
                ui.displayPrivateMessage(displayMessage);
            } else if (mode.equals("room")) {
                ui.displayRoomMessage(displayMessage);
            }
            
        } catch (IOException e) {
            ui.showError("Error reading file: " + e.getMessage());
        }
    }
    
    private void handleFileReceived(Message msg) {
        String displayMsg = "📥 Received file: " + msg.getFileName() + 
            " (" + formatFileSize(msg.getFileSize()) + ") from " + msg.getSender();
        
        // Check if it's an image file
        boolean isImage = ImagePreviewDialog.isImageFile(msg.getFileName());
        
        // Show different options based on file type
        String[] options;
        if (isImage) {
            options = new String[]{"Preview", "Save", "Ignore"};
        } else {
            options = new String[]{"Save", "Ignore"};
        }
        
        int choice = javax.swing.JOptionPane.showOptionDialog(
            null,
            displayMsg + "\n\nWhat would you like to do?",
            "File Received",
            javax.swing.JOptionPane.DEFAULT_OPTION,
            javax.swing.JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        // Handle choice
        if (isImage && choice == 0) {
            // Preview image
            ImagePreviewDialog.showImagePreview(
                (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(ui),
                msg.getFileName(),
                msg.getFileData()
            );
        } else if ((isImage && choice == 1) || (!isImage && choice == 0)) {
            // Save file
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            fileChooser.setSelectedFile(new File(msg.getFileName()));
            
            int result = fileChooser.showSaveDialog(null);
            
            if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
                try {
                    File saveFile = fileChooser.getSelectedFile();
                    Files.write(saveFile.toPath(), msg.getFileData());
                    ui.showInfo("File saved successfully to: " + saveFile.getAbsolutePath());
                } catch (IOException e) {
                    ui.showError("Error saving file: " + e.getMessage());
                }
            }
        }
        
        // Display in chat - determine message type from receiver
        Message.MessageType msgType;
        String receiver = msg.getReceiver();
        
        if (receiver == null || receiver.isEmpty()) {
            msgType = Message.MessageType.BROADCAST_MSG;
        } else if (receiver.startsWith("#")) {
            msgType = Message.MessageType.ROOM_MSG;
        } else {
            msgType = Message.MessageType.PRIVATE_MSG;
        }
        
        String fileIcon = isImage ? "🖼️" : "📁";
        Message displayMessage = new Message(msgType, msg.getSender(),
            fileIcon + " " + msg.getFileName() + " (" + formatFileSize(msg.getFileSize()) + ")");
        displayMessage.setReceiver(msg.getReceiver());
        
        if (msgType == Message.MessageType.BROADCAST_MSG) {
            ui.displayBroadcastMessage(displayMessage);
        } else if (msgType == Message.MessageType.PRIVATE_MSG) {
            ui.displayPrivateMessage(displayMessage);
        } else {
            ui.displayRoomMessage(displayMessage);
        }
    }
    
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        }
    }
    
    public synchronized void sendMessage(Message msg) {
        if (!connected || output == null || socket == null || socket.isClosed()) {
            System.err.println("Cannot send message - not connected");
            return;
        }
        
        try {
            output.writeObject(msg);
            output.flush();
        } catch (java.net.SocketException e) {
            System.err.println("Socket error sending message: " + e.getMessage());
            if (connected) {
                connected = false;
                javax.swing.SwingUtilities.invokeLater(() -> 
                    ui.showError("Connection lost - socket closed")
                );
            }
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
            // Don't disconnect on single send error - might be transient
        }
    }
    
    public void disconnect() {
        connected = false;
        
        // Close active call if any
        if (activeCallWindow != null) {
            try {
                activeCallWindow.forceClose();
            } catch (Exception e) {
                // Ignore
            }
        }
        
        cleanupCallState();
        
        try {
            if (output != null) {
                try {
                    Message logoutMsg = new Message(Message.MessageType.LOGOUT, username, "");
                    output.writeObject(logoutMsg);
                    output.flush();
                } catch (IOException e) {
                    // Ignore
                }
            }
        } finally {
            try { if (output != null) output.close(); } catch (IOException e) { /* ignore */ }
            try { if (input != null) input.close(); } catch (IOException e) { /* ignore */ }
            try { if (socket != null) socket.close(); } catch (IOException e) { /* ignore */ }
            
            output = null;
            input = null;
            socket = null;
        }
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public String getUsername() {
        return username;
    }
    
    // ==================== SCREENSHOT METHODS ====================
    
    /**
     * Capture và gửi screenshot
     */
    public void sendScreenshot(String receiver, String mode) {
        try {
            // Capture screen with resize to reduce size (max 800x600)
            byte[] screenshotData = ScreenCaptureUtil.captureAndResize(1280, 720);
            
            String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HHmmss_ddMMyyyy"));
            String fileName = "screenshot_" + timestamp + ".png";
            
            // Create screenshot message
            Message msg = new Message(Message.MessageType.SCREENSHOT, username, 
                "📸 Screenshot");
            msg.setFileName(fileName);
            msg.setFileData(screenshotData);
            msg.setFileSize(screenshotData.length);
            
            // Set receiver based on mode
            if (mode.equals("private")) {
                msg.setReceiver(receiver);
            } else if (mode.equals("room")) {
                msg.setReceiver(receiver);
            }
            // broadcast mode: receiver is null
            
            sendMessage(msg);
            
            ui.showInfo("Screenshot sent successfully! (" + formatFileSize(screenshotData.length) + ")");
            
        } catch (Exception e) {
            ui.showError("Error capturing screenshot: " + e.getMessage());
        }
    }
    
    /**
     * Xử lý screenshot nhận được
     */
    private void handleScreenshotReceived(Message msg) {
        String displayMsg = "📸 Screenshot from " + msg.getSender() + 
            " (" + formatFileSize(msg.getFileSize()) + ")";
        
        // Show notification with preview option
        int choice = javax.swing.JOptionPane.showConfirmDialog(
            null, 
            displayMsg + "\n\nDo you want to view this screenshot?",
            "Screenshot Received",
            javax.swing.JOptionPane.YES_NO_OPTION
        );
        
        if (choice == javax.swing.JOptionPane.YES_OPTION) {
            // Show image preview
            ImagePreviewDialog.showImagePreview(
                (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(ui),
                msg.getFileName(),
                msg.getFileData()
            );
        }
        
        // Display in chat
        Message.MessageType msgType;
        String receiver = msg.getReceiver();
        
        if (receiver == null || receiver.isEmpty()) {
            msgType = Message.MessageType.BROADCAST_MSG;
        } else if (receiver.startsWith("#")) {
            msgType = Message.MessageType.ROOM_MSG;
        } else {
            msgType = Message.MessageType.PRIVATE_MSG;
        }
        
        Message displayMessage = new Message(msgType, msg.getSender(),
            "📸 Screenshot (" + formatFileSize(msg.getFileSize()) + ") [Click to view]");
        displayMessage.setReceiver(msg.getReceiver());
        
        if (msgType == Message.MessageType.BROADCAST_MSG) {
            ui.displayBroadcastMessage(displayMessage);
        } else if (msgType == Message.MessageType.PRIVATE_MSG) {
            ui.displayPrivateMessage(displayMessage);
        } else {
            ui.displayRoomMessage(displayMessage);
        }
    }
    
    // ==================== REACTION METHODS ====================
    
    /**
     * Gửi reaction cho tin nhắn
     */
    public void sendReaction(String messageId, String reactionType, String receiver, String mode) {
        Message msg = new Message(Message.MessageType.MESSAGE_REACTION, username, reactionType);
        msg.setMessageId(messageId);
        msg.setReactionType(reactionType);
        msg.setReceiver(receiver);
        sendMessage(msg);
    }
    
    /**
     * Xử lý reaction nhận được
     */
    private void handleReactionReceived(Message msg) {
        ui.displayReaction(msg);
    }
    
    // ==================== VIDEO CALL METHODS ====================
    
    /**
     * Gửi yêu cầu video call
     */
    public void sendVideoCallRequest(String receiver, boolean videoEnabled, boolean audioEnabled) {
        // Kiểm tra nếu đang trong cuộc gọi hoặc đang có dialog
        if (isInCall || isCallDialogOpen) {
            ui.showError("You are already in a call or waiting for response!");
            return;
        }
        
        if (activeCallWindow != null || currentCallId != null) {
            ui.showError("You are already in a call!");
            return;
        }
        
        // Mark as calling
        isInCall = true;
        String callId = java.util.UUID.randomUUID().toString();
        currentCallId = callId;
        
        Message msg = new Message(Message.MessageType.VIDEO_CALL_REQUEST, username, 
            videoEnabled ? "Video call request" : "Audio call request");
        msg.setReceiver(receiver);
        msg.setCallId(callId);
        msg.setVideoEnabled(videoEnabled);
        msg.setAudioEnabled(audioEnabled);
        sendMessage(msg);
        
        String callType = videoEnabled ? "video call" : "audio call";
        ui.showInfo("Calling " + receiver + " (" + callType + ")...");
        
        // Auto cleanup sau 30s nếu không có response
        new Thread(() -> {
            try {
                Thread.sleep(30000);
                if (currentCallId != null && currentCallId.equals(callId) && activeCallWindow == null) {
                    cleanupCallState();
                    javax.swing.SwingUtilities.invokeLater(() ->
                        ui.showInfo("Call timeout - no response from " + receiver)
                    );
                }
            } catch (InterruptedException e) {
                // Ignore
            }
        }).start();
    }
    
    /**
     * Chấp nhận video call
     */
    public void acceptVideoCall(String caller, String callId) {
        Message msg = new Message(Message.MessageType.VIDEO_CALL_ACCEPT, username, 
            "Call accepted");
        msg.setReceiver(caller);
        msg.setCallId(callId);
        sendMessage(msg);
    }
    
    /**
     * Từ chối video call
     */
    public void rejectVideoCall(String caller, String callId) {
        Message msg = new Message(Message.MessageType.VIDEO_CALL_REJECT, username, 
            "Call rejected");
        msg.setReceiver(caller);
        msg.setCallId(callId);
        sendMessage(msg);
    }
    
    /**
     * Kết thúc video call
     */
    public void endVideoCall(String otherUser, String callId) {
        Message msg = new Message(Message.MessageType.VIDEO_CALL_END, username, 
            "Call ended");
        msg.setReceiver(otherUser);
        msg.setCallId(callId);
        sendMessage(msg);
    }
    
    /**
     * Xử lý yêu cầu video call
     */
    private void handleVideoCallRequest(Message msg) {
        String caller = msg.getSender();
        String callId = msg.getCallId();
        boolean videoEnabled = msg.isVideoEnabled();
        boolean audioEnabled = msg.isAudioEnabled();
        
        // Kiểm tra nếu đang trong cuộc gọi khác
        if (activeCallWindow != null && currentCallId != null) {
            rejectVideoCall(caller, callId);
            ui.showInfo("Busy - Already in another call");
            return;
        }
        
        String callType = videoEnabled ? "Video Call" : "Audio Call";
        
        int choice = javax.swing.JOptionPane.showConfirmDialog(
            null,
            caller + " is calling you (" + callType + ")\n\nDo you want to answer?",
            "Incoming " + callType,
            javax.swing.JOptionPane.YES_NO_OPTION
        );
        
        if (choice == javax.swing.JOptionPane.YES_OPTION) {
            acceptVideoCall(caller, callId);
            currentCallId = callId;
            activeCallWindow = new VideoCallWindow(caller, callId, videoEnabled, this);
            activeCallWindow.setVisible(true);
        } else {
            rejectVideoCall(caller, callId);
        }
    }
    
    /**
     * Xử lý chấp nhận video call
     */
    private void handleVideoCallAccept(Message msg) {
        String receiver = msg.getSender();
        String callId = msg.getCallId();
        
        // Validate call ID
        if (currentCallId == null || !currentCallId.equals(callId)) {
            System.err.println("Call ID mismatch or no active call");
            cleanupCallState();
            return;
        }
        
        // Kiểm tra nếu đã có window (shouldn't happen)
        if (activeCallWindow != null) {
            System.err.println("Window already exists");
            return;
        }
        
        ui.showInfo(receiver + " accepted your call!");
        
        // Chỉ người GỌI mới mở window khi được accept
        try {
            activeCallWindow = new VideoCallWindow(receiver, callId, true, this);
            activeCallWindow.setVisible(true);
        } catch (Exception e) {
            System.err.println("Error opening video window: " + e.getMessage());
            e.printStackTrace();
            cleanupCallState();
            endVideoCall(receiver, callId);
            ui.showError("Failed to start video call: " + e.getMessage());
        }
    }
    
    /**
     * Xử lý từ chối video call
     */
    private void handleVideoCallReject(Message msg) {
        String receiver = msg.getSender();
        cleanupCallState();
        ui.showInfo(receiver + " rejected your call.");
    }
    
    /**
     * Xử lý kết thúc video call
     */
    private void handleVideoCallEnd(Message msg) {
        String sender = msg.getSender();
        
        // Đóng video call window nếu đang mở
        if (activeCallWindow != null) {
            try {
                activeCallWindow.forceClose();
            } catch (Exception e) {
                System.err.println("Error closing window: " + e.getMessage());
            }
        }
        
        cleanupCallState();
        ui.showInfo(sender + " ended the call.");
    }
    
    /**
     * Xử lý video frame nhận được từ remote
     */
    private void handleVideoFrame(Message msg) {
        try {
            if (activeCallWindow != null && currentCallId != null && 
                msg.getCallId() != null && msg.getCallId().equals(currentCallId)) {
                byte[] frameData = msg.getFileData();
                if (frameData != null && frameData.length > 0) {
                    activeCallWindow.displayRemoteFrame(frameData);
                }
            }
        } catch (Exception e) {
            System.err.println("Error handling video frame: " + e.getMessage());
            // Don't crash - just skip this frame
        }
    }
    
    /**
     * Gửi video frame tới remote user
     */
    public void sendVideoFrame(String receiver, String callId, byte[] frameData) {
        if (!connected || receiver == null || callId == null || frameData == null) {
            return;
        }
        try {
            // Limit frame size to prevent network overload
            if (frameData.length > 500_000) { // 500KB max
                System.err.println("Frame too large: " + frameData.length + " bytes - skipping");
                return;
            }
            
            Message msg = new Message(Message.MessageType.VIDEO_FRAME, username, "video frame");
            msg.setReceiver(receiver);
            msg.setCallId(callId);
            msg.setFileData(frameData);
            sendMessage(msg);
        } catch (Exception e) {
            System.err.println("Error sending video frame: " + e.getMessage());
        }
    }
    
    /**
     * Cleanup toàn bộ call state
     */
    private void cleanupCallState() {
        isInCall = false;
        isCallDialogOpen = false;
        activeCallWindow = null;
        currentCallId = null;
    }
    
    /**
     * Cleanup khi đóng call window (public cho VideoCallWindow gọi)
     */
    public void cleanupCallWindow() {
        cleanupCallState();
    }
}
