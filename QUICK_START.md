# Hướng dẫn chạy nhanh

## Bước 1: Chạy Server

1. Mở file `ServerUI.java` trong package `server`
2. Run as Java Application
3. Nhập port (mặc định: 12345)
4. Click "Start Server"

## Bước 2: Chạy Client (có thể chạy nhiều instance)

1. Mở file `ClientUI.java` trong package `client`
2. Run as Java Application
3. Điền thông tin:
   - Server IP: localhost (hoặc IP máy chạy server)
   - Server Port: 12345
   - Username: tên bạn chọn
4. Click "Connect"

## Bước 3: Test các chức năng

### Test Broadcast (Chat tất cả)
- Tab "📢 Broadcast (All)"
- Gửi message → tất cả client sẽ nhận

### Test Private Chat (1-1)
- Tab "💬 Private Chat (1-1)"
- Chọn user từ danh sách
- Gửi message → chỉ user đó nhận

### Test Room Chat
- Tab "🏠 Room Chat"
- Click "Join Room" → chọn room có sẵn (General, Gaming, Study)
- Hoặc "Create Room" → tạo room mới
- Gửi message → chỉ members trong room nhận

## Lưu ý
- Phải chạy Server trước, sau đó mới chạy Client
- Có thể chạy nhiều Client cùng lúc để test chat
- Username phải unique (không trùng lặp)
