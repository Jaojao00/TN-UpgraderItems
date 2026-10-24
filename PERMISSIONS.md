# 🔑 Bảng Quyền Hạn & Lệnh (Permissions & Commands) — TN-UpgraderItems

**Tác giả (Author):** Tài Nguyễn  
**Plugin:** `TN-UpgraderItems` (Paper 1.21+)

---

## 1. 📋 Bảng Quyền Hạn (Permissions)

| Node Quyền Hạn (Permission) | Mặc định | Mô tả chi tiết |
| :--- | :---: | :--- |
| `tnupgrader.use` | `true` (Mọi người chơi) | Cho phép người chơi mở và sử dụng giao diện **Nâng Cấp Vật Phẩm (Đấu Giá 3x3)**, **Bàn Cường Hóa Trang Bị End-Game**, **Tìm Phần Thưởng**, **Thống Kê**, **Cài Đặt**, đặt/sử dụng **Đài Nâng Cấp** và **Sổ Tay Nâng Cấp Di Động**. |
| `tnupgrader.command.open` | `true` (Mọi người chơi) | Cho phép mở trực tiếp giao diện nâng cấp bằng lệnh `/upgrader`, `/nangcap`, `/nc`, `/tnupgrader`. |
| `tnupgrader.admin` | `op` (Quản trị viên) | Quyền quản trị tối cao: bao gồm toàn bộ quyền người chơi và cho phép dùng các lệnh `/upgrader tool`, `/upgrader block`, `/upgrader setvalue`, `/upgrader reload`, đồng thời bỏ qua giới hạn bắt buộc đứng gần Đài Nâng Cấp. |

---

## 2. 💻 Bảng Lệnh Chi Tiết (Commands)

**Lệnh chính:** `/upgrader`  
**Lệnh rút gọn (Aliases):** `/nangcap`, `/nc`, `/tnupgrader`, `/upgradeitems`

| Cú pháp Lệnh | Quyền yêu cầu | Công dụng |
| :--- | :--- | :--- |
| `/nangcap` *(hoặc `/upgrader open`)* | `tnupgrader.use` | Mở giao diện chính **Nâng Cấp Vật Phẩm (Đấu Giá 3x3)**. |
| `/nangcap equip` *(hoặc `/nangcap cuonghoa`)* | `tnupgrader.use` | Mở trực tiếp **Bàn Cường Hóa Trang Bị End-Game** (Thuộc tính & Phù phép). |
| `/nangcap stats` *(hoặc `/nangcap thongke`)* | `tnupgrader.use` | Mở bảng **Thống Kê Cá Nhân & Bảng Xếp Hạng Top 5 Đại Gia Server**. |
| `/nangcap tool [tên_người_chơi] [số_lượng]` | `tnupgrader.admin` | Trao vật phẩm **⚙ Sổ Tay Nâng Cấp Di Động** cho bản thân hoặc người chơi khác. |
| `/nangcap block [tên_người_chơi] [số_lượng]` | `tnupgrader.admin` | Trao khối chức năng **⚒ Đài Nâng Cấp Tối Thượng** cho bản thân hoặc người chơi khác. |
| `/nangcap setvalue <giá_trị_V>` | `tnupgrader.admin` | Đặt nhanh điểm giá trị `V` cho vật phẩm đang cầm trên tay chính và lưu thẳng vào `config.yml`. |
| `/nangcap reload` | `tnupgrader.admin` | Tải lại `config.yml`, bảng giá `V`, công thức chế tạo và các hook (`MythicMobs`, `ExcellentEnchants`, `Floodgate`). |
