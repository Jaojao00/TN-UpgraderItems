package vn.tainguyen.tnupgrader.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import vn.tainguyen.tnupgrader.TNUpgraderPlugin;
import vn.tainguyen.tnupgrader.manager.EquipmentManager;
import vn.tainguyen.tnupgrader.model.PlayerProfile;
import vn.tainguyen.tnupgrader.model.UiTheme;
import vn.tainguyen.tnupgrader.model.UpgradeAttribute;
import vn.tainguyen.tnupgrader.util.ItemBuilder;
import vn.tainguyen.tnupgrader.util.TextUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class EquipmentUpgraderGui {

    public static final int EQUIPMENT_SLOT = 10;
    public static final int[] MATERIAL_SLOTS = {19, 20, 28, 29};
    public static final int[] WHEEL_RING_SLOTS = MainUpgraderGui.WHEEL_RING_SLOTS;
    public static final int WHEEL_CENTER_SLOT = 13;
    public static final int TAB_SWITCH_SLOT = 16;
    public static final int RESET_ATTR_SLOT = 24;
    public static final int EXECUTE_UPGRADE_SLOT = 25;
    public static final int PITY_INFO_SLOT = 26;

    private final TNUpgraderPlugin plugin;
    private final NamespacedKey enchantKeyTag;

    public EquipmentUpgraderGui(TNUpgraderPlugin plugin) {
        this.plugin = plugin;
        this.enchantKeyTag = new NamespacedKey(plugin, "equip_enchant_key");
    }

    public NamespacedKey getEnchantKeyTag() {
        return enchantKeyTag;
    }

    public void open(Player player, UpgraderSession session) {
        session.setTransitioning(true);
        session.setCurrentMenu(UpgraderSession.MenuType.EQUIPMENT_UPGRADER);

        PlayerProfile profile = plugin.getProfileManager().getProfile(player);
        UiTheme theme = profile.getTheme();

        Component title = TextUtil.color("&8&l✦ " + theme.getAccentColor() + "&lCƯỜNG HÓA TRANG BỊ &8&l✦");
        Inventory inv = Bukkit.createInventory(null, 54, title);
        session.setActiveInventory(inv);

        render(player, session, inv);
        player.openInventory(inv);
        session.setTransitioning(false);
    }

    public void render(Player player, UpgraderSession session, Inventory inv) {
        PlayerProfile profile = plugin.getProfileManager().getProfile(player);
        UiTheme theme = profile.getTheme();
        EquipmentManager eqManager = plugin.getEquipmentManager();

        ItemStack bg = new ItemBuilder(theme.getBackgroundGlass()).name(" ").hideFlags().build();
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, bg);
        }

        // Hàng đầu
        inv.setItem(0, new ItemBuilder(Material.SMITHING_TABLE)
                .name("&6&l⚒ CƯỜNG HÓA END-GAME")
                .lore(
                        "&7Tối ưu hóa sức mạnh Vũ khí, Công cụ & Giáp",
                        "&7(Hỗ trợ cả đồ &dMythicMobs &7& &bExcellentEnchants&7).",
                        "",
                        "&e▸ Cách thao tác (PC & PE):",
                        "&f1. Chạm vào Trang bị dưới túi đồ để đặt vào ô",
                        "&f2. Chạm vào Nguyên liệu (Vàng, Kim cương, Netherite...)",
                        "&f3. Chọn Thuộc tính hoặc Phù phép ở hàng dưới",
                        "&f4. Bấm &6⚡ CƯỜNG HÓA &fđể quay vòng quay may rủi!"
                )
                .build());

        inv.setItem(7, new ItemBuilder(Material.GOLD_INGOT)
                .name("&e&l🎲 CHẾ ĐỘ ĐẤU GIÁ VẬT PHẨM")
                .lore(
                        "&7Quay về giao diện &6Đấu Giá Nâng Đời Vật Phẩm",
                        "&7(Đặt cược 3x3 đổi mọi vật phẩm trong game).",
                        "",
                        "&e▸ Chạm để chuyển về Đấu Giá Vật Phẩm!"
                )
                .glow(true)
                .build());

        inv.setItem(8, new ItemBuilder(Material.BARRIER)
                .name("&c&l✕ Đóng giao diện")
                .lore("&7Trang bị & nguyên liệu sẽ tự động trả về túi.")
                .build());

        // --- Ô TRANG BỊ (SLOT 10) ---
        ItemStack eq = session.getEquipmentItem();
        if (eq != null && !eq.getType().isAir()) {
            int stars = eqManager.getTotalUpgradeStars(eq);
            Map<UpgradeAttribute, Integer> attrs = eqManager.getAllAttributes(eq);
            ItemStack eqDisplay = new ItemBuilder(eq.clone())
                    .addLore(
                            "",
                            "&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                            "&7Cấp sao tổng: &e+" + stars + " ⭐",
                            "&7Số dòng thuộc tính bổ sung: &a" + attrs.size() + "&7/&6" + eqManager.getMaxCustomAttributes(),
                            "&c▸ Chạm để lấy trang bị về túi đồ"
                    )
                    .build();
            inv.setItem(EQUIPMENT_SLOT, eqDisplay);
        } else {
            inv.setItem(EQUIPMENT_SLOT, new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE)
                    .name("&b&lÔ ĐẶT TRANG BỊ")
                    .lore(
                            "&7Chạm vào &eVũ khí, Giáp hoặc Công cụ",
                            "&7trong túi đồ của bạn để đưa lên đây!",
                            "",
                            "&8(Hỗ trợ đồ Netherite, MythicMobs...)"
                    )
                    .build());
        }

        // --- 4 Ô NGUYÊN LIỆU CƯỜNG HÓA (SLOTS 19, 20, 28, 29) ---
        ItemStack[] mats = session.getMaterialItems();
        for (int i = 0; i < MATERIAL_SLOTS.length; i++) {
            int slot = MATERIAL_SLOTS[i];
            ItemStack m = mats[i];
            if (m != null && !m.getType().isAir()) {
                double val = plugin.getConfigManager().getStackValue(m);
                inv.setItem(slot, new ItemBuilder(m.clone())
                        .addLore(
                                "",
                                "&7Điểm nguyên liệu: &6+" + TextUtil.formatValue(val) + " V",
                                "&c▸ Chạm để rút nguyên liệu về"
                        )
                        .build());
            } else {
                inv.setItem(slot, new ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                        .name("&7Ô Nguyên Liệu #" + (i + 1))
                        .lore(
                                "&7Chạm vào Khoáng sản / Nguyên liệu",
                                "&7trong túi đồ để nạp điểm &6V &7nâng cấp!"
                        )
                        .build());
            }
        }

        // --- TÍNH CHI PHÍ & TỶ LỆ THÀNH CÔNG ---
        double matV = session.getTotalMaterialValue();
        double requiredV = calculateRequiredCostV(session);
        double pityBonus = profile.getPityBonus();
        double chance = calculateUpgradeChance(session, profile);

        // --- VÒNG QUAY TRUNG TÂM ---
        renderEquipmentWheel(session, inv, chance, requiredV, matV, pityBonus);

        // --- BÊN PHẢI: CHUYỂN TAB THUỘC TÍNH <-> PHÙ PHÉP ---
        boolean enchantTab = session.isEnchantUpgradeTab();
        inv.setItem(TAB_SWITCH_SLOT, new ItemBuilder(enchantTab ? Material.ENCHANTED_BOOK : Material.NETHER_STAR)
                .name(enchantTab
                        ? "&d&l✦ CHẾ ĐỘ: NÂNG CẤP PHÙ PHÉP"
                        : "&6&l⚔ CHẾ ĐỘ: NÂNG CẤP THUỘC TÍNH")
                .lore(
                        "&7Đang chọn: " + (enchantTab ? "&dPhù Phép (Vanilla & ExcellentEnchants)" : "&6Thuộc Tính Bổ Sung End-Game"),
                        "",
                        "&f• &6Thuộc Tính Bổ Sung: &7Cộng Sát thương, Máu,",
                        "&7  Hút máu, Chí mạng, Sát thương chuẩn (Tối đa " + eqManager.getMaxCustomAttributes() + " dòng).",
                        "&f• &dPhù Phép: &7Nâng cấp bùa trên trang bị vượt",
                        "&7  giới hạn thông thường!",
                        "",
                        "&e▸ Chạm để đổi giữa Thuộc Tính & Phù Phép!"
                )
                .glow(true)
                .build());

        // Slot 24: Tẩy luyện thuộc tính
        inv.setItem(RESET_ATTR_SLOT, new ItemBuilder(Material.GRINDSTONE)
                .name("&c&l♻ Tẩy Luyện Thuộc Tính")
                .lore(
                        "&7Xóa các dòng thuộc tính bổ sung hiện tại",
                        "&7trên trang bị để chọn lại bộ " + eqManager.getMaxCustomAttributes() + " dòng mới.",
                        "",
                        "&e▸ Shift + Click để tẩy luyện trang bị!"
                )
                .build());

        // Slot 26: Điểm Bảo Hiểm May Mắn (Pity)
        double addPity = plugin.getConfig().getDouble("equipment-upgrader.pity-bonus-per-fail", 3.5);
        double maxPity = plugin.getConfig().getDouble("equipment-upgrader.max-pity-bonus", 35.0);
        inv.setItem(PITY_INFO_SLOT, new ItemBuilder(Material.TOTEM_OF_UNDYING)
                .name("&a&l🍀 Bảo Hiểm May Mắn: &e+" + TextUtil.formatPercent(pityBonus) + "%")
                .lore(
                        "&7Khi cường hóa thất bại, trang bị của bạn",
                        "&aKHÔNG BỊ MẤT &7mà còn được cộng tích lũy",
                        "&e+" + TextUtil.formatPercent(addPity) + "% &7tỉ lệ thành công cho lần kế tiếp!",
                        "",
                        "&7Tích lũy hiện tại: &a+" + TextUtil.formatPercent(pityBonus) + "% &7(Tối đa " + TextUtil.formatPercent(maxPity) + "%)"
                )
                .glow(pityBonus > 0)
                .build());

        // Slot 25: Nút CƯỜNG HÓA NGAY
        boolean canUpgradeNow = canExecuteUpgrade(session) && matV > 0;
        inv.setItem(EXECUTE_UPGRADE_SLOT, new ItemBuilder(canUpgradeNow ? Material.GOLD_BLOCK : Material.ANVIL)
                .name(canUpgradeNow ? "&6&l⚡ CƯỜNG HÓA NGAY ⚡" : "&e&lCƯỜNG HÓA &7(Chưa sẵn sàng)")
                .lore(
                        "&7Nguyên liệu đã nạp: &6" + TextUtil.formatValue(matV) + " V &7/ &e" + TextUtil.formatValue(requiredV) + " V",
                        "&7Bảo hiểm may mắn: &a+" + TextUtil.formatPercent(pityBonus) + "%",
                        "&7Tổng tỉ lệ thành công: &a&l" + TextUtil.formatPercent(chance) + "%",
                        "",
                        "&7Thất bại: &aGiữ nguyên trang bị &7(&e+" + TextUtil.formatPercent(addPity) + "% May mắn&7)",
                        "",
                        canUpgradeNow ? "&6&l▸ CHẠM ĐỂ QUAY CƯỜNG HÓA!" : "&c▸ Hãy đặt Trang bị, Nguyên liệu & chọn dòng nâng cấp!"
                )
                .glow(canUpgradeNow)
                .build());

        // --- HÀNG 5 (SLOTS 36..44): DANH SÁCH THUỘC TÍNH HOẶC PHÙ PHÉP ---
        if (!enchantTab) {
            UpgradeAttribute[] attrs = UpgradeAttribute.values();
            Map<UpgradeAttribute, Integer> currentAttrs = eq != null ? eqManager.getAllAttributes(eq) : Map.of();
            int maxSlots = eqManager.getMaxCustomAttributes();
            int maxLvl = eqManager.getMaxAttributeLevel();

            for (int i = 0; i < Math.min(9, attrs.length); i++) {
                UpgradeAttribute attr = attrs[i];
                int slot = 36 + i;
                int curLvl = currentAttrs.getOrDefault(attr, 0);
                boolean isSelected = session.getSelectedAttribute() == attr;
                boolean slotFullLocked = (eq != null) && (curLvl == 0) && (currentAttrs.size() >= maxSlots);
                boolean maxedOut = curLvl >= maxLvl;
                double nextCost = eqManager.getAttributeUpgradeCostV(curLvl + 1);
                double perLvl = eqManager.getAttributePerLevel(attr);

                Material icon = slotFullLocked ? Material.BARRIER : attr.getDefaultIcon();
                inv.setItem(slot, new ItemBuilder(icon)
                        .name(eqManager.getAttributeDisplayName(attr) + " &7[Cấp " + curLvl + "/" + maxLvl + "]")
                        .lore(
                                "&7" + eqManager.getAttributeDescription(attr),
                                "",
                                "&7Chỉ số mỗi cấp: &a+" + TextUtil.formatValue(perLvl) + eqManager.getAttributeUnit(attr),
                                "&7Hiện tại: &e+" + TextUtil.formatValue(perLvl * curLvl) + eqManager.getAttributeUnit(attr)
                                        + " &8▸ &a+" + TextUtil.formatValue(perLvl * (curLvl + 1)) + eqManager.getAttributeUnit(attr),
                                "&7Chi phí 100% tỉ lệ: &6" + TextUtil.formatValue(nextCost) + " V",
                                "",
                                maxedOut ? "&6&l★ ĐÃ ĐẠT CẤP TỐI ĐA!"
                                        : (slotFullLocked
                                        ? "&c✘ Đã đạt giới hạn " + maxSlots + " dòng thuộc tính!"
                                        : (isSelected ? "&a&l✔ ĐANG CHỌN NÂNG CẤP" : "&e▸ Chạm để chọn dòng thuộc tính này"))
                        )
                        .glow(isSelected)
                        .hideFlags()
                        .build());
            }
        } else {
            // Tab Phù Phép (Vanilla & ExcellentEnchants)
            if (eq == null || eq.getType().isAir()) {
                for (int i = 0; i < 9; i++) {
                    inv.setItem(36 + i, new ItemBuilder(Material.ENCHANTED_BOOK)
                            .name("&7Hãy đặt Trang Bị vào ô trước")
                            .lore("&8Hệ thống sẽ hiển thị các Phù Phép", "&8(Vanilla & ExcellentEnchants) phù hợp.")
                            .build());
                }
            } else {
                List<Enchantment> applicable = plugin.getExcellentEnchantsHook().getApplicableEnchantments(eq);
                if (session.getSelectedEnchantment() == null && !applicable.isEmpty()) {
                    session.setSelectedEnchantment(applicable.get(0));
                }
                for (int i = 0; i < 9; i++) {
                    int slot = 36 + i;
                    if (i >= applicable.size()) {
                        inv.setItem(slot, bg);
                        continue;
                    }
                    Enchantment ench = applicable.get(i);
                    int curLvl = eq.getItemMeta() != null ? eq.getItemMeta().getEnchantLevel(ench) : 0;
                    int maxAllowed = eqManager.getMaxAllowedEnchantLevel(ench);
                    double cost = eqManager.getEnchantUpgradeCostV(ench, curLvl + 1);
                    boolean isSel = ench.equals(session.getSelectedEnchantment());
                    String enchName = plugin.getExcellentEnchantsHook().getEnchantDisplayName(ench);

                    inv.setItem(slot, new ItemBuilder(Material.ENCHANTED_BOOK)
                            .name("&d" + enchName + " &f[Cấp " + curLvl + "/" + maxAllowed + "]")
                            .lore(
                                    "&7Cấp hiện tại: &e" + curLvl + " &8▸ &aCấp " + (curLvl + 1),
                                    "&7Chi phí 100% tỉ lệ: &6" + TextUtil.formatValue(cost) + " V",
                                    "",
                                    curLvl >= maxAllowed
                                            ? "&6&l★ ĐÃ ĐẠT CẤP TỐI ĐA!"
                                            : (isSel ? "&a&l✔ ĐANG CHỌN NÂNG CẤP" : "&e▸ Chạm để chọn phù phép này")
                            )
                            .glow(isSel)
                            .pdc(enchantKeyTag, ench.getKey().toString())
                            .build());
                }
            }
        }

        // --- HÀNG 6: ĐIỀU HƯỚNG ---
        ItemStack returnMatsBtn = new ItemBuilder(Material.HOPPER)
                .name("&c&l↩ Thu hồi Trang bị & Nguyên liệu")
                .lore("&7Trả toàn bộ trang bị và nguyên liệu về túi đồ.")
                .build();
        inv.setItem(47, returnMatsBtn);
        inv.setItem(48, returnMatsBtn);

        inv.setItem(49, new ItemBuilder(Material.BUNDLE)
                .name("&f&lNGUYÊN LIỆU - &6&l" + TextUtil.formatValue(matV) + " V")
                .lore(
                        "&7Điểm nguyên liệu đã nạp: &6" + TextUtil.formatValue(matV) + " V",
                        "&7Điểm yêu cầu (100%): &e" + TextUtil.formatValue(requiredV) + " V"
                )
                .build());

        ItemStack backMainBtn = new ItemBuilder(Material.ARROW)
                .name("&e&l🎲 Về Đấu Giá Nâng Cấp Vật Phẩm")
                .lore("&7Chuyển sang giao diện Đấu Giá Vật Phẩm 3x3.")
                .build();
        inv.setItem(52, backMainBtn);
        inv.setItem(53, backMainBtn);
    }

    private void renderEquipmentWheel(UpgraderSession session, Inventory inv, double chance,
                                      double requiredV, double matV, double pityBonus) {
        UpgraderSession.SpinState state = session.getSpinState();
        double displayChance = (state == UpgraderSession.SpinState.IDLE) ? chance : session.getLockedSuccessChance();
        int activeSegments = (int) Math.round((displayChance / 100.0) * WHEEL_RING_SLOTS.length);
        if (displayChance > 0 && activeSegments == 0) activeSegments = 1;

        for (int i = 0; i < WHEEL_RING_SLOTS.length; i++) {
            int slot = WHEEL_RING_SLOTS[i];
            boolean isWinArc = i < activeSegments;
            boolean isPointer = (state != UpgraderSession.SpinState.IDLE) && (session.getCurrentWheelIndex() == i);
            Material mat = isPointer ? Material.WHITE_STAINED_GLASS_PANE
                    : (isWinArc ? Material.YELLOW_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE);
            inv.setItem(slot, new ItemBuilder(mat)
                    .name(isPointer ? "&e&l▼ KIM QUAY ▼" : (isWinArc ? "&eVùng Thành Công" : "&8Vùng Thất Bại"))
                    .glow(isPointer)
                    .build());
        }

        inv.setItem(WHEEL_CENTER_SLOT, new ItemBuilder(Material.CLOCK)
                .name("&a&l" + TextUtil.formatPercent(displayChance) + "%")
                .lore(
                        state == UpgraderSession.SpinState.SPINNING ? "&e&lĐANG CƯỜNG HÓA..." : "&7Tỉ lệ cường hóa thành công",
                        "",
                        "&7Nguyên liệu: &6" + TextUtil.formatValue(matV) + " / " + TextUtil.formatValue(requiredV) + " V",
                        "&7Bảo hiểm may mắn: &a+" + TextUtil.formatPercent(pityBonus) + "%"
                )
                .glow(displayChance > 0)
                .build());
    }

    public double calculateRequiredCostV(UpgraderSession session) {
        ItemStack eq = session.getEquipmentItem();
        if (eq == null || eq.getType().isAir()) return 500.0;
        EquipmentManager eqManager = plugin.getEquipmentManager();
        if (!session.isEnchantUpgradeTab()) {
            UpgradeAttribute attr = session.getSelectedAttribute();
            int curLvl = eqManager.getAttributeLevel(eq, attr);
            return eqManager.getAttributeUpgradeCostV(curLvl + 1);
        } else {
            Enchantment ench = session.getSelectedEnchantment();
            if (ench == null) return 600.0;
            int curLvl = eq.getItemMeta() != null ? eq.getItemMeta().getEnchantLevel(ench) : 0;
            return eqManager.getEnchantUpgradeCostV(ench, curLvl + 1);
        }
    }

    public boolean canExecuteUpgrade(UpgraderSession session) {
        ItemStack eq = session.getEquipmentItem();
        if (eq == null || !plugin.getEquipmentManager().isUpgradableEquipment(eq)) {
            return false;
        }
        if (!session.isEnchantUpgradeTab()) {
            return plugin.getEquipmentManager().canUpgradeAttribute(eq, session.getSelectedAttribute());
        } else {
            Enchantment ench = session.getSelectedEnchantment();
            if (ench == null) return false;
            int curLvl = eq.getItemMeta() != null ? eq.getItemMeta().getEnchantLevel(ench) : 0;
            return curLvl < plugin.getEquipmentManager().getMaxAllowedEnchantLevel(ench);
        }
    }

    public double calculateUpgradeChance(UpgraderSession session, PlayerProfile profile) {
        if (!canExecuteUpgrade(session)) return 0.0;
        double matV = session.getTotalMaterialValue();
        if (matV <= 0.0) return 0.0;
        double reqV = calculateRequiredCostV(session);
        double baseChance = (matV / Math.max(1.0, reqV)) * 100.0;
        double total = baseChance + profile.getPityBonus();
        return Math.max(0.0, Math.min(plugin.getConfigManager().getMaxSuccessChance(), total));
    }

    public void startEquipmentUpgrade(Player player, UpgraderSession session) {
        if (session.getSpinState() == UpgraderSession.SpinState.SPINNING) return;
        PlayerProfile profile = plugin.getProfileManager().getProfile(player);
        if (!canExecuteUpgrade(session) || session.getTotalMaterialValue() <= 0.0) {
            player.sendMessage(TextUtil.color(plugin.getConfigManager().getPrefix()
                    + "&cVui lòng đặt Trang bị hợp lệ, nạp Nguyên liệu và chọn dòng thuộc tính chưa đạt giới hạn!"));
            session.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        double matV = session.getTotalMaterialValue();
        double chance = calculateUpgradeChance(session, profile);
        double rolled = ThreadLocalRandom.current().nextDouble(0.0, 100.0);
        boolean win = rolled <= chance && chance > 0.0;

        session.clearMaterialsWithoutReturn();
        session.setLockedSuccessChance(chance);
        session.setLastRolledNumber(rolled);

        if (!profile.isSpinAnimation()) {
            finishEquipmentUpgrade(player, session, matV, chance, rolled, win);
            return;
        }

        session.setSpinState(UpgraderSession.SpinState.SPINNING);
        final int totalSteps = 14;
        BukkitRunnable task = new BukkitRunnable() {
            int step = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    session.setSpinTask(null);
                    finishEquipmentUpgrade(player, session, matV, chance, rolled, win);
                    return;
                }
                session.setCurrentWheelIndex(step % WHEEL_RING_SLOTS.length);
                session.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 0.9f + (0.07f * step));
                if (session.getCurrentMenu() == UpgraderSession.MenuType.EQUIPMENT_UPGRADER && session.getActiveInventory() != null) {
                    render(player, session, session.getActiveInventory());
                }
                if (step >= totalSteps) {
                    cancel();
                    session.setSpinTask(null);
                    finishEquipmentUpgrade(player, session, matV, chance, rolled, win);
                    return;
                }
                step++;
            }
        };
        session.setSpinTask(task.runTaskTimer(plugin, 2L, 2L));
    }

    private void finishEquipmentUpgrade(Player player, UpgraderSession session, double matV,
                                        double chance, double rolled, boolean win) {
        session.setSpinState(UpgraderSession.SpinState.IDLE);
        PlayerProfile profile = plugin.getProfileManager().getProfile(player);
        ItemStack eq = session.getEquipmentItem();
        if (eq == null || eq.getType().isAir()) return;

        profile.recordResult(win, matV, win ? (matV * 2) : 0.0);

        if (win) {
            profile.setPityBonus(0.0);
            if (!session.isEnchantUpgradeTab()) {
                UpgradeAttribute attr = session.getSelectedAttribute();
                plugin.getEquipmentManager().upgradeAttribute(eq, attr);
                int newLvl = plugin.getEquipmentManager().getAttributeLevel(eq, attr);
                player.sendMessage(TextUtil.color(plugin.getConfigManager().getPrefix()
                        + "&a&lCƯỜNG HÓA THÀNH CÔNG! &fDòng " + plugin.getEquipmentManager().getAttributeDisplayName(attr)
                        + " &fđã tăng lên &eCấp " + newLvl + " &7(Tỉ lệ: &a" + TextUtil.formatPercent(chance) + "%&7)!"));
            } else {
                Enchantment ench = session.getSelectedEnchantment();
                if (ench != null) {
                    plugin.getEquipmentManager().upgradeEnchantment(eq, ench);
                    int newLvl = eq.getItemMeta().getEnchantLevel(ench);
                    player.sendMessage(TextUtil.color(plugin.getConfigManager().getPrefix()
                            + "&a&lCƯỜNG HÓA PHÙ PHÉP THÀNH CÔNG! &d"
                            + plugin.getExcellentEnchantsHook().getEnchantDisplayName(ench)
                            + " &fđã đạt &eCấp " + newLvl + "&f!"));
                }
            }
            session.playSound(player, Sound.BLOCK_SMITHING_TABLE_USE, 1.0f, 1.2f);
            session.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.4f);
        } else {
            double addPity = plugin.getConfig().getDouble("equipment-upgrader.pity-bonus-per-fail", 3.5);
            double maxPity = plugin.getConfig().getDouble("equipment-upgrader.max-pity-bonus", 35.0);
            double newPity = Math.min(maxPity, profile.getPityBonus() + addPity);
            profile.setPityBonus(newPity);
            session.playSound(player, Sound.BLOCK_ANVIL_LAND, 0.7f, 0.6f);
            player.sendMessage(TextUtil.color(plugin.getConfigManager().getPrefix()
                    + "&c&lCƯỜNG HÓA THẤT BẠI! &7Trang bị của bạn được &aGIỮ NGUYÊN AN TOÀN &7và nhận thêm &e+"
                    + TextUtil.formatPercent(addPity) + "% Bảo Hiểm May Mắn &7(Hiện có: &a+"
                    + TextUtil.formatPercent(newPity) + "%&7)!"));
        }

        plugin.getProfileManager().saveAll();
        if (player.isOnline() && session.getCurrentMenu() == UpgraderSession.MenuType.EQUIPMENT_UPGRADER
                && session.getActiveInventory() != null) {
            render(player, session, session.getActiveInventory());
        }
    }

    public Enchantment resolveEnchantFromKey(String keyStr) {
        if (keyStr == null) return null;
        NamespacedKey key = NamespacedKey.fromString(keyStr);
        if (key == null) return null;
        return Registry.ENCHANTMENT.get(key);
    }
}
