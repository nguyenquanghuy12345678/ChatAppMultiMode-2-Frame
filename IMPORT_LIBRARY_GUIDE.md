# 🔧 HƯỚNG DẪN IMPORT THƯ VIỆN VÀO ECLIPSE

## ✅ CÁC FILE ĐÃ TẢI VỀ (thư mục `lib/`):

1. **webcam-capture-0.3.12.jar** - Thư viện webcam chính
2. **slf4j-api-1.7.2.jar** - Logging API
3. **bridj-0.6.2.jar** - Native library support

## 📝 CÁC BƯỚC IMPORT VÀO ECLIPSE:

### Bước 1: Refresh Project
```
1. Chuột phải vào project "ChatAppMultiMode"
2. Chọn "Refresh" hoặc nhấn F5
```

### Bước 2: Mở Build Path
```
1. Chuột phải vào project "ChatAppMultiMode"
2. Chọn "Build Path" → "Configure Build Path..."
```

### Bước 3: Thêm JAR Files
```
1. Trong cửa sổ "Java Build Path", chọn tab "Libraries"
2. Click nút "Add JARs..." (KHÔNG phải "Add External JARs")
3. Mở rộng folder "ChatAppMultiMode"
4. Mở folder "lib"
5. Chọn CẢ 3 FILE:
   ☑ webcam-capture-0.3.12.jar
   ☑ slf4j-api-1.7.2.jar
   ☑ bridj-0.6.2.jar
6. Click "OK"
7. Click "Apply and Close"
```

### Bước 4: Clean và Rebuild
```
1. Menu: Project → Clean...
2. Chọn "ChatAppMultiMode"
3. Click "Clean"
4. Đợi build xong
```

## ✅ KIỂM TRA

Sau khi import xong:
1. Mở file `WebcamCapture.java`
2. Các dòng `import com.github.sarxos.webcam.Webcam;` KHÔNG còn lỗi đỏ
3. Không còn lỗi "Webcam cannot be resolved to a type"

## 🧪 TEST

Chạy file `WebcamTest.java`:
```
1. Chuột phải vào WebcamTest.java
2. Run As → Java Application
3. Nếu có camera: Sẽ thấy video trực tiếp
4. Nếu không có camera: Sẽ báo "No webcam detected"
```

## ❗ NẾU VẪN CÒN LỖI

### Lỗi: "The type com.github.sarxos.webcam.Webcam is not accessible"

**Cách 1 - Thêm vào module-info.java:**
```java
module ChatAppMultiMode {
    requires java.desktop;
    requires webcam.capture;  // Thêm dòng này
    requires bridj;           // Thêm dòng này
    requires slf4j.api;       // Thêm dòng này
}
```

**Cách 2 - Xóa module-info.java:**
```
1. Tìm file "src/module-info.java"
2. Xóa file này đi
3. Project → Clean
```

### Lỗi: JAR files không hiển thị trong Build Path

```
1. Đóng Eclipse
2. Mở file .classpath bằng notepad
3. Kiểm tra có 3 dòng này:
   <classpathentry kind="lib" path="lib/webcam-capture-0.3.12.jar"/>
   <classpathentry kind="lib" path="lib/slf4j-api-1.7.2.jar"/>
   <classpathentry kind="lib" path="lib/bridj-0.6.2.jar"/>
4. Lưu và mở lại Eclipse
```

## 🎥 SỬ DỤNG VIDEO CALL

Sau khi import xong thư viện:

1. **Start Server**: Chạy `ServerUI.java`
2. **Start 2 Clients**: Chạy 2 lần `ClientUI.java`
3. **Connect**: Username khác nhau (User1, User2)
4. **Video Call**: 
   - Client 1: Chọn User2 → Click "Call"
   - Client 2: Accept call
   - Cửa sổ video call mở với camera thật!

## 📸 YÊU CẦU CAMERA

- Webcam phải được kết nối TRƯỚC KHI chạy app
- Đóng các app khác đang dùng camera (Skype, Zoom, Teams...)
- Cấp quyền camera cho Java nếu cần

---
**Lưu ý**: Nếu không có camera, app vẫn chạy nhưng hiển thị "No webcam detected"
