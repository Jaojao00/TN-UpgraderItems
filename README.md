# ⚒ TN-UpgraderItems (Paper 1.21+)
**Tác giả (Author):** Tài Nguyễn  
**Phiên bản hỗ trợ:** Paper / Purpur `1.21` ▸ `1.21.11+` (Java 21)  
**Đặc điểm cốt lõi:** **Hoạt động độc lập 100%** với Hệ Sinh Thái Cổ Ngữ, Thuộc Tính, Đá Cường Hóa & Bảo Vật Thần Thoại của riêng `TN-UpgraderItems` *(và tự động mở rộng nếu máy chủ có cài thêm `MythicMobs`, `ExcellentEnchants`, `Floodgate/Geyser`)*.

---

## 🌟 Hệ Sinh Thái Độc Quyền Của Riêng `TN-UpgraderItems` (Không Phụ Thuộc Plugin Ngoài)

Ngay cả khi máy chủ của bạn **không cài bất kỳ plugin phụ trợ nào**, `TN-UpgraderItems` vẫn tự tạo ra một hệ sinh thái nâng cấp End-Game hoàn chỉnh, sâu sắc và gây nghiện:

### 1. 🔮 12 Cổ Ngữ Độc Quyền TN-Upgrader (`NativeEnchantment` - Tab 3 Bàn Cường Hóa)
Các kỹ năng nội tại được xử lý trực tiếp bởi lõi của plugin, chia làm 3 nhánh chuyên biệt:

#### ⛏ Nhánh Công Cụ & Khai Thác (Cuốc chim, Xẻng, Rìu, Cuốc)
1. **⛏ Mũi Khoan Địa Tâm (`VEIN_MINER`):** Khi đào 1 khối quặng hoặc chặt 1 khúc gỗ, tự động phá vỡ liên hoàn cả mạch quặng/cây gỗ liền kề *(Giữ phím Shift để tạm tắt khi cần đào 1 khối)*.
2. **🔨 Búa Thần Khai Sơn (`DRILL_3X3`):** Mở rộng phạm vi đào đá, đất, sỏi, cát thành vùng rộng **3x3 khối** giúp đào hầm và san lấp mặt bằng nhanh gấp 9 lần.
3. **🔥 Lò Nung Tự Động (`AUTO_SMELT`):** Tự động nung chảy Quặng Sắt, Vàng, Đồng, Cát, Đá cuội thành Thỏi kim loại & Kính ngay khi đào, kèm rơi thêm hạt Kinh Nghiệm (EXP) và nhân sản lượng theo cấp *Gia Tài*.
4. **🧲 Từ Trường Hút Đồ (`TELEKINESIS`):** Mọi khoáng sản đào được hoặc vật phẩm rơi từ quái bị tiêu diệt sẽ **bay thẳng vào túi đồ** của bạn, không bao giờ sợ rơi xuống Lava.
5. **🌾 Thu Hoạch Mùa Vụ (`HARVESTER`):** Khi thu hoạch lúa, cà rốt, khoai tây, củ dền đã chín sẽ **tự động gieo lại hạt giống mới** xuống đất.

#### ⚔ Nhánh Vũ Khí Chiến Đấu (Kiếm, Rìu, Cung, Nỏ, Đinh ba, Chùy 1.21)
6. **⚡ Lôi Phạt Thiên Kích (`THUNDER_STRIKE`):** Đòn đánh có xác suất triệu hồi tia sét giáng thẳng xuống mục tiêu, gây thêm sát thương sét.
7. **❄ Hàn Băng Phong Ấn (`FROST_BITE`):** Đóng băng và làm chậm mục tiêu, khiến kẻ địch tê cóng không thể chạy trốn.
8. **☠ Độc Dược Hắc Ám (`VENOMOUS`):** Tiêm độc tố gây hiệu ứng Trúng Độc (Poison) & Khô Héo (Wither) rút máu liên tục.
9. **🗡 Trảm Quyết Đột Kích (`EXECUTIONER`):** Khi máu mục tiêu dưới 35%, đòn đánh bùng nổ thêm **+12% ▸ +60% sát thương kết liễu**.

#### 🛡 Nhánh Giáp Trụ Hộ Thể (Mũ, Áo, Quần, Ủng, Elytra, Khiên)
10. **💚 Sinh Mệnh Tái Tạo (`VITALITY_REGEN`):** Khi bị tấn công, tự động kích hoạt luồng sinh khí Hồi Phục (Regeneration).
11. **🔥 Khiên Hỏa Diệm (`MOLTEN_SHIELD`):** Thiêu đốt kẻ tấn công bạn và ban hiệu ứng Kháng Lửa bảo vệ cơ thể.
12. **🌪 Bước Chân Thần Tốc (`WIND_STEP`):** Khi máu tụt xuống dưới 40%, tự động kích hoạt **Tăng Tốc II & Lá Chắn Tim Vàng (Absorption)** để thoát hiểm hoặc lật kèo.

---

