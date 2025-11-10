# ✅ ĐÃ LOẠI BỎ MAVEN - CHUYỂN VỀ JAVA THUẦN

## 🔧 ĐÃ THỰC HIỆN:

1. ✅ Xóa `pom.xml`
2. ✅ Xóa thư mục `target/`
3. ✅ Cập nhật `.classpath` - Loại bỏ Maven dependencies
4. ✅ Cập nhật `.project` - Loại bỏ Maven nature
5. ✅ Tạo thư mục `bin/` cho output
6. ✅ Giữ lại 3 thư viện JAR trong `lib/`:
   - webcam-capture-0.3.12.jar
   - slf4j-api-1.7.2.jar
   - bridj-0.6.2.jar

---

## 🎯 BẠN CẦN LÀM TRONG ECLIPSE:

### Bước 1: Đóng Eclipse (nếu đang mở)
```
File → Exit
```

### Bước 2: Xóa các file Maven còn sót
```
1. Mở Windows Explorer
2. Đi đến: D:\eclipse-workspace\ChatAppMultiMode
3. Xóa các file/folder (nếu có):
   - .settings/org.eclipse.m2e.*
   - .mvn/
   - mvnw
   - mvnw.cmd
```

### Bước 3: Mở lại Eclipse
```
File → Open Projects from File System...
Hoặc: Import → General → Existing Projects into Workspace
Chọn: D:\eclipse-workspace\ChatAppMultiMode
```

### Bước 4: Refresh Project
```
Chuột phải vào project → Refresh (F5)
```

### Bước 5: Clean Project
```
Menu: Project → Clean...
Chọn: ChatAppMultiMode
Click: Clean
```

### Bước 6: Kiểm tra Build Path
```
Chuột phải project → Build Path → Configure Build Path
Tab "Libraries" → Phải có:
  ✓ JRE System Library [JavaSE-11]
  ✓ webcam-capture-0.3.12.jar
  ✓ slf4j-api-1.7.2.jar
  ✓ bridj-0.6.2.jar

Nếu chưa có các JAR:
  1. Click "Add JARs..."
  2. Chọn folder "lib"
  3. Chọn cả 3 file JAR
  4. OK → Apply and Close
```

### Bước 7: Test WebcamTest
```
1. Mở: src/client/WebcamTest.java
2. Chuột phải → Run As → Java Application
3. Không còn lỗi ClassNotFoundException!
```

---

## 🎥 CHẠY ỨNG DỤNG:

### Server:
```
Mở: src/server/ServerUI.java
Chuột phải → Run As → Java Application
```

### Client:
```
Mở: src/client/ClientUI.java
Chuột phải → Run As → Java Application
(Có thể chạy nhiều lần cho nhiều client)
```

### Video Call:
```
1. Connect 2 clients
2. Click "Call" → Accept
3. Cửa sổ video mở với camera!
```

---

## ✅ KẾT QUẢ:

- ✅ Không còn Maven
- ✅ Dự án Java thuần Eclipse
- ✅ Output: `bin/` (không phải `target/`)
- ✅ Thư viện: 3 JAR files trong `lib/`
- ✅ Có thể Run As → Java Application

---

## 🐛 NẾU VẪN CÒN LỖI:

### Lỗi: "Could not find or load main class"
```
→ Clean project
→ Kiểm tra bin/ folder có được tạo
→ Rebuild project
```

### Lỗi: "Webcam cannot be resolved"
```
→ Kiểm tra Build Path có 3 JAR
→ Nếu thiếu: Add JARs từ lib/
→ Clean & Rebuild
```

### Eclipse vẫn nghĩ đây là Maven project
```
→ Đóng Eclipse
→ Xóa file: .settings/org.eclipse.m2e.core.prefs
→ Mở lại Eclipse
→ Refresh project
```

---

**Trạng thái**: ✅ Đã loại bỏ Maven  
**Bước tiếp**: Đóng Eclipse → Mở lại → Refresh → Clean  
**Ngày**: 10/11/2025
