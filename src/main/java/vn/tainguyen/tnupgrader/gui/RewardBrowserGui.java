package vn.tainguyen.tnupgrader.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import vn.tainguyen.tnupgrader.TNUpgraderPlugin;
import vn.tainguyen.tnupgrader.model.ItemCategory;
import vn.tainguyen.tnupgrader.model.PlayerProfile;
import vn.tainguyen.tnupgrader.model.RewardItem;
import vn.tainguyen.tnupgrader.model.SortOrder;
import vn.tainguyen.tnupgrader.model.UiTheme;
import vn.tainguyen.tnupgrader.util.ItemBuilder;
import vn.tainguyen.tnupgrader.util.TextUtil;

import java.util.List;

public class RewardBrowserGui {

    public static final int ITEMS_PER_PAGE = 28;
    public static final int[] ITEM_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final TNUpgraderPlugin plugin;
    private final NamespacedKey rewardIdKey;
    private final NamespacedKey rewardQtyKey;

    public RewardBrowserGui(TNUpgraderPlugin plugin) {
        this.plugin = plugin;
        this.rewardIdKey = new NamespacedKey(plugin, "browser_reward_id");
        this.rewardQtyKey = new NamespacedKey(plugin, "browser_reward_qty");
    }

    public NamespacedKey getRewardIdKey() {
        return rewardIdKey;
    }

    public NamespacedKey getRewardQtyKey() {
        return rewardQtyKey;
    }

