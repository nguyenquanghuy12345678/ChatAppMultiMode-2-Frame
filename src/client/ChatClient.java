package client;

import common.ChatRoom;
import common.Message;
import common.User;

import java.io.*;
import java.net.Socket;
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
            while (connected) {
                Message msg = (Message) input.readObject();
                handleMessage(msg);
            }
        } catch (IOException | ClassNotFoundException e) {
            if (connected) {
                ui.showError("Mất kết nối với server!");
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
    
    private void sendMessage(Message msg) {
        try {
            output.writeObject(msg);
            output.flush();
        } catch (IOException e) {
            ui.showError("Không thể gửi message: " + e.getMessage());
        }
    }
    
    public void disconnect() {
        try {
            connected = false;
            
            if (output != null) {
                Message logoutMsg = new Message(Message.MessageType.LOGOUT, username, "");
                output.writeObject(logoutMsg);
                output.flush();
                output.close();
            }
            
            if (input != null) input.close();
            if (socket != null) socket.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public String getUsername() {
        return username;
    }
}
