# ✅ Thư Viện Đã Được Tải Về

## 📦 Các thư viện trong thư mục `lib/`:

1. **webcam-capture-0.3.12.jar** - Thư viện chính để capture camera
2. **slf4j-api-1.7.36.jar** - Logging framework (dependency)
3. **slf4j-simple-1.7.36.jar** - Logging implementation

## 🔧 Cấu hình Eclipse

File `.classpath` đã được cập nhật tự động với các thư viện này.

### Nếu Eclipse chưa nhận thư viện:

1. **Refresh Project**:
   - Chuột phải vào project → `Refresh` (F5)

2. **Clean & Build**:
   - Menu: `Project` → `Clean...`
   - Chọn project của bạn
   - Click `Clean`

3. **Kiểm tra Build Path**:
   - Chuột phải vào project → `Build Path` → `Configure Build Path`
   - Tab `Libraries` → Kiểm tra xem 3 file JAR đã có chưa
   - Nếu chưa có, click `Add JARs...` → Chọn từ thư mục `lib/`

## 🎥 Tính năng Video Call

### Đã hoàn thành:
- ✅ Tải thư viện webcam-capture
- ✅ Cập nhật `WebcamCapture.java` để sử dụng camera thật
- ✅ Cập nhật `VideoCallWindow.java` để hiển thị video
- ✅ Tích hợp với `ClientUI.java`

### Cách sử dụng:
1. Kết nối với server
2. Chọn user để gọi
3. Click nút **"Call"** (Video Call)
4. Partner nhận và accept call
5. Cửa sổ video call sẽ mở với camera của bạn

### Các nút điều khiển:
- **Camera** - Bật/tắt camera
- **Mute** - Tắt tiếng (chưa implement)
- **End Call** - Kết thúc cuộc gọi

## 🐛 Xử lý lỗi

### Nếu camera không hoạt động:
1. Kiểm tra camera đã được kết nối chưa
2. Đóng các ứng dụng khác đang sử dụng camera
3. Cấp quyền camera cho Java/Eclipse
4. Kiểm tra console để xem thông báo lỗi

### Nếu có lỗi compile:
1. Refresh project (F5)
2. Clean and build project
3. Restart Eclipse

## 📝 Ghi chú

- Thư viện webcam-capture tự động detect camera có sẵn
- Hỗ trợ đa nền tảng: Windows, macOS, Linux
- Độ phân giải mặc định: 640x480
- Frame rate: ~30 FPS

## 🚀 Các bước tiếp theo (TODO):

1. **Truyền video frame qua network**:
   - Implement `client.sendVideoFrame(image, partnerName)` 
   - Compress image trước khi gửi (JPEG)
   - Handle receive và display remote frame

2. **Audio support**:
   - Thêm thư viện audio capture
   - Implement audio streaming

3. **Tối ưu**:
   - Giảm độ phân giải khi network chậm
   - Add quality control
   - Buffer management

---

**Tác giả**: GitHub Copilot  
**Ngày tạo**: 10/11/2025
