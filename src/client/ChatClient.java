package client;

import common.ChatRoom;
import common.Message;
import common.User;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.List;
import javax.swing.JOptionPane;

public class ChatClient {
    private String serverIP;
    private int serverPort;
    private String username;
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private ClientUI ui;
    private boolean connected;
    
    // Call state
    private VideoCallWindow activeCallWindow = null;
    private String currentCallId = null;
    
    public ChatClient() { this.connected = false; }
    public void setUI(ClientUI ui) { this.ui = ui; }
    
    public String getUsername() { return username; }
    
    // --- 1. CONNECT VỚI PASSWORD ---
    public boolean connect(String serverIP, int serverPort, String username, String password) {
        try {
            this.serverIP = serverIP;
            this.serverPort = serverPort;
            this.username = username;
            
            socket = new Socket(serverIP, serverPort);
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
            
            Message loginMsg = new Message(Message.MessageType.LOGIN, username, "");
            loginMsg.setPassword(password);
            sendMessage(loginMsg);
            
            Message response = (Message) input.readObject();
            if (response.getType() == Message.MessageType.SUCCESS) {
                connected = true;
                new Thread(this::listenForMessages).start();
                return true;
            } else {
                ui.showError(response.getContent());
                disconnect();
                return false;
            }
        } catch (Exception e) {
            ui.showError("Connection Error: " + e.getMessage());
            return false;
        }
    }
    
    // --- 2. HÀM ĐĂNG KÝ (Nút Register gọi cái này) ---
    public boolean register(String serverIP, int serverPort, String username, String password) {
        try {
            Socket tempSocket = new Socket(serverIP, serverPort);
            ObjectOutputStream tempOut = new ObjectOutputStream(tempSocket.getOutputStream());
            ObjectInputStream tempIn = new ObjectInputStream(tempSocket.getInputStream());
            
            Message regMsg = new Message(Message.MessageType.REGISTER, username, "");
            regMsg.setPassword(password);
            tempOut.writeObject(regMsg);
            tempOut.flush();
            
            Message response = (Message) tempIn.readObject();
            boolean success = (response.getType() == Message.MessageType.SUCCESS);
            
            if (success) ui.showInfo(response.getContent());
            else ui.showError(response.getContent());
            
            tempOut.close();
            tempIn.close();
            tempSocket.close();
            return success;
        } catch (Exception e) {
            ui.showError("Register Error: " + e.getMessage());
            return false;
        }
    }

    private void listenForMessages() {
        try {
            while (connected) {
                Message msg = (Message) input.readObject();
                handleMessage(msg);
            }
        } catch (Exception e) {
            if (connected) disconnect();
        }
    }
    
    @SuppressWarnings("unchecked")
    private void handleMessage(Message msg) {
        switch (msg.getType()) {
            case USER_LIST: ui.updateUserList((List<User>) msg.getData()); break;
            case ROOM_LIST: ui.updateRoomList((List<ChatRoom>) msg.getData()); break;
            case PRIVATE_MSG: ui.displayPrivateMessage(msg); break;
            case ROOM_MSG: ui.displayRoomMessage(msg); break;
            case BROADCAST_MSG: ui.displayBroadcastMessage(msg); break;
            case SUCCESS: ui.showInfo(msg.getContent()); break;
            case ERROR: ui.showError(msg.getContent()); break;
            
            // Xử lý File/Ảnh nhận được
            case FILE_TRANSFER: 
            case SCREENSHOT:
                // Xác định tab hiển thị
                String tab = "broadcast";
                if(msg.getReceiver() != null) {
                    if(msg.getReceiver().startsWith("#")) tab = "room";
                    else tab = "private";
                }
                boolean isImg = ImagePreviewDialog.isImageFile(msg.getFileName());
                ui.displayFileInChat(msg.getSender(), msg.getFileName(), msg.getFileData(), isImg, tab);
                break;
                
            case VIDEO_CALL_REQUEST: handleVideoCallRequest(msg); break;
            case VIDEO_CALL_ACCEPT: handleVideoCallAccept(msg); break;
            case VIDEO_CALL_REJECT: ui.showInfo(msg.getSender() + " busy/rejected."); break;
            case VIDEO_CALL_END: handleVideoCallEnd(msg); break;
            case VIDEO_FRAME: 
                if (activeCallWindow != null) activeCallWindow.displayRemoteFrame(msg.getFileData());
                break;
            case AUDIO_DATA:
                if (activeCallWindow != null) activeCallWindow.playAudio(msg.getFileData());
                break;
            case MESSAGE_REACTION: ui.displayReaction(msg); break;
            default: break;
        }
    }
    
    public void sendMessage(Message msg) {
        try { output.writeObject(msg); output.flush(); } catch (IOException e) { e.printStackTrace(); }
    }
    
    // --- 3. CÁC HÀM GỬI TIN & FILE (Nút Gửi gọi cái này) ---
    public void sendPrivateMessage(String r, String c) { 
        Message m = new Message(Message.MessageType.PRIVATE_MSG, username, c); m.setReceiver(r); sendMessage(m); 
    }
    public void sendRoomMessage(String r, String c) {
        Message m = new Message(Message.MessageType.ROOM_MSG, username, c); m.setReceiver(r); sendMessage(m);
    }
    public void sendBroadcastMessage(String c) {
        sendMessage(new Message(Message.MessageType.BROADCAST_MSG, username, c));
    }
    
