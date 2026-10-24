# 🛡️ Chính Sách Bảo Mật (Security Policy) — TN-UpgraderItems

Tài liệu này mô tả chính sách bảo mật, các phiên bản được hỗ trợ cập nhật vá lỗi, các cơ chế **Chống Nhân Bản Vật Phẩm (Anti-Dupe)** tích hợp sẵn và quy trình báo cáo lỗ hổng bảo mật cho plugin **TN-UpgraderItems** (Tác giả: **Tài Nguyễn**).

---

## 📌 1. Các Phiên Bản Được Hỗ Trợ (Supported Versions)

Hiện tại các phiên bản dưới đây của **TN-UpgraderItems** đang nhận các bản cập nhật bảo mật và vá lỗi:

| Phiên bản Plugin | Phiên bản Server Hỗ Trợ | Java | Trạng thái Hỗ trợ |
| :--- | :--- | :--- | :--- |
| **`1.0.x`** | Paper / Purpur / Pufferfish `1.21` ▸ `1.21.11+` | Java 21+ | ✅ Đang được hỗ trợ tích cực |
| `< 1.0.0` | Các phiên bản cũ hơn `1.21` | Java 17 | ❌ Không hỗ trợ |

---

## 🔒 2. Các Cơ Chế Bảo Mật & Chống Dupe Đồ (Built-in Anti-Exploit)

Để đảm bảo an toàn tuyệt đối cho nền kinh tế của máy chủ Minecraft, **TN-UpgraderItems** đã được thiết kế với các lớp bảo vệ nghiêm ngặt:

1. **Chống Nhân Bản Khi Đang Quay (Spin Lock Anti-Dupe):**
   - Ngay tại thời điểm người chơi bấm nút **`NÂNG CẤP`** hoặc **`CƯỜNG HÓA NGAY`**, toàn bộ vật phẩm cược / nguyên liệu được trừ lập tức khỏi bộ nhớ phiên (`clearWagerWithoutReturn()`) trước khi hiệu ứng vòng quay bắt đầu.
   - Mọi thao tác click hoặc kéo thả trong GUI đều bị khóa hoàn toàn khi trạng thái đang là `SPINNING`.
2. **Bảo Vệ Khi Đóng GUI, Mất Kết Nối Hoặc Tắt Server:**
   - Khi người chơi đóng bảng (`InventoryCloseEvent`), thoát mạng (`PlayerQuitEvent`), hoặc khi máy chủ tắt/reload (`onDisable`), hệ thống tự động hủy tiến trình quay và hoàn trả an toàn các vật phẩm chưa quay về túi đồ (hoặc rơi xuống chân nếu túi đầy).
3. **Chặn Kéo Thả & Shift-Click Trái Phép (`InventoryDragEvent` & `InventoryClickEvent`):**
   - Toàn bộ sự kiện click/drag trên giao diện đều được kiểm soát ở mức ưu tiên `EventPriority.HIGHEST` (`setCancelled(true)`), ngăn chặn tuyệt đối việc lấy kính trang trí hoặc vật phẩm hiển thị trong kho `Tìm Phần Thưởng`.
4. **Xác Thực Dữ Liệu Bền Vững (`PersistentDataContainer` - PDC):**
   - Các dòng thuộc tính bổ sung trên trang bị, Khối Đài Nâng Cấp và Sổ Tay Nâng Cấp Di Động đều được định danh bằng `NamespacedKey` nội bộ của Paper 1.21+, không thể bị làm giả bằng cách đổi tên trên Đe (Anvil) thông thường.

---

## 🚨 3. Báo Cáo Lỗ Hổng Bảo Mật (Reporting a Vulnerability)

Nếu bạn phát hiện bất kỳ lỗi nhân bản vật phẩm (Dupe glitch), lỗi quyền hạn (Permission bypass), hoặc lỗi gây lag máy chủ (Crash/DoS):

1. **Vui lòng KHÔNG công khai lỗ hổng trên mục Issues công cộng** để tránh việc lỗi bị lợi dụng trên các máy chủ đang hoạt động.
2. Hãy báo cáo trực tiếp cho tác giả **Tài Nguyễn**:
   - **GitHub:** [@Jaojao00](https://github.com/Jaojao00)
   - **Repository Security Advisory:** Sử dụng tính năng *Report a vulnerability* trong tab **Security** của kho lưu trữ [TN-UpgraderItems](https://github.com/Jaojao00/TN-UpgraderItems/security).
3. **Thông tin cần cung cấp khi báo cáo:**
   - Phiên bản Paper/Purpur (`ver`) và phiên bản `TN-UpgraderItems`.
   - Các bước tái hiện lỗi chi tiết (kèm video hoặc log console nếu có).
   - Danh sách các plugin liên quan (VD: `MythicMobs`, `ExcellentEnchants`, `Geyser/Floodgate`).

Chúng tôi cam kết phản hồi trong vòng **24–48 giờ** và phát hành bản vá khẩn cấp ngay sau khi xác nhận lỗ hổng.
