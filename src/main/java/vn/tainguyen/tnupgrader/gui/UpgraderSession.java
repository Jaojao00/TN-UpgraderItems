package vn.tainguyen.tnupgrader.gui;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import vn.tainguyen.tnupgrader.TNUpgraderPlugin;
import vn.tainguyen.tnupgrader.model.ItemCategory;
import vn.tainguyen.tnupgrader.model.NativeEnchantment;
import vn.tainguyen.tnupgrader.model.RewardItem;
import vn.tainguyen.tnupgrader.model.SortOrder;
import vn.tainguyen.tnupgrader.model.UpgradeAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UpgraderSession {

    public enum MenuType {
        MAIN_UPGRADER,
        REWARD_BROWSER,
        EQUIPMENT_UPGRADER,
        SETTINGS,
        STATISTICS,
        THEME_PICKER,
        FAVORITES,
        GUIDE
    }

    public enum SpinState {
        IDLE,
        SPINNING,
        RESULT_WIN,
        RESULT_LOSS
    }

    public enum UpgradeTab {
        ATTRIBUTES,
        VANILLA_ENCHANTS,
        NATIVE_RUNES
    }

    private final TNUpgraderPlugin plugin;
    private final UUID playerUuid;
    private MenuType currentMenu = MenuType.MAIN_UPGRADER;
    private Inventory activeInventory;
    private boolean transitioning = false;
    private boolean awaitingChatSearch = false;

    // --- Chế độ 1: Đấu Giá Nâng Cấp Vật Phẩm (3x3 Đồ Cược) ---
    private final ItemStack[] wagerItems = new ItemStack[9];
    private RewardItem targetReward = null;
    private int targetQuantity = 1;
    private int selectedMultiplier = 1;

    // --- Trạng thái Vòng Quay ---
    private SpinState spinState = SpinState.IDLE;
    private BukkitTask spinTask = null;
    private int currentWheelIndex = 0;
    private double lastRolledNumber = 0.0;
    private double lockedSuccessChance = 0.0;

    // --- Bộ lọc Menu Tìm Phần Thưởng ---
    private String searchQuery = "";
    private ItemCategory categoryFilter = ItemCategory.ALL;
    private SortOrder sortOrder = SortOrder.PRICE_ASC;
    private int browserPage = 0;

    // --- Chế độ 2: Cường Hóa Trang Bị End-Game ---
    private ItemStack equipmentItem = null;
    private final ItemStack[] materialItems = new ItemStack[4];
    private UpgradeAttribute selectedAttribute = UpgradeAttribute.ATTACK_DAMAGE;
    private Enchantment selectedEnchantment = null;
    private NativeEnchantment selectedNativeEnchant = NativeEnchantment.VEIN_MINER;
    private UpgradeTab upgradeTab = UpgradeTab.ATTRIBUTES;
    private int enchantPage = 0;

    public UpgraderSession(TNUpgraderPlugin plugin, UUID playerUuid) {
        this.plugin = plugin;
        this.playerUuid = playerUuid;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(playerUuid);
    }

    public MenuType getCurrentMenu() {
        return currentMenu;
    }

    public void setCurrentMenu(MenuType currentMenu) {
        this.currentMenu = currentMenu;
    }

    public Inventory getActiveInventory() {
        return activeInventory;
    }

    public void setActiveInventory(Inventory activeInventory) {
        this.activeInventory = activeInventory;
    }

    public boolean isTransitioning() {
        return transitioning;
    }

    public void setTransitioning(boolean transitioning) {
        this.transitioning = transitioning;
    }

    public boolean isAwaitingChatSearch() {
        return awaitingChatSearch;
    }

    public void setAwaitingChatSearch(boolean awaitingChatSearch) {
        this.awaitingChatSearch = awaitingChatSearch;
    }

    // ==========================================================================
    // QUẢN LÝ LƯỚI ĐỒ CƯỢC 3x3 (CHẾ ĐỘ 1)
    // ==========================================================================
    public ItemStack[] getWagerItems() {
        return wagerItems;
    }

    public int getTotalWagerItemCount() {
        int count = 0;
        for (ItemStack item : wagerItems) {
            if (item != null && !item.getType().isAir()) {
                count += item.getAmount();
            }
        }
        return count;
    }

    public double getTotalWagerValue() {
        double total = 0.0;
        for (ItemStack item : wagerItems) {
            if (item != null && !item.getType().isAir()) {
                total += plugin.getConfigManager().getStackValue(item);
            }
        }
        return total;
    }

    public boolean addWagerItem(ItemStack toAdd) {
        if (toAdd == null || toAdd.getType().isAir()) return false;
        // Gộp vào ô cùng loại trước nếu còn chỗ
        for (int i = 0; i < wagerItems.length; i++) {
            ItemStack existing = wagerItems[i];
            if (existing != null && existing.isSimilar(toAdd) && existing.getAmount() < existing.getMaxStackSize()) {
                int canAdd = existing.getMaxStackSize() - existing.getAmount();
                int move = Math.min(canAdd, toAdd.getAmount());
                existing.setAmount(existing.getAmount() + move);
                toAdd.setAmount(toAdd.getAmount() - move);
                if (toAdd.getAmount() <= 0) {
                    return true;
                }
            }
        }
        // Đặt vào ô trống
        for (int i = 0; i < wagerItems.length; i++) {
            if (wagerItems[i] == null || wagerItems[i].getType().isAir()) {
                wagerItems[i] = toAdd.clone();
                toAdd.setAmount(0);
                return true;
            }
        }
        return false;
    }

    public void clearWagerWithoutReturn() {
        for (int i = 0; i < wagerItems.length; i++) {
            wagerItems[i] = null;
        }
    }

    public void returnWagerItems(Player player) {
        if (player == null) return;
        for (int i = 0; i < wagerItems.length; i++) {
            ItemStack item = wagerItems[i];
            if (item != null && !item.getType().isAir()) {
                wagerItems[i] = null;
                giveItemSafely(player, item);
            }
        }
    }

    // ==========================================================================
    // QUẢN LÝ NGUYÊN LIỆU & TRANG BỊ (CHẾ ĐỘ 2 - CƯỜNG HÓA TRANG BỊ)
    // ==========================================================================
    public ItemStack getEquipmentItem() {
        return equipmentItem;
    }

    public void setEquipmentItem(ItemStack equipmentItem) {
        this.equipmentItem = equipmentItem;
    }

    public ItemStack[] getMaterialItems() {
        return materialItems;
    }

    public double getTotalMaterialValue() {
        double total = 0.0;
        for (ItemStack item : materialItems) {
            if (item != null && !item.getType().isAir()) {
                total += plugin.getConfigManager().getStackValue(item);
            }
        }
        return total;
    }

    public boolean addMaterialItem(ItemStack toAdd) {
        if (toAdd == null || toAdd.getType().isAir()) return false;
        for (int i = 0; i < materialItems.length; i++) {
            ItemStack existing = materialItems[i];
            if (existing != null && existing.isSimilar(toAdd) && existing.getAmount() < existing.getMaxStackSize()) {
                int canAdd = existing.getMaxStackSize() - existing.getAmount();
                int move = Math.min(canAdd, toAdd.getAmount());
                existing.setAmount(existing.getAmount() + move);
                toAdd.setAmount(toAdd.getAmount() - move);
                if (toAdd.getAmount() <= 0) return true;
            }
        }
        for (int i = 0; i < materialItems.length; i++) {
            if (materialItems[i] == null || materialItems[i].getType().isAir()) {
                materialItems[i] = toAdd.clone();
                toAdd.setAmount(0);
                return true;
            }
        }
        return false;
    }

    public void clearMaterialsWithoutReturn() {
        for (int i = 0; i < materialItems.length; i++) {
            materialItems[i] = null;
        }
    }

    public void returnEquipmentAndMaterials(Player player) {
        if (player == null) return;
        if (equipmentItem != null && !equipmentItem.getType().isAir()) {
            ItemStack eq = equipmentItem;
            equipmentItem = null;
            giveItemSafely(player, eq);
        }
        for (int i = 0; i < materialItems.length; i++) {
            ItemStack mat = materialItems[i];
            if (mat != null && !mat.getType().isAir()) {
                materialItems[i] = null;
                giveItemSafely(player, mat);
            }
        }
    }

    public void returnAllItems(Player player) {
        cancelSpinTask();
        returnWagerItems(player);
        returnEquipmentAndMaterials(player);
    }

    public static void giveItemSafely(Player player, ItemStack item) {
        if (item == null || item.getType().isAir()) return;
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
        for (ItemStack left : leftovers.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), left);
        }
    }

    public static void giveRewardQuantity(Player player, RewardItem reward, int totalQuantity) {
        int remaining = Math.max(1, totalQuantity);
        while (remaining > 0) {
            ItemStack proto = reward.createItemStack(1);
            int maxStack = Math.max(1, proto.getMaxStackSize());
            int batch = Math.min(remaining, maxStack);
            ItemStack give = reward.createItemStack(batch);
            giveItemSafely(player, give);
            remaining -= batch;
        }
    }

    // ==========================================================================
    // GETTERS & SETTERS CHO MỤC TIÊU, BỘ LỌC & VÒNG QUAY
    // ==========================================================================
    public RewardItem getTargetReward() {
        return targetReward;
    }

    public void setTargetReward(RewardItem targetReward) {
        this.targetReward = targetReward;
    }

    public int getTargetQuantity() {
        return targetQuantity;
    }

    public void setTargetQuantity(int targetQuantity) {
        this.targetQuantity = Math.max(1, Math.min(6400, targetQuantity));
    }

    public int getSelectedMultiplier() {
        return selectedMultiplier;
    }

    public void setSelectedMultiplier(int selectedMultiplier) {
        this.selectedMultiplier = Math.max(1, Math.min(10, selectedMultiplier));
    }

    public double getCurrentSuccessChance() {
        if (targetReward == null) return 0.0;
        double wager = getTotalWagerValue();
        return targetReward.calculateChance(
                wager,
                targetQuantity,
                plugin.getConfigManager().getChanceMultiplier(),
                plugin.getConfigManager().getMaxSuccessChance()
        );
    }

    public SpinState getSpinState() {
        return spinState;
    }

    public void setSpinState(SpinState spinState) {
        this.spinState = spinState;
    }

    public BukkitTask getSpinTask() {
        return spinTask;
    }

    public void setSpinTask(BukkitTask spinTask) {
        this.spinTask = spinTask;
    }

    public void cancelSpinTask() {
        if (spinTask != null) {
            spinTask.cancel();
            spinTask = null;
        }
        if (spinState == SpinState.SPINNING) {
            spinState = SpinState.IDLE;
        }
    }

    public int getCurrentWheelIndex() {
        return currentWheelIndex;
    }

    public void setCurrentWheelIndex(int currentWheelIndex) {
        this.currentWheelIndex = currentWheelIndex;
    }

    public double getLastRolledNumber() {
        return lastRolledNumber;
    }

    public void setLastRolledNumber(double lastRolledNumber) {
        this.lastRolledNumber = lastRolledNumber;
    }

    public double getLockedSuccessChance() {
        return lockedSuccessChance;
    }

    public void setLockedSuccessChance(double lockedSuccessChance) {
        this.lockedSuccessChance = lockedSuccessChance;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery != null ? searchQuery : "";
    }

    public ItemCategory getCategoryFilter() {
        return categoryFilter;
    }

    public void setCategoryFilter(ItemCategory categoryFilter) {
        this.categoryFilter = categoryFilter;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int getBrowserPage() {
        return browserPage;
    }

    public void setBrowserPage(int browserPage) {
        this.browserPage = Math.max(0, browserPage);
    }

    public UpgradeAttribute getSelectedAttribute() {
        return selectedAttribute;
    }

    public void setSelectedAttribute(UpgradeAttribute selectedAttribute) {
        this.selectedAttribute = selectedAttribute;
    }

    public Enchantment getSelectedEnchantment() {
        return selectedEnchantment;
    }

    public void setSelectedEnchantment(Enchantment selectedEnchantment) {
        this.selectedEnchantment = selectedEnchantment;
    }

    public NativeEnchantment getSelectedNativeEnchant() {
        return selectedNativeEnchant;
    }

    public void setSelectedNativeEnchant(NativeEnchantment selectedNativeEnchant) {
        this.selectedNativeEnchant = selectedNativeEnchant;
    }

    public UpgradeTab getUpgradeTab() {
        return upgradeTab;
    }

    public void setUpgradeTab(UpgradeTab upgradeTab) {
        this.upgradeTab = upgradeTab != null ? upgradeTab : UpgradeTab.ATTRIBUTES;
    }

    public boolean isEnchantUpgradeTab() {
        return upgradeTab == UpgradeTab.VANILLA_ENCHANTS;
    }

    public void setEnchantUpgradeTab(boolean enchantUpgradeTab) {
        this.upgradeTab = enchantUpgradeTab ? UpgradeTab.VANILLA_ENCHANTS : UpgradeTab.ATTRIBUTES;
    }

    public int getEnchantPage() {
        return enchantPage;
    }

    public void setEnchantPage(int enchantPage) {
        this.enchantPage = Math.max(0, enchantPage);
    }

    public void playSound(Player player, Sound sound, float volume, float pitch) {
        if (player == null) return;
        if (plugin.getProfileManager().getProfile(player).isSoundEnabled()) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    public List<RewardItem> getFilteredRewards() {
        List<RewardItem> all = plugin.getConfigManager().getRewardList();
        List<RewardItem> filtered = new ArrayList<>();
        double wager = getTotalWagerValue();

        for (RewardItem item : all) {
            if (categoryFilter != ItemCategory.ALL && item.getCategory() != categoryFilter) {
                continue;
            }
            if (!item.matchesSearch(searchQuery)) {
                continue;
            }
            filtered.add(item);
        }

        double mult = plugin.getConfigManager().getChanceMultiplier();
        double maxChance = plugin.getConfigManager().getMaxSuccessChance();

        filtered.sort((a, b) -> switch (sortOrder) {
            case PRICE_ASC -> Double.compare(a.getUnitValue(), b.getUnitValue());
            case PRICE_DESC -> Double.compare(b.getUnitValue(), a.getUnitValue());
            case CHANCE_DESC -> {
                int qa = a.getSuggestedQuantity(wager);
                int qb = b.getSuggestedQuantity(wager);
                double ca = a.calculateChance(wager, qa, mult, maxChance);
                double cb = b.calculateChance(wager, qb, mult, maxChance);
                yield Double.compare(cb, ca);
            }
            case NAME_ASC -> a.getDisplayName().compareToIgnoreCase(b.getDisplayName());
        });

        return filtered;
    }
}
