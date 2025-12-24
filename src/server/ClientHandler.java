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
            // Tăng buffer size lên 1MB (như Zoom/Teams) để xử lý video call thực tế
            java.io.BufferedOutputStream bufferedOut = new java.io.BufferedOutputStream(socket.getOutputStream(), 1048576);
            output = new ObjectOutputStream(bufferedOut);
            output.flush();
            input = new ObjectInputStream(new java.io.BufferedInputStream(socket.getInputStream(), 1048576));
            
            // Đọc tin nhắn đầu tiên (Login hoặc Register)
            Message firstMsg = (Message) input.readObject();
            
            // --- XỬ LÝ ĐĂNG KÝ ---
            if (firstMsg.getType() == Message.MessageType.REGISTER) {
                String u = firstMsg.getSender();
                String p = firstMsg.getPassword();
                boolean success = server.getDbManager().registerUser(u, p);
                
                if (success) {
                    sendMessage(new Message(Message.MessageType.SUCCESS, "SERVER", "Đăng ký thành công!"));
                } else {
                    sendMessage(new Message(Message.MessageType.ERROR, "SERVER", "Username đã tồn tại!"));
                }
                // Sau khi đăng ký xong thì đóng kết nối, user phải login lại
                socket.close();
                return;
            }
            
            // --- XỬ LÝ ĐĂNG NHẬP ---
            if (firstMsg.getType() == Message.MessageType.LOGIN) {
                String u = firstMsg.getSender();
                String p = firstMsg.getPassword();
                
                // 1. Check DB
                if (!server.getDbManager().checkLogin(u, p)) {
                    sendMessage(new Message(Message.MessageType.ERROR, "SERVER", "Sai tài khoản hoặc mật khẩu!"));
                    socket.close();
                    return;
                }
                
                // 2. Check Online
                if (server.isUsernameTaken(u)) {
                    sendMessage(new Message(Message.MessageType.ERROR, "SERVER", "Tài khoản đang đăng nhập nơi khác!"));
                    socket.close();
                    return;
                }
                
                // 3. Login thành công
                user = new User(u, socket.getInetAddress().getHostAddress(), socket.getPort());
                server.addClient(this);
                
                sendMessage(new Message(Message.MessageType.SUCCESS, "SERVER", "Đăng nhập thành công!"));
                
                // Gửi data ban đầu
                Message userList = new Message(Message.MessageType.USER_LIST, "SERVER", "");
                userList.setData(server.getOnlineUsers());
                sendMessage(userList);
                
                Message roomList = new Message(Message.MessageType.ROOM_LIST, "SERVER", "");
                roomList.setData(server.getRoomList());
                sendMessage(roomList);
                
                server.broadcastUserList();
                server.log(u + " logged in.");
                
                // Vòng lặp nhận tin nhắn
                while (running) {
                    Message msg = (Message) input.readObject();
                    handleMessage(msg);
                }
            }
            
        } catch (Exception e) {
            if (running) server.log("Client error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }
    
    private void handleMessage(Message msg) {
        switch (msg.getType()) {
            case PRIVATE_MSG:
                // Lưu tin nhắn TEXT vào DB
                server.getDbManager().saveMessage(
                    msg.getSender(), msg.getReceiver(), msg.getContent(), msg.getTimestamp(), "TEXT"
                );
                server.sendPrivateMessage(msg);
                break;

            case FILE_TRANSFER:
                routeMedia(msg, "FILE");
                break;
            
            case GET_HISTORY:
                // Lấy lịch sử và gửi lại cho Client
                String partner = msg.getReceiver();
                java.util.List<Message> history = server.getDbManager().getChatHistory(user.getUsername(), partner);
                for (Message m : history) {
                    sendMessage(m);
                }
                break;

            // ... (Các case khác giữ nguyên: SCREENSHOT, VIDEO, etc.) ...
            case SCREENSHOT:
                routeMedia(msg, "SCREENSHOT");
                break;
                
            case VIDEO_CALL_REQUEST:
            case VIDEO_CALL_ACCEPT:
            case VIDEO_CALL_REJECT:
            case VIDEO_CALL_END:
            case VIDEO_FRAME:
            case AUDIO_DATA:
            case MESSAGE_REACTION:
                if (msg.getReceiver() != null) server.sendPrivateMessage(msg);
                else server.broadcastMessage(msg);
                break;
                
            case ROOM_MSG: server.sendRoomMessage(msg); break;
            case BROADCAST_MSG: server.broadcastMessage(msg); break;
            case CREATE_ROOM: server.createRoom(msg.getContent(), msg.getSender()); break;
            case JOIN_ROOM: server.joinRoom(msg.getContent(), msg.getSender()); break;
            case LEAVE_ROOM: server.leaveRoom(msg.getContent(), msg.getSender()); break;
            case LOGOUT: running = false; break;
            
            // Các message type xử lý ở nơi khác (LOGIN/REGISTER trong run(), USER_LIST/ROOM_LIST/SUCCESS/ERROR là server response)
            default:
                // Ignore hoặc log nếu cần
                break;
        }
    }

    // Unified routing for media messages (file/screenshot)
    private void routeMedia(Message msg, String kind) {
        // kind is "FILE" or "SCREENSHOT" (for logging/DB type if needed)
        if (msg.getReceiver() != null) {
            if (msg.getReceiver().startsWith("#")) {
                // Room delivery
                server.sendRoomMessage(msg);
            } else {
                // Private delivery; save only files to DB history
                if ("FILE".equals(kind)) {
                    server.getDbManager().saveMessage(
                        msg.getSender(), msg.getReceiver(), msg.getFileName(), msg.getTimestamp(), "FILE"
                    );
                }
                server.sendPrivateMessage(msg);
            }
        } else {
            // Broadcast delivery
            server.broadcastMessage(msg);
        }
    }
    
    public void sendMessage(Message msg) {
        try {
            output.writeObject(msg);
            output.flush();
            output.reset(); // Xóa cache để tránh memory leak và lỗi buffer
        } catch (IOException e) { 
            if (running) e.printStackTrace(); 
        }
    }
    
    public User getUser() { return user; }
    
    public void cleanup() {
        try {
            running = false;
            if (user != null) {
                server.removeClient(this);
                server.log(user.getUsername() + " disconnected");
            }
            if (socket != null) socket.close();
        } catch (IOException e) {}
    }
}