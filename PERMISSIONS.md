# 🔑 Bảng Quyền Hạn, Phân Cấp VIP & Chống Cày Đồ — TN-UpgraderItems

**Tác giả (Author):** Tài Nguyễn  
**Plugin:** `TN-UpgraderItems` (Paper 1.21+)

---

## 1. 📋 Bảng Quyền Hạn Chi Tiết (Permissions)

### A. Quyền Truy Cập Chế Độ & Nhánh Nâng Cấp
| Node Quyền Hạn (Permission) | Mặc định | Mô tả chi tiết |
| :--- | :---: | :--- |
| `tnupgrader.use` | `true` | Quyền cơ bản để mở giao diện nâng cấp và tương tác với Đài Nâng Cấp / Sổ Tay. |
| `tnupgrader.command.open` | `true` | Cho phép dùng lệnh `/upgrader`, `/nangcap`, `/nc` để mở trực tiếp từ xa. |
| `tnupgrader.mode.wager` | `true` | Cho phép sử dụng **Chế độ 1: Đấu Giá Nâng Đời Vật Phẩm (3x3)**. |
| `tnupgrader.mode.equipment` | `true` | Cho phép sử dụng **Chế độ 2: Bàn Cường Hóa Trang Bị & Cổ Ngữ**. |
| `tnupgrader.tab.attributes` | `true` | Cho phép nâng cấp ở **Tab 1: Thuộc Tính Cường Hóa** (Sát thương, Đào nhanh, Máu, Giáp, Hút máu...). |
| `tnupgrader.tab.enchants` | `true` | Cho phép nâng cấp ở **Tab 2: Phù Phép** (Hiệu Suất, Chậm Hỏng, Gia Tài, Sửa Chữa...). |
| `tnupgrader.tab.runes` | `true` | Cho phép khắc kỹ năng ở **Tab 3: 12 Cổ Ngữ Độc Quyền TN-Upgrader** (Đào Mạch Quặng, Đào 3x3...). |

### B. Quyền Mở Khóa Bậc Phần Thưởng (Chống Cày Đồ Cuối Game Sớm)
| Node Quyền Hạn (Permission) | Mặc định | Mô tả chi tiết |
| :--- | :---: | :--- |
| `tnupgrader.tier.epic` | `true` | Cho phép chọn nâng cấp các vật phẩm **Bậc Cao Cấp** có đơn giá từ `1,500 V` đến dưới `10,000 V` (Phôi Netherite, Trang bị Netherite, Sách Phù Phép...). |
| `tnupgrader.tier.legendary` | `op` | Cho phép chọn nâng cấp các **Bảo Vật & Trang Bị Thần Thoại** có đơn giá từ `10,000 V` trở lên (Huyết Kiếm Đoạt Mệnh, Cuốc Chim Khai Sơn, Chiến Giáp Hộ Thần...). Có thể cấp cho Rank VIP/Cảnh Giới cao qua LuckPerms. |

### C. Quyền Đặc Quyền VIP (Tăng Giới Hạn Lượt & Trần Tỉ Lệ Thắng)
| Node Quyền Hạn (Permission) | Mặc định | Mô tả chi tiết |
| :--- | :---: | :--- |
| `tnupgrader.limit.30` | `op` | Tăng giới hạn lượt Đấu Giá mỗi ngày từ `15 lượt` lên **30 lượt/ngày** (Dành cho VIP 1). |
| `tnupgrader.limit.50` | `op` | Tăng giới hạn lượt Đấu Giá mỗi ngày lên **50 lượt/ngày** (Dành cho VIP 2 / MVP). |
| `tnupgrader.maxchance.85` | `op` | Tăng trần tỉ lệ thành công tối đa từ `75%` lên **85%** (Dành cho VIP). |

### D. Quyền Quản Trị & Bỏ Qua Giới Hạn (Admin / Bypass)
| Node Quyền Hạn (Permission) | Mặc định | Mô tả chi tiết |
| :--- | :---: | :--- |
| `tnupgrader.admin` | `op` | Quyền Quản Trị Viên tối cao (Bao gồm toàn bộ quyền bên dưới và các lệnh Admin). |
| `tnupgrader.bypass.limit` | `op` | Bỏ qua giới hạn số lượt Đấu Giá mỗi ngày (Quay không giới hạn). |
| `tnupgrader.bypass.cooldown` | `op` | Bỏ qua thời gian chờ (Cooldown `8 giây`) giữa các lần quay. |
| `tnupgrader.bypass.chance` | `op` | Cho phép đạt trần tỉ lệ tối đa `100%`. |
| `tnupgrader.bypass.ratio` | `op` | Bỏ qua kiểm tra tỉ lệ đơn giá tối thiểu (`15%`) và mở khóa toàn bộ hệ số nhân `x6 ▸ x10`. |

