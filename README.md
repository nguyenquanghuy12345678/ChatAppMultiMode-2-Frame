# Chat Application Multi-Mode

Ứng dụng chat Java với 3 chế độ chat khác nhau: **Chat 1-1 (Private)**, **Chat Room**, và **Broadcast (Tất cả)**.
![Server Management Mode](img/Server%20Management%20Mode.png)
![Client Access Chat](img/Client%20Access%20Chat.png)
## 🌟 Tính năng

### Server
- ✅ Quản lý tất cả client kết nối theo thời gian thực
- ✅ Theo dõi thông tin: Username, IP Address, Port
- ✅ Quản lý các Chat Rooms
- ✅ Ghi log tất cả hoạt động
- ✅ Giao diện thân thiện, cập nhật real-time
- ✅ Hiển thị số lượng clients và rooms
- ✅ Danh sách chi tiết tất cả clients và rooms

### Client
- ✅ **3 Chế độ Chat:**
  - 📢 **Broadcast**: Gửi tin nhắn đến TẤT CẢ người dùng online
  - 💬 **Private Chat (1-1)**: Chat riêng với 1 người cụ thể
  - 🏠 **Room Chat**: Chat trong room với nhiều người
  
- ✅ Kết nối qua TCP/IP với Server IP và Port
- ✅ Xem danh sách tất cả users online
- ✅ Xem danh sách tất cả chat rooms
- ✅ Tạo room mới
- ✅ Tham gia/Rời khỏi room
- ✅ Giao diện thân thiện với tabs

## 📁 Cấu trúc Project

```
ChatAppMultiMode/
├── src/
│   ├── common/              # Classes dùng chung
│   │   ├── Message.java     # Định nghĩa message
│   │   ├── User.java        # Thông tin user
│   │   └── ChatRoom.java    # Thông tin room
│   ├── server/              # Server side
│   │   ├── ChatServer.java      # Logic server
│   │   ├── ClientHandler.java   # Xử lý từng client
│   │   └── ServerUI.java        # Giao diện server
│   └── client/              # Client side
│       ├── ChatClient.java  # Logic client
│       └── ClientUI.java    # Giao diện client
└── README.md
```

## 🚀 Hướng dẫn sử dụng

### 1. Biên dịch & chạy (Windows PowerShell)

Yêu cầu: đã cài JDK 17+ (khuyến nghị JDK 21). Các lệnh dưới đây chạy trong thư mục dự án.

```powershell
# 1) Biên dịch toàn bộ dự án vào thư mục out
if (Test-Path out) { Remove-Item -Recurse -Force out }
New-Item -ItemType Directory -Path out | Out-Null
javac -d out src\module-info.java src\common\*.java src\server\*.java src\client\*.java

# 2) Chạy Server UI
java --module-path out -m ChatAppMultiMode/server.ServerUI

# 3) (Mở một cửa sổ PowerShell khác) Chạy Client UI
java --module-path out -m ChatAppMultiMode/client.ClientUI
```

**Các bước:**
1. Nhập **Port** (mặc định: 12345)
2. Click **Start Server**
3. Server sẽ hiển thị:
   - Trạng thái: Running
   - Số lượng Clients
   - Số lượng Rooms
   - Danh sách Clients online
   - Danh sách Rooms
   - Logs chi tiết

### 2. Khởi động Client

> Lưu ý: Có thể chạy nhiều client cùng lúc bằng cách mở thêm cửa sổ PowerShell và chạy lại lệnh Client UI.

**Các bước đăng nhập:**
1. Nhập **Server IP** (localhost hoặc IP của máy chạy server)
2. Nhập **Server Port** (12345 hoặc port đã cấu hình)
3. Nhập **Username** (tên hiển thị của bạn)
4. Click **Connect**

### 3. Sử dụng các chế độ Chat

#### 📢 Broadcast (Chat tất cả)
- Tab: **"📢 Broadcast (All)"**
- Gõ tin nhắn và click **"Send to All"**
- Tin nhắn sẽ được gửi đến TẤT CẢ users online

