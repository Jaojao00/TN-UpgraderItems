# ⚒ TN-UpgraderItems (Paper 1.21+)
**Tác giả (Author):** Tài Nguyễn  
**Phiên bản hỗ trợ:** Paper / Purpur `1.21` ▸ `1.21.11+` (Java 21)  
**Tích hợp hỗ trợ:** `MythicMobs`, `ExcellentEnchants`, `Floodgate / Geyser (PE Bedrock)`

---

## 🌟 Điểm Độc Đáo & Đột Phá Của Plugin
**TN-UpgraderItems** kết hợp hoàn hảo **2 chế độ nâng cấp gây nghiện** trong cùng một hệ thống giao diện trực quan, hỗ trợ thao tác **1-Chạm (One-Tap)** cực mượt cho cả người chơi **PE (Điện thoại)** lẫn **PC**:

### 1. 🎲 Chế Độ 1: Đấu Giá Nâng Cấp Vật Phẩm (Chuẩn 100% Bộ Ảnh Mẫu)
- **Lưới Đồ Cược 3x3 (`ĐỒ CƯỢC 0/64`)**: Người chơi chỉ cần **chạm trực tiếp** vào vật phẩm trong túi đồ để đưa lên lưới 3x3 mà không cần kéo thả. Mỗi vật phẩm có điểm giá trị **`V`** riêng (VD: Thỏi sắt = `27 V` $\rightarrow$ 36 Thỏi sắt = `972 V`, Kim cương = `250 V`, Thỏi Netherite = `1656 V`, Cánh Elytra = `16000 V`, Táo vàng phù phép = `6000 V`...).
- **Vòng Quay Tỷ Lệ May Rủi Kịch Tính (`0.00%` ▸ `100.00%`)**:
  - Công thức chuẩn:
    $$\text{Tỷ lệ thành công (\%)} = \min\left(100\%, \frac{\text{Tổng Giá Trị Cược } (V)}{\text{Đơn Giá Đích } (V) \times \text{Số Lượng Đích}} \times 100\right)$$
  - Khi bấm **`NÂNG CẤP`**, vòng tròn trung tâm chuyển sang trạng thái **`ĐANG QUAY`**, kim chỉ thị chạy vòng quanh vòng tròn 8 ô kèm âm thanh tích-tắc chậm dần đầy hồi hộp rồi dừng lại ở **`THÀNH CÔNG`** (nổ pháo hoa, nhận vật phẩm, thông báo toàn server) hoặc **`THẤT BẠI`** (nút đổi thành **`TIẾP TỤC`**).
- **Thanh Hệ Số Nhân Nhanh (`x1` ▸ `x10`) & `Số lượng đích`**: Bấm `x2`, `x5`, `x10` để hệ thống tự động chọn ngay phần thưởng có giá trị gấp tương ứng số `V` đang cược!
- **Kho `TÌM PHẦN THƯỞNG` Thông Minh**:
  - Tìm kiếm theo tên tiếng Việt không dấu/có dấu hoặc mã vật phẩm (`Tìm: tên / mã`, VD: gõ `kim` ra *Ủng kim cương, Kiếm kim cương, Quặng kim cương...*).
  - Lọc theo danh mục (`Tất cả`, `Vũ khí & Giáp`, `Công cụ`, `Khoáng sản`, `Thức ăn`, `Khối & Tự nhiên`, `MythicMobs & Phù phép`).
  - Sắp xếp theo `Giá tăng`, `Giá giảm`, `Tỉ lệ cao ▸ thấp`, `Tên A ▸ Z`.
  - Tự động tính số lượng đề xuất (`xN`) khi số `V` cược lớn hơn đơn giá vật phẩm.
- **Tiện Ích Cá Nhân Hóa**:
  - **`Màu Giao diện`**: Đổi chủ đề màu kính (Vàng Hoàng Kim, Xanh Kim Cương, Lục Bảo, Đỏ Hỏa Ngục, Tím Huyền Bí, Đen Tối Giản).
  - **`Yêu thích ★`**: Lưu các món đồ mục tiêu hay nâng cấp.
  - **`Cài đặt`**: Bật/tắt hiệu ứng quay vòng tròn (Quay hồi hộp vs Quay siêu tốc), âm thanh, thông báo server.
  - **`Thống kê`**: Xem tổng lượt đập, tỉ lệ thắng thực tế, lợi nhuận ròng `V`, chuỗi thắng kỷ lục và **Top 5 Đại Gia Nâng Cấp** toàn server!

---