    public void sendFile(File f, String r, String mode) {
        try {
            byte[] d = Files.readAllBytes(f.toPath());
            Message msg = new Message(Message.MessageType.FILE_TRANSFER, username, f.getName());
            msg.setFileName(f.getName()); msg.setFileData(d); msg.setFileSize(f.length());
            
            if (mode.equals("private")) msg.setReceiver(r);
            else if (mode.equals("room")) msg.setReceiver(r);
            
            sendMessage(msg);
            // Server sẽ echo lại cho người nhận và tất cả thành viên, không cần hiển thị ngay để tránh double
            
        } catch(Exception e) { ui.showError(e.getMessage()); }
    }
    
    public void sendScreenshot(String r, String mode) {
        try {
            // Ẩn cửa sổ để chụp
            ui.setVisible(false);
            Thread.sleep(300);
            byte[] d = ScreenCaptureUtil.captureAndResize(1024, 768);
            ui.setVisible(true);
            
            String name = "screen_" + System.currentTimeMillis() + ".png";
            Message msg = new Message(Message.MessageType.SCREENSHOT, username, "Screenshot");
            msg.setFileName(name); msg.setFileData(d); msg.setFileSize(d.length);
            
            if (mode.equals("private")) msg.setReceiver(r);
            else if (mode.equals("room")) msg.setReceiver(r);
            
            sendMessage(msg);
            // Server sẽ echo lại cho người nhận và tất cả thành viên, không cần hiển thị ngay để tránh double
            
        } catch(Exception e) { 
            ui.setVisible(true); 
            ui.showError(e.getMessage()); 
        }
    }
    
    // --- 4. HÀM LẤY LỊCH SỬ CHAT (Khi chọn User) ---
    public void requestChatHistory(String partner) {
        Message msg = new Message(Message.MessageType.GET_HISTORY, username, "");
        msg.setReceiver(partner);
        sendMessage(msg);
    }

    // --- 5. LOGIC GỌI ĐIỆN ---
    public void sendVideoCallRequest(String receiver, boolean videoEnabled, boolean audioEnabled) {
        if (activeCallWindow != null) { ui.showError("Already in a call!"); return; }
        currentCallId = java.util.UUID.randomUUID().toString();
        Message msg = new Message(Message.MessageType.VIDEO_CALL_REQUEST, username, "Call");
        msg.setReceiver(receiver); msg.setCallId(currentCallId);
        msg.setVideoEnabled(videoEnabled); msg.setAudioEnabled(audioEnabled);
        sendMessage(msg);
    }
    
    public void sendAudioData(String receiver, String callId, byte[] data) {
        Message msg = new Message(Message.MessageType.AUDIO_DATA, username, "audio");
        msg.setReceiver(receiver); msg.setCallId(callId); msg.setFileData(data);
        sendMessage(msg);
    }
    
    public void sendVideoFrame(String receiver, String callId, byte[] data) {
        Message msg = new Message(Message.MessageType.VIDEO_FRAME, username, "frame");
        msg.setReceiver(receiver); msg.setCallId(callId); msg.setFileData(data);
        sendMessage(msg);
    }
    
    // Xử lý nhận cuộc gọi
    private void handleVideoCallRequest(Message msg) {
        if (activeCallWindow != null) {
            Message rej = new Message(Message.MessageType.VIDEO_CALL_REJECT, username, "Busy");
            rej.setReceiver(msg.getSender()); rej.setCallId(msg.getCallId()); sendMessage(rej);
            return;
        }
        int choice = JOptionPane.showConfirmDialog(null, msg.getSender() + " calling...", "Call", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            currentCallId = msg.getCallId();
            Message acc = new Message(Message.MessageType.VIDEO_CALL_ACCEPT, username, "Accepted");
            acc.setReceiver(msg.getSender()); acc.setCallId(msg.getCallId()); sendMessage(acc);
            activeCallWindow = new VideoCallWindow(msg.getSender(), currentCallId, msg.isVideoEnabled(), this);
            activeCallWindow.setVisible(true);
        } else {
            Message rej = new Message(Message.MessageType.VIDEO_CALL_REJECT, username, "Rejected");
            rej.setReceiver(msg.getSender()); rej.setCallId(msg.getCallId()); sendMessage(rej);
        }
    }
    
    private void handleVideoCallAccept(Message msg) {
        currentCallId = msg.getCallId();
        if (activeCallWindow == null) {
            activeCallWindow = new VideoCallWindow(msg.getSender(), currentCallId, true, this);
            activeCallWindow.setVisible(true);
        }
    }
    
    private void handleVideoCallEnd(Message msg) {
        if (activeCallWindow != null) { activeCallWindow.forceClose(); activeCallWindow = null; }
        currentCallId = null; ui.showInfo("Call ended");
    }
    
    public void endVideoCall(String receiver, String callId) {
        Message msg = new Message(Message.MessageType.VIDEO_CALL_END, username, "Ended");
        msg.setReceiver(receiver); msg.setCallId(callId); sendMessage(msg);
    }
    
    public void cleanupCallWindow() { activeCallWindow = null; currentCallId = null; }
    
    public void createRoom(String r) { sendMessage(new Message(Message.MessageType.CREATE_ROOM, username, r)); }
    public void joinRoom(String r) { Message m = new Message(Message.MessageType.JOIN_ROOM, username, r); m.setContent(r); sendMessage(m); }
    public void leaveRoom(String r) { Message m = new Message(Message.MessageType.LEAVE_ROOM, username, r); m.setContent(r); sendMessage(m); }
    
    public void disconnect() {
        try { connected = false; if (socket != null) socket.close(); } catch (Exception e) {}
    }
}