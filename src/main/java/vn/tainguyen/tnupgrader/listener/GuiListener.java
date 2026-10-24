package vn.tainguyen.tnupgrader.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import vn.tainguyen.tnupgrader.TNUpgraderPlugin;
import vn.tainguyen.tnupgrader.gui.EquipmentUpgraderGui;
import vn.tainguyen.tnupgrader.gui.MainUpgraderGui;
import vn.tainguyen.tnupgrader.gui.RewardBrowserGui;
import vn.tainguyen.tnupgrader.gui.UpgraderSession;
import vn.tainguyen.tnupgrader.model.PlayerProfile;
import vn.tainguyen.tnupgrader.model.RewardItem;
import vn.tainguyen.tnupgrader.model.UpgradeAttribute;
import vn.tainguyen.tnupgrader.util.TextUtil;

public class GuiListener implements Listener {

    private final TNUpgraderPlugin plugin;

    public GuiListener(TNUpgraderPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        UpgraderSession session = plugin.getSessionManager().get(player.getUniqueId());
        if (session == null || session.getActiveInventory() == null) {
            return;
        }
        if (!event.getView().getTopInventory().equals(session.getActiveInventory())) {
            return;
        }

        event.setCancelled(true);

        // Chặn thao tác khi vòng quay đang chạy
        if (session.getSpinState() == UpgraderSession.SpinState.SPINNING) {
            return;
        }

        Inventory clickedInv = event.getClickedInventory();
        if (clickedInv == null) {
            return;
        }

        // Xử lý khi người chơi chạm/click vào TÚI ĐỒ BÊN DƯỚI (One-Tap cho cả PE & PC)
        if (clickedInv.equals(player.getInventory())) {
            handlePlayerInventoryClick(player, session, event);
            return;
        }

        // Xử lý khi người chơi click vào GIAO DIỆN PHÍA TRÊN
        int slot = event.getSlot();
        switch (session.getCurrentMenu()) {
            case MAIN_UPGRADER -> handleMainGuiClick(player, session, slot, event);
            case REWARD_BROWSER -> handleRewardBrowserClick(player, session, slot, event);
            case EQUIPMENT_UPGRADER -> handleEquipmentGuiClick(player, session, slot, event);
            case SETTINGS -> handleSettingsClick(player, session, slot);
            case STATISTICS, GUIDE -> {
                if (slot == 31 || slot == 22) {
                    session.playSound(player, Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
                    plugin.getMainUpgraderGui().open(player, session);
                }
            }
            case FAVORITES -> handleFavoritesClick(player, session, slot, event);
            default -> {
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        UpgraderSession session = plugin.getSessionManager().get(player.getUniqueId());
        if (session != null && event.getView().getTopInventory().equals(session.getActiveInventory())) {
            event.setCancelled(true);
        }
    }

    private void handlePlayerInventoryClick(Player player, UpgraderSession session, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) {
            return;
        }
        if (plugin.getConfigManager().isUpgraderTool(clicked) || plugin.getConfigManager().isUpgraderBlockItem(clicked)) {
            player.sendMessage(TextUtil.color(plugin.getConfigManager().getPrefix()
                    + "&cKhông thể đặt công cụ/khối nâng cấp làm đồ cược!"));
            return;
        }

        if (session.getCurrentMenu() == UpgraderSession.MenuType.MAIN_UPGRADER) {
            double val = plugin.getConfigManager().getStackValue(clicked);
            if (val <= 0.0) {
                player.sendMessage(TextUtil.color(plugin.getConfigManager().getPrefix()
                        + "&cVật phẩm này không có giá trị đặt cược (0 V)!"));
                session.playSound(player, Sound.ENTITY_VILLAGER_NO, 0.8f, 1.0f);
                return;
            }
            if (session.getSpinState() != UpgraderSession.SpinState.IDLE) {
                session.setSpinState(UpgraderSession.SpinState.IDLE);
            }
            ItemStack copy = clicked.clone();
            if (session.addWagerItem(copy)) {
                if (copy.getAmount() <= 0) {
                    event.setCurrentItem(null);
                } else {
                    clicked.setAmount(copy.getAmount());
                }
                session.playSound(player, Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.3f);
                plugin.getMainUpgraderGui().render(player, session, session.getActiveInventory());
            } else {
                player.sendMessage(TextUtil.color(plugin.getConfigManager().getPrefix()
                        + "&eLưới 3x3 Đồ Cược đã đầy (9/9 ô)!"));
            }
        } else if (session.getCurrentMenu() == UpgraderSession.MenuType.EQUIPMENT_UPGRADER) {
            // Nếu ô Trang bị đang trống và vật phẩm là Vũ khí/Giáp/Công cụ -> Đặt vào ô Trang Bị
            if ((session.getEquipmentItem() == null || session.getEquipmentItem().getType().isAir())
                    && plugin.getEquipmentManager().isUpgradableEquipment(clicked)) {
                session.setEquipmentItem(clicked.clone());
                event.setCurrentItem(null);
                session.playSound(player, Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1.0f, 1.1f);
                plugin.getEquipmentUpgraderGui().render(player, session, session.getActiveInventory());
                return;
            }
            // Ngược lại nạp vào 4 ô Nguyên liệu Cường Hóa
            double val = plugin.getConfigManager().getStackValue(clicked);
            if (val <= 0.0) {
                player.sendMessage(TextUtil.color(plugin.getConfigManager().getPrefix()
                        + "&cVật phẩm này không có điểm nguyên liệu nâng cấp (0 V)!"));
                return;
            }
            ItemStack copy = clicked.clone();
            if (session.addMaterialItem(copy)) {
                if (copy.getAmount() <= 0) {
                    event.setCurrentItem(null);
                } else {
                    clicked.setAmount(copy.getAmount());
                }
                session.playSound(player, Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.3f);
                plugin.getEquipmentUpgraderGui().render(player, session, session.getActiveInventory());
            } else {
                player.sendMessage(TextUtil.color(plugin.getConfigManager().getPrefix()
                        + "&e4 Ô Nguyên Liệu đã đầy!"));
            }
        }
    }

    private void handleMainGuiClick(Player player, UpgraderSession session, int slot, InventoryClickEvent event) {
        PlayerProfile profile = plugin.getProfileManager().getProfile(player);

        // Kiểm tra click vào 9 ô ĐỒ CƯỢC 3x3 để rút đồ về
        for (int i = 0; i < MainUpgraderGui.WAGER_SLOTS.length; i++) {
            if (slot == MainUpgraderGui.WAGER_SLOTS[i]) {
                ItemStack wagered = session.getWagerItems()[i];
                if (wagered != null && !wagered.getType().isAir()) {
                    session.getWagerItems()[i] = null;
                    UpgraderSession.giveItemSafely(player, wagered);
                    session.setSpinState(UpgraderSession.SpinState.IDLE);
                    session.playSound(player, Sound.ENTITY_ITEM_PICKUP, 0.8f, 0.9f);
                    plugin.getMainUpgraderGui().render(player, session, session.getActiveInventory());
                }
                return;
            }
        }

        // Kiểm tra click vào hàng Hệ Số Nhân x1 ▸ x10 (Slots 36..44)
        if (slot >= 36 && slot <= 44) {
            int idx = slot - 36;
            int mult = MainUpgraderGui.MULTIPLIER_VALUES[idx];
            plugin.getMainUpgraderGui().selectByMultiplier(player, session, mult);
            return;
        }

        switch (slot) {
            case 1 -> {
                // Đổi màu giao diện (Theme)
                profile.setTheme(profile.getTheme().next());
                plugin.getProfileManager().saveAll();
                session.playSound(player, Sound.UI_BUTTON_CLICK, 0.8f, 1.3f);
                plugin.getMainUpgraderGui().open(player, session);
            }
            case 2 -> {
                session.playSound(player, Sound.UI_BUTTON_CLICK, 0.8f, 1.1f);
                plugin.getSubMenusGui().openGuide(player, session);
            }
            case 6 -> {
                session.playSound(player, Sound.UI_BUTTON_CLICK, 0.8f, 1.1f);
                plugin.getSubMenusGui().openFavorites(player, session);
            }
            case 7, 52, 53 -> {
                // Chuyển sang Bàn Cường Hóa Trang Bị End-Game
                session.playSound(player, Sound.BLOCK_ANVIL_PLACE, 0.7f, 1.2f);
                plugin.getEquipmentUpgraderGui().open(player, session);
            }
            case 8 -> player.closeInventory();
            case MainUpgraderGui.TARGET_ITEM_SLOT, 45, 46 -> {
                // Mở menu TÌM PHẦN THƯỞNG
                session.playSound(player, Sound.UI_BUTTON_CLICK, 0.9f, 1.1f);
                plugin.getRewardBrowserGui().open(player, session);
            }
            case MainUpgraderGui.FAVORITE_BUTTON_SLOT -> {
                RewardItem target = session.getTargetReward();
                if (target != null) {
                    boolean added = profile.toggleFavorite(target.getId());
                    plugin.getProfileManager().saveAll();
                    session.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, added ? 1.4f : 0.8f);
                    plugin.getMainUpgraderGui().render(player, session, session.getActiveInventory());
                }
            }
            case MainUpgraderGui.UPGRADE_BUTTON_SLOT -> {
                if (session.getSpinState() == UpgraderSession.SpinState.RESULT_WIN
                        || session.getSpinState() == UpgraderSession.SpinState.RESULT_LOSS) {
                    // Nút TIẾP TỤC sau khi quay xong
                    session.setSpinState(UpgraderSession.SpinState.IDLE);
                    session.playSound(player, Sound.UI_BUTTON_CLICK, 0.8f, 1.1f);
                    plugin.getMainUpgraderGui().render(player, session, session.getActiveInventory());
                } else {
                    // Bấm NÂNG CẤP
                    plugin.getMainUpgraderGui().startUpgradeSpin(player, session);
                }
            }
            case MainUpgraderGui.TARGET_QTY_SLOT -> {
                int current = session.getTargetQuantity();
                if (event.isShiftClick()) {
                    session.setTargetQuantity(current >= 10 ? 1 : current + 5);
                } else if (event.isRightClick()) {
                    session.setTargetQuantity(Math.max(1, current - 1));
                } else {
                    session.setTargetQuantity(current + 1);
                }
                session.setSpinState(UpgraderSession.SpinState.IDLE);
                session.playSound(player, Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                plugin.getMainUpgraderGui().render(player, session, session.getActiveInventory());
            }
            case 47, 48 -> {
                // Bỏ cược
                session.returnWagerItems(player);
                session.setSpinState(UpgraderSession.SpinState.IDLE);
                session.playSound(player, Sound.ITEM_ARMOR_EQUIP_GENERIC, 0.9f, 1.0f);
                plugin.getMainUpgraderGui().render(player, session, session.getActiveInventory());
            }
            case 49 -> {
                // Chạm vào TÚI ĐỒ để nạp nhanh 1 món đồ có giá trị từ túi lên lưới 3x3
                ItemStack[] contents = player.getInventory().getStorageContents();
                for (int i = 0; i < contents.length; i++) {
                    ItemStack it = contents[i];
                    if (it != null && !it.getType().isAir() && plugin.getConfigManager().getStackValue(it) > 0) {
                        ItemStack copy = it.clone();
                        if (session.addWagerItem(copy)) {
                            if (copy.getAmount() <= 0) {
                                player.getInventory().setItem(i, null);
                            } else {
                                it.setAmount(copy.getAmount());
                            }
                            session.setSpinState(UpgraderSession.SpinState.IDLE);
                            session.playSound(player, Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.3f);
                            plugin.getMainUpgraderGui().render(player, session, session.getActiveInventory());
                            return;
                        }
                    }
                }
            }
            case 50 -> {
                session.playSound(player, Sound.UI_BUTTON_CLICK, 0.8f, 1.1f);
                plugin.getSubMenusGui().openSettings(player, session);
            }
            case 51 -> {
                session.playSound(player, Sound.UI_BUTTON_CLICK, 0.8f, 1.1f);
                plugin.getSubMenusGui().openStatistics(player, session);
            }
            default -> {
            }
        }
    }

    private void handleRewardBrowserClick(Player player, UpgraderSession session, int slot, InventoryClickEvent event) {
        PlayerProfile profile = plugin.getProfileManager().getProfile(player);

        if (slot == 1 || slot == 2) {
            if (event.isRightClick() || event.isShiftClick()) {
                session.setSearchQuery("");
                session.setBrowserPage(0);
                session.playSound(player, Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
                plugin.getRewardBrowserGui().render(player, session, session.getActiveInventory());
            } else {
                // Nhập từ khóa qua khung Chat
                session.setAwaitingChatSearch(true);
                player.closeInventory();
                player.sendMessage(TextUtil.color(plugin.getConfigManager().getPrefix()
                        + "&eHãy gõ tên vật phẩm cần tìm vào khung chat &7(VD: &fkim&7, &fnetherite&7, &ftáo&7) hoặc gõ &ccancel &7để hủy:"));
                session.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
            }
            return;
        }

        if (slot == 5) {
            session.setCategoryFilter(session.getCategoryFilter().next());
            session.setBrowserPage(0);
            session.playSound(player, Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            plugin.getRewardBrowserGui().render(player, session, session.getActiveInventory());
            return;
        }

        if (slot == 6) {
            session.setSortOrder(session.getSortOrder().next());
            session.setBrowserPage(0);
            session.playSound(player, Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            plugin.getRewardBrowserGui().render(player, session, session.getActiveInventory());
            return;
        }

        if (slot == 8) {
            session.playSound(player, Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
            plugin.getMainUpgraderGui().open(player, session);
            return;
        }

        if (slot == 45 || slot == 46) {
            if (session.getBrowserPage() > 0) {
                session.setBrowserPage(session.getBrowserPage() - 1);
                session.playSound(player, Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
                plugin.getRewardBrowserGui().render(player, session, session.getActiveInventory());
            }
            return;
        }

        if (slot == 52 || slot == 53) {
            int total = session.getFilteredRewards().size();
            int maxPages = Math.max(1, (int) Math.ceil((double) total / RewardBrowserGui.ITEMS_PER_PAGE));
            if (session.getBrowserPage() + 1 < maxPages) {
                session.setBrowserPage(session.getBrowserPage() + 1);
                session.playSound(player, Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.2f);
                plugin.getRewardBrowserGui().render(player, session, session.getActiveInventory());
            }
            return;
        }

        // Kiểm tra click vào vật phẩm phần thưởng trong danh sách
        ItemStack clicked = event.getCurrentItem();
        if (clicked != null && !clicked.getType().isAir() && clicked.hasItemMeta()) {
            PersistentDataContainer pdc = clicked.getItemMeta().getPersistentDataContainer();
            String rewardId = pdc.get(plugin.getRewardBrowserGui().getRewardIdKey(), PersistentDataType.STRING);
            Integer suggestedQty = pdc.get(plugin.getRewardBrowserGui().getRewardQtyKey(), PersistentDataType.INTEGER);
            if (rewardId != null) {
                RewardItem reward = plugin.getConfigManager().getRewardById(rewardId);
                if (reward != null) {
                    if (event.isRightClick()) {
                        profile.toggleFavorite(reward.getId());
                        plugin.getProfileManager().saveAll();
                        session.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.3f);
                        plugin.getRewardBrowserGui().render(player, session, session.getActiveInventory());
                    } else {
                        session.setTargetReward(reward);
                        session.setTargetQuantity(suggestedQty != null ? suggestedQty : 1);
                        session.setSpinState(UpgraderSession.SpinState.IDLE);
                        session.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                        plugin.getMainUpgraderGui().open(player, session);
                    }
                }
            }
        }
    }

    private void handleEquipmentGuiClick(Player player, UpgraderSession session, int slot, InventoryClickEvent event) {
        // Rút Trang Bị từ Slot 10
        if (slot == EquipmentUpgraderGui.EQUIPMENT_SLOT) {
            ItemStack eq = session.getEquipmentItem();
            if (eq != null && !eq.getType().isAir()) {
                session.setEquipmentItem(null);
                UpgraderSession.giveItemSafely(player, eq);
                session.playSound(player, Sound.ENTITY_ITEM_PICKUP, 0.8f, 0.9f);
                plugin.getEquipmentUpgraderGui().render(player, session, session.getActiveInventory());
            }
            return;
        }

        // Rút Nguyên liệu từ 4 ô (19, 20, 28, 29)
        for (int i = 0; i < EquipmentUpgraderGui.MATERIAL_SLOTS.length; i++) {
            if (slot == EquipmentUpgraderGui.MATERIAL_SLOTS[i]) {
                ItemStack mat = session.getMaterialItems()[i];
                if (mat != null && !mat.getType().isAir()) {
                    session.getMaterialItems()[i] = null;
                    UpgraderSession.giveItemSafely(player, mat);
                    session.playSound(player, Sound.ENTITY_ITEM_PICKUP, 0.8f, 0.9f);
                    plugin.getEquipmentUpgraderGui().render(player, session, session.getActiveInventory());
                }
                return;
            }
        }

        // Chọn dòng Thuộc Tính hoặc Phù Phép ở Hàng 5 (Slots 36..44)
        if (slot >= 36 && slot <= 44) {
            int idx = slot - 36;
            if (!session.isEnchantUpgradeTab()) {
                UpgradeAttribute[] attrs = UpgradeAttribute.values();
                if (idx < attrs.length) {
                    session.setSelectedAttribute(attrs[idx]);
                    session.playSound(player, Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                    plugin.getEquipmentUpgraderGui().render(player, session, session.getActiveInventory());
                }
            } else {
                ItemStack clicked = event.getCurrentItem();
                if (clicked != null && clicked.hasItemMeta()) {
                    String keyStr = clicked.getItemMeta().getPersistentDataContainer()
                            .get(plugin.getEquipmentUpgraderGui().getEnchantKeyTag(), PersistentDataType.STRING);
                    Enchantment ench = plugin.getEquipmentUpgraderGui().resolveEnchantFromKey(keyStr);
                    if (ench != null) {
                        session.setSelectedEnchantment(ench);
                        session.playSound(player, Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                        plugin.getEquipmentUpgraderGui().render(player, session, session.getActiveInventory());
                    }
                }
            }
            return;
        }

        switch (slot) {
            case 7, 52, 53 -> {
                session.playSound(player, Sound.UI_BUTTON_CLICK, 0.8f, 1.1f);
                plugin.getMainUpgraderGui().open(player, session);
            }
            case 8 -> player.closeInventory();
            case EquipmentUpgraderGui.TAB_SWITCH_SLOT -> {
                session.setEnchantUpgradeTab(!session.isEnchantUpgradeTab());
                session.playSound(player, Sound.UI_BUTTON_CLICK, 0.8f, 1.3f);
                plugin.getEquipmentUpgraderGui().render(player, session, session.getActiveInventory());
            }
            case EquipmentUpgraderGui.RESET_ATTR_SLOT -> {
                if (event.isShiftClick() && session.getEquipmentItem() != null) {
                    plugin.getEquipmentManager().resetCustomAttributes(session.getEquipmentItem());
                    session.playSound(player, Sound.BLOCK_GRINDSTONE_USE, 1.0f, 1.0f);
                    player.sendMessage(TextUtil.color(plugin.getConfigManager().getPrefix()
                            + "&eĐã tẩy luyện toàn bộ dòng thuộc tính bổ sung trên trang bị!"));
                    plugin.getEquipmentUpgraderGui().render(player, session, session.getActiveInventory());
                }
            }
            case EquipmentUpgraderGui.EXECUTE_UPGRADE_SLOT -> {
                plugin.getEquipmentUpgraderGui().startEquipmentUpgrade(player, session);
            }
            case 47, 48 -> {
                session.returnEquipmentAndMaterials(player);
                session.playSound(player, Sound.ITEM_ARMOR_EQUIP_GENERIC, 0.9f, 1.0f);
                plugin.getEquipmentUpgraderGui().render(player, session, session.getActiveInventory());
            }
            default -> {
            }
        }
    }

    private void handleSettingsClick(Player player, UpgraderSession session, int slot) {
        PlayerProfile profile = plugin.getProfileManager().getProfile(player);
        switch (slot) {
            case 10 -> {
                profile.setSpinAnimation(!profile.isSpinAnimation());
                plugin.getProfileManager().saveAll();
                session.playSound(player, Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                plugin.getSubMenusGui().openSettings(player, session);
            }
            case 12 -> {
                profile.setSoundEnabled(!profile.isSoundEnabled());
                plugin.getProfileManager().saveAll();
                session.playSound(player, Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                plugin.getSubMenusGui().openSettings(player, session);
            }
            case 14 -> {
                profile.setBroadcastEnabled(!profile.isBroadcastEnabled());
                plugin.getProfileManager().saveAll();
                session.playSound(player, Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                plugin.getSubMenusGui().openSettings(player, session);
            }
            case 16 -> {
                profile.setTheme(profile.getTheme().next());
                plugin.getProfileManager().saveAll();
                session.playSound(player, Sound.UI_BUTTON_CLICK, 0.8f, 1.3f);
                plugin.getSubMenusGui().openSettings(player, session);
            }
            case 22 -> {
                session.playSound(player, Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
                plugin.getMainUpgraderGui().open(player, session);
            }
            default -> {
            }
        }
    }

    private void handleFavoritesClick(Player player, UpgraderSession session, int slot, InventoryClickEvent event) {
        if (slot == 49) {
            session.playSound(player, Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
            plugin.getMainUpgraderGui().open(player, session);
            return;
        }
        ItemStack clicked = event.getCurrentItem();
        if (clicked != null && !clicked.getType().isAir() && clicked.hasItemMeta()) {
            PersistentDataContainer pdc = clicked.getItemMeta().getPersistentDataContainer();
            String rewardId = pdc.get(plugin.getRewardBrowserGui().getRewardIdKey(), PersistentDataType.STRING);
            Integer qty = pdc.get(plugin.getRewardBrowserGui().getRewardQtyKey(), PersistentDataType.INTEGER);
            if (rewardId != null) {
                if (event.isRightClick()) {
                    plugin.getProfileManager().getProfile(player).toggleFavorite(rewardId);
                    plugin.getProfileManager().saveAll();
                    session.playSound(player, Sound.UI_BUTTON_CLICK, 0.8f, 0.9f);
                    plugin.getSubMenusGui().openFavorites(player, session);
                } else {
                    RewardItem item = plugin.getConfigManager().getRewardById(rewardId);
                    if (item != null) {
                        session.setTargetReward(item);
                        session.setTargetQuantity(qty != null ? qty : 1);
                        session.setSpinState(UpgraderSession.SpinState.IDLE);
                        session.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                        plugin.getMainUpgraderGui().open(player, session);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChatSearch(AsyncChatEvent event) {
        Player player = event.getPlayer();
        UpgraderSession session = plugin.getSessionManager().get(player.getUniqueId());
        if (session == null || !session.isAwaitingChatSearch()) {
            return;
        }
        event.setCancelled(true);
        session.setAwaitingChatSearch(false);

        String msg = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();
        if (!msg.equalsIgnoreCase("cancel") && !msg.equalsIgnoreCase("huy")) {
            session.setSearchQuery(msg);
            session.setBrowserPage(0);
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                plugin.getRewardBrowserGui().open(player, session);
            }
        });
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        UpgraderSession session = plugin.getSessionManager().get(player.getUniqueId());
        if (session == null) return;
        if (session.isTransitioning() || session.isAwaitingChatSearch()) {
            return;
        }
        if (event.getInventory().equals(session.getActiveInventory())) {
            // Trả lại toàn bộ đồ cược và trang bị nếu người chơi đóng bảng
            session.returnAllItems(player);
            session.setActiveInventory(null);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UpgraderSession session = plugin.getSessionManager().remove(player.getUniqueId());
        if (session != null) {
            session.returnAllItems(player);
        }
    }
}