### 2. ⚔ Chế Độ 2: Bàn Cường Hóa Trang Bị End-Game (MythicMobs & ExcellentEnchants)
- **Tạo End-Game Content Đích Thực**: Khi người chơi đã có đồ **Netherite full bùa** hoặc trang bị **MythicMobs**, họ vẫn có động lực cày cuốc tài nguyên để cường hóa thêm các dòng chỉ số độc quyền!
- **9 Dòng Thuộc Tính Bổ Sung Độc Quyền**:
  1. `⚔ Sát Thương Tấn Công` (`+Attack Damage`)
  2. `❤ Sinh Lực Tối Đa` (`+Max Health`)
  3. `🛡 Giáp Phòng Thủ` (`+Armor`)
  4. `💎 Độ Cứng Giáp` (`+Armor Toughness`)
  5. `⚡ Tốc Độ Xuất Chiêu` (`+Attack Speed`)
  6. `👟 Tốc Độ Di Chuyển` (`+Movement Speed`)
  7. `🩸 Hút Máu Chiến Đấu` *(Hồi máu theo % sát thương gây ra)*
  8. `💥 Bạo Kích Huỷ Diệt` *(Xác suất nổ x1.5 sát thương kèm hiệu ứng)*
  9. `🔥 Sát Thương Chuẩn (Xuyên Giáp)` *(Đánh xuyên giáp đối thủ & Boss MythicMobs)*
- **Cân Bằng Tuyệt Đối**: Mặc định giới hạn tối đa **3 dòng thuộc tính bổ sung** trên mỗi món trang bị (`max-custom-attributes: 3`), mỗi dòng nâng tối đa **Cấp 10**.
- **Nâng Cấp Phù Phép Vượt Giới Hạn (`ExcellentEnchants` & Vanilla)**: Tự động quét toàn bộ phù phép từ `Registry.ENCHANTMENT` của Paper 1.21+ (bao gồm mọi phù phép custom của **ExcellentEnchants**) để nâng cấp vượt mốc thông thường!
- **Thất Bại Không Mất Đồ + Bảo Hiểm May Mắn (Pity)**: Cường hóa thất bại sẽ **giữ nguyên trạng thái trang bị**, chỉ tiêu hao nguyên liệu và cộng tích lũy **+3.5% Bảo Hiểm May Mắn** cho lần cường hóa kế tiếp!

---

## 🛠 Công Thức Chế Tạo Khối & Công Cụ Đặc Biệt
1. **⚒ Đài Nâng Cấp Tối Thượng (Khối chức năng đặt dưới đất - Có Hologram nổi)**:
   - Hàng 1: `Thỏi Vàng` | `Kim Cương` | `Thỏi Vàng`
   - Hàng 2: `Kim Cương` | `Cái Đe (Anvil)` | `Kim Cương`
   - Hàng 3: `Thỏi Vàng` | `Khối Vàng` | `Thỏi Vàng`
2. **⚙ Sổ Tay Nâng Cấp Di Động (Công cụ cầm tay mở mọi lúc mọi nơi)**:
   - Hàng 1: `Trống` | `Thỏi Vàng` | `Trống`
   - Hàng 2: `Kim Cương` | `Quyển Sách` | `Kim Cương`
   - Hàng 3: `Trống` | `Thỏi Vàng` | `Trống`

---

## 💻 Lệnh & Quyền Hạn (Commands & Permissions)
| Lệnh | Mô tả | Quyền hạn |
|---|---|---|
| `/upgrader` (hoặc `/nangcap`, `/nc`, `/tnupgrader`) | Mở giao diện **Nâng Cấp Vật Phẩm** | `tnupgrader.use` (Mặc định: `true`) |
| `/upgrader equip` | Mở nhanh **Bàn Cường Hóa Trang Bị End-Game** | `tnupgrader.use` |
| `/upgrader stats` | Mở bảng **Thống Kê & Top Server** | `tnupgrader.use` |
| `/upgrader tool [tên] [số_lượng]` | Nhận **Sổ Tay Nâng Cấp Di Động** | `tnupgrader.admin` (Mặc định: `op`) |
| `/upgrader block [tên] [số_lượng]` | Nhận **Đài Nâng Cấp Tối Thượng** | `tnupgrader.admin` |
| `/upgrader setvalue <giá_trị_V>` | Đặt điểm giá trị `V` cho vật phẩm đang cầm trên tay | `tnupgrader.admin` |
| `/upgrader reload` | Tải lại `config.yml`, bảng giá `V` và hooks | `tnupgrader.admin` |