    public void open(Player player, UpgraderSession session) {
        session.setTransitioning(true);
        session.setCurrentMenu(UpgraderSession.MenuType.REWARD_BROWSER);

        PlayerProfile profile = plugin.getProfileManager().getProfile(player);
        UiTheme theme = profile.getTheme();

        Component title = TextUtil.color("&8&l✦ " + theme.getAccentColor() + "&lTÌM PHẦN THƯỞNG &8&l✦");
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

        List<RewardItem> filtered = session.getFilteredRewards();
        int totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / ITEMS_PER_PAGE));
        if (session.getBrowserPage() >= totalPages) {
            session.setBrowserPage(totalPages - 1);
        }
        int page = session.getBrowserPage();

        // Nền viền
        ItemStack bg = new ItemBuilder(theme.getBackgroundGlass()).name(" ").hideFlags().build();
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, bg);
        }

        // --- HÀNG ĐẦU (THANH TÌM KIẾM, DANH MỤC, SẮP XẾP - CHUẨN ẢNH 3, 4, 7, 9) ---
        // Slot 1 & 2: Tìm: tên / mã
        String query = session.getSearchQuery();
        ItemStack searchBox = new ItemBuilder(Material.NAME_TAG)
                .name("&fTìm: " + (query.isBlank() ? "&7tên / mã" : "&e" + query))
                .lore(
                        "&7Từ khóa hiện tại: " + (query.isBlank() ? "&8(Tất cả)" : "&e\"" + query + "\""),
                        "",
                        "&a▸ Chạm / Chuột trái: &fNhập từ khóa tìm kiếm",
                        "&e  (VD: gõ 'kim', 'netherite', 'táo'...)",
                        "&c▸ Chuột phải / Shift: &fXóa bộ lọc từ khóa"
                )
                .glow(!query.isBlank())
                .build();
        inv.setItem(1, searchBox);
        inv.setItem(2, searchBox);

        // Slot 4: Thông tin tổng kết quả & Cược hiện tại (Giống: "33 kết quả • Cược: 972 V")
        inv.setItem(4, new ItemBuilder(Material.PAPER)
                .name("&f" + filtered.size() + " kết quả &8• &7Cược: &6" + TextUtil.formatValue(wagerV) + " V")
                .lore(
                        "&7Danh mục: &e" + session.getCategoryFilter().getDisplayName(),
                        "&7Sắp xếp: &b" + session.getSortOrder().getDisplayName(),
                        "",
                        "&7Hệ thống tự động quy đổi số lượng &fxN",
                        "&7và tính &a% tỉ lệ thành công &7theo số V cược!"
                )
                .build());

        // Slot 5: Lọc Danh Mục (Tất cả / Thức ăn / Vũ khí...)
        ItemCategory cat = session.getCategoryFilter();
        inv.setItem(5, new ItemBuilder(cat.getIcon())
                .name("&e" + cat.getDisplayName())
                .lore(
                        "&7Lọc phần thưởng theo nhóm vật phẩm:",
                        formatCategoryLine(cat, ItemCategory.ALL),
                        formatCategoryLine(cat, ItemCategory.EQUIPMENT),
                        formatCategoryLine(cat, ItemCategory.TOOLS),
                        formatCategoryLine(cat, ItemCategory.MINERALS),
                        formatCategoryLine(cat, ItemCategory.FOOD),
                        formatCategoryLine(cat, ItemCategory.BLOCKS),
                        formatCategoryLine(cat, ItemCategory.SPECIAL),
                        "",
                        "&e▸ Chạm để chuyển danh mục!"
                )
                .hideFlags()
                .build());

        // Slot 6: Sắp xếp (Giá tăng / Giá giảm / Tỉ lệ...)
        SortOrder sort = session.getSortOrder();
        inv.setItem(6, new ItemBuilder(Material.HOPPER)
                .name("&b" + sort.getDisplayName())
                .lore(
                        "&7Thứ tự sắp xếp phần thưởng:",
                        formatSortLine(sort, SortOrder.PRICE_ASC),
                        formatSortLine(sort, SortOrder.PRICE_DESC),
                        formatSortLine(sort, SortOrder.CHANCE_DESC),
                        formatSortLine(sort, SortOrder.NAME_ASC),
                        "",
                        "&e▸ Chạm để đổi kiểu sắp xếp!"
                )
                .build());

        // Slot 8: Nút [X] Quay lại Nâng Cấp Vật Phẩm
        inv.setItem(8, new ItemBuilder(Material.ARROW)
                .name("&c&l✕ Quay lại Nâng Cấp")
                .lore("&7Trở về màn hình Nâng Cấp Vật Phẩm chính.")
                .build());

        // --- DANH SÁCH VẬT PHẨM PHẦN THƯỞNG ---
        int startIndex = page * ITEMS_PER_PAGE;
        double multSetting = plugin.getConfigManager().getChanceMultiplier();
        double maxChance = plugin.getConfigManager().getPlayerMaxSuccessChance(player);
        int maxQty = plugin.getConfigManager().getMaxTargetQuantity();
        double minRatio = plugin.getConfigManager().getMinUnitValueRatio();

        for (int i = 0; i < ITEM_SLOTS.length; i++) {
            int slot = ITEM_SLOTS[i];
            int idx = startIndex + i;
            if (idx >= filtered.size()) {
                inv.setItem(slot, new ItemBuilder(Material.AIR).build());
                continue;
            }

            RewardItem item = filtered.get(idx);
            int suggestedQty = Math.min(maxQty, item.getSuggestedQuantity(wagerV));
            double totalV = item.getTotalValue(suggestedQty);
            double chance = item.calculateChance(wagerV, suggestedQty, multSetting, maxChance);
            boolean isFav = profile.isFavorite(item.getId());
            boolean isSelected = session.getTargetReward() != null
                    && session.getTargetReward().getId().equals(item.getId());
            boolean tierUnlocked = plugin.getConfigManager().canPlayerSelectRewardTier(player, item);
            double minReqUnitV = item.getUnitValue() * minRatio;

            String chanceColor = chance >= 60.0 ? "&a" : (chance >= 30.0 ? "&e" : "&6");
            ItemStack iconStack = item.createItemStack(Math.min(64, suggestedQty));

            ItemStack display = new ItemBuilder(iconStack)
                    .name((tierUnlocked ? "&f" : "&c🔒 ") + item.getDisplayName() + " &7x" + suggestedQty + (isFav ? " &e★" : ""))
                    .lore(
                            "&7" + TextUtil.formatValue(totalV) + " V &8• " + chanceColor + TextUtil.formatPercent(chance) + "%",
                            "",
                            "&8Đơn giá: &e" + TextUtil.formatValue(item.getUnitValue()) + " V / món",
                            "&8Yêu cầu đơn giá đồ cược: &6≥ " + TextUtil.formatValue(minReqUnitV) + " V / món",
                            "&8Phân bậc: " + plugin.getConfigManager().getRequiredTierPermissionLabel(item),
                            "",
                            tierUnlocked
                                    ? "&a▸ Chạm / Chuột trái: &fChọn làm mục tiêu nâng cấp"
                                    : "&c✘ Bạn chưa có quyền mở khóa bậc phần thưởng này!",
                            "&e▸ Chuột phải: &f" + (isFav ? "Bỏ khỏi Yêu thích" : "Thêm vào Yêu thích ★")
                    )
                    .glow(isSelected || isFav)
                    .pdc(rewardIdKey, item.getId())
                    .pdcInt(rewardQtyKey, suggestedQty)
                    .build();
            inv.setItem(slot, display);
        }

        // --- HÀNG CUỐI: PHÂN TRANG (< Trước | Trang X/Y | Sau >) ---
        if (page > 0) {
            ItemStack prevBtn = new ItemBuilder(Material.SPECTRAL_ARROW)
                    .name("&e‹ Trước")
                    .lore("&7Chuyển về trang " + page + "/" + totalPages)
                    .build();
            inv.setItem(45, prevBtn);
            inv.setItem(46, prevBtn);
        } else {
            ItemStack disabledPrev = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                    .name("&8‹ Trước (Đầu trang)")
                    .build();
            inv.setItem(45, disabledPrev);
            inv.setItem(46, disabledPrev);
        }

        inv.setItem(49, new ItemBuilder(Material.BOOK)
                .name("&fTrang " + (page + 1) + "/" + totalPages)
                .lore(
                        "&7Tổng số vật phẩm khớp: &e" + filtered.size(),
                        "&7Đang cược: &6" + TextUtil.formatValue(wagerV) + " V"
                )
                .build());

        if (page + 1 < totalPages) {
            ItemStack nextBtn = new ItemBuilder(Material.SPECTRAL_ARROW)
                    .name("&eSau ›")
                    .lore("&7Chuyển sang trang " + (page + 2) + "/" + totalPages)
                    .build();
            inv.setItem(52, nextBtn);
            inv.setItem(53, nextBtn);
        } else {
            ItemStack disabledNext = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                    .name("&8Sau › (Cuối trang)")
                    .build();
            inv.setItem(52, disabledNext);
            inv.setItem(53, disabledNext);
        }
    }

    private String formatCategoryLine(ItemCategory current, ItemCategory target) {
        return (current == target ? " &a▸ &l" : " &8• &7") + target.getDisplayName();
    }

    private String formatSortLine(SortOrder current, SortOrder target) {
        return (current == target ? " &b▸ &l" : " &8• &7") + target.getDisplayName();
    }
}