---

## 2. 🛡 Cơ Chế Chống Lợi Dụng Cày Đồ (Anti-Abuse System)

Để bảo vệ kinh tế máy chủ (đặc biệt là Skyblock / Tu Tiên / Survival):
1. **Khóa Trần Tỉ Lệ (`75%`) & Thuế Sàn (`0.80`):** Người chơi thường tối đa chỉ đạt `75%` tỉ lệ thắng và chịu `20%` hao hụt giá trị quy đổi, ngăn chặn tuyệt đối việc đổi ngang `100%` không rủi ro.
2. **Giới Hạn Nghiêm Ngặt `64` Vật Phẩm Cược (`ĐỒ CƯỢC 0/64`):** Tổng số lượng vật phẩm đặt vào 9 ô lưới 3x3 không bao giờ vượt quá `64 món`.
3. **Chặn Lấy Đồ Rẻ Cày Đồ Đắt (`min-unit-value-ratio: 0.15`):** Đơn giá của **mỗi món đồ đặt cược** bắt buộc phải đạt ít nhất **15%** đơn giá của vật phẩm mục tiêu (VD: Không thể đem Sắt/Đồng rẻ tiền đi quay Phôi Netherite hay Kiếm Thần Thoại).
4. **Giới Hạn Hệ Số Nhân (`x5`) & Số Lượng Đích (`x3`):** Chặn việc đặt 1 món rẻ tiền rồi chọn hệ số `x10` để cầu may 1% lấy đồ End-Game.
5. **Giới Hạn Lượt Quay Mỗi Ngày (`15 lượt/ngày`) & Cooldown (`8 giây`):** Ngăn chặn treo máy hoặc spam cày đồ liên tục.
6. **Loại Bỏ Đồ Rác Farm Tự Động:** Tre, Đá Cuội, Đất, Gỗ, Nông sản rẻ tiền có giá trị `0 V`, chỉ Khoáng sản tinh luyện và Đá Cường Hóa mới dùng để nâng cấp được.

---

## 3. 💻 Bảng Lệnh Chi Tiết (Commands)

**Lệnh chính:** `/upgrader`  
**Lệnh rút gọn (Aliases):** `/nangcap`, `/nc`, `/tnupgrader`, `/upgradeitems`

| Cú pháp Lệnh | Quyền yêu cầu | Công dụng |
| :--- | :--- | :--- |
| `/nangcap` *(hoặc `/upgrader open`)* | `tnupgrader.use` & `tnupgrader.mode.wager` | Mở giao diện chính **Nâng Cấp Vật Phẩm (Đấu Giá 3x3)**. |
| `/nangcap equip` *(hoặc `/nangcap cuonghoa`)* | `tnupgrader.use` & `tnupgrader.mode.equipment` | Mở trực tiếp **Bàn Cường Hóa Trang Bị & Cổ Ngữ**. |
| `/nangcap stats` *(hoặc `/nangcap thongke`)* | `tnupgrader.use` | Mở bảng **Thống Kê Cá Nhân & Bảng Xếp Hạng Top 5 Đại Gia Server**. |
| `/nangcap tool [tên] [số_lượng]` | `tnupgrader.admin` | Trao vật phẩm **⚙ Sổ Tay Nâng Cấp Di Động**. |
| `/nangcap block [tên] [số_lượng]` | `tnupgrader.admin` | Trao khối chức năng **⚒ Đài Nâng Cấp Tối Thượng**. |
| `/nangcap stone [tên] [số_lượng]` | `tnupgrader.admin` | Trao **💎 Đá Cường Hóa Tinh Anh** (`1,000 V`). |
| `/nangcap crystal [tên] [số_lượng]` | `tnupgrader.admin` | Trao **🔮 Tinh Thể Cổ Ngữ Thần Thoại** (`3,000 V`). |
| `/nangcap setvalue <giá_trị_V>` | `tnupgrader.admin` | Đặt nhanh điểm giá trị `V` cho vật phẩm đang cầm trên tay chính. |
| `/nangcap reload` | `tnupgrader.admin` | Tải lại `config.yml`, bảng giá `V`, công thức chế tạo và các hook. |
