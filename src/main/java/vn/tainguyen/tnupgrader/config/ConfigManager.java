package vn.tainguyen.tnupgrader.config;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import vn.tainguyen.tnupgrader.TNUpgraderPlugin;
import vn.tainguyen.tnupgrader.model.ItemCategory;
import vn.tainguyen.tnupgrader.model.RewardItem;
import vn.tainguyen.tnupgrader.util.ItemBuilder;
import vn.tainguyen.tnupgrader.util.TextUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConfigManager {

    private final TNUpgraderPlugin plugin;
    private final NamespacedKey specialItemKey;
    private final NamespacedKey customValueKey;

    private final Map<Material, Double> materialValues = new EnumMap<>(Material.class);
    private final Map<String, Double> mythicValues = new LinkedHashMap<>();
    private final Map<String, RewardItem> rewardCatalog = new LinkedHashMap<>();

    private String prefix;
    private boolean requireBlockOrTool;
    private double maxSuccessChance;
    private double chanceMultiplier;
    private double broadcastMinValue;
    private double broadcastMaxChance;
    private boolean peOneTapMode;

    public ConfigManager(TNUpgraderPlugin plugin) {
        this.plugin = plugin;
        this.specialItemKey = new NamespacedKey(plugin, "special_type");
        this.customValueKey = new NamespacedKey(plugin, "custom_unit_value");
        reload();
    }

    public final void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        this.prefix = plugin.getConfig().getString("settings.prefix", "&8[&6&lTN-Upgrader&8] &r");
        this.requireBlockOrTool = plugin.getConfig().getBoolean("settings.require-block-or-tool", false);
        this.maxSuccessChance = plugin.getConfig().getDouble("settings.max-success-chance", 100.0);
        this.chanceMultiplier = plugin.getConfig().getDouble("settings.chance-multiplier", 1.0);
        this.broadcastMinValue = plugin.getConfig().getDouble("settings.broadcast-min-value", 4000.0);
        this.broadcastMaxChance = plugin.getConfig().getDouble("settings.broadcast-max-chance", 35.0);
        this.peOneTapMode = plugin.getConfig().getBoolean("settings.pe-one-tap-mode", true);

        loadItemValues();
        buildRewardCatalog();
    }

    private void loadItemValues() {
        materialValues.clear();
        mythicValues.clear();

        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("item-values");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                Material mat = Material.matchMaterial(key);
                if (mat != null && mat.isItem() && !mat.isAir()) {
                    double val = sec.getDouble(key, 0.0);
                    if (val > 0) {
                        materialValues.put(mat, val);
                    }
                }
            }
        }

        ConfigurationSection mythicSec = plugin.getConfig().getConfigurationSection("hooks.mythicmobs.items");
        if (mythicSec != null) {
            for (String mythicId : mythicSec.getKeys(false)) {
                double val = mythicSec.getDouble(mythicId, 1500.0);
                if (val > 0) {
                    mythicValues.put(mythicId, val);
                }
            }
        }
    }

    public void buildRewardCatalog() {
        rewardCatalog.clear();

        // 1. Vật phẩm Vanilla trong bảng giá
        for (Map.Entry<Material, Double> entry : materialValues.entrySet()) {
            Material mat = entry.getKey();
            double val = entry.getValue();
            String id = "vanilla:" + mat.name().toLowerCase(Locale.ROOT);
            String vnName = TextUtil.getVietnameseName(mat);
            ItemCategory cat = ItemCategory.fromMaterial(mat);
            RewardItem reward = new RewardItem(
                    id,
                    vnName,
                    mat,
                    new ItemStack(mat),
                    val,
                    cat,
                    RewardItem.SourceType.VANILLA
            );
            rewardCatalog.put(id, reward);
        }

        // 2. Vật phẩm MythicMobs
        if (plugin.getMythicMobsHook() != null && plugin.getConfig().getBoolean("hooks.mythicmobs.enabled", true)) {
            for (Map.Entry<String, Double> entry : mythicValues.entrySet()) {
                String mythicId = entry.getKey();
                double val = entry.getValue();
                ItemStack stack = plugin.getMythicMobsHook().getMythicItemStack(mythicId, 1);
                String displayName = plugin.getMythicMobsHook().getDisplayName(stack, mythicId);
                String id = "mythic:" + mythicId.toLowerCase(Locale.ROOT);
                RewardItem reward = new RewardItem(
                        id,
                        displayName,
                        stack.getType(),
                        stack,
                        val,
                        ItemCategory.SPECIAL,
                        RewardItem.SourceType.MYTHICMOBS
                );
                rewardCatalog.put(id, reward);
            }
        }

        // 3. Sách Phù Phép Cao Cấp (Vanilla & ExcellentEnchants)
        if (plugin.getExcellentEnchantsHook() != null
                && plugin.getConfig().getBoolean("hooks.excellentenchants.include-books-in-reward-browser", true)) {
            double baseBookVal = plugin.getConfig().getDouble("hooks.excellentenchants.base-custom-book-value", 1800.0);
            for (Enchantment enchant : plugin.getExcellentEnchantsHook().getAllRegisteredEnchantments()) {
                int maxLvl = Math.max(1, enchant.getMaxLevel());
                boolean isCustom = plugin.getExcellentEnchantsHook().isCustomEnchantment(enchant);
                // Đưa tất cả sách ExcellentEnchants và các sách Vanilla quan trọng vào kho phần thưởng
                if (isCustom || isFeaturedVanillaEnchant(enchant)) {
                    double bookVal = isCustom ? baseBookVal : (baseBookVal * 0.65);
                    ItemStack bookStack = plugin.getExcellentEnchantsHook().createEnchantedBook(enchant, maxLvl);
                    String enchantName = plugin.getExcellentEnchantsHook().getEnchantDisplayName(enchant)
                            + " " + plugin.getExcellentEnchantsHook().toRoman(maxLvl);
                    String id = "enchant:" + enchant.getKey().getKey().toLowerCase(Locale.ROOT);
                    RewardItem reward = new RewardItem(
                            id,
                            "Sách " + enchantName,
                            Material.ENCHANTED_BOOK,
                            bookStack,
                            bookVal,
                            ItemCategory.SPECIAL,
                            RewardItem.SourceType.EXCELLENT_ENCHANT
                    );
                    rewardCatalog.put(id, reward);
                }
            }
        }
    }

    private boolean isFeaturedVanillaEnchant(Enchantment enchant) {
        String key = enchant.getKey().getKey().toLowerCase(Locale.ROOT);
        return key.equals("sharpness") || key.equals("protection") || key.equals("efficiency")
                || key.equals("fortune") || key.equals("mending") || key.equals("unbreaking")
                || key.equals("looting") || key.equals("power") || key.equals("density")
                || key.equals("breach") || key.equals("wind_burst");
    }

    public CollectionView<RewardItem> getAllRewards() {
        return new CollectionView<>(new ArrayList<>(rewardCatalog.values()));
    }

    public List<RewardItem> getRewardList() {
        return new ArrayList<>(rewardCatalog.values());
    }

    public RewardItem getRewardById(String id) {
        if (id == null) return null;
        return rewardCatalog.get(id.toLowerCase(Locale.ROOT));
    }

    /**
     * Tính giá trị (V) của 1 đơn vị vật phẩm (Chưa nhân số lượng stack)
     */
    public double getUnitValue(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return 0.0;
        }
        if (isUpgraderTool(item) || isUpgraderBlockItem(item)) {
            return 0.0;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.getPersistentDataContainer().has(customValueKey, PersistentDataType.DOUBLE)) {
            Double custom = meta.getPersistentDataContainer().get(customValueKey, PersistentDataType.DOUBLE);
            if (custom != null && custom > 0) {
                return custom;
            }
        }

        // Kiểm tra vật phẩm MythicMobs
        if (plugin.getMythicMobsHook() != null) {
            String mythicId = plugin.getMythicMobsHook().getMythicItemId(item);
            if (mythicId != null) {
                for (Map.Entry<String, Double> entry : mythicValues.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase(mythicId)) {
                        return entry.getValue();
                    }
                }
                return plugin.getConfig().getDouble("hooks.mythicmobs.default-mythic-item-value", 1500.0);
            }
        }

        double base = materialValues.getOrDefault(item.getType(), 0.0);
        if (item.getType() == Material.ENCHANTED_BOOK && meta instanceof EnchantmentStorageMeta storageMeta) {
            if (!storageMeta.getStoredEnchants().isEmpty()) {
                base = Math.max(base, 400.0);
                for (Map.Entry<Enchantment, Integer> ench : storageMeta.getStoredEnchants().entrySet()) {
                    base += ench.getValue() * 150.0;
                }
            }
        }

        // Cộng thêm giá trị nếu trang bị đã có Phù Phép hoặc Cấp Cường Hóa End-Game
        if (base > 0 && meta != null) {
            if (meta.hasEnchants()) {
                for (int lvl : meta.getEnchants().values()) {
                    base += lvl * 45.0;
                }
            }
            if (plugin.getEquipmentManager() != null) {
                int totalStars = plugin.getEquipmentManager().getTotalUpgradeStars(item);
                if (totalStars > 0) {
                    base += totalStars * 350.0;
                }
            }
        }
        return base;
    }

    /**
     * Tính tổng giá trị (V) của cả cụm ItemStack (Đơn giá * Số lượng)
     */
    public double getStackValue(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return 0.0;
        }
        return getUnitValue(item) * item.getAmount();
    }

    /**
     * Tính tổng giá trị (V) của toàn bộ vật phẩm có thể đặt cược trong túi đồ người chơi
     */
    public double getPlayerInventoryValue(Player player) {
        double total = 0.0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item != null && !item.getType().isAir()) {
                total += getStackValue(item);
            }
        }
        return total;
    }

    public void setMaterialValue(Material material, double value) {
        materialValues.put(material, value);
        plugin.getConfig().set("item-values." + material.name(), value);
        plugin.saveConfig();
        buildRewardCatalog();
    }

    // ==========================================================================
    // KHỐI ĐÀI NÂNG CẤP & SỔ TAY NÂNG CẤP DI ĐỘNG
    // ==========================================================================
    public ItemStack createUpgraderBlockItem(int amount) {
        String matName = plugin.getConfig().getString("crafting.upgrader-block.material", "ANVIL");
        Material mat = Material.matchMaterial(matName);
        if (mat == null) mat = Material.ANVIL;
        String name = plugin.getConfig().getString("crafting.upgrader-block.name", "&6&l⚒ ĐÀI NÂNG CẤP TỐI THƯỢNG ⚒");
        List<String> lore = plugin.getConfig().getStringList("crafting.upgrader-block.lore");
        return new ItemBuilder(mat, amount)
                .name(name)
                .lore(lore)
                .glow(true)
                .pdc(specialItemKey, "UPGRADER_BLOCK")
                .build();
    }

    public ItemStack createPortableToolItem(int amount) {
        String matName = plugin.getConfig().getString("crafting.portable-tool.material", "ENCHANTED_BOOK");
        Material mat = Material.matchMaterial(matName);
        if (mat == null) mat = Material.ENCHANTED_BOOK;
        String name = plugin.getConfig().getString("crafting.portable-tool.name", "&e&l⚙ SỔ TAY NÂNG CẤP DI ĐỘNG ⚙");
        List<String> lore = plugin.getConfig().getStringList("crafting.portable-tool.lore");
        return new ItemBuilder(mat, amount)
                .name(name)
                .lore(lore)
                .glow(true)
                .pdc(specialItemKey, "PORTABLE_TOOL")
                .build();
    }

    public boolean isUpgraderBlockItem(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return false;
        String val = item.getItemMeta().getPersistentDataContainer().get(specialItemKey, PersistentDataType.STRING);
        return "UPGRADER_BLOCK".equals(val);
    }

    public boolean isUpgraderTool(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return false;
        String val = item.getItemMeta().getPersistentDataContainer().get(specialItemKey, PersistentDataType.STRING);
        return "PORTABLE_TOOL".equals(val);
    }

    public void registerRecipes() {
        NamespacedKey blockKey = new NamespacedKey(plugin, "upgrader_block_recipe");
        NamespacedKey toolKey = new NamespacedKey(plugin, "portable_tool_recipe");
        Bukkit.removeRecipe(blockKey);
        Bukkit.removeRecipe(toolKey);

        if (plugin.getConfig().getBoolean("crafting.upgrader-block.enabled", true)) {
            ShapedRecipe blockRecipe = new ShapedRecipe(blockKey, createUpgraderBlockItem(1));
            List<String> shape = plugin.getConfig().getStringList("crafting.upgrader-block.shape");
            if (shape.size() == 3) {
                blockRecipe.shape(shape.get(0), shape.get(1), shape.get(2));
                ConfigurationSection ingSec = plugin.getConfig().getConfigurationSection("crafting.upgrader-block.ingredients");
                if (ingSec != null) {
                    for (String k : ingSec.getKeys(false)) {
                        Material m = Material.matchMaterial(ingSec.getString(k, "AIR"));
                        if (m != null && !k.isEmpty()) {
                            blockRecipe.setIngredient(k.charAt(0), m);
                        }
                    }
                }
                Bukkit.addRecipe(blockRecipe);
            }
        }

        if (plugin.getConfig().getBoolean("crafting.portable-tool.enabled", true)) {
            ShapedRecipe toolRecipe = new ShapedRecipe(toolKey, createPortableToolItem(1));
            List<String> shape = plugin.getConfig().getStringList("crafting.portable-tool.shape");
            if (shape.size() == 3) {
                toolRecipe.shape(shape.get(0), shape.get(1), shape.get(2));
                ConfigurationSection ingSec = plugin.getConfig().getConfigurationSection("crafting.portable-tool.ingredients");
                if (ingSec != null) {
                    for (String k : ingSec.getKeys(false)) {
                        Material m = Material.matchMaterial(ingSec.getString(k, "AIR"));
                        if (m != null && !k.isEmpty()) {
                            toolRecipe.setIngredient(k.charAt(0), m);
                        }
                    }
                }
                Bukkit.addRecipe(toolRecipe);
            }
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean isRequireBlockOrTool() {
        return requireBlockOrTool;
    }

    public double getMaxSuccessChance() {
        return maxSuccessChance;
    }

    public double getChanceMultiplier() {
        return chanceMultiplier;
    }

    public double getBroadcastMinValue() {
        return broadcastMinValue;
    }

    public double getBroadcastMaxChance() {
        return broadcastMaxChance;
    }

    public boolean isPeOneTapMode() {
        return peOneTapMode;
    }

    public static class CollectionView<T> {
        private final List<T> list;
        public CollectionView(List<T> list) {
            this.list = Collections.unmodifiableList(list);
        }
        public List<T> asList() {
            return list;
        }
    }
}
