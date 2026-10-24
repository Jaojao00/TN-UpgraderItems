package vn.tainguyen.tnupgrader.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import vn.tainguyen.tnupgrader.TNUpgraderPlugin;
import vn.tainguyen.tnupgrader.model.PlayerProfile;
import vn.tainguyen.tnupgrader.model.RewardItem;
import vn.tainguyen.tnupgrader.model.UiTheme;
import vn.tainguyen.tnupgrader.util.ItemBuilder;
import vn.tainguyen.tnupgrader.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

public class SubMenusGui {

    private final TNUpgraderPlugin plugin;

    public SubMenusGui(TNUpgraderPlugin plugin) {
        this.plugin = plugin;
    }

    // ==========================================================================
    // 1. MENU CÀI ĐẶT (SETTINGS)
    // ==========================================================================
    public void openSettings(Player player, UpgraderSession session) {
        session.setTransitioning(true);
        session.setCurrentMenu(UpgraderSession.MenuType.SETTINGS);

        PlayerProfile profile = plugin.getProfileManager().getProfile(player);
        UiTheme theme = profile.getTheme();

        Component title = TextUtil.color("&8&l✦ " + theme.getAccentColor() + "&lCÀI ĐẶT NÂNG CẤP &8&l✦");
        Inventory inv = Bukkit.createInventory(null, 27, title);
        session.setActiveInventory(inv);

        ItemStack bg = new ItemBuilder(theme.getBackgroundGlass()).name(" ").hideFlags().build();
        for (int i = 0; i < 27; i++) inv.setItem(i, bg);

        inv.setItem(10, new ItemBuilder(profile.isSpinAnimation() ? Material.CLOCK : Material.SUGAR)
                .name("&e&l⏳ Hiệu Ứng Vòng Quay")
                .lore(
                        "&7Trạng thái: " + (profile.isSpinAnimation() ? "&aBật (Quay hồi hộp)" : "&eTắt (Kết quả tức thì)"),
                        "",
                        "&7Khi bật, kim đồng hồ sẽ chạy vòng quanh",
                        "&7vòng tròn tỉ lệ đầy kịch tính như CS:GO.",
                        "",
                        "&e▸ Chạm để bật/tắt!"
                )
                .glow(profile.isSpinAnimation())
                .build());

        inv.setItem(12, new ItemBuilder(profile.isSoundEnabled() ? Material.NOTE_BLOCK : Material.BARRIER)
                .name("&b&l🔊 Âm Thanh Hiệu Ứng")
                .lore(
                        "&7Trạng thái: " + (profile.isSoundEnabled() ? "&aĐang bật" : "&cĐang tắt"),
                        "",
                        "&7Phát âm thanh tích tắc khi quay và",
                        "&7nhạc chiến thắng khi nâng cấp thành công.",
                        "",
                        "&e▸ Chạm để bật/tắt!"
                )
                .build());

        inv.setItem(14, new ItemBuilder(profile.isBroadcastEnabled() ? Material.BELL : Material.GRAY_DYE)
                .name("&6&l📢 Thông Báo Vinh Danh Server")
                .lore(
                        "&7Trạng thái: " + (profile.isBroadcastEnabled() ? "&aĐang bật" : "&cĐang tắt"),
                        "",
                        "&7Thông báo lên toàn máy chủ khi bạn",
                        "&7đập trúng vật phẩm hiếm hoặc tỉ lệ thấp.",
                        "",
                        "&e▸ Chạm để bật/tắt!"
                )
                .build());

        inv.setItem(16, new ItemBuilder(theme.getAccentGlass())
                .name(theme.getAccentColor() + "&l🎨 Màu Giao Diện: " + theme.getDisplayName())
                .lore(
                        "&7Đổi màu chủ đạo của toàn bộ bảng",
                        "&7Nâng Cấp Vật Phẩm theo sở thích cá nhân.",
                        "",
                        "&e▸ Chạm để đổi màu giao diện!"
                )
                .build());

        inv.setItem(22, new ItemBuilder(Material.ARROW)
                .name("&c&l✕ Quay lại Nâng Cấp Vật Phẩm")
                .build());

        player.openInventory(inv);
        session.setTransitioning(false);
    }

