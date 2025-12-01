package server;

import common.ChatRoom;
import common.Message;
import common.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private int port;
    private ServerSocket serverSocket;
    private Map<String, ClientHandler> clients;
    private Map<String, ChatRoom> chatRooms;
    private boolean running;
    private ServerUI ui;
    
    // --- MỚI: Database Manager ---
    private DatabaseManager dbManager;
    
    public ChatServer(int port) {
        this.port = port;
        this.clients = new ConcurrentHashMap<>();
        this.chatRooms = new ConcurrentHashMap<>();
        this.running = false;
        
        // Khởi tạo Database
        this.dbManager = new DatabaseManager();
        
        // Tạo room mặc định
        chatRooms.put("#General", new ChatRoom("General", "SYSTEM"));
        chatRooms.put("#Gaming", new ChatRoom("Gaming", "SYSTEM"));
    }
    
    public void setUI(ServerUI ui) { this.ui = ui; }
    
    // Getter cho DB Manager
    public DatabaseManager getDbManager() { return dbManager; }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            log("Server started on port " + port);
            
            new Thread(() -> {
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        ClientHandler handler = new ClientHandler(clientSocket, this);
                        handler.start();
                    } catch (IOException e) {
                        if (running) log("Accept error: " + e.getMessage());
                    }
                }
            }).start();
            
        } catch (IOException e) {
            log("Could not start server: " + e.getMessage());
        }
    }
    
    public void stop() {
        try {
            running = false;
            for (ClientHandler handler : clients.values()) handler.cleanup();
            clients.clear();
            if (serverSocket != null) serverSocket.close();
            log("Server stopped");
        } catch (IOException e) { log("Stop error: " + e.getMessage()); }
    }
    
    public synchronized void addClient(ClientHandler handler) {
        clients.put(handler.getUser().getUsername(), handler);
        updateUI();
    }
    
    public synchronized void removeClient(ClientHandler handler) {
        if (handler.getUser() != null) {
            String username = handler.getUser().getUsername();
            for (ChatRoom room : chatRooms.values()) {
                if (room.isMember(username)) room.removeMember(username);
            }
            clients.remove(username);
            broadcastUserList();
            broadcastRoomList(null);
            updateUI();
        }
    }
    
    public boolean isUsernameTaken(String username) {
        return clients.containsKey(username);
    }
    
    public List<User> getOnlineUsers() {
        List<User> users = new ArrayList<>();
        for (ClientHandler handler : clients.values()) users.add(handler.getUser());
        return users;
    }
    
    public List<ChatRoom> getRoomList() {
        return new ArrayList<>(chatRooms.values());
    }
    
    // --- CÁC HÀM GỬI TIN ---
    
    public void broadcastUserList() {
        Message msg = new Message(Message.MessageType.USER_LIST, "SERVER", "");
        msg.setData(getOnlineUsers());
        for (ClientHandler handler : clients.values()) handler.sendMessage(msg);
    }
    
    public void sendPrivateMessage(Message msg) {
        ClientHandler receiver = clients.get(msg.getReceiver());
        ClientHandler sender = clients.get(msg.getSender());
        if (receiver != null) {
            receiver.sendMessage(msg);
            // Echo lại cho sender để đồng bộ (client không tự render nữa)
            if (sender != null) {
                sender.sendMessage(msg);
            }
        }
    }
    
    public void sendRoomMessage(Message msg) {
        ChatRoom room = chatRooms.get(msg.getReceiver());
        if (room != null) {
            for (String member : room.getMembers()) {
                ClientHandler h = clients.get(member);
                if (h != null) h.sendMessage(msg);
            }
        }
    }
    
    public void broadcastMessage(Message msg) {
        for (ClientHandler handler : clients.values()) handler.sendMessage(msg);
    }
    
    public void createRoom(String roomName, String creator) {
        String normalized = roomName == null ? "" : roomName.trim();
        if (normalized.isEmpty()) return;
        String fullRoomName = normalized.startsWith("#") ? normalized : "#" + normalized;

        if (!chatRooms.containsKey(fullRoomName)) {
            ChatRoom room = new ChatRoom(normalized, creator);
            chatRooms.put(fullRoomName, room);
            broadcastRoomList("New room: " + fullRoomName);
            updateUI();
        }
    }
    
    public void joinRoom(String roomName, String username) {
        ChatRoom room = chatRooms.get(roomName);
        ClientHandler handler = clients.get(username);
        if (room != null && handler != null) {
            room.addMember(username);
            handler.getUser().setCurrentRoom(roomName);
            handler.sendMessage(new Message(Message.MessageType.SUCCESS, "SERVER", "Joined " + roomName));
            updateUI();
        }
    }
    
    public void leaveRoom(String roomName, String username) {
        ChatRoom room = chatRooms.get(roomName);
        ClientHandler handler = clients.get(username);
        if (room != null && handler != null) {
            room.removeMember(username);
            handler.getUser().setCurrentRoom(null);
            handler.sendMessage(new Message(Message.MessageType.SUCCESS, "SERVER", "Left " + roomName));
            updateUI();
        }
    }
    
    private void broadcastRoomList(String info) {
        Message msg = new Message(Message.MessageType.ROOM_LIST, "SERVER", info == null ? "" : info);
        msg.setData(getRoomList());
        for (ClientHandler handler : clients.values()) handler.sendMessage(msg);
    }
    
    public synchronized boolean kickClient(String username) {
        ClientHandler h = clients.get(username);
        if (h != null) {
            h.sendMessage(new Message(Message.MessageType.ERROR, "SERVER", "You were kicked."));
            h.cleanup();
            return true;
        }
        return false;
    }

    public synchronized boolean deleteRoom(String roomName) {
        ChatRoom r = chatRooms.get(roomName);
        if (r != null && r.getMembers().isEmpty()) {
            chatRooms.remove(roomName);
            broadcastRoomList("Deleted room: " + roomName);
            updateUI();
            return true;
        }
        return false;
    }

    public void log(String message) {
        String logMsg = "[" + new Date() + "] " + message;
        System.out.println(logMsg);
        if (ui != null) ui.appendLog(logMsg);
    }
    
    private void updateUI() {
        if (ui != null) {
            ui.updateStatistics(clients.size(), chatRooms.size());
            ui.updateClientList(getOnlineUsers());
            ui.updateRoomList(getRoomList());
        }
    }
    
    public boolean isRunning() { return running; }
    public int getPort() { return port; }
}