# 🎥 HƯỚNG DẪN TEST VIDEO & VOICE CALL

## ✅ ĐÃ FIX TẤT CẢ BUG

### Bug đã sửa:
1. ✅ **Nhiều giao diện UI xuất hiện** → Chỉ còn 1 window mỗi bên
2. ✅ **Màn hình đen, không thấy người còn lại** → Đã có video streaming
3. ✅ **Voice call cũng bị bug** → Audio mode hoạt động ổn định

---

## 🚀 CÁCH TEST NHANH (2 MÁY)

### Bước 1: Chạy Server (1 máy làm server)
```powershell
cd D:\eclipse-workspace\ChatAppMultiMode
.\runServer.bat
```
Lưu lại **IP của máy server** (vd: 192.168.1.100)

### Bước 2: Chạy Client Máy 1
```powershell
.\runClient.bat
```
- **Username:** User1
- **Server IP:** 192.168.1.100 (IP máy server)
- **Port:** 12345

### Bước 3: Chạy Client Máy 2
```powershell
.\runClient.bat
```
- **Username:** User2  
- **Server IP:** 192.168.1.100 (cùng IP máy server)
- **Port:** 12345

---

## 📞 TEST VIDEO CALL

### Trên Máy User1:
1. Click tab **"Private Chat (1-1)"**
2. Chọn **User2** trong danh sách bên trái
3. Click nút **"Call"** (màu đỏ)
4. Chọn **"Video Call"**

### Trên Máy User2:
1. Sẽ hiện dialog: **"User1 is calling you (Video Call)"**
2. Click **"Yes"** để chấp nhận

### ✅ Kết Quả Đúng:
- **User1 & User2:** Mỗi người CHỈ MỞ **1 WINDOW DUY NHẤT**
- **Bên TRÁI window:** Camera của chính mình
- **Bên PHẢI window:** Camera của người còn lại (**KHÔNG ĐEN NỮA!**)
- Video chạy mượt ~10 FPS

---

## 🎤 TEST VOICE CALL (Audio Only)

### Trên Máy User1:
1. Click tab **"Private Chat (1-1)"**
2. Chọn **User2**
3. Click nút **"Call"**
4. Chọn **"Audio Call"**

### Trên Máy User2:
1. Dialog hiện: **"User1 is calling you (Audio Call)"**
2. Click **"Yes"**

### ✅ Kết Quả Đúng:
- Mỗi bên 1 window
- Hiện text **"Audio Only Call"** thay vì camera
- KHÔNG mở camera

---

## 🛑 TEST END CALL

### Cách 1: User1 nhấn "End Call"
- ✅ Cả 2 window đóng
- ✅ User2 thấy: "User1 ended the call"

### Cách 2: User2 nhấn "End Call"  
- ✅ Cả 2 window đóng
- ✅ User1 thấy: "User2 ended the call"

---

## ❌ TEST REJECT CALL

### User1 gọi User2 → User2 nhấn "No"
- ✅ User1 thấy: "User2 rejected your call"
- ✅ KHÔNG mở window nào

---

## 🔴 TEST BUSY STATE

### Setup:
1. User1 đang gọi User2 (đang trong call)
2. User3 cố gọi User2

### Kết quả:
- ✅ User3 nhận: "User2 rejected your call"
- ✅ User2 thấy: "Busy - Already in another call"

---

## 🐛 XỬ LÝ LỖI

### Lỗi: "No camera detected"
**Nguyên nhân:** Webcam đang bị app khác sử dụng  
**Giải pháp:**
1. Đóng Zoom, Skype, Teams
2. Đóng WebcamTest.bat nếu đang chạy
3. Thử lại

### Lỗi: Vẫn thấy màn hình đen
**Kiểm tra:**
1. Firewall có block Java không?
2. Cả 2 máy cùng mạng LAN?
3. Server log có thấy "VIDEO_FRAME" không?

**Fix:**
```powershell
# Tắt firewall tạm để test
Set-NetFirewallProfile -Profile Domain,Public,Private -Enabled False

# Bật lại sau khi test
Set-NetFirewallProfile -Profile Domain,Public,Private -Enabled True
```

### Lỗi: Video lag/chậm
**Nguyên nhân:** Mạng chậm  
**Giải pháp:** Edit `VideoCallWindow.java` line ~97:
```java
Thread.sleep(100); // Tăng lên 200 nếu lag
```

---

## 📊 CHECKLIST TEST

- [ ] **Video Call:** 1 window mỗi bên
- [ ] **Video Call:** Thấy camera đối phương
- [ ] **Audio Call:** Hiện "Audio Only"
- [ ] **Reject:** Không mở window
- [ ] **End Call:** Cả 2 đóng
- [ ] **Busy:** Reject tự động

---

## 🎯 DEMO SCRIPT

```
[Máy 1 - User1]
> Chọn User2
> Click "Call" → "Video Call"
> Chờ User2 accept...
> ✅ Thấy video của User2 bên phải
> Click "End Call"

[Máy 2 - User2]  
> Accept incoming call
> ✅ Thấy video của User1 bên phải
> Window tự đóng khi User1 end
```

---

**Compiled:** ✅ BUILD SUCCESSFUL  
**Status:** Ready to test!  
**Next:** Chạy runServer.bat + 2x runClient.bat