    // ==========================================================================
    // 2. MENU THỐNG KÊ & BẢNG XẾP HẠNG (STATISTICS)
    // ==========================================================================
    public void openStatistics(Player player, UpgraderSession session) {
        session.setTransitioning(true);
        session.setCurrentMenu(UpgraderSession.MenuType.STATISTICS);

        PlayerProfile profile = plugin.getProfileManager().getProfile(player);
        UiTheme theme = profile.getTheme();

        Component title = TextUtil.color("&8&l✦ " + theme.getAccentColor() + "&lTHỐNG KÊ NÂNG CẤP &8&l✦");
        Inventory inv = Bukkit.createInventory(null, 36, title);
        session.setActiveInventory(inv);

        ItemStack bg = new ItemBuilder(theme.getBackgroundGlass()).name(" ").hideFlags().build();
        for (int i = 0; i < 36; i++) inv.setItem(i, bg);

        double profit = profile.getTotalWonValue() - profile.getTotalWageredValue();
        String profitStr = (profit >= 0 ? "&a+" : "&c") + TextUtil.formatValue(profit) + " V";

        inv.setItem(13, new ItemBuilder(Material.NETHER_STAR)
                .name("&6&l📊 HỒ SƠ CỦA: &e" + player.getName())
                .lore(
                        "&7Tổng số lần nâng cấp: &f" + profile.getTotalAttempts(),
                        "&7Số lần Thành công: &a" + profile.getTotalWins(),
                        "&7Số lần Thất bại: &c" + profile.getTotalLosses(),
                        "&7Tỉ lệ thắng thực tế: &e" + TextUtil.formatPercent(profile.getWinRate()) + "%",
                        "",
                        "&7Tổng giá trị đã cược: &6" + TextUtil.formatValue(profile.getTotalWageredValue()) + " V",
                        "&7Tổng giá trị thu về: &a" + TextUtil.formatValue(profile.getTotalWonValue()) + " V",
                        "&7Lợi nhuận ròng: " + profitStr,
                        "",
                        "&7Chuỗi thắng hiện tại: &b🔥 " + profile.getCurrentStreak(),
                        "&7Chuỗi thắng kỷ lục: &6🏆 " + profile.getBestStreak(),
                        "&7Bảo hiểm may mắn (Pity): &a+" + TextUtil.formatPercent(profile.getPityBonus()) + "%"
                )
                .glow(true)
                .build());

        // Top 5 Đại Gia Nâng Cấp trên Server (Slots 19, 20, 21, 22, 23, 24, 25 -> lấy 20..24)
        List<PlayerProfile> topList = new ArrayList<>(plugin.getProfileManager().getAllProfiles());
        topList.sort((a, b) -> Double.compare(b.getTotalWonValue(), a.getTotalWonValue()));

        int[] topSlots = {20, 21, 22, 23, 24};
        for (int i = 0; i < topSlots.length; i++) {
            int slot = topSlots[i];
            if (i < topList.size()) {
                PlayerProfile top = topList.get(i);
                Material medal = switch (i) {
                    case 0 -> Material.GOLD_BLOCK;
                    case 1 -> Material.IRON_BLOCK;
                    case 2 -> Material.COPPER_BLOCK;
                    default -> Material.EMERALD;
                };
                inv.setItem(slot, new ItemBuilder(medal)
                        .name("&6&lTOP #" + (i + 1) + " &f" + top.getPlayerName())
                        .lore(
                                "&7Tổng thắng: &a" + TextUtil.formatValue(top.getTotalWonValue()) + " V",
                                "&7Số trận thắng: &e" + top.getTotalWins() + "/" + top.getTotalAttempts()
                                        + " (&a" + TextUtil.formatPercent(top.getWinRate()) + "%&e)",
                                "&7Chuỗi thắng cao nhất: &6🔥 " + top.getBestStreak()
                        )
                        .glow(i == 0)
                        .build());
            } else {
                inv.setItem(slot, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                        .name("&8TOP #" + (i + 1) + " (Chưa có)")
                        .build());
            }
        }

        inv.setItem(31, new ItemBuilder(Material.ARROW)
                .name("&c&l✕ Quay lại Nâng Cấp Vật Phẩm")
                .build());

        player.openInventory(inv);
        session.setTransitioning(false);
    }