### 2. 💎 Đá Cường Hóa, Tinh Thể & Bảo Vật Thần Thoại Nội Tại
Plugin tích hợp sẵn bộ nguyên liệu và trang bị Thần Thoại riêng trong kho **`TÌM PHẦN THƯỞNG`** và bàn chế tạo:
- **💎 Đá Cường Hóa Tinh Anh (`1,000 V`):** Chế tạo từ *Thỏi Vàng + Kim Cương + Mảnh Thạch Anh Tím* (hoặc nhận qua lệnh `/nangcap stone`).
- **🔮 Tinh Thể Cổ Ngữ Thần Thoại (`3,000 V`):** Nguyên liệu cao cấp dùng để khắc Cổ Ngữ và đột phá trang bị (`/nangcap crystal`).
- **🍀 Bùa Đại Cát TN-Upgrader (`2,500 V`):** Tín vật may mắn giá trị cao.
- **4 Bảo Vật Thần Thoại Độc Quyền:**
  - **⛏ Cuốc Chim Thần Công Khai Sơn (`18,000 V`)**
  - **⚔ Huyết Kiếm Đoạt Mệnh (`20,000 V`)**
  - **🪓 Chiến Rìu Băng Lôi Khai Thiên (`17,500 V`)**
  - **🛡 Chiến Giáp Hộ Thần Bất Diệt (`22,000 V`)**

---

### 3. ⚔ Bàn Cường Hóa Trang Bị 3 Tab & Đấu Giá Vật Phẩm 3x3
- **Bàn Đấu Giá Vật Phẩm (`NÂNG CẤP VẬT PHẨM`):** Lưới đồ cược 3x3 (hỗ trợ **1-Chạm cho PE & PC**), vòng quay tỷ lệ may rủi kịch tính (`0.00%` ▸ `100.00%`), hệ số nhân nhanh `x1` ▸ `x10`, kho `Tìm Phần Thưởng`, `Màu Giao diện`, `Yêu thích`, `Cài đặt`, `Thống kê Top Server`.
- **Bàn Cường Hóa Trang Bị (`CƯỜNG HÓA TRANG BỊ & CỔ NGỮ`)** chia làm **3 Tab rõ ràng**:
  - **Tab 1 (`Ô 15` - Thuộc Tính Cường Hóa):** 9 chỉ số gốc (*Sát Thương, Sinh Lực, Giáp, Độ Cứng Giáp, Tốc Đánh & Đào Nhanh +25%/cấp, Tốc Chạy, Hút Máu, Bạo Kích, Sát Thương Chuẩn*).
  - **Tab 2 (`Ô 16` - Phù Phép Cơ Bản):** *Hiệu Suất (Đào Nhanh), Chậm Hỏng (Độ Bền), Gia Tài, Sửa Chữa, Chạm Nhẹ, Đào Nhanh Dưới Nước, Sắc Bén, Bảo Vệ...* nâng vượt giới hạn thông thường.
  - **Tab 3 (`Ô 17` - Cổ Ngữ Độc Quyền TN-Upgrader):** Khắc 12 Cổ Ngữ nội tại độc quyền lên Công cụ, Vũ khí, Giáp.
  - **Bảo Hiểm May Mắn (Pity):** Đập xịt **GIỮ NGUYÊN 100% TRANG BỊ** và cộng tích lũy **+3.5% May Mắn** cho lần đập kế tiếp!

---

## 💻 Lệnh & Quyền Hạn (Commands & Permissions)
| Lệnh | Mô tả | Quyền hạn |
|---|---|---|
| `/upgrader` (hoặc `/nangcap`, `/nc`, `/tnupgrader`) | Mở giao diện **Nâng Cấp Vật Phẩm (Đấu Giá 3x3)** | `tnupgrader.use` (Mặc định: `true`) |
| `/upgrader equip` | Mở nhanh **Bàn Cường Hóa Trang Bị & Cổ Ngữ** | `tnupgrader.use` |
| `/upgrader stats` | Mở bảng **Thống Kê & Top Server** | `tnupgrader.use` |
| `/upgrader tool [tên] [số_lượng]` | Nhận **Sổ Tay Nâng Cấp Di Động** | `tnupgrader.admin` (Mặc định: `op`) |
| `/upgrader block [tên] [số_lượng]` | Nhận **Đài Nâng Cấp Tối Thượng** | `tnupgrader.admin` |
| `/upgrader stone [tên] [số_lượng]` | Nhận **Đá Cường Hóa Tinh Anh (1,000 V)** | `tnupgrader.admin` |
| `/upgrader crystal [tên] [số_lượng]` | Nhận **Tinh Thể Cổ Ngữ Thần Thoại (3,000 V)** | `tnupgrader.admin` |
| `/upgrader setvalue <giá_trị_V>` | Đặt điểm giá trị `V` cho vật phẩm đang cầm trên tay | `tnupgrader.admin` |
| `/upgrader reload` | Tải lại `config.yml`, bảng giá `V` và công thức | `tnupgrader.admin` |