#### 💬 Private Chat (1-1)
- Tab: **"💬 Private Chat (1-1)"**
- Chọn user trong danh sách **"Online Users"**
- Gõ tin nhắn và click **"Send"**
- Chỉ user được chọn nhận được tin nhắn

#### 🏠 Room Chat
- Tab: **"🏠 Room Chat"**
- **Tạo room mới**: Click **"Create Room"** → Nhập tên room
- **Tham gia room**: Chọn room → Click **"Join Room"**
- **Chat trong room**: Gõ tin nhắn → Click **"Send"**
- **Rời room**: Click **"Leave Room"**
- Tin nhắn chỉ được gửi đến các thành viên trong room

## 🔧 Cấu hình

### Port mặc định
- Server Port: **12345**
- Có thể thay đổi trong ServerUI

### Rooms mặc định
Server tự động tạo 3 rooms:
- **General** - Room chung
- **Gaming** - Room game thủ
- **Study** - Room học tập

## 💡 Lưu ý kỹ thuật

### Giao thức
- **TCP/IP Socket** cho kết nối ổn định
- **Object Serialization** để truyền dữ liệu
- **Multi-threading** xử lý nhiều client đồng thời

### Kiến trúc
- **Server-Client Architecture**
- **Event-driven UI** với Swing
- **Real-time updates** cho tất cả clients

### Xử lý lỗi
- Kiểm tra username trùng lặp
- Tự động ngắt kết nối khi client offline
- Thông báo lỗi rõ ràng cho user

## 🎯 Tính năng nổi bật

1. **Quản lý real-time**: Server luôn cập nhật danh sách clients và rooms
2. **3 chế độ chat**: Linh hoạt cho mọi nhu cầu giao tiếp
3. **UI thân thiện**: Giao diện đẹp, dễ sử dụng
4. **Thông tin chi tiết**: Hiển thị IP, Port, Timestamp
5. **Rooms động**: Tạo và quản lý rooms tùy ý
6. **Log đầy đủ**: Server ghi lại mọi hoạt động

## 🐛 Xử lý sự cố

### Client không kết nối được
- Kiểm tra Server đã chạy chưa
- Kiểm tra IP và Port có đúng không
- Kiểm tra firewall

### Username đã tồn tại
- Chọn username khác
- Đảm bảo client cũ đã disconnect

### Không gửi được tin nhắn
- Kiểm tra kết nối mạng
- Đảm bảo đã chọn đúng người nhận (Private) hoặc đã join room (Room Chat)

## 📝 Ví dụ kịch bản sử dụng

### Kịch bản 1: Chat nhóm học tập
1. User A tạo room "Học Java"
2. User B, C join room "Học Java"
3. Tất cả chat trong room để thảo luận

### Kịch bản 2: Thông báo chung
1. Admin gửi broadcast: "Server sẽ bảo trì 10 phút"
2. Tất cả users nhận được thông báo

### Kịch bản 3: Chat riêng
1. User A chọn User B
2. Chat riêng về công việc cá nhân

## 🎨 Giao diện

### Server UI
- Control Panel: Start/Stop server
- Statistics: Số clients, số rooms
- Client List: Danh sách chi tiết
- Room List: Danh sách rooms
- Logs: Theo dõi hoạt động

### Client UI
- Login Screen: Đăng nhập
- 3 Tabs: Broadcast, Private, Room
- User List: Xem users online
- Room List: Xem và quản lý rooms

## 📞 Thông tin kỹ thuật

- **Ngôn ngữ**: Java
- **Framework UI**: Swing
- **Network**: TCP/IP Sockets
- **Serialization**: Java ObjectStreams
- **Threading**: Java Threads
- **Collections**: ConcurrentHashMap (thread-safe)

---

**Chúc bạn có trải nghiệm chat vui vẻ! 🎉**
