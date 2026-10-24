package vn.tainguyen.tnupgrader.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import vn.tainguyen.tnupgrader.TNUpgraderPlugin;
import vn.tainguyen.tnupgrader.model.PlayerProfile;
import vn.tainguyen.tnupgrader.model.RewardItem;
import vn.tainguyen.tnupgrader.model.UiTheme;
import vn.tainguyen.tnupgrader.util.ItemBuilder;
import vn.tainguyen.tnupgrader.util.TextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MainUpgraderGui {

    // 9 ô lưới ĐỒ CƯỢC 3x3 bên trái
    public static final int[] WAGER_SLOTS = {
            9, 10, 11,
            18, 19, 20,
            27, 28, 29
    };

    // 8 ô vòng tròn quay theo chiều kim đồng hồ bắt đầu từ đỉnh 12h (Slot 4)
    public static final int[] WHEEL_RING_SLOTS = {
            4, 5, 14, 23, 22, 21, 12, 3
    };

    public static final int WHEEL_CENTER_SLOT = 13;
    public static final int TARGET_ITEM_SLOT = 16;
    public static final int UPGRADE_BUTTON_SLOT = 25;
    public static final int TARGET_QTY_SLOT = 26;
    public static final int FAVORITE_BUTTON_SLOT = 24;

    // Hàng hệ số nhân x1 -> x10 (Slots 36..44)
    public static final int[] MULTIPLIER_VALUES = {1, 2, 3, 4, 5, 6, 7, 8, 10};

    private final TNUpgraderPlugin plugin;

    public MainUpgraderGui(TNUpgraderPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, UpgraderSession session) {
        session.setTransitioning(true);
        session.setCurrentMenu(UpgraderSession.MenuType.MAIN_UPGRADER);

        PlayerProfile profile = plugin.getProfileManager().getProfile(player);
        UiTheme theme = profile.getTheme();

        Component title = TextUtil.color("&8&l✦ " + theme.getAccentColor() + "&lNÂNG CẤP VẬT PHẨM &8&l✦");
        Inventory inv = Bukkit.createInventory(null, 54, title);
        session.setActiveInventory(inv);

        render(player, session, inv);
        player.openInventory(inv);
        session.setTransitioning(false);
    }

    public void render(Player player, UpgraderSession session, Inventory inv) {
        PlayerProfile profile = plugin.getProfileManager().getProfile(player);
        UiTheme theme = profile.getTheme();
        double wagerV = session.getTotalWagerValue();
        int wagerCount = session.getTotalWagerItemCount();
        double invV = plugin.getConfigManager().getPlayerInventoryValue(player);
        boolean isPe = plugin.getFloodgateHook().isBedrockPlayer(player);

        // Nền mặc định
        ItemStack bg = new ItemBuilder(theme.getBackgroundGlass()).name(" ").hideFlags().build();
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, bg);
        }

        // --- HÀNG 1 (HEADER & TIỆN ÍCH NHANH) ---
        int maxWagerItems = plugin.getConfigManager().getMaxWagerItems();
        int dailyUsed = profile.getDailyAttemptsUsed();
        int dailyLimit = plugin.getConfigManager().getPlayerDailyLimit(player);
        double playerMaxChance = plugin.getConfigManager().getPlayerMaxSuccessChance(player);
        double chanceMult = plugin.getConfigManager().getChanceMultiplier();
        double minRatio = plugin.getConfigManager().getMinUnitValueRatio();
        int maxMult = plugin.getConfigManager().getMaxUpgradeMultiplier();
        int maxQty = plugin.getConfigManager().getMaxTargetQuantity();
        double lowestUnitV = session.getLowestWagerUnitValue();

        // Slot 0: ĐỒ CƯỢC X/64 (Hiển thị đầy đủ Hạn chế Chống Cày Đồ & Quyền Hạn)
        inv.setItem(0, new ItemBuilder(Material.CHEST)
                .name("&e&lĐỒ CƯỢC &f" + wagerCount + "/" + maxWagerItems)
                .lore(
                        "&7Tổng giá trị đang cược: &6" + TextUtil.formatValue(wagerV) + " V",
                        "&7Đơn giá món thấp nhất: &e" + TextUtil.formatValue(lowestUnitV) + " V / món",
                        "",
                        "&c&l🛡 CƠ CHẾ CHỐNG CÀY ĐỒ & GIỚI HẠN:",
                        "&f• &7Số lượng cược tối đa: &e" + wagerCount + "/" + maxWagerItems + " vật phẩm",
                        "&f• &7Lượt Đấu Giá hôm nay: &e" + dailyUsed + "/" + (dailyLimit >= 999999 ? "∞" : dailyLimit) + " lượt",
                        "&f• &7Trần tỉ lệ tối đa của bạn: &a" + TextUtil.formatPercent(playerMaxChance) + "% &8(Hệ số x" + TextUtil.formatPercent(chanceMult) + ")",
                        "&f• &7Yêu cầu bậc nguyên liệu: &fĐơn giá món cược &e≥ " + Math.round(minRatio * 100) + "% &fđơn giá đích",
                        "&f• &7Hệ số nâng cấp tối đa: &ex" + maxMult + " &8| &7Số lượng đích tối đa: &ex" + maxQty,
                        "",
                        "&e▸ Cách đặt đồ cược (PC & PE):",
                        "&f  Chạm/Click trực tiếp vào vật phẩm trong &6Túi Đồ",
                        "&f  bên dưới để đưa lên lưới 3x3 (Hoặc chạm ô 3x3 để rút về)."
                )
                .build());

        // Slot 1: Màu Giao diện
        inv.setItem(1, new ItemBuilder(theme.getAccentGlass())
                .name(theme.getAccentColor() + "&l🎨 Màu Giao diện")
                .lore(
                        "&7Chủ đề hiện tại: " + theme.getAccentColor() + theme.getDisplayName(),
                        "",
                        "&e▸ Chạm để đổi màu giao diện!"
                )
                .build());

        // Slot 2: Hướng dẫn
        inv.setItem(2, new ItemBuilder(Material.KNOWLEDGE_BOOK)
                .name("&b&l📖 Hướng dẫn")
                .lore(
                        "&7Xem chi tiết cơ chế tính điểm &6V&7,",
                        "&7vòng quay tỷ lệ may rủi và chế độ",
                        "&7Cường Hóa Trang Bị End-Game.",
                        "",
                        "&e▸ Chạm để xem hướng dẫn!"
                )
                .build());

        // Slot 6: Danh sách Yêu thích
        inv.setItem(6, new ItemBuilder(Material.NETHER_STAR)
                .name("&d&l★ Yêu thích &7(" + profile.getFavoriteItemIds().size() + ")")
                .lore(
                        "&7Mở nhanh danh sách các phần thưởng",
                        "&7bạn đã đánh dấu yêu thích.",
                        "",
                        "&e▸ Chạm để mở danh sách Yêu thích!"
                )
                .build());

        // Slot 7: Chuyển sang Chế Độ Cường Hóa Trang Bị End-Game
        inv.setItem(7, new ItemBuilder(Material.NETHERITE_SWORD)
                .name("&6&l⚔ CƯỜNG HÓA TRANG BỊ & CỔ NGỮ &8[Độc Quyền]")
                .lore(
                        "&7Chuyển sang bàn &eCường Hóa Công Cụ, Vũ Khí & Giáp",
                        "&7với Hệ Sinh Thái Độc Quyền của &6TN-UpgraderItems&7:",
                        "",
                        "&f• &6Thuộc Tính Cường Hóa: &7Đào nhanh, Máu, Sát thương...",
                        "&f• &dPhù Phép Vượt Cấp: &7Hiệu Suất, Chậm Hỏng, Gia Tài...",
                        "&f• &b12 Cổ Ngữ Độc Quyền: &7Đào Mạch Quặng, Đào 3x3,",
                        "&7  Nung Quặng Tự Động, Từ Trường Hút Đồ, Sấm Sét...",
                        "",
                        "&a▸ Chạm để chuyển sang Bàn Cường Hóa Trang Bị!"
                )
                .glow(true)
                .hideFlags()
                .build());

        // Slot 8: Nút Đóng [X]
        inv.setItem(8, new ItemBuilder(Material.BARRIER)
                .name("&c&l✕ Đóng giao diện")
                .lore("&7Đồ đang cược sẽ tự động trả về túi đồ.")
                .build());

        // --- LƯỚI ĐỒ CƯỢC 3x3 BÊN TRÁI ---
        ItemStack[] wagers = session.getWagerItems();
        for (int i = 0; i < WAGER_SLOTS.length; i++) {
            int slot = WAGER_SLOTS[i];
            ItemStack item = wagers[i];
            if (item != null && !item.getType().isAir()) {
                double stackV = plugin.getConfigManager().getStackValue(item);
                ItemStack display = new ItemBuilder(item.clone())
                        .addLore(
                                "",
                                "&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                                "&7Giá trị cược: &6" + TextUtil.formatValue(stackV) + " V",
                                "&c▸ Chạm để rút về túi đồ"
                        )
                        .build();
                inv.setItem(slot, display);
            } else {
                inv.setItem(slot, new ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                        .name("&7Ô Đồ Cược #" + (i + 1))
                        .lore(
                                "&8Trống",
                                "&7Chạm vào vật phẩm dưới túi đồ",
                                "&7để đặt vào ô cược này."
                        )
                        .build());
            }
        }

        // --- VÒNG QUAY TỶ LỆ TRUNG TÂM (8 Ô VÒNG TRÒN + TÂM SLOT 13) ---
        renderWheel(session, inv);

        // --- KHU VỰC VẬT PHẨM ĐÍCH BÊN PHẢI ---
        inv.setItem(15, new ItemBuilder(theme.getAccentGlass()).name(" ").build());
        inv.setItem(17, new ItemBuilder(theme.getAccentGlass()).name(" ").build());

        RewardItem target = session.getTargetReward();
        int qty = session.getTargetQuantity();
        double chance = session.getCurrentSuccessChance();

        if (target == null) {
            inv.setItem(TARGET_ITEM_SLOT, new ItemBuilder(Material.SPYGLASS)
                    .name("&e&lChọn vật phẩm")
                    .lore(
                            "&7Chạm để tìm vật phẩm mục tiêu",
                            "&7hoặc bấm các nút &ex1 ▸ x10 &7bên dưới!",
                            "",
                            "&6▸ Chạm để mở Tìm Phần Thưởng"
                    )
                    .glow(true)
                    .build());
        } else {
            double totalTargetV = target.getTotalValue(qty);
            boolean isFav = profile.isFavorite(target.getId());
            ItemStack targetDisplay = new ItemBuilder(target.createItemStack(Math.min(64, qty)))
                    .name("&f&l" + target.getDisplayName() + (isFav ? " &e★" : ""))
                    .lore(
                            "&6" + TextUtil.formatValue(totalTargetV) + " V &8• &fx" + qty,
                            "",
                            "&7Đơn giá: &e" + TextUtil.formatValue(target.getUnitValue()) + " V &7/ món",
                            "&7Đồ cược hiện tại: &e" + TextUtil.formatValue(wagerV) + " V",
                            "&7Tỉ lệ thành công: &a&l" + TextUtil.formatPercent(chance) + "%",
                            "",
                            "&e▸ Chạm để đổi vật phẩm mục tiêu khác"
                    )
                    .glow(true)
                    .build();
            inv.setItem(TARGET_ITEM_SLOT, targetDisplay);
        }

        // Slot 24: Đánh dấu Yêu thích món đang chọn
        boolean currentFav = target != null && profile.isFavorite(target.getId());
        inv.setItem(FAVORITE_BUTTON_SLOT, new ItemBuilder(currentFav ? Material.GOLD_INGOT : Material.IRON_NUGGET)
                .name(currentFav ? "&e&l★ Đã Yêu Thích" : "&7☆ Thêm vào Yêu thích")
                .lore(
                        target != null
                                ? "&7Mục tiêu: &f" + target.getDisplayName()
                                : "&7Hãy chọn vật phẩm mục tiêu trước.",
                        "",
                        "&e▸ Chạm để bật/tắt Yêu thích!"
                )
                .glow(currentFav)
                .build());

        // Slot 25: Nút NÂNG CẤP / TIẾP TỤC (Chuẩn màu vàng ảnh mẫu)
        if (session.getSpinState() == UpgraderSession.SpinState.SPINNING) {
            inv.setItem(UPGRADE_BUTTON_SLOT, new ItemBuilder(Material.CLOCK)
                    .name("&e&l⏳ ĐANG QUAY...")
                    .lore("&7Vòng quay may mắn đang quyết định số phận!")
                    .glow(true)
                    .build());
        } else if (session.getSpinState() == UpgraderSession.SpinState.RESULT_WIN
                || session.getSpinState() == UpgraderSession.SpinState.RESULT_LOSS) {
            inv.setItem(UPGRADE_BUTTON_SLOT, new ItemBuilder(Material.GOLD_BLOCK)
                    .name("&6&lTIẾP TỤC")
                    .lore(
                            session.getSpinState() == UpgraderSession.SpinState.RESULT_WIN
                                    ? "&a&l✔ NÂNG CẤP THÀNH CÔNG!"
                                    : "&c&l✘ NÂNG CẤP THẤT BẠI (" + TextUtil.formatPercent(session.getLastRolledNumber()) + ")",
                            "",
                            "&e▸ Chạm để tiếp tục lượt nâng cấp mới!"
                    )
                    .glow(true)
                    .build());
        } else {
            String blockReason = getSpinBlockReason(player, session, profile);
            boolean ready = (blockReason == null);
            inv.setItem(UPGRADE_BUTTON_SLOT, new ItemBuilder(ready ? Material.GOLD_BLOCK : Material.GOLDEN_SWORD)
                    .name(ready ? "&6&l⚡ NÂNG CẤP ⚡" : "&c&lNÂNG CẤP &7(Chưa thỏa điều kiện)")
                    .lore(
                            "&7Đồ cược: &6" + TextUtil.formatValue(wagerV) + " V &8(" + wagerCount + "/" + maxWagerItems + " món)",
                            "&7Mục tiêu: &f" + (target != null ? target.getDisplayName() + " x" + qty + " (" + TextUtil.formatValue(target.getTotalValue(qty)) + " V)" : "Chưa chọn"),
                            "&7Tỉ lệ thành công: &a&l" + TextUtil.formatPercent(chance) + "% &8(Tối đa " + TextUtil.formatPercent(playerMaxChance) + "%)",
                            "&7Lượt hôm nay: &e" + dailyUsed + "/" + (dailyLimit >= 999999 ? "∞" : dailyLimit) + " lượt",
                            "",
                            ready ? "&6&l▸ CHẠM ĐỂ QUAY NÂNG CẤP NGAY!" : "&c✘ " + blockReason
                    )
                    .glow(ready)
                    .hideFlags()
                    .build());
        }

        // Slot 26: Số lượng đích: X
        inv.setItem(TARGET_QTY_SLOT, new ItemBuilder(Material.PAPER, Math.min(64, qty))
                .name("&fSố lượng đích: &e" + qty + "&7/" + maxQty)
                .lore(
                        "&7Nhân số lượng vật phẩm mục tiêu nhận được",
                        "&7(Giới hạn tối đa &ex" + maxQty + " &7mỗi lượt để chống cày đồ).",
                        "",
                        "&a▸ Chuột trái / Chạm: &f+1 số lượng",
                        "&c▸ Chuột phải: &f-1 số lượng",
                        "&e▸ Shift + Click: &fĐặt lại về 1"
                )
                .build());

        // --- HÀNG 5 (SLOTS 36..44): HỆ SỐ NHÂN NHANH x1 ▸ x10 ---
        boolean bypassRatio = player.hasPermission("tnupgrader.bypass.ratio") || player.hasPermission("tnupgrader.admin");
        for (int i = 0; i < MULTIPLIER_VALUES.length; i++) {
            int mult = MULTIPLIER_VALUES[i];
            int slot = 36 + i;
            boolean lockedMult = !bypassRatio && mult > maxMult;
            boolean selected = session.getSelectedMultiplier() == mult && !lockedMult;
            double targetValForMult = wagerV > 0 ? (wagerV * mult) : (1000.0 * mult);
            double approxChance = Math.min(playerMaxChance, (100.0 / mult) * chanceMult);

            if (lockedMult) {
                inv.setItem(slot, new ItemBuilder(Material.RED_STAINED_GLASS_PANE, mult)
                        .name("&c&l🔒 x" + mult + " &8(Khóa)")
                        .lore(
                                "&7Hệ số nhân vượt quá giới hạn &ex" + maxMult,
                                "&7đã bị khóa để chống lợi dụng cày đồ!",
                                "",
                                "&8Yêu cầu quyền: tnupgrader.bypass.ratio"
                        )
                        .build());
            } else {
                inv.setItem(slot, new ItemBuilder(selected ? Material.YELLOW_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE, mult)
                        .name((selected ? "&e&l" : "&f") + "x" + mult)
                        .lore(
                                "&7Tự động chọn phần thưởng hợp lệ có giá trị",
                                "&7gấp &ex" + mult + " lần &7tổng đồ cược!",
                                "",
                                "&7Giá trị đích ước tính: &6~" + TextUtil.formatValue(targetValForMult) + " V",
                                "&7Tỉ lệ trúng tương ứng: &a~" + TextUtil.formatPercent(approxChance) + "%",
                                "",
                                "&e▸ Chạm để chọn nhanh hệ số x" + mult
                        )
                        .glow(selected)
                        .build());
            }
        }

        // --- HÀNG 6 (SLOTS 45..53): THANH ĐIỀU HƯỚNG CHÍNH ---
        // Slot 45, 46: Tìm vật phẩm
        ItemStack searchBtn = new ItemBuilder(Material.COMPASS)
                .name("&b&l🔍 Tìm vật phẩm")
                .lore(
                        "&7Mở kho &fTìm Phần Thưởng &7với bộ lọc",
                        "&7danh mục, tìm kiếm tên và sắp xếp giá.",
                        "",
                        "&e▸ Chạm để mở kho phần thưởng!"
                )
                .glow(true)
                .build();
        inv.setItem(45, searchBtn);
        inv.setItem(46, searchBtn);

        // Slot 47, 48: Bỏ cược
        ItemStack clearBetBtn = new ItemBuilder(Material.HOPPER)
                .name("&c&l↩ Bỏ cược")
                .lore(
                        "&7Trả toàn bộ vật phẩm đang đặt trong",
                        "&7lưới 3x3 (&6" + TextUtil.formatValue(wagerV) + " V&7) về lại túi đồ.",
                        "",
                        "&e▸ Chạm để thu hồi đồ cược!"
                )
                .build();
        inv.setItem(47, clearBetBtn);
        inv.setItem(48, clearBetBtn);

        // Slot 49: TÚI ĐỒ - XXX V
        inv.setItem(49, new ItemBuilder(Material.BUNDLE)
                .name("&f&lTÚI ĐỒ - &6&l" + TextUtil.formatValue(wagerV) + " V")
                .lore(
                        "&7Tổng giá trị đang cược: &6" + TextUtil.formatValue(wagerV) + " V",
                        "&7Tổng giá trị trong túi đồ: &e" + TextUtil.formatValue(invV) + " V",
                        "&7Thiết bị: " + (isPe ? "&aBedrock / PE (Tối ưu 1-chạm)" : "&bJava / PC"),
                        "",
                        "&a▸ Chạm vào đây để tự động nạp nhanh",
                        "&a  1 món đồ có giá trị từ túi lên ô cược!"
                )
                .build());

        // Slot 50: Cài đặt
        inv.setItem(50, new ItemBuilder(Material.COMPARATOR)
                .name("&e&l⚙ Cài đặt")
                .lore(
                        "&7Hiệu ứng vòng quay: " + (profile.isSpinAnimation() ? "&aBật (Hồi hộp)" : "&eTắt (Quay siêu tốc)"),
                        "&7Âm thanh: " + (profile.isSoundEnabled() ? "&aBật" : "&cTắt"),
                        "&7Thông báo toàn server: " + (profile.isBroadcastEnabled() ? "&aBật" : "&cTắt"),
                        "",
                        "&e▸ Chạm để mở Cài đặt!"
                )
                .build());

        // Slot 51: Thống kê
        inv.setItem(51, new ItemBuilder(Material.BOOK)
                .name("&a&l📊 Thống kê")
                .lore(
                        "&7Tổng lượt nâng cấp: &f" + profile.getTotalAttempts(),
                        "&7Thành công: &a" + profile.getTotalWins() + " &8| &7Thất bại: &c" + profile.getTotalLosses(),
                        "&7Tỉ lệ thắng thực tế: &e" + TextUtil.formatPercent(profile.getWinRate()) + "%",
                        "&7Chuỗi thắng kỷ lục: &6🔥 " + profile.getBestStreak(),
                        "",
                        "&e▸ Chạm để xem chi tiết & Top Server!"
                )
                .build());

        // Slot 52, 53: Nút chuyển nhanh sang Cường Hóa Trang Bị End-Game
        ItemStack equipModeBtn = new ItemBuilder(Material.ANVIL)
                .name("&6&l⚒ BÀN CƯỜNG HÓA & CỔ NGỮ ĐỘC QUYỀN")
                .lore(
                        "&7Nâng cấp Công cụ, Vũ khí, Giáp trụ với:",
                        "&e• Thuộc tính Đào Nhanh, Sát Thương, Hút Máu",
                        "&d• Phù phép Hiệu Suất, Chậm Hỏng, Gia Tài",
                        "&b• 12 Cổ Ngữ Độc Quyền (Đào Mạch Quặng, 3x3...)",
                        "",
                        "&6▸ Chạm để mở Bàn Cường Hóa Trang Bị!"
                )
                .glow(true)
                .build();
        inv.setItem(52, equipModeBtn);
        inv.setItem(53, equipModeBtn);
    }

    /**
     * Vẽ Vòng Tròn Tỷ Lệ 8 Ô + Tâm Đồng Hồ (Slot 13) chuẩn theo trạng thái
     */
    public void renderWheel(UpgraderSession session, Inventory inv) {
        double wagerV = session.getTotalWagerValue();
        double chance = session.getCurrentSuccessChance();
        UpgraderSession.SpinState state = session.getSpinState();

        double displayChance = (state == UpgraderSession.SpinState.IDLE) ? chance : session.getLockedSuccessChance();
        int activeYellowSegments = (int) Math.round((displayChance / 100.0) * WHEEL_RING_SLOTS.length);
        if (displayChance > 0 && activeYellowSegments == 0) {
            activeYellowSegments = 1;
        }

        for (int i = 0; i < WHEEL_RING_SLOTS.length; i++) {
            int slot = WHEEL_RING_SLOTS[i];
            boolean isWinningArc = i < activeYellowSegments;
            boolean isPointerHere = (state != UpgraderSession.SpinState.IDLE) && (session.getCurrentWheelIndex() == i);

            Material paneMat;
            String segName;
            if (isPointerHere) {
                if (state == UpgraderSession.SpinState.RESULT_WIN) {
                    paneMat = Material.LIME_STAINED_GLASS_PANE;
                    segName = "&a&l▼ KIM CHỈ THỊ (THÀNH CÔNG!) ▼";
                } else if (state == UpgraderSession.SpinState.RESULT_LOSS) {
                    paneMat = Material.RED_STAINED_GLASS_PANE;
                    segName = "&c&l▼ KIM CHỈ THỊ (THẤT BẠI!) ▼";
                } else {
                    paneMat = Material.WHITE_STAINED_GLASS_PANE;
                    segName = "&e&l▼ KIM QUAY ĐANG CHẠY ▼";
                }
            } else if (isWinningArc) {
                paneMat = Material.YELLOW_STAINED_GLASS_PANE;
                segName = "&eVùng Thành Công (" + TextUtil.formatPercent(displayChance) + "%)";
            } else {
                paneMat = Material.GRAY_STAINED_GLASS_PANE;
                segName = "&8Vùng Thất Bại";
            }

            inv.setItem(slot, new ItemBuilder(paneMat)
                    .name(segName)
                    .lore(
                            "&7Tỉ lệ thành công: &e" + TextUtil.formatPercent(displayChance) + "%",
                            isWinningArc ? "&a✔ Kim dừng ở vùng màu vàng = THẮNG" : "&c✘ Kim dừng ở vùng màu xám = THUA"
                    )
                    .glow(isPointerHere)
                    .build());
        }

        // Tâm Vòng Quay (Slot 13) - Giống hệt chữ hiển thị giữa vòng tròn trong 9 ảnh
        if (state == UpgraderSession.SpinState.SPINNING) {
            inv.setItem(WHEEL_CENTER_SLOT, new ItemBuilder(Material.RECOVERY_COMPASS)
                    .name("&a&l" + TextUtil.formatPercent(displayChance))
                    .lore(
                            "&e&lĐANG QUAY",
                            "",
                            "&7Hãy chờ kim chỉ thị dừng lại..."
                    )
                    .glow(true)
                    .build());
        } else if (state == UpgraderSession.SpinState.RESULT_WIN) {
            inv.setItem(WHEEL_CENTER_SLOT, new ItemBuilder(Material.NETHER_STAR)
                    .name("&a&l" + TextUtil.formatPercent(session.getLastRolledNumber()))
                    .lore(
                            "&a&lTHÀNH CÔNG!",
                            "",
                            "&7Điểm quay được: &a" + TextUtil.formatPercent(session.getLastRolledNumber())
                                    + " &7(Yêu cầu ≤ &e" + TextUtil.formatPercent(displayChance) + "&7)",
                            "&e▸ Bấm nút TIẾP TỤC bên phải!"
                    )
                    .glow(true)
                    .build());
        } else if (state == UpgraderSession.SpinState.RESULT_LOSS) {
            inv.setItem(WHEEL_CENTER_SLOT, new ItemBuilder(Material.REDSTONE_BLOCK)
                    .name("&a&l" + TextUtil.formatPercent(session.getLastRolledNumber()))
                    .lore(
                            "&c&lTHẤT BẠI",
                            "",
                            "&7Điểm quay được: &c" + TextUtil.formatPercent(session.getLastRolledNumber())
                                    + " &7(Yêu cầu ≤ &e" + TextUtil.formatPercent(displayChance) + "&7)",
                            "&e▸ Bấm nút TIẾP TỤC bên phải!"
                    )
                    .build());
        } else {
            if (wagerV <= 0.0 || session.getTargetReward() == null) {
                inv.setItem(WHEEL_CENTER_SLOT, new ItemBuilder(Material.CLOCK)
                        .name("&e&l0.00")
                        .lore(
                                "&7Chọn đồ cược",
                                "",
                                "&8Đặt vật phẩm vào lưới 3x3 bên trái",
                                "&8và chọn phần thưởng mục tiêu bên phải."
                        )
                        .build());
            } else {
                inv.setItem(WHEEL_CENTER_SLOT, new ItemBuilder(Material.CLOCK)
                        .name("&a&l" + TextUtil.formatPercent(chance))
                        .lore(
                                "&7Tỉ lệ thành công",
                                "",
                                "&7Tổng cược: &6" + TextUtil.formatValue(wagerV) + " V",
                                "&7Giá trị đích: &6" + TextUtil.formatValue(session.getTargetReward().getTotalValue(session.getTargetQuantity())) + " V"
                        )
                        .glow(true)
                        .build());
            }
        }
    }

    /**
     * Kiểm tra toàn bộ điều kiện chống lợi dụng cày đồ & quyền hạn trước khi cho phép quay
     */
    public String getSpinBlockReason(Player player, UpgraderSession session, PlayerProfile profile) {
        if (!player.hasPermission("tnupgrader.mode.wager") && !player.hasPermission("tnupgrader.admin")) {
            return "Bạn không có quyền sử dụng bàn Đấu Giá (tnupgrader.mode.wager)!";
        }
        RewardItem target = session.getTargetReward();
        double wagerV = session.getTotalWagerValue();
        if (target == null || wagerV <= 0.0) {
            return "Hãy đặt đồ cược (3x3) và chọn vật phẩm đích!";
        }
        int dailyLimit = plugin.getConfigManager().getPlayerDailyLimit(player);
        if (profile.getDailyAttemptsUsed() >= dailyLimit) {
            return "Bạn đã hết lượt Đấu Giá hôm nay (" + dailyLimit + "/" + dailyLimit + " lượt)!";
        }
        if (!plugin.getConfigManager().canPlayerSelectRewardTier(player, target)) {
            return "Cần quyền " + plugin.getConfigManager().getRequiredTierPermissionLabel(target) + "&c!";
        }
        boolean bypassRatio = player.hasPermission("tnupgrader.bypass.ratio") || player.hasPermission("tnupgrader.admin");
        if (!bypassRatio) {
            double minRatio = plugin.getConfigManager().getMinUnitValueRatio();
            double reqMinUnitV = target.getUnitValue() * minRatio;
            double lowestUnitV = session.getLowestWagerUnitValue();
            if (lowestUnitV < reqMinUnitV) {
                return "Đơn giá mỗi món cược phải ≥ " + TextUtil.formatValue(reqMinUnitV)
                        + " V (" + Math.round(minRatio * 100) + "% giá đích) để chống lấy đồ rác cày đồ xịn!";
            }
            int maxMult = plugin.getConfigManager().getMaxUpgradeMultiplier();
            double totalTargetV = target.getTotalValue(session.getTargetQuantity());
            double minRequiredTotalWager = totalTargetV / Math.max(1, maxMult);
            if (wagerV + 0.001 < minRequiredTotalWager) {
                return "Tổng cược tối thiểu phải đạt " + TextUtil.formatValue(minRequiredTotalWager)
                        + " V (Tối đa nhân x" + maxMult + " giá trị cược)!";
            }
        }
        return null;
    }

    /**
     * Bắt đầu quay nâng cấp vật phẩm với hiệu ứng hồi hộp như trong ảnh 5 & 6
     */
    public void startUpgradeSpin(Player player, UpgraderSession session) {
        if (session.getSpinState() == UpgraderSession.SpinState.SPINNING) {
            return;
        }
        PlayerProfile profile = plugin.getProfileManager().getProfile(player);
        String blockReason = getSpinBlockReason(player, session, profile);
        if (blockReason != null) {
            player.sendMessage(TextUtil.color(plugin.getConfigManager().getPrefix() + "&c" + blockReason));
            session.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Kiểm tra Cooldown chống spam
        if (!player.hasPermission("tnupgrader.bypass.cooldown") && !player.hasPermission("tnupgrader.admin")) {
            int cdSec = plugin.getConfigManager().getCooldownSeconds();
            if (cdSec > 0) {
                long elapsedMs = System.currentTimeMillis() - profile.getLastSpinTimeMs();
                long remainMs = (cdSec * 1000L) - elapsedMs;
                if (remainMs > 0) {
                    double remainSec = Math.ceil(remainMs / 100.0) / 10.0;
                    player.sendMessage(TextUtil.color(plugin.getConfigManager().getPrefix()
                            + "&eVui lòng chờ &c" + remainSec + " giây &enữa trước khi quay lượt tiếp theo!"));
                    session.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }
            }
        }

        RewardItem target = session.getTargetReward();
        double wagerV = session.getTotalWagerValue();
        int qty = session.getTargetQuantity();
        double chance = session.getCurrentSuccessChance();
        double rolled = ThreadLocalRandom.current().nextDouble(0.0, 100.0);
        boolean win = rolled <= chance && chance > 0.0;

        // Trừ đồ cược & ghi nhận lượt dùng trong ngày + thời gian cooldown ngay khi bắt đầu quay
        session.clearWagerWithoutReturn();
        profile.incrementDailyAttempts();
        profile.setLastSpinTimeMs(System.currentTimeMillis());
        session.setLockedSuccessChance(chance);
        session.setLastRolledNumber(rolled);

        // Tính vị trí dừng trên vòng tròn 8 ô tương ứng với con số rolled
        int activeYellowSegments = (int) Math.round((chance / 100.0) * WHEEL_RING_SLOTS.length);
        if (chance > 0 && activeYellowSegments == 0) activeYellowSegments = 1;

        int landingSlotIndex;
        if (win) {
            int maxWinIdx = Math.max(0, activeYellowSegments - 1);
            landingSlotIndex = (int) Math.min(maxWinIdx, Math.floor((rolled / Math.max(0.01, chance)) * activeYellowSegments));
        } else {
            int startLossIdx = Math.min(WHEEL_RING_SLOTS.length - 1, activeYellowSegments);
            int lossSpan = Math.max(1, WHEEL_RING_SLOTS.length - startLossIdx);
            double lossProgress = (rolled - chance) / Math.max(0.01, 100.0 - chance);
            landingSlotIndex = Math.min(WHEEL_RING_SLOTS.length - 1, startLossIdx + (int) Math.floor(lossProgress * lossSpan));
        }

        if (!profile.isSpinAnimation()) {
            session.setCurrentWheelIndex(landingSlotIndex);
            finishUpgradeSpin(player, session, target, qty, wagerV, chance, rolled, win);
            return;
        }

        session.setSpinState(UpgraderSession.SpinState.SPINNING);
        session.setCurrentWheelIndex(0);
        if (session.getActiveInventory() != null) {
            render(player, session, session.getActiveInventory());
        }

        final int totalSteps = (WHEEL_RING_SLOTS.length * 2) + landingSlotIndex;
        BukkitRunnable runnable = new BukkitRunnable() {
            int step = 0;
            int waitTicks = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    session.setSpinTask(null);
                    finishUpgradeSpin(player, session, target, qty, wagerV, chance, rolled, win);
                    return;
                }
                if (waitTicks > 0) {
                    waitTicks--;
                    return;
                }
                session.setCurrentWheelIndex(step % WHEEL_RING_SLOTS.length);
                float pitch = 0.8f + (1.0f * step / Math.max(1, totalSteps));
                session.playSound(player, Sound.BLOCK_NOTE_BLOCK_HAT, 0.8f, Math.min(2.0f, pitch));

                if (session.getCurrentMenu() == UpgraderSession.MenuType.MAIN_UPGRADER && session.getActiveInventory() != null) {
                    renderWheel(session, session.getActiveInventory());
                }

                if (step >= totalSteps) {
                    cancel();
                    session.setSpinTask(null);
                    finishUpgradeSpin(player, session, target, qty, wagerV, chance, rolled, win);
                    return;
                }

                step++;
                int remaining = totalSteps - step;
                if (remaining <= 2) waitTicks = 4;
                else if (remaining <= 5) waitTicks = 2;
                else if (remaining <= 9) waitTicks = 1;
                else waitTicks = 0;
            }
        };
        session.setSpinTask(runnable.runTaskTimer(plugin, 2L, 2L));
    }

    private void finishUpgradeSpin(Player player, UpgraderSession session, RewardItem target, int qty,
                                   double wagerV, double chance, double rolled, boolean win) {
        PlayerProfile profile = plugin.getProfileManager().getProfile(player);
        double targetTotalV = target.getTotalValue(qty);
        profile.recordResult(win, wagerV, win ? targetTotalV : 0.0);

        if (win) {
            session.setSpinState(UpgraderSession.SpinState.RESULT_WIN);
            UpgraderSession.giveRewardQuantity(player, target, qty);
            session.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
            session.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
            spawnCelebrationFirework(player);

            player.sendMessage(TextUtil.color(plugin.getConfigManager().getPrefix()
                    + "&a&lTHÀNH CÔNG! &7Bạn đã nâng cấp trúng &e" + target.getDisplayName() + " x" + qty
                    + " &7(&6" + TextUtil.formatValue(targetTotalV) + " V&7) với tỉ lệ &a"
                    + TextUtil.formatPercent(chance) + "% &7(Điểm quay: &f" + TextUtil.formatPercent(rolled) + "&7)!"));

            if (profile.isBroadcastEnabled()
                    && (targetTotalV >= plugin.getConfigManager().getBroadcastMinValue()
                    || chance <= plugin.getConfigManager().getBroadcastMaxChance())) {
                Bukkit.broadcast(TextUtil.color(plugin.getConfigManager().getPrefix()
                        + "&e&l" + player.getName() + " &fđã liều lĩnh nâng cấp thành công &6&l"
                        + target.getDisplayName() + " x" + qty + " &8(&e" + TextUtil.formatValue(targetTotalV)
                        + " V&8) &fvới tỉ lệ chỉ &a&l" + TextUtil.formatPercent(chance) + "%&f!"));
            }
        } else {
            session.setSpinState(UpgraderSession.SpinState.RESULT_LOSS);
            session.playSound(player, Sound.BLOCK_ANVIL_LAND, 0.7f, 0.6f);
            player.sendMessage(TextUtil.color(plugin.getConfigManager().getPrefix()
                    + "&c&lTHẤT BẠI! &7Điểm quay &c" + TextUtil.formatPercent(rolled)
                    + " &7vượt ngoài vùng thắng &e" + TextUtil.formatPercent(chance)
                    + "%&7. Chúc bạn may mắn ở lượt tiếp theo!"));
        }

        plugin.getProfileManager().saveAll();

        if (player.isOnline() && session.getCurrentMenu() == UpgraderSession.MenuType.MAIN_UPGRADER
                && session.getActiveInventory() != null) {
            render(player, session, session.getActiveInventory());
        }
    }

    /**
     * Chọn nhanh phần thưởng theo hệ số nhân x1 ▸ x10 giống thanh hệ số trong ảnh
     */
    public void selectByMultiplier(Player player, UpgraderSession session, int multiplier) {
        boolean bypassRatio = player.hasPermission("tnupgrader.bypass.ratio") || player.hasPermission("tnupgrader.admin");
        int maxMult = plugin.getConfigManager().getMaxUpgradeMultiplier();
        if (!bypassRatio && multiplier > maxMult) {
            player.sendMessage(TextUtil.color(plugin.getConfigManager().getPrefix()
                    + "&cHệ số x" + multiplier + " đã bị khóa (Tối đa x" + maxMult + " để chống lợi dụng cày đồ)!"));
            session.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        session.setSelectedMultiplier(multiplier);
        double wagerV = session.getTotalWagerValue();
        double baseWager = wagerV > 0 ? wagerV : 250.0;
        double desiredTargetV = baseWager * multiplier;
        double lowestUnitV = session.getLowestWagerUnitValue();
        double minRatio = plugin.getConfigManager().getMinUnitValueRatio();
        int maxQty = plugin.getConfigManager().getMaxTargetQuantity();

        List<RewardItem> all = new ArrayList<>(plugin.getConfigManager().getRewardList());
        if (all.isEmpty()) return;

        RewardItem bestItem = null;
        int bestQty = 1;
        double bestDiff = Double.MAX_VALUE;

        for (RewardItem item : all) {
            if (!plugin.getConfigManager().canPlayerSelectRewardTier(player, item)) {
                continue;
            }
            if (!bypassRatio && lowestUnitV > 0 && lowestUnitV < item.getUnitValue() * minRatio) {
                continue;
            }
            int qty = 1;
            if (item.getUnitValue() < desiredTargetV) {
                qty = Math.max(1, Math.min(maxQty, (int) Math.round(desiredTargetV / item.getUnitValue())));
            }
            double totalV = item.getTotalValue(qty);
            double diff = Math.abs(totalV - desiredTargetV);
            if (diff < bestDiff) {
                bestDiff = diff;
                bestItem = item;
                bestQty = qty;
            }
        }

        if (bestItem != null) {
            session.setTargetReward(bestItem);
            session.setTargetQuantity(bestQty);
            if (session.getSpinState() != UpgraderSession.SpinState.SPINNING) {
                session.setSpinState(UpgraderSession.SpinState.IDLE);
            }
            session.playSound(player, Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            if (session.getActiveInventory() != null) {
                render(player, session, session.getActiveInventory());
            }
        } else {
            player.sendMessage(TextUtil.color(plugin.getConfigManager().getPrefix()
                    + "&eKhông tìm thấy vật phẩm phù hợp với bậc nguyên liệu đang cược! Hãy đặt nguyên liệu có giá trị cao hơn."));
            session.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
    }

    private void spawnCelebrationFirework(Player player) {
        try {
            Firework fw = player.getWorld().spawn(player.getLocation().add(0, 1, 0), Firework.class);
            FireworkMeta meta = fw.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder()
                    .withColor(Color.YELLOW, Color.ORANGE, Color.LIME)
                    .withFade(Color.WHITE)
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .trail(true)
                    .build());
            meta.setPower(0);
            fw.setFireworkMeta(meta);
        } catch (Exception ignored) {
        }
    }
}
