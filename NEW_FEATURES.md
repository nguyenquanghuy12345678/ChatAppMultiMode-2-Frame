# 🎉 Tính Năng Mới - ChatAppMultiMode

## 📁 Gửi File / Ảnh

### Mô tả
Cho phép gửi file nhỏ (tối đa 5MB) qua socket cho:
- ✅ **Broadcast** - Gửi cho tất cả mọi người
- ✅ **Private Chat** - Gửi riêng cho 1 người
- ✅ **Room Chat** - Gửi trong phòng chat

### Cách sử dụng
1. Nhấn nút **📁** bên cạnh ô nhập tin nhắn
2. Chọn file muốn gửi (tối đa 5MB)
3. File sẽ được gửi qua socket
4. Người nhận sẽ thấy thông báo và có thể lưu file

### Kỹ thuật
```java
// Client gửi file
public void sendFile(File file, String receiver, String mode) {
    byte[] fileData = Files.readAllBytes(file.toPath());
    Message msg = new Message(MessageType.FILE_TRANSFER, username, ...);
    msg.setFileName(file.getName());
    msg.setFileData(fileData);
    msg.setFileSize(file.length());
    sendMessage(msg);
}

// Server xử lý
case FILE_TRANSFER:
    handleFileTransfer(msg);
    // Forward to receiver(s)
    break;

// Client nhận file
private void handleFileReceived(Message msg) {
    // Show dialog to save file
    Files.write(saveFile.toPath(), msg.getFileData());
}
```

### Luồng dữ liệu
```
Client A → FileInputStream → byte[] → Message Object
         ↓
    ObjectOutputStream → Socket → Server
         ↓
    Server → Forward Message → Target Client(s)
         ↓
    Client B → ObjectInputStream → byte[] → FileOutputStream
```

---

## 😊 Emoji / Biểu Tượng Cảm Xúc

### Mô tả
Thêm emoji vào tin nhắn một cách dễ dàng với emoji picker

### Emoji có sẵn
😄 😂 ❤️ 👍 🔥 ⭐ ☀️ 🌙 ☁️ ✅ ❌ ⚠️ ℹ️ 📁 🖼️ 📤

### Cách sử dụng
1. Nhấn nút **😊** bên cạnh ô nhập tin nhắn
2. Chọn emoji từ popup dialog
3. Emoji sẽ tự động thêm vào tin nhắn
4. Gửi tin nhắn như bình thường

### Icon Manager
```java
// Sử dụng IconManager
import common.IconManager;

// Load icon từ resources/icons
ImageIcon icon = IconManager.loadIcon("smile.png", 32);

// Tạo button với icon
JButton btn = IconManager.createIconButton("heart.png", "Like", 24);

// Lấy emoji Unicode
String emoji = IconManager.getEmoji("smile"); // → "😄"
```

### Thêm icons từ resources
Bạn có thể thêm file icon PNG vào:
```
resources/
  └── icons/
      ├── smile.png
      ├── heart.png
      ├── sun.png
      └── ...
```

IconManager sẽ tự động load từ `resources/icons/`

---

## 🔧 Cập Nhật Code

### Files đã thay đổi

1. **common/IconManager.java** (MỚI)
   - Load icons từ resources/icons
   - Hỗ trợ Unicode emoji
   - Utility methods cho UI

2. **common/Message.java**
   - Thêm `MessageType.FILE_TRANSFER`
   - Thêm fields: `fileName`, `fileData`, `fileSize`
   - Getters/Setters cho file transfer

3. **client/ClientUI.java**
   - Thêm nút emoji (😊) cho cả 3 tabs
   - Thêm nút file (📁) cho cả 3 tabs
   - Method `showEmojiPicker()` - Hiển thị dialog chọn emoji
   - Method `sendFile()` - Chọn và gửi file

4. **client/ChatClient.java**
   - Method `sendFile()` - Đọc file và gửi qua socket
   - Method `handleFileReceived()` - Nhận và lưu file
   - Method `formatFileSize()` - Format kích thước file

5. **server/ClientHandler.java**
   - Method `handleFileTransfer()` - Xử lý và forward file

---

## ⚙️ Cấu Hình

### Giới hạn file size
Mặc định: **5MB**

Thay đổi trong `ClientUI.java`:
```java
// Check file size (limit to 5MB)
if (selectedFile.length() > 5 * 1024 * 1024) {
    showError("File size exceeds 5MB limit!");
    return;
}
```

### Thêm emoji mới
Trong `ClientUI.showEmojiPicker()`:
```java
String[] emojis = {
    "😄", "😂", "❤️", "👍",
    // Thêm emoji mới vào đây
    "🎉", "🎊", "🎈", "🎁"
};
```

---

## 📝 Ghi Chú

### File Transfer
- ✅ Hỗ trợ mọi loại file
- ✅ Giới hạn 5MB để tránh quá tải socket
- ✅ Hiển thị progress (tên file + kích thước)
- ✅ Cho phép người nhận chọn nơi lưu file
- ⚠️ Không nén file (có thể thêm sau)
- ⚠️ Không resume nếu lỗi (có thể thêm sau)

### Emoji
- ✅ Sử dụng Unicode emoji (không cần icon files)
- ✅ 16 emoji phổ biến
- ✅ Có thể thêm/bớt dễ dàng
- ✅ IconManager hỗ trợ load icon PNG (optional)
- 💡 Có thể tải icon từ Flaticon, Icons8, etc.

---

## 🚀 Hướng Phát Triển

### Tính năng có thể thêm:
1. **File Transfer nâng cao**
   - Progress bar khi gửi/nhận file
   - Nén file trước khi gửi
   - Resume download nếu bị ngắt
   - Gửi nhiều file cùng lúc
   - Preview ảnh trong chat

2. **Emoji nâng cao**
   - Search emoji
   - Recent/Frequently used
   - Custom emoji upload
   - Emoji reactions

3. **Icons từ resources**
   - Tải bộ icon đẹp vào resources/icons
   - Animated icons
   - Theme-based icons
