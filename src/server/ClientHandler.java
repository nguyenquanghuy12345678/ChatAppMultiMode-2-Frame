package server;

import common.Message;
import common.User;
import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;
    private ChatServer server;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private User user;
    private boolean running;
    
    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
        this.running = true;
    }
    
    @Override
    public void run() {
        try {
            // Khởi tạo streams
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
            
            // Nhận thông tin đăng nhập
            Message loginMsg = (Message) input.readObject();
            
            if (loginMsg.getType() == Message.MessageType.LOGIN) {
                String username = loginMsg.getSender();
                
                // Kiểm tra username đã tồn tại chưa
                if (server.isUsernameTaken(username)) {
                    sendMessage(new Message(Message.MessageType.ERROR, "SERVER", 
                        "Username đã tồn tại!"));
                    socket.close();
                    return;
                }
                
                // Tạo user mới
                user = new User(username, 
                    socket.getInetAddress().getHostAddress(), 
                    socket.getPort());
                
                // Thêm vào server
                server.addClient(this);
                
                // Gửi thông báo thành công
                sendMessage(new Message(Message.MessageType.SUCCESS, "SERVER", 
                    "Đăng nhập thành công!"));
                
                // Gửi danh sách user hiện tại
                Message userListMsg = new Message(Message.MessageType.USER_LIST, "SERVER", "");
                userListMsg.setData(server.getOnlineUsers());
                sendMessage(userListMsg);
                
                // Gửi danh sách rooms
                Message roomListMsg = new Message(Message.MessageType.ROOM_LIST, "SERVER", "");
                roomListMsg.setData(server.getRoomList());
                sendMessage(roomListMsg);
                
                // Thông báo cho tất cả client khác
                server.broadcastUserList();
                
                server.log(username + " đã kết nối từ " + 
                    socket.getInetAddress().getHostAddress());
                
                // Lắng nghe messages
                while (running) {
                    Message msg = (Message) input.readObject();
                    handleMessage(msg);
                }
            }
            
        } catch (IOException | ClassNotFoundException e) {
            if (running) {
                server.log("Lỗi với client " + 
                    (user != null ? user.getUsername() : "unknown") + ": " + e.getMessage());
            }
        } finally {
            cleanup();
        }
    }
    
    private void handleMessage(Message msg) {
        switch (msg.getType()) {
            case PRIVATE_MSG:
                server.sendPrivateMessage(msg);
                break;
                
            case ROOM_MSG:
                server.sendRoomMessage(msg);
                break;
                
            case BROADCAST_MSG:
                server.broadcastMessage(msg);
                break;
                
            case FILE_TRANSFER:
                handleFileTransfer(msg);
                break;
                
            case SCREENSHOT:
                handleScreenshot(msg);
                break;
                
            case MESSAGE_REACTION:
                handleReaction(msg);
                break;
                
            case VIDEO_CALL_REQUEST:
            case VIDEO_CALL_ACCEPT:
            case VIDEO_CALL_REJECT:
            case VIDEO_CALL_END:
                handleVideoCall(msg);
                break;
                
            case CREATE_ROOM:
                server.createRoom(msg.getContent(), msg.getSender());
                break;
                
            case JOIN_ROOM:
                server.joinRoom(msg.getContent(), msg.getSender());
                break;
                
            case LEAVE_ROOM:
                server.leaveRoom(msg.getContent(), msg.getSender());
                break;
                
            case LOGOUT:
                running = false;
                break;
                
            default:
                break;
        }
    }
    
    public void sendMessage(Message msg) {
        try {
            output.writeObject(msg);
            output.flush();
        } catch (IOException e) {
            String uname = (user != null ? user.getUsername() : "unknown");
            server.log("Không thể gửi message đến " + uname);
        }
    }
    
    private void handleFileTransfer(Message msg) {
        String receiver = msg.getReceiver();
        
        if (receiver == null) {
            // Broadcast file to all
            server.broadcastMessage(msg);
            server.log(user.getUsername() + " sent file to all: " + msg.getFileName());
        } else if (receiver.startsWith("#")) {
            // Room file transfer
            server.sendRoomMessage(msg);
            server.log(user.getUsername() + " sent file to room " + receiver + ": " + msg.getFileName());
        } else {
            // Private file transfer
            server.sendPrivateMessage(msg);
            server.log(user.getUsername() + " sent file to " + receiver + ": " + msg.getFileName());
        }
    }
    
    private void handleScreenshot(Message msg) {
        String receiver = msg.getReceiver();
        
        if (receiver == null) {
            // Broadcast screenshot to all
            server.broadcastMessage(msg);
            server.log(user.getUsername() + " sent screenshot to all");
        } else if (receiver.startsWith("#")) {
            // Room screenshot
            server.sendRoomMessage(msg);
            server.log(user.getUsername() + " sent screenshot to room " + receiver);
        } else {
            // Private screenshot
            server.sendPrivateMessage(msg);
            server.log(user.getUsername() + " sent screenshot to " + receiver);
        }
    }
    
    private void handleReaction(Message msg) {
        String receiver = msg.getReceiver();
        
        if (receiver == null) {
            // Broadcast reaction
            server.broadcastMessage(msg);
        } else if (receiver.startsWith("#")) {
            // Room reaction
            server.sendRoomMessage(msg);
        } else {
            // Private reaction
            server.sendPrivateMessage(msg);
        }
        
        server.log(user.getUsername() + " reacted " + msg.getReactionType() + 
            " to message " + msg.getMessageId());
    }
    
    private void handleVideoCall(Message msg) {
        // Forward video call messages to the receiver
        String receiver = msg.getReceiver();
        if (receiver != null) {
            server.sendPrivateMessage(msg);
            server.log("Video call message: " + msg.getType() + 
                " from " + user.getUsername() + " to " + receiver);
        }
    }
    
    public User getUser() {
        return user;
    }
    
    public void cleanup() {
        try {
            running = false;
            if (user != null) {
                server.removeClient(this);
                server.log(user.getUsername() + " đã ngắt kết nối");
            }
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
