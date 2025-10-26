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
    private Map<String, ClientHandler> clients;  // username -> handler
    private Map<String, ChatRoom> chatRooms;     // roomName -> room
    private boolean running;
    private ServerUI ui;
    
    public ChatServer(int port) {
        this.port = port;
        this.clients = new ConcurrentHashMap<>();
        this.chatRooms = new ConcurrentHashMap<>();
        this.running = false;
        
        // Tạo một số room mặc định
        chatRooms.put("General", new ChatRoom("General", "SYSTEM"));
        chatRooms.put("Gaming", new ChatRoom("Gaming", "SYSTEM"));
        chatRooms.put("Study", new ChatRoom("Study", "SYSTEM"));
    }
    
    public void setUI(ServerUI ui) {
        this.ui = ui;
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            log("Server đã khởi động trên port " + port);
            
            // Thread chấp nhận kết nối
            new Thread(() -> {
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        ClientHandler handler = new ClientHandler(clientSocket, this);
                        handler.start();
                    } catch (IOException e) {
                        if (running) {
                            log("Lỗi khi chấp nhận kết nối: " + e.getMessage());
                        }
                    }
                }
            }).start();
            
        } catch (IOException e) {
            log("Không thể khởi động server: " + e.getMessage());
        }
    }
    
    public void stop() {
        try {
            running = false;
            
            // Ngắt kết nối tất cả clients
            for (ClientHandler handler : clients.values()) {
                handler.cleanup();
            }
            clients.clear();
            
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            log("Server đã dừng");
        } catch (IOException e) {
            log("Lỗi khi dừng server: " + e.getMessage());
        }
    }
    
    public synchronized void addClient(ClientHandler handler) {
        clients.put(handler.getUser().getUsername(), handler);
        updateUI();
    }
    
    public synchronized void removeClient(ClientHandler handler) {
        if (handler.getUser() != null) {
            String username = handler.getUser().getUsername();
            
            // Rời khỏi tất cả rooms
            for (ChatRoom room : chatRooms.values()) {
                if (room.isMember(username)) {
                    room.removeMember(username);
                }
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
        for (ClientHandler handler : clients.values()) {
            users.add(handler.getUser());
        }
        return users;
    }
    
    public List<ChatRoom> getRoomList() {
        return new ArrayList<>(chatRooms.values());
    }
    
    public void broadcastUserList() {
        Message msg = new Message(Message.MessageType.USER_LIST, "SERVER", "");
        msg.setData(getOnlineUsers());
        
        for (ClientHandler handler : clients.values()) {
            handler.sendMessage(msg);
        }
    }
    
    public void sendPrivateMessage(Message msg) {
        ClientHandler receiver = clients.get(msg.getReceiver());
        ClientHandler sender = clients.get(msg.getSender());
        
        if (receiver != null) {
            receiver.sendMessage(msg);
            // Echo lại cho người gửi
            if (sender != null) {
                sender.sendMessage(msg);
            }
            log("Private: " + msg.getSender() + " -> " + msg.getReceiver() + ": " + msg.getContent());
        } else {
            if (sender != null) {
                Message error = new Message(Message.MessageType.ERROR, "SERVER", 
                    "Người dùng " + msg.getReceiver() + " không online");
                sender.sendMessage(error);
            }
        }
    }
    
    public void sendRoomMessage(Message msg) {
        String roomName = msg.getReceiver();
        ChatRoom room = chatRooms.get(roomName);
        
        if (room != null) {
            for (String member : room.getMembers()) {
                ClientHandler handler = clients.get(member);
                if (handler != null) {
                    handler.sendMessage(msg);
                }
            }
            log("Room [" + roomName + "]: " + msg.getSender() + ": " + msg.getContent());
        }
    }
    
    public void broadcastMessage(Message msg) {
        for (ClientHandler handler : clients.values()) {
            handler.sendMessage(msg);
        }
        log("Broadcast: " + msg.getSender() + ": " + msg.getContent());
    }
    
    public void createRoom(String roomName, String creator) {
        String normalized = roomName == null ? "" : roomName.trim();
        if (normalized.isEmpty()) {
            ClientHandler creatorHandler = clients.get(creator);
            if (creatorHandler != null) {
                creatorHandler.sendMessage(new Message(Message.MessageType.ERROR, "SERVER",
                        "Tên phòng không hợp lệ"));
            }
            return;
        }

        if (!chatRooms.containsKey(normalized)) {
            ChatRoom room = new ChatRoom(normalized, creator);
            chatRooms.put(normalized, room);

            broadcastRoomList("Room mới: " + normalized);

            log("Room mới được tạo: " + normalized + " bởi " + creator);
            updateUI();
        } else {
            ClientHandler creatorHandler = clients.get(creator);
            if (creatorHandler != null) {
                creatorHandler.sendMessage(new Message(Message.MessageType.ERROR, "SERVER",
                        "Room đã tồn tại"));
            }
        }
    }
    
    public void joinRoom(String roomName, String username) {
        ChatRoom room = chatRooms.get(roomName);
        ClientHandler handler = clients.get(username);
        
        if (room != null && handler != null) {
            if (room.addMember(username)) {
                handler.getUser().setCurrentRoom(roomName);
                
                Message success = new Message(Message.MessageType.SUCCESS, "SERVER", 
                    "Đã tham gia room: " + roomName);
                handler.sendMessage(success);
                
                // Thông báo cho các thành viên khác
                Message notification = new Message(Message.MessageType.ROOM_MSG, "SERVER", 
                    username + " đã tham gia room");
                notification.setReceiver(roomName);
                sendRoomMessage(notification);
                
                log(username + " đã tham gia room: " + roomName);
                broadcastRoomList(null);
                updateUI();
            } else {
                Message error = new Message(Message.MessageType.ERROR, "SERVER",
                        "Room đã đầy hoặc không thể tham gia");
                handler.sendMessage(error);
            }
        }
    }
    
    public void leaveRoom(String roomName, String username) {
        ChatRoom room = chatRooms.get(roomName);
        ClientHandler handler = clients.get(username);
        
        if (room != null && handler != null) {
            room.removeMember(username);
            handler.getUser().setCurrentRoom(null);
            
            Message success = new Message(Message.MessageType.SUCCESS, "SERVER", 
                "Đã rời room: " + roomName);
            handler.sendMessage(success);
            
            // Thông báo cho các thành viên khác
            Message notification = new Message(Message.MessageType.ROOM_MSG, "SERVER", 
                username + " đã rời room");
            notification.setReceiver(roomName);
            sendRoomMessage(notification);
            
            log(username + " đã rời room: " + roomName);
            broadcastRoomList(null);
            updateUI();
        }
    }
    
    public void log(String message) {
        String logMsg = "[" + new Date() + "] " + message;
        System.out.println(logMsg);
        if (ui != null) {
            ui.appendLog(logMsg);
        }
    }
    
    private void updateUI() {
        if (ui != null) {
            ui.updateStatistics(clients.size(), chatRooms.size());
            ui.updateClientList(getOnlineUsers());
            ui.updateRoomList(getRoomList());
        }
    }

    private void broadcastRoomList(String info) {
        Message msg = new Message(Message.MessageType.ROOM_LIST, "SERVER",
                info == null ? "" : info);
        msg.setData(getRoomList());
        for (ClientHandler handler : clients.values()) {
            handler.sendMessage(msg);
        }
    }

    public synchronized boolean kickClient(String username) {
        ClientHandler handler = clients.get(username);
        if (handler != null) {
            handler.sendMessage(new Message(Message.MessageType.ERROR, "SERVER",
                    "Bạn đã bị ngắt kết nối bởi quản trị viên"));
            handler.cleanup();
            log("Đã kick: " + username);
            return true;
        }
        return false;
    }

    public synchronized boolean deleteRoom(String roomName) {
        ChatRoom room = chatRooms.get(roomName);
        if (room == null) return false;
        if (!room.getMembers().isEmpty()) {
            return false; // chỉ cho phép xóa khi phòng trống
        }
        chatRooms.remove(roomName);
        broadcastRoomList("Đã xóa room: " + roomName);
        log("Đã xóa room: " + roomName);
        updateUI();
        return true;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public int getPort() {
        return port;
    }
}