    // ==========================================================================
    // 3. MENU YÊU THÍCH (FAVORITES)
    // ==========================================================================
    public void openFavorites(Player player, UpgraderSession session) {
        session.setTransitioning(true);
        session.setCurrentMenu(UpgraderSession.MenuType.FAVORITES);

        PlayerProfile profile = plugin.getProfileManager().getProfile(player);
        UiTheme theme = profile.getTheme();

        Component title = TextUtil.color("&8&l✦ " + theme.getAccentColor() + "&lDANH SÁCH YÊU THÍCH &8&l✦");
        Inventory inv = Bukkit.createInventory(null, 54, title);
        session.setActiveInventory(inv);

        ItemStack bg = new ItemBuilder(theme.getBackgroundGlass()).name(" ").hideFlags().build();
        for (int i = 0; i < 54; i++) inv.setItem(i, bg);

        List<String> favIds = new ArrayList<>(profile.getFavoriteItemIds());
        double wagerV = session.getTotalWagerValue();
        double mult = plugin.getConfigManager().getChanceMultiplier();
        double maxChance = plugin.getConfigManager().getMaxSuccessChance();

        int idx = 0;
        for (int slot : RewardBrowserGui.ITEM_SLOTS) {
            if (idx >= favIds.size()) {
                inv.setItem(slot, new ItemBuilder(Material.AIR).build());
                continue;
            }
            RewardItem item = plugin.getConfigManager().getRewardById(favIds.get(idx++));
            if (item == null) continue;
            int qty = item.getSuggestedQuantity(wagerV);
            double totalV = item.getTotalValue(qty);
            double chance = item.calculateChance(wagerV, qty, mult, maxChance);

            inv.setItem(slot, new ItemBuilder(item.createItemStack(Math.min(64, qty)))
                    .name("&e★ &f" + item.getDisplayName() + " &7x" + qty)
                    .lore(
                            "&7" + TextUtil.formatValue(totalV) + " V &8• &a" + TextUtil.formatPercent(chance) + "%",
                            "",
                            "&a▸ Chạm / Chuột trái: &fChọn làm mục tiêu nâng cấp",
                            "&c▸ Chuột phải: &fXóa khỏi danh sách Yêu thích"
                    )
                    .glow(true)
                    .pdc(plugin.getRewardBrowserGui().getRewardIdKey(), item.getId())
                    .pdcInt(plugin.getRewardBrowserGui().getRewardQtyKey(), qty)
                    .build());
        }

        inv.setItem(49, new ItemBuilder(Material.ARROW)
                .name("&c&l✕ Quay lại Nâng Cấp Vật Phẩm")
                .build());

        player.openInventory(inv);
        session.setTransitioning(false);
    }

