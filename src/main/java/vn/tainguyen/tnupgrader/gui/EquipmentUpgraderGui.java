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
import vn.tainguyen.tnupgrader.hook.ExcellentEnchantsHook;
import vn.tainguyen.tnupgrader.manager.EquipmentManager;
import vn.tainguyen.tnupgrader.model.NativeEnchantment;
import vn.tainguyen.tnupgrader.model.PlayerProfile;
import vn.tainguyen.tnupgrader.model.UiTheme;
import vn.tainguyen.tnupgrader.model.UpgradeAttribute;
import vn.tainguyen.tnupgrader.util.ItemBuilder;
import vn.tainguyen.tnupgrader.util.TextUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class EquipmentUpgraderGui {

    public static final int EQUIPMENT_SLOT = 10;
    public static final int[] MATERIAL_SLOTS = {19, 20, 28, 29};
    public static final int[] WHEEL_RING_SLOTS = MainUpgraderGui.WHEEL_RING_SLOTS;
    public static final int WHEEL_CENTER_SLOT = 13;

    public static final int ATTR_TAB_SLOT = 15;
    public static final int ENCHANT_TAB_SLOT = 16;
    public static final int RUNE_TAB_SLOT = 17;

    public static final int RESET_ATTR_SLOT = 24;
    public static final int EXECUTE_UPGRADE_SLOT = 25;
    public static final int PITY_INFO_SLOT = 26;

    public static final int ENCHANT_PREV_PAGE_SLOT = 45;
    public static final int ENCHANT_NEXT_PAGE_SLOT = 46;

    private static final String[] PREVIEW_ENCHANT_KEYS = {
            "efficiency",     // 1. Hiệu Suất (Đào Nhanh)
            "unbreaking",     // 2. Chậm Hỏng (Tăng Độ Bền)
            "fortune",        // 3. Gia Tài (Nhân Khoáng Sản)
            "mending",        // 4. Sửa Chữa (Tự Hồi Độ Bền)
            "silk_touch",     // 5. Chạm Nhẹ (Đào Nguyên Khối)
            "aqua_affinity",  // 6. Đào Nhanh Dưới Nước
            "sharpness",      // 7. Sắc Bén (Tăng Sát Thương)
            "looting",        // 8. Nhặt Đồ (Tăng Rơi Đồ)
            "protection"      // 9. Bảo Vệ Toàn Diện
    };

    private final TNUpgraderPlugin plugin;
    private final NamespacedKey enchantKeyTag;
    private final NamespacedKey runeKeyTag;

    public EquipmentUpgraderGui(TNUpgraderPlugin plugin) {
        this.plugin = plugin;
        this.enchantKeyTag = new NamespacedKey(plugin, "equip_enchant_key");
        this.runeKeyTag = new NamespacedKey(plugin, "equip_rune_key");
    }

    public NamespacedKey getEnchantKeyTag() {
        return enchantKeyTag;
    }

    public NamespacedKey getRuneKeyTag() {
        return runeKeyTag;
    }

    public void open(Player player, UpgraderSession session) {
        session.setTransitioning(true);
        session.setCurrentMenu(UpgraderSession.MenuType.EQUIPMENT_UPGRADER);

        PlayerProfile profile = plugin.getProfileManager().getProfile(player);
        UiTheme theme = profile.getTheme();

        Component title = TextUtil.color("&8&l✦ " + theme.getAccentColor() + "&lCƯỜNG HÓA TRANG BỊ & CỔ NGỮ &8&l✦");
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
        ExcellentEnchantsHook enchHook = plugin.getExcellentEnchantsHook();

        ItemStack bg = new ItemBuilder(theme.getBackgroundGlass()).name(" ").hideFlags().build();
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, bg);
        }

        // --- HÀNG 1: HƯỚNG DẪN TỪNG BƯỚC RÕ RÀNG ---
        inv.setItem(0, new ItemBuilder(Material.KNOWLEDGE_BOOK)
                .name("&6&l📖 HƯỚNG DẪN CƯỜNG HÓA (RẤT DỄ)")
                .lore(
                        "&fHệ thống Cường Hóa Độc Quyền của &eTN-UpgraderItems",
                        "&f(Hoạt động độc lập 100% cho Công cụ, Vũ khí, Giáp):",
                        "",
                        "&e▸ Bước 1: &fChạm vào &bCuốc, Rìu, Xẻng, Kiếm, Giáp",
                        "&f  dưới túi đồ để đặt lên &bÔ Trang Bị (Ô 10)&f.",
                        "&e▸ Bước 2: &fChọn 1 trong 3 nhánh nâng cấp:",
                        "&f  • &6Ô 15 (Tab 1): &fThuộc Tính Cường Hóa (Đào nhanh, Máu...)",
                        "&f  • &dÔ 16 (Tab 2): &fPhù Phép (Hiệu Suất, Chậm Hỏng, Gia Tài...)",
                        "&f  • &bÔ 17 (Tab 3): &fCổ Ngữ Độc Quyền (Đào Mạch Quặng, 3x3...)",
                        "&e▸ Bước 3: &fChạm vào dòng muốn nâng ở hàng dưới cùng",
                        "&f  rồi nạp Khoáng sản / Đá Cường Hóa vào 4 ô nguyên liệu.",
                        "&e▸ Bước 4: &fBấm nút &6&l⚡ CƯỜNG HÓA NGAY &fđể quay!"
                )
                .glow(true)
                .build());

        inv.setItem(7, new ItemBuilder(Material.GOLD_INGOT)
                .name("&e&l🎲 CHUYỂN VỀ: ĐẤU GIÁ VẬT PHẨM")
                .lore(
                        "&7Chuyển sang bàn &6Đấu Giá Nâng Đời Vật Phẩm",
                        "&7(Đặt cược 3x3 đổi mọi vật phẩm & bảo vật).",
                        "",
                        "&e▸ Chạm vào đây để chuyển chế độ!"
                )
                .glow(true)
                .build());

        inv.setItem(8, new ItemBuilder(Material.BARRIER)
                .name("&c&l✕ Đóng Giao Diện")
                .lore("&7Trang bị và nguyên liệu sẽ tự động trả về túi đồ.")
                .build());

        // --- Ô TRANG BỊ (SLOT 10) ---
        ItemStack eq = session.getEquipmentItem();
        if (eq != null && !eq.getType().isAir()) {
            int stars = eqManager.getTotalUpgradeStars(eq);
            Map<UpgradeAttribute, Integer> attrs = eqManager.getAllAttributes(eq);
            Map<NativeEnchantment, Integer> runes = eqManager.getAllNativeEnchants(eq);
            ItemStack eqDisplay = new ItemBuilder(eq.clone())
                    .addLore(
                            "",
                            "&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                            "&7Cấp sao tổng: &e+" + stars + " ⭐",
                            "&7Thuộc tính đã ép: &a" + attrs.size() + "&7/&6" + eqManager.getMaxCustomAttributes() + " dòng",
                            "&7Cổ ngữ độc quyền: &d" + runes.size() + "&7/&5" + eqManager.getMaxNativeEnchants() + " dòng",
                            "&c▸ Chạm vào đây để lấy trang bị về lại túi đồ"
                    )
                    .build();
            inv.setItem(EQUIPMENT_SLOT, eqDisplay);
        } else {
            inv.setItem(EQUIPMENT_SLOT, new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE)
                    .name("&b&l⬆ Ô ĐẶT TRANG BỊ CẦN NÂNG CẤP ⬆")
                    .lore(
                            "&7Hiện tại: &cChưa đặt trang bị",
                            "",
                            "&e▸ Cách đặt trang bị (PC & Điện thoại PE):",
                            "&f  Bạn chỉ cần &echạm 1 lần &fvào &bCuốc chim, Rìu,",
                            "&f  &bXẻng, Cuốc, Kiếm, Cung, Giáp, Cánh Elytra...",
                            "&f  ở dưới túi đồ là món đồ tự động nhảy lên đây!"
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
                                "&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                                "&7Điểm nguyên liệu: &6+" + TextUtil.formatValue(val) + " V",
                                "&c▸ Chạm vào đây để rút nguyên liệu về túi"
                        )
                        .build());
            } else {
                inv.setItem(slot, new ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                        .name("&eÔ Nạp Nguyên Liệu #" + (i + 1))
                        .lore(
                                "&7Dùng để nạp điểm &6(V) &7tăng tỉ lệ thành công.",
                                "",
                                "&e▸ Cách nạp nguyên liệu:",
                                "&f  Sau khi đã đặt Trang Bị ở ô trên, hãy chạm vào",
                                "&f  &6Sắt, Vàng, Kim cương, Đá Cường Hóa Tinh Anh...",
                                "&f  trong túi đồ để đưa vào ô này!"
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

        // --- 3 NÚT CHUYỂN TAB ĐỘC LẬP, RÕ RÀNG (SLOTS 15, 16, 17) ---
        UpgraderSession.UpgradeTab tab = session.getUpgradeTab();

        // Slot 15: Tab 1 - Thuộc Tính Cường Hóa
        boolean isAttrTab = tab == UpgraderSession.UpgradeTab.ATTRIBUTES;
        inv.setItem(ATTR_TAB_SLOT, new ItemBuilder(Material.NETHER_STAR)
                .name((isAttrTab ? "&a&l✔ " : "&7") + "&6&lTAB 1: THUỘC TÍNH CƯỜNG HÓA")
                .lore(
                        isAttrTab ? "&a(Đang mở bảng Thuộc Tính ở hàng dưới)" : "&e(Chạm để chuyển sang bảng Thuộc Tính)",
                        "",
                        "&7Ép tối đa &6" + eqManager.getMaxCustomAttributes() + " dòng chỉ số gốc &7lên trang bị:",
                        "&f• &e⛏ Tốc Đánh & Đào Nhanh (+25% Tốc đào/cấp)",
                        "&f• &c⚔ Sát Thương &8| &d❤ Sinh Lực &8| &b🛡 Giáp",
                        "&f• &4🩸 Hút Máu &8| &6💥 Chí Mạng &8| &5🔥 Đam Chuẩn",
                        "",
                        isAttrTab ? "&a▸ Bạn đang ở Tab này!" : "&e▸ Chạm vào đây để mở Tab Thuộc Tính!"
                )
                .glow(isAttrTab)
                .build());

        // Slot 16: Tab 2 - Phù Phép Cơ Bản (Hiệu Suất, Chậm Hỏng, Đào Nhanh, Gia Tài...)
        boolean isEnchantTab = tab == UpgraderSession.UpgradeTab.VANILLA_ENCHANTS;
        inv.setItem(ENCHANT_TAB_SLOT, new ItemBuilder(Material.ENCHANTED_BOOK)
                .name((isEnchantTab ? "&a&l✔ " : "&7") + "&d&lTAB 2: PHÙ PHÉP (ENCHANT)")
                .lore(
                        isEnchantTab ? "&a(Đang mở bảng Phù Phép ở hàng dưới)" : "&e(Chạm để chuyển sang bảng Phù Phép)",
                        "",
                        "&7Nâng cấp vượt giới hạn các dòng &dPhù Phép&7:",
                        "&f• &e⛏ Hiệu Suất (Đào Nhanh) &7- Đào đá & quặng siêu tốc",
                        "&f• &b🛡 Chậm Hỏng (Độ Bền) &7- Giảm hao mòn, lâu hư đồ",
                        "&f• &a💎 Gia Tài &8| &2💚 Sửa Chữa &8| &f✨ Chạm Nhẹ",
                        "&f• &c⚔ Sắc Bén &8| &b🛡 Bảo Vệ &8| &3🌊 Đào Nhanh Dưới Nước",
                        "",
                        isEnchantTab ? "&a▸ Bạn đang ở Tab này!" : "&e▸ Chạm vào đây để mở Tab Phù Phép!"
                )
                .glow(isEnchantTab)
                .build());

        // Slot 17: Tab 3 - Cổ Ngữ Độc Quyền TN-Upgrader (Không cần plugin ngoài!)
        boolean isRuneTab = tab == UpgraderSession.UpgradeTab.NATIVE_RUNES;
        inv.setItem(RUNE_TAB_SLOT, new ItemBuilder(Material.ECHO_SHARD)
                .name((isRuneTab ? "&a&l✔ " : "&7") + "&b&lTAB 3: CỔ NGỮ ĐỘC QUYỀN TN")
                .lore(
                        isRuneTab ? "&a(Đang mở bảng Cổ Ngữ Độc Quyền ở hàng dưới)" : "&e(Chạm để chuyển sang bảng Cổ Ngữ Độc Quyền)",
                        "",
                        "&7Hệ thống &b12 Kỹ Năng Nội Tại Độc Quyền &7của plugin:",
                        "&f• &b⛏ Mũi Khoan Địa Tâm &7(Đào cả mạch quặng & chặt cây)",
                        "&f• &e🔨 Búa Thần Khai Sơn &7(Đào rộng 3x3 khối đá/đất)",
                        "&f• &6🔥 Lò Nung Tự Động &8| &d🧲 Từ Trường Hút Đồ Vào Túi",
                        "&f• &e⚡ Lôi Phạt Sấm Sét &8| &b❄ Hàn Băng &8| &d💚 Tái Tạo Máu",
                        "",
                        isRuneTab ? "&a▸ Bạn đang ở Tab này!" : "&e▸ Chạm vào đây để mở Tab Cổ Ngữ Độc Quyền!"
                )
                .glow(isRuneTab)
                .build());

        // Slot 24: Tẩy luyện thuộc tính & cổ ngữ
        inv.setItem(RESET_ATTR_SLOT, new ItemBuilder(Material.GRINDSTONE)
                .name("&c&l♻ Tẩy Luyện Thuộc Tính & Cổ Ngữ")
                .lore(
                        "&7Công dụng: Xóa các dòng Thuộc Tính và Cổ Ngữ",
                        "&7đã ép trên trang bị để bạn chọn lại bộ mới",
                        "&7(Giữ nguyên các dòng Phù Phép/Enchant cơ bản).",
                        "",
                        "&e▸ Giữ Shift + Click chuột để tẩy luyện!"
                )
                .build());

        // Slot 26: Điểm Bảo Hiểm May Mắn (Pity)
        double addPity = plugin.getConfig().getDouble("equipment-upgrader.pity-bonus-per-fail", 3.5);
        double maxPity = plugin.getConfig().getDouble("equipment-upgrader.max-pity-bonus", 35.0);
        inv.setItem(PITY_INFO_SLOT, new ItemBuilder(Material.TOTEM_OF_UNDYING)
                .name("&a&l🍀 Bảo Hiểm May Mắn: &e+" + TextUtil.formatPercent(pityBonus) + "%")
                .lore(
                        "&7Cơ chế bảo vệ trang bị người chơi:",
                        "&f• Khi nâng cấp thất bại, trang bị của bạn",
                        "&f  &a&lGIỮ NGUYÊN 100% &f(không bị vỡ hay tụt cấp!).",
                        "&f• Đồng thời bạn được cộng thêm &e+" + TextUtil.formatPercent(addPity) + "%",
                        "&f  tỉ lệ may mắn cho lần đập tiếp theo!",
                        "",
                        "&7Đang tích lũy: &a+" + TextUtil.formatPercent(pityBonus) + "% &7(Tối đa +" + TextUtil.formatPercent(maxPity) + "%)"
                )
                .glow(pityBonus > 0)
                .build());

        // Slot 25: Nút CƯỜNG HÓA NGAY
        boolean canUpgradeNow = canExecuteUpgrade(session) && matV > 0;
        String selectedTargetName = getSelectedUpgradeDisplayName(session);
        inv.setItem(EXECUTE_UPGRADE_SLOT, new ItemBuilder(canUpgradeNow ? Material.GOLD_BLOCK : Material.ANVIL)
                .name(canUpgradeNow ? "&6&l⚡ BẤM ĐỂ CƯỜNG HÓA NGAY ⚡" : "&e&lCƯỜNG HÓA &7(Chưa đủ điều kiện)")
                .lore(
                        "&7Mục đang chọn: &f" + selectedTargetName,
                        "&7Nguyên liệu đã nạp: &6" + TextUtil.formatValue(matV) + " V &7/ Cần &e" + TextUtil.formatValue(requiredV) + " V",
                        "&7Bảo hiểm may mắn: &a+" + TextUtil.formatPercent(pityBonus) + "%",
                        "&7Tổng tỉ lệ thành công: &a&l" + TextUtil.formatPercent(chance) + "%",
                        "",
                        "&7Nếu thất bại: &aTrang bị giữ nguyên an toàn!",
                        "",
                        canUpgradeNow
                                ? "&6&l▸ CHẠM VÀO ĐÂY ĐỂ BẮT ĐẦU QUAY!"
                                : "&c▸ Hãy đặt Trang bị (Ô 10) & nạp Nguyên liệu (Ô 19,20,28,29)!"
                )
                .glow(canUpgradeNow)
                .build());

        // --- HÀNG 5 (SLOTS 36..44): HIỂN THỊ THEO TAB ĐANG CHỌN ---
        switch (tab) {
            case ATTRIBUTES -> renderAttributeRow(session, inv, eq, eqManager);
            case VANILLA_ENCHANTS -> renderEnchantmentRow(session, inv, eq, eqManager, enchHook, bg);
            case NATIVE_RUNES -> renderNativeRuneRow(session, inv, eq, eqManager, bg);
        }

        // --- HÀNG 6: ĐIỀU HƯỚNG ---
        ItemStack returnMatsBtn = new ItemBuilder(Material.HOPPER)
                .name("&c&l↩ Rút Toàn Bộ Đồ Về Túi")
                .lore("&7Trả ngay Trang bị và Nguyên liệu đang đặt về túi đồ.")
                .build();
        inv.setItem(47, returnMatsBtn);
        inv.setItem(48, returnMatsBtn);

        inv.setItem(49, new ItemBuilder(Material.BUNDLE)
                .name("&f&lĐIỂM NGUYÊN LIỆU: &6&l" + TextUtil.formatValue(matV) + " V")
                .lore(
                        "&7Điểm nguyên liệu đã nạp: &6" + TextUtil.formatValue(matV) + " V",
                        "&7Điểm để đạt 100% tỉ lệ: &e" + TextUtil.formatValue(requiredV) + " V",
                        "",
                        "&7(Nạp càng nhiều khoáng sản / Đá Cường Hóa, tỉ lệ càng cao!)"
                )
                .build());

        ItemStack backMainBtn = new ItemBuilder(Material.ARROW)
                .name("&e&l🎲 Về Bàn Đấu Giá Vật Phẩm 3x3")
                .lore("&7Chuyển sang giao diện Đấu Giá Nâng Đời Vật Phẩm.")
                .build();
        inv.setItem(52, backMainBtn);
        inv.setItem(53, backMainBtn);
    }

    private void renderAttributeRow(UpgraderSession session, Inventory inv, ItemStack eq, EquipmentManager eqManager) {
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
            List<String> lore = new ArrayList<>();
            lore.add("&7Công dụng: &f" + eqManager.getAttributeDescription(attr));
            if (attr == UpgradeAttribute.ATTACK_SPEED) {
                lore.add("&a✦ Hiệu ứng kép: &fVừa đánh nhanh vừa &eĐào Nhanh +25%/cấp&f!");
            }
            lore.add("");
            lore.add("&7Tăng mỗi cấp: &a+" + TextUtil.formatValue(perLvl) + eqManager.getAttributeUnit(attr));
            lore.add("&7Cấp hiện tại: &eCấp " + curLvl + " &8▸ &a&lNâng lên Cấp " + (curLvl + 1));
            lore.add("&7Điểm nguyên liệu cần (100%): &6" + TextUtil.formatValue(nextCost) + " V");
            lore.add("");
            if (maxedOut) {
                lore.add("&6&l★ ĐÃ ĐẠT CẤP ĐỘ TỐI ĐA (" + maxLvl + "/" + maxLvl + ")!");
            } else if (slotFullLocked) {
                lore.add("&c✘ Trang bị đã đủ giới hạn " + maxSlots + " dòng thuộc tính!");
                lore.add("&7(Chỉ có thể nâng tiếp " + maxSlots + " dòng đã có hoặc Tẩy Luyện)");
            } else if (isSelected) {
                lore.add("&a&l✔ ĐANG CHỌN DÒNG NÀY");
                lore.add("&e▸ Hãy nạp nguyên liệu rồi bấm Ô Vàng (Số 25) để đập!");
            } else {
                lore.add("&e▸ Chạm vào đây để chọn nâng cấp dòng này!");
            }

            inv.setItem(slot, new ItemBuilder(icon)
                    .name(eqManager.getAttributeDisplayName(attr) + " &f[Cấp " + curLvl + "/" + maxLvl + "]")
                    .lore(lore)
                    .glow(isSelected)
                    .hideFlags()
                    .build());
        }
    }

    private void renderEnchantmentRow(UpgraderSession session, Inventory inv, ItemStack eq,
                                      EquipmentManager eqManager, ExcellentEnchantsHook enchHook, ItemStack bg) {
        if (eq == null || eq.getType().isAir()) {
            for (int i = 0; i < 9; i++) {
                int slot = 36 + i;
                String keyStr = PREVIEW_ENCHANT_KEYS[i];
                Enchantment ench = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(keyStr));
                if (ench == null) {
                    inv.setItem(slot, bg);
                    continue;
                }
                String name = enchHook.getEnchantDisplayName(ench);
                String desc = enchHook.getEnchantDescription(ench);
                int maxAllowed = eqManager.getMaxAllowedEnchantLevel(ench);
                double baseCost = eqManager.getEnchantUpgradeCostV(ench, 1);

                inv.setItem(slot, new ItemBuilder(Material.ENCHANTED_BOOK)
                        .name("&d&l" + name + " &7(Tối đa Cấp " + maxAllowed + ")")
                        .lore(
                                "&7Công dụng: &f" + desc,
                                "&7Chi phí Cấp 1 (100%): &6" + TextUtil.formatValue(baseCost) + " V",
                                "",
                                "&e&l👉 HƯỚNG DẪN ĐỂ NÂNG CẤP:",
                                "&f  Hãy chạm vào &bCuốc, Xẻng, Rìu, Kiếm hoặc Giáp",
                                "&f  trong túi đồ bên dưới để đặt vào &bÔ Trang Bị (Ô 10)",
                                "&f  trước, sau đó chọn phù phép này để nâng cấp!"
                        )
                        .glow(i < 4)
                        .build());
            }
            return;
        }

        List<Enchantment> applicable = enchHook.getApplicableEnchantments(eq);
        if (applicable.isEmpty()) {
            for (int i = 0; i < 9; i++) {
                inv.setItem(36 + i, new ItemBuilder(Material.BARRIER)
                        .name("&cTrang bị này không hỗ trợ ép Phù Phép")
                        .build());
            }
            return;
        }

        if (session.getSelectedEnchantment() == null || !applicable.contains(session.getSelectedEnchantment())) {
            session.setSelectedEnchantment(applicable.get(0));
        }

        int perPage = 9;
        int totalPages = Math.max(1, (int) Math.ceil((double) applicable.size() / perPage));
        if (session.getEnchantPage() >= totalPages) {
            session.setEnchantPage(0);
        }
        int page = session.getEnchantPage();
        int startIndex = page * perPage;

        for (int i = 0; i < 9; i++) {
            int slot = 36 + i;
            int idx = startIndex + i;
            if (idx >= applicable.size()) {
                inv.setItem(slot, bg);
                continue;
            }
            Enchantment ench = applicable.get(idx);
            int curLvl = eq.getItemMeta() != null ? eq.getItemMeta().getEnchantLevel(ench) : 0;
            int maxAllowed = eqManager.getMaxAllowedEnchantLevel(ench);
            double cost = eqManager.getEnchantUpgradeCostV(ench, curLvl + 1);
            boolean isSel = ench.equals(session.getSelectedEnchantment());
            String enchName = enchHook.getEnchantDisplayName(ench);
            String desc = enchHook.getEnchantDescription(ench);
            String effectNext = enchHook.getEnchantEffectSummary(ench, curLvl + 1);

            inv.setItem(slot, new ItemBuilder(Material.ENCHANTED_BOOK)
                    .name("&d&l" + enchName + " &f[Cấp " + curLvl + "/" + maxAllowed + "]")
                    .lore(
                            "&7Công dụng: &f" + desc,
                            "&7Khi lên cấp " + (curLvl + 1) + ": &a" + effectNext,
                            "",
                            "&7Cấp hiện tại trên đồ: &eCấp " + curLvl + " &8▸ &a&lNâng lên Cấp " + (curLvl + 1),
                            "&7Điểm nguyên liệu cần (100%): &6" + TextUtil.formatValue(cost) + " V",
                            "",
                            curLvl >= maxAllowed
                                    ? "&6&l★ ĐÃ ĐẠT CẤP PHÙ PHÉP TỐI ĐA!"
                                    : (isSel
                                    ? "&a&l✔ ĐANG CHỌN PHÙ PHÉP NÀY\n&e▸ Hãy nạp nguyên liệu rồi bấm Ô Vàng (Số 25) để đập!"
                                    : "&e▸ Chạm vào đây để chọn nâng cấp phù phép này!")
                    )
                    .glow(isSel)
                    .pdc(enchantKeyTag, ench.getKey().toString())
                    .build());
        }

        if (totalPages > 1) {
            inv.setItem(ENCHANT_PREV_PAGE_SLOT, new ItemBuilder(Material.SPECTRAL_ARROW)
                    .name("&e‹ Trang Trước &7(" + (page + 1) + "/" + totalPages + ")")
                    .lore("&7Xem các phù phép trước đó.")
                    .build());
            inv.setItem(ENCHANT_NEXT_PAGE_SLOT, new ItemBuilder(Material.SPECTRAL_ARROW)
                    .name("&eTrang Sau › &7(" + (page + 1) + "/" + totalPages + ")")
                    .lore("&7Xem thêm các phù phép khác (" + applicable.size() + " phù phép).")
                    .build());
        }
    }

    /**
     * Hiển thị Tab 3: Cổ Ngữ Độc Quyền của riêng TN-UpgraderItems (Đào mạch quặng, Đào 3x3, Lò nung tự động, Từ trường hút đồ...)
     */
    private void renderNativeRuneRow(UpgraderSession session, Inventory inv, ItemStack eq,
                                     EquipmentManager eqManager, ItemStack bg) {
        List<NativeEnchantment> listToShow = (eq == null || eq.getType().isAir())
                ? Arrays.asList(NativeEnchantment.values())
                : NativeEnchantment.getApplicable(eq);

        if (listToShow.isEmpty()) {
            for (int i = 0; i < 9; i++) {
                inv.setItem(36 + i, bg);
            }
            return;
        }

        if (session.getSelectedNativeEnchant() == null || !listToShow.contains(session.getSelectedNativeEnchant())) {
            session.setSelectedNativeEnchant(listToShow.get(0));
        }

        int perPage = 9;
        int totalPages = Math.max(1, (int) Math.ceil((double) listToShow.size() / perPage));
        if (session.getEnchantPage() >= totalPages) {
            session.setEnchantPage(0);
        }
        int page = session.getEnchantPage();
        int startIndex = page * perPage;

        Map<NativeEnchantment, Integer> currentRunes = eq != null ? eqManager.getAllNativeEnchants(eq) : Map.of();
        int maxRuneSlots = eqManager.getMaxNativeEnchants();

        for (int i = 0; i < 9; i++) {
            int slot = 36 + i;
            int idx = startIndex + i;
            if (idx >= listToShow.size()) {
                inv.setItem(slot, bg);
                continue;
            }
            NativeEnchantment rune = listToShow.get(idx);
            int curLvl = currentRunes.getOrDefault(rune, 0);
            int maxLvl = rune.getMaxLevel();
            double cost = rune.getUpgradeCostV(curLvl + 1);
            boolean isSel = rune == session.getSelectedNativeEnchant();
            boolean slotFullLocked = (eq != null) && (curLvl == 0) && (currentRunes.size() >= maxRuneSlots);

            if (eq == null || eq.getType().isAir()) {
                inv.setItem(slot, new ItemBuilder(rune.getIcon())
                        .name(rune.getDisplayName() + " &7(Tối đa Cấp " + maxLvl + ")")
                        .lore(
                                "&8[Cổ Ngữ Độc Quyền TN-Upgrader]",
                                "&7Dành cho: &e" + rune.getTargetGroup().getLabel(),
                                "&7Công dụng: &f" + rune.getDescription(),
                                "&7Hiệu lực Cấp 1: &a" + rune.formatEffect(1),
                                "&7Chi phí Cấp 1: &6" + TextUtil.formatValue(rune.getUpgradeCostV(1)) + " V",
                                "",
                                "&e&l👉 HƯỚNG DẪN ĐỂ KHẮC CỔ NGỮ:",
                                "&f  Hãy chạm vào &bCông cụ, Vũ khí hoặc Giáp &fdưới túi đồ",
                                "&f  để đặt vào &bÔ Trang Bị (Ô 10) &frồi chọn Cổ Ngữ này!"
                        )
                        .glow(true)
                        .hideFlags()
                        .build());
            } else {
                inv.setItem(slot, new ItemBuilder(slotFullLocked ? Material.BARRIER : rune.getIcon())
                        .name(rune.getDisplayName() + " &f[Cấp " + curLvl + "/" + maxLvl + "]")
                        .lore(
                                "&8[Cổ Ngữ Độc Quyền TN-Upgrader]",
                                "&7Công dụng: &f" + rune.getDescription(),
                                "&7Khi lên Cấp " + (curLvl + 1) + ": &a" + rune.formatEffect(curLvl + 1),
                                "",
                                "&7Cấp hiện tại: &eCấp " + curLvl + " &8▸ &a&lNâng lên Cấp " + (curLvl + 1),
                                "&7Điểm nguyên liệu cần (100%): &6" + TextUtil.formatValue(cost) + " V",
                                "",
                                curLvl >= maxLvl
                                        ? "&6&l★ ĐÃ ĐẠT CẤP CỔ NGỮ TỐI ĐA!"
                                        : (slotFullLocked
                                        ? "&c✘ Đã đạt giới hạn " + maxRuneSlots + " dòng Cổ Ngữ trên 1 món đồ!"
                                        : (isSel
                                        ? "&a&l✔ ĐANG CHỌN CỔ NGỮ NÀY\n&e▸ Hãy nạp nguyên liệu rồi bấm Ô Vàng (Số 25) để đập!"
                                        : "&e▸ Chạm vào đây để chọn khắc Cổ Ngữ này!"))
                        )
                        .glow(isSel)
                        .hideFlags()
                        .pdc(runeKeyTag, rune.name())
                        .build());
            }
        }

        if (totalPages > 1) {
            inv.setItem(ENCHANT_PREV_PAGE_SLOT, new ItemBuilder(Material.SPECTRAL_ARROW)
                    .name("&e‹ Trang Cổ Ngữ Trước &7(" + (page + 1) + "/" + totalPages + ")")
                    .build());
            inv.setItem(ENCHANT_NEXT_PAGE_SLOT, new ItemBuilder(Material.SPECTRAL_ARROW)
                    .name("&eTrang Cổ Ngữ Sau › &7(" + (page + 1) + "/" + totalPages + ")")
                    .build());
        }
    }

    private String getSelectedUpgradeDisplayName(UpgraderSession session) {
        return switch (session.getUpgradeTab()) {
            case ATTRIBUTES -> plugin.getEquipmentManager().getAttributeDisplayName(session.getSelectedAttribute());
            case VANILLA_ENCHANTS -> {
                Enchantment ench = session.getSelectedEnchantment();
                yield ench != null ? "&d" + plugin.getExcellentEnchantsHook().getEnchantDisplayName(ench) : "&7Chưa chọn Phù Phép";
            }
            case NATIVE_RUNES -> {
                NativeEnchantment rune = session.getSelectedNativeEnchant();
                yield rune != null ? rune.getDisplayName() : "&7Chưa chọn Cổ Ngữ";
            }
        };
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
                        "&7Đang chọn: " + getSelectedUpgradeDisplayName(session),
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
        return switch (session.getUpgradeTab()) {
            case ATTRIBUTES -> {
                UpgradeAttribute attr = session.getSelectedAttribute();
                int curLvl = eqManager.getAttributeLevel(eq, attr);
                yield eqManager.getAttributeUpgradeCostV(curLvl + 1);
            }
            case VANILLA_ENCHANTS -> {
                Enchantment ench = session.getSelectedEnchantment();
                if (ench == null) yield 600.0;
                int curLvl = eq.getItemMeta() != null ? eq.getItemMeta().getEnchantLevel(ench) : 0;
                yield eqManager.getEnchantUpgradeCostV(ench, curLvl + 1);
            }
            case NATIVE_RUNES -> {
                NativeEnchantment rune = session.getSelectedNativeEnchant();
                if (rune == null) yield 800.0;
                int curLvl = eqManager.getNativeEnchantLevel(eq, rune);
                yield rune.getUpgradeCostV(curLvl + 1);
            }
        };
    }

    public boolean canExecuteUpgrade(UpgraderSession session) {
        ItemStack eq = session.getEquipmentItem();
        if (eq == null || !plugin.getEquipmentManager().isUpgradableEquipment(eq)) {
            return false;
        }
        return switch (session.getUpgradeTab()) {
            case ATTRIBUTES -> plugin.getEquipmentManager().canUpgradeAttribute(eq, session.getSelectedAttribute());
            case VANILLA_ENCHANTS -> {
                Enchantment ench = session.getSelectedEnchantment();
                if (ench == null) yield false;
                int curLvl = eq.getItemMeta() != null ? eq.getItemMeta().getEnchantLevel(ench) : 0;
                yield curLvl < plugin.getEquipmentManager().getMaxAllowedEnchantLevel(ench);
            }
            case NATIVE_RUNES -> plugin.getEquipmentManager().canUpgradeNativeEnchant(eq, session.getSelectedNativeEnchant());
        };
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
                    + "&cHướng dẫn: Hãy đặt Trang bị (Ô 10), nạp Khoáng sản/Đá Cường Hóa (Ô 19,20,28,29) và chọn dòng nâng cấp hợp lệ!"));
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
            switch (session.getUpgradeTab()) {
                case ATTRIBUTES -> {
                    UpgradeAttribute attr = session.getSelectedAttribute();
                    plugin.getEquipmentManager().upgradeAttribute(eq, attr);
                    int newLvl = plugin.getEquipmentManager().getAttributeLevel(eq, attr);
                    player.sendMessage(TextUtil.color(plugin.getConfigManager().getPrefix()
                            + "&a&lCƯỜNG HÓA THÀNH CÔNG! &fThuộc tính " + plugin.getEquipmentManager().getAttributeDisplayName(attr)
                            + " &fđã tăng lên &eCấp " + newLvl + " &7(Tỉ lệ: &a" + TextUtil.formatPercent(chance) + "%&7)!"));
                }
                case VANILLA_ENCHANTS -> {
                    Enchantment ench = session.getSelectedEnchantment();
                    if (ench != null) {
                        plugin.getEquipmentManager().upgradeEnchantment(eq, ench);
                        int newLvl = eq.getItemMeta().getEnchantLevel(ench);
                        player.sendMessage(TextUtil.color(plugin.getConfigManager().getPrefix()
                                + "&a&lNÂNG CẤP PHÙ PHÉP THÀNH CÔNG! &d"
                                + plugin.getExcellentEnchantsHook().getEnchantDisplayName(ench)
                                + " &fđã đạt &eCấp " + newLvl + " &7("
                                + plugin.getExcellentEnchantsHook().getEnchantEffectSummary(ench, newLvl) + ")&f!"));
                    }
                }
                case NATIVE_RUNES -> {
                    NativeEnchantment rune = session.getSelectedNativeEnchant();
                    if (rune != null) {
                        plugin.getEquipmentManager().upgradeNativeEnchant(eq, rune);
                        int newLvl = plugin.getEquipmentManager().getNativeEnchantLevel(eq, rune);
                        player.sendMessage(TextUtil.color(plugin.getConfigManager().getPrefix()
                                + "&a&lKHẮC CỔ NGỮ ĐỘC QUYỀN THÀNH CÔNG! " + rune.getDisplayName()
                                + " &fđã đạt &eCấp " + newLvl + " &7(" + rune.formatEffect(newLvl) + ")&f!"));
                    }
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
                    + "&c&lCƯỜNG HÓA THẤT BẠI! &7Trang bị của bạn được &aGIỮ NGUYÊN 100% &7và cộng thêm &e+"
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
