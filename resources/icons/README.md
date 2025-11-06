# 📁 Icons Folder - 50 Emoji Icons

## ⬇️ Hướng Dẫn Tải Icons

### Bước 1: Tải Emoji Icons (FREE)
Tải 50 emoji icons miễn phí từ các nguồn sau:

#### 🌟 Nguồn Khuyến Nghị
1. **Flaticon** - https://www.flaticon.com/
   - Search: "emoji", "emoticon", "smiley"
   - Chọn bộ icon đẹp, tải về PNG format
   - Size: 128x128 hoặc 256x256 pixels

2. **Icons8** - https://icons8.com/icons/set/emoji
   - Có sẵn bộ emoji đẹp
   - Download PNG, size 100-200px
   - Free for personal use

3. **Emoji One** - https://www.emojione.com/
   - Bộ emoji open source
   - Format: PNG với nền trong suốt

### Bước 2: Đổi Tên File
Đổi tên các file icon theo danh sách này và copy vào `resources/icons/`:

```
Emotions (1-15):
- emoji_smile.png       - 😄 Mặt cười
- emoji_laugh.png       - 😂 Cười lớn
- emoji_wink.png        - 😉 Nháy mắt
- emoji_love.png        - 😍 Yêu
- emoji_heart.png       - ❤️ Trái tim
- emoji_kiss.png        - 😘 Hôn
- emoji_thinking.png    - 🤔 Suy nghĩ
- emoji_cool.png        - 😎 Ngầu
- emoji_star.png        - ⭐ Sao
- emoji_sad.png         - 😢 Buồn
- emoji_cry.png         - 😭 Khóc
- emoji_angry.png       - 😠 Giận
- emoji_surprised.png   - � Ngạc nhiên
- emoji_sleepy.png      - 😴 Buồn ngủ
- emoji_sick.png        - 🤒 Ốm

Celebrations (16-25):
- emoji_party.png       - 🎉 Tiệc tùng
- emoji_celebrate.png   - 🎊 Chúc mừng
- emoji_fire.png        - 🔥 Lửa/Hot
- emoji_clap.png        - 👏 Vỗ tay
- emoji_thumbsup.png    - 👍 Like
- emoji_thumbsdown.png  - 👎 Dislike
- emoji_ok.png          - 👌 OK
- emoji_peace.png       - ✌️ Peace
- emoji_muscle.png      - 💪 Mạnh
- emoji_pray.png        - 🙏 Cầu nguyện

Nature (26-35):
- emoji_sun.png         - ☀️ Mặt trời
- emoji_moon.png        - 🌙 Mặt trăng
- emoji_star2.png       - 🌟 Ngôi sao
- emoji_cloud.png       - ☁️ Mây
- emoji_rain.png        - 🌧️ Mưa
- emoji_snow.png        - ❄️ Tuyết
- emoji_thunder.png     - ⚡ Sét
- emoji_rainbow.png     - 🌈 Cầu vồng
- emoji_flower.png      - 🌸 Hoa
- emoji_tree.png        - 🌳 Cây

Animals (36-40):
- emoji_cat.png         - 🐱 Mèo
- emoji_dog.png         - 🐶 Chó
- emoji_bird.png        - 🐦 Chim
- emoji_fish.png        - � Cá
- emoji_butterfly.png   - 🦋 Bướm

Food (41-45):
- emoji_pizza.png       - 🍕 Pizza
- emoji_cake.png        - 🎂 Bánh
- emoji_coffee.png      - ☕ Cà phê
- emoji_beer.png        - 🍺 Bia
- emoji_fruit.png       - 🍎 Trái cây

System (46-50):
- emoji_check.png       - ✅ Đúng
- emoji_cross.png       - ❌ Sai
- emoji_warning.png     - ⚠️ Cảnh báo
- emoji_info.png        - ℹ️ Thông tin
- emoji_question.png    - ❓ Hỏi
```

### Bước 3: Kiểm Tra Kích Thước
- Format: PNG với nền trong suốt
- Size khuyến nghị: 128x128 hoặc 256x256 pixels
- Chất lượng: Rõ nét, không bị vỡ

### Bước 4: Copy vào Project
```powershell
# Copy tất cả file vào
Copy-Item *.png d:\eclipse-workspace\ChatAppMultiMode\resources\icons\
```

---

## 🎨 Tạo Icons Nhanh (Nếu Không Tải)

### Sử dụng Placeholder
App sẽ tự động tạo placeholder icon (hình tròn xám) nếu không tìm thấy file

### Tạo Icons Đơn Giản
Bạn có thể tạo nhanh bằng Paint/GIMP:
1. Tạo ảnh 128x128 pixels
2. Vẽ emoji đơn giản
3. Save as PNG
4. Đổi tên theo danh sách

---

## 💡 Sử Dụng Trong Chat

### Chọn Emoji
1. Click nút **😊** bên cạnh ô nhập tin
2. Chọn emoji từ grid (10x5 = 50 emojis)
3. Emoji code được thêm vào: `[:emoji_smile:]`

### Emoji trong Message
Khi gửi message:
```
Hello [:emoji_smile:] How are you? [:emoji_thumbsup:]
```

Người nhận sẽ thấy:
```
Hello 😄 How are you? 👍
```
(với icon images, không phải text Unicode)

---

## 🔧 Kỹ Thuật

### Emoji Code Format
```
[:emoji_name:]
```

### Rendering
- App sử dụng `EmojiTextPane` (custom JTextPane)
- Parse emoji codes: `Pattern.compile("\\[:([^\\]]+):\\]")`
- Insert icons inline: `StyleConstants.setIcon()`

---

## 📌 Lưu Ý Quan Trọng

### ✅ SỬ DỤNG ICONS (PNG)
- Không bị lỗi hiển thị □
- Hỗ trợ tất cả hệ điều hành
- Tùy chỉnh được

### ❌ KHÔNG DÙNG UNICODE TEXT
- Unicode emojis (😄❤️☀️) → hiển thị □ hoặc �
- Phụ thuộc vào font hệ thống
- Không đồng nhất

---

## 🚀 Quick Start

Nếu chưa có icons, app vẫn chạy được với placeholder icons!

Sau đó từ từ thêm icons vào để đẹp hơn.