    // ==========================================================================
    // 4. MENU HƯỚNG DẪN CHI TIẾT (GUIDE)
    // ==========================================================================
    public void openGuide(Player player, UpgraderSession session) {
        session.setTransitioning(true);
        session.setCurrentMenu(UpgraderSession.MenuType.GUIDE);

        PlayerProfile profile = plugin.getProfileManager().getProfile(player);
        UiTheme theme = profile.getTheme();

        Component title = TextUtil.color("&8&l✦ " + theme.getAccentColor() + "&lHƯỚNG DẪN TN-UPGRADER &8&l✦");
        Inventory inv = Bukkit.createInventory(null, 27, title);
        session.setActiveInventory(inv);

        ItemStack bg = new ItemBuilder(theme.getBackgroundGlass()).name(" ").hideFlags().build();
        for (int i = 0; i < 27; i++) inv.setItem(i, bg);

        inv.setItem(10, new ItemBuilder(Material.ANVIL)
                .name("&6&l1. Chế Tạo Đài Nâng Cấp & Sổ Di Động")
                .lore(
                        "&7Bạn có thể mở bảng nâng cấp bằng:",
                        "&f• Lệnh nhanh: &e/nangcap &7hoặc &e/upgrader",
                        "&f• &6Đài Nâng Cấp Tối Thượng: &7Ghép từ",
                        "&7  &eThỏi Vàng + Kim Cương + Khối Vàng + Cái Đe&7.",
                        "&f• &eSổ Tay Nâng Cấp Di Động: &7Ghép từ",
                        "&7  &eThỏi Vàng + Kim Cương + Quyển Sách&7."
                )
                .glow(true)
                .build());

        inv.setItem(12, new ItemBuilder(Material.DIAMOND)
                .name("&b&l2. Cơ Chế Đấu Giá Nâng Đời Vật Phẩm")
                .lore(
                        "&7Mỗi vật phẩm đều có điểm giá trị &6(V)&7:",
                        "&f• Chạm vào đồ trong túi để đưa lên lưới &e3x3 Đồ Cược&f.",
                        "&f• Bấm &bTìm vật phẩm &fhoặc nút &ex1 ▸ x10 &fđể chọn đích.",
                        "&f• Công thức: &aTỉ lệ (%) = (Tổng Cược V / Giá Đích V) × 100",
                        "&f• Bấm &6NÂNG CẤP &fđể quay vòng quay may rủi!"
                )
                .glow(true)
                .build());

        inv.setItem(14, new ItemBuilder(Material.NETHERITE_SWORD)
                .name("&c&l3. Hệ Sinh Thái Cường Hóa & Cổ Ngữ Độc Quyền")
                .lore(
                        "&7Hoạt động độc lập 100% với 3 nhánh nâng cấp:",
                        "&f• &6Tab 1 (Thuộc Tính): &7Đào Nhanh +25%/cấp, Sát thương,",
                        "&7  Máu, Giáp, Hút máu, Chí mạng, Đam chuẩn xuyên giáp.",
                        "&f• &dTab 2 (Phù Phép): &7Hiệu Suất, Chậm Hỏng, Gia Tài,",
                        "&7  Sửa Chữa, Chạm Nhẹ, Đào Nhanh Dưới Nước...",
                        "&f• &bTab 3 (12 Cổ Ngữ Độc Quyền TN): &7Mũi Khoan Địa Tâm",
                        "&7  (Đào mạch quặng), Búa 3x3, Lò Nung Tự Động, Hút Đồ...",
                        "&f• Thất bại &aGIỮ NGUYÊN ĐỒ &f+ tích lũy &e% May Mắn&f!"
                )
                .glow(true)
                .hideFlags()
                .build());

        inv.setItem(16, new ItemBuilder(Material.RECOVERY_COMPASS)
                .name("&a&l4. Tối Ưu Hóa Cho Điện Thoại (PE) & PC")
                .lore(
                        "&7Giao diện thiết kế thao tác &e1-Chạm (One-Tap)&7:",
                        "&f• Người chơi PE/Bedrock chỉ cần &echạm 1 lần &fvào",
                        "&f  vật phẩm dưới túi đồ là tự động nạp lên ô cược.",
                        "&f• Không cần kéo thả hay giữ Shift phức tạp!"
                )
                .glow(true)
                .build());

        inv.setItem(22, new ItemBuilder(Material.ARROW)
                .name("&c&l✕ Quay lại Nâng Cấp Vật Phẩm")
                .build());

        player.openInventory(inv);
        session.setTransitioning(false);
    }
}
