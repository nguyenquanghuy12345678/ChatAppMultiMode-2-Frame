# 🎥 HƯỚNG DẪN SỬ DỤNG VIDEO CALL

## ✅ HOÀN THÀNH

### 1. Thư viện đã tải về (thư mục `lib/`):
- ✅ webcam-capture-0.3.12.jar
- ✅ slf4j-api-1.7.36.jar  
- ✅ slf4j-simple-1.7.36.jar

### 2. File đã cập nhật:
- ✅ `.classpath` - Đã thêm 3 thư viện JAR
- ✅ `WebcamCapture.java` - Sử dụng camera thật
- ✅ `VideoCallWindow.java` - Hiển thị video call với camera
- ✅ `ClientUI.java` - Tích hợp video call window
- ✅ `WebcamTest.java` - File test camera

## 🔧 BƯỚC TIẾP THEO (QUAN TRỌNG!)

### Bước 1: Refresh Eclipse Project
```
1. Chuột phải vào project "ChatAppMultiMode"
2. Chọn "Refresh" hoặc nhấn F5
3. Đợi Eclipse rebuild project
```

### Bước 2: Clean & Build
```
1. Menu: Project → Clean...
2. Chọn "ChatAppMultiMode"
3. Click "Clean"
4. Đợi build xong
```

### Bước 3: Kiểm tra Build Path (nếu vẫn còn lỗi)
```
1. Chuột phải project → Build Path → Configure Build Path
2. Tab "Libraries"
3. Kiểm tra 3 file JAR có trong danh sách:
   - webcam-capture-0.3.12.jar
   - slf4j-api-1.7.36.jar
   - slf4j-simple-1.7.36.jar
4. Nếu chưa có:
   - Click "Add JARs..."
   - Navigate to folder "lib"
   - Chọn cả 3 file JAR
   - Click OK
```

## 🧪 TEST CAMERA

### Chạy WebcamTest để kiểm tra camera:
```
1. Mở file: src/client/WebcamTest.java
2. Chuột phải → Run As → Java Application
3. Nếu camera hoạt động, bạn sẽ thấy video trực tiếp
```

**Kết quả mong đợi:**
- ✅ Cửa sổ mở ra
- ✅ Camera bật và hiển thị video
- ✅ Status bar hiển thị: "✅ Camera running..."
- ✅ Frame counter tăng dần

**Nếu có lỗi:**
- ❌ "No webcam detected" → Kiểm tra camera đã cắm chưa
- ❌ Camera đang được dùng bởi app khác → Đóng app đó
- ❌ Permission denied → Cấp quyền camera cho Java

## 🎮 SỬ DỤNG VIDEO CALL

### 1. Khởi động Server:
```
Run: src/server/ServerUI.java
```

### 2. Khởi động Client (2 instances):
```
Run: src/client/ClientUI.java (Client 1)
Run: src/client/ClientUI.java (Client 2)
```

### 3. Kết nối:
```
Client 1: 
  - Server: localhost
  - Port: 12345
  - Username: User1
  - Connect

Client 2:
  - Server: localhost
  - Port: 12345  
  - Username: User2
  - Connect
```

### 4. Thực hiện Video Call:
```
Client 1:
  1. Chọn "User2" trong danh sách online
  2. Click nút "Call" (Video Call button)
  
Client 2:
  1. Nhận thông báo video call từ User1
  2. Click "Accept"
  3. Cửa sổ VideoCallWindow mở ra
  
Cả hai:
  - Camera tự động bật
  - Video hiển thị ở phần "You"
  - Phần "Partner" chờ video từ partner (TODO)
```

### 5. Các nút điều khiển:
- **Camera** (màu xanh) - Bật/tắt camera
- **Mute** (màu vàng) - Tắt tiếng (chưa implement)
- **End Call** (màu đỏ) - Kết thúc cuộc gọi

## 📋 CHECKLIST

### Hoàn thành:
- [x] Tải thư viện webcam-capture
- [x] Cập nhật .classpath
- [x] WebcamCapture.java với camera thật
- [x] VideoCallWindow.java hiển thị video
- [x] ClientUI.java tích hợp video call
- [x] WebcamTest.java để test
- [x] Hướng dẫn sử dụng

### Chưa hoàn thành (TODO):
- [ ] Truyền video frame qua network
- [ ] Nhận và hiển thị video từ partner
- [ ] Compress video frame (JPEG)
- [ ] Audio capture và streaming
- [ ] Tối ưu bandwidth

## 🐛 TROUBLESHOOTING

### Lỗi compile "package com is not accessible":
```
→ Refresh project (F5)
→ Clean & Build
→ Restart Eclipse
```

### Camera không bật:
```
→ Kiểm tra camera đã cắm
→ Đóng app khác đang dùng camera (Skype, Zoom, etc.)
→ Cấp quyền camera cho Java
→ Chạy WebcamTest.java để debug
```

### Nút Call không mở VideoCallWindow:
```
→ Kiểm tra console có lỗi không
→ Đảm bảo đã Refresh + Clean project
→ Kiểm tra thư viện trong Build Path
```

### Video lag:
```
→ Giảm frame rate (thay 33ms → 66ms)
→ Giảm resolution (640x480 → 320x240)
→ Đóng app khác đang chạy
```

## 📝 GHI CHÚ KỸ THUẬT

### Frame Rate:
- Mặc định: 30 FPS (sleep 33ms)
- Có thể giảm xuống 15 FPS (sleep 66ms) để tiết kiệm CPU

### Resolution:
- Mặc định: 640x480
- Có thể thay đổi trong WebcamCapture.start():
  ```java
  webcam.setViewSize(new Dimension(320, 240)); // Lower resolution
  ```

### Memory:
- Mỗi frame ~900KB (640x480 RGB)
- 30 FPS = ~27 MB/s data
- Cần compress trước khi gửi qua network

## 🚀 NEXT STEPS

Để hoàn thiện video streaming:

1. **Compress frames**:
   ```java
   ByteArrayOutputStream baos = new ByteArrayOutputStream();
   ImageIO.write(image, "jpg", baos);
   byte[] imageBytes = baos.toByteArray();
   ```

2. **Send to partner**:
   ```java
   client.sendVideoFrame(imageBytes, partnerName);
   ```

3. **Receive and display**:
   ```java
   BufferedImage received = ImageIO.read(new ByteArrayInputStream(frameData));
   videoWindow.displayRemoteFrame(received);
   ```

---

**Status**: ✅ Camera support ready!  
**Next**: Implement network video streaming  
**Date**: 10/11/2025
