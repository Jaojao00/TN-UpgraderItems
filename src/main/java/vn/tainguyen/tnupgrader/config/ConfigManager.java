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
import vn.tainguyen.tnupgrader.hook.ExcellentEnchantsHook;
import vn.tainguyen.tnupgrader.manager.EquipmentManager;
import vn.tainguyen.tnupgrader.model.ItemCategory;
import vn.tainguyen.tnupgrader.model.NativeEnchantment;
import vn.tainguyen.tnupgrader.model.RewardItem;
import vn.tainguyen.tnupgrader.model.UpgradeAttribute;
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

        // 2. HỆ SINH THÁI VẬT PHẨM & TRANG BỊ ĐỘC QUYỀN TN-UPGRADER (Hoạt động độc lập 100%)
        registerNativeEcosystemItems();

        // 3. Sách Phù Phép Cơ Bản (Hiệu Suất, Chậm Hỏng, Đào Nhanh, Gia Tài, Sắc Bén, Bảo Vệ...)
        if (plugin.getExcellentEnchantsHook() != null
                && plugin.getConfig().getBoolean("hooks.excellentenchants.include-books-in-reward-browser", true)) {
            double baseBookVal = plugin.getConfig().getDouble("hooks.excellentenchants.base-custom-book-value", 1800.0);
            for (Enchantment enchant : plugin.getExcellentEnchantsHook().getAllRegisteredEnchantments()) {
                int maxLvl = Math.max(1, enchant.getMaxLevel());
                boolean isCustom = plugin.getExcellentEnchantsHook().isCustomEnchantment(enchant);
                if (isCustom || isFeaturedVanillaEnchant(enchant)) {
                    double bookVal = isCustom ? baseBookVal : Math.round(baseBookVal * 0.65);
                    ItemStack bookStack = plugin.getExcellentEnchantsHook().createEnchantedBook(enchant, maxLvl);
                    String enchantName = plugin.getExcellentEnchantsHook().getEnchantDisplayName(enchant)
                            + " " + ExcellentEnchantsHook.toRoman(maxLvl);
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

        // 4. (Tùy chọn) Vật phẩm MythicMobs nếu server có cài MythicMobs
        if (plugin.getMythicMobsHook() != null && plugin.getMythicMobsHook().isEnabled()) {
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
    }

    /**
     * Khởi tạo hệ sinh thái Đá Cường Hóa, Tinh Thể Cổ Ngữ và Trang Bị Thần Thoại của riêng TN-UpgraderItems
     */
    private void registerNativeEcosystemItems() {
        // A. Đá Cường Hóa & Tinh Thể Nâng Cấp Nội Tại
        ItemStack upgradeStone = createUpgradeStoneItem(1);
        rewardCatalog.put("tn:upgrade_stone", new RewardItem(
                "tn:upgrade_stone",
                "Đá Cường Hóa Tinh Anh",
                Material.AMETHYST_SHARD,
                upgradeStone,
                1000.0,
                ItemCategory.MINERALS,
                RewardItem.SourceType.VANILLA
        ));

        ItemStack runeCrystal = createRuneCrystalItem(1);
        rewardCatalog.put("tn:rune_crystal", new RewardItem(
                "tn:rune_crystal",
                "Tinh Thể Cổ Ngữ Thần Thoại",
                Material.ECHO_SHARD,
                runeCrystal,
                3000.0,
                ItemCategory.MINERALS,
                RewardItem.SourceType.VANILLA
        ));

        ItemStack luckyCharm = createLuckyCharmItem(1);
        rewardCatalog.put("tn:lucky_charm", new RewardItem(
                "tn:lucky_charm",
                "Bùa Đại Cát TN-Upgrader",
                Material.HEART_OF_THE_SEA,
                luckyCharm,
                2500.0,
                ItemCategory.SPECIAL,
                RewardItem.SourceType.VANILLA
        ));

        // B. 4 Trang Bị & Công Cụ Thần Thoại Nội Tại (Tích hợp sẵn Cổ Ngữ & Thuộc Tính Độc Quyền)
        EquipmentManager eqMgr = plugin.getEquipmentManager();
        if (eqMgr != null) {
            // 1. Cuốc Chim Thần Công Khai Sơn
            ItemStack legendPick = new ItemBuilder(Material.NETHERITE_PICKAXE)
                    .name("&b&l⛏ CUỐC CHIM THẦN CÔNG KHAI SƠN")
                    .lore(
                            "&8[Bảo Vật Thần Thoại TN-Upgrader]",
                            "&7Tuyệt tác công cụ khai thác tối thượng,",
                            "&7đào xuyên lòng đất và tự động luyện kim!"
                    )
                    .enchant(Enchantment.EFFICIENCY, 6)
                    .enchant(Enchantment.UNBREAKING, 5)
                    .enchant(Enchantment.FORTUNE, 4)
                    .enchant(Enchantment.MENDING, 1)
                    .build();
            setCustomUnitValue(legendPick, 18000.0);
            eqMgr.upgradeAttribute(legendPick, UpgradeAttribute.ATTACK_SPEED);
            eqMgr.upgradeAttribute(legendPick, UpgradeAttribute.ATTACK_SPEED);
            eqMgr.upgradeNativeEnchant(legendPick, NativeEnchantment.VEIN_MINER);
            eqMgr.upgradeNativeEnchant(legendPick, NativeEnchantment.VEIN_MINER);
            eqMgr.upgradeNativeEnchant(legendPick, NativeEnchantment.DRILL_3X3);
            eqMgr.upgradeNativeEnchant(legendPick, NativeEnchantment.AUTO_SMELT);
            rewardCatalog.put("tn:legend_pickaxe", new RewardItem(
                    "tn:legend_pickaxe",
                    "Cuốc Chim Thần Công Khai Sơn",
                    Material.NETHERITE_PICKAXE,
                    legendPick,
                    18000.0,
                    ItemCategory.TOOLS,
                    RewardItem.SourceType.VANILLA
            ));

            // 2. Huyết Kiếm Đoạt Mệnh
            ItemStack legendSword = new ItemBuilder(Material.NETHERITE_SWORD)
                    .name("&c&l⚔ HUYẾT KIẾM ĐOẠT MỆNH")
                    .lore(
                            "&8[Vũ Khí Thần Thoại TN-Upgrader]",
                            "&7Thanh thần kiếm triệu hồi thiên lôi",
                            "&7và hút sinh lực kẻ địch trong nháy mắt!"
                    )
                    .enchant(Enchantment.SHARPNESS, 6)
                    .enchant(Enchantment.UNBREAKING, 5)
                    .enchant(Enchantment.LOOTING, 4)
                    .enchant(Enchantment.MENDING, 1)
                    .build();
            setCustomUnitValue(legendSword, 20000.0);
            eqMgr.upgradeAttribute(legendSword, UpgradeAttribute.ATTACK_DAMAGE);
            eqMgr.upgradeAttribute(legendSword, UpgradeAttribute.LIFESTEAL);
            eqMgr.upgradeAttribute(legendSword, UpgradeAttribute.CRIT_STRIKE);
            eqMgr.upgradeNativeEnchant(legendSword, NativeEnchantment.THUNDER_STRIKE);
            eqMgr.upgradeNativeEnchant(legendSword, NativeEnchantment.EXECUTIONER);
            rewardCatalog.put("tn:legend_sword", new RewardItem(
                    "tn:legend_sword",
                    "Huyết Kiếm Đoạt Mệnh",
                    Material.NETHERITE_SWORD,
                    legendSword,
                    20000.0,
                    ItemCategory.EQUIPMENT,
                    RewardItem.SourceType.VANILLA
            ));

            // 3. Chiến Rìu Băng Lôi
            ItemStack legendAxe = new ItemBuilder(Material.NETHERITE_AXE)
                    .name("&e&l🪓 CHIẾN RÌU BĂNG LÔI KHAI THIÊN")
                    .lore(
                            "&8[Vũ Khí & Công Cụ Thần Thoại TN-Upgrader]",
                            "&7Vừa chặt đổ cả rừng cây trong 1 nhát,",
                            "&7vừa đóng băng và kết liễu kẻ địch!"
                    )
                    .enchant(Enchantment.SHARPNESS, 6)
                    .enchant(Enchantment.EFFICIENCY, 6)
                    .enchant(Enchantment.UNBREAKING, 5)
                    .build();
            setCustomUnitValue(legendAxe, 17500.0);
            eqMgr.upgradeAttribute(legendAxe, UpgradeAttribute.ATTACK_DAMAGE);
            eqMgr.upgradeAttribute(legendAxe, UpgradeAttribute.TRUE_DAMAGE);
            eqMgr.upgradeNativeEnchant(legendAxe, NativeEnchantment.VEIN_MINER);
            eqMgr.upgradeNativeEnchant(legendAxe, NativeEnchantment.FROST_BITE);
            rewardCatalog.put("tn:legend_axe", new RewardItem(
                    "tn:legend_axe",
                    "Chiến Rìu Băng Lôi Khai Thiên",
                    Material.NETHERITE_AXE,
                    legendAxe,
                    17500.0,
                    ItemCategory.EQUIPMENT,
                    RewardItem.SourceType.VANILLA
            ));

            // 4. Chiến Giáp Hộ Thần Bất Diệt
            ItemStack legendChest = new ItemBuilder(Material.NETHERITE_CHESTPLATE)
                    .name("&6&l🛡 CHIẾN GIÁP HỘ THẦN BẤT DIỆT")
                    .lore(
                            "&8[Giáp Thần Thoại TN-Upgrader]",
                            "&7Áo giáp hộ thể tự động hồi phục máu",
                            "&7và phản lửa thiêu rụi kẻ tấn công!"
                    )
                    .enchant(Enchantment.PROTECTION, 5)
                    .enchant(Enchantment.UNBREAKING, 5)
                    .enchant(Enchantment.MENDING, 1)
                    .build();
            setCustomUnitValue(legendChest, 22000.0);
            eqMgr.upgradeAttribute(legendChest, UpgradeAttribute.MAX_HEALTH);
            eqMgr.upgradeAttribute(legendChest, UpgradeAttribute.MAX_HEALTH);
            eqMgr.upgradeAttribute(legendChest, UpgradeAttribute.ARMOR);
            eqMgr.upgradeNativeEnchant(legendChest, NativeEnchantment.VITALITY_REGEN);
            eqMgr.upgradeNativeEnchant(legendChest, NativeEnchantment.MOLTEN_SHIELD);
            eqMgr.upgradeNativeEnchant(legendChest, NativeEnchantment.WIND_STEP);
            rewardCatalog.put("tn:legend_chestplate", new RewardItem(
                    "tn:legend_chestplate",
                    "Chiến Giáp Hộ Thần Bất Diệt",
                    Material.NETHERITE_CHESTPLATE,
                    legendChest,
                    22000.0,
                    ItemCategory.EQUIPMENT,
                    RewardItem.SourceType.VANILLA
            ));
        }
    }

    public ItemStack createUpgradeStoneItem(int amount) {
        ItemStack stack = new ItemBuilder(Material.AMETHYST_SHARD, amount)
                .name("&b&l💎 ĐÁ CƯỜNG HÓA TINH ANH")
                .lore(
                        "&8[Nguyên Liệu Độc Quyền TN-Upgrader]",
                        "&7 Tinh thể kết tụ ma lực tinh khiết,",
                        "&7 chuyên dùng để nạp điểm nâng cấp trang bị.",
                        "",
                        "&6▸ Giá trị quy đổi: &e1,000 V / viên",
                        "&a▸ Công dụng: &fĐặt vào Ô Nguyên Liệu Cường Hóa",
                        "&f  hoặc Lưới Đồ Cược 3x3 để tăng mạnh tỉ lệ thắng!"
                )
                .glow(true)
                .build();
        setCustomUnitValue(stack, 1000.0);
        return stack;
    }

    public ItemStack createRuneCrystalItem(int amount) {
        ItemStack stack = new ItemBuilder(Material.ECHO_SHARD, amount)
                .name("&d&l🔮 TINH THỂ CỔ NGỮ THẦN THOẠI")
                .lore(
                        "&8[Nguyên Liệu Cao Cấp TN-Upgrader]",
                        "&7 Chứa đựng sức mạnh cổ ngữ nguyên thủy,",
                        "&7 giúp đột phá giới hạn Vũ khí, Công cụ & Giáp.",
                        "",
                        "&6▸ Giá trị quy đổi: &e3,000 V / viên",
                        "&a▸ Công dụng: &fNạp vào Bàn Cường Hóa Trang Bị",
                        "&f  để nâng cấp Cổ Ngữ Độc Quyền & Thuộc Tính!"
                )
                .glow(true)
                .build();
        setCustomUnitValue(stack, 3000.0);
        return stack;
    }

    public ItemStack createLuckyCharmItem(int amount) {
        ItemStack stack = new ItemBuilder(Material.HEART_OF_THE_SEA, amount)
                .name("&a&l🍀 BÙA ĐẠI CÁT TN-UPGRADER")
                .lore(
                        "&8[Bảo Vật May Mắn TN-Upgrader]",
                        "&7 Tín vật mang lại vận may rực rỡ cho",
                        "&7 các thợ rèn khi nâng cấp trang bị.",
                        "",
                        "&6▸ Giá trị quy đổi: &e2,500 V / bùa"
                )
                .glow(true)
                .build();
        setCustomUnitValue(stack, 2500.0);
        return stack;
    }

    public void setCustomUnitValue(ItemStack item, double value) {
        if (item == null || item.getType().isAir()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(customValueKey, PersistentDataType.DOUBLE, value);
            item.setItemMeta(meta);
        }
    }

    private boolean isFeaturedVanillaEnchant(Enchantment enchant) {
        String key = enchant.getKey().getKey().toLowerCase(Locale.ROOT);
        return !key.contains("curse");
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

        if (plugin.getMythicMobsHook() != null && plugin.getMythicMobsHook().isEnabled()) {
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

    public double getStackValue(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return 0.0;
        }
        return getUnitValue(item) * item.getAmount();
    }

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
        NamespacedKey stoneKey = new NamespacedKey(plugin, "upgrade_stone_recipe");
        Bukkit.removeRecipe(blockKey);
        Bukkit.removeRecipe(toolKey);
        Bukkit.removeRecipe(stoneKey);

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

        // Công thức chế tạo Đá Cường Hóa Tinh Anh (1,000 V) của riêng TN-UpgraderItems
        ShapedRecipe stoneRecipe = new ShapedRecipe(stoneKey, createUpgradeStoneItem(1));
        stoneRecipe.shape("GAG", "ADA", "GAG");
        stoneRecipe.setIngredient('G', Material.GOLD_INGOT);
        stoneRecipe.setIngredient('A', Material.AMETHYST_SHARD);
        stoneRecipe.setIngredient('D', Material.DIAMOND);
        Bukkit.addRecipe(stoneRecipe);
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
